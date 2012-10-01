'''
filename: positionCompareMotorClass.py

This class uses position compare to determine motor status. It does not use low level Motor Status directly.
Only 3 PVs - set, readback, and stop - are required. No other motor status are handled by this class.
The motor's positional tolerance must be set, that is the same as the retry deadband value of the Motor Record.

Created on 27 Oct 2010

@author: fy65
'''

from gda.epics import CAClient 
from gda.device.scannable import ScannableMotionBase

class PositionCompareMotorClass(ScannableMotionBase):
    '''Create a scannable for a single motor'''
    def __init__(self, name, pvinstring, pvoutstring, pvstopstring, tolerance, unitstring, formatstring):
        self.setName(name);
        self.setInputNames([name])
        self.Units=[unitstring]
        self.setOutputFormat([formatstring])
        self.setLevel(3)
        self.incli=CAClient(pvinstring)
        self.outcli=CAClient(pvoutstring)
        self.stopcli=CAClient(pvstopstring)
        self._tolerance=tolerance
        
    def setTolerance(self, tolerance):
        self._tolerance=tolerance
        
    def atScanStart(self):
        if not self.incli.isConfigured():
            self.incli.configure()
        if not self.outcli.isConfigured():
            self.outcli.configure()
        if not self.stopcli.isConfigured():
            self.stopcli.configure()
         
    def rawGetPosition(self):
        try:
            if not self.outcli.isConfigured():
                self.outcli.configure()
                output=float(self.outcli.caget())
                self.outcli.clearup()
            else:
                output=float(self.outcli.caget())
            return output
        except:
            print "Error returning current position"
            return 0

    def getTargetPosition(self):
        try:
            if not self.incli.isConfigured():
                self.incli.configure()
                target=float(self.incli.caget())
                self.incli.clearup()
            else:
                target=float(self.incli.caget())
            return target
        except:
            print "Error returning target position"
            return 0
       
    def rawAsynchronousMoveTo(self,new_position):
        try:
            if not self.incli.isConfigured():
                self.incli.configure()
                self.incli.caput(new_position)
                self.incli.clearup()
            else:
                self.incli.caput(new_position)
        except:
            print "error moving to position"

    def rawIsBusy(self):
        return ( not abs(self.rawGetPosition() - self.getTargetPosition()) < self._tolerance)

    def atScanEnd(self):
        if self.incli.isConfigured():
            self.incli.clearup()
        if self.outcli.isConfigured():
            self.outcli.clearup()
        if self.stopcli.isConfigured():
            self.stopcli.clearup()
            
    def stop(self):
        if not self.stopcli.isConfigured():
            self.stopcli.configure()
            self.stopcli.caput(1)
            self.stopcli.clearup()
        else:
            self.stopcli.caput(1)

    def toString(self):
        return self.name + " : " + str(self.getPosition())
              
