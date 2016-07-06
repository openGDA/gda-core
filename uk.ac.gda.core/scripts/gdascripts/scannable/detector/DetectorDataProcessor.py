from gda.device.scannable import PseudoDevice
from gda.device.scannable import PositionCallableProvider
from gdascripts.scannable.detector.ProcessingDetectorWrapper import BasicDataSetProvider
from java.util.concurrent import Callable
from gdascripts.scannable.detector.DatasetShapeRenderer import RectPainter
from org.eclipse.dawnsci.analysis.api.io import ScanFileHolderException
from gda.device.detector.hardwaretriggerable import HardwareTriggerableDetector
from org.eclipse.january.dataset import Slice

class DetectorDataProcessorPositionCallable(Callable):
	
	def __init__(self, datasetProvider, processors, roi=None):
		self.datasetProvider = datasetProvider
		self.processors = processors
		self.roi = roi
		#self.x1, self.y1, self.x2, self.y2 = roi
	
	def call(self):
		result = []
		for processor in self.processors:
			if self.roi is None:
				xoffset, yoffset = 0, 0
			else:
				xoffset, yoffset, _, _ = self.roi
			xoffset = 0 if xoffset==None else xoffset
			yoffset = 0 if yoffset==None else yoffset
			twodDataSetResult = processor.process(self.__getDataset(), xoffset, yoffset)
			d = twodDataSetResult.resultsDict
			for key in processor.labelList:
				result.append(d[key])
		return result

	def __getDataset(self):
		dataset = self.datasetProvider.getDataset()
		if self.roi is None:
			return dataset
		else:
			x1, y1, x2, y2 = self.roi
			min_y = min(y1, y2)
			max_y = max(y1, y2)
			min_x = min(x1, x2)
			max_x = max(x1, x2)
			slice_y = Slice(min_y,max_y,1)
			slice_x = Slice(min_x,max_x,1)
			return dataset.getSlice(slice_y,slice_x)
# 			return dataset[min(y1, y2):max(y1, y2), min(x1, x2):max(x1, x2)]
	

class DetectorDataProcessor(PseudoDevice, PositionCallableProvider):

	def __init__(self, name, processingDetector, twodDatasetProcessors, prefix_name_to_extranames=True):
		self.name = name
		self.inputNames = []
		self.level = 10
		
		self.det = processingDetector
		self.processors = twodDatasetProcessors
		self.prefix_name_to_extranames = prefix_name_to_extranames
		
	def asynchronousMoveTo(self, t):
		raise Exception("DetectorDataProcessors don't yet support input parameters")
		
	def isBusy(self): 
		return False
	
	def getPosition(self, dataset=None):
		if dataset is None:
			return self.getPositionCallable().call()
		else:
			return self.getPositionCallable(dataset).call()

	def getPositionCallable(self, dataset=None):
		if dataset is None:
			datasetProvider = self.det.getDatasetProvider()
		else:
			datasetProvider = BasicDataSetProvider(dataset)
		return self._createPositionCallable(datasetProvider)
		
	def _createPositionCallable(self, datasetProvider):
		return DetectorDataProcessorPositionCallable(datasetProvider, self.processors)
	
	def getExtraNames(self):
		result = []
		for processor in self.processors:
			for label in processor.labelList:
				if self.prefix_name_to_extranames:
					result.append(self.name + '_' + label)
				else:
					result.append(label)
		return result
	
	def getOutputFormat(self):
		count = 0
		for processor in self.processors:
			count += len(processor.labelList)
		return ['%f'] * count
	
	
class DetectorDataProcessorWithRoi(DetectorDataProcessor):
	
	def __init__(self, name, processingDetector, twodDatasetProcessors, prefix_name_to_extranames=True):
		DetectorDataProcessor.__init__(self, name, processingDetector, twodDatasetProcessors, prefix_name_to_extranames)
		self.x1 = None
		self.y1 = None
		self.x2 = None
		self.y2 = None

	def setRoi(self, x1, y1=None, x2=None, y2=None):
		if x1 is None:
			self.y1 = self.x1 = self.x2 = self.y2 = None
			y1 = x2 = y2 = None
			try:
				self.det.removeShape(self, 'roi')
				print self.name, " roi --> *cleared*"
			except (ScanFileHolderException, IOError), _:
				print self.name, " roi --> *cleared (could not load file to display)"
		else:
			if x2 < x1:
				x1, x2 = x2, x1
			if y2 < y1:
				y1, y2 = y2, y1				
				
			self.x1 = x1
			self.y1 = y1
			self.x2 = x2
			self.y2 = y2
			try:
				self.det.addShape(self, 'roi', RectPainter(y1, x1, y2, x2))
				print self.name, " roi --> (%d, %d, %d, %d)" % (x1, y1, x2, y2)
			except (ScanFileHolderException, IOError), _:
				print self.name, " roi --> (%d, %d, %d, %d) (could not load file to display)" % (x1, y1, x2, y2)
				
		
	def getRoi(self):
		return self.x1, self.y1, self.x2, self.y2

	def _createPositionCallable(self, datasetProvider):
		return DetectorDataProcessorPositionCallable(datasetProvider, self.processors, (self.x1, self.y1, self.x2, self.y2))
	
	
class HardwareTriggerableDetectorDataProcessor(DetectorDataProcessorWithRoi, HardwareTriggerableDetector):
	
	def __init__(self, name, processingDetector, twodDatasetProcessors, prefix_name_to_extranames=True):
		DetectorDataProcessorWithRoi.__init__(self, name, processingDetector, twodDatasetProcessors, prefix_name_to_extranames)
	
	# HardwareTriggerableDetector
	
	def getHardwareTriggerProvider(self):
		return self.det.getHardwareTriggerProvider()

	def setHardwareTriggering(self, b):
		pass # let the wrapper do this 
								
	def isHardwareTriggering(self):
		return self.det.isHardwareTriggering()

	def integratesBetweenPoints(self):
		return self.det.integratesBetweenPoints()

	def arm(self):
		pass #  let the wrapper do this
		
	# Detector
		
	def setCollectionTime(self, t):
		raise Exception("Collection time not supported")

	def prepareForCollection(self):
		pass
	
	def getCollectionTime(self):
		return self.det.getCollectionTime()

	def collectData(self):
		pass
		
	def getStatus(self):
		return 0

	def readout(self):
		return self.getPositionCallable().call()
		
	def endCollection(self):
		pass

	def getDataDimensions(self):
		return [len(self.getExtraNames())]

	def createsOwnFiles(self):
		return False

	def getDescription(self):
		return " "

	def getDetectorID(self):
		return " "

	def getDetectorType(self):
		return " "
