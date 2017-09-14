package net.nixill.dicecalc.discord.editables;

import java.util.Random;

import net.nixill.dicecalc.discord.MessageSender;
import net.nixill.dicecalc.discord.commands.DiceCommand;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;
import sx.blah.discord.util.RequestBuffer.RequestFuture;

public class Editables {
  private static Random rand = new Random();
  
  public static void newDiceCommand(String cmdText, IMessage msg, boolean full) {
    Record rec = new Record();
    long seed = rand.nextLong();
    
    String out = DiceCommand.run(cmdText, msg, full, seed);
    
    IChannel chan = msg.getChannel();
    
    RequestFuture<IMessage> req = MessageSender.send(chan, out, new RequestBuffer.IRequest<IMessage>() {
      public IMessage request() throws RateLimitException {
        if (!chan.isPrivate()) {
          String out2 = "It looks like I can't speak in " + chan.getGuild().getName() + "/#" + chan.getName() + "\n"
              + "So I'm PMing you your results instead! :3\n\n" + out;
          return msg.getAuthor().getOrCreatePMChannel().sendMessage(out2);
        } else {
          return null;
        }
      }
    });
    
    rec.setOriginalMsg(msg);
    rec.setSeed(seed);
    rec.setResponseMsgReq(req);
    
    EditCycler.newRecord(rec);
  }
  
  public static void editDiceCommand(Record rec, String cmdText, IMessage msg, boolean full) {
    long seed = rec.getSeed();
    
    String out = DiceCommand.run(cmdText, msg, full, seed);
    
    MessageSender.update(rec.getResponseMsg(), out);
  }
}
