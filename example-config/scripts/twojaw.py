#
# Example Scannable to demonstrate:
#
# 1. the methods and String arrays which need to be written to define a Scannable
#
# 2. how the output array might be larger than the input array
#


from gda.device.scannable import PseudoDevice
from gda.device.scannable import ScannableMotor
from gda.device.motor import TotalDummyMotor

class twojawslits(PseudoDevice):
    
    def __init__(self,name,slit1,slit2):
        self.setName(name)                                         # required
        self.setInputNames(["gap","offset"])                       # required
        self.setExtraNames([slit1.getName(),slit2.getName()])      # required
        self.setOutputFormat(["%5.2g","%5.2g","%5.2g","%5.2g"])    # required
        self.slit1 = slit1
        self.slit2 = slit2
        
    #
    # Required to fulfil the Scannable interface.
    # Returns an array of numbers defined by the inputNames and extraNames arrays.
    #    
    def getPosition(self):
        return [self.calcGap(),self.calcOffset(),self.slit1(),self.slit2()]
    
    #
    # Required to fulfil the Scannable interface.
    # Operates this scannable based on an array of numbers defined by the inputNames array
    #
    def asynchronousMoveTo(self,newPosition):
        
        if newPosition.__len__() != 2:
            raise Exception("position array of wrong length!")
        
        if newPosition[0] == None:
            newPosition[0] = self.calcGap()
        
        if newPosition[1] == None:
            newPosition[1] = self.calcOffset()
            
        motor1Target = newPosition[1] - (newPosition[0] / 2.)
        motor2Target = newPosition[1] + (newPosition[0] / 2.)
        
        self.slit1.asynchronousMoveTo(motor1Target)
        self.slit2.asynchronousMoveTo(motor2Target)
        
    #
    # Required to fulfil the Scannable interface.
    # Returns a boolean to say if this Scannable is in a state to have its asychronousMoveTo called.
    #
    def isBusy(self):
        return self.slit1.isBusy() and self.slit2.isBusy()


    #
    # internal to this example - calculates the slit size
    #
    def calcGap(self):
        return self.slit2() - self.slit1()
    
    #
    # internal to this example - calculates the slit centre relative to 0 
    # (assuming the two motors are using the same coordinate system and motor2 is at a higher position than motor 1)
    #
    def calcOffset(self):
        return self.calcGap() / 2 + self.slit1()
  
s1motor1 = TotalDummyMotor()
s1motor1.setName("s1motor1")
s1slit1 = ScannableMotor()
s1slit1.setName("s1slit1")
s1slit1.setMotor(s1motor1)
s1slit1.configure()

s1motor2 = TotalDummyMotor()
s1motor2.setName("s1motor2")  
s1slit2 = ScannableMotor()
s1slit2.setName("s1slit2")
s1slit2.setMotor(s1motor2)
s1slit2.configure()

s1 = twojawslits("s1",s1slit1,s1slit2)

# to setup these objects, type into the jython console:
# run "twojaw"

#
# Note that you do not have to supply two values every time. So if you only wish to change the slit gap, type:
#
# s1([2,None])
#
# You can also use None in scan commands for Scannables. E.g.:
#
# scan s1 [0,0] [1,0] [0.1,None]
#
        