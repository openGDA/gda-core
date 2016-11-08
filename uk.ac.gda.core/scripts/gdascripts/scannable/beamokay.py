from gda.device.scannable import ScannableMotionBase
from gda.jython import JythonServerFacade
from gda.jython.Jython import RUNNING

from time import sleep, strftime
from gda.device import DeviceException


def reprtime():
    return strftime('%H:%M:%S')

class WaitWhileScannableBelowThresholdMonitorOnly(ScannableMotionBase):
    '''
    Can be configured with any monitor-like scannable and a minimum threshold.
    This scannable's getPosition method will not return until the monitor-like scannable
    returns a number above the threshold.
    
    When it does return getPosition returns 1 if okay or zero to indicate that during the last point a
    beamdump occured.
    
    getPosition reports status changes and time.
    '''

    def __init__(self, name, scannableToMonitor, minimumThreshold, secondsBetweenChecks=1, secondsToWaitAfterBeamBackUp=0):
        self.scannableToMonitor = scannableToMonitor
        self.minimumThreshold = minimumThreshold
        self.secondsBetweenChecks = secondsBetweenChecks
        self.secondsToWaitAfterBeamBackUp = secondsToWaitAfterBeamBackUp
        
        self.setName(name);
        self.setInputNames([])
        self.setExtraNames([name+"_beamok"]);

        self.Units=[]
        self.setOutputFormat(['%.0f'])
        self.setLevel(6)
        
        self.lastStatus = True # Good
        self._operating_continuously=False
        
    def setOperatingContinuously(self, b):
        '''when set to True, no topup monitor will be active. Default is False.
        '''
        self._operating_continuously = b

    def isOperatingContinously(self):
        return self._operating_continuously

    def atScanStart(self):
        print '=== Beam checking enabled: '+self.scannableToMonitor.getName()+' must exceed '+str(self.minimumThreshold)+', currently '+str(self._getStatus())
        self.statusRemainedGoodSinceLastGetPosition = True
        if self._operating_continuously:
            while not self._getStatusAndHandleChange():  
                # not okay, so wait here
                sleep(self.secondsBetweenChecks)
                self._collectNewMonitorValue()  
            

    def isBusy(self):
        '''This can't be used as isBusy is not checked unless the scannable
        is 'moved' by passing in a number'''
        return False

    def waitWhileBusy(self):
        if not self._operating_continuously:
            if JythonServerFacade.getInstance().getScanStatus()==RUNNING:
                # loop until okay
                while not self._getStatusAndHandleChange():  
                    # not okay
                    sleep(self.secondsBetweenChecks)
                    self._collectNewMonitorValue()  
                # now okay
        
    def getPosition(self):
        '''If scan is running then pauses until status is okay and returning False
        if the scan was not okay. If scan is not running, return the current state
        and print a warning that the scan is not being paused.
        This only works if scan is not continuous.
        '''
        self.statusRemainedGoodSinceLastGetPosition = 1.0
        
        if not self._operating_continuously:
            if JythonServerFacade.getInstance().getScanStatus()==RUNNING:
                # loop until okay
                while not self._getStatusAndHandleChange():  
                    # not okay
                    self.statusRemainedGoodSinceLastGetPosition = 0.0
                    sleep(self.secondsBetweenChecks)
                    self._collectNewMonitorValue()  
                # now okay
            else: # scan not running
                currentStatus = self._getStatus()
                if not currentStatus: # bad
                    print self.name + " not holding read-back as no scan is running"
                self.statusRemainedGoodSinceLastGetPosition = currentStatus
        
        return self.statusRemainedGoodSinceLastGetPosition

    def _getStatus(self):
        val = self.scannableToMonitor.getPosition()
        if type(val) in (type(()), type([])):
            val = val[0]
        #ensure scan continues when topup is shutdown. 
        if val==-1 and self.scannableToMonitor.getName()=="topup_time":
            return True
        status =  (val >= self.minimumThreshold)
        return status

    def _getStatusAndHandleChange(self):
        ## Check current status, reports and returns it
        status = self._getStatus()
        self.handleStatusChange(status) 
        return status
        
    def handleStatusChange(self,status):
        ## check for status change to provide feedback:
        if status and self.lastStatus:
            pass # still okay
        if status and not self.lastStatus:
            print "*** " + self.name + ": Beam back up at: " + reprtime() + " . Resuming scan in " + str(self.secondsToWaitAfterBeamBackUp) + "s..."
            self.lastStatus = True
            sleep(self.secondsToWaitAfterBeamBackUp)
            print "*** " + self.name + ":  Resuming scan now at " + reprtime()
        if not status and not self.lastStatus:
            pass # beam still down
        if not status and self.lastStatus:
            print "*** " + self.name + ": Beam down at: " + reprtime() + " . Pausing scan..."
            self.lastStatus = False
            
    def _collectNewMonitorValue(self):
        pass


class WaitWhileScannableBelowThreshold(WaitWhileScannableBelowThresholdMonitorOnly):

    '''
    For any scannable the first number returned will pause a scan by halting the 
    getPosition method.
    
    getPosition returns 1 if okay or zero to indicate that during the last point a
    beamdump occured.
    
    getPosition reports status changes and time.
    
    In this version if a time is specified in a scan command, the scannable
    to monitor will be triggered; this is useful if a countertimer is used to monitor
    some physical parameter.
    This will start the thing counting in the synchronousMoveTo method, and if needbe start
    it counting again before reading it every secondsBetweenChecks.
    '''

    # overide

    def __init__(self, name, scannableToMonitor, minimumThreshold, secondsBetweenChecks=1, secondsToWaitAfterBeamBackUp=None):
        self.countTime = None
        WaitWhileScannableBelowThresholdMonitorOnly.__init__( self, name, scannableToMonitor, minimumThreshold, secondsBetweenChecks, secondsToWaitAfterBeamBackUp )

    def asynchronousMoveTo(self, time):
        # Store the time for the case that the threshold is low and a new count must be made.
        self.countTime = time
        if time !=None:
            self._triggerCount(time)
    
    def isBusy(self):
        return self.scannableToMonitor.isBusy()

    def _collectNewMonitorValue(self):
        if self.countTime !=None:
            self._triggerCount(self.countTime)#
            self._waitForCountToComplete()

    def _triggerCount(self, time):
        self.scannableToMonitor.asynchronousMoveTo(time)

    def _waitForCountToComplete(self):
        while self.scannableToMonitor.isBusy():
            sleep(.1)


class WaitForScannableState(WaitWhileScannableBelowThresholdMonitorOnly):
    '''USefult mainly for waiting for a shutter or beamline front-end to open
    '''
    
    def __init__(self, name, scannableToMonitor, secondsBetweenChecks, secondsToWaitAfterBeamBackUp=None, readyStates=['Open'], faultStates=['Fault']):
        WaitWhileScannableBelowThresholdMonitorOnly.__init__( self, name, scannableToMonitor, None, secondsBetweenChecks, secondsToWaitAfterBeamBackUp )
        self.readyStates = readyStates
        self.faultStates = faultStates
        self.setExtraNames([]);
        self.setOutputFormat([])
    
    def atScanStart(self):
        readyStatesString = self.readyStates[0] if len(self.readyStates)==1 else str(self.readyStates)
        print '=== Beam checking enabled: '+self.scannableToMonitor.getName()+' must be in state: ' + readyStatesString+', currently '+str(self._getStatus())
        self.statusRemainedGoodSinceLastGetPosition = True
        
    def getPosition(self):
        WaitWhileScannableBelowThresholdMonitorOnly.getPosition(self)
        return None
        
    def _getStatus(self):
        pos = self.scannableToMonitor.getPosition()
        if type(pos) in (type(()), type([])):
            pos = pos[0]
        if pos in self.faultStates:
            raise DeviceException(self.name + " found " + self.scannableToMonitor.name + " to be in state: " + pos)
        return pos in self.readyStates

    def handleStatusChange(self, status):
        readyStatesString = self.readyStates[0] if len(self.readyStates)==1 else str(self.readyStates)
        ## check for status change to provide feedback:
        if status and self.lastStatus:
            pass # still okay
        if status and not self.lastStatus:
            delayReport = " . Resuming scan" if not self.secondsToWaitAfterBeamBackUp else (" . Resuming scan in " + str(self.secondsToWaitAfterBeamBackUp) + "s...")
            print "*** " + self.name + ": ready in state " + readyStatesString + " at: " + reprtime() + delayReport
            self.lastStatus = True
            if self.secondsToWaitAfterBeamBackUp:
                sleep(self.secondsToWaitAfterBeamBackUp)
                print "*** " + self.name + ":" + reprtime() + " . Resuming scan now."
        if not status and not self.lastStatus:
            pass # beam still down
        if not status and self.lastStatus:
            print "*** " + self.name + ": not ready: " + reprtime() + " . Pausing scan..."
            self.lastStatus = False
