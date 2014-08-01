package com.ganqiang.dbstat.api;

import java.util.List;
import java.util.Map;

public abstract class UpdateBuilder
{
  public List<String> getUpdateSqls(List<Map<String,Object>> selectResult){
    return buildUpdateSqls(selectResult);
  }

  public abstract List<String> buildUpdateSqls(List<Map<String,Object>> selectResult);
}
