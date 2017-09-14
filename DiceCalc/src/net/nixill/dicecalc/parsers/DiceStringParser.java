package net.nixill.dicecalc.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.nixill.dice.DiceParameter;
import net.nixill.dice.functions.ConcatenateFunction;
import net.nixill.dice.functions.DiceFunction;
import net.nixill.dice.functions.DivisionFunction;
import net.nixill.dice.functions.DropFirstFunction;
import net.nixill.dice.functions.DropHighFunction;
import net.nixill.dice.functions.DropRangeFunction;
import net.nixill.dice.functions.ExponentiationFunction;
import net.nixill.dice.functions.IntegerDivisionFunction;
import net.nixill.dice.functions.KeepMaxFunction;
import net.nixill.dice.functions.ModuloFunction;
import net.nixill.dice.functions.MultiplicationFunction;
import net.nixill.dice.functions.RepeatFunction;
import net.nixill.dice.functions.RerollFunction;
import net.nixill.dice.functions.RollFunction;
import net.nixill.dice.functions.RollUntilFunction;
import net.nixill.dice.functions.WeightedRollFunction;
import net.nixill.dice.objects.DiceList;
import net.nixill.dice.objects.SingleValue;
import net.nixill.dice.objects.StaticValue;
import net.nixill.dicecalc.config.SavedObjects;
import net.nixill.dicecalc.functions.LoadObjectFunction;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

public class DiceStringParser {
  private static HashMap<String, DiceFunction.Builder<? extends DiceFunction>> typeMap;
  private static HashMap<String, Integer>                                      pris;
  private static HashMap<String, String>                                       args;
  
  public static DiceParameter parse(IDiscordClient cli, IChannel channel, IUser user, String line) {
    String remLine = line.replace(" ", "").replace("\\", "").replace("`", "").toLowerCase();
    
    String pLPar = "\\(*";
    String pPosNum = "(\\d*\\.)?\\d+";
    String pNum = "-?" + pPosNum;
    String pDie = "d" + pPosNum;
    String pValue = "(" + pNum + "|" + pDie + ")";
    String pList = "\\[(" + pNum + "\\,)*" + pNum + "\\]";
    String pImport = "\\{\\$?(\\w+)\\}";
    String pObject = "(" + pValue + "|" + pList + "|" + pImport + ")";
    String pRPar = "\\)*";
    String pOps = "((d|k|u|r|x|xr)(>|>=|<|<=|=|!=)|(m[nx]|u|r|w|xr?)|(d|k)(l|h|)|\\+|-|\\*|//?|\\%|\\^|\\&)";
    String pOpsAfter = "[\\d\\(\\-\\.\\[d\\{]";
    String pOpsLA = pOps + "(?=" + pOpsAfter + ")";
    
    Pattern p = Pattern.compile("^(" + pLPar + pObject + pRPar + pOps + ")*" + pLPar + pObject + pRPar + "$");
    Matcher m = p.matcher(remLine);
    
    if (!m.matches()) {
      throw new IllegalArgumentException("Dice syntax is invalid.");
    }
    
    else {
      int priMod = 0;
      
      ArrayList<DiceParameter> values = new ArrayList<>();
      ArrayList<DiceFunction.Builder<? extends DiceFunction>> types = new ArrayList<>();
      ArrayList<Integer> priority = new ArrayList<>();
      
      Matcher leftPar = Pattern.compile("^" + pLPar).matcher("");
      Matcher number = Pattern.compile("^" + pObject).matcher("");
      Matcher rightPar = Pattern.compile("^" + pRPar).matcher("");
      Matcher operation = Pattern.compile("^" + pOpsLA).matcher("");
      
      while (true) {
        String s = next(leftPar, remLine);
        remLine = remLine.substring(s.length());
        priMod += 10 * s.length();
        
        s = next(number, remLine);
        remLine = remLine.substring(s.length());
        DiceParameter o;
        if (Pattern.matches(pList, s)) {
          Matcher m2 = Pattern.compile(pNum).matcher(s);
          ArrayList<SingleValue> list = new ArrayList<>();
          while (m2.find()) {
            String no = m2.group(0);
            double n = Double.parseDouble(no);
            list.add(new StaticValue(n));
          }
          o = new DiceList(list);
        } else if (s.matches(pDie)) {
          double n = Double.parseDouble(s.substring(1));
          o = dieFunction(n);
        } else if (s.matches(pImport)) {
          String name = s.substring(1, s.length() - 1);
          boolean global = name.startsWith("$");
          if (global) {
            name = name.substring(1);
          }
          String k = SavedObjects.findObject((global) ? null : channel, name);
          if (k == null)
            throw new NullPointerException("The command {" + ((global) ? "$" : "") + name + "} doesn't exist.");
          o = new LoadObjectFunction(name, (global) ? 1003865133L : channel.getLongID());
        } else {
          o = new StaticValue(Double.parseDouble(s));
        }
        values.add(o);
        
        s = next(rightPar, remLine);
        remLine = remLine.substring(s.length());
        priMod -= 10 * s.length();
        
        if (remLine.length() == 0) break;
        
        s = next(operation, remLine);
        if (s == null) {
          Matcher nextMatcher = Pattern.compile(pOpsAfter).matcher(remLine);
          if (nextMatcher.find()) remLine = remLine.substring(0, nextMatcher.start());
          throw new UnsupportedOperationException("Unrecognized operation " + remLine);
        }
        remLine = remLine.substring(s.length());
        types.add(getType(s));
        int pri = getPri(s) + priMod;
        priority.add(pri);
      }
      
      int maxPri = 2147483647;
      while (!types.isEmpty()) {
        int maxPriLoop = -2147483648;
        int i = 0;
        boolean bw = rtl(maxPri);
        if (bw) {
          i = types.size() - 1;
        }
        while (true) {
          if (bw) {
            if (i < 0) break;
          } else {
            if (i >= types.size()) break;
          }
          int pri = priority.get(i);
          if (pri == maxPri) {
            DiceFunction.Builder<? extends DiceFunction> b = types.remove(i);
            DiceParameter l = values.remove(i);
            DiceParameter r = values.get(i);
            priority.remove(i);
            
            b.with(getArg(b.getType(), true), l);
            b.with(getArg(b.getType(), false), r);
            
            DiceFunction f = b.build();
            
            values.set(i, f);
            
            if (!bw) i--;
          } else {
            if (maxPriLoop < pri) {
              maxPriLoop = pri;
            }
          }
          if (bw)
            i--;
          else
            i++;
        }
        maxPri = maxPriLoop;
      }
      return values.get(0);
    }
  }
  
  private static RollFunction dieFunction(double n) {
    RollFunction f = DiceFunction.builder(RollFunction.class).with("count", 1).with("sides", n).with("solo", "yes")
        .build();
    return f;
  }
  
  private static String next(Matcher thisMatcher, String line) {
    thisMatcher.reset(line);
    
    if (!thisMatcher.lookingAt())
      return null;
    else
      return line.substring(0, thisMatcher.end());
  }
  
  public static DiceFunction.Builder<? extends DiceFunction> getType(String op) {
    if (typeMap == null) makeLists();
    return typeMap.get(op).clone();
  }
  
  public static int getPri(String op) {
    if (pris == null) makeLists();
    return pris.get(op);
  }
  
  public static String getArg(String op, boolean left) {
    if (args == null) makeLists();
    String side = (left ? "left" : "right");
    return args.getOrDefault(op.toString() + " " + side, side);
  }
  
  private static boolean rtl(int pri) {
    switch (pri % 10) {
      case 9: // d(dice), u
        return false;
      case 8: // w
        return false;
      case 7: // r, x, xr
        return false;
      case 6: // k, d(drop), mn, mx
        return false;
      case 5:
        return false;
      case 4:
        return false;
      case 3: // ^
        return true;
      case 2: // * / // %
        return false;
      case 1: // + -
        return false;
      case 0: // &
        return false;
      default: // Unreachable because an integer modulo 10 is always either
               // 9, 8, 7, 6, 5, 4, 3, 2, 1, or 0. But Java wants me to include
               // it anyway.
        return false;
    }
  }
  
  private static void makeLists() {
    typeMap = new HashMap<>();
    DiceFunction.Builder<RollUntilFunction> uBuilder = DiceFunction.builder(RollUntilFunction.class).with("count", 400)
        .close();
    DiceFunction.Builder<RerollFunction> rBuilder = DiceFunction.builder(RerollFunction.class).with("count_factor", 2)
        .close();
    DiceFunction.Builder<RerollFunction> xBuilder = rBuilder.clone().with("keep", StaticValue.t).close();
    DiceFunction.Builder<RerollFunction> xrBuilder = xBuilder.clone().with("recurse", StaticValue.t).close();
    typeMap.put("+", DiceFunction.builder(ConcatenateFunction.class).close());
    typeMap.put("-", DiceFunction.builder(ConcatenateFunction.class).with("negate", StaticValue.t).close());
    typeMap.put("*", DiceFunction.builder(MultiplicationFunction.class).close());
    typeMap.put("/", DiceFunction.builder(DivisionFunction.class).close());
    typeMap.put("//", DiceFunction.builder(IntegerDivisionFunction.class).close());
    typeMap.put("%", DiceFunction.builder(ModuloFunction.class).close());
    typeMap.put("^", DiceFunction.builder(ExponentiationFunction.class).close());
    typeMap.put("&", DiceFunction.builder(RepeatFunction.class).close());
    typeMap.put("d", DiceFunction.builder(RollFunction.class).close());
    typeMap.put("dh", DiceFunction.builder(DropHighFunction.class).with("oper", "dh").close());
    typeMap.put("dl",
        DiceFunction.builder(DropHighFunction.class).with("oper", "dl").with("lowest", StaticValue.t).close());
    typeMap.put("d=", DiceFunction.builder(DropRangeFunction.class).with("oper", "d=").close());
    typeMap.put("d!=",
        DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "d!=").close());
    typeMap.put("d>", DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "d>")
        .with("max", StaticValue.negInf).close());
    typeMap.put("d>=",
        DiceFunction.builder(DropRangeFunction.class).with("oper", "d>=").with("max", StaticValue.posInf).close());
    typeMap.put("d<", DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "d<")
        .with("max", StaticValue.posInf).close());
    typeMap.put("d<=",
        DiceFunction.builder(DropRangeFunction.class).with("oper", "d<=").with("max", StaticValue.negInf).close());
    typeMap.put("k", DiceFunction.builder(DropFirstFunction.class).with("keep", StaticValue.t).close());
    typeMap.put("kh",
        DiceFunction.builder(DropHighFunction.class).with("keep", StaticValue.t).with("oper", "kh").close());
    typeMap.put("kl", DiceFunction.builder(DropHighFunction.class).with("keep", StaticValue.t).with("oper", "kl")
        .with("lowest", StaticValue.t).close());
    typeMap.put("k=",
        DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "k=").close());
    typeMap.put("k!=", DiceFunction.builder(DropRangeFunction.class).with("oper", "k!=").close());
    typeMap.put("k>",
        DiceFunction.builder(DropRangeFunction.class).with("oper", "k>").with("max", StaticValue.negInf).close());
    typeMap.put("k>=", DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "k>=")
        .with("max", StaticValue.posInf).close());
    typeMap.put("k<",
        DiceFunction.builder(DropRangeFunction.class).with("oper", "k<").with("max", StaticValue.posInf).close());
    typeMap.put("k<=", DiceFunction.builder(DropRangeFunction.class).with("keep", StaticValue.t).with("oper", "k<=")
        .with("max", StaticValue.negInf).close());
    typeMap.put("mn", DiceFunction.builder(KeepMaxFunction.class).with("keep_left", StaticValue.t).close());
    typeMap.put("mx", DiceFunction.builder(KeepMaxFunction.class).with("keep_left", StaticValue.t)
        .with("invert", StaticValue.t).close());
    typeMap.put("r", rBuilder.clone().with("oper", "r").close());
    typeMap.put("r=", rBuilder.clone().with("oper", "r=").close());
    typeMap.put("r!=", rBuilder.clone().with("oper", "r!=").with("invert", StaticValue.t).close());
    typeMap.put("r>",
        rBuilder.clone().with("oper", "r>").with("high", StaticValue.negInf).with("invert", StaticValue.t).close());
    typeMap.put("r>=", rBuilder.clone().with("oper", "r>=").with("high", StaticValue.posInf).close());
    typeMap.put("r<",
        rBuilder.clone().with("oper", "r<").with("high", StaticValue.posInf).with("invert", StaticValue.t).close());
    typeMap.put("r<=", rBuilder.clone().with("oper", "r<=").with("high", StaticValue.negInf).close());
    typeMap.put("u", uBuilder.clone().with("oper", "").close());
    typeMap.put("u=", uBuilder.clone().with("oper", "=").close());
    typeMap.put("u!=", uBuilder.clone().with("oper", "!=").with("invert", StaticValue.t).close());
    typeMap.put("u>",
        uBuilder.clone().with("oper", ">").with("other", StaticValue.min).with("invert", StaticValue.t).close());
    typeMap.put("u>=", uBuilder.clone().with("oper", ">=").with("other", StaticValue.max).close());
    typeMap.put("u<",
        uBuilder.clone().with("oper", "<").with("other", StaticValue.max).with("invert", StaticValue.t).close());
    typeMap.put("u<=", uBuilder.clone().with("oper", "<=").with("other", StaticValue.min).close());
    typeMap.put("w", DiceFunction.builder(WeightedRollFunction.class).close());
    typeMap.put("x", xBuilder.clone().with("oper", "x").close());
    typeMap.put("x=", xBuilder.clone().with("oper", "x=").close());
    typeMap.put("x!=", xBuilder.clone().with("oper", "x!=").with("invert", StaticValue.t).close());
    typeMap.put("x>",
        xBuilder.clone().with("oper", "x>").with("high", StaticValue.negInf).with("invert", StaticValue.t).close());
    typeMap.put("x>=", xBuilder.clone().with("oper", "x>=").with("high", StaticValue.posInf).close());
    typeMap.put("x<",
        xBuilder.clone().with("oper", "x<").with("high", StaticValue.posInf).with("invert", StaticValue.t).close());
    typeMap.put("x<=", xBuilder.clone().with("oper", "x<=").with("high", StaticValue.negInf).close());
    typeMap.put("xr", xrBuilder.clone().with("oper", "xr").close());
    typeMap.put("xr=", xrBuilder.clone().with("oper", "xr=").close());
    typeMap.put("xr!=", xrBuilder.clone().with("oper", "xr!=").with("invert", StaticValue.t).close());
    typeMap.put("xr>",
        xrBuilder.clone().with("oper", "xr>").with("high", StaticValue.negInf).with("invert", StaticValue.t).close());
    typeMap.put("xr>=", xrBuilder.clone().with("oper", "xr>=").with("high", StaticValue.posInf).close());
    typeMap.put("xr<",
        xrBuilder.clone().with("oper", "xr<").with("high", StaticValue.posInf).with("invert", StaticValue.t).close());
    typeMap.put("xr<=", xrBuilder.clone().with("oper", "xr<=").with("high", StaticValue.negInf).close());
    
    pris = new HashMap<>();
    pris.put("d", 9);
    pris.put("dh", 6);
    pris.put("dl", 6);
    pris.put("d=", 6);
    pris.put("d!=", 6);
    pris.put("d>", 6);
    pris.put("d>=", 6);
    pris.put("d<", 6);
    pris.put("d<=", 6);
    pris.put("k", 6);
    pris.put("kh", 6);
    pris.put("kl", 6);
    pris.put("k=", 6);
    pris.put("k!=", 6);
    pris.put("k>", 6);
    pris.put("k>=", 6);
    pris.put("k<", 6);
    pris.put("k<=", 6);
    pris.put("mn", 5);
    pris.put("mx", 5);
    pris.put("r", 7);
    pris.put("r=", 7);
    pris.put("r!=", 7);
    pris.put("r>", 7);
    pris.put("r>=", 7);
    pris.put("r<", 7);
    pris.put("r<=", 7);
    pris.put("u", 9);
    pris.put("u=", 9);
    pris.put("u!=", 9);
    pris.put("u>", 9);
    pris.put("u>=", 9);
    pris.put("u<", 9);
    pris.put("u<=", 9);
    pris.put("w", 8);
    pris.put("x", 7);
    pris.put("x=", 7);
    pris.put("x!=", 7);
    pris.put("x>", 7);
    pris.put("x>=", 7);
    pris.put("x<", 7);
    pris.put("x<=", 7);
    pris.put("xr", 7);
    pris.put("xr=", 7);
    pris.put("xr!=", 7);
    pris.put("xr>", 7);
    pris.put("xr>=", 7);
    pris.put("xr<", 7);
    pris.put("xr<=", 7);
    pris.put("+", 1);
    pris.put("-", 1);
    pris.put("*", 2);
    pris.put("/", 2);
    pris.put("//", 2);
    pris.put("%", 2);
    pris.put("^", 3);
    pris.put("&", 0);
    
    args = new HashMap<>();
    args.put("DropFirstFunction left", "dice");
    args.put("DropFirstFunction right", "count");
    args.put("DropHighFunction left", "dice");
    args.put("DropHighFunction right", "count");
    args.put("DropRangeFunction left", "dice");
    args.put("DropRangeFunction right", "min");
    args.put("RepeatFunction left", "function");
    args.put("RepeatFunction right", "count");
    args.put("RerollFunction left", "dice");
    args.put("RerollFunction right", "low");
    args.put("RollFunction left", "count");
    args.put("RollFunction right", "sides");
    args.put("RollUntilFunction left", "sides");
    args.put("RollUntilFunction right", "stopper");
    args.put("WeightedRollFunction left", "count");
    args.put("WeightedRollFunction right", "weights");
  }
}
