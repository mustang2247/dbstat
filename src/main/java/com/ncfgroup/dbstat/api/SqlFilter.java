package com.ncfgroup.dbstat.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.util.DateUtil;
import com.ncfgroup.dbstat.util.SqlConf;
import com.ncfgroup.dbstat.util.StringUtil;

public class SqlFilter
{
  private static final Logger logger = Logger.getLogger(SqlFilter.class);
  private static class SqlFilterHolder{
    private final static SqlFilter instance = new SqlFilter();
  }
  public static SqlFilter getInstance() {
    return SqlFilterHolder.instance;
  }
  private static List<String> builtinkeys;
  
  static{
    builtinkeys = new ArrayList<String>();
    builtinkeys = new ArrayList<String>();
    builtinkeys.add("yesterday");
    builtinkeys.add("today");
    builtinkeys.add("lasthour");
    builtinkeys.add("hour");
    builtinkeys.add("weekofyear");
    builtinkeys.add("weekofmonth");
    builtinkeys.add("lastweek");
    builtinkeys.add("lastweekofyear");
    builtinkeys.add("lastweekfirstday");
    builtinkeys.add("lastweekendday");
    builtinkeys.add("lastmonth");
    builtinkeys.add("month");
    builtinkeys.add("monthofyear");
    builtinkeys.add("lastmonthofyear");
    builtinkeys.add("lastyear");
    builtinkeys.add("year");
    builtinkeys.add("uuid");
  }

  private static List<String> getKeysFromSql(String sql){
    if (StringUtil.isNullOrBlank(sql)){
      throw new RuntimeException("sql is null");
    }
    List<String> strlist = new ArrayList<String>();
    Pattern pattern2 = Pattern.compile("\\{[^}]+\\}");
    Matcher m2 = pattern2.matcher(sql);
    while (m2.find()){
      String tmp = m2.group().replaceAll("[\\{\\}]", "");
      strlist.add(tmp);
    }
    return strlist;
  }

  private String getVariableValue(String str){
    if (StringUtil.isNullOrBlank(str)){
      throw new RuntimeException("sql is null");
    }
    String newStr = str.trim();
    if("yesterday".equalsIgnoreCase(newStr)){
      return DateUtil.getYesterday();
    }else if("today".equalsIgnoreCase(newStr)){
      return DateUtil.getToday();
    }else if("lasthour".equalsIgnoreCase(newStr)){
      return DateUtil.getLasthour();
    }else if("hour".equalsIgnoreCase(newStr)){
      return DateUtil.getHour();
    }else if("lastweek".equalsIgnoreCase(newStr)){
      return DateUtil.getLastWeek();
    }else if("lastweekday".equalsIgnoreCase(newStr)){
      return DateUtil.getLastWeekDay();
    }else if("lastmonth".equalsIgnoreCase(newStr)){
      return DateUtil.getLastMonth();
    }else if("month".equalsIgnoreCase(newStr)){
      return DateUtil.getMonth();
    }else if("lastyear".equalsIgnoreCase(newStr)){
      return DateUtil.getLastYear();
    }else if("year".equalsIgnoreCase(newStr)){
      return DateUtil.getYear();
    }else if("uuid".equalsIgnoreCase(newStr)){
      return UUID.randomUUID().toString();
    }else if("lastweekofyear".equalsIgnoreCase(newStr)){
      return String.valueOf(DateUtil.getLastWeekOfYear());
    }else if("lastweekfirstday".equalsIgnoreCase(newStr)){
      return DateUtil.getLastWeekFirstDay();
    }else if("lastweekendday".equalsIgnoreCase(newStr)){
      return DateUtil.getLastWeekEndDay();
    }else if("lastmonthofyear".equalsIgnoreCase(newStr)){
      return String.valueOf(DateUtil.getLastMonthOfYear());
    }else if("weekofyear".equalsIgnoreCase(newStr)){
      return String.valueOf(DateUtil.getWeekOfYear());
    }else if("weekofmonth".equalsIgnoreCase(newStr)){
      return String.valueOf(DateUtil.getWeekOfMonth());
    }else if("monthofyear".equalsIgnoreCase(newStr)){
      return String.valueOf(DateUtil.getMonthOfYear());
    }
    return null;
  }

  public List<SqlConf> doSelect(List<SqlConf> sqlList){
    List<SqlConf> newSqlList = new ArrayList<SqlConf>();
    for(SqlConf sql : sqlList){
      String sqlvalue = sql.getValue();
      logger.info("Before execute doselect filter, sql value : " + sqlvalue);
      List<String> sqlkeys = getKeysFromSql(sqlvalue);
      for(String sqlkey : sqlkeys){
        for (String systemkey : builtinkeys) {
          if (systemkey.equalsIgnoreCase(sqlkey)) {
            String value = getVariableValue(sqlkey);
            sqlvalue = sqlvalue.replace("#{"+sqlkey+"}", value);
          }
        }
      }
      SqlConf newsql = new SqlConf();
      newsql.setValue(sqlvalue);
      newsql.setClazz(sql.getClazz());
      newsql.setRefid(sql.getRefid());
      newsql.setTid(sql.getTid());
      newsql.setId(sql.getId());
      newSqlList.add(newsql);
      logger.info("After execute doselect filter, sql value : " + sqlvalue);
    }
    return newSqlList;
  }

  public  List<String> getExecuteSqls(SqlConf writerSqlConf,List<Map<String,Object>> selectResult){
    String value = writerSqlConf.getValue();
    List<String> keys = getKeysFromSql(value);
    List<String> sqls = new ArrayList<String>();
    for(Map<String,Object> map : selectResult){
      String sql = value;
      for(String key : map.keySet()) {
        for(String sqlkey : keys) {
          if(sqlkey.equalsIgnoreCase(key)){
            Object columnvalue = map.get(key);
            if (columnvalue == null) {
              sql = sql.replace("${"+sqlkey+"}", "");
            } else {
              sql = sql.replace("${"+sqlkey+"}", columnvalue.toString());
            }
          }
          for (String var : builtinkeys) {
            if (var.equalsIgnoreCase(sqlkey)) {
              String newvalue = getVariableValue(var);
              sql = sql.replace("#{"+sqlkey+"}", newvalue);
            }
          }
        }
      }
      logger.info(Thread.currentThread().getName() + " get executed sql : " + sql);
      sqls.add(sql);
    }
    return sqls;
  }

  public static void main(String... args){    
    SqlConf sc = new SqlConf();
    sc.setValue("select aa from aaa #{yesterDay} ${yesteraDay}");
    List<SqlConf> sclist=new ArrayList<SqlConf>();
    sclist.add(sc);
    for(SqlConf sca : sclist){
      System.out.println(sca.getValue());
    }
    
    System.err.println(SqlFilter.getInstance().getVariableValue("uuid"));
    System.err.println(SqlFilter.getInstance().getVariableValue("weekofmonth"));
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    System.err.println(SqlFilter.getInstance().getVariableValue("yesterday"));
  }

}
