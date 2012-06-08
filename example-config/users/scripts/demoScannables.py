from gda.device.scannable import PseudoDevice
import math

class ScannableGaussian(PseudoDevice):

	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		y = math.exp(-x*x)
		return( [self.currentposition, y] )

	def asynchronousMoveTo(self,new_position):
		self.currentposition = new_position
		
