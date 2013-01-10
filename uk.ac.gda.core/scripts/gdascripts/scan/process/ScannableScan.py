from gda.device.scannable import PseudoDevice
from gdascripts.scan.process.ScanDataProcessor import ScanDataProcessor
from gda.data import NumTracker

from gda.jython.commands import ScannableCommands

class ScannableScan(PseudoDevice):
	
	def __init__(self, name, datasetProcessor, scanClass, *args):
		self.name = name
		self.inputNames = []
		self.extraNames = ['scan'] + list(datasetProcessor.labelList)
		self.outputFormat = ['%i']+['%f']*len(datasetProcessor.labelList)
		
		self.datasetProcessorName = datasetProcessor.name
		self.scan = scanClass([ScanDataProcessor([datasetProcessor])])
		self.numTracker = NumTracker('scanbase_numtrack')
		self.args = tuple(args)
			
	def asynchronousMoveTo(self):
		raise Exception("Not supported")
	
	def isBusy(self):
		return False# getPosition is blocking
	
	def getPosition(self):
		if ScannableCommands.isPosCommandIsInTheProcessOfListingAllScannables():
			raise Exception(self.name + " is not readout while the pos commands samples all Scannables")
		sdpr = self.scan(*self.args)[self.datasetProcessorName]
		result = [int(self.numTracker.getCurrentFileNumber())]
		for label in self.extraNames[1:]:
			result.append(sdpr.result[label])
		return result
		