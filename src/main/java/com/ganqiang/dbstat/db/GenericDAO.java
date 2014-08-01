package com.ganqiang.dbstat.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class GenericDAO implements IGenericDAO
{
  private static final Logger logger = Logger.getLogger(GenericDAO.class);
  
  private Connection con;
  
  public GenericDAO(Connection con){
    this.con = con;
  }

  @Override
  public List<Map<String,Object>> find(final String sql)
  {
    Map<String,Object> rowData = null;
    List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    try {
      pstmt = con.prepareStatement(sql);
      rs=pstmt.executeQuery();
      ResultSetMetaData md = rs.getMetaData();
      int columnCount = md.getColumnCount();
      while (rs.next()){   
          rowData = new HashMap<String,Object>(columnCount);   
          for (int i=1; i<=columnCount; i++){   
            rowData.put(md.getColumnLabel(i).toUpperCase(),rs.getObject(i));
          }
          list.add(rowData);   
      }
    } catch (Exception e) {e.printStackTrace();
      logger.error("Cannot to excute find method by sql : "+sql, e);
    } finally{
      DBHelper.close(con, rs, pstmt);
    }
    return list;
  }

  public int[] executeBatch(final List<String> sqls)
  {
    int[] count = {};
    Statement stmt = null;
    ResultSet rs = null;
    try {
      DBHelper.openTransaction(con);
      stmt = con.createStatement();
      for(String sql : sqls){
        stmt.addBatch(sql);
      }
      count = stmt.executeBatch();
      DBHelper.commit(con);
    } catch (SQLException e) {
      DBHelper.rollback(con);
      logger.error("Cannot to excute insert method by sql : "+sqls, e);
    } finally{
      DBHelper.close(con, stmt,rs);
    }
    return count;
  }
}
