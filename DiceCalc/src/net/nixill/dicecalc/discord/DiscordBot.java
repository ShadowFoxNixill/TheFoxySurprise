package net.nixill.dicecalc.discord;

import net.nixill.dice.functions.DiceFunction;
import net.nixill.dicecalc.config.Config;
import net.nixill.dicecalc.discord.editables.EditCycler;
import net.nixill.dicecalc.discord.events.MessageEditListener;
import net.nixill.dicecalc.discord.events.MessageListener;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventDispatcher;
import sx.blah.discord.util.DiscordException;

public class DiscordBot {
  private static IDiscordClient cli;
  
  public static void main(String[] args) {
    Config.read();
    DiceFunction.setAlwaysRerun(true);
    String token = Config.getToken();
    try {
      cli = new ClientBuilder().withToken(token).withRecommendedShardCount().login();
      EventDispatcher dis = cli.getDispatcher();
      dis.registerListener(new MessageListener(cli));
      dis.registerListener(new MessageEditListener());
      new Thread(new EditCycler()).start();
    } catch (DiscordException e) {
      e.printStackTrace();
      System.err.println(e.getErrorMessage());
    }
  }
  
  public static IDiscordClient getClient() {
    return cli;
  }
}
