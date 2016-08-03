#!/bin/bash
# This script is invoked when the gda_servers_core script has been run locally or has been traversed a
# second time as a result of starting the gda on the control machine from another machine via the ssh tunnel.

# It starts the gda-server target that has been selected by the gda_server_target_select.sh script earlier in
# the startup loop. For a full overview of GDA9 startup see
# http://confluence.diamond.ac.uk/display/CT/Deployment%2C+Target+Selection+and+Startup.
#
# N.B. in the near term (from 10/08/16) the target selection mechanism will be retired in favour of separate
# deployments for release/snapshot of development server targets. 

. ${GDA_WORKSPACE_PARENT}/${GDA_CORE_CONFIG_rel}/bin/bashlog_function

MY_PATH=$(readlink -e ${BASH_SOURCE[0]})
MY_WORKSPACE_PARENT=${MY_PATH%%/workspace_git*}

LATCH_SCRIPT="$( dirname $MY_PATH )/latch.sh"
RELEASE_VERSION_SCRIPT="$( dirname $MY_PATH )/read_release_version.sh"

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

VALID_OPTIONS="|--devel|--debug|--release|--snapshot|--latch|"
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
BEAMLINE_CONFIG="$GDA_WORKSPACE_PARENT/$GDA_INSTANCE_CONFIG_rel"

# Determine the required server application install location an add it to the path:
#
# Default: used the current latched version
#
if [[ "$ARGS_IN" != *"devel"* ]] && [[ "$ARGS_IN" != *"release"* ]] && [[ "$ARGS_IN" != *"snapshot"* ]]; then
    $LATCH_SCRIPT                                              # record the current latched state in the log
    export PATH="$MY_WORKSPACE_PARENT/launch:${PATH}"

# One-time run options:
#
# Devel: add the beamline workspace default server location to the path
#
elif [[ "$ARGS_IN" == *"devel"* ]]; then

    # Disallow multiple target options along the way
    #
    if [[ "$ARGS_IN" == *"release"* ]] || [[ "$ARGS_IN" == *"snapshot"* ]]; then
        err_msg_exit "Cannot specify two or more target options together; they are mutually exclusive"
    fi
    export PATH="$MY_WORKSPACE_PARENT/server:${PATH}"          # i.e. "module load gda-server devel"
    echo -e "\n\tSetting up GDA 9 SERVER development target"

# Disallow multiple target options along the way
#
elif [[ "$ARGS_IN" == *"snapshot"* ]] && [[ "$ARGS_IN" == *"release"* ]]; then
    err_msg_exit "Cannot specify two or more target options together; they are mutually exclusive"

# Release or Snapshot: module load the appropriate path
#
else
    if [[ "$ARGS_IN" == *"snapshot"* ]]; then
        module load gda-server/snapshot
    else
        source $RELEASE_VERSION_SCRIPT                          # set the RELEASE variable
        module load gda-server/$RELEASE
    fi
fi

# Set server application specifics
SERVER_INSTALL_PATH=$(readlink -f $(dirname $(which gda-server)))
SERVER_INSTALL_DIRNAME=$(basename "$SERVER_INSTALL_PATH")

# Initialise the java startup arguments, defaulting to dummy if env var not set
vm_args="-Dgda.mode=${GDA_MODE:-dummy} -Djava.awt.headless=true"

if [[ "$ARGS_IN" == *"debug"* ]]; then
    vm_args="$vm_args -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"
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

MY_NAME=`basename "$MY_PATH"`

# Write any error before attempting to launch the application into the startup file used to determine
# whether startup has completed. This allows the error message to be retrieved displayed on the workstation
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

# If starting a java development target, just use path to servers directory to match on (to allow
# for successive exported builds) otherwise use full path to the gda-server executable
#
function set_target_server_matcher {
    local splitter="gda-server"
    local DEV_MODE_PATTERN="\/servers\/server_"
    if [[ "$SERVER_INSTALL_PATH" =~ $DEV_MODE_PATTERN ]]; then
        splitter="server_"
    fi
    local TOKENS=(${SERVER_INSTALL_PATH//$splitter/ })
    TARGET_SERVER_MATCHER=${TOKENS[0]}
}

# Check that all running servers that match the supplied grep pattern belong the current user exiting if not
#
function check_running_servers_ownership {
    readarray -t servers_arr < <(ps -ef | grep -E "$1")
    readarray -t my_servers_arr < <(ps -ef | grep -E "$1" |grep `whoami`)
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
        if [[ -z $(grep "${TARGET_SERVER_MATCHER}" <<< "${server_proc}") ]]; then
            fault_source="target"
            ((alien_count++))
        elif [[ -z $(grep "${GDA_WORKSPACE_PARENT}" <<< "${server_proc}") ]]; then
            fault_source="deployment"
            ((alien_count++))
        else
            pid=$(awk '{ print $2 }' <<< $server_proc)
            pids_to_kill+=" $pid"
        fi
    done
    if [[ $alien_count > 0 ]]; then
        [[ $alien_count == 1 ]] && conjugate="is" || conjugate="are"
        log_error_to_startup_file_and_exit "$alien_count of the running GDA Servers on ($HOSTNAME) $conjugate based on a different $fault_source than the one you are trying to restart/stop. Please examine and deal with this as appropriate before proceeding."
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
    check_matching_server_target
    OSGI_PIDS_TO_KILL=$pids_to_kill
    check_running_servers_ownership "$ALL_RUNNING_SUBORDINATE_SERVERS" "Name, Channel and/or Log"
    check_matching_server_target
    bashlog info "$MY_NAME" "killing$OSGI_PIDS_TO_KILL"
    kill "$OSGI_PIDS_TO_KILL"
    sleep 10

    # the above should also have got rid of the Log, Channel and Name Server, but just in case:
    for pid in $pids_to_kill
    do
        if [[ -n "$(ps -p $pid)" ]]; then
            bashlog info "$MY_NAME" "killing $pid"
            kill "$pid"
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
COMMAND="gda-server -data $USER_WORKSPACE -configuration $ECLIPSE_RUNTIME_CONFIG -c $BEAMLINE_CONFIG -p $GDA_PROFILES -vmArgs $vm_args"

# and execute it retaining stdin
echo "Starting the GDA Server at $SERVER_INSTALL_PATH/gda-server"
echo $COMMAND
$COMMAND &
