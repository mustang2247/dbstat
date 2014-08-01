package com.ncfgroup.dbstat.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.api.SelectBuilder;
import com.ncfgroup.dbstat.api.SqlFilter;
import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.ReflectionUtil;
import com.ncfgroup.dbstat.util.SqlConf;
import com.ncfgroup.dbstat.util.StringUtil;

public class AsyncController
{
  private static final Logger logger = Logger.getLogger(AsyncController.class);

  private static class AsyncControllerHolder
  {
    private final static AsyncController instance = new AsyncController();
  }

  public static AsyncController getInstance() {
    return AsyncControllerHolder.instance;
  }

  private AsyncController()
  {
  }
  private int reader_thread_count = Runtime.getRuntime().availableProcessors();
  private int writer_thread_count = Runtime.getRuntime().availableProcessors();
  private final BlockingQueue<Map<String,Object>> message = new LinkedBlockingQueue<Map<String,Object>>();
  private final BlockingQueue<Job> job_queue = new LinkedBlockingQueue<Job>();
//  private final ConcurrentHashMap<String,String> message = new ConcurrentHashMap<String,String>();

  public void startup(final Event event)
  {
    logger.info("async controller is startup...");
    ConfigHandler conf = (ConfigHandler)event.getContext();
    List<SqlConf> sqlList = conf.getSplitSelectSqlList();
    List<SqlConf> newSqlList = SqlFilter.getInstance().doSelect(sqlList);
    for (SqlConf sc : newSqlList) {
      if (StringUtil.isNullOrBlank(sc.getClazz())) {
        Map<String,Object> map = new HashMap<String,Object>();
        map.put(sc.getId(), sc);
        try {
          message.put(map);
        } catch (InterruptedException e) {
          logger.error(e);
        }
      } else {
        SelectBuilder sb = (SelectBuilder)ReflectionUtil.getInstance(sc.getClazz());
        sb.put(message);
      }
    }

    int size = message.size();
    logger.info("async handle reader_thread_count is "+reader_thread_count+", writer_thread_count is " + writer_thread_count);
    ExecutorService schedulerService = Executors.newCachedThreadPool();
    for (int i = 0; i < size; i++) {
      Scheduler schetask = new Scheduler(message,job_queue, event);
      schedulerService.execute(schetask);
    }
    ExecutorService workerService = Executors.newCachedThreadPool();
    for (int i = 0; i < size; i++) {
      Worker worker = new Worker(job_queue);
      workerService.execute(worker);
    }
  }

}
