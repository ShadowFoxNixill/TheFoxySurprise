package net.nixill.dicecalc.discord.editables;

import java.util.ArrayList;
import java.util.HashMap;

import net.nixill.dicecalc.discord.MessageSender;
import sx.blah.discord.handle.obj.IMessage;

public class EditCycler implements Runnable {
  private static ArrayList<ArrayList<Cyclable>> cycleList;
  private static HashMap<IMessage, Record>      recordMap;
  private static HashMap<Long, BugReport>       reportMap;
  
  static {
    cycleList = new ArrayList<>();
    for (int a = 0; a < 31; a++) {
      cycleList.add(new ArrayList<>());
    }
    
    recordMap = new HashMap<>();
    reportMap = new HashMap<>();
  }
  
  @Override
  public void run() {
    Object monitor = new Object();
    synchronized (monitor) {
      while (true) {
        try {
          monitor.wait(10000);
          for (Cyclable cyc : cycleList.remove(0)) {
            if (cyc instanceof Record) {
              Record rec = (Record) cyc;
              recordMap.remove(rec.getOriginalMsg());
              MessageSender.delete(rec.getOriginalMsg());
            } else if (cyc instanceof BugReport) {
              BugReport bug = (BugReport) cyc;
              reportMap.remove(bug.channel);
            }
          }
          cycleList.add(new ArrayList<>());
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  public static void newRecord(Record rec) {
    cycleList.get(cycleList.size() - 1).add(rec);
    recordMap.put(rec.getOriginalMsg(), rec);
  }
  
  public static Record getRecord(IMessage id) {
    return recordMap.get(id);
  }
  
  public static void deleteRecord(IMessage id) {
    recordMap.remove(id);
  }
  
  public static void newBugReport(BugReport rep) {
    if (reportMap.containsKey(rep.channel)) reportMap.remove(rep.channel);
    reportMap.put(rep.channel, rep);
    cycleList.get(cycleList.size() - 1).add(rep);
  }
  
  public static BugReport getBugReport(long id) {
    return reportMap.get(id);
  }
  
  public static void deleteBugReport(long id) {
    reportMap.remove(id);
  }
}
