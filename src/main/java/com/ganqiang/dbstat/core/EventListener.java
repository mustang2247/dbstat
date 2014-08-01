package com.ganqiang.dbstat.core;

import java.util.List;

public interface EventListener { 

  List<Event> invokeAll(Event event); 
  Event invoke(Event event,PluginType ptype);
}
