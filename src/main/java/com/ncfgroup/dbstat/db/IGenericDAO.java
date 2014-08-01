package com.ncfgroup.dbstat.db;

import java.util.List;
import java.util.Map;

public interface IGenericDAO
{
  List<Map<String,Object>> find(final String sql);
 
  int[] executeBatch(final List<String> sqls);
}
