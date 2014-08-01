package com.ncfgroup.dbstat.core;

public enum Process
{

  INIT("init"),SERVICE("service"),DESTORY("destory");

  private String value;

  Process(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

}
