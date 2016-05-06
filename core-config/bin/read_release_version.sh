#!/bin/bash
# Determines the release version that this workspace was materialised from. At the moment this
# is done based on the file written by the pewma script, until Matthew Webber returns and a
# dedicated file that is written for both pewma and eclipse materialisations is available.

# Get the parent dir of the workspace this script is in
MY_PATH=$(readlink -e ${BASH_SOURCE[0]})
MY_WORKSPACE_PARENT=${MY_PATH%%/workspace_git*}

# The location of the file that references the cquery used to materialise this workspace
PEWMA_SCRIPT="$MY_WORKSPACE_PARENT/workspace/pewma-script.txt"

function warn {
	TEXT="WARNING: $1, cannot determine release version, using default:"
	echo -e "\033[38;5;214m$TEXT\033[0m"
	echo
}
RELEASE=9.0
## Main routine ##
#
if [[ ! -f $PEWMA_SCRIPT ]]; then
	warn "pewma-script.txt not present in workspace"
else
	# Read release version from pewma-script file
	#
	using_default=true
	while read -r line
	do
		if [[ "$line" == *gda-v?.*.cquery* ]] ; then
			line=${line##*gda-v}
			RELEASE=${line%%.cquery}
			using_default=false
			break
		fi
	done < $PEWMA_SCRIPT

	if [[ $using_default == true ]]; then
		warn "cquery details could not be found in pewma-script.txt"
	fi
fi
echo -e "\tRelease version is $RELEASE"
