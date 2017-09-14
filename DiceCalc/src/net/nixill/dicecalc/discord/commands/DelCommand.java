package net.nixill.dicecalc.discord.commands;

import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.config.SavedObjects;
import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

public class DelCommand {
  
  public static void run(IDiscordClient cli, String cName, boolean global, IMessage msg) {
    IChannel chan = msg.getChannel();
    IUser user = msg.getAuthor();
    
    if (global) {
      if (user.getLongID() == Config.getCreator()) {
        boolean res = SavedObjects.unsaveGlobal(cName);
        if (res) {
          MessageSender.send(chan, "Success, global command {" + cName + "} is no more.");
        } else {
          MessageSender.send(chan, "Failure; global command {" + cName + "} never existed.");
        }
      } else {
        MessageSender.send(chan, "Sorry, only my creator can delete globals.");
      }
    } else {
      Boolean res = SavedObjects.unsaveObject(chan, cName);
      if (res) {
        MessageSender.send(chan, "Success, the command {" + cName + "} is no more.");
      } else {
        MessageSender.send(chan, "Failure; the command {" + cName + "} never existed in this server.\n"
            + "Maybe you were using a global? Only my creator can delete those.");
      }
    }
  }
  
}
