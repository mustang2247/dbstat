package com.ncfgroup.dbstat.write;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;

import com.ncfgroup.dbstat.api.DeleteBuilder;
import com.ncfgroup.dbstat.api.InsertBuilder;
import com.ncfgroup.dbstat.api.SqlFilter;
import com.ncfgroup.dbstat.api.UpdateBuilder;
import com.ncfgroup.dbstat.core.Job;
import com.ncfgroup.dbstat.db.DBFactory;
import com.ncfgroup.dbstat.db.IGenericDAO;
import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.Constant;
import com.ncfgroup.dbstat.util.ReflectionUtil;
import com.ncfgroup.dbstat.util.SqlConf;
import com.ncfgroup.dbstat.util.StringUtil;


public final class DBWriter implements Writeable
{
  private static final Logger logger = Logger.getLogger(DBWriter.class);

  private static class DBWriterHolder{
    private final static DBWriter instance = new DBWriter();
  }

  public static DBWriter getInstance() {
    return DBWriterHolder.instance;
  }

  private DBWriter(){
  }

  @SuppressWarnings("unchecked")
  @Override
  public void write(Job job)
  {
    logger.info(Thread.currentThread().getName() +" dbwriter begin...");
    Map<String,Object> context = job.getContent();
    DataSource ds = (DataSource) context.get(Constant.writer_datasource);
    IGenericDAO dao = DBFactory.getInstance().buildGenericDAO(true, ds);
    for (String key : context.keySet()) {
      if(key.equalsIgnoreCase(Constant.writer_datasource)){
        continue;
      }
      List<Map<String,Object>> selectResult = (List<Map<String,Object>>) context.get(key);
      List<SqlConf> insertlist = ConfigHandler.insertSqlList;
      List<SqlConf> updatelist = ConfigHandler.updateSqlList;
      List<SqlConf> deletelist = ConfigHandler.deleteSqlList;
      List<String> insertsqls = getSqls(key,Constant.insert,insertlist,selectResult);
      List<String> updatesqls = getSqls(key,Constant.update,updatelist,selectResult);
      List<String> deletesqls = getSqls(key,Constant.delete,deletelist,selectResult);
      if (insertsqls != null && insertsqls.size() > 0){
        dao.executeBatch(insertsqls);
        logger.info("dbwriter " + Thread.currentThread().getName() +" build insert sql size : " + insertsqls.size());
      }
      if (updatesqls != null && updatesqls.size() > 0){
        dao.executeBatch(updatesqls);
        logger.info("dbwriter " + Thread.currentThread().getName() +" build update sql size : " + updatesqls.size());
      }
      if (deletesqls != null && deletesqls.size() > 0){
        dao.executeBatch(deletesqls);
        logger.info("dbwriter " + Thread.currentThread().getName() +" build delete sql size : " + deletesqls.size());
      }
      break;
    }
    logger.info(Thread.currentThread().getName() +" dbwriter end...");
  }

   public List<String> getSqls(String sqlid,String action,List<SqlConf> sclist,List<Map<String,Object>> selectResult){
     List<String> sqls = new ArrayList<String>();
     for(SqlConf sc : sclist){
       if (StringUtil.isNullOrBlank(sc.getClazz())) {
         if(sc.getRefid().equalsIgnoreCase(sqlid)){
           List<String> sqllist = SqlFilter.getInstance().getExecuteSqls(sc,selectResult);
           if (sqllist.size() > 0) {
             sqls.addAll(sqllist);
             logger.info("dbwriter " + Thread.currentThread().getName()+" build config sql refid : " +sc.getRefid()+ ",sql size : "+sqllist.size());
           } else {
             logger.warn("dbwriter " + Thread.currentThread().getName()+" build config sql refid : " +sc.getRefid()+ " is null. ");
           }           
         }
       } else {
         if (Constant.insert.equals(action)){
           InsertBuilder ib = (InsertBuilder)ReflectionUtil.getInstance(sc.getClazz());
           if (ib == null) {
             logger.error("dbwriter " + Thread.currentThread().getName() + " get classs insert sql object is null.");
             throw new RuntimeException("dbwriter " + Thread.currentThread().getName() + " get class insert sql insert object is null.");
           }
           String refid = null;
           if (!StringUtil.isNullOrBlank(sc.getRefid())){
             refid = sc.getRefid();
           }
           if(sqlid.equalsIgnoreCase(refid)){
             List<String> sqllist = ib.getInsertSqls(selectResult);
             if (sqllist.size() > 0) {
               sqls.addAll(sqllist);
               logger.info(Thread.currentThread().getName()+" build class insert sql refid : " +sc.getRefid()+ ",sql size : "+sqllist.size());
             } else {
               logger.warn(Thread.currentThread().getName()+" build class insert sql refid : " +sc.getRefid()+ " is null. ");
             }
           }
         } else if (Constant.update.equals(action)){
           UpdateBuilder ib = (UpdateBuilder)ReflectionUtil.getInstance(sc.getClazz());
           if (ib == null) {
             logger.error("dbwriter " + Thread.currentThread().getName() + " get class update sql object is null.");
             throw new RuntimeException("dbwriter " + Thread.currentThread().getName() + " get class update sql object is null.");
           }
           String refid = null;
           if (!StringUtil.isNullOrBlank(sc.getRefid())){
             refid = sc.getRefid();
           }
           if(sqlid.equalsIgnoreCase(refid)){
             List<String> sqllist = ib.getUpdateSqls(selectResult);
             if (sqllist.size() > 0) {
               sqls.addAll(sqllist);
               logger.info(Thread.currentThread().getName()+" build class update sql refid : " +sc.getRefid()+ ",sql size : "+sqllist.size());
             } else {
               logger.warn(Thread.currentThread().getName()+" build class update sql refid : " +sc.getRefid()+ " is null. ");
             }
           }
         } else if (Constant.delete.equals(action)){
           DeleteBuilder ib = (DeleteBuilder)ReflectionUtil.getInstance(sc.getClazz());
           if (ib == null) {
             logger.error("dbwriter " + Thread.currentThread().getName() + " get class delete sql object is null.");
             throw new RuntimeException("dbwriter " + Thread.currentThread().getName() + " get class delete sql object is null.");
           }
           String refid = null;
           if (!StringUtil.isNullOrBlank(sc.getRefid())){
             refid = sc.getRefid();
           }
           if(sqlid.equalsIgnoreCase(refid)){
             List<String> sqllist = ib.getDeleteSqls(selectResult);
             if (sqllist.size() > 0) {
               sqls.addAll(sqllist);
               logger.info(Thread.currentThread().getName()+" build class delete sql table : " + sc.getValue() + " size : "+sqllist.size());
             } else {
               logger.warn(Thread.currentThread().getName()+" build class delete sql table : " + sc.getValue() + "is null. ");
             }
           }
         }
       }
     }
     return sqls;
   }
}
