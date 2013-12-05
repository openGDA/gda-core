from gda.device.scannable import ScannableBase

class SimpleDummyScannable(ScannableBase):
	def __init__(self, name, initialValue):
		self.name = name
		self.currentposition = initialValue

	def isBusy(self):
		return False

	def getPosition(self):
		return self.currentposition

	def asynchronousMoveTo(self,new_position):
		self.currentposition = new_position




class VerboseDummyScannable(ScannableBase):
	def __init__(self, name, initialValue = 0.0):
		self.name = name
		self.currentposition = initialValue

	def isBusy(self):
		print " "*30 + self.name + ".isBusy() --> False"
		return False

	def getPosition(self):
		print " "*30 + self.name + ".getPosition() --> " + str(self.currentposition)
		return self.currentposition

	def asynchronousMoveTo(self,new_position):
		# ensure it's a float
		new_position = float(new_position)
		print " "*30 + self.name + ".asynchronousMoveTo( %s )" % str(new_position)
		self.currentposition = new_position

	def atScanStart(self):
		print " "*30 + self.name + ".atScanStart()"

	def atScanEnd(self):
		print " "*30 + self.name + ".atScanEnd()"

	def getLevel(self):
		level = PseudoDevice.getLevel(self)
		print " "*30 + self.name + ".getLevel() --> " + str(level)
		return level
