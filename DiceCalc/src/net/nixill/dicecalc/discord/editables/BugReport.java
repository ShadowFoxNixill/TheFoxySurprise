package net.nixill.dicecalc.discord.editables;

import java.time.LocalDateTime;

import net.nixill.dicecalc.config.Config;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

public class BugReport extends Cyclable {
  public String        command;
  public Throwable     error;
  public LocalDateTime time;
  public long          channel;
  public boolean       isDM;
  public boolean       canSpeak;
  
  public EmbedObject toEmbed() {
    EmbedBuilder eb = new EmbedBuilder();
    eb.withTitle("--- THEFOXYSURPRISE BUG REPORT ---\n\n");
    eb.withTimestamp(time);
    eb.withDesc("Error thrown:\n" + error.toString() + "\n```\n" + getStackTrace() + "\n```");
    
    eb.appendField("TFS Version", Config.getVersion(), true);
    eb.appendField("Attempted Command", command, true);
    
    if (isDM) {
      eb.appendField("Channel info", "Occurred in DM", true);
    } else {
      if (canSpeak) {
        eb.appendField("Channel info", "Occurred in guild\nTFS can speak", true);
      } else {
        eb.appendField("Channel info", "Occurred in guild\nTFS cannot speak", true);
      }
    }
    
    eb.withFooterText(
        "Please add any comments with further pertinent information, like what you were doing that caused the error.");
    
    eb.withColor(0xd32e15);
    
    return eb.build();
  }
  
  @Deprecated
  public String toString() {
    StringBuilder sb = new StringBuilder();
    
    return sb.toString();
  }
  
  public String getStackTrace() {
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement e : error.getStackTrace()) {
      sb.append(e.toString() + "\n");
    }
    return sb.toString();
  }
}
