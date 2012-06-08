from gda.device.detector import PseudoDetector
from gda.device import Detector
import time


# TODO: Make a real detector!
class ImageReadingDummyDetector(PseudoDetector):
	
	def __init__(self, name, idxscannable, collectionFolder, posToFileDict, infoString):
		self.name = name
		self.idxscannable = idxscannable
		self.collectionFolder = collectionFolder
		self.posToFileDict = posToFileDict
		self.collectionTime = 0.
		self.info = infoString
		self.timeCollectionStarted = 0


	def collectData(self):
		self.timeCollectionStarted = time.time()
	
	def getStatus(self):
		busy = (time.time()-self.timeCollectionStarted) < self.getCollectionTime()
		return Detector.BUSY if busy else Detector.IDLE
	
	def createsOwnFiles(self):
		return True
	
	def readout(self):
		idx = self.idxscannable()
		try:
			filename = self.posToFileDict[idx]
		except KeyError:
			s = "The ImageReadingDummyDetector %s has no data for the position %f (set via scannable %s)\n" \
				% (self.name, idx, self.idxscannable.name)
			keys = self.posToFileDict.keys()
			keys.sort()
			s+= "Try values: %s\n" % keys
			s+= "Try scans:\n"
			s+= self.info
			raise KeyError(s)
		if self.collectionFolder[-1] != '/':
			self.collectionFolder += '/'
		return self.collectionFolder + filename
