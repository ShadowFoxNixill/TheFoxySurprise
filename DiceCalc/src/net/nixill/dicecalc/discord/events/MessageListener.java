package net.nixill.dicecalc.discord.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nixill.dicecalc.discord.commands.AnnounceCommand;
import net.nixill.dicecalc.discord.commands.BugReportCommand;
import net.nixill.dicecalc.discord.commands.DelCommand;
import net.nixill.dicecalc.discord.commands.DiceCommand;
import net.nixill.dicecalc.discord.commands.HelpCommand;
import net.nixill.dicecalc.discord.commands.NotifyCommand;
import net.nixill.dicecalc.discord.commands.QuitCommand;
import net.nixill.dicecalc.discord.editables.Editables;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;

public class MessageListener implements IListener<MessageReceivedEvent> {
  private IDiscordClient cli;
  
  public MessageListener(IDiscordClient client) {
    cli = client;
    DiceCommand.makeLists();
  }
  
  @Override
  public void handle(MessageReceivedEvent e) {
    IMessage msg = e.getMessage();
    
    String msgText = msg.getFormattedContent();
    String msgLC = msgText.toLowerCase();
    
    boolean mentioned = false;
    String mention = "@";
    if (msg.getGuild() != null) {
      mention += cli.getOurUser().getDisplayName(msg.getGuild());
    } else {
      mention += cli.getOurUser().getName();
    }
    mention = (mention + " ").toLowerCase();
    if (msgLC.startsWith(mention)) {
      mentioned = true;
      msgText = msgText.substring(mention.length());
      msgLC = msgText.toLowerCase();
    }
    if (msg.getChannel().isPrivate()) mentioned = true;
    
    // Match the command against a dice-roll command.
    Matcher mtc = Pattern.compile("^\\!(f|full|)(d|dice|r|roll) .*").matcher(msgLC);
    if (mtc.matches()) {
      String cmdText = null;
      boolean full = true;
      if (mtc.group(1).equals("")) full = false;
      cmdText = msgText.substring(mtc.end(2) + 1);
      Editables.newDiceCommand(cmdText, msg, full);
    }
    
    // Match the command against a delsave command.
    mtc = Pattern.compile("^\\!del(?:save)? ($?)(\\w+)").matcher(msgLC);
    if (mtc.matches()) {
      String cmdText = mtc.group(2);
      boolean global = mtc.group(1).equals("$");
      DelCommand.run(cli, cmdText, global, msg);
    }
    
    // Match the command against a help command.
    mtc = Pattern.compile("^\\!(help)( .*)?").matcher(msgLC);
    if (mtc.matches()) {
      String helpText;
      if (mtc.group(2) != null) {
        helpText = mtc.group(2).substring(1);
      } else {
        helpText = "";
      }
      HelpCommand.run(cli, msg, helpText);
    }
    
    // Match the command against a quit command.
    mtc = Pattern.compile("^\\!(quit)( .*)?").matcher(msgLC);
    if (mtc.matches() && mentioned) {
      QuitCommand.run(cli, msg);
    }
    
    // Match the command against a notify command.
    mtc = Pattern.compile("^\\!(notify) (?:(@.+) )*(on|off|query|toggle)(?: (.+))?$", Pattern.CASE_INSENSITIVE)
        .matcher(msgText);
    if (mtc.matches()) {
      NotifyCommand.run(cli, msg, mentioned, mtc.group(3));
    }
    
    // Match the command against an announce command.
    mtc = Pattern.compile("\\!(announce) (.+)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL).matcher(msgText);
    if (mtc.matches() && mentioned) {
      AnnounceCommand.run(cli, msg, mtc.group(2));
    }
    
    // Match the command against a bug report command.
    mtc = Pattern.compile("\\!(bug|(bug)?report).*").matcher(msgLC);
    if (mtc.matches()) {
      BugReportCommand.run(cli, msg.getChannel(), msg.getAuthor());
    }
  }
}
