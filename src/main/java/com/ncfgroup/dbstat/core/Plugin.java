package com.ncfgroup.dbstat.core;

public abstract class Plugin
{
  protected Plugin(){
  }
  
  private PluginType pluginType;

  public  PluginType getPluginType(){
    return pluginType;
  }

  public void setPluginType(PluginType pluginType){
    this.pluginType = pluginType;
  }

  public abstract Event init(Event event);
  
  public abstract Event service(Event event);
  
  public abstract Event destroy(Event event);

  public final Event onEvent(Event event){
    switch(event.getEvenType()){
      case INIT:
        return init(event);
      case SERVICE:
        return service(event);  
      case DESTORY:
        return destroy(event);
      default:
        throw new RuntimeException("Unknown event type!");
    }
  }

}
