package com.ncfgroup.dbstat.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.ncfgroup.dbstat.core.StorageType;

public class ConfigHandler
{
  private static final Logger logger = Logger.getLogger(ConfigHandler.class);

  private final String LOG4J_PATH = System.getProperty("user.dir")+"/conf/log4j.conf";
  private final String xmlconf = System.getProperty("user.dir")+"/conf/db-stat.xml";
  private final String xsdconf = System.getProperty("user.dir")+"/conf/db-stat.xsd";
  private static final String root = "db-stat";
  public static final String timer = "//"+root+"/"+"timer"+"/.";
  private final String reader = "//"+root+"/"+"reader"+"/.";
  private final String writer = "//"+root+"/"+"writer"+"/.";
  private final String instance = "instance";
  private final String interval = "interval";
  private final String starttime = "starttime";
  public final String type = "type";
  public final String datasource_driverclassname = "datasource.driverclassname";
  public final String datasource_url = "datasource.url";
  public final String datasource_username = "datasource.username";
  public final String datasource_password = "datasource.password";
  public final String datasource_initpoolsize = "datasource.initpoolsize";
  private static String size = "10";// default pool init size
  private final String select = "select";
  private final String sql = "sql";
  private final String id = "id";
  private final String tid = "tid";
  private final String clazz = "class";
  private final String value = "value";
  private final String refid = "refid";
  private final String insert = "insert";
  private final String update = "update";
  private final String delete = "delete";
  private Map<String,TimerConf> timerMap = null;
  private Map<String,Object> readerMap = null;
  private Map<String,Object> writerMap = null;
  public static Map<String,List<SqlConf>> selectSqlMap = null;//key: tid
  public static List<SqlConf> selectSqlList = null;
  public static List<SqlConf> insertSqlList = null;
  public static List<SqlConf> updateSqlList = null;
  public static List<SqlConf> deleteSqlList = null;
  private List<SqlConf> splitSelectSqlList = null;
  
  public ConfigHandler(){
    PropertyConfigurator.configure(LOG4J_PATH);
    logger.info("loading log4j.conf file...");
    timerMap = new HashMap<String,TimerConf>();
    readerMap = new HashMap<String,Object>();
    writerMap = new HashMap<String,Object>();
    selectSqlMap = new HashMap<String,List<SqlConf>>();
    selectSqlList = new ArrayList<SqlConf>();
    insertSqlList = new ArrayList<SqlConf>();
    updateSqlList = new ArrayList<SqlConf>();
    deleteSqlList = new ArrayList<SqlConf>();
    XMLHandler xml = new XMLHandler();
    Validate(xml);
    Document doc = xml.loadXML(xmlconf);
    read(xml,doc);
  }
  
  public void Validate(XMLHandler xml){
    boolean flag = xml.Validate(xsdconf, xmlconf);
    if(!flag){
      logger.error("XML file verification failed.");
      System.exit(1);
    }else{
      logger.info("Xml file has been successfully verified.");
    }
  }
  
  public String getXmlConf(){
    return xmlconf;
  }

  public Map<String, TimerConf> getTimerMap(){
    return timerMap;
  }

  public Map<String, Object> getReaderMap(){
    return readerMap;
  }

  public Map<String, Object> getWriterMap(){
    return writerMap;
  }
  
  public Map<String, List<SqlConf>> getSelectSqlMap()
  {
    return selectSqlMap;
  }

  public List<SqlConf> getSplitSelectSqlList()
  {
    return splitSelectSqlList;
  }

  public void setSplitSelectSqlList(List<SqlConf> splitSelectSqlList)
  {
    this.splitSelectSqlList = splitSelectSqlList;
  }

  public List<SqlConf> getSelectSqlList(String tid){
    return selectSqlMap.get(tid);
  }
  
  public List<SqlConf> getInsertSqlList(){
    return insertSqlList;
  }
  
  public List<SqlConf> getUpdateSqlList(){
    return updateSqlList;
  }
  
  public List<SqlConf> getDeleteSqlList(){
    return deleteSqlList;
  }

  @SuppressWarnings("unchecked")
  public void read(XMLHandler xml,Document doc){
    Element timerNode = xml.getNode(doc, timer);
    List<Element> instanceNodes = timerNode.elements(instance);
    for(Element ele : instanceNodes){
      String idvalue = ele.attributeValue(id);
      String intervalvalue = ele.elementText(interval);
      String starttimevalue = ele.elementText(starttime);
      Long interval = null;
      if (StringUtil.isNullOrBlank(intervalvalue) || intervalvalue.equalsIgnoreCase("0")) {
        interval = null;
      }else if (!ArithUtil.isNumeric(intervalvalue)) {
        interval = Long.valueOf(ArithUtil.parseExp(intervalvalue));
      }
      boolean flag = DateUtil.checkDate(starttimevalue);
      Date stime = null;
      if (flag) {
        stime = DateUtil.strToDate(starttimevalue);
        if(stime.before(new Date())){
          logger.info("configuration file format is fault:<starttime> of instance id="+idvalue+", Statistical start time has expired");
//          System.exit(1);
          continue;
        }
      } else {
        logger.error("configuration file format is fault:<starttime> of instance id="+idvalue);
        System.exit(1);
      }
      TimerConf tc = new TimerConf(idvalue,stime,interval);
      timerMap.put(idvalue, tc);
    }
    
    if (timerMap == null || timerMap.isEmpty()){
      logger.error("configuration file format is fault: all instance's starttime have expired");
      System.exit(1);
    }
    
    Element readerNode = xml.getNode(doc, reader);
    readerMap.put(type, StorageType.DB);
    readerMap.put(datasource_driverclassname, readerNode.element("datasource").elementText("driverclassname"));
    readerMap.put(datasource_url, readerNode.element("datasource").elementText("url"));
    readerMap.put(datasource_username, readerNode.element("datasource").elementText("username"));
    readerMap.put(datasource_password, readerNode.element("datasource").elementText("password"));
    String rinitpoolsize = readerNode.element("datasource").elementText("initpoolsize");
    if (!StringUtil.isNullOrBlank(rinitpoolsize)) {
      if (Integer.valueOf(rinitpoolsize) > 0) {
        size = rinitpoolsize;
      }
    }
    readerMap.put(datasource_initpoolsize, size);

    List<Element> selectlist = readerNode.element(select).elements(sql);
    List<String> idlist = new ArrayList<String>();
    List<SqlConf> sclist = new ArrayList<SqlConf>();
    for (Element ele : selectlist) {
      String idvalue = ele.attributeValue(id);
      String tidvalue = ele.attributeValue(tid);
      if (idlist.contains(idvalue)) {
        logger.error("configuration file format is fault.<select>-<sql>-id attribute repeat configuration.");
        System.exit(1);
      }
      if (!timerMap.containsKey(tidvalue)) {
        logger.info("configuration file format is fault.<select>-<sql>-tid attribute of id="+idvalue+" is unknown.");
//        System.exit(1);
        continue;
      }
      idlist.add(idvalue);
      SqlConf sql = new SqlConf();
      for (Object sub : ele.attributes()) {
        Node node = (Node)sub;
        if(id.equals(node.getName())){
          sql.setId(node.getStringValue());
        }else if(clazz.equals(node.getName())){
          sql.setClazz(node.getStringValue().trim());
        }else if(value.equals(node.getName())){
          sql.setValue(node.getStringValue());
        }else if(tid.equals(node.getName())){
          sql.setTid(node.getStringValue());
        }
      }
      if (ele.element(value)!=null && !StringUtil.isNullOrBlank(ele.getStringValue())){
        sql.setValue(ele.getStringValue());
      } else if(ele.element(clazz)!=null && !StringUtil.isNullOrBlank(ele.getStringValue()) ){
        sql.setClazz(ele.getStringValue().trim());
      }
      if (selectSqlMap == null || selectSqlMap.isEmpty()){
        sclist.add(sql);
        selectSqlMap.put(tidvalue, sclist);
      } else {
        if (selectSqlMap.containsKey(tidvalue)){
          List<SqlConf> scl = selectSqlMap.get(tidvalue);
          scl.add(sql);
          selectSqlMap.put(tidvalue, scl);
        } else {
          sclist = new ArrayList<SqlConf>();
          sclist.add(sql);
          selectSqlMap.put(tidvalue, sclist);
        }
      }
      selectSqlList.add(sql);
    }
    Element writerNode = xml.getNode(doc, writer);
    
    writerMap.put(type, StorageType.DB);
    writerMap.put(datasource_driverclassname, writerNode.element("datasource").elementText("driverclassname"));
    writerMap.put(datasource_url, writerNode.element("datasource").elementText("url"));
    writerMap.put(datasource_username, writerNode.element("datasource").elementText("username"));
    writerMap.put(datasource_password, writerNode.element("datasource").elementText("password"));
    String winitpoolsize = readerNode.element("datasource").elementText("initpoolsize");
    if (!StringUtil.isNullOrBlank(winitpoolsize)) {
      if (Integer.valueOf(winitpoolsize) < 0) {
        size = winitpoolsize;
      }
    }
    writerMap.put(datasource_initpoolsize, size);
    loadWriterSql(writerNode.element(insert),insertSqlList);
    loadWriterSql(writerNode.element(update),updateSqlList);
    loadWriterSql(writerNode.element(delete),deleteSqlList);
    
    idlist.clear();
    idlist = null;
  }
  
  private void loadWriterSql(Element e,List<SqlConf> sqlList){
    if (e != null) {
      @SuppressWarnings("unchecked")
      List<Element> elist = e.elements(sql);
      for (Element ele : elist) {
        SqlConf sql = new SqlConf();
        for (Object sub : ele.attributes()) {
          Node node = (Node)sub;
          if(id.equals(node.getName())){
            sql.setId(node.getStringValue());
          }else if(clazz.equals(node.getName())){
            sql.setClazz(node.getStringValue());
          }else if(value.equals(node.getName())){
            sql.setValue(node.getStringValue());
          }else if(refid.equals(node.getName())){
            sql.setRefid(node.getStringValue());
          }
        }
        if (ele.element(value)!=null && !StringUtil.isNullOrBlank(ele.getStringValue())){
          sql.setValue(ele.getStringValue());
        } else if(ele.element(clazz)!=null && !StringUtil.isNullOrBlank(ele.getStringValue()) ){
          sql.setClazz(ele.getStringValue().trim());
        }
        sqlList.add(sql);
      }
    }
  }

  public static void main(String... args){
    ConfigHandler conf = new ConfigHandler();
    for(String key : selectSqlMap.keySet()){
      System.out.println(key+"   "+selectSqlMap.get(key).size());
    }
  }
  
}
