from gda.epics import CAClient
from time import sleep

def putvalue(basePv, ext, value) :
    fullPv = basePv + ext
    print "caput %s = %s"%(fullPv, value)
    caclient = CAClient()
    caclient.caput(fullPv, value, 10.0)
    
def caget(pvName) :
    print "caget %s"%(pvName)
    caclient = CAClient()
    value = caclient.caget(pvName)
    print "value = %s"%(value)
    return value

def set_hdf_input_port(basePvName, arrayPort):
    print "Setting hdf input"
    putvalue(basePvName, ":HDF5:NDArrayPort", arrayPort)

def set_sca_input_port(basePvName, numChannels, arrayPort):
    print "Setting SCA input array port"
    for num in range(1, numChannels+1) :        
        putvalue(basePvName, ":C"+str(num)+"_SCAS:NDArrayPort", arrayPort)

def set_roi_input_port(basePvName, numChannels, arrayPort):
    print "Setting ROI input array port"
    for num in range(1, numChannels+1) :        
        putvalue(basePvName, ":ROI"+str(num)+":NDArrayPort", arrayPort)


def set_xspress_use_dtc(basePvName, useDtc):
    """
    Set the hdf, scaler and ROI plugins to point at either the detector
    or the deadtime correction factor (DTC) plugin.
    
        basePvName - base pv name of xspress detector
        useDtc   - whether to setup detector to use DTC plugin in the plugin chain.
    """
    
    ## Get the port names of the detector and dtc plugins
    detPort = caget(basePvName+":PortName_RBV")
    detPortDtc = caget(basePvName+":DTC:PortName_RBV")
    portToUse = detPort
    if useDtc :
        print("Using DTC values in Xspress detector")
        portToUse = detPortDtc
    else :
        print("Not using DTC values in Xspress")
        portToUse = detPort

    # Number of detector elements (channels)
    numElements = int(caget(basePvName+":MaxSizeY_RBV"))
    print("Number of channels : %s"%(numElements))

    ## Enable/disable application of DTC to MCA data
    putvalue(basePvName, ":DTC:ApplyDTC", 1 if useDtc else 0)
    set_hdf_input_port(basePvName, portToUse)
    set_sca_input_port(basePvName, numElements, portToUse)
    set_roi_input_port(basePvName, numElements, portToUse)

def setup_xspress_detector(basePvName):
    """ Set up an xspress3, xspress4 detector to get it ready for doing scans where each point in the scan is
    externally triggered (e.g. by a Tfg) :
     1) The trigger mode is set to 'TTL Veto Only' 
     2) If needed, the data dimensions of the Hdf writer (and other plugins) are set by collecting a
     single software triggered frame of data.
    """
    print "Setting up XSpress detector : ", basePvName

    print "  Trigger mode = 'TTL Veto Only'"
    set_trigger_mode(basePvName, 3) # TTL veto trigger mode

    numDataDimensions = caget(basePvName + ":HDF5:NDimensions_RBV")
    if int(numDataDimensions) == 0 :
        print "  Data dimensions are not yet known - collecting one frame of internal triggered data..."
        collect_software_triggered_frame(basePvName, 1.0)

def set_trigger_mode(basePvName, triggerMode) :
    putvalue(basePvName, ":TriggerMode", triggerMode) # TTL veto trigger mode

def collect_software_triggered_frame(basePvName, collectionTime) :
    """Collect one frame of software (internal) triggered data on the detector.
    The original trigger mode is restored after the frame has been collected.
    """
    print "Collecting 1 internal triggered frame of data from detector %s"%(basePvName)
    origTrigger = caget(basePvName+":TriggerMode")
    putvalue(basePvName, ":NumImages", 1) # number of framews
    putvalue(basePvName, ":ImageMode", 0) # single image mode  
    set_trigger_mode(basePvName, 0) # internal trigger mode
    putvalue(basePvName, ":AcquireTime", collectionTime) # collection time
    print "Starting detector ..."
    putvalue(basePvName, ":Acquire", 1) # start the detector

    #restore the trigger mode
    print "Finished - restoring trigger mode"
    set_trigger_mode(basePvName, origTrigger)

def setup_swmr(basePvName, switchOn, numFramesFlush, ndAttributeFlush):
    """ Set SWMR mode hdf options : switch SWMR mode on, num frame flush, attribute chunk
    """
    print "Setting SWMR hdf options for detector %s"%(basePvName)

    print "  HDF5 SWMR mode = %s"%(switchOn)
    putvalue(basePvName, ":HDF5:SWMRMode", switchOn) # Set SWMR mode on.

    print "  HDF5 SWMR : Flush on nth frame = %d, NDAttribute flush = %d"%(numFramesFlush, ndAttributeFlush)
    putvalue(basePvName, ":HDF5:NumFramesFlush", numFramesFlush)
    putvalue(basePvName, ":HDF5:NDAttributeChunk", ndAttributeFlush)

def setupResGrades(basePvName, collect) :
    val = 0
    if collect == True :
        val = 1
        
    if int(caget(basePvName+":COLLECTRESGRADES_RBV")) != val:
        print "Setting Xspress4 IOC "+basePvName+" to collect Resgrades"
        CAClient.put(basePvName+":COLLECTRESGRADES", val)
        Thread.sleep(500)
    if int(caget(basePvName+":RECONNECT_REQUIRED")) == 1 :
        print "Reconnecting IOC connection to Xspress detector"
        CAClient.put(basePvName+":CONNECT", 1)
        Thread.sleep(2000)
        print "Finished reconnecting"

def set_hdf5_filetemplate(basePvName):
    print "Setting hdf filename template"
    hdf5Values = { "FileTemplate" : "%s%s%d.hdf", "FileWriteMode" : 2}
    for key in hdf5Values :
        pv = basePvName + ":HDF5:" + key
        value = hdf5Values[key]
        print "  Setting " + pv + " to " + str(value)
        if isinstance(value, str) :
            CAClient.putStringAsWaveform(pv, value) 
        else :
            CAClient.put(pv, value)


from org.slf4j import LoggerFactory
import traceback

def run_in_try_catch(function):
    logger = LoggerFactory.getLogger("run_in_try_catch")

    try :
        print "Running ",function.__name__," function"
        function()
    except (Exception, java.lang.Throwable) as ex:
        stacktrace=traceback.format_exc()
        print "Problem running ",function.__name__," - see log for more details"
        print "Stack trace : ", stacktrace
        logger.warn("Problem running jython function {} :\n{}", function.__name__, stacktrace)

