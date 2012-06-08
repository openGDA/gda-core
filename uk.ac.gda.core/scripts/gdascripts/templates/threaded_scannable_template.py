from gda.device.scannable import PseudoDevice
from java.lang import Thread, Runnable

#
# Threaded version of the class in scannable_template in case the work performed
# within the rawAsynchronousMoveTo takes a long time.
#
class threadedScannableTemplate(PseudoDevice):

	#
	# The constructor. 
	#
	def __init__(self, name):
		self.name = name
		self.currentposition = 10 # this scannable represents a single number
		self.setInputNames([name])
		self.setExtraNames([])
		self.setOutputFormat(["%5.5g"])
		self.iambusy = 0 # flag to hold the status of the scannable

	#
	# Returns the value represented by this Scannable. This should be a number
	# or an array of numbers
	#
	def rawGetPosition(self):
		return self.currentposition

	#
	# Creates a new moveScannableThread object to do the work and then starts it
	# in a new thread.
	#
	def rawAsynchronousMoveTo(self,new_position):
	
		self.iambusy = 1
		newThread = moveScannableThread(self,new_position)
		t = Thread(newThread)
		t.start()

	#
	# Returns false (0) if the action started by rawAsynchronousMoveTo has been 
   # completed
	#
	def rawIsBusy(self):
		return self.iambusy


#
# An object called internally by the threadedScannableTemplate.
#
# It is very important that the busy flag is set to 0 at the end of 
# the run method.
#
class moveScannableThread(Runnable):

	#
	# Constructor for this class
	#
	def __init__(self, theScannable,new_position):
		self.myScannable = theScannable
		self.target = new_position

	#
	# Does the work to move what the Scannable represents. This is run in a new 
   # thread started by the line in rawAsynchronousMoveTo: t.start()
	#
	def run(self):
		print "you have asked me to move to",str(self.target)
		self.myScannable.currentposition = self.target	
		self.myScannable.iambusy=0




