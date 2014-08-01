package com.ncfgroup.dbstat.util;

import java.util.Date;

public class TimerConf
{
  private Long interval;
  private Date starttime;
  private String id;
  
  public TimerConf(String id,Date starttime,Long interval){
    this.id = id;
    this.starttime = starttime;
    this.interval = interval;
  }

  public String getNextDate(){
    String intervalstr = interval.toString();
    if("2592000000".equals(intervalstr)){//month interval
      return DateUtil.getNextMonthFirstDay();
    }else if("604800000".equals(intervalstr)){//week interval
      return DateUtil.getNextWeekFirstDay();
    }else if("86400000".equals(intervalstr)){//day interval
      return DateUtil.getTomorrow();
    }else{//other
      Date d = DateUtil.addMillSecond(starttime, Integer.valueOf(intervalstr));
      String str = DateUtil.dateToStr(d);
      return str;
    }
  }

  public Long getInterval()
  {
    return interval;
  }
  public void setInterval(Long interval)
  {
    this.interval = interval;
  }
  public Date getStarttime()
  {
    return starttime;
  }
  public void setStarttime(Date starttime)
  {
    this.starttime = starttime;
  }
  public String getId()
  {
    return id;
  }
  public void setId(String id)
  {
    this.id = id;
  }

  

}
