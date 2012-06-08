from gda.device.scannable import PseudoDevice

#
# A template for all Scannable classes. 
#
# The rawIsBusy, rawGetPosition, and rawAsynchronousMoveTo methods must be 
# implemented. 
# 
# The others (commented out here) are optional depending on how your scannable 
# works.
#
# ******************************************************************************
# Note: the inputNames, extraNames and outputFormat arrays defined in __init__:
# Your Scannable could represent no numbers, a single number or an array of 
# numbers. These arrays define what the Scannable represents.
#
# The inputNames array is a list of labels of the elements accepted by the 
# new_position argument of the rawAsynchronousMoveTo method. 
# 
# The extraNames array is a list of labels of extra elements in case the array 
# returned by rawGetPosition is larger than the array accepted by 
# rawAsynchronousMoveTo.
#
# The outputFormat array is used when pretty-printing the scannable and lists 
# the format to use for each element in the array returned by rawGetPosition.
# It is very important that the size of this array matches the sum of the sizes
# of inputNames and extraNames.
#
#
# ******************************************************************************
#
class scannableTemplate(PseudoDevice):

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
	# Does the work represented by this Scannable. If this takes a long time, 
   # then you should run a separate thread from within this method. See the
   # threaded_scannable_template.py script for details on hwo to do this.
	#
	def rawAsynchronousMoveTo(self,new_position):
		self.iambusy = 1
		self.currentposition = new_position
		self.iambusy = 0

	#
	# Returns false (0) if the action started by rawAsynchronousMoveTo has been 
   # completed
	#
	def rawIsBusy(self):
		return self.iambusy

	#
	# Called when panic stop called on the system.
	#
#	def stop(self):
#		print str(self.name),"stop called!"

	#
	# Implement this to override the pretty-print version of this Scannable
	#
#	def toString(self):
#		return self.name

	#
	# Given an object, this returns true (1) if that object is a valid position
	# for this scannable to use in its rawAsynchronousMoveTo method
	#
#	def isPositionValid(self):
#		return 1

	#
	# Called just before every node in a scan
	#
#	def atPointStart(self):
#		print str(self.name),"doing atPointStart()!"

	#
	# Called after every node in a scan
	#
#	def atPointEnd(self):
#		print str(self.name),"doing atPointEnd()!"

	#
	# In multi-dimensional scans, called before each line in the scan
   # This is still called once in single dimensional scans.
	#
#	def atScanLineStart(self):
#		print str(self.name),"doing atScanStart()!"

	#
	# In multi-dimensional scans, called after each line in the scan
   # This is still called once in single dimensional scans.
	#
#	def atScanLineEnd(self):
#		print str(self.name),"doing atScanEnd()!"

	#
	# Called at the start of the scan (called once in multi-dimensional scans)
	#
#	def atScanGroupStart(self):
#		print str(self.name),"doing atGroupStart()!"

	#
	# Called at the end of the scan (called once in multi-dimensional scans)
	#
#	def atScanGroupEnd(self):
#		print str(self.name),"doing atGroupEnd()!"


