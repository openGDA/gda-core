#!/bin/bash
# This script is invoked when the gda_servers_core script has been run locally or has been traversed a
# second time as a result of starting the gda on the control machine from another machine via the ssh tunnel.

# It starts the gda-server target that has been selected by the gda_server_target_select.sh script earlier in
# the startup loop. It can also be used interactively to start a particular server target on demand; enter
# gda_server_target_launch.sh --help (-h) for details. For a full overview of GDA9 startup see
# http://confluence.diamond.ac.uk/display/CT/Deployment%2C+Target+Selection+and+Startup.

MY_PATH=$(readlink -e ${BASH_SOURCE[0]})
MY_WORKSPACE_PARENT=${MY_PATH%%/workspace_git*}

LATCH_SCRIPT="$( dirname $MY_PATH )/latch.sh"
RELEASE_VERSION_SCRIPT="$( dirname $MY_PATH )/read_release_version.sh"

# light red text
function lr {
	echo -e "\033[1;31m$1\033[0m"
}

# Exit, displaying supplied error message in red text and help message
function err_msg_exit {
	echo -e "\n$(lr "*** ERROR ***: $1")"
	help
	exit 1
}

function help {
	cat 1>&2 <<EOF

Usage: $(basename "${BASH_SOURCE[0]}") [OPERATION] [OPTIONS]

Start/Stop/Restart the gda server in the mode specified by the options as indicated by OPERATION.
If no target option is specified, the currently latched one will be used, this is referenced by
the launch symlink and initially defaults to release:
At the moment this is:
$(source "$LATCH_SCRIPT")

Required Environment variables:
BEAMLINE     The beamline identifier e.g. i18

OPERATIONS:

start		Launch a new gda-server process without checking if one already exists
stop		Kill the currently running gda-server and its associated processes
restart		stop followed by start (the default)

OPTIONS:

Target:
--release   Run the most recent released build of the server
--snapshot  Run the latest Jenkins built snapshot associated with the beamline
--devel     Run the most recent export of the server in the beamline workspace

Other:
--debug     Start the server in debug mode waiting for a connection of port 8000
--help      Display this message

N.B. the release, snapshot and devel options are mutually exclusive and will be rejected if more than one is specified

EXAMPLES:

    gda_server_target_launch.sh

        This will restart the latched version of the GDA server (defaults to released).

    gda_server_target_launch.sh start --devel --debug

        This will initialise the server build last exported from the beamline workspace in debug mode an wait for connection on port 8000 before proceeding

EOF
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

if [[ $ARGS_IN == *"help"* ]]; then
	help
	exit 0
fi

VALID_OPTIONS="|--devel|--debug|--release|--snapshot|--help|--latch|"
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
	module load gda-server/$BEAMLINE
	$LATCH_SCRIPT                                              # record the current latched state in the log

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

# if restarting or stopping
if [[ "$ARGS_IN" != *" start"* ]]; then
	RUNNING_SERVER_PID=$(ps -ef | grep [G]da-server | awk '{ print $2 }')
	if [[ -n "$RUNNING_SERVER_PID" ]]; then
		$(kill "$RUNNING_SERVER_PID")
		sleep 10
	fi
	# this should also have got rid of the Log, Channel and Name Server, but just in case:
	SERVER_PIDS=$(ps -ef | grep [D]gda.deploytype | awk '{ print $2 }')
	if [[ -n "$SERVER_PIDS" ]]; then
		pid_array=($SERVER_PIDS)
		for pid in "${pid_array[@]}"
		do
			$(kill "$pid")
		done
	fi

	if [[ "$ARGS_IN" == *"stop"* ]]; then
		exit 0
	fi

fi

# now we must be restarting or starting only
# Assemble the command string
COMMAND="gda-server -data $USER_WORKSPACE -configuration $ECLIPSE_RUNTIME_CONFIG -c $BEAMLINE_CONFIG -vmArgs $vm_args"

# and execute it retaining stdin
echo "Starting the GDA Server at $SERVER_INSTALL_PATH/gda-server"
echo $COMMAND
$COMMAND &

