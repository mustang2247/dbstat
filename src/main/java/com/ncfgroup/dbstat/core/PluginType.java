package com.ncfgroup.dbstat.core;

public enum PluginType
{
  TIMER("timer"),DBREADER("dbreader"),DBWRITER("dbwriter");

  private String value;

  PluginType(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
