from gov.aps.jca.event import MonitorEvent
from gov.aps.jca.event import MonitorListener
from time import sleep
from gda.epics import CAClient
from gda.device.scannable import PseudoDevice
from java import lang
from java.lang import Thread, Runnable
from gdascripts.pd.time_pds import tictoc
from gda.scan import ScanPositionProvider
from gda.device import DeviceException
import math

class BusyThread(Runnable):
    
    def __init__(self, master, period):
        self.master = master
        self.period = period
        self.tictoc = tictoc()

    def run(self):
        while (self.master.scanRunning):
            self.master.dontBeBusy()
            sleep(self.period)

class RampWaitThread(Runnable):
    
    def __init__(self, master, duration):
        self.master = master
        self.duration = duration

    def run(self):
        sleep(self.duration)
        self.master.nextRamp()
            
class LinkamRampMaster4000(PseudoDevice, MonitorListener, Runnable, ScanPositionProvider):
    
    def __init__(self, name, linkam):
        self.setName(name)
        self.setInputNames(["time"])
        self.linkam = linkam
        self.fixOutput()
        self.setLevel(5)
        self.outcli=CAClient(self.linkam.pvbase+":STATUS")
        self.dontbebusy = False
        self.time = 0
        self.interval = 6
        self.tictoc = tictoc()
        self.monitor = None
        self.thread = None
        self.atScanEnd()
        self.oldstate = -1

    def fixOutput(self):
        if self.linkam.dscenabled:
            self.setExtraNames(["temp", "dsc"])
            self.setOutputFormat(["%4.1f", "%3.1f", "%5.0f"])
        else:
            self.setExtraNames(["temp"])
            self.setOutputFormat(["%4.1f", "%3.1f"])
            
    def atScanStart(self):
        self.fixOutput()
        self.tictoc.reset()
        self.scanRunning=True
        if not self.outcli.isConfigured():
            self.outcli.configure()
        self.monitor=self.outcli.camonitor(self)
        self.thread=Thread(BusyThread(self,self.interval),"BusyThread: "+self.getName())
        self.thread.start()
        self.dontbebusy = True
        self.currentramp = -1
        self.nextRamp()

    def stop(self):
        self.atScanEnd()

    def atScanEnd(self):
        if not self.monitor == None:
            self.outcli.removeMonitor(self.monitor)
            self.monitor=None
        if self.outcli.isConfigured():    
            self.outcli.clearup()
        if not self.thread == None:
            self.thread=None
        self.scanRunning=False
        self.currentramp = -1
          
    def getPosition(self):
        li = self.linkam.getPosition()
        if self.linkam.dscenabled:
            return [ self.tictoc(), li[0], li[1] ]
        return [ self.tictoc(), li[0] ]

    def asynchronousMoveTo(self, position):
        if self.scanRunning:
            self.dontbebusy = False

    def isBusy(self):
        if self.dontbebusy:
            return False
        if not self.scanRunning:
            return False
        return True

    def dontBeBusy(self):
        self.dontbebusy = True

    def monitorChanged(self, mevent):
        state = int(mevent.getDBR().getEnumValue()[0])
        if self.currentramp == -1:
            return
        if not state == self.oldstate and state == 3:
            self.nextRamp()
        self.oldstate = state

    def nextRamp(self):
        if not self.scanRunning:
            return
        self.currentramp += 1
        if self.currentramp >= len(self.ramps):
            return
        targettemp = self.ramps[self.currentramp][1]
        rate = math.fabs(self.ramps[self.currentramp][0])
        if self.currentramp > 0 and math.fabs(targettemp - self.ramps[self.currentramp-1][1]) < 0.05:
            # it's not a rate after all
            wthread=Thread(RampWaitThread(self,rate),"WaitThread: "+self.getName())
            wthread.start()
            return
        self.linkam.setRate(rate)
        self.linkam.setLimit(targettemp)
        self.linkam.start()
        
    def setRamps(self, ramps):
        self.ramps = ramps
        self.calculateTime()

    def setCollectionInterval(self, interval):
        if interval > 1:
            self.interval = interval
        else:
            print "Collection interval too short %3.1f s" % interval
        
    def calculateTime(self):
        temp = self.linkam.getPosition()[0]
        rampno = 0
        totaltime = 0 
        for ramp in self.ramps:
            rate = ramp[0]
            target = ramp[1]
            if rampno > 0 and math.fabs(temp - target) < 0.05:
                totaltime += rate
            else:
                totaltime += 60.0 * math.fabs(temp - target) / math.fabs(rate)
            temp = target
            rampno += 1
        self.time = totaltime
            
    def size(self):
        if self.time == 0:
            self.calculateTime()
        return int(self.time / self.interval) + 1

    def get(self, index):
        return index  
