package com.ganqiang.dbstat.db;

import java.sql.Connection;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

public class DBFactory
{
  private static final Logger logger = Logger.getLogger(GenericDAO.class);
  private static IGenericDAO genericDAO = null;
  
  private DBFactory(){}
  private static class DBFactoryHolder
  {
    private final static DBFactory instance = new DBFactory();
  }

  public static DBFactory getInstance() {
    return DBFactoryHolder.instance;
  }
  
  private Connection getConnection(boolean isAsync,DataSource dataSource) {
    Connection conn = null;
    try {
      if(isAsync){
        Future<Connection> future = dataSource.getConnectionAsync();
        while (!future.isDone()) {
          logger.info("Job " +Thread.currentThread().getName()+ " connection is not yet available.It will auto sleep for 1 ms.");
          try {
            Thread.sleep(1000);
          } catch (InterruptedException x) {
            logger.error("Job " + Thread.currentThread().getName() + " auto sleep is blocked.",x);
          }
        }
        conn = future.get();
      } else {
        conn = dataSource.getConnection();
      }
    } catch (Exception e) {
      logger.error("Job " + Thread.currentThread().getName() + " db connection is fault.",e);
    }
    return conn;
  }

  public synchronized IGenericDAO buildGenericDAO(boolean isAsync,DataSource ds){
    genericDAO = new GenericDAO(getConnection(isAsync,ds));
    return genericDAO;
  }

  public synchronized IGenericDAO buildGenericDAO(DataSource ds){
    genericDAO = new GenericDAO(getConnection(true,ds));
    return genericDAO;
  }
}
