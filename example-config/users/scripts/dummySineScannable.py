from gda.device.scannable import PseudoDevice
from java.lang import Math as Math

class DummySineScannable(PseudoDevice):
	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue 
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		return [ self.currentposition, Math.sin(self.currentposition) ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position
	

