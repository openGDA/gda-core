import java
from gda.device.scannable import ScannableMotionBase
from math import exp, sqrt

class GaussianX(ScannableMotionBase):
	"""Device to allow control and readback of X value"""
	def __init__(self, name):
#		PseudoDevice.__init__(self) #do not required as it will be called at end of this __init__ by default
		self.setName(name)
		self.setInputNames([name])
		self.X = 0
		
	def rawIsBusy(self):
		return 0

	def rawGetPosition(self):
		return self.X

	def rawAsynchronousMoveTo(self,new_position):
		self.X = new_position	

class GaussianY(ScannableMotionBase):
	"""Device to readback at a certain position of another device e.g. a GaussianX
	x = GaussianX("XVal")
	y = GaussianY("YVal", x, 0., 10., 100., 5)
	pos x 10
	pos y
	scan x -10 10 1 y
	"""
	def __init__(self, name, gaussianX, centre, width, height, background):
#		PseudoDevice.__init__(self)
		self.setName(name)
		self.setInputNames([name])
		#self.setExtraNames([name])
		self.gaussianX = gaussianX
		self.width=width
		self.height=height
		self.centre = centre
		self.background=background

	def rawIsBusy(self):
		return 0

	def rawGetPosition(self):
		if self.height == None:
			return self.background + self.gaussianX.getPosition()
		return self.background + self.height * exp( -1.0 *(((self.gaussianX.getPosition() - self.centre )**2)/ self.width) )

	def rawAsynchronousMoveTo(self,new_position):
		pass	
