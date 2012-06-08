class A:
	pass


class B:
	def __init__(self):
		print "B.__init__() called"
		self.value = 1

	def setValue(self, val):
		self.value = val

	def getValue(self):
		return self.value

	def printValue(self):
		print "A " + str(self.value) + " A"


class C(B):
	def printValue(self):
		print "CCCCCCCCCCCCCCCCCCCCCC"
		print "C", str(self.value).center(18), "C"
		print "CCCCCCCCCCCCCCCCCCCCCC"