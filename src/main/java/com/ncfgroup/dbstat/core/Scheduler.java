package com.ncfgroup.dbstat.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.read.DBReader;
import com.ncfgroup.dbstat.util.ConfigHandler;
import com.ncfgroup.dbstat.util.Constant;


public class Scheduler implements Runnable
{
  private static final Logger logger = Logger.getLogger(Scheduler.class);
  private BlockingQueue<Job> job_queue;
  private Event event;
  private BlockingQueue<Map<String,Object>> message;

  public Scheduler(BlockingQueue<Job> job_queue,Event event){
    this.job_queue = job_queue;
    this.event = event;
  }

  public Scheduler(BlockingQueue<Map<String,Object>> message,BlockingQueue<Job> job_queue,Event event){
    this.message = message;
    this.job_queue = job_queue;
    this.event = event;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void run()
  {
    logger.info("scheduler " + Thread.currentThread().getName() + " begin to handle job ");
    try {
      Map<String,Object> map = message.take();
      logger.info("scheduler " + Thread.currentThread().getName() + " handle " + map.keySet().toString() + " select sql");
      ConfigHandler config = (ConfigHandler) event.getContext();
      StorageType type = (StorageType)config.getReaderMap().get(config.type);
      Job job = null;
      switch(type){
        case DB:
          EventManager em = EventManager.getInstance();
          Event event = em.invoke(new Event(map,Process.SERVICE),PluginType.DBREADER);
          Object context = event.getContext();
          map.put(Constant.reader_datasource, context);
          Job dbjob = new Job(StorageType.DB,map);
          List<?> list = DBReader.getInstance().read(dbjob);
          if(list != null && list.size()>0){
            map.remove(Constant.reader_datasource);
            String key = map.keySet().iterator().next();
            map.put(key, list);
            job = new Job(StorageType.DB,map);
          }else{
            logger.warn("scheduler " + Thread.currentThread().getName() + " dbreader get result is null. current thread is stop. ");
            Thread.currentThread().stop();
          }
          break;
        case FS:
          break;
        case CACHE:
          break;
        default:
          throw new RuntimeException("Unknown job type!");  
      }
      job_queue.put(job);
    } catch (InterruptedException e) {
      logger.error("scheduler " + Thread.currentThread().getName() + " create job is blocked");
      Thread.currentThread().interrupt();
    }
    logger.info("scheduler " + Thread.currentThread().getName() + " handle finish.");
  }
}
