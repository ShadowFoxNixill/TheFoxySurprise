package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.obj.PrivateChannel;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

public class NotifyCommand {
  public static void run(IDiscordClient cli, IMessage msg, boolean mentioned, String setting) {
    IChannel chan = msg.getChannel();
    IUser auth = msg.getAuthor();
    
    setting = setting.toLowerCase();
    
    if (msg.getMentions().contains(cli.getOurUser()) || mentioned || chan instanceof PrivateChannel) {
      while (true) {
        if (!(chan instanceof PrivateChannel)) {
          if (chan.getGuild().getOwner().equals(auth)) break;
          if (chan.getModifiedPermissions(auth).contains(Permissions.MANAGE_SERVER)) break;
          return;
        }
      }
      
      String msgText = msg.getContent();
      String rplText = auth.mention() + ": ";
      if (setting.equals("on")) {
        Config.addToNotifyList(chan.getLongID());
        rplText += "I've added this channel to the notify list.\n"
            + "Whenever bot announcements are made by my creator, this channel will receive them.";
      } else if (msgText.toLowerCase().endsWith(" off")) {
        Config.removeFromNotifyList(chan.getLongID());
        rplText += "I've removed this channel from the notify list.\n"
            + "This channel will no longer be included as a recipient for bot announcements.";
      } else if (msgText.toLowerCase().endsWith(" query")) {
        if (Config.isInNotifyList(chan.getLongID())) {
          rplText += "I'm in your notify list.\n"
              + "Whenever bot announcements are made by my creator, this channel will receive them.";
        } else {
          rplText += "I'm not in your notify list.\n" + "This channel won't receive bot announcements from my creator.";
        }
      } else {
        if (Config.isInNotifyList(chan.getLongID())) {
          Config.removeFromNotifyList(chan.getLongID());
          rplText += "I've removed this channel from the notify list.\n"
              + "This channel will no longer be included as a recipient for bot announcements.";
        } else {
          Config.addToNotifyList(chan.getLongID());
          rplText += "I've added this channel to the notify list.\n"
              + "Whenever bot announcements are made by my creator, this channel will receive them.";
        }
      }
      
      String rText = rplText;
      
      if (!chan.isPrivate()) {
        MessageSender.send(chan, rText, new RequestBuffer.IRequest<IMessage>() {
          public IMessage request() throws RateLimitException {
            Config.removeFromNotifyList(chan.getLongID());
            Config.write();
            IChannel pmChan = msg.getAuthor().getOrCreatePMChannel();
            String nowToSend = "It looks like I can't talk in " + chan.getGuild().getName() + "/#" + chan.getName()
                + "\nBecause of this, I can't have that channel on my notify list. I've already removed it for you.";
            return pmChan.sendMessage(nowToSend);
          }
        });
      }
      
      Config.write();
    }
  }
}
