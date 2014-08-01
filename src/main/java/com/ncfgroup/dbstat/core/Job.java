package com.ncfgroup.dbstat.core;

import java.util.Map;

public class Job
{
  private String id = Thread.currentThread().getName();
  private StorageType type;
  private Map<String,Object> content;

  public Job(StorageType type, Map<String,Object> map){
    this.type = type;
    this.content = map;
  }
  
  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }
  public Map<String,Object> getContent()
  {
    return content;
  }
  public void setContent(Map<String,Object> content)
  {
    this.content = content;
  }

  public StorageType getType()
  {
    return type;
  }

  public void setType(StorageType type)
  {
    this.type = type;
  }
}
