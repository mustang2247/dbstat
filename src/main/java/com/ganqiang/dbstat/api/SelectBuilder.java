package com.ganqiang.dbstat.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.util.SqlConf;
import com.ncfgroup.dbstat.util.StringUtil;


public abstract class SelectBuilder
{
  private static final Logger logger = Logger.getLogger(SelectBuilder.class);
  
  public void put(BlockingQueue<Map<String,Object>> message_queue){
    SqlConf sqlconf = buildSelectSql();
    if (sqlconf == null || StringUtil.isNullOrBlank(sqlconf.getId()) 
        || StringUtil.isNullOrBlank(sqlconf.getValue())){
      logger.error("custom select object or its attribute is null.");
      throw new RuntimeException("custom select object or its attribute is null.");
    }
    try {
      Map<String,Object> map = new HashMap<String,Object>();
      map.put(sqlconf.getId(), sqlconf);
      message_queue.put(map);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public abstract SqlConf buildSelectSql();
}
