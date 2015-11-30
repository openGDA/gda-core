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
		return addNoise(val, self.height * self.noise)

	def rawAsynchronousMoveTo(self,new_position):
		pass

class EdgeY(ScannableMotionBase):
	def __init__(self, name, x, centre, width, start, end, noise=0):
		self.setName(name)
		self.setInputNames([name])
		self.gx = x
		self.centre = centre
		self.width = width
		self.start = start
		self.end = end
		self.noise = noise

	def rawIsBusy(self):
		return False

	def rawGetPosition(self):
		height = self.end - self.start
		erf = approxErrorFunction((self.gx.getPosition() - self.centre)/self.width)
		erf = (erf + 1)/2.0
		return self.start + height * addNoise(erf, self.noise)

	def rawAsynchronousMoveTo(self,new_position):
		pass

def approxErrorFunction(x):
	#approximation based on https://en.wikipedia.org/wiki/Error_function#Numerical_approximation
	t = 1.0/(1 + 0.5 * abs(x))
	tau = t * exp(-x**2 - 1.265512223 + 1.00002368*t + 0.37409196*t**2 + 0.09678418*t**3 - 0.18628806*t**4 + 0.27886807*t**5 - 1.13520398*t**6 + 1.48851587*t**7 - 0.82215223*t**8 + 0.17087277*t**9)
	if x < 0:
		return tau - 1
	else:
		return 1 - tau

def addNoise(x, noise):
	return x + noise * (1 - 2 * random.random())
