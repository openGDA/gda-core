import java
import gda
from gda.device.scannable import ScannableMotionBase
from math import exp, sqrt

class StepX(ScannableMotionBase):
    """Device to allow control and read back of slit positions"""
    def __init__(self, name):
        PseudoDevice.__init__(self) #@UndefinedVariable
        self.setName(name)
        self.setInputNames([name])
        self.X = 0
        
    def isBusy(self):
        return 0

    def getPosition(self):
        return self.X

    def asynchronousMoveTo(self,new_position):
        self.X = new_position    

class StepY(ScannableMotionBase):
    """Device to read back at a certain position of another device e.g. a SlitPos
        slitPos = SlitPos("Slit Position")
        detRead = DetRead("Detector Reading", slitPos)
        scan slitPos -10 10 1 detRead
        """
    def __init__(self, name, slitPos, centre, width, height):
        PseudoDevice.__init__(self) #@UndefinedVariable
        self.setName(name)
        self.setInputNames([name])
        self.slitPos = slitPos
        self.height=height
        self.centre = centre
        self.width=width
        
    def isBusy(self):
        return 0

    def getPosition(self):
        if (self.centre-(self.width/2.0))<=self.slitPos.getPosition()<=(self.centre+(self.width/2.0)):
            return self.height
        else:
            return 0

    def asynchronousMoveTo(self,new_position):
        pass    
