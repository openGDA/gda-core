class B:
	
	def setValue(self, val):
		self.value = val

	def getValue(self):
		return self.value

	def printValue(self):
		print "* " + str(self.value) + " *"

	def __init__(self):
		print "B.__init__() called"

	self.value = 1
