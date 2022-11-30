'''
Created on 10 Apr 2014

@author: zrb13439
'''
from gda.device.scannable import ScannableBase
from gda.epics import CAClient
import time

class PVWithSeparateReadbackAndToleranceScannable(ScannableBase):

    def __init__(self, name, pv_set, pv_read, timeout, tolerance = 0.0005): #BL16B-EA-PSU-01
        self.name = name
        self.inputNames = [name]
        self.outputFormat = ['%6.4f']

        self.timeout = timeout
        self.tol = tolerance
        self._time_triggered = None
        self._last_target = None
        self._pv_set = CAClient(pv_set)
        self._pv_read = CAClient(pv_read)
        self._pv_set.configure()
        self._pv_read.configure()

    def asynchronousMoveTo(self, value):
        self._pv_set.caput(value)
        self._time_triggered = time.time()
        self._last_target =  value
    
    def isBusy(self):
        
        if self._last_target == None:
            return False
        
        i = (float(self._pv_read.caget()))
        
        if abs(i - self._last_target) <= self.tol:
            return False
        
        if (time.time() - self._time_triggered) > self.timeout:
            raise Exception('Timed out after %fs setting current to %f. The current has hung at %f, and the voltage is %f\n*Is the voltage set too low?*' % ((self.timeout, self.last_target) + self.getPosition()))
        
        return True

    def getPosition(self):
        return float(self._pv_read.caget())