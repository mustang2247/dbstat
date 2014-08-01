package com.ganqiang.dbstat.api;

import java.util.List;
import java.util.Map;

public abstract class DeleteBuilder
{
  public List<String> getDeleteSqls(List<Map<String,Object>> selectResult){
    return buildDeleteSqls(selectResult);
  }

  public abstract List<String> buildDeleteSqls(List<Map<String,Object>> selectResult);
}
