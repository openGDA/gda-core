import sys    
import os
import time
from gda.factory import Finder
from gda.configuration.properties import LocalProperties
from gdascripts.messages import handle_messages
from gda.jython import InterfaceProvider
from gda.device.scannable import ScannableBase

        
def isLive():
    mode = LocalProperties.get("gda.mode")
    return mode =="live"

try:
    from gda.device import Scannable
    from gda.jython.commands.GeneralCommands import ls_names, vararg_alias
    
    def ls_scannables():
        ls_names(Scannable)
    
    alias("ls_scannables")


    from epics_scripts.pv_scannable_utils import createPVScannable, caput, caget
    alias("createPVScannable")
    alias("caput")
    alias("caget")
    
    from gda.scan.RepeatScan import create_repscan, repscan
    vararg_alias("repscan")

    #setup tools to create metadata in Nexus files
    from gdascripts.metadata.metadata_commands import setTitle, meta_add, meta_ll, meta_ls, meta_rm
    alias("setTitle")
    alias("meta_add")
    alias("meta_ll")
    alias("meta_ls")
    alias("meta_rm")
    from gda.data.scan.datawriter import NexusDataWriter
    LocalProperties.set(NexusDataWriter.GDA_NEXUS_METADATAPROVIDER_NAME,"metashop")
    
    #create time scannables
    from gdascripts.pd.time_pds import waittimeClass2, showtimeClass, showincrementaltimeClass, actualTimeClass
    waittime=waittimeClass2('waittime')
    showtime=showtimeClass('showtime')
    inctime=showincrementaltimeClass('inctime')
    actualTime=actualTimeClass("actualTime")

    #run user editable startup script 
    if isLive():
        run("localStationUser.py")

except:
    exceptionType, exception, traceback = sys.exc_info()
    handle_messages.log(None, "Error in localStation", exceptionType, exception, traceback, False)

