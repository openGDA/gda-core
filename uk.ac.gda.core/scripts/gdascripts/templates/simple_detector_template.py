from gda.device.detector import DetectorBase

#
# A template class to use as a basis to create your own Detector objects.
#
# Detectors must work in the following manner:
#     - a call to collectData to collect some new data. Ideally this should be 
#       asynchronous (i.e. the function returns immediately and the work is done
#       in a new Thread). See threaded_detector_template.py for this.
#     - repeated calls to getStatus may be made by external classes to see if
#       data is still being collected.
#     - once getStatus returns false (0) then a call to readout maybe made by 
#       external classes to collect the data.
#
#
class templateDetectorClass(DetectorBase):

	#
	# The constructor.
	#
	def __init__(self, name):
		self.setName(name)
		self.isCollecting = 0
		self.myData = 0

	#
	# Performs the work to collect some data. This method should not return the 
	# data, but instead keep the status field up to date.
	#
	def collectData(self):
		self.isCollecting = 1
		print "you have asked me to collect data!"
		self.myData += 1
		self.isCollecting = 0
		return

	#
	# Returns true (1) if this object is busy collecting data
	#
	def getStatus(self):
		return self.isCollecting

	#
	# Returns the last data which was collected. This should only be called when 
	# getStatus returns false
	#
	def readout(self):
		return self.myData

		





