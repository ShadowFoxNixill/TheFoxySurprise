package net.nixill.dicecalc.functions;

import java.util.ArrayList;
import java.util.HashMap;

import net.nixill.dice.DiceManager;
import net.nixill.dice.DiceParameter;
import net.nixill.dice.functions.DiceFunction;
import net.nixill.dice.objects.DiceList;
import net.nixill.dice.objects.DiceObject;
import net.nixill.dicecalc.config.SavedObjects;
import net.nixill.dicecalc.discord.DiscordBot;
import net.nixill.dicecalc.discord.commands.DiceCommand;
import sx.blah.discord.handle.obj.IChannel;

public class LoadObjectFunction extends DiceFunction {
  // Insert parameter fields here
  private String                                objName;
  private long                                  chanId;
  
  private static HashMap<String, DiceParameter> reqdParams;
  private static HashMap<String, String>        reqdStrings;
  
  private static ArrayList<String>              namesUsed = new ArrayList<>();
  
  // Constructors with doubles and bools go first
  public LoadObjectFunction(String name, IChannel channel) {
    this(name, channel.getLongID());
  }
  
  public LoadObjectFunction(String name, String channel) {
    this(name, Long.parseLong(channel, 36));
  }
  
  // Constructor with DiceParameter last
  public LoadObjectFunction(String name, long channel) {
    objName = name;
    chanId = channel;
    
    if (name == null) { throw new NullPointerException(
        "In a load-object function, the name of the object to load is required."); }
    
    strings.put("name", name);
    strings.put("channel", Long.toString(channel, 36));
  }
  
  // Constructor with HashMaps
  public LoadObjectFunction(HashMap<String, DiceParameter> params, HashMap<String, String> strings) {
    // Run superconstructor
    super(params, strings);
    
    // Take fields from maps
    objName = strings.get("name");
    chanId = Long.parseLong(strings.getOrDefault("channel", "0"), 36);
    
    // If required parameters are null, throw NPE
    if (objName == null || chanId == 0) { throw new NullPointerException(
        "In a load-object function, the name of the object to load, and the (base 36) ID of the channel, are required."); }
  }
  
  public LoadObjectFunction(Builder<LoadObjectFunction> b) {
    // Run super constructor
    super(b);
    
    // Take fields from maps
    objName = strings.get("name");
    chanId = Long.parseLong(strings.getOrDefault("channel", "0"), 36);
    
    // If required parameters are null, throw NPE
    if (objName == null || chanId == 0) { throw new NullPointerException(
        "In a load-object function, the name of the object to load, and the (base 36) ID of the channel, are required."); }
  }
  
  // Method that runs the function objects
  public DiceObject f() {
    return f(objName, chanId);
  }
  
  // Static methods that run the functions without objects
  public static DiceObject f(String name, long channel) {
    if (channel == 1003865133L) {
      return f(name, (IChannel) null);
    } else {
      return f(name, DiscordBot.getClient().getChannelByID(channel));
    }
  }
  
  public static DiceObject f(String name, String channel) {
    return f(name, Long.parseLong(channel, 36));
  }
  
  public static DiceObject f(String name, IChannel channel) {
    String k = SavedObjects.findObject(channel, name);
    k = k.substring(0, k.indexOf(' '));
    DiceParameter par = SavedObjects.loadObject(channel, name);
    DiceObject out = null;
    if (par instanceof DiceFunction) {
      DiceFunction df = (DiceFunction) par;
      if (namesUsed.contains(name)) {
        return df.getLastResult();
      } else {
        namesUsed.add(name);
        out = df.run();
        if (!k.equals("global")) {
          SavedObjects.saveObject(channel, name, df);
        }
        namesUsed.remove(name);
      }
    } else if (par instanceof DiceObject) {
      out = (DiceObject) par;
    }
    
    DiceCommand.setOutTS();
    String str = "{" + name + "} is `";
    if (par instanceof DiceList) {
      String parStr = par.toString();
      str += parStr.substring(parStr.indexOf('[')) + "`";
    } else {
      str += par.toString() + "`";
    }
    if (k.equals("global ")) {
      str += " (global command)";
    }
    DiceManager.logOut(str);
    DiceCommand.setOutOP();
    
    return out;
  }
  
  // Method that returns the name of the DiceFunction
  @Override
  public String getName() {
    return "LOAD";
  }
  
  // Methods that return the parameters of the DiceFunction (as DiceParameters)
  public String getObjectName() {
    return objName;
  }
  
  public String getChanAsStr() {
    return Long.toString(chanId, 36);
  }
  
  public long getChannelId() {
    return chanId;
  }
  
  public IChannel getChannel() {
    return DiscordBot.getClient().getChannelByID(chanId);
  }
  
  // Method that returns the output string of the DiceFunction. Can either use
  // string maps or defaults.
  @Override
  public String getOutString() {
    if (outString != null) return outString;
    String ms = getMappedString(getName());
    if (ms != null)
      return ms;
    else
      return "(LOAD channel=[channel] name=[name])"; // TODO review this
  }
  
  // Method that returns whether or not to add the function to global output on
  // run. Can either use out maps or defaults.
  @Override
  public boolean getOutput() {
    if (output != null) return output.booleanValue();
    Boolean op = getMappedOutput(getName());
    if (op != null)
      return op;
    else
      return false; // TODO review this
  }
  
  // Method that returns whether or not to add the result to global history on
  // run. Can either use hist maps or defaults.
  @Override
  public boolean getHistory() {
    if (history != null) return history.booleanValue();
    Boolean hi = getMappedHistory(getName());
    if (hi != null)
      return hi;
    else
      return false; // TODO review this
  }
  
  // Method that returns the important parameters of the DiceFunction.
  public static HashMap<String, DiceParameter> getRequiredParams() {
    if (reqdParams == null) {
      reqdParams = new HashMap<>();
    }
    return new HashMap<>(reqdParams);
  }
  
  // Method that returns the important strings of the DiceFunction.
  // Can be omitted if there are no default string parameters.
  public static HashMap<String, String> getRequiredStrings() {
    if (reqdStrings == null) {
      reqdStrings = new HashMap<>();
      reqdStrings.put("name", null);
      reqdStrings.put("channel", null);
    }
    return new HashMap<>(reqdStrings);
  }
  
  // Method that makes a new builder from an existing object
  public Builder<LoadObjectFunction> rebuild() {
    return rebuild(LoadObjectFunction.class);
  }
}
