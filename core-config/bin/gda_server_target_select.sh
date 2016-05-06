#!/bin/bash
# Allows the server startup target to be selected and/or latched between release, snapshot or devel;
# release and snapshot are in standard network locations whereas devel corresponds to the location
# pointed to by the server link alongside workspace git. This is automatically generated when you
# export a server product build from your workspace.
#
# Also supports starting the selected server target in debug mode - will wait for an IDE to be attached
# on port 8000. Entering gda_server_target_select.sh --help (-h) gives details of the allowed options.
#
# For full details of GDA9 startup
# see http://confluence.diamond.ac.uk/display/CT/Deployment%2C+Target+Selection+and+Startup.

# Get the parent dir of the workspace this script is in
MY_PATH=$(readlink -e ${BASH_SOURCE[0]})

LATCH_SCRIPT="$( dirname $MY_PATH )/latch.sh"

# Light green text
#
function lg {
	echo -e "\033[1;32m$1\033[0m"
}

# light red text
#
function lr {
	echo -e "\033[1;31m$1\033[0m"
}

# black text yellow background
#
function yb {
	echo -e "\033[0;30;103m$1\033[0m"
}

# Help message
#
function gda_servers_help {
	cat 1>&2 <<EOF

Usage: $0 [OPERATION] [OPTIONS]

Start/Stop/Restart the gda-server target specified by the options, if no target option
is specified, the currently latched one will be used, at the moment this is:
$(source "$LATCH_SCRIPT")

The OPERATION specifies the required action, the default (if no OPERATION is specified
is restart.

Required Environment variables:
BEAMLINE     The beamline identifier e.g. i18

OPERATIONS:

--start		Launch a new gda-server process without checking if on already exists
--stop		Kill the currently running gda-server and its associated processes
--restart	stop followed by start (the default)

OPTIONS:

Server Target:
-r  --$(lg r)elease   Run the most recent released build of the server
-s  --$(lg s)napshot  Run the latest Jenkins built snapshot associated with the beamline
-d  --$(lg d)evel     Run the most recent export of the server in the beamline workspace

Target persistence:
-l  --$(lg l)atch     Set the specified startup target as the default one from now on (must be used with a target option)

Other:
-b  --de$(lg b)ug     Start the server in debug mode waiting for a connection of port 8000 (this setting cannot be latched)
-h  --$(lg h)elp      Display this message

N.B. 
1. the -s, -d and -r options are mutually exclusive and will be rejected if more than one is specified.
2. the -l option will be rejected if not specified with either -s, -d or -r

EXAMPLES :

    $0

        This will (re)start the currently latched GDA server target (this is the command normally run by the GDA Server launcher).

    $0 --start -d --debug

        This will initialise the server build last exported from the beamline workspace in debug mode an wait for connection on port 8000 before proceeding.
        
    $0 --restart --snapshot -l
    
    	This will restart the current snapshot build and set this as the build to start in future when the no target option is specified.

EOF
}

# Exit, displaying supplied error message in red text and help message
#
function err_msg_exit {
	echo -e "\n$(lr "*** ERROR ***: $1")"
	gda_servers_help
	exit 1
}

# Check the option/operation being specified is a valid one; if it is a target option remember it
#
function check_ops {
	VALID_OPTIONS="|start|stop|restart|devel|debug|release|snapshot|latch|help|"
	if [[ "$VALID_OPTIONS" != *"|$1|"* ]]; then
		err_msg_exit "'$1' is not a valid option"
	fi
	if [[ "|devel|release|snapshot|" == *"|$1|"* ]]; then
		TARGET=$1
	fi
}

## Main routine ##
#
i=0
args_arr=()
operation="--restart"			# default
for word in $@; do

	# parse '--' options
	if [[ "${word:0:2}" == "--" ]]; then
		check_ops "${word:2}"
		if [[ "${word:2}" == "help" ]]; then
			gda_servers_help
			exit 0
		fi
		if [[ "|--restart|--start|--stop|" == *"|$word|"* ]]; then
			operation="$1"
			continue
		fi
		args_arr[$i]="${word}"
		((++i))
	# parse '-' options
	elif [[ "${word:0:1}" == "-" ]] && [[ "${#word}" == 2 ]]; then

		case "${word:1}" in
			s)
				arg="snapshot"
				;;
			d)
				arg="devel"
				;;
			r)
				arg="release"
				;;
			b)
				arg="debug"
				;;
			l)
				arg="latch"
				;;
			h)
				gda_servers_help
				exit 0
				;;
			*)
				arg="${word:1}"
				;;
		esac
		check_ops "$arg"
		args_arr[$i]="--$arg"
		((++i))
	else
		err_msg_exit "$word is not a valid option"
	fi
done
ARGS=${args_arr[@]}

# Disallow multiple targets
#
if [[ "$ARGS" == *"devel"* ]]; then
	if [[ "$ARGS" == *"release"* ]] || [[ "$ARGS" == *"snapshot"* ]]; then
		err_msg_exit "Cannot specify two or more target options together; they are mutually exclusive"
	fi
elif [[ "$ARGS" == *"snapshot"* ]] && [[ "$ARGS" == *"release"* ]]; then
	err_msg_exit "Cannot specify two or more target options together; they are mutually exclusive"
fi

# Disallow latching when no target specified, otherwise latch as requested
#
if [[ "$ARGS" == *"latch"* ]]; then
	if [[ -z $TARGET ]]; then
		err_msg_exit "Must specify a startup target option to latch"
	else
		$LATCH_SCRIPT "$TARGET"
	fi
fi

if [[ "$operation" != "--stop" ]]; then
	if [[ -n $TARGET ]]; then
		echo -e "\n\tStarting the $(yb $TARGET) GDA Server target"
	else 
		$LATCH_SCRIPT
	fi
fi

