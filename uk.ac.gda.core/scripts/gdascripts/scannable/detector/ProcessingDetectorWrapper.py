from uk.ac.diamond.scisoft.analysis import SDAPlotter
from uk.ac.diamond.scisoft.analysis.plotserver import GuiParameters
from org.eclipse.dawnsci.analysis.dataset.roi import RectangularROI, RectangularROIList
from org.eclipse.january.dataset import DatasetFactory
from org.slf4j import LoggerFactory
from gda.device import DetectorSnapper, DeviceException

try:
	from gda.analysis import Plotter
except ImportError:
	Plotter = None

from java.time import Instant #@UnresolvedImport

from gda.data.fileregistrar import IFileRegistrar
from gda.device.Detector import BUSY
from gda.device.scannable import ScannableMotionBase
from gda.device.scannable import PositionCallableProvider
from gda.factory import Finder
from gda.util.logging.LoggingUtils import logSince
from gda.util.logging.LoggingUtils import logStackTrace
from gdascripts.scannable.detector.DatasetShapeRenderer import DatasetShapeRenderer
from java.util.concurrent import Callable #@UnresolvedImport
import time
import os
import re

from gdascripts.analysis.io.DatasetProvider import LazyDataSetProvider, BasicDataSetProvider
import gda.device.detector.NXDetectorData
import gda.device.detector.NXDetectorDataWithFilepathForSrs
from gda.device.detector.hardwaretriggerable import HardwareTriggerableDetector,\
	HardwareTriggeredDetector

import scisoftpy as dnp
import sys, traceback


ROOT_NAMESPACE_DICT = None
"""If set a variable SRSWriteAtFileCreation will be appended with a file path template of the form e.g.:
   pilatus100k_path_template='123-pilatus100k/%5i.cbf."""

def _appendStringToSRSFileHeader(self, s):
	"""Not thread safe"""
	h = ROOT_NAMESPACE_DICT.get('SRSWriteAtFileCreation', '')
	if h in (None,''):
		h='\n'
	ROOT_NAMESPACE_DICT['SRSWriteAtFileCreation'] = h + s

def _displayProcessingDetectorWrapper(logger, panel_name, panel_name_rcp, renderer, dataset):
	if panel_name_rcp:
		try:
			rois={} # Get the rois we want to update
			for shape_key, shape_value in renderer.shapesToPaint.iteritems():
				shape = shape_value.get('roi')
				if (shape):
					rois[shape_key.name]=shape

			roilist = dnp.plot.getrois(name=panel_name_rcp) or dnp.plot.roi_list()

			if roilist:
				logger.debug("{} already has rois, merging processing detector wrapper rois into {}", panel_name_rcp, roilist)
			else:
				logger.debug("{} has no rois, adding processing detector wrapper rois", panel_name_rcp)

			for name, shape in rois.iteritems():
				roi = RectangularROI([shape.x1, shape.y1],[shape.x2, shape.y2])
				rectroi = dnp.plot.roi.rectangle(name=name, point=roi.getPoint(), lengths=roi.getLengths(), plot=True)
				rectroi.fixed = True
				roilist.append(rectroi) # append updates if the named roi already exists

			dnp.plot.delroi(name=panel_name_rcp)  # Removing the rois before plotting the image data, can prevent a rare problem
			dnp.plot.delrois(name=panel_name_rcp) # where some rois get locked as not visible and/or not fixed

			logger.debug("Plotting dataset on {}", panel_name_rcp)
			dnp.plot.image(dataset, name=panel_name_rcp)

			logger.debug("Setting ROIs for {} to {}", panel_name_rcp, roilist) 
			# Ensure setrois() is given a bean, if not, it will accept a roilist in place of a bean, but then fail in subtle and
			# inobvious ways later
			bean=dnp.plot.setrois(bean=dnp.plot.getbean(name=panel_name_rcp), roilist=roilist)
			# Avoid send=True in setrois(), so we can suppress warnings by calling setbean() instead, otherwise we would get
			# warnings every time we updated any roi
			dnp.plot.setbean(bean=bean, name=panel_name_rcp, warn=False)

		except:
			logger.error("Exception applying ROIs to {}, fall back to rendering ROIs onto dataset values: {}", panel_name_rcp,
				''.join(traceback.format_exception(*sys.exc_info())) )
			rois=None

		if rois==None:
			logger.debug("SDAPlotter.imagePlot({}, ...renderShapesOntoDataset...)", panel_name_rcp)
			SDAPlotter.imagePlot(panel_name_rcp, renderer.renderShapesOntoDataset(dataset))
	if panel_name:
		logger.debug("Plotter.plotImage({}, ...renderShapesOntoDataset...)", panel_name)
		Plotter.plotImage(panel_name, renderer.renderShapesOntoDataset(dataset))


class FileRegistrar(object):

	def __init__(self):
		resistrarDict = Finder.getFindablesOfType(IFileRegistrar)
		self.registrars = resistrarDict.values()

	def registerFile(self, filepath):
		for registrar in self.registrars:
			registrar.registerFile(filepath)


class ProcessingDetectorWrapperPositionCallable(Callable):

	def __init__(self, collectionTime, filepathToReport, processors, pathToRegister,
				panel_name, panel_name_rcp, datasetProvider, renderer, fileRegistrar,
				return_performance_metrics, name="<undefined>"):
		self.collectionTime = collectionTime
		self.filepathToReport = filepathToReport
		self.processors = processors
		self.pathToRegister = pathToRegister
		self.panel_name = panel_name
		self.panel_name_rcp = panel_name_rcp
		self.datasetProvider = datasetProvider
		self.renderer = renderer
		self.fileRegistrar = fileRegistrar
		self.return_performance_metrics = return_performance_metrics

		if return_performance_metrics:
			self.time_created = time.time()

		self.logger = LoggerFactory.getLogger("ProcessingDetectorWrapperPositionCallable:%s" % name)
		logStackTrace(self.logger, "__init__() completed, returning...")

	def call(self):
		logStackTrace(self.logger, "call()")

		# 1. Register file
		if self.pathToRegister:
			self.fileRegistrar.registerFile(self.pathToRegister)

		# 2. generate position
		position = [self.collectionTime]
		if self.filepathToReport:
			position.append(self.filepathToReport)

		processorResults = []
		for processor in self.processors:
			processorResults += list(processor.getPosition(self.datasetProvider.getDataset()))
		# 3. display image
		_displayProcessingDetectorWrapper(self.logger,
			self.panel_name, self.panel_name_rcp, self.renderer, self.datasetProvider.getDataset())

		metricsResults = [time.time()-self.time_created] if self.return_performance_metrics else []
		return position + metricsResults + processorResults


class ProcessingDetectorWrapper(ScannableMotionBase, PositionCallableProvider):
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

		self.logger = LoggerFactory.getLogger("ProcessingDetectorWrapper:%s" % name)

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

		self.include_path_in_output = True

	def _setDetector(self, det):
		self._det = det

	@property
	def det(self):
		return self._det

	def getExtraNames(self):

		extraNames = []
		if self.include_path_in_output:
			extraNames.append('path')

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

		_format = ['%f']
		if self.include_path_in_output:
			_format.append(self.__getFilePathFormat())

		return _format + metricsFormats + processorFormats

	def __getFilePathFormat(self):
		return '%i' if self.returnPathAsImageNumberOnly else '%s'

	def atScanStart(self):
		try:
			if self.det.tifwriter.isWaitForFileArrival():
				self.logger.warn("ProcessingDetectorWrapper is slower if waitForFileArrival on tifwriter is True")
				print "Warning: ProcessingDetectorWrapper is slower if waitForFileArrival on tifwriter is True"
		except AttributeError:
			pass
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
		self.logger.trace("getPosition() _operatingInScan={} _preparedForScan={}",
			self._operatingInScan, self._preparedForScan)
		start_time = Instant.now()
		result=self.getPositionCallable().call()
		logSince(self.logger, "getPosition() took", start_time)
		self.logger.trace("getPosition() returning {}", result)
		return result

	def atScanEnd(self):
		self._operatingInScan = False
		self._preparedForScan = False
		self.logger.trace("Saved last file to: {} ", self.getFilepath())
		#print self.name + " %s saved last file to: %s" % (self.name, self.getFilepath())
		self.det.atScanEnd()

	def stop(self):
		self._operatingInScan = False
		self._preparedForScan = False
		self.det.stop()

	def atCommandFailure(self):
		self._operatingInScan = False
		self._preparedForScan = False
		self.det.atCommandFailure()

	def getPositionCallable(self):
		logStackTrace(self.logger, "getPositionCallable()")
		pathToRegister = self.getFilepath() if self.det.createsOwnFiles() else None
		processors = self.processors if self.process_image else []
		if self.display_image and self.panel_name == None and self.panel_name_rcp == None:
			raise Exception("No panel_name or panel_name_rcp set in %s. Set this or set %s.display_image=False"
						 % (self.name, self.name))
		panel_name = self.panel_name if self.display_image else None
		panel_name_rcp = self.panel_name_rcp if self.display_image else None

		if self.include_path_in_output:
			filepathToReport = self.__getFilePathRepresentation()
		else:
			filepathToReport = None

		return ProcessingDetectorWrapperPositionCallable(
			self.det.getCollectionTime(), filepathToReport,
			processors, pathToRegister, panel_name, panel_name_rcp,
			self.getDatasetProvider(), self.renderer, self.fileRegistrar,
			self.return_performance_metrics, self.name)

	def getFilepath(self):
		try:
			# assume detector has read out an NXDetectorDataWithFilepathForSrs object
			path_from_detector = self._readout().getFilepath()
		except AttributeError:
			path_from_detector = self._readout()
		return self.replacePartOfPath(path_from_detector)


	def _readout(self):
		if self.cached_readout is None:
			self.cached_readout = self.det.readout()
		return self.cached_readout

	def __getFilePathRepresentation(self):
		if self.returnPathAsImageNumberOnly:
			path = self.getFilepath()
			try:
				fname = os.path.splitext(os.path.basename(path))[0] # filename only
				return int(re.findall("\d+$", fname)[0])
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
			self._configureNewDatasetProvider()
		return self.datasetProvider

	def _configureNewDatasetProvider(self, wait_for_exposure_callable=None):

		def createDatasetProvider(path):
			if path == '':
				raise IOError("Could no load dataset: %s does not have a record of the last file saved" % self.name)
			path = self.replacePartOfPath(path)
			if path[0] != '/':
				#if relative path then we have to assume it's from the data directory
				path = gda.jython.InterfaceProvider.getPathConstructor().createFromDefaultProperty() + "/" + path
			self.datasetProvider = LazyDataSetProvider(path, self.iFileLoader, self.fileLoadTimout, self.printNfsTimes, wait_for_exposure_callable)
			self.logger.debug("datasetProvider is {}", self.datasetProvider)

		if self.det.createsOwnFiles():
			path = self.getFilepath()
			createDatasetProvider(path)
		else:
#			if not isinstance(dataset, DataSet, gda.device.detector.NXDetectorData):
#				raise Exception("If a detector does not write its own files, ProcessingDetectorWrapper %s only works with detectors that readout DataSets.")
			dataset = self._readout()
			if isinstance(dataset, gda.device.detector.NXDetectorDataWithFilepathForSrs):
				path = dataset.getFilepath()
				self.logger.debug("dataset is NXDetectorDataWithFilepathForSrs: {}", dataset)
				createDatasetProvider(path)
				return
			elif isinstance(dataset, gda.device.detector.NXDetectorData):
				data = dataset.getNexusTree().getChildNode(1).getChildNode(1).getData()
				dataset = DatasetFactory.createFromObject(data.getBuffer())
				dataset.setShape(data.dimensions)
				dataset.squeeze()
				self.logger.debug("dataset is NXDetectorData: {}", dataset)
			else:
				self.logger.debug("dataset is neither: {}", dataset)
			self.datasetProvider = BasicDataSetProvider(dataset)
			self.logger.debug("datasetProvider is {}", self.datasetProvider)

	def replacePartOfPath(self, path):
		if self.toreplace != None and self.replacement != None:
			path = str(path)
			path = path.replace(self.toreplace, self.replacement)
		#replace the double \\ resulting from windows mounts to /
		path = str(path)
		return path.replace('\\', '/')

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
			self.logger.error("Could not display ROI", e)
			print "Could not display ROI on ", self.getName(), " as: ", `e`
		except DeviceException, e: # NXDetector will throw these if readout when not ready (catches too much)
			self.logger.error("Could not display ROI", e)
			print "Could not display ROI on ", self.getName(), " as: ", `e`

	def display(self, retryUntilTimeout = True):
		self.logger.debug("display({})", retryUntilTimeout)
		if self.panel_name == None and self.panel_name_rcp == None:
			raise Exception("No panel_name or panel_name_rcp set in %s. " +
				"Set this or set %s.display_image=False" % (self.name, self.name))
		_displayProcessingDetectorWrapper(self.logger,
			self.panel_name, self.panel_name_rcp, self.renderer, self.getDataset(retryUntilTimeout))


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
		self._det = det

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


# Note should extend HardwareTriggerableProcessingDetectorWrapper, but Jython 2.5.1 fails for some reason
class SwitchableHardwareTriggerableProcessingDetectorWrapper(ProcessingDetectorWrapper, HardwareTriggerableDetector, DetectorSnapper):

	def __init__(self, name,
				detector,
				hardware_triggered_detector,
				detector_for_snaps,
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
				return_performance_metrics=False,
				array_monitor_for_hardware_triggering=None):

		ProcessingDetectorWrapper.__init__(self, name, detector, processors, panel_name, toreplace, replacement, iFileLoader,
										 root_datadir, fileLoadTimout, printNfsTimes, returnPathAsImageNumberOnly, panel_name_rcp, return_performance_metrics)
		self.inputNames = []
		if hardware_triggered_detector is not None:
			if not isinstance(hardware_triggered_detector, HardwareTriggeredDetector):
				raise TypeError("expected hardware_triggered_detector to implement HardwareTriggeredDetector")
		self.hardware_triggered_detector = hardware_triggered_detector
		self.hardware_triggering = False
		self.detector_for_snaps = detector_for_snaps
		self.array_monitor_for_hardware_triggering = array_monitor_for_hardware_triggering
		self._seconds_to_wait_after_count_reached = 0
		self.scanned_det = None

	def _setDetector(self, det):
		self.detector = det

	@property
	def det(self):
		#AbstractContinuousScanLine sets to non-hardware triggering before endCollection/etc are called
		if self.scanned_det == None:
			return self.hardware_triggered_detector if self.isHardwareTriggering() else self.detector
		return self.scanned_det

#		return {'pilatus100k_path_template='123-pilatus100k/%5i.cbf.'}

	# Scannable

	def getExtraNames(self):
		return ['t'] + ProcessingDetectorWrapper.getExtraNames(self)

	def asynchronousMoveTo(self, t):
		raise Exception("This detector cannot be operated outside a scan. Try 'scan x 1 1 1 det <t_exp>'")

	def __call__(self, *args):
		if not len(args):
			return self.getPosition()
		collection_time = args[0]
		self.setCollectionTime(collection_time)
		self.acquire()
		return self.getPosition()

	# HardwareTriggerableDetector

	def getHardwareTriggerProvider(self):
		return self.hardware_triggered_detector.getHardwareTriggerProvider()

	def setHardwareTriggering(self, b):
		self.hardware_triggering = b

	def setNumberImagesToCollect(self, n):
		self.hardware_triggered_detector.setNumberImagesToCollect(n)

	def getNumberImagesToCollect(self):
		return self.hardware_triggered_detector.getNumberImagesToCollect()

	def isHardwareTriggering(self):
		return self.hardware_triggering

	def integratesBetweenPoints(self):
		return self.hardware_triggered_detector.integratesBetweenPoints()

	# Detector

	def setCollectionTime(self, t):
		if self.det is not None:
			self.det.setCollectionTime(t)
		if self.hardware_triggered_detector is not None:
			self.hardware_triggered_detector.setCollectionTime(t)
		if self.detector_for_snaps is not None:
			self.detector_for_snaps.setCollectionTime(t)
		self._seconds_to_wait_after_count_reached = t

	def prepareForCollection(self):
		self.scanned_det = None
		self.scanned_det = self.det
		self.scanned_det.prepareForCollection()

	def atScanStart(self):
		ProcessingDetectorWrapper.atScanStart(self)
		if self.array_monitor_for_hardware_triggering:
			self.array_monitor_for_hardware_triggering.prepareForCollection(999, None) # Number not used

	def getCollectionTime(self):
		if self.isHardwareTriggering():
			return self.hardware_triggered_detector.getCollectionTime()
		else:
			return self.det.getCollectionTime()

	def collectData(self):
		self.clearLastAcquisitionState()
		self.det.collectData()

	def getStatus(self):
		return self.det.getStatus()

	def getDatasetProvider(self):
		self.logger.trace("getDatasetProvider() self.isHardwareTriggering() = {}", self.isHardwareTriggering())
		if self.datasetProvider == None:
			if self.array_monitor_for_hardware_triggering and self._operatingInScan and self.isHardwareTriggering():
				wait_for_exposure_callable = self.array_monitor_for_hardware_triggering.read(1)[0]

				class Adapter():

					def __init__(self, c, name, seconds_to_wait_after_count_reached):
						self.c = c
						self.name = name
						self.seconds_to_wait_after_count_reached = seconds_to_wait_after_count_reached

					def call(self):
						self.c.appendTo(None, self.name)
						time.sleep(self.seconds_to_wait_after_count_reached)

				self._configureNewDatasetProvider(Adapter(wait_for_exposure_callable, self.name, self._seconds_to_wait_after_count_reached))
			else:
				self._configureNewDatasetProvider()
		return self.datasetProvider

	def readout(self):
		if self.isHardwareTriggering():
			self.clearLastAcquisitionState()
		return self.getPositionCallable().call()

	def endCollection(self):
		self.det.endCollection()

#	def atScanLineEnd(self):
#		self.det.atScanLineEnd()

	def atScanEnd(self):
		ProcessingDetectorWrapper.atScanEnd(self)
		self.scanned_det = None
#		if self.array_monitor_for_hardware_triggering:
#			self.array_monitor_for_hardware_triggering.prepareForCollection(999, None) # Number not used

	def stop(self):
		ProcessingDetectorWrapper.stop(self)
		self.scanned_det = None

	def atCommandFailure(self):
		ProcessingDetectorWrapper.atCommandFailure(self)
		self.scanned_det = None

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

	def prepareForAcquisition(self, collection_time):
		self.detector_for_snaps.setCollectionTime(collection_time)

	def getPositionCallable(self):
		self.logger.debug("getPositionCallable() self.isHardwareTriggering() = {}", self.isHardwareTriggering())
		if self.isHardwareTriggering():
			self.clearLastAcquisitionState()
			# self.hardware_triggered_detector.lastReadoutValue = None
		return ProcessingDetectorWrapper.getPositionCallable(self)

#	public double getAcquireTime() throws Exception;
#
#	public double getAcquirePeriod() throws Exception;

	def acquire(self):
		self.clearLastAcquisitionState()
		self.detector_for_snaps.atScanStart()
		self.detector_for_snaps.atScanLineStart()
		self.detector_for_snaps.collectData()
		self.detector_for_snaps.waitWhileBusy()
		self.cached_readout = self.detector_for_snaps.readout()
		self.detector_for_snaps.atScanLineEnd()
		self.detector_for_snaps.atScanEnd()
