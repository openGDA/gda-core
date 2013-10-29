from gda.device.scannable import ScannableBase, ScannableUtils
from gda.scan import ScanPositionProvider, ScanBase
from gda.configuration.properties import LocalProperties
import gda.jython.commands.ScannableCommands.scan
import time
import math
from epics_scripts.simple_channel_access import caput, caget
from  uk.ac.gda.devices.excalibur.scannable import ChipRegAnperScannable 
from gda.device.detector.addetector import ADDetector
from gda.device.detector import NXDetector

class   dacScan_positions(ScanPositionProvider):
    def __init__(self, firstScannable, start, stop, step):
        self.firstScannable = firstScannable
        self.start = start
        self.stop = stop
        self.step = ScanBase.sortArguments(start, stop, step);
        numberSteps = ScannableUtils.getNumberSteps(firstScannable, self.start, self.stop, self.step)
        self.points = []
        self.points.append(start)
        previousPoint = start
        for i in range(numberSteps):
            nextPoint = ScannableUtils.calculateNextPoint(previousPoint, self.step);
            self.points.append(nextPoint)
            previousPoint = nextPoint
        self.forward=True

    def get(self, index):
        max_index = self.size()-1
        if index > max_index:
            raise Exception("Position %d is outside possible range : %d" % (index, max_index))
        return dacscan_position(self.points[index], self.start, self.stop, self.step)
    
    def size(self):
        return len(self.points)
    
    def __str__(self):
        return "Scan %s from %s to %s in steps of %s. No of points = %d" % (self.firstScannable.getName(), `self.start`,`self.stop`,`self.step`, self.size() ) 
    def toString(self):
        return self.__str__()


class dacscan_position:
    def __init__(self, position, start, stop, step):
        self.position = position


class dac_scannable(ScannableBase):
    """
    Class that takes a scannable 
    Each position it receives contains is a tuple with the first element being the stop value to be sent to the scannable on first point in scan
    The scond value is the value that the scannable should reach for thiis scannable to report isBusy = false
    It has 1 input
    getposition returns the value at which the isBusy is to return false
    
    
    scan command 
    from flyscan_position_provider import flyscan_positions
    from  flyscan_scannable import flyscannable
    scan flyscannable(scannableA) flyscan_positions(scannableA, 0, 10, 1) det
    """
    def __init__(self, scannable):
        self.scannable = scannable
        if len( self.scannable.getInputNames()) != 1:
            raise Exception("No support for scannables with inputNames != 1")
        self.name = scannable.getName()+"_dac_scan"
        self.inputNames = [scannable.getInputNames() [0]]
        self.extraNames= []
        self.outputFormats=[scannable.getOutputFormat() [0]]
        self.level = 5
        self.requiredPosVal = 0.
        
    def isBusy(self):
        #we cannot monitor if the threshold value is changed so always return false
        return False

        
    def rawAsynchronousMoveTo(self,new_position):
        self.requiredPosVal = new_position
        return

    def rawGetPosition(self):
        return self.requiredPosVal


from gda.device.detector.addetector.triggering import SimpleAcquire

class BurstModeTrigger(SimpleAcquire):
    def __init__(self, adBase):
        SimpleAcquire.__init__(self,adBase, 0.)
        self.prefix = LocalProperties.get("gda.epics.excalibur.pvprefix")
    def prepareForCollection(self, collectionTime, numImages):
        SimpleAcquire.prepareForCollection(self,collectionTime, numImages)
        caput(self.prefix +':CONFIG:ACQUIRE:OperationMode',"Burst")
    def collectData(self):
        SimpleAcquire.collectData(self)



class DacScanTrigger(SimpleAcquire):
    def __init__(self, adBase, dacNumber, start, stop, step, ):
        SimpleAcquire.__init__(self,adBase, 0.)
        self.started = False
        self.start = start
        self.stop = stop
        self.step = step
        self.dacNumber = dacNumber
        self.prefix = LocalProperties.get("gda.epics.excalibur.pvprefix")
    def prepareForCollection(self, collectionTime, numImages, scanInfo):
        #need to clear capture here as it stays high
        caput(self.prefix +":CONFIG:HDF:NumCapture", 0)
        caput(self.prefix +":CONFIG:HDF:Capture", 0)

        SimpleAcquire.prepareForCollection(self,collectionTime, numImages, scanInfo)
        
        caput(self.prefix +':CONFIG:ACQUIRE:OperationMode',"DAC Scan")
        caput(self.prefix +':CONFIG:ACQUIRE:ScanDac',self.dacNumber)
        caput(self.prefix +':CONFIG:ACQUIRE:ScanStart',self.start)         # param
        caput(self.prefix +':CONFIG:ACQUIRE:ScanStop',self.stop)        # param
        caput(self.prefix +':CONFIG:ACQUIRE:ScanStep',self.step)          # param
        #ensure divisors of readoutNodes are all set to 1
        caput(self.prefix +":1:MASTER:FrameDivisor", 1)
        caput(self.prefix +":2:MASTER:FrameDivisor", 1)
#        caput(self.prefix +":3:MASTER:FrameDivisor", 1)
#        caput(self.prefix +":4:MASTER:FrameDivisor", 1)
#        caput(self.prefix +":5:MASTER:FrameDivisor", 1)
#        caput(self.prefix +":6:MASTER:FrameDivisor", 1)
# do not send RESYNC as it puts starts hdf writers and sets NUMCapture_RBV to 0
#        caput(self.prefix +":SYNC:RESEND.PROC", 1)
#        print "Waiting 2 seconds for RESYNC"
#        time.sleep(2.)
#        print "Wait complete"
        self.started=False
    def collectData(self):
        if not self.started:
            SimpleAcquire.collectData(self)
        self.started = True

def dacscan(args):
    dacScannableFound=False
    newargs=[]
    oldTrigger=None
    i=0;
    while i< len(args):
        #if arg is ChipRegAnperScannable and args[i] is a Detector
        if isinstance( args[i],  ChipRegAnperScannable) and (isinstance( args[i+4],ADDetector) or isinstance( args[i+4],NXDetector)) :
            dacScannable=dac_scannable(args[i])
            newargs.append(dacScannable)
            det = args[i+4]
            trigger=DacScanTrigger(det.getCollectionStrategy().getAdBase(), args[i].getIndex(), args[i+1], args[i+2], args[i+3])
            det.setCollectionStrategy(trigger)
            dacScannableFound=True
        else:
            newargs.append(args[i])
        i=i+1
    if not dacScannableFound:
        raise Exception("Usage dacscan ... dacScannable start stop step detector ...")
    gda.jython.commands.ScannableCommands.scan(newargs)


