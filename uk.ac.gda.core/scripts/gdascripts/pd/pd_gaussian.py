import java
from gda.device.scannable import ScannableMotionBase
from math import exp, sqrt
import random

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
	def __init__(self, name, gaussianX, centre, width, height, background, noise=0):
#		PseudoDevice.__init__(self)
		self.setName(name)
		self.setInputNames([name])
		#self.setExtraNames([name])
		self.gaussianX = gaussianX
		self.width=width
		self.height=height
		self.centre = centre
		self.background = background
		self.noise = noise

	def rawIsBusy(self):
		return 0

	def rawGetPosition(self):
		if self.height == None:
			return self.background + self.gaussianX.getPosition()
		val = self.background + self.height * exp( -1.0 *(((self.gaussianX.getPosition() - self.centre )**2)/ self.width) )
		return val + self.height * self.noise * (0.5 - random.random())

	def rawAsynchronousMoveTo(self,new_position):
		pass

class EdgeY(GaussianY):
	def __init__(self, name, gaussianX, centre, width, height, background, noise=0):
		self.cur_position = background
		self.setName(name)
		self.setInputNames([name])
		self.reset = background
		self.gauss = GaussianY(name + "_gauss", gaussianX, centre, width, height, 0, noise)

	def rawGetPosition(self):
		self.cur_position += self.gauss.getPosition()
		return self.cur_position

	def atScanLineStart(self):
		self.cur_position = self.reset
