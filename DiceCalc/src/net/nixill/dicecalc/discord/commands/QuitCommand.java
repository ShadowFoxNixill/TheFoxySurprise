package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

public class QuitCommand {
  public static void run(IDiscordClient cli, IMessage msg) {
    if (msg.getAuthor().getLongID() == Config.getCreator()) {
      MessageSender.send(msg.getChannel(), "Goodbye!~");
      Config.write();
      cli.logout();
      System.out.println("The bot is logged out.");
    } else {
      MessageSender.send(msg.getChannel(), "I will not be silenced!");
    }
  }
}
