package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IMessage;

public class HelpCommand {
  public static void run(IDiscordClient cli, IMessage msg, String helpText) {
    MessageSender.send(msg.getChannel(),
        "https://docs.google.com/document/d/1jj7l7qArbZXlWQZKbFhT8abk1lCCMfvl1LuJVPfpQOo/edit");
  }
}
