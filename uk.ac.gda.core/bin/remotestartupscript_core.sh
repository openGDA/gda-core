#!/bin/bash

export GDA_MODE=live

GDA_VAR=$(readlink -f $GDA_WORKSPACE_PARENT/../var)

. $GDA_FACILITY_CONFIG/bin/loadjava.sh
. $GDA_FACILITY_CONFIG/bin/gda_setup_logging gda_servers_output

umask 0002
# This should fix a problem where sub-directories created in a visit folder end up
# with a different mask to the default.

GDA_CORE_SCRIPT=$GDA_CORE_CONFIG/bin/gda
export JAVA_OPTS="-Dgda.deploytype=1 -XX:MaxPermSize=1024m" # Seems to fix the reset_namespace problem
GDA_CORE_SCRIPT_OPTIONS="--headless servers --debug --debugport=8001"

echo  $GDA_CORE_SCRIPT $GDA_CORE_SCRIPT_OPTIONS
echo  $GDA_CORE_SCRIPT $GDA_CORE_SCRIPT_OPTIONS >> $GDA_CONSOLE_LOG
nohup $GDA_CORE_SCRIPT $GDA_CORE_SCRIPT_OPTIONS >> $GDA_CONSOLE_LOG  2>&1 &

# look for the output file which will tell us when the servers have started
OBJECT_SERVER_STARTUP_FILE=/$GDA_VAR/object_server_startup_server_main
/$GDA_FACILITY_CONFIG/bin/utils/lookForFile $OBJECT_SERVER_STARTUP_FILE $HOSTNAME

echo Completed $0