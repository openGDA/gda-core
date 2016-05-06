#!/bin/bash
# Reads or rewrites the beamline specific server 'launch' link as requested. This allows Data
# Acquisition staff to latch the GDA9 server application that will be started by the launcher.
#
# For a full overview of GDA9 startup see
# http://confluence.diamond.ac.uk/display/CT/Deployment%2C+Target+Selection+and+Startup

# Get the parent dir of the workspace this script is in
MY_PATH=$(readlink -e ${BASH_SOURCE[0]})
MY_WORKSPACE_PARENT=${MY_PATH%%/workspace_git*}

RELEASE_VERSION_SCRIPT="$( dirname $MY_PATH )/read_release_version.sh"
SNAPSHOT_PARENT="/dls/science/groups/daq/gdaserver/master/builds"
RELEASE_PARENT="/dls_sw/apps/gdaserver"

function red {
	TEXT="\033[1;31m$1\033[0m"
}

function amber {
	TEXT="\033[38;5;214m$1\033[0m"
}

function green {
	TEXT="\033[1;32m$1\033[0m"
}

function blue {
	TEXT="\033[1;34m$1\033[0m"
}

if [[ $# > 1 ]]; then
	red "ERROR: latch script only accepts a maximum of one parameter"
	echo -e $TEXT
	exit 1
fi

## Main routine ##
#
# With no parameters, read the link and display the target that has been latched
#
if [[ -z $1 ]]; then
	if [[ ! -e "$MY_WORKSPACE_PARENT/launch" ]]; then
		red "ERROR: 'launch' link not present, cannot read latch."
		echo -e $TEXT
		exit 1
	fi

	DEST=$(readlink  "$MY_WORKSPACE_PARENT/launch")
	source $RELEASE_VERSION_SCRIPT                                 # set the RELEASE variable
	case $DEST in
		"$MY_WORKSPACE_PARENT/server")
			red "devel"
			;;
		"$SNAPSHOT_PARENT/snapshot-linux64")
			amber "snapshot"
			;;
		"$RELEASE_PARENT/$RELEASE/builds/release-linux64")
			green "release"
			;;
		*)
			blue "$DEST"
			;;
	esac
	echo -e "\tThe $TEXT GDA Server target is selected"
	exit 0
fi

# If a new target has been specified, remove the old link and replace it with one to the requested target
#
rm -f "$MY_WORKSPACE_PARENT/launch"
case $1 in
	devel)
		LOCATION="$MY_WORKSPACE_PARENT/server"
		;;
	snapshot)
		LOCATION="$SNAPSHOT_PARENT/snapshot-linux64"
		;;
	release)
		source "$RELEASE_VERSION_SCRIPT"                           # set the RELEASE variable
		LOCATION="$RELEASE_PARENT/$RELEASE/builds/release-linux64"
	;;
	*)
		if [[ ! -f "$1/gda-server" ]]; then
			red "ERROR: $1/gda-server is not a valid file"
			echo -e $TEXT
			exit 1
		fi
		LOCATION="$1"
	;;
esac

ln -s "$LOCATION" "$MY_WORKSPACE_PARENT/launch"
echo -e "\tThe $1 GDA Server target has been set"

