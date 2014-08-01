package com.ganqiang.dbstat.core;

import java.io.Serializable;

public class Event implements Serializable
{
  private static final long serialVersionUID = 3975681407581630740L;

  private Object context = null;
  private Process evenType = null;
  
  public Event(Process etype){
    this.evenType = etype;
  }
  
  public Event(Object context, Process etype){
    this.context = context;
    this.evenType = etype;
  }

  public Object getContext()
  {
    return context;
  }

  public void setContext(Object context)
  {
    this.context = context;
  }

  public Process getEvenType()
  {
    return evenType;
  }

  public void setEvenType(Process evenType)
  {
    this.evenType = evenType;
  }

}