from gdascripts.scannable.epics.PvManager import PvManager


from time import sleep
from gda.device.detector import PseudoDetector
from gda.device.Detector import BUSY, IDLE

# test = ScanFileContainer()
# test.loadPilatusData("/dls/i16/data/Pilatus/test1556.tif")
# test.plot()
# matrix = test.getImage().doubleMatrix()


class EpicsPilatus(PseudoDetector):
	'''Pilatus PD
	obj=PilatusClass(name,pvroot,filepath,filename)
	e.g. pilatus=PilatusClass('P100k','BL16I-EA-PILAT-01:','/dls/i16/data/Pilatus/','p')
	pilatus.getThesholdEnergy to print threshhold
	Make sure camserver is runnning
	ssh -X det@i16-pilatus1  (password Pilatus2)
 	ps aux | grep cam   to see if camserver running
	to start camserver...
	cd p2_1mod
	camonly
	self.display_data=0 removes data plotting and data summary for faster aquisition
	self.setFileName(name) sets data file name e.g. 'p'
	self.setFilePath(name) sets data file path e.g. '/dls/i16/data/Pilatus/'
	self.defaultFileName()/ self.defaultFilePath()sets data file name/path back to the name given when the device was created
	'''

	def __init__(self, name, pvroot, filepath, filename, fileformat):
		self.name = name
		self.inputNames = ['ExposureTime']
		self.extraNames = ['FileNum']
		self.outputFormat = ['%.2f', '%.0f']
		self.level = 9

		self.filepath = None
		self.filename = None
		self.fileformat = None
		self.filenum = None
		self.cached_exptime = None
		self.filenum = None

		self.pvs = PvManager(['Acquire','NImages','Abort','ExposureTime','FilePath','Filename','FileNumber','FileFormat','ThresholdEnergy'], pvroot)	
		self.configure(filepath, filename, fileformat)

	def configure(self, filepath = None, filename=None, fileformat = None):
		self.pvs.configure()
		if filepath:
			self.setFilepath(filepath)
		if filename:
			self.setFilename(filename)
		if fileformat:
			self.setFileformat(fileformat)
		self.setNumberImages(1)
		self.updateFilenumber()
		self.cached_exptime = 0
		sleep(1)

# DETECTOR INTERFACE
	def createsOwnFiles(self):
		return True

	def setCollectionTime(self, t):
		if t != self.cached_exptime:
			self.setCollectionTimeCommand(t)
		self.cached_exptime = t
	
	def getCollectionTime(self):
		return self.cached_exptime
	
	def collectData(self):
		self.acquireCommand()
	
	def getStatus(self):
		if self.isBusyCommand():
			return BUSY
		else:
			return IDLE
	
	def readout(self):
		self.updateFilenumber()
		return self.fileformat % (self.filepath, self.filename, self.filenum)

# SCANNABLE INTERFACE (currently used by pos but not scan ??)

	def stop(self):
		self.stopCommand()

	def getPosition(self):
		self.updateFilenumber()
		self.readout()
		return [self.cached_exptime, self.filenum]

	def asynchronousMoveTo(self, t):
		self.setCollectionTime(t)
		self.collectData()

	def getFilepath(self):
		return self.filepath
# PILATUS COMMANDS
	# Note: Command appended where name already taken
	def setFilename(self, filename):
		self.pvs['Filename'].caput(filename)
		self.filename = filename

	def setFilepath(self, filepath):
		self.filepath=filepath		
		self.pvs['FilePath'].caput(filepath)
		
	def setFileformat(self, fileformat):
		self.fileformat = fileformat
		self.pvs['FileFormat'].caput(fileformat)
		
	def updateFilenumber(self):
		self.filenum = float(self.pvs['FileNumber'].caget())-1

	def setNumberImages(self, n):
		self.pvs['NImages'].caput(n)

	def setCollectionTimeCommand(self, t):
		self.pvs['ExposureTime'].caput(t)
		sleep(1)

	def isBusyCommand(self):
		return float(self.pvs['Acquire'].caget())

	def stopCommand(self):
		self.pvs['Abort'].caput(1)
		
	def acquireCommand(self):
		self.pvs['Acquire'].caput(1)
		
	def getThresholdEnergy(self):
		print "WARNING: this method returns before the underlying hardware has been updated"
		return float(self.pvs['ThresholdEnergy'].caget())

