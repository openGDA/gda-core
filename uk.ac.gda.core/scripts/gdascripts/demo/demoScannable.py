#@PydevCodeAnalysisIgnore
# example of a simple scannable class

# To use in the command-line type:
# from demoScannable import demoScannableClass
#
# Then to instantiate an instance named y:
# y=demoScannableClass(10)

import java
from gda.jython.scannable import PseudoDevice

class demoScannableClass(PseudoDevice):

	def __init__(self, initialValue):
		self.currentposition = initialValue

	def isMoving(self):
		return 0

	def getPosition(self):
		return self.currentposition

	def asynchronousMoveTo(self,new_position):
		self.currentposition = new_position
	
	def atStart(self):
		print "doing atStart()!"

	def atEnd(self):
		print "doing atEnd()!"