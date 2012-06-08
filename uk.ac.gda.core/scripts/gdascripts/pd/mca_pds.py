#
# Utilities to read from an mca using Pseudo Devices
#

from time import sleep
from java import lang
from gda.device.scannable import ScannableMotionBase
from gda.device import Scannable
from gda.factory import Finder

#
#
#
class ctmcaClass(ScannableMotionBase):
	"""Operates the mca as a PD.  Any movement 
	acquires data for the supplied length of time.
	If used in a scan, the output is the live time for
	each point, whereas if the Detector object was used,
	the entire spectrum would be readout at each point."""
	def __init__(self, name, mca):
		self.setName(name)
		self.setInputNames(['time'])
		self.Units=['sec']
		self.setOutputFormat(['%4.3f'])
		self.setLevel(5)
		self.mca=mca

	def rawAsynchronousMoveTo(self,time):
		self.mca.clear();
		self.mca.setCollectionTime(time)
		self.mca.startAcquisition()
		#put a sleep in here so rawIsBusy cannot be called too fast as Epics not always ready
		sleep(.5)
		return

	def rawGetPosition(self):
		return self.mca.getElapsedParameters()[1]

	def rawIsBusy(self):
		return self.mca.isActive()

#
#
#
class rdmcaClass(ScannableMotionBase):
	"""
	Reads out an mca spectrum as a PD. Moving it changes
	which mca element is being read.  This PD could be
	scanned to read out a spectrum element by element.
	"""
	def __init__(self, name, mca):
		self.setName(name)
		self.setInputNames(['channel'])
		self.Units=['int','int']
		self.setOutputFormat(['%4.0f','%4.0f'])
		self.setLevel(6)
		self.channel=0
		self.mca=mca

	def rawAsynchronousMoveTo(self,channel):
		self.channel=int(channel)
		
	def rawGetPosition(self):
		return self.mca.getData()[self.channel]

	def rawIsBusy(self):
		return 0

#
#
#
class rdROIClass(ScannableMotionBase):
	"""
	A region of interest in an mca spectrum as a PD.
	This cannot be moved, but its position is an array
	consisting of the sum of elements in the region
	followed by each individual element.
	"""
	def __init__(self, name,mca,lower,upper):
		self.setName(name)
		self.setLevel(10)
		self.lower = lower
		self.upper = upper
		self.mca=mca
		self.configure()

	def configure(self):
		inputs=[self.getName()]
		units =['int']
		formats = ['%10.0f']
		for i in range(self.upper - self.lower + 1):
			inputs.append('channel')
			units.append('int')
			formats.append('%4.0f')
		self.setInputNames(inputs)
		self.Units=units
		self.setOutputFormat(formats)

	def setLower(self,limit):
		"""Changes the lower limit of this ROI.  Use this rather than setting roi.lower directly"""
		self.lower = limit
		self.configure()
			
	def setUpper(self,limit):
		"""Changes the upper limit of this ROI.  Use this rather than setting roi.upper directly"""
		self.upper = limit
		self.configure()

	def rawAsynchronousMoveTo(self,channel):
		i = 0
		
	def rawGetPosition(self):
		output = [0]
		total = 0
		spectrum = self.mca.getData()
		for i in range(self.lower,self.upper + 1):
			total += spectrum[i]
			output.append(spectrum[i])
		output[0] = total
		return output

	def rawIsBusy(self):
		return 0

#
#
#
class rdScaClass(ScannableMotionBase):
	"""
	A region of interest in an mca spectrum as a PD.
	This cannot be moved, but its position is the 
	sum of elements in the region.
	"""
	def __init__(self, name,mca,lower,upper):
		self.setName(name)
		self.setLevel(10)
		self.lower = lower
		self.upper = upper
		self.mca=mca
		inputs=[name]
		units =['int']
		formats = ['%4.0f']
		self.setInputNames(inputs)
		self.Units=units
		self.setOutputFormat(formats)

	def rawAsynchronousMoveTo(self,channel):
		i = 0
		
	def rawGetPosition(self):
		total = 0
		spectrum = self.mca.getData()
		for i in range(self.lower,self.upper + 1):
			total += spectrum[i]
		return total

	def rawIsBusy(self):
		return 0



