package com.ncfgroup.dbstat.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;

import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.DateUtil;
import com.ncfgroup.dbstat.util.SqlConf;
import com.ncfgroup.dbstat.util.TimerConf;
import com.ncfgroup.dbstat.util.XMLHandler;

public class TimerPlugin extends Plugin
{
  private static final Logger logger = Logger.getLogger(DBReaderPlugin.class);

  private Timer timer;
  private List<Task> tasklist;

  @Override
  public Event init(Event event)
  {
    logger.info("timer plugin init begin");
    ConfigHandler conf = (ConfigHandler)event.getContext();
    initTimer(conf);
    logger.info("timer plugin init end");
    return null;
  }

  private void initTimer(final ConfigHandler conf){
    timer = new Timer();
    tasklist = new ArrayList<Task>();
    Map<String, List<SqlConf>> map = conf.getSelectSqlMap();
    for (final String tid : map.keySet()) {
      TimerTask task = new TimerTask() {
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
          List<SqlConf> sc = conf.getSelectSqlList(tid);
          conf.setSplitSelectSqlList(sc);
          Event e = new Event(conf,Process.SERVICE);
          logger.info("Timer begin to stat...");
          AsyncController.getInstance().startup(e);

          //modify xml config file
          Map<String,TimerConf> tcMap = conf.getTimerMap();
          TimerConf tc = tcMap.get(tid);

          XMLHandler xml = new XMLHandler();
          Document doc = xml.loadXML(conf.getXmlConf());
          Element timerNode = xml.getNode(doc, ConfigHandler.timer);
          List<Element> instanceNodes = timerNode.elements("instance");
          for(Element ele : instanceNodes){
            String idvalue = ele.attributeValue("id");
            if (!idvalue.equalsIgnoreCase(tid)){
              continue;
            }
            ele.element("starttime").setText(tc.getNextDate());
            xml.saveXML(doc,conf.getXmlConf());
          }
          tc.setStarttime(DateUtil.strToDate(tc.getNextDate()));
          logger.info("update id=["+tid+"] of TimerMap cache...");
        }
      };
      tasklist.add(new Task(tid,task));
    }
  }

  @Override
  public Event service(Event event)
  {
    logger.info("timer plugin service begin");
    ConfigHandler conf = (ConfigHandler)event.getContext();
    for (Task task : tasklist){
      String tid = task.getTid();
      TimerTask timetask = task.getTask();
      TimerConf tc = conf.getTimerMap().get(tid);
      if (tc.getInterval() == null || tc.getInterval() == 0) {
        timer.schedule(timetask, tc.getStarttime());
        logger.info("One-off timer ["+tid+"] plugin loading...");
      } else {
        timer.schedule(timetask, tc.getStarttime(), tc.getInterval());
        logger.info("Multi-off timer ["+tid+"] plugin loading...");
      }
    }
    logger.info("timer plugin service end");
    return null;
  }

  @Override
  public Event destroy(Event event)
  {
    logger.info("timer plugin destroy start");
    for (Task task : tasklist){
      TimerTask timertask = task.getTask();
      if (timertask != null) {
        timertask.cancel();
        timertask = null;
      }
      if (timer != null) {
        timer.cancel();
        timer = null;
      }
    }
    tasklist.clear();
    tasklist = null;
    logger.info("timer plugin destroy end");
    return null;
  }
  
  public static void main(String... args){
    Timer tt = new Timer();
    TimerTask task = new TimerTask(){
      public void run() {
        System.out.println("================");
      }
    };
    
    tt.schedule(task, DateUtil.strToDate("2013-10-21 10:07:00"),1000);
    
    System.out.println(222222222);
  }
  
  class Task{
    private String tid;
    private TimerTask task;
    
    public Task(String tid,TimerTask task){
      this.tid = tid;
      this.task = task;
    }
    
    public String getTid()
    {
      return tid;
    }
    public void setTid(String tid)
    {
      this.tid = tid;
    }
    public TimerTask getTask()
    {
      return task;
    }
    public void setTask(TimerTask task)
    {
      this.task = task;
    }
    
  }
   
}
