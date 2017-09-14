package net.nixill.dicecalc.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.nixill.dice.DiceParameter;
import net.nixill.dice.functions.DiceFunction;
import net.nixill.dice.objects.DiceList;
import net.nixill.dice.objects.DiceObject;
import net.nixill.dice.objects.Die;
import net.nixill.dice.objects.SingleValue;
import net.nixill.dice.objects.StaticValue;
import net.nixill.dice.objects.WeightedDie;

public class DiceJsonParser {
  
  public static JsonObject objToJson(DiceParameter object) {
    JsonObject jsObject = new JsonObject();
    if (object instanceof SingleValue) {
      SingleValue sv = (SingleValue) object;
      jsObject.addProperty("value", sv.getValue());
      jsObject.addProperty("max", sv.getMaxValue());
      if (sv instanceof Die) jsObject.addProperty("type", "die");
      if (sv instanceof StaticValue) jsObject.addProperty("type", "static");
      if (sv instanceof WeightedDie) jsObject.addProperty("type", "die");
    } else if (object instanceof DiceList) {
      DiceList dl = (DiceList) object;
      JsonArray jsArray = new JsonArray();
      for (SingleValue o : dl.list()) {
        jsArray.add(objToJson(o));
      }
      jsObject.add("items", jsArray);
      jsObject.addProperty("type", "list");
    } else if (object instanceof DiceFunction) {
      DiceFunction df = (DiceFunction) object;
      Class<? extends DiceFunction> cls = df.getClass();
      jsObject.addProperty("type", "function");
      jsObject.addProperty("class", cls.getName());
      jsObject.add("last", objToJson(df.getLastResult()));
      HashMap<String, DiceParameter> params = df.getParams();
      for (String k : params.keySet()) {
        jsObject.add("_" + k, objToJson(params.get(k)));
      }
      HashMap<String, String> strings = df.getStrings();
      for (String k : strings.keySet()) {
        jsObject.addProperty("$" + k, strings.get(k));
      }
    }
    return jsObject;
  }
  
  @SuppressWarnings("unchecked")
  public static DiceParameter jsonToObj(JsonObject object) {
    String type = object.get("type").getAsString();
    if (type.equals("static")) {
      double value = object.get("value").getAsDouble();
      double max = object.get("max").getAsDouble();
      return new StaticValue(value, max);
    } else if (type.equals("die")) {
      double value = object.get("value").getAsDouble();
      double sides = object.get("max").getAsDouble();
      return new Die(sides, value);
    } else if (type.equals("list")) {
      JsonArray arr = object.getAsJsonArray("items");
      Iterator<JsonElement> iter = arr.iterator();
      ArrayList<SingleValue> list = new ArrayList<>();
      while (iter.hasNext()) {
        JsonObject obj = iter.next().getAsJsonObject();
        list.add((SingleValue) jsonToObj(obj));
      }
      return new DiceList(list);
    } else if (type.equals("function")) {
      String className = object.get("class").getAsString();
      Class<? extends DiceFunction> cls;
      try {
        cls = (Class<? extends DiceFunction>) Class.forName(className);
      } catch (ClassNotFoundException | ClassCastException e) {
        return null;
      }
      DiceFunction.Builder<? extends DiceFunction> builder = DiceFunction.builder(cls);
      builder.withLastResult((DiceObject) jsonToObj(object.getAsJsonObject("last")));
      for (Entry<String, JsonElement> e : object.entrySet()) {
        String key = e.getKey();
        if (key.startsWith("_")) {
          JsonObject obj = e.getValue().getAsJsonObject();
          builder.with(key.substring(1), jsonToObj(obj));
        } else if (key.startsWith("$")) {
          String obj = e.getValue().getAsString();
          builder.with(key.substring(1), obj);
        }
      }
      return builder.build();
    }
    return null;
  }
  
}
