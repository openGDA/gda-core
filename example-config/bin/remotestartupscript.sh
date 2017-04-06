#!/bin/bash
# This script is only invoked when user gda2 ssh's to the control machine. It is run by an entry in gda's ~/.ssh/authorized_keys
# Because of this, no environment variables (other than SSH_ORIGINAL_COMMAND potentially) have been set when it executes.
#
# If you wish to use the dls-config common /remote/startupscript.sh you must source it, performing any custom operations beforehand.
# If you don't have any custom operations then you can just invoke the common script directly from your authorized_keys file.
#
# If not using the common script, you will need to set GDA_NO_PROMPT and GDA_IN_REMOTE_STARTUP to true and initialise your Bash
# environment before invoking the main gda script to get correct startup behaviour. Also please add "< /dev/null > /dev/null 2>&1"
# to the end of the line that calls the gda script to prevent ssh from hanging incorrectly due to an unclosed stream.



# If you want any special behaviour add it above this line
here_absolute_path="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd -P )"
source ${here_absolute_path}/../../../gda-diamond.git/dls-config/live/gda-servers-startup-script.sh  # Derive dls-config relative path as appropriate
