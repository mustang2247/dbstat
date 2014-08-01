package com.ncfgroup.dbstat.write;

import com.ncfgroup.dbstat.core.Job;

public interface Writeable
{
  void write(Job job);
}
