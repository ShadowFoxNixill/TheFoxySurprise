package net.nixill.dicecalc.discord.commands;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nixill.dice.DiceManager;
import net.nixill.dice.DiceParameter;
import net.nixill.dice.functions.DiceFunction;
import net.nixill.dice.objects.DiceObject;
import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.config.SavedObjects;
import net.nixill.dicecalc.discord.DiscordBot;
import net.nixill.dicecalc.discord.editables.BugReport;
import net.nixill.dicecalc.discord.editables.EditCycler;
import net.nixill.dicecalc.parsers.DiceStringParser;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

public class DiceCommand {
  private static HashMap<String, String> outSD = new HashMap<>();
  private static HashMap<String, String> outTS = new HashMap<>();
  private static HashMap<String, String> outOP = new HashMap<>();
  
  public static String run(String cmdText, IMessage msg, boolean full, long seed) {
    DiceManager.seedRandom(seed);
    
    if (outSD == null) makeLists();
    
    IChannel chan = msg.getChannel();
    IUser user = msg.getAuthor();
    
    StringBuilder output = new StringBuilder("");
    try {
      Matcher cmdMatcher = Pattern.compile("^(.*?)(?:(\\@|\\:)[\\h\\v]*\\{?(\\$?)(\\w*?)\\}?[\\h\\v]*)?(?:\\'(.*))?$")
          .matcher(cmdText);
      
      cmdMatcher.matches();
      String cmdLine = cmdMatcher.group(1);
      String cmdOrRes = cmdMatcher.group(2);
      String cmdGlobal = cmdMatcher.group(3);
      String cmdName = cmdMatcher.group(4);
      String comment = cmdMatcher.group(5);
      if (cmdName != null) cmdName = cmdName.toLowerCase();
      if (comment != null) comment = comment.trim();
      
      DiceFunction.setStringMap(outSD);
      DiceParameter p = DiceStringParser.parse(DiscordBot.getClient(), chan, user, cmdLine);
      if (p != null) {
        DiceFunction.setStringMap(outTS);
        String pString = p.toString();
        output.append("Input: `" + p.toString() + "`");
        if (comment != null) output.append(" (" + comment + ")");
        DiceFunction.setStringMap(outOP);
        DiceObject res = p.get();
        String result = res.toString();
        int position = result.indexOf(" ");
        if (position == -1) position = result.length();
        result = "**" + result.substring(0, position) + "**" + result.substring(position);
        output.append(" / Result: " + result + "\n");
        if (cmdName != null) {
          boolean isGlobal = (cmdGlobal.equals("$"));
          IChannel sChan = chan;
          if (isGlobal) {
            if (user.getLongID() != Config.getCreator()) {
              isGlobal = false;
            } else {
              sChan = null;
            }
          }
          boolean isCmd = (cmdOrRes.equals(":"));
          if (isCmd) {
            SavedObjects.saveObject(sChan, cmdName, p);
            output.append("Saved `" + pString + "` to **{" + cmdName + "}**\n");
          } else {
            SavedObjects.saveObject(sChan, cmdName, res);
            output.append("Saved `" + res + "` to **{" + cmdName + "}**\n");
          }
          if (isGlobal) {
            output.insert(output.length() - 1, " globally");
          }
        }
        if (full) {
          output.append("Rolls:\n");
          ArrayList<String> out = DiceManager.getOutput();
          for (String l : out) {
            output.append(l + "\n");
          }
        }
        DiceManager.clearOutput();
      } else {
        output.append("... It seems you didn't input anything.\n");
        output.append("Maybe you tried to use a saved roll or command that doesn't exist.\n");
        output.append("Anyway, I can't give you any result without an input.");
      }
    } catch (Exception ex) {
      output.append("An error occurred while attempting your command:\n");
      output.append(ex.toString() + "\n");
      if (Config.debug()) {
        for (StackTraceElement e : ex.getStackTrace()) {
          output.append(e.toString() + "\n");
        }
      } else {
        BugReport bug = new BugReport();
        bug.channel = chan.getLongID();
        bug.canSpeak = chan.getModifiedPermissions(DiscordBot.getClient().getOurUser())
            .contains(Permissions.SEND_MESSAGES);
        bug.command = msg.getContent();
        bug.time = LocalDateTime.now();
        bug.isDM = chan.isPrivate();
        bug.error = ex;
        EditCycler.newBugReport(bug);
        output.append(
            "To receive a PM with information on this bug, as well as a link to report it, type `!report` within the "
            + "next five minutes.");
      }
      DiceManager.clearOutput();
    }
    
    output.insert(0, user.mention() + ": ");
    
    return output.toString();
  }
  
  public static void makeLists() {
    outSD.put("ROLL", "(d{sides})");
    outSD.put("LOAD", "\\{{name}\\}");
    
    outTS.put("CONCATENATE", "({left}[negate<=0?+][negate>0?-]{right})");
    outTS.put("DIVIDE", "({left}/{right})");
    outTS.put("DROP_FIRST", "({dice}{oper}{count})");
    outTS.put("DROP_HIGH", "({dice}{oper}{count})");
    outTS.put("DROP_RANGE", "({dice}{oper}{min})");
    outTS.put("INTDIVIDE", "({left}/{right})");
    outTS.put("KEEP_MAX", "({left}m[invert<=0?n][invert>0?x]{right})");
    outTS.put("MODULO", "({left}%{right})");
    outTS.put("EXP", "({left}^{right})");
    outTS.put("REPEAT", "({function}&{count})");
    outTS.put("REROLL", "({dice}{oper}{low})");
    outTS.put("ROLL", "([!solo?{count}]d{sides})");
    outTS.put("TIMES", "({left}*{right})");
    outTS.put("UNTIL", "({sides}u{oper}{stopper})");
    outTS.put("WEIGHTED_ROLL", "({count}w{weights})");
    outTS.put("LOAD", "\\{{name}\\}");
    
    outOP.put("REROLL", "{$dice}{oper}{#low}");
    outOP.put("ROLL", "{#count}d{#sides}");
    outOP.put("UNTIL", "{#sides}u{oper}{#stopper}");
    outOP.put("LOAD", "\\{{name}\\}");
  }
  
  public static void setOutTS() {
    DiceFunction.setStringMap(outTS);
  }
  
  public static void setOutSD() {
    DiceFunction.setStringMap(outSD);
  }
  
  public static void setOutOP() {
    DiceFunction.setStringMap(outOP);
  }
}
