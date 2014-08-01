package com.ganqiang.dbstat.api;

import java.util.List;
import java.util.Map;

public abstract class InsertBuilder
{
  public List<String> getInsertSqls(List<Map<String,Object>> selectResult){
    return buildInsertSqls(selectResult);
  }

  public abstract List<String> buildInsertSqls(List<Map<String,Object>> selectResult);
}
