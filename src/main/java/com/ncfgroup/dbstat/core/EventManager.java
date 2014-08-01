package com.ncfgroup.dbstat.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.TimerConf;

public class EventManager implements EventListener
{
  private static final Logger logger = Logger.getLogger(EventManager.class);
  private static class EventManagerHolder{
    private final static EventManager instance = new EventManager();
  }

  public static EventManager getInstance() {
    return EventManagerHolder.instance;
  }

  private EventManager(){   
  }

  private LinkedHashMap<PluginType,Plugin> plugins = new LinkedHashMap<PluginType,Plugin>();

  public LinkedHashMap<PluginType,Plugin> getPlugins(){
    return plugins;
  }

  public EventManager activePlugin(Plugin p){
    this.plugins.put(p.getPluginType(), p);
    return this;
  }

  public List<Event> invokeAll(Event event){
    List<Event> eventlist =new ArrayList<Event>();
    Set<Entry<PluginType, Plugin>> set = plugins.entrySet();
    for (Entry<PluginType, Plugin> entry : set) {
      Plugin plugin = entry.getValue();
      Event e = plugin.onEvent(event);
      eventlist.add(e);
    }
    return eventlist;
  }

  public Event invoke(Event event,PluginType ptype){
    Plugin plugin = plugins.get(ptype);
    Event e = plugin.onEvent(event);
    return e;
  }

  public void registerPlugins(ConfigHandler conf){
    Map<String,TimerConf> timerMap = conf.getTimerMap();
    Map<String,Object> readerMap = conf.getReaderMap();
    Map<String,Object> writerMap = conf.getWriterMap();
    if (!timerMap.isEmpty()) {
      Plugin p = new TimerPlugin();
      p.setPluginType(PluginType.TIMER);
      getInstance().activePlugin(p);
      logger.info("timer plugin register...");
    }
    if (!readerMap.isEmpty()) {
      Plugin p = new DBReaderPlugin();
      p.setPluginType(PluginType.DBREADER);
      getInstance().activePlugin(p);
      logger.info("dbreader plugin register...");
    }
    if (!writerMap.isEmpty()) {
      Plugin p = new DBWriterPlugin();
      p.setPluginType(PluginType.DBWRITER);
      getInstance().activePlugin(p);
      logger.info("dbwriter plugin register...");
    }
  }

}
