
# This file should not be edited
# please put any beamline specific configuration code in a local scripts folder and name setup_bimorph.py
# see setup_dummy_bimorph.py here for instructions
#
# NOTE: This script should be run and expects the following to be in the root namespace:
#   peak2d  -- which should point to the detector in use
#   scanAborter
#   pos
#   scan

import java.lang.InterruptedException
from gda.device.scannable import PseudoDevice
from time import sleep
from gdascripts.pd.dummy_pds import DummyPD
from gda.scan import ConcurrentScan
from uk.ac.gda.beans import BeansFactory
from gda.epics import CAClient
from gdascripts.scannable.installStandardScannableMetadataCollection import meta

class TopupCountdown(PseudoDevice):

    def __init__(self, name):
        self.setName(name)
        self.setInputNames([])
        self.setOutputFormat([])
        self.secsBefore = 10
        self.tupv = CAClient("SR-CS-FILL-01:COUNTDOWN")
        self.tupv.configure()
        self.setLevel(7)

    def atPointStart(self):
        while True:
            p = float(self.tupv.caget())
            if p < 0:
                return
            if p > float(self.secsBefore):
                return
            sleep(5)
            print "* " + self.name + " waiting for topup to complete"

    def isBusy(self):
        return False

    def getPosition(self):
        return

    def asynchronousMoveTo(self, newPosition):
        return 

    def _readPv(self):
        return float(self.tupv.caget())

bm_topup = TopupCountdown("bm_topup")

class ScanAborter(PseudoDevice):
    def __init__(self, name, mon, minValue):
        self.name = name
        self.inputNames = []
        self.extraNames = [mon.getName()]
        
        self.mon = mon
        self.minValue=minValue
        
    def rawIsBusy(self):
        return False
    
    def getPosition(self):
        if not self.isOK():
            raise Exception("scanAborter stopping scan as " + self.mon.name + " is < " + `self.minValue`)
        return self.mon()

    def isOK(self):
        return self.mon() > self.minValue

    def asynchronousMoveTo(self):
        raise Exception(self.name + " cannot be moved");

# scanAborter = ScanAborterAndEnergy("scanAborter",qbpm1, 0.5)    
def_mon = DummyPD('mon')
def_mon.asynchronousMoveTo(1)
defScanAborter = ScanAborter("scanAborter",def_mon, 0.2)

def generatePositions(initialPos, increment):
    #    initialPos=[0,0,0,0,0,0,0,0]
    #    increment=5
    positions=[]
    positions.append(initialPos)
    for i in range(1,len(initialPos)+1):
        newPos=[]
        for j in range(len(initialPos)):
            newPos.append(initialPos[j])
        for k in range(i):
            newPos[k]+=increment
        positions.append(newPos)
    return positions

def generateGroupedPositions(initialPos, increment, groups_string):
    
    #initialPos=[0]*16
    #increment=50
    #groups_string="1-3,4,5,6,7,8,9,10,11,12,13,14-16"
    
    positions = []
    positions.append(initialPos[:])
    groups = groups_string.split(",")
    for g in groups:
        loc = g.find("-")
        if loc!=-1:
            start=int(g[:loc])
            end=int(g[loc+1:])
        else:
            start=end=int(g)  
        for i in range(start-1,end):
            initialPos[i]=increment
            
        positions.append(initialPos[:])
        
    return positions

#This is now depricated as the SlitScanner class below should be used instead. This has been left as some beamlines may still use it.
def getData(mirror, 
        increment, 
        slitToScanSize, # scannable to move slit width
        slitToScanPos, # scannable to move slit position
        slitSize, # width of slit in mm
        otherSlitSize, # scannable to move other slit width
        otherSlitPos, # scannable to move other slit position
        slitStart, 
        slitEnd, 
        slitStep,
        detector,
        exposure=768,
        settleTime=300,
        otherSlitSizeValue = 7.0 ):

    targetPositions = generatePositions(mirror.getPosition(),increment)
    print targetPositions
    pos([otherSlitPos, 0.]) #@UndefinedVariable
    pos([otherSlitSize, otherSlitSizeValue]) #@UndefinedVariable
    pos([slitToScanSize, slitSize]) #@UndefinedVariable
    for position in targetPositions:
        print "moving mirror to " + `position`
        pos([mirror, position]) #@UndefinedVariable
        print "mirror is now at " + `mirror()`
        print "Sleeping for " + `settleTime` + "s"
        sleep(settleTime)
        print "Slept"
        scanCompleted = False
        beam_was_down = False
        while(not scanCompleted):
            # loop until beam okay
            while( not self.scanAborter.isOK()): #@UndefinedVariable
                print "beam was down. checking again in 30s."
                sleep(30)
                beam_was_down = True
            # try the scan    
            try:
                if beam_was_down:
                    print "Beam has come back up. Waiting 10 minutes"
                    sleep(10*60)
                print "beam is ok so now trying scan"
                scan([slitToScanPos, slitStart, slitEnd, slitStep, detector, exposure, peak2d, self.scanAborter, bm_topup]) #@UndefinedVariable
                scanCompleted = True
            except java.lang.InterruptedException, e: # th scan always throws these
                if( self.scanAborter.isOK()): # Race condition here. rob W @UndefinedVariable
                    raise e
                else:
                    print "scan aborted due to drop in beam current"
                    # and try again from start


#This class is for use with the rich bean editor for bimorph optimization (BimorphParameters).
#This depricates the getData method above.
#It can also be used from the console as follows.
#from gdascripts.bimorph.bimorph_mirror_optimising import SlitScanner
#slitscanner = SlitScanner()
#slitscanner.run(globals(),"bm",50.0,"slits1_size","slits1_pos",1.0,"slits2_size","slits2_pos",-3.89,-5.21,-0.04,"det",35.0,5.0,1.0,False,None,"1-3,4,5,6,7")
class SlitScanner():
    
    def __init__(self):
        self.namespace = None
        self.mirror = None
        self.increment = None
        self.slitToScanSize = None
        self.slitToScanPos = None
        self.slitSize = None
        self.otherSlitSize = None
        self.otherSlitPos = None
        self.slitStart = None
        self.slitEnd = None
        self.slitStep = None
        self.detector = None
        self.exposure = None
        self.settleTime = None
        self.otherSlitSizeValue = None
        self.otherSlitPosValue = None
        self.peak2d = None
        self.doOptimization = None
        self.grouped = None
        self.groups_string = None
        self.scanAborter = None
        
        
    def setScanAborter(self, aborter):
        self.scanAborter = aborter
        
    def run(self, namespace, mirrorName=None, increment=None, slitToScanSizeName=None, slitToScanPosName=None, slitSize=None, otherSlitSizeName=None, otherSlitPosName=None, slitStart=None, slitEnd=None, slitStep=None, detectorName=None, exposure=None, settleTime=None, otherSlitSizeValue=None, otherSlitPosValue=None, doOptimization=None, grouped=None, groups_string=None):
        
        if self.scanAborter==None:
            self.scanAborter=defScanAborter
        
        self.namespace = namespace
        self.mirror = self.namespace.get(mirrorName)
        self.increment = increment
        self.slitToScanSize = self.namespace.get(slitToScanSizeName)
        self.slitToScanPos = self.namespace.get(slitToScanPosName)
        self.slitSize = slitSize
        self.otherSlitSize = self.namespace.get(otherSlitSizeName)
        self.otherSlitPos = self.namespace.get(otherSlitPosName)
        self.slitStart = slitStart
        self.slitEnd = slitEnd
        self.slitStep = slitStep
        self.detector = self.namespace.get(detectorName)
        self.exposure = exposure
        self.settleTime = settleTime
        self.otherSlitSizeValue = otherSlitSizeValue
        self.otherSlitPosValue = otherSlitPosValue
        self.peak2d = self.namespace.get("peak2d")
        self.grouped = grouped
        self.groups_string = groups_string
        number_electrodes = len(self.mirror.inputNames)
        if grouped==None:
            targetPositions = generatePositions(self.mirror.getPosition()[0:number_electrodes],self.increment)
        else:
            targetPositions = generateGroupedPositions(self.mirror.getPosition()[0:number_electrodes], self.increment, groups_string)
        
        print targetPositions
        self.otherSlitPos(self.otherSlitPosValue)
        self.otherSlitSize(self.otherSlitSizeValue)
        self.slitToScanSize(self.slitSize)
        for position in targetPositions:
            print "moving mirror to " + `position`
            self.mirror(position)
            print "mirror is now at " + `self.mirror()`
            print "Sleeping for " + `self.settleTime` + "s"
            sleep(self.settleTime)
            print "Slept"
            scanCompleted = False
            beam_was_down = False
            while(not scanCompleted):
                while( not self.scanAborter.isOK()):
                    print "beam was down. checking again in 30s."
                    sleep(30)
                    beam_was_down = True 
                try:
                    if beam_was_down:
                        print "Beam has come back up. Waiting 10 minutes"
                        sleep(10*60)
                    print "beam is ok so now trying scan"
                    if(len(meta.ls()) == 0):
                        curscan = ConcurrentScan([self.slitToScanPos, self.slitStart, self.slitEnd, self.slitStep, self.detector, self.exposure, self.peak2d, self.scanAborter, bm_topup])
                    else:
                        curscan = ConcurrentScan([self.slitToScanPos, self.slitStart, self.slitEnd, self.slitStep, self.detector, self.exposure, self.peak2d, self.scanAborter, bm_topup, meta])
                    curscan.runScan()
                    scanCompleted = True
                except java.lang.InterruptedException, e:
                    if self.scanAborter.isOK():
                        raise e
                    else:
                        print "scan aborted due to drop in beam current"
