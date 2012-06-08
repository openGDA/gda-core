from gda.device.scannable import ScannableMotionBase


class SimplestPD(ScannableMotionBase):
	"""Device to allow control and readback of X value"""
	def __init__(self, name, position):
		self.setName(name)
		self.setInputNames([name])
		self.X = position
		
	def rawIsBusy(self):
		return 0

	def rawGetPosition(self):
		return self.X

	def rawAsynchronousMoveTo(self,new_position):
		self.X = new_position	

class DummyPD(ScannableMotionBase):
	'''Dummy PD Class'''
	def __init__(self, name):
		self.setName(name)
		self.setInputNames([name])
		self.Units=['Units']
		self.setOutputFormat(['%6.4f'])
		self.setLevel(3)
		self.currentposition=0.0

	def rawIsBusy(self):
		return 0

	def rawAsynchronousMoveTo(self,new_position):
		self.currentposition = float(new_position)

	def rawGetPosition(self):
		return self.currentposition
	
	
class MultiInputExtraFieldsDummyPD(ScannableMotionBase):
	'''Multi input Dummy PD Class supporting input and extra fields'''
	def __init__(self, name, inputNames, extraNames):
		self.setName(name)
		self.setInputNames(inputNames)
		self.setExtraNames(extraNames)
		self.Units=['Units']*(len(inputNames)+len(extraNames))
		self.setOutputFormat(['%6.4f']*(len(inputNames)+len(extraNames)))
		self.setLevel(3)
		self.currentposition=[0.0]*len(inputNames)
		#self.completeInstantiation()
		
	def rawIsBusy(self):
		return 0

	def rawAsynchronousMoveTo(self,new_position):
		if type(new_position)==type(1) or type(new_position)==type(1.0):
			new_position=[new_position]
		assert len(new_position)==len(self.currentposition), "Wrong new_position size"
		for i in range(len(new_position)):
			if new_position[i] != None:
				self.currentposition[i] = float(new_position[i])

	def rawGetPosition(self):
		extraValues = range(100,100+(len(self.getExtraNames())))
		return self.currentposition + map(float,extraValues)

class ZeroInputExtraFieldsDummyPD(ScannableMotionBase):
	'''Zero input/extra field dummy pd
	'''
	def __init__(self, name):
		self.setName(name)
		self.setInputNames([])
		self.setOutputFormat([])

	def atScanStart(self):
		print "In atScanStart() method of zero input/extra dummy pd"

	def rawIsBusy(self):
		return 0

	def rawAsynchronousMoveTo(self,new_position):
		pass

	def rawGetPosition(self):
		pass
