package com.ncfgroup.dbstat;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.core.Event;
import com.ncfgroup.dbstat.core.EventManager;
import com.ncfgroup.dbstat.core.PluginType;
import com.ncfgroup.dbstat.core.Process;
import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.ReflectionUtil;

public class DBStat
{
  private static final Logger logger = Logger.getLogger(DBStat.class);

  private static void start(){
    ConfigHandler conf = (ConfigHandler) ReflectionUtil.getInstance("com.ncfgroup.dbstat.util.ConfigHandler");
    EventManager em = EventManager.getInstance();
    em.registerPlugins(conf);
    logger.info("register plugins finish.");
    Event event = new Event(conf,Process.INIT);
    em.invokeAll(event);
    logger.info("loading all plugin init method.");
    Event timerEvent = new Event(conf,Process.SERVICE);
    em.invoke(timerEvent,PluginType.TIMER);
  }

  private static void stop(){
    EventManager em = EventManager.getInstance();
    Event event = new Event(null,Process.DESTORY);
    try {
      em.invokeAll(event);
    } catch (Exception e) {
      logger.error(e);
    }
    System.exit(0);
  }

  public static void main(String args[]) {
    if (args.length == 0 || args.length > 1) {
      logger.error("Invalid parameter.Please input the one parameter.");
      System.exit(1);
    } else {
      if ("start".equalsIgnoreCase(args[0])) {
        logger.info("Start up dbstat.");
        start();
        logger.info("Started up dbstat success.");
      } else if("stop".equalsIgnoreCase(args[0])){
        logger.info("Stop dbstat.");
        stop();
      } else {
        logger.warn("Invalid parameter.Please input start or stop parameter.");
        System.exit(1);
      }
    }
  }

}
