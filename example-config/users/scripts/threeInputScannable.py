import java
from gda.device.scannable import ScannableMotionBase

class hklScannable(ScannableMotionBase):

	def __init__(self, name):
		self.name = name
		self.currentPosition = [1,1,1]
		self.setInputNames(['h','k','l'])
		self.setOutputFormat(['%.1f','%.1f','%.1f'])

	def isBusy(self):
		return 0

	def getPosition(self):
		return self.currentPosition

	def asynchronousMoveTo(self,new_position):
		self.currentPosition = new_position
	




	
