'''
Created on 24 Jul 2012

@author: fy65
'''

from time import sleep
from gda.device.detector import DetectorBase
import random
from gda.device import Detector
class DummyDetector(DetectorBase):
    def __init__(self,name):
        self.name=name
        self.data=random.randint(10,1000000)
        self._status=Detector.IDLE
        
    def collectData(self):
        self._status=Detector.BUSY
        self.data=(random.randint(10,1000000))*self.getCollectionTime()
        sleep(self.getCollectionTime())
        self._status=Detector.IDLE
        
    def readout(self):
        return int(self.data)
    
    def setStatus(self,status):
        self._status=status
        
    def getStatus(self):
        return self._status
    
