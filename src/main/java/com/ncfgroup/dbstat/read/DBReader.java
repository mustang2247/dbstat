package com.ncfgroup.dbstat.read;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ncfgroup.dbstat.core.Job;
import com.ncfgroup.dbstat.db.DBFactory;
import com.ncfgroup.dbstat.db.IGenericDAO;
import com.ncfgroup.dbstat.util.Constant;
import com.ncfgroup.dbstat.util.SqlConf;

public class DBReader implements Readable
{
  private static final Logger logger = Logger.getLogger(DBReader.class);

  private static class DBReaderHolder
  {
    private final static DBReader instance = new DBReader();
  }

  public static DBReader getInstance() {
    return DBReaderHolder.instance;
  }

  private DBReader(){
  }

  @Override
  public <T> T read(Job job)
  {
    Map<String,Object> context = job.getContent();
    DataSource ds = (DataSource) context.get(Constant.reader_datasource);
    IGenericDAO dao = DBFactory.getInstance().buildGenericDAO(true, ds);
    context.remove(Constant.reader_datasource);
    String key = context.keySet().iterator().next();
    SqlConf sc = (SqlConf)context.get(key);
    List<Map<String,Object>> list = dao.find(sc.getValue());
    logger.info("dbreader " + Thread.currentThread().getName() + " execute select sql : " + sc.getValue());
    logger.info("dbreader " + Thread.currentThread().getName() + " execute select result size : " + list.size());
    return (T) list;
  }

}
