package com.ncfgroup.dbstat.core;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.ncfgroup.dbstat.db.DBHelper;
import com.ncfgroup.dbstat.util.ConfigHandler;


public class DBWriterPlugin extends Plugin
{
  private static final Logger logger = Logger.getLogger(DBReaderPlugin.class);
  
  private static DataSource dataSource = null;

  @Override
  public Event init(Event event)
  {
    logger.info("dbwriter plugin init begin");
    ConfigHandler conf = (ConfigHandler)event.getContext();
    initDBPool(conf);
    testConnection();
    logger.info("dbwriter plugin init end");
    return null;
  }

  @Override
  public Event destroy(Event event)
  {
    logger.info("dbwriter plugin destroy begin");
    if (dataSource != null){
      dataSource.close();
    }
    logger.info("dbwriter plugin destroy end");
    return null;
  }

  @Override
  public Event service(Event event)
  {
    logger.info("dbwriter plugin service begin");
    event.setContext(dataSource);
    logger.info("dbwriter plugin service end");
    return event;
  }

  private static void initDBPool(ConfigHandler conf){
    Map<String,Object> map = conf.getWriterMap();
    PoolProperties p = new PoolProperties();
    p.setUrl(map.get(conf.datasource_url).toString());
    p.setDriverClassName(map.get(conf.datasource_driverclassname).toString());
    p.setUsername(map.get(conf.datasource_username).toString());
    p.setPassword(map.get(conf.datasource_password).toString());
    p.setJmxEnabled(true);
    p.setTestWhileIdle(false);
    p.setTestOnBorrow(true);
    p.setValidationQuery("SELECT 1");
    p.setTestOnReturn(false);
    p.setLogValidationErrors(true);
    p.setValidationInterval(30000);
    p.setTimeBetweenEvictionRunsMillis(30000);
    p.setMaxActive(200);
    p.setInitialSize(Integer.valueOf(map.get(conf.datasource_initpoolsize).toString()));
    p.setMaxWait(10000);
//    p.setRemoveAbandonedTimeout(120);
    p.setMinEvictableIdleTimeMillis(30000);
    p.setMinIdle(10);
    p.setLogAbandoned(true);
    p.setRemoveAbandoned(false);
    p.setJdbcInterceptors(
      "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"+
      "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
    dataSource = new DataSource();
    dataSource.setPoolProperties(p);
  }

  private void testConnection(){
    Connection con = null;
    ResultSet rs = null;
    Statement st = null;
    try {
      con = dataSource.getConnection();      
      st = con.createStatement();
      rs = st.executeQuery("select 1");
      while (rs.next()) {
        logger.info("dbwriter plugin test connection success.");
      }
      rs.close();
      st.close();
    } catch (Exception e) {
      logger.error("dbwriter plugin test connection fail. " + e);
      System.exit(1);
    } finally {
      DBHelper.close(con, st, rs);
    }
  }

}
