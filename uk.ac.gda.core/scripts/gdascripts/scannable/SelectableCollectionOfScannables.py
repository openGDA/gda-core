from gda.device.scannable import ScannableMotionBase
from gda.jython.commands.InputCommands import requestInput


class SelectableCollectionOfScannables(ScannableMotionBase):
	"""Holds a list of single input scannables. Moveing the collection
	moves all the scannables. The fields of the resulting collection
	will be built from those of the component scannables. A single scannable
	can be selected as the 'important' scannable; this one will be
	reported via the last field(s), and will thus be plotted by default.

	"""
	
	def __init__(self, name, singleInputFieldScannableList):
		self.name = name
		self.inputNames = ['exp']

		self.scannables = singleInputFieldScannableList
		self.selectedIdx = len(singleInputFieldScannableList)-1
		self.last_exp = 0.
#		self.restoreSelectedIdx()
		
	def asynchronousMoveTo(self, pos):
		self.last_exp = float(pos)
		for scn in self.scannables:
			scn.asynchronousMoveTo(float(pos))
			
	def isBusy(self):
		for scn in self.scannables:
			if scn.isBusy():
				return True
		return False
	
	def getPosition(self):
		result = [self.last_exp]
		for scn in self.getReorderedScannables():
			result.extend(forceToList(scn.getPosition()))
		return result
	
	def getExtraNames(self):
		result = []
		for scn in self.getReorderedScannables():
			result.extend(getAllScannableFieldNames(scn))
		return result
	
	def getOutputFormat(self):
		result = ['%f']
		for scn in self.getReorderedScannables():
			result.extend(scn.getOutputFormat())
		return result
	
	def getReorderedScannables(self):
		result = []
		for i, scn in enumerate(self.scannables):
			if i != self.selectedIdx:
				result.append(scn)
		result.append(self.scannables[self.selectedIdx])
		return result	
	
	def selectScannable(self, idx=None):
		if idx is not None:
			self.setSelectedIdx(idx)
		else:
			print self.__repr__()
			idx = requestInput('Enter number')
			self.setSelectedIdx(int(idx))

	def setSelectedIdx(self, idx):
		if not(0<=idx<len(self.scannables)):
			raise ValueError("Index must be an integer from 0 to %i"%len(self.scannables))
		self.selectedIdx = idx
#		self.storeMonitorIdx()

	def storeSelectedIdx(self):
		raise Exception("STUB")
	
	def restoreSelectedIdx(self):
		self.selectedIdx = 0
		raise Exception("STUB")
		
	def __str__(self):
		return self.__repr__()
	
	def __repr__(self):
		result = '\n'	
		# all scn values
		positions = []
		for scn in self.scannables:
			positions.extend(forceToList(scn.getPosition()))
			
		for idx, scn, pos in zip(range(len(self.scannables)), self.scannables, positions):
			if idx==self.selectedIdx:
				result += '--> '
			else:
				result += '    '				
			result += "%i. %s : %f\n" % (idx, scn.name, pos)
		return result

def getAllScannableFieldNames(scn):
	return list(scn.getInputNames()) + list(scn.getExtraNames())

def forceToList(ob):
	if isinstance(ob, str):
		return [ob]
	try:
		return list(ob)
	except TypeError :
		return [ob]