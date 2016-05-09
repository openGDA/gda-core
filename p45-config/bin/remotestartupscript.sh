#!/bin/bash
# This script is only invoked when user gda2 ssh's to the control machine. It is run by an entry in gda's ~/.ssh/authorized_keys

here_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# This script is run as a single command by ssh, so we need to set up our environment
# See http://stackoverflow.com/questions/216202/why-does-an-ssh-remote-command-get-fewer-environment-variables-then-when-run-man
. /usr/share/Modules/init/bash

# There is no user or screen to prompt or display pop-ups
export GDA_NO_PROMPT=true

# Set an environment variable to indicate we came through the remote startup script, so that we can error if we attempt to do this recursively
export GDA_IN_REMOTE_STARTUP=true

if [[ -n "${SSH_ORIGINAL_COMMAND}" ]]; then
	if [[ "${SSH_ORIGINAL_COMMAND}" == *"restart"* ]]; then
		SSH_ORIGINAL_COMMAND="${SSH_ORIGINAL_COMMAND/restart/--restart}"
	elif [[ "${SSH_ORIGINAL_COMMAND}" == *"start"* ]]; then
		SSH_ORIGINAL_COMMAND="${SSH_ORIGINAL_COMMAND/start/--start}"
	elif [[ "${SSH_ORIGINAL_COMMAND}" == *"stop"* ]]; then
		SSH_ORIGINAL_COMMAND="${SSH_ORIGINAL_COMMAND/stop/--stop}"
	fi
	${here_dir}/gda  ${SSH_ORIGINAL_COMMAND} --mode=live servers
else
	${here_dir}/gda --restart --mode=live servers
fi