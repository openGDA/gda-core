from java.lang import Thread, Runnable
from gda.device.detector import DetectorBase

#
# A more complex template for detectors in which the work to perform the data
# collection is performed in its own thread.
#
#
#
class threadedTemplateDetectorClass(DetectorBase):

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
		newThread = collectDataThread(self)
		t = Thread(newThread)
		t.start()

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
		return self.myData;


#
# A method called internally by the threadedTemplateDetectorClass to collect 
# the data in a separate thread. 
#
# It is very important that the isCollecting flag is set to 0 at the end of 
# this method.
#
class collectDataThread(Runnable):

	def __init__(self, theDetector):
		self.myDetector = theDetector

	def run(self):
		print "you have asked me to collect data!"
		self.myDetector.myData +=1
		self.myDetector.isCollecting = 0

