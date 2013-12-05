from gda.device.scannable import ScannableMotionBase
import math
import random


class SimpleScannable(ScannableMotionBase):
	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue
		self.setInputNames(['x'])
		self.setOutputFormat(['%.3f'])

	def isBusy(self):
		return False

	def getPosition(self):
		return self.currentposition

	def asynchronousMoveTo(self, position):
		self.currentposition = position



class ScannableGaussian(ScannableMotionBase):
	"""
	This class is a Scannable (PseudoDevice) which returns the value of a Gaussian at each sampled point in the scan.

	The user can define 4 optional parameters of the Gaussian

	- centre (default=0)
	- width (default=1)
	- height (default=1)
	- noise level (fraction of the true value; default=0)

	Example usage: 
	>>>sg = ScannableGaussian("sg", 0.0)
	>>>sg = ScannableGaussian("sg", 0.0, centre=3.0)
	>>>sg = ScannableGaussian("sg", 0.0, centre=3.0, width=0.5)
	>>>sg = ScannableGaussian("sg", 0.0, centre=3.0, width=0.5, height=2)
	>>>sg = ScannableGaussian("sg", 0.0, centre=3.0, width=0.5, height=2, noise=0.1)

	Constructor: def __init__(self, name, initialValue, centre=0, width=1, height=1, noise=0):	
	"""

	def __init__(self, name, initialValue, centre=0, width=1, height=1, noise=0):
		self.name = name
		self.currentposition = initialValue
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

		# Gaussian-specific parameters
		setattr(self, "centre", centre);
		print "centre: " + str(getattr(self, "centre"))
		setattr(self, "width", width);
		print "width: " + str(getattr(self, "width"))
		setattr(self, "height", height);
		print "height: " + str(getattr(self, "height"))
		setattr(self, "noise", noise);
		print "noise: " + str(getattr(self, "noise"))
		# printParameters(self)

	def printParameters(self):
		print "parameters for Gaussian scannable:"
		print "centre: " + str(getattr(self, "centre"))
		print "width: " + str(getattr(self, "width"))
		print "height: " + str(getattr(self, "height"))
		print "noise: " + str(getattr(self, "noise"))

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		centre = getattr(self, "centre")
		width = getattr(self, "width")
		height = getattr(self, "height")
		noise = getattr(self, "noise")
		x2 = x - centre
		sigma = 0.425 * width
		y = math.exp(-x2*x2/(sigma*sigma)) * height * (1 + (random.random() - 0.5)*noise)
		return [ x, y ]

	def asynchronousMoveTo(self,new_position):
		self.currentposition = new_position

	

class ScannableSine(ScannableMotionBase):
	"""
	This class is a Scannable (PseudoDevice) which returns the value of a sine at each sampled point in the scan.

	The user can optionally define 4 optional parameters of the sine:

	- period (default=1.0)
	- phase (default=0.0)
	- y_offset (displacement of the sine wave on the y-axis; default=0.0)
	- noise level (fraction of the true value; default=0.0)

	Example usage: 
	>>>ss = ScannableSine("ss", 0.0)
	>>>ss = ScannableSine("ss", 0.0, period=0.5)
	>>>ss = ScannableSine("ss", 0.0, phase=0.2, y_offset=1.5)
	>>>ss = ScannableSine("ss", 0.0, period=3.0, noise=0.1)
	>>>ss = ScannableSine("ss", 0.0, period=2.0, magnitude=2.0, phase=0.5, y_offset=1.0, noise=0.1)

	Constructor: def __init__(self, name, initialValue, period=1.0, magnitude=1.0, phase=0.0, y_offset=0.0, noise=0.0):	
	"""

	def __init__(self, name, initialValue, period=1.0, magnitude=1.0, phase=0.0, y_offset=0.0, noise=0.0):
		self.name = name
		self.currentposition = initialValue
		self.period = period
		self.phase = phase
		self.y_offset = y_offset
		self.noise = noise
		self.magnitude = magnitude

		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		x =  self.currentposition;
		y = self.y_offset + self.magnitude * math.sin((x-self.phase) / self.period) + ((random.random()-0.5)*self.noise)
		return [ x, y ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position	
	


class ScannableRandom(ScannableMotionBase):
	"""
	For each scanned position, the value returned is random.random()
	"""
	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue 
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		return [ self.currentposition, random.random() ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position



class ScannableRandomRange(ScannableMotionBase):
	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue
		self.randomRange = initialValue
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		y = random.randrange(self.randomRange)
		return [ x, y ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position



class ScannableGaussianDev(ScannableMotionBase):
	def __init__(self, name, initialValue, mu=0, sigma=1):
		self.name = name
		self.currentposition = initialValue
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])
		
		# GaussianDev-specific values
		self.mu = mu
		self.sigma = sigma

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		y = random.gauss(self.mu, self.sigma)
		return [ x, y ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position



class ScannableExponentialDev(PseudoDevice):
	def __init__(self, name, initialValue, lambd=0.25):
		self.name = name
		self.currentposition = initialValue
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])
		
		# ExponentialDev-specific values
		self.lambd = lambd

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		y = random.expovariate(self.lambd)
		return [ x, y ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position



class ScannableXYFunc(PseudoDevice):
	def __init__(self, name, initialValue, func):
		self.name = name
		self.currentposition = initialValue
		self.func = func;
		self.setInputNames(['x'])
		self.setExtraNames(['y'])
		self.setOutputFormat(['%.4f', '%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		x = self.currentposition
		y = self.func(x)
		return [ x, y ]

	def asynchronousMoveTo(self, new_position):
		self.currentposition = new_position



class ScannableGaussianWidth(PseudoDevice):
	"""
	This class is a Scannable (PseudoDevice) which takes a reference to a scannableGaussian instance:
	and sets the width parameter of this Gaussian to its current value, by a custom implementation of its asynchronousMoveTo method:

	def asynchronousMoveTo(self, new_position):
		self.gaussian.setWidth(new_position)


	Example usage: 
	>>>sgw = ScannableGaussianWidth("sgw", 0.0, sg)

	where "sg" is an existing instance of the ScannableGaussian device
	"""

	def __init__(self,name,gaussian):
		self.name = name
		self.gaussian = gaussian
		self.setInputNames(['width'])
		self.setExtraNames([])
		self.setOutputFormat(['%.4f'])

	def isBusy(self):
		return False

	def getPosition(self):
		return self.gaussian.getWidth()

	def asynchronousMoveTo(self, new_position):
		self.gaussian.setWidth(new_position)
		
