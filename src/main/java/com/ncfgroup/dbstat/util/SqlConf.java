package com.ncfgroup.dbstat.util;

public class SqlConf
{
  private String clazz;
  private String id;
  private String refid;
  private String value;
  private String tid;
  
  public String getValue()
  {
    return value;
  }
  public void setValue(String value)
  {
    this.value = value;
  }
  public String getClazz()
  {
    return clazz;
  }
  public void setClazz(String clazz)
  {
    this.clazz = clazz;
  }
  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }
  public String getRefid()
  {
    return refid;
  }
  public void setRefid(String refid)
  {
    this.refid = refid;
  }
  public String getTid()
  {
    return tid;
  }
  public void setTid(String tid)
  {
    this.tid = tid;
  }
}
