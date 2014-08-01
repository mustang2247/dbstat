package com.ncfgroup.dbstat.core;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.ncfgroup.dbstat.util.Constant;
import com.ncfgroup.dbstat.write.DBWriter;

public class Worker implements Runnable
{
  private static Logger logger = Logger.getLogger(Worker.class);
  private BlockingQueue<Job> job_queue;

  public Worker(BlockingQueue<Job> job_queue){
    this.job_queue = job_queue;
  }

  @Override
  public void run()
  {
    logger.info("worker " + Thread.currentThread().getName() + " startup");
    Job job = null;
    try {
      job = job_queue.take();
      Map<String,Object> map = job.getContent();
      switch (job.getType()) {
        case DB:
          EventManager em = EventManager.getInstance();
          Event event = em.invoke(new Event(map,Process.SERVICE),PluginType.DBWRITER);
          Object context = event.getContext();
          map.put(Constant.writer_datasource, context);
          DBWriter.getInstance().write(job);
          break;
        case FS:
          break;
        case CACHE:
          break;
        default:
          throw new RuntimeException("Unknown job type!");
      }
    } catch (InterruptedException e) {
      logger.error("worker " + Thread.currentThread().getName() + " get job failed.");
      Thread.currentThread().interrupt();
    }
    logger.info("worker " + Thread.currentThread().getName() + " end");
  }
  

}
