package com.ncfgroup.dbstat.core;

public enum StorageType
{
  DB("db"),FS("fs"),CACHE("cache");
  private String value;

  StorageType(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
