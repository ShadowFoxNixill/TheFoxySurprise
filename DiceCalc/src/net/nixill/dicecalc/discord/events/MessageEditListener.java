package net.nixill.dicecalc.discord.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nixill.dicecalc.discord.MessageSender;
import net.nixill.dicecalc.discord.editables.EditCycler;
import net.nixill.dicecalc.discord.editables.Editables;
import net.nixill.dicecalc.discord.editables.Record;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageUpdateEvent;
import sx.blah.discord.handle.obj.IMessage;

public class MessageEditListener implements IListener<MessageUpdateEvent> {
  public void handle(MessageUpdateEvent event) {
    IMessage msg = event.getMessage();
    Record rec = EditCycler.getRecord(msg);
    
    if (rec != null) {
      String msgText = msg.getContent();
      String msgLC = msgText.toLowerCase();
      
      Matcher mtc = Pattern.compile("^\\!(f|full|)(d|dice|r|roll) .*").matcher(msgLC);
      if (mtc.matches()) {
        String cmdText = null;
        boolean full = true;
        if (mtc.group(1).equals("")) full = false;
        cmdText = msgText.substring(mtc.end(2) + 1);
        Editables.editDiceCommand(rec, cmdText, msg, full);
      } else if (msgLC.matches("!delete")) {
        MessageSender.delete(rec.getResponseMsg());
        EditCycler.deleteRecord(msg);
      } else {
        MessageSender.update(rec.getResponseMsg(),
            "The request message is no longer a !roll command. Command editing only supports !roll commands. However, "
                + "the message is still linked and can continue to be edited until the end of the five minute period. "
                + "You can also delete the roll entirely by changing your command to `!delete`.");
      }
    }
  }
}
