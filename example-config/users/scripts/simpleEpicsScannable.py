import java
from gda.device.scannable import ScannableMotionBase
from gda.epics import CAClient


class SimpleEpicsScannable(ScannableMotionBase):

	def __init__(self, name, pvName):
		self.name = name
		self.pvName = pvName
		self.ca = CAClient()

	def rawIsBusy(self):
		return 0

	def rawGetPosition(self):
		return float(self.ca.caget(self.pvName))

	def rawAsynchronousMoveTo(self,new_position):
		self.ca.caput(self.pvName)



#myEpicsTest = demoScannableClass("myEpicsTest","some pv string...")


	
