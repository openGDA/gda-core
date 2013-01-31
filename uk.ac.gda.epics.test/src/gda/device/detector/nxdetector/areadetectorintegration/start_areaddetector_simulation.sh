if ["$1" == ""]; then
   echo "ERROR: Hostname argument expected"
   exit 1
fi
ssh -t $1 'echo hello; export EPICS_CA_SERVER_PORT=6064; export EPICS_CA_REPEATER_PORT=6065; echo $EPICS_CA_SERVER_PORT; cd /dls_sw/work/R3.14.11/support/BL07I-BUILDER/iocs/BLRWI-DI-IOC-01/; bin/linux-x86/stBLRWI-DI-IOC-01.sh'

