from gda.epics import CAClient
from time import sleep

def caput(basePv, ext, value) :
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
    caput(basePvName, ":HDF5:NDArrayPort", arrayPort)

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
    caput(basePvName, ":TriggerMode", triggerMode) # TTL veto trigger mode

def collect_software_triggered_frame(basePvName, collectionTime) :
    """Collect one frame of software (internal) triggered data on the detector.
    The original trigger mode is restored after the frame has been collected.
    """
    baseCamPvName = basePvName+":CAM"
    print "Collecting 1 internal triggered frame of data from detector %s"%(basePvName)
    origTrigger = caget(baseCamPvName+":TriggerMode")
    caput(baseCamPvName, ":NumImages", 1) # number of framews
    caput(baseCamPvName, ":ImageMode", 0) # single image mode  
    set_trigger_mode(basePvName, 0) # internal trigger mode
    caput(baseCamPvName, ":AcquireTime", collectionTime) # collection time
    print "Starting detector ..."
    caput(baseCamPvName, ":Acquire", 1) # start the detector

    #restore the trigger mode
    print "Finished - restoring trigger mode"
    set_trigger_mode(basePvName, origTrigger)

def setup_swmr(basePvName, switchOn, numFramesFlush, ndAttributeFlush):
    """ Set SWMR mode hdf options : switch SWMR mode on, num frame flush, attribute chunk
    """
    print "Setting SWMR hdf options for detector %s"%(basePvName)

    print "  HDF5 SWMR mode = %s"%(switchOn)
    caput(basePvName, ":HDF5:SWMRMode", switchOn) # Set SWMR mode on.

    print "  HDF5 SWMR : Flush on nth frame = %d, NDAttribute flush = %d"%(numFramesFlush, ndAttributeFlush)
    caput(basePvName, ":HDF5:NumFramesFlush", numFramesFlush)
    caput(basePvName, ":HDF5:NDAttributeChunk", ndAttributeFlush)


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

