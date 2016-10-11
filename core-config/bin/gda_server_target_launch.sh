#!/bin/bash
# This script is invoked when the gda_servers_core script has been run locally or has been traversed a
# second time as a result of starting the gda on the control machine from another machine via the ssh tunnel.

# It starts the gda-server pointed to by the server link under GDA_WORKSPACE_PARENT.

. ${GDA_WORKSPACE_PARENT}/${GDA_CORE_CONFIG_rel}/bin/bashlog_function

# light red text
function lr {
    echo -e "\033[1;31m$1\033[0m"
}

# Exit, displaying supplied error message in red text
function err_msg_exit {
    echo -e "\n$(lr "*** ERROR ***: $1")"
    exit 1
}

# Require the BEAMLINE environment variable to have been set
if [[ -z $BEAMLINE ]] ; then
    err_msg_exit "BEAMLINE environment variable not set - GDA server cannot start"
fi

# Resolve the input arguments - need the ':-' as beamline gda script uses set -o nounset
if [[ -n "${SSH_ORIGINAL_COMMAND:-}" ]]; then
    ARGS_IN="$SSH_ORIGINAL_COMMAND"
else
    ARGS_IN="$@"
fi

VALID_OPTIONS="--debug|--debug-wait|"
for word in $ARGS_IN; do
    if [[ "sword" == "--"* ]] && [[ "$VALID_OPTIONS" != *"|$word|"* ]]; then
        err_msg_exit "'$word' is not a valid option"
    fi
done
# Ensure directory containing workspaces exists, and is writable by everyone
USER_WORKSPACE_PARENT=~/scratch/gda_server_user_workspaces
[ -d $USER_WORKSPACE_PARENT ] || mkdir -m 777 $USER_WORKSPACE_PARENT

# Ensure directory containing configs exists, and is writable by everyone
ECLIPSE_RUNTIME_CONFIG_PARENT=~/scratch/gda_server_eclipse_configurations
[ -d $ECLIPSE_RUNTIME_CONFIG_PARENT ] || mkdir -m 777 $ECLIPSE_RUNTIME_CONFIG_PARENT

# Initialise the beamline specific config
BEAMLINE_CONFIG="$GDA_WORKSPACE_PARENT/${GDA_INSTANCE_CONFIG_rel:-config}"

########################
#  No need to select server path as a it's always workspace_parent/server
########################

# Set server application specifics - find absolute path
SERVER_INSTALL_PATH=$(readlink -f "${GDA_WORKSPACE_PARENT}/server")
SERVER_INSTALL_DIRNAME=$(basename "$SERVER_INSTALL_PATH")

# Initialise the java startup arguments, defaulting to dummy if env var not set
vm_args="-Dgda.mode=${GDA_MODE:-dummy} -Djava.awt.headless=true"

if [[ "$ARGS_IN" == *"--debug"* ]]; then
    [[ "$ARGS_IN" == *"--debug-wait"* ]] && wait="y" || wait="n"
    vm_args="$vm_args -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=${wait},address=8000"
fi

# Set user workspace and eclipse runtime configuration location (user and server build specific)
USER_WORKSPACE=$USER_WORKSPACE_PARENT/$(whoami)
ECLIPSE_RUNTIME_CONFIG_DIRNAME=$(whoami)-$SERVER_INSTALL_DIRNAME
ECLIPSE_RUNTIME_CONFIG=$ECLIPSE_RUNTIME_CONFIG_PARENT/$ECLIPSE_RUNTIME_CONFIG_DIRNAME

# Add java location to the path
module load java/gda90

#############################
# Start/Restart/Stop handling
#############################

MY_NAME=$(basename "$(readlink -e "$0")")         # The name of this script

# Write any error before attempting to launch/stop the application into the startup file used to determine
# whether the operation has completed. This allows the error message to be retrieved displayed on the workstation
# when running in live mode. The message is also written via the normal bashlog route.
#
function log_error_to_startup_file_and_exit {
    echo -e "\e[31m[$(date '+%F %T') $HOSTNAME ($MY_NAME)] ERROR: $1\e[0m" > $OBJECT_SERVER_STARTUP_FILE
    bashlog error "$MY_NAME" "$1"
    exit 1
}

function exit_servers_to_kill {
    log_error_to_startup_file_and_exit "You are attempting to start the GDA server for beamline $BEAMLINE but there are already $1 server(s) running on this machine ($HOSTNAME), please kill them manually or use the --restart option."
}

# Retrieve the server build version number from its install path (i.e. the last element of its path)
#
function set_target_server_matcher {
    local TOKENS=(${SERVER_INSTALL_PATH//\// })
    TARGET_SERVER_MATCHER=${TOKENS[${#TOKENS[@]}-1]}
}

# Check that all running servers that match the supplied grep pattern belong the current user exiting if not
#
function check_running_servers_ownership {
    readarray -t servers_arr < <(ps -ef | grep -E "$1")
    readarray -t my_servers_arr < <(ps -ef | grep -E "$1" | grep `whoami`)
    if [[ ${#servers_arr[@]} !=  ${#my_servers_arr[@]} ]]; then
        log_error_to_startup_file_and_exit "There are $2 servers started by other users running on this machine ($HOSTNAME), please examine and deal with them as appropriate before proceeding."
    fi
}

# Check that all servers in the array of those belonging to the current user were launched from
# the same deployment target as those about to be launched and from the same deployment directory,
# using the previously set matcher.
#
function check_matching_server_target {
    local alien_count=0
    pids_to_kill=""
    for server_proc in "${my_servers_arr[@]}"
    do
        if [[ -z $(grep "$1" <<< "${server_proc}") ]]; then
            ((alien_count++))
        else
            pid=$(awk '{ print $2 }' <<< $server_proc)
            echo "pid: ${pid}"
            pids_to_kill+=" $pid"
        fi
    done
    if [[ $alien_count > 0 ]]; then
        [[ $alien_count == 1 ]] && conjugate="is" || conjugate="are"
        log_error_to_startup_file_and_exit "$alien_count of the running GDA Servers on ($HOSTNAME) $conjugate based on a different $2 than the one you are trying to restart/stop. Please examine and deal with this as appropriate before proceeding."
    fi
}

# Kill the process indicated by parameter 1 then sleep for parameter 2 seconds before checking
# that the kill has been successful. If not, use kill -9 on the process. Parameter 3 is the logging
# prefix for the initial kill attempt.
#
function kill_with_SIGKILL_if_necessary {
    bashlog info "$MY_NAME" "$3 $1"
    kill $1
    sleep $2
    if [[ "$(ps -p $1)" == *"$1 "* ]]; then
        bashlog info "$MY_NAME" "SIGKILL required for $1"
        kill -9 $1
    fi
}

ALL_RUNNING_GDA_OSGI_SERVERS='\-name [G]da-server'
ALL_RUNNING_SUBORDINATE_SERVERS='org.jacorb.naming.[N]ameServer|gda.factory.corba.util.[C]hannelServer|gda.util.[L]ogServer'

START_ONLY_PATTERN="\bstart\b"
RESTART_OR_STOP_PATTERN="\brestart\b|\bstop\b"

# Ensure if "--start" option is selected, the presence of any running GDA servers or Name/Channel/Log servers aborts the requested operation
#
if [[ "$ARGS_IN" =~ $START_ONLY_PATTERN  ]]; then
    if [[ -n "$(ps -ef | grep "$ALL_RUNNING_GDA_OSGI_SERVERS")" ]]; then
        exit_servers_to_kill "GDA"
    fi
    if [[ -n "$(ps -ef | grep -E "$ALL_RUNNING_SUBORDINATE_SERVERS")" ]]; then
        exit_servers_to_kill "Name, Channel and/or Log"
    fi

# If "--restart" or "--stop" were specified only proceed if the GDA server and Name/Channel/Log server processes to be terminated were
# started by the current user from the same deployment target as that from which the operation is being requested .
#
elif [[ "$ARGS_IN" =~ $RESTART_OR_STOP_PATTERN ]]; then
    set_target_server_matcher
    check_running_servers_ownership "$ALL_RUNNING_GDA_OSGI_SERVERS" "GDA"
    check_matching_server_target ${TARGET_SERVER_MATCHER} "target"
    OSGI_SERVER_PIDS_TO_KILL=$pids_to_kill
    check_running_servers_ownership "$ALL_RUNNING_SUBORDINATE_SERVERS" "Name, Channel and/or Log"
    check_matching_server_target ${GDA_WORKSPACE_PARENT} "deployment"
    for pid in $OSGI_SERVER_PIDS_TO_KILL; do
        kill_with_SIGKILL_if_necessary "$pid" "8" "Shutting down"
    done

    # the above should also have got rid of the Log, Channel and Name Servers, but just in case:
    for pid in $pids_to_kill; do
        if [[ "$(ps -p $pid)" == *"$pid "* ]]; then
            kill_with_SIGKILL_if_necessary "$pid" "1" "Cleaning up"
        fi
    done
    if [[ "$ARGS_IN" == *"stop"* ]]; then
        exit 0
    fi
else
    log_error_to_startup_file_and_exit "No valid start/restart/stop option specified"
fi

if [[ -z "${GDA_PROFILES:-}" ]]; then
	GDA_PROFILES=main
fi

# now we must be restarting or starting only
# Assemble the command string

COMMAND="${GDA_WORKSPACE_PARENT}/server/gda-server -clean -data $USER_WORKSPACE -configuration $ECLIPSE_RUNTIME_CONFIG -c $BEAMLINE_CONFIG -p $GDA_PROFILES -vmArgs $vm_args"

# and execute it retaining stdin
echo "Starting the GDA Server at $SERVER_INSTALL_PATH/gda-server"
echo $COMMAND
$COMMAND &
