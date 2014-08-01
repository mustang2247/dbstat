#!/bin/sh

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set DBSTAT_HOME if not already set
[ -z "$DBSTAT_HOME" ] && DBSTAT_HOME=`cd "$PRGDIR/.." ; pwd`

if [ -r "$DBSTAT_HOME"/bin/bootstrap.sh ]; then
  BASEDIR="$DBSTAT_HOME"
  . "$DBSTAT_HOME"/bin/bootstrap.sh
else
  echo "Cannot find $DBSTAT_HOME/bin/bootstrap.sh"
  echo "This file is needed to run this program"
  exit 1
fi

if [ -x "$DBSTAT_HOME"/bin/bootstrap.sh ]; then
  . "$DBSTAT_HOME"/bin/bootstrap.sh 
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$DBSTAT_HOME" ] && DBSTAT_HOME=`cygpath --unix "$DBSTAT_HOME"`
  [ -n "$DBSTAT_BASE" ] && DBSTAT_BASE=`cygpath --unix "$DBSTAT_BASE"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# Now switch to home directory of echo
cd $DBSTAT_HOME

if [ -z "$JAVA_OPTS" ]; then
  JAVA_OPTS="-server -Xms2560m -Xprof -Xmx2560m -Xmn2g -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Djcore.parser=SAX"
else
  JAVA_OPTS="$JAVA_OPTS -server -Xms2560m -Xprof -Xmx2560m -Xmn2g -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
fi

export DBSTAT_PID=$DBSTAT_HOME/dbstat.pid
export DBSTAT_MONITOR_PID=$DBSTAT_HOME/monitor.pid
export DBSTAT_PRGNAME="dbstat"

check_process_exists()
{
  if [ -f "$DBSTAT_PID" ]; then
    pid=`cat $DBSTAT_PID`
    [ -z $pid ] &&  pid="-1"
    kill -0 $pid > /dev/null 2>&1
    find_pids=`ps aux | grep $DBSTAT_PRGNAME | grep -v 'grep' | awk '{print $2}'`
    for find_pid in $find_pids
    do  
      if [ $find_pid = $pid ]; then
        echo "Process exists, can't start or run new process"
        exit 0
      fi  
    done
  fi
}

if [ "$1" = "start" ] ; then
  check_process_exists
  if [ ! -d "$DBSTAT_HOME"/log ]; then
    mkdir $DBSTAT_HOME/log
  fi

  while true;
  do 
    $DBSTAT_HOME/bin/monitor.sh $RUNJAVA -classpath $CLASSPATH $JAVA_OPTS com.ncfgroup.dbstat.DBStat start
    code=$?
    #echo "DB-Stat is terminated and exit code is " $code
    if [ $code == 0 ]; then
      exit $code
    fi
    echo "DB-Stat is going to be restarted 5 seconds layer"
    sleep 5;
  done &
  echo $! > $DBSTAT_MONITOR_PID

  echo "Start success."
elif [ "$1" = "stop" ] ; then
  if [ -f "$DBSTAT_PID" ]; then
    $RUNJAVA -classpath $CLASSPATH $JAVA_OPTS\
      com.ncfgroup.dbstat.DBStat stop

    kill -9 `cat $DBSTAT_MONITOR_PID` > /dev/null 2>&1 | xargs rm -rf $DBSTAT_MONITOR_PID
    kill -9 `cat $DBSTAT_PID` > /dev/null 2>&1 | xargs rm -rf $DBSTAT_PID
    #ps -ef | grep $DBSTAT_PRGNAME | awk '{print $2}' | xargs kill -9 > /dev/null 2>&1 &
    #rm $DBSTAT_PID
    echo "Stop success."
  else
    ps -ef | grep $DBSTAT_PRGNAME | awk '{print $2}' | xargs kill -9 > /dev/null 2>&1 &
    #echo "Stop failed: $DBSTAT_PID not exist"
  fi
elif [ '$1' = 'helps' ] ; then
  more <<'EOF'
Thank for choosing DB Stat Software
==============================================
All rights are reserved by UCF Group INC.
Version : beta-1.0.0
Author  : Gan.Qiang

Usage: 
startup.sh       startup the db stat
stop.sh          stop the db stat
==============================================
EOF
fi

