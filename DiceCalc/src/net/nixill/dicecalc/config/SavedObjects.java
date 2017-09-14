package net.nixill.dicecalc.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.nixill.dice.DiceParameter;
import net.nixill.dicecalc.parsers.DiceJsonParser;
import sx.blah.discord.handle.obj.IChannel;

public class SavedObjects {
  private static JsonObject savedObjects;
    
  public static void read(JsonObject savedObjects, JsonObject savedAliases) {
    SavedObjects.savedObjects = savedObjects;
  }

  public static void saveObject(IChannel channel, String name, DiceParameter object) {
    JsonObject jsObject = DiceJsonParser.objToJson(object);
    
    if (channel != null) {
      String serverId;
      if (channel.isPrivate())
        serverId = Long.toString(channel.getLongID(), 36);
      else
        serverId = Long.toString(channel.getGuild().getLongID(), 36);
      
      String saveName = serverId + " " + name;
      savedObjects.add(saveName, jsObject);
    } else {
      String saveName = "global " + name;
      savedObjects.add(saveName, jsObject);
    }
    
    // Save config
    Config.write();
  }

  public static boolean unsaveObject(IChannel channel, String name) {
    String serverId;
    if (channel.isPrivate())
      serverId = Long.toString(channel.getLongID(), 36);
    else
      serverId = Long.toString(channel.getGuild().getLongID(), 36);
    
    // Delete direct entry
    JsonElement old = savedObjects.remove(serverId + " " + name);
    if (old == null) return false; // No command was removed
    
    // Save
    Config.write();
    
    return true; // A command was removed successfully
  }

  public static boolean unsaveGlobal(String name) {
    JsonElement old = savedObjects.remove("global " + name);
    if (old == null) return false;
    return true;
  }

  public static DiceParameter loadObject(IChannel channel, String name) {
    String key = findObject(channel, name);
    if (key == null) return null;
    DiceParameter dp = DiceJsonParser.jsonToObj(savedObjects.getAsJsonObject(key));
    return dp;
  }

  public static DiceParameter loadObject(String name) {
    if (name == null) return null;
    DiceParameter dp = DiceJsonParser.jsonToObj(savedObjects.getAsJsonObject(name));
    return dp;
  }

  public static String findObject(IChannel channel, String name) {
    JsonObject jsObject = null;
    String key;
    
    if (channel != null) {
      String serverId;
      if (channel.isPrivate())
        serverId = Long.toString(channel.getLongID(), 36); // keep PMs separate from each other
      else
        serverId = Long.toString(channel.getGuild().getLongID(), 36);
      
      // First search saved objects directly for (channel user name)
      key = serverId + " " + name;
      jsObject = savedObjects.getAsJsonObject(key);
      if (jsObject != null) return key;
    }
    
    // Finally search for globals.
    key = "global " + name;
    jsObject = savedObjects.getAsJsonObject(key);
    if (jsObject != null) return key;
    
    return null;
  }
}
