package com.ncfgroup.dbstat.read;

import com.ncfgroup.dbstat.core.Job;

public interface Readable
{
  <T> T read(Job job);
}
