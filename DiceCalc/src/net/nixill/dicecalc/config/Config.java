package net.nixill.dicecalc.config;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.nixill.dicecalc.discord.DiscordBot;
import sx.blah.discord.handle.obj.IChannel;

public class Config {
  // The indentation of these properties is representative of the actual tree.
  // @formatter:off
  private static JsonObject configTree;
    private static boolean debugMode;
    private static String token;
    private static JsonObject configVersion;
      private static int major;
      private static int minor;
      private static int launch;
    private static long creator;
    private static IChannel creatorPM;
    private static JsonArray notifyList;
    private static JsonObject savedObjects;
    private static JsonObject savedAliases;
  // @formatter:on
  
  private static Gson       printer;
  
  public static void read() {
    try {
      configTree = new JsonParser().parse(new FileReader("config.json")).getAsJsonObject();
    } catch (IOException | IllegalStateException e) {
      System.err.println("Couldn't read the config file. A new one will be generated.");
      configTree = new JsonObject();
    }
    configVersion = getOrMakeObject(configTree, "version");
    updateVersion(5, 2);
    debugMode = getOrMakeProperty(configTree, "debug", false);
    creator = getOrMakeProperty(configTree, "creator", 106621544809115648L).longValue();
    token = getOrMakeProperty(configTree, "token", "Nope. Fill this in thyself.");
    notifyList = getOrMakeArray(configTree, "notify");
    savedObjects = getOrMakeObject(configTree, "saved");
    
    GsonBuilder builder = new GsonBuilder();
    if (debugMode) builder.setPrettyPrinting();
    printer = builder.create();
    
    SavedObjects.read(savedObjects, savedAliases);
    
    write();
  }
  
  public static void write() {
    try (BufferedWriter w = new BufferedWriter(new FileWriter("config.json"))) {
      w.write(printer.toJson(configTree));
    } catch (IOException e) {
      e.printStackTrace();
      System.err.println("Couldn't save the config.");
    }
  }
  
  public static JsonArray getOrMakeArray(JsonObject parent, String name) {
    JsonArray arr = parent.getAsJsonArray(name);
    if (arr == null) {
      arr = new JsonArray();
      parent.add(name, arr);
    }
    return arr;
  }
  
  public static JsonObject getOrMakeObject(JsonObject parent, String name) {
    JsonObject obj = parent.getAsJsonObject(name);
    if (obj == null) {
      obj = new JsonObject();
      parent.add(name, obj);
    }
    return obj;
  }
  
  public static Number getOrMakeProperty(JsonObject parent, String name, Number property) {
    JsonElement elm = parent.get(name);
    if (elm == null) {
      elm = new JsonPrimitive(property);
      parent.add(name, elm);
    }
    return elm.getAsNumber();
  }
  
  public static String getOrMakeProperty(JsonObject parent, String name, String property) {
    JsonElement elm = parent.get(name);
    if (elm == null) {
      elm = new JsonPrimitive(property);
      parent.add(name, elm);
    }
    return elm.getAsString();
  }
  
  public static Boolean getOrMakeProperty(JsonObject parent, String name, Boolean property) {
    JsonElement elm = parent.get(name);
    if (elm == null) {
      elm = new JsonPrimitive(property);
      parent.add(name, elm);
    }
    return elm.getAsBoolean();
  }
  
  public static void addToNotifyList(long chanId) {
    JsonPrimitive prim = new JsonPrimitive(chanId);
    if (notifyList.contains(prim))
      return;
    else
      notifyList.add(prim);
  }
  
  public static boolean isInNotifyList(long chanId) {
    JsonPrimitive prim = new JsonPrimitive(chanId);
    return notifyList.contains(prim);
  }
  
  public static void removeFromNotifyList(long chanId) {
    JsonPrimitive prim = new JsonPrimitive(chanId);
    notifyList.remove(prim);
  }
  
  public static ArrayList<Long> notifyList() {
    ArrayList<Long> out = new ArrayList<>(notifyList.size());
    for (JsonElement e : notifyList) {
      out.add(e.getAsLong());
    }
    return out;
  }
  
  public static void replaceNotifyList(ArrayList<Long> list) {
    notifyList = new JsonArray();
    for (Long s : list) {
      list.add(s);
    }
    configTree.add("notify", notifyList);
  }
  
  public static long getCreator() {
    return creator;
  }
  
  public static IChannel getCreatorPM() {
    if (creatorPM == null) {
      creatorPM = DiscordBot.getClient().getOrCreatePMChannel(DiscordBot.getClient().fetchUser(creator));
    }
    return creatorPM;
  }
  
  public static String getVersion() {
    return "" + major + "." + minor + " launch " + launch;
  }
  
  public static void updateVersion(int newMj, int newMn) {
    major = getOrMakeProperty(configVersion, "major", 1).intValue();
    minor = getOrMakeProperty(configVersion, "minor", 0).intValue();
    launch = getOrMakeProperty(configVersion, "launch", 0).intValue();
    int newLn = launch + 1;
    
    // TODO This code will update TFS' config to the latest version if it
    // doesn't already match.
    // It should be changed with every new config-structure update.
    if (major == 4) {
      configTree.remove("saved");
      configTree.remove("saved-aliases");
      configTree.remove("creator");
      
      JsonArray oldNotifyList = getOrMakeArray(configTree, "notify");
      configTree.remove("notify");
      JsonArray newNotifyList = getOrMakeArray(configTree, "notify");
      for (JsonElement elm : oldNotifyList) {
        newNotifyList.add(new Long(elm.getAsString()));
      }
    }
    
    if (newMj > major) {
      newMn = 0;
      newLn = 1;
      configVersion.addProperty("major", newMj);
    }
    
    if (newMn != minor) {
      newLn = 1;
      configVersion.addProperty("minor", newMn);
    }
    
    configVersion.addProperty("launch", newLn);
    
    major = newMj;
    minor = newMn;
    launch = newLn;
    
    System.out.println("TheFoxySurprise version " + major + "." + minor + " launch " + launch);
  }
  
  public static boolean debug() {
    return debugMode;
  }
  
  public static String getToken() {
    return token;
  }
}
