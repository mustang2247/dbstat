#!/bin/sh

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

EXECUTABLE=command.sh
exec $DBSTAT_HOME/bin/"$EXECUTABLE" stop "$@"
