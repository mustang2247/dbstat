package com.ncfgroup.dbstat.util;

public final class JobHelper
{
  private static final String job_bind = "=";

  public static String generateJobId(String filename,String offset){
    if (StringUtil.isNullOrBlank(filename) || StringUtil.isNullOrBlank(offset)) {
      throw new RuntimeException("not exsit filename or offset field.");
    }
    return filename + job_bind + offset;
  }

  public static String[] splitJob(String job){
    if (StringUtil.isNullOrBlank(job)) {
      throw new NullPointerException("job name is null.");
    }
    return job.split(job_bind);
  }
}
