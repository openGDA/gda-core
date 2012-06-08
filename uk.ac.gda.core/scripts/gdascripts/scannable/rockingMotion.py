'''
This module provide a class definition for creating a scannable that does continuous rocking motion of a specified scannable between two re-settable limits.
Usage:
    To create this scannable:
        >>>from rockingMotion import ContinuousRocking
        >>>rocktheta=ContinuousRocking("rocktheta", theta, 5, -5)
    To use it in a scan:
        >>>scan ds 1 10 1 detector 10 rocktheta
    the rocking motion starts at Scan start and stop at Scan end. Emergency or panic stop is provided.
    You can change the rocking range and rocking motor by:
        >>>rocktheta.setRockingScannable(scannale)
        >>>rocktheta.setLowerGdalimits(-1)
        >>>rocktheta.setUpperGdalimits(10)
Created on 24 Jun 2010

@author: fy65
'''
from gda.device.scannable import ScannableMotionBase
from java.lang import Thread, Runnable
from gda.jython import JythonServerFacade, Jython
from gda.device.scannable.scannablegroup import ScannableGroup

class ContinuousRocking(ScannableGroup, Runnable):
    '''Create PD for continuous rocking motion of the specified scannable within its limits during a scan. '''
    def __init__(self, name, pd, upperlimit, lowerlimit):
        self.setName(name)
        self.setInputNames([name])
        self.pd=pd
        self.setUpperGdaLimits(upperlimit)
        self.setLowerGdaLimits(lowerlimit)
        self.setLevel(5)
        self.runThread=False
        self.thread=None
        
    def atScanStart(self):
        '''prepare to start scan: start rocking motion thread'''
        ScannableMotionBase.atScanStart()
        self.thread=Thread(self, "Thread: "+self.getName())
        self.runThread=True
        self.thread.start()
         
    def atScanEnd(self):
        '''clean up after scan finished successfully: stop rocking motion thread'''
        ScannableMotionBase.atScanEnd()
        self.runThread=False
        #self.thread.interrupt()
        self.thread=None
        self.pd.stop()         

    def run(self):
        '''rocking motion of the specified scannable during scan. This call must be non-blocking.'''
        while(self.runThread):
            if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING):
                if not self.pd.isBusy():
                    self.pd.asynchronousMoveTo(self.getUpperGdaLimits()[0])
            if (JythonServerFacade.getInstance().getScanStatus() == Jython.RUNNING):
                if not self.pd.isBusy():
                    self.pd.asynchronousMoveTo(self.getLowerGdaLimits()[0])
    
    def setLowerGdaLimits(self, lowerlimit):
        ScannableMotionBase.setLowerGdaLimits(lowerlimit)
        
    def getLowerGdaLimits(self):
        return ScannableMotionBase.getLowerGdaLimits()
    
    def setUpperGdaLimits(self, upperlimit):
        ScannableMotionBase.setUpperGdaLimits(upperlimit)
    
    def getUpperGdaLimits(self):
        return ScannableMotionBase.getUpperGdaLimits()
    
    def setRockingScannable(self, scnb):
        self.pd = scnb
        
    def getRockingScannable(self):
        return self.pd
    
    def rawGetPosition(self):
        '''This method not applies for the object, but returns pd position to satisfy framework interface'''
        return self.pd.getPosition()
      
    def rawAsynchronousMoveTo(self,new_position):
        '''No action, just pass'''
        pass
    
    def rawIsBusy(self):
        '''always return False'''
        return 0

    def toString(self):
        return self.name + " : " + str(self.getPosition())
              
    def stop(self):
        '''stop control thread on emergence stop or unexpected crash and clean up after scan finished successfully.
         If required, can be used to manually clean up the object.'''
        if not self.thread == None:
            self.runThread=False
            self.thread=None
            self.pd.stop()   
            