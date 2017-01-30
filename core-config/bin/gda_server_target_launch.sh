#!/bin/bash
# This script is invoked when the gda_servers_core script has been run locally or has been traversed a
# second time as a result of starting the gda on the control machine from another machine via the ssh tunnel.

# It starts the gda-server pointed to by the server link under GDA_WORKSPACE_PARENT.

. ${GDA_WORKSPACE_PARENT}/${GDA_CORE_CONFIG_rel}/bin/bashlog_function

# Write any error before attempting to launch/stop the application into the startup file used to determine
# whether the operation has completed. This allows the error message to be retrieved displayed on the workstation
# when running in live mode. The message is also written via the normal bashlog route.
#
function log_error_to_startup_file_and_exit {
    echo "$(date '+%F %T') $HOSTNAME ($MY_NAME) ERROR: $1" > $OBJECT_SERVER_STARTUP_FILE
    bashlog error "$MY_NAME" "$1"
    exit 0
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
        fi
        pid=$(awk '{ print $2 }' <<< $server_proc)
        echo "pid: ${pid}"
        pids_to_kill+=" $pid"
    done
    if [[ "$force" == false ]] && [[ $alien_count > 0 ]]; then
        [[ $alien_count == 1 ]] && conjugate="is" || conjugate="are"
        log_error_to_startup_file_and_exit "$alien_count of the running GDA Servers on ($HOSTNAME) $conjugate based on a different $2 than the one you are trying to restart/stop. Please examine and deal with this as appropriate or retry with --force"
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

#############################
# Paths and arguments setup
#############################

# Require the BEAMLINE environment variable to have been set
if [[ -z $BEAMLINE ]] ; then
    log_error_to_startup_file_and_exit "BEAMLINE environment variable not set - GDA server cannot start"
fi

# Ensure directory containing workspaces exists, and is writable by everyone
readonly USER_WORKSPACE_PARENT=~/scratch/gda_server_user_workspaces
[ -d $USER_WORKSPACE_PARENT ] || mkdir -m 777 $USER_WORKSPACE_PARENT

# Ensure directory containing configs exists, and is writable by everyone
readonly ECLIPSE_RUNTIME_CONFIG_PARENT=~/scratch/gda_server_eclipse_configurations
[ -d $ECLIPSE_RUNTIME_CONFIG_PARENT ] || mkdir -m 777 $ECLIPSE_RUNTIME_CONFIG_PARENT

# Initialise the beamline specific config
readonly BEAMLINE_CONFIG="$GDA_WORKSPACE_PARENT/${GDA_INSTANCE_CONFIG_rel:-config}"

# Set server application specifics - find absolute path
readonly SERVER_INSTALL_PATH=$(readlink -f "${GDA_WORKSPACE_PARENT}/server")
readonly SERVER_INSTALL_DIRNAME=$(basename "$SERVER_INSTALL_PATH")

# Set user workspace and eclipse runtime configuration location (user and server build specific)
readonly USER_WORKSPACE=$USER_WORKSPACE_PARENT/$(whoami)
readonly ECLIPSE_RUNTIME_CONFIG_DIRNAME=$(whoami)-$SERVER_INSTALL_DIRNAME
readonly ECLIPSE_RUNTIME_CONFIG=$ECLIPSE_RUNTIME_CONFIG_PARENT/$ECLIPSE_RUNTIME_CONFIG_DIRNAME

# Add java location to the path
module load java/gda93

# Resolve the input arguments - need the ':-' as beamline gda script uses set -o nounset
if [[ -n "${SSH_ORIGINAL_COMMAND:-}" ]]; then
    ARGS_IN="$SSH_ORIGINAL_COMMAND"
else
    ARGS_IN="$@"
fi

VALID_OPTIONS="--debug|--debug-wait|"
for word in $ARGS_IN; do
    if [[ "sword" == "--"* ]] && [[ "$VALID_OPTIONS" != *"|$word|"* ]]; then
        log_error_to_startup_file_and_exit "'$word' is not a valid option"
    fi
done

# Initialise the GDA and java startup arguments, defaulting GDA_MODE to dummy if env var not set
vm_args="-Dgda.mode=${GDA_MODE:-dummy} -Djava.awt.headless=true"

if [[ "$ARGS_IN" == *"--debug"* ]]; then
    [[ "$ARGS_IN" == *"--debug-wait"* ]] && wait="y" || wait="n"
    vm_args="$vm_args -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=${wait},address=8000"
fi

if [[ "$ARGS_IN" == *"--jrebel"* ]]; then
    [[ -d "/localhome/gda2" ]] && rebel_parent="/localhome/gda2" || rebel_parent="/scratch"
    if [[ $rebel_parent == "/scratch" ]] && [[ ! -d "/scratch" ]]; then
        log_error_to_startup_file_and_exit "The parent folder of the rebel.base folder $rebel_parent/.jrebel does not exist, GDA with JRebel cannot start"
    fi
    vm_args="$vm_args -agentpath:$rebel_parent/jrebel/lib/libjrebel64.so -Drebel.remoting_plugin=true -Drebel.remoting_port=8666 -Drebel.base=$rebel_parent/.jrebel"
fi

[[ "$ARGS_IN" == *"--force"* ]] && force=true || force=false

#############################
# Start/Restart/Stop handling
#############################

readonly MY_NAME=$(basename "$(readlink -e "$0")")         # The name of this script

readonly ALL_RUNNING_GDA_OSGI_SERVERS='\-name [G]da-server'
readonly ALL_RUNNING_SUBORDINATE_SERVERS='org.jacorb.naming.[N]ameServer|gda.factory.corba.util.[C]hannelServer|gda.util.[L]ogServer'

readonly START_ONLY_PATTERN="\bstart\b"
readonly RESTART_OR_STOP_PATTERN="\brestart\b|\bstop\b"

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
    # check if same user is restarting stopping application as owner of running one whose proc details are stored in $my_servers_arr
    check_running_servers_ownership "$ALL_RUNNING_GDA_OSGI_SERVERS" "GDA"
    # now check the main application was started from the same server build using $my_servers_arr
    check_matching_server_target ${TARGET_SERVER_MATCHER} "target"
    OSGI_SERVER_PIDS_TO_KILL=$pids_to_kill
    # repeat above tests for Name/Channel/Log servers via my_servers_arr and checking if deployment folder matches
    check_running_servers_ownership "$ALL_RUNNING_SUBORDINATE_SERVERS" "Name, Channel and/or Log"
    check_matching_server_target ${GDA_WORKSPACE_PARENT} "deployment"
    for pid in $OSGI_SERVER_PIDS_TO_KILL; do
        kill_with_SIGKILL_if_necessary "$pid" "7" "Shutting down"
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

readonly COMMAND="${GDA_WORKSPACE_PARENT}/server/gda-server -clean -data $USER_WORKSPACE -configuration $ECLIPSE_RUNTIME_CONFIG -c $BEAMLINE_CONFIG -p $GDA_PROFILES -vmArgs $vm_args"

# and execute it retaining stdin
echo "Starting the GDA Server at $SERVER_INSTALL_PATH/gda-server"
echo $COMMAND
$COMMAND &
