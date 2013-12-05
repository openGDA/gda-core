import java
#import gda.device.scannable.PseudoDevice
import gda.device.scannable.ScannableMotionBase

class TwoCircle(gda.device.scannable.ScannableMotionBase):

    def __init__(self, name, theta, ttheta):
        self.name = name
        self.theta = theta
        self.ttheta = ttheta
        self.setInputNames(['theta'])
        self.setExtraNames(['ttheta'])
        self.setOutputFormat(['%.4f', '%.4f'])

    def isBusy(self):
        return self.theta.isBusy() or self.ttheta.isBusy()

    def getPosition(self):
        return [ self.theta.getPosition(), self.ttheta.getPosition()]

    def asynchronousMoveTo(self,newPosition):
        self.theta.asynchronousMoveTo(newPosition)
        self.ttheta.asynchronousMoveTo(2*newPosition)
