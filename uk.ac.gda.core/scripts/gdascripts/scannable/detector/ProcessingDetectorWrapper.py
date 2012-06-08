from uk.ac.diamond.scisoft.analysis import SDAPlotter
from gda.analysis import DataSet
try:
	from gda.analysis import Plotter
except ImportError:
	Plotter = None

import java.lang.Long #@UnresolvedImport

from gda.data.fileregistrar import IFileRegistrar
from gda.device.Detector import BUSY
from gda.device.scannable import PseudoDevice
from gda.device.scannable import PositionCallableProvider
from gda.factory import Finder
from gdascripts.scannable.detector.DatasetShapeRenderer import DatasetShapeRenderer
from java.util.concurrent import Callable #@UnresolvedImport
import time
from gdascripts.analysis.io.DatasetProvider import LazyDataSetProvider, BasicDataSetProvider
import gda.device.detector.NXDetectorData
from gda.device.detector.hardwaretriggerable import HardwareTriggerableDetector

class FileRegistrar(object):
	
	def __init__(self):
		resistrarDict = Finder.getInstance().getFindablesOfType(IFileRegistrar)
		self.registrars = resistrarDict.values()
	
	def registerFile(self, filepath):
		for registrar in self.registrars:
			registrar.registerFile(filepath)


class ProcessingDetectorWrapperPositionCallable(Callable):

	def __init__(self, collectionTime, filepathToReport, processors, pathToRegister, panelName, panelNameRCP, datasetProvider, renderer, fileRegistrar, return_performance_metrics):
		self.collectionTime = collectionTime
		self.filepathToReport = filepathToReport
		self.processors = processors
		self.pathToRegister = pathToRegister
		self.panelName = panelName
		self.panelNameRCP = panelNameRCP
		self.datasetProvider = datasetProvider
		self.renderer = renderer
		self.fileRegistrar = fileRegistrar
		self.return_performance_metrics = return_performance_metrics
		if return_performance_metrics:
			self.time_created = time.time()
			

	def call(self):
		# 1. Register file
		if self.pathToRegister:
			self.fileRegistrar.registerFile(self.pathToRegister)
		# 2. generate position	
		position = [self.collectionTime, self.filepathToReport]
		processorResults = []
		for processor in self.processors:
			processorResults += list(processor.getPosition(self.datasetProvider.getDataset()))
		# 3. display image
		if self.panelName:
			#print "Plotter.plotImage(%r, ...)" % self.panelName
			Plotter.plotImage(self.panelName, self.renderer.renderShapesOntoDataset(self.datasetProvider.getDataset()))
		if self.panelNameRCP:
			#print "RCPPlotter.imagePlot(%r, ...)" % self.panelNameRCP
			SDAPlotter.imagePlot(self.panelNameRCP, self.renderer.renderShapesOntoDataset(self.datasetProvider.getDataset()))
		metricsResults = [time.time()-self.time_created] if self.return_performance_metrics else []
		return position + metricsResults + processorResults
		

class ProcessingDetectorWrapper(PseudoDevice, PositionCallableProvider):
	def __init__(self,
				name,
				detector,
				processors=[],
				panel_name=None,
				toreplace=None,
				replacement=None,
				iFileLoader=None,
				root_datadir=None,
				fileLoadTimout=None,
				printNfsTimes=False,
				returnPathAsImageNumberOnly=False,
				panel_name_rcp=None,
				return_performance_metrics=False):
				
	
		self._setDetector(detector)
		self.processors = processors
		self.panel_name = panel_name
		self.panel_name_rcp = panel_name_rcp
		self.toreplace = toreplace
		self.replacement = replacement
		self.iFileLoader = iFileLoader
		self.root_datadir = root_datadir
		self.fileLoadTimout = fileLoadTimout
		self.printNfsTimes = printNfsTimes
		self.returnPathAsImageNumberOnly = returnPathAsImageNumberOnly
		self.return_performance_metrics = return_performance_metrics
		self.disable_operation_outside_scans = False
		
		self.name = name
		self.inputNames = ['t']
		self.level = 9

		self.fileRegistrar = FileRegistrar() # as this is not currently a proper detector
		
		self.display_image = True
		self.process_image = True
		self.renderer = DatasetShapeRenderer()

		self.cached_readout = None # renewed per point
		self.datasetProvider = None # renewed per point

		self._operatingInScan = False
		self._preparedForScan = False
	
	def _setDetector(self, det):
		self.det = det
	
	def getExtraNames(self):
		extraNames = ['path']
		if self.return_performance_metrics:
			extraNames.append("t_process")
		if self.process_image:
			for processor in self.processors:
				extraNames += list(processor.getInputNames())
				extraNames += list(processor.getExtraNames())
		return extraNames
	
	def getOutputFormat(self):
		processorFormats = [] 
		if self.process_image:
			for processor in self.processors:
				processorFormats += list(processor.getOutputFormat())
		metricsFormats = ['%f'] if self.return_performance_metrics else []
		return ['%f', self.__getFilePathFormat()] + metricsFormats + processorFormats
	
	def __getFilePathFormat(self):
		return '%i' if self.returnPathAsImageNumberOnly else '%s'
	
	def atScanStart(self):
		self.det.atScanStart()
		self._operatingInScan = True
		self._preparedForScan = False
		
	def atScanLineStart(self):
		self.det.atScanLineStart()
		
	def asynchronousMoveTo(self, t):
		self.clearLastAcquisitionState()
		if not self._operatingInScan:
			if self.disable_operation_outside_scans:
				raise Exception("This detector cannot be operated outside a scan. Try 'scan x 1 1 1 det <t_exp>'")
			self.det.setCollectionTime(t)
			self.det.prepareForCollection()
		else:
			if not self._preparedForScan:
				self.det.setCollectionTime(t)
				self.det.prepareForCollection()
				self._preparedForScan = True
				
		self.det.collectData();
		
	def clearLastAcquisitionState(self):
		self.datasetProvider = None
		self.cached_readout = None
		
	def isBusy(self): 
		return self.det.getStatus() == BUSY
	
	def waitWhileBusy(self):
		self.det.waitWhileBusy() # from BlockingDetector interface
	
	def getPosition(self):
		return self.getPositionCallable().call()
	
	def atScanEnd(self):
		self._operatingInScan = False
		self._preparedForScan = False
		print self.name + " %s saved last file to: %s" % (self.name, self.getFilepath())
		self.det.atScanEnd()
		
	def stop(self):
		self._operatingInScan = False
		self._preparedForScan = False
		self.det.stop()
		
	def atCommandFailure(self):
		self._operatingInScan = False
		self._preparedForScan = False
		self.det.atCommandFailure()
###
	def getPositionCallable(self):
		pathToRegister = self.getFilepath() if self.det.createsOwnFiles() else None
		processors = self.processors if self.process_image else []
		if self.display_image and self.panel_name == None and self.panel_name_rcp == None:
			raise Exception("No panel_name or panel_name_rcp set in %s. Set this or set %s.display_image=False"
						 % (self.name, self.name))	
		panelName = self.panel_name if self.display_image else None
		panelNameRCP = self.panel_name_rcp if self.display_image else None
		return ProcessingDetectorWrapperPositionCallable(
			self.det.getCollectionTime(), self.__getFilePathRepresentation(),
			processors, pathToRegister, panelName, panelNameRCP,
			self.getDatasetProvider(), self.renderer, self.fileRegistrar, self.return_performance_metrics
			)
	
	def getFilepath(self):
		if self.det.createsOwnFiles():
			try:
				# assume detector has read out an NXDetectorDataWithFilepathForSrs object
				path_from_detector = self._readout().getFilepath()
			except AttributeError:
				path_from_detector = self._readout()
			return self.replacePartOfPath(path_from_detector)
		else:
			try:
				last_image_path = self.det.getLastImagePath()
				if last_image_path is not None:
					return last_image_path
			except AttributeError:
				pass # only some python detectors support this (and its a mess)
			return time.strftime("%H%M%S", time.localtime()) # %Y%m%d% makes it too long!
		
	def _readout(self):
		if self.cached_readout is None:
			self.cached_readout = self.det.readout()
		return self.cached_readout

	def __getFilePathRepresentation(self):
		if self.returnPathAsImageNumberOnly:
			path = self.getFilepath()
			# return a java Long to prevent the PyLong from adding an 'L' to long 
			try:
				return java.lang.Long(int(''.join([c for c in path.split('/')[-1] if c.isdigit()])))
			except ValueError:
				return -1
		else:
			return self.getFilepathRelativeToRootDataDir()
	
	def getFilepathRelativeToRootDataDir(self):
		if self.root_datadir == None:
			return self.getFilepath()
		try:
			return self.getFilepath().split(self.root_datadir)[1]
		except IndexError:
			raise ValueError("%s.root_datadir = '%s' was not found at start of '%s'. Fix this or try '%s.root_datadir=None'" % (self.name, self.root_datadir, self.getFilepath(), self.name))
			
	def getDataset(self, retryUntilTimeout = True):
		return self.getDatasetProvider().getDataset(retryUntilTimeout)
	
	def getDatasetProvider(self):
		if self.datasetProvider == None:
			self.__configureNewDatasetProvider()
		return self.datasetProvider

###
	
	def __configureNewDatasetProvider(self):
		if self.det.createsOwnFiles():
			path = self.getFilepath()
			if path == '':
				raise IOError("Could no load dataset: %s does not have a record of the last file saved" % self.name)
			path = self.replacePartOfPath(path)
			self.datasetProvider = LazyDataSetProvider(path, self.iFileLoader, self.fileLoadTimout, self.printNfsTimes)
		else:
#			if not isinstance(dataset, DataSet, gda.device.detector.NXDetectorData):
#				raise Exception("If a detector does not write its own files, ProcessingDetectorWrapper %s only works with detectors that readout DataSets.")
			dataset = self._readout()
			if isinstance(dataset, gda.device.detector.NXDetectorData):
				data = dataset.getNexusTree().getChildNode(1).getChildNode(1).getData()
				dataset = DataSet(data.getBuffer())
				dataset.setShape(data.dimensions)
				dataset.squeeze()
				
			self.datasetProvider = BasicDataSetProvider(dataset)
	
	def replacePartOfPath(self, path):
		if self.toreplace != None and self.replacement != None:
			path = str(path)
			path = path.replace(self.toreplace, self.replacement)
		#replace the double \\ resulting from windows mounts to /
		path = str(path)
		return path.replace('\\', '/')
	
###
	def addShape(self, detectorDataProcessor, shapeid, shape):
		self.renderer.addShape(detectorDataProcessor, shapeid, shape)
		if self.display_image:	
			# The last taken image is often not in the current folder, so don't retry until timeout
			self.__tryToDisplay(False) 
			
	def removeShape(self, detectorDataProcessor, shapeid):
		self.renderer.removeShape(detectorDataProcessor, shapeid)
		if self.display_image:	
			self.__tryToDisplay(False)
			
	def __tryToDisplay(self, retryUntilTimeout = True):
		try:
			self.display(retryUntilTimeout)
		except IOError, e:
			print "Could not display ROI on ", self.getName(), " as: ", `e`
	
	def display(self, retryUntilTimeout = True):
		if self.panel_name == None and self.panel_name_rcp == None:
			raise Exception("No panel_name or panel_name_rcp set in %s. " +
				"Set this or set %s.display_image=False" % (self.name, self.name))	
		
		if self.panel_name:
			#print "Plotter.plotImage(%r, ...)" % self.panel_name
			Plotter.plotImage(self.panel_name, self.renderer.
				renderShapesOntoDataset(self.getDataset(retryUntilTimeout)))
		if self.panel_name_rcp:
			#print "RCPPlotter.imagePlot(%r, ...)" % self.panel_name_rcp
			SDAPlotter.imagePlot(self.panel_name_rcp, self.renderer.
				renderShapesOntoDataset(self.getDataset(retryUntilTimeout)))


class HardwareTriggerableProcessingDetectorWrapper(ProcessingDetectorWrapper, HardwareTriggerableDetector):
	
	def __init__(self, name,
				detector,
				processors=[],
				panel_name=None,
				toreplace=None,
				replacement=None,
				iFileLoader=None,
				root_datadir=None,
				fileLoadTimout=None,
				printNfsTimes=False,
				returnPathAsImageNumberOnly=False,
				panel_name_rcp=None,
				return_performance_metrics=False):
		ProcessingDetectorWrapper.__init__(self, name, detector, processors, panel_name, toreplace, replacement, iFileLoader,
										 root_datadir, fileLoadTimout, printNfsTimes, returnPathAsImageNumberOnly, panel_name_rcp, return_performance_metrics)
		self.inputNames = []
	
	def _setDetector(self, det):
		if not isinstance(det, HardwareTriggerableDetector):
			raise TypeError("expected detector to implement HardwareTriggerableDetector")
		self.det = det
	
	# Scannable
	
	def getExtraNames(self):
		return ['t'] + ProcessingDetectorWrapper.getExtraNames(self)
	
	def asynchronousMoveTo(self, t):
		raise Exception("This detector cannot be operated outside a scan. Try 'scan x 1 1 1 det <t_exp>'")

	def __call__(self, *args):
		if not len(args):
			return self.getPosition()
		collection_time = args[0]
		self.det.setCollectionTime(collection_time)
		self.det.prepareForCollection()
		self.det.collectData()
		self.det.waitWhileBusy()
		return self.getPosition()
	
	# HardwareTriggerableDetector
	
	def getHardwareTriggerProvider(self):
		return self.det.getHardwareTriggerProvider()

	def setHardwareTriggering(self, b):
		self.det.setHardwareTriggering(b)
								
	def isHardwareTriggering(self):
		return self.det.isHardwareTriggering()

	def integratesBetweenPoints(self):
		return self.det.integratesBetweenPoints()

	def arm(self):
		self.det.arm()
		
	# Detector
		
	def setCollectionTime(self, t):
		self.det.setCollectionTime(t)

	def prepareForCollection(self):
		self.det.prepareForCollection()
	
	def getCollectionTime(self):
		return self.det.getCollectionTime()

	def collectData(self):
		self.clearLastAcquisitionState()
		if not self.isHardwareTriggering():
			self.det.collectData()
		
	def getStatus(self):
		return self.det.getStatus()

	def readout(self):
		return self.getPositionCallable().call()
		
	def endCollection(self):
		self.det.endCollection()

	def getDataDimensions(self):
		return [len(self.getExtraNames())]

	def createsOwnFiles(self):
		return self.det.createsOwnFiles()

	def getDescription(self):
		return self.det.getDescription()

	def getDetectorID(self):
		return self.det.getDetectorID()

	def getDetectorType(self):
		return self.det.getDetectorType()
