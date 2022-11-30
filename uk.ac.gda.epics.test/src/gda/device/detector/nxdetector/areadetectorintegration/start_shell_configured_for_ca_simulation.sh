if ["$1" == ""]; then
   echo "ERROR: Hostname argument expected"
   exit 1
fi

ssh -X $1 -t 'export EPICS_CA_SERVER_PORT=6064; export EPICS_CA_REPEATER_PORT=6065; $SHELL'

