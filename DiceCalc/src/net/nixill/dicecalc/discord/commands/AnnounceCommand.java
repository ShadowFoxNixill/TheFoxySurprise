package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RequestBuffer.IRequest;

public class AnnounceCommand {
  public static void run(IDiscordClient cli, IMessage msg, String announcement) {
    if (msg.getAuthor().getLongID() == Config.getCreator()) {
      MessageSender.send(Config.getCreatorPM(), "Attempting to send the following announcement:\n" + announcement);
      for (Long s : Config.notifyList()) {
        MessageSender.send(cli.getChannelByID(s), announcement, new IRequest<IMessage>() {
          @Override
          public IMessage request() {
            Config.removeFromNotifyList(s);
            return null;
          }
        });
      }
      MessageSender.send(Config.getCreatorPM(), "Announcement has been sent.");
    }
  }
}
