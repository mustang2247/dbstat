#!/bin/bash
echo $$ > $DBSTAT_PID
exec $@
