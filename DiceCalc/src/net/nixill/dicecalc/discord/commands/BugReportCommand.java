package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.discord.MessageSender;
import net.nixill.dicecalc.discord.editables.BugReport;
import net.nixill.dicecalc.discord.editables.EditCycler;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class BugReportCommand {
  public static void run(IDiscordClient cli, IChannel chan, IUser auth) {
    BugReport bug = EditCycler.getBugReport(chan.getLongID());
    
    if (bug != null) {
      MessageSender.send(auth.getOrCreatePMChannel(),
          "To file a bug report, go to https://github.com/ShadowFoxNixill/TheFoxySurprise/issues/new and put the"
          + "following information in the comments.",
          bug.toEmbed());
    } else {
      MessageSender.send(auth.getOrCreatePMChannel(), "There wasn't a recent bug in " + chan.mention()
          + ", but you can still file a bug report at https://github.com/ShadowFoxNixill/TheFoxySurprise/issues/new");
    }
  }
}
