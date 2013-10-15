from java.lang import InterruptedException, System
from BeamlineParameters import JythonNameSpaceMapping
from scan import Scan
from gda.configuration.properties import LocalProperties
from gda.data.scan.datawriter import XasAsciiDataWriter, NexusExtraMetadataDataWriter, DefaultDataWriterFactory, ConfigurableAsciiFormat, NexusDataWriter, XasAsciiNexusDataWriter
from gda.device.scannable import XasScannable, XasScannableWithDetectorFramesSetup, JEPScannable
from gda.exafs.scan import ExafsScanPointCreator, XanesScanPointCreator, ScanStartedMessage
from gda.exafs.scan import RepetitionsProperties
from gda.factory import Finder
from gda.jython import ScriptBase
from gda.jython.scriptcontroller.event import ScanCreationEvent, ScanFinishEvent, ScriptProgressEvent
from gda.jython.scriptcontroller.logging import XasProgressUpdater, LoggingScriptController, XasLoggingMessage
from gda.scan import ScanBase, ConcurrentScan
from uk.ac.gda.beans.exafs import XasScanParameters, XanesScanParameters, XesScanParameters

class XasScan(Scan):

	def __init__(self,detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers, configXspressDeadtime=False, moveMonoToStartBeforeScan=False, useItterator=False, handleGapConverter=False):
		Scan.__init__(self, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver, XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers)
		self.moveMonoToStartBeforeScan=moveMonoToStartBeforeScan
		self.useItterator=useItterator
		self.handleGapConverter=handleGapConverter

	def __call__(self, sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions= 1, validation=True):
		experimentFullPath, experimentFolderName = self.determineExperimentPath(experimentFullPath)
		ScanBase.interrupted = False
		ScriptBase.interrupted = False
		ScriptBase.paused = False
		controller = self.ExafsScriptObserver
		sampleBean, scanBean, detectorBean, outputBean = self._createBeans(experimentFullPath, sampleFileName, scanFileName, detectorFileName, outputFileName)
		# create unique ID for this scan (all repetitions will share the same ID)
		scriptType = "Exafs"
		if isinstance(scanBean, XanesScanParameters):
			scriptType = "Xanes"
		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
		self.log("Starting",scriptType,detectorBean.getExperimentType(),"scan over scannable '"+self.energy_scannable.getName()+"'...")
		# give the beans to the xasdatawriter class to help define the folders/filenames 
		beanGroup = self._createBeanGroup(experimentFolderName, validation, controller, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean)
		self._doLooping(beanGroup,scriptType,scan_unique_id, numRepetitions, experimentFullPath, experimentFolderName, controller, sampleBean, scanBean, detectorBean, outputBean)
	
	# reset the properties used to control repetition behaviour
	def setQueuePropertiesStart(self, numRepetitions):
		LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
		LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
		LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY,str(numRepetitions))
	
	def setQueuePropertiesEnd(self):
		LocalProperties.set("gda.scan.useScanPlotSettings", "false")
		LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false")
	
	def printRepetition(self, numRepetitions, repetitionNumber, scriptType):
		if numRepetitions > 1:
			self.log("Starting repetition", str(repetitionNumber),"of",numRepetitions)
		else:
			self.log("Starting "+scriptType+" scan...")
			
	def calcInitialPercent(self, numRepetitions, repetitionNumber):
		return str(int((float(repetitionNumber - 1) / float(numRepetitions)) * 100)) + "%"
	
	def calcTimeSinceRepetitionsStarted(self, timeRepetitionsStarted):
		return System.currentTimeMillis() - timeRepetitionsStarted
	
	def getLogMessage(self, numRepetitions, repetitionNumber, timeRepetitionsStarted, scan_unique_id, scriptType, scanBean, experimentFolderName):
		initialPercent = self.calcInitialPercent(numRepetitions, repetitionNumber)
		timeSinceRepetitionsStarted = self.calcTimeSinceRepetitionsStarted(timeRepetitionsStarted)
		return XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(1), str(1),initialPercent,str(0),str(timeSinceRepetitionsStarted),scanBean,experimentFolderName)
		
	def handleScanInterrupt(self, numRepetitions, repetitionNumber):
		ScanBase.interrupted = False
		if LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true":
			LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY,"false")
			# check if a panic stop has been issued, so the whole script should stop
			if ScriptBase.isInterrupted():
				ScriptBase.interrupted = False
				raise e
			# only wanted to skip this repetition, so absorb the exception and continue the loop
			if numRepetitions > 1:
				self.log("Repetition", str(repetitionNumber),"skipped.")
		else:
			print e
			raise # any other exception we are not expecting so raise whatever this is to abort the script
						
	def _doItterator(self, iterator, numRepetitions, beanGroup,scriptType,scan_unique_id, experimentFullPath, controller,timeRepetitionsStarted, sampleBean, scanBean, detectorBean, outputBean, repetitionNumber, experimentFolderName):
		iterator.resetIterator()
		num_sample_repeats = int(iterator.getNumberOfRepeats())
		total_repeats = num_sample_repeats * numRepetitions
		for i in range(num_sample_repeats):
			iterator.moveToNext()
			sampleName = iterator.getNextSampleName()
			descriptions = iterator.getNextSampleDescriptions()
			this_repeat = ((repetitionNumber -1 ) * num_sample_repeats) + (i + 1)
			initialPercent = self.calcInitialPercent(total_repeats, this_repeat)
# 			print "initialPercent",str(initialPercent),"% of repeat",str(i+1),"of repetition",str(repetitionNumber)
			timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted
			logmsg = XasLoggingMessage(scan_unique_id, scriptType, "Starting "+scriptType+" scan...", str(repetitionNumber), str(numRepetitions), str(i+1), str(num_sample_repeats), initialPercent,str(0),str(timeSinceRepetitionsStarted),beanGroup.getScan(),experimentFolderName)
			if num_sample_repeats == 1:
				self.printRepetition(numRepetitions, repetitionNumber, scriptType)
			# the iterator has already printed a message if num_sample_repeats > 1
			self._doScan(beanGroup,scriptType,scan_unique_id, experimentFullPath, controller,timeRepetitionsStarted, sampleBean, scanBean, detectorBean, outputBean, numRepetitions, repetitionNumber, experimentFolderName,sampleName,descriptions,logmsg)
	
	def _doLooping(self,beanGroup,scriptType,scan_unique_id, numRepetitions, experimentFullPath, experimentFolderName, controller, sampleBean, scanBean, detectorBean, outputBean):
		"""
		This is the basic looping based on the number of repetitions set in the UI.
		
		Beamlines should override this method if extra looping logic is required e.g. from sample environment settings
		"""
		ScriptBase.checkForPauses()
		if self.moveMonoToStartBeforeScan==True:
			self.moveMonoToInitialPosition(scanBean) # I20 always moves things back to initial positions after each scan. To save time, move mono to initial position here
			self.energy_scannable.waitWhileBusy()
			
		self.setQueuePropertiesStart(numRepetitions)
		repetitionNumber = 0
		timeRepetitionsStarted = System.currentTimeMillis();
		try:
			while True:
				repetitionNumber+= 1
				# TODO does this need to be inside the _doIterator loop?
				self._beforeEachRepetition(beanGroup,scriptType,scan_unique_id, numRepetitions, controller,repetitionNumber)
				if self.handleGapConverter==True:
					self.setupHarmonic()
				try:
					if self.useItterator==True:
						iterator = self.samplePreparer.createIterator(sampleBean,beanGroup.getDetector().getExperimentType())
						self._doItterator(iterator, numRepetitions, beanGroup,scriptType,scan_unique_id, experimentFullPath, controller,timeRepetitionsStarted, sampleBean, scanBean, detectorBean, outputBean, repetitionNumber, experimentFolderName)
					else:
						# resolve these two values here as they will vary when using iterators
						sampleName = sampleBean.getName()
						descriptions = sampleBean.getDescriptions()
						self.printRepetition(numRepetitions, repetitionNumber, scriptType)
						logmsg = self.getLogMessage(numRepetitions, repetitionNumber, timeRepetitionsStarted, scan_unique_id, scriptType, scanBean, experimentFolderName)
						self._doScan(beanGroup,scriptType,scan_unique_id, experimentFullPath, controller,timeRepetitionsStarted, sampleBean, scanBean, detectorBean, outputBean, numRepetitions, repetitionNumber, experimentFolderName,sampleName,descriptions,logmsg)
				except InterruptedException, e:
					self.handleScanInterrupt(numRepetitions, repetitionNumber)
				self._runScript(beanGroup.getOutput().getAfterScriptName())# run the after scan script
				self.checkForPause(numRepetitions, repetitionNumber)
				finished=self.checkIfRepetitionsFinished(numRepetitions, repetitionNumber)
				if finished is True:
					break
				numRepetitions = self.numRepsFromProperty
		finally:
			if self.moveMonoToStartBeforeScan==True:
				self.energy_scannable.stop()
			if self.handleGapConverter==True:
				gap_converter.enableAutoConversion()
			# repetition loop completed, so reset things
			self.setQueuePropertiesEnd()
			self._resetHeader()
			self.detectorPreparer.completeCollection()
			ScriptBase.checkForPauses()
			
	# Runs a single XAS/XANES scan.
	def _doScan(self,beanGroup,scriptType,scan_unique_id, experimentFullPath, controller,timeRepetitionsStarted, sampleBean, scanBean, detectorBean, outputBean, numRepetitions, repetitionNumber, experimentFolderName,sampleName,descriptions,logmsg):
		#self.loggingcontroller.update(None,logmsg)
		self.XASLoggingScriptController.update(None,ScanStartedMessage(scanBean,detectorBean)) # informs parts of the UI about current scan
		# run the before scan script
		self._runScript(outputBean.getBeforeScriptName())
		# work out which detectors to use (they will need to have been configured already by the GUI)
		detectorList = self._getDetectors(detectorBean, scanBean) 
		# work out extra scannables to include
		sampleScannables, outputScannables, scanPlotSettings = self.runPreparers(beanGroup, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean)
		signalParameters = self._getSignalList(outputBean)
		loggingbean = XasProgressUpdater(self.XASLoggingScriptController,logmsg,timeRepetitionsStarted)
		# build the scan command arguments
		args = self.buildScanArguments(scanBean, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean)
		# run the scan
		controller.update(None, ScriptProgressEvent("Running scan"))
		ScanBase.interrupted = False
		thisscan = self.createScan(args, scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetitionNumber,experimentFolderName, experimentFullPath)
		controller.update(None, ScanCreationEvent(thisscan.getName()))
		if (scanPlotSettings != None):
			self.log("Setting the filter for columns to plot...")
			thisscan.setScanPlotSettings(scanPlotSettings)
		thisscan.runScan()
		#update observers
		controller.update(None, ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));

	def _createAndconfigureXASScannable(self):
		xas_scannable = XasScannable()
		xas_scannable.setName("xas_scannable")
		# to be consistent with the rest of this object, no longer use the scannable defined in the bean but 
		# use the energy scannable injected in the constructor
		xas_scannable.setEnergyScannable(self.energy_scannable)
		return xas_scannable

	def createScan(self, args, scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath):
		thisscan = ConcurrentScan(args)
		thisscan = self._setUpDataWriter(thisscan, scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath)
		thisscan.setReturnScannablesToOrginalPositions(False)
		return thisscan
	
	# run the beamline specific preparers			
	def runPreparers(self, beanGroup, experimentFullPath, sampleBean, scanBean, detectorBean, outputBean):
		self.detectorPreparer.prepare(scanBean, detectorBean, outputBean, experimentFullPath)
		sampleScannables = self.samplePreparer.prepare(sampleBean)
		outputScannables = self.outputPreparer.prepare(outputBean, scanBean)
		scanPlotSettings = self.outputPreparer.getPlotSettings(detectorBean,outputBean)
		return sampleScannables, outputScannables, scanPlotSettings

	def buildScanArguments(self):
		xas_scannable = self._createAndconfigureXASScannable()
		xas_scannable.setDetectors(detectorList)
		args = [xas_scannable, self.resolveEnergiesFromScanBean(scanBean)]
		args = self.addScanArguments(args, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean)
		return args

	def addScannableArgs(self, args, sampleScannables, outputScannables, detectorList, signalParameters, loggingbean):
		if sampleScannables != None:
			args += sampleScannables
		if outputScannables != None:
			args += outputScannables
		args += detectorList
		args += signalParameters
		args += [loggingbean]
		return args

	def resolveEnergiesFromScanBean(self, scanBean):
		if isinstance(scanBean, XanesScanParameters):
			return XanesScanPointCreator.calculateEnergies(scanBean)
		else:
			return ExafsScanPointCreator.calculateEnergies(scanBean)

	def _beforeEachRepetition(self,beanGroup,scriptType,scan_unique_id, numRepetitions, controller, repetitionNumber):
		beanGroup.setScanNumber(repetitionNumber)
		times = []
		if isinstance(beanGroup.getScan(),XasScanParameters):
			times = ExafsScanPointCreator.getScanTimeArray(beanGroup.getScan())
		elif isinstance(beanGroup.getScan(),XanesScanParameters):
			times = XanesScanPointCreator.getScanTimeArray(beanGroup.getScan())
		if len(times) > 0:
			self.ionchambers.setTimes(times)
			self.log("Setting detector frame times, using array of length",str(len(times)) + "...")
			ScriptBase.checkForPauses()
		return

	def _getSignalList(self, outputParameters):
		signalList = []
		for signal in outputParameters.getSignalList():
			 scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(), signal.getDataFormat(), signal.getName(), signal.getExpression())
			 signalList.append(scannable)
		return signalList
			
	def _getDetectors(self, detectorBean, scanBean):
		expt_type = detectorBean.getExperimentType()
		detectorList = []
		if expt_type == "Transmission":
			self.log("This is a transmission scan")
			for group in detectorBean.getDetectorGroups():
				if group.getName() == detectorBean.getTransmissionParameters().getDetectorType():
					return self._createDetArray(group.getDetector(), scanBean)
		elif expt_type == "XES":
			for group in detectorBean.getDetectorGroups():
				if group.getName() == "XES":
					return self._createDetArray(group.getDetector(), scanBean)
		else:
			self.log("This is a fluoresence scan")
			for group in detectorBean.getDetectorGroups():
				if group.getName() == detectorBean.getFluorescenceParameters().getDetectorType():
					#print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
					return self._createDetArray(group.getDetector(), scanBean)

	"""
	Get the relevant datawriter config, create a datawriter and if it of the correct type then give it the config.
	Give the datawriter to the scan.
	"""
	def _setUpDataWriter(self,thisscan,scanBean,detectorBean,sampleBean,outputBean,sampleName,descriptions,repetition,experimentFolderName,experimentFullPath):
		nexusSubFolder = experimentFolderName +"/" + outputBean.getNexusDirectory()
		asciiSubFolder = experimentFolderName +"/" + outputBean.getAsciiDirectory()
		nexusFileNameTemplate = nexusSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".nxs"
		asciiFileNameTemplate = asciiSubFolder +"/"+ sampleName+"_%d_"+str(repetition)+".dat"
		if LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX):
			nexusFileNameTemplate = nexusSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".nxs"
			asciiFileNameTemplate = asciiSubFolder +"/%d_"+ sampleName+"_"+str(repetition)+".dat"
		dataWriter = XasAsciiNexusDataWriter()
		dataWriter.setRunFromExperimentDefinition(True);
		dataWriter.setScanBean(scanBean);
		dataWriter.setDetectorBean(detectorBean);
		dataWriter.setSampleBean(sampleBean);
		dataWriter.setOutputBean(outputBean);
		dataWriter.setSampleName(sampleName);
		dataWriter.setXmlFolderName(experimentFullPath)
		dataWriter.setXmlFileName(self._determineDetectorFilename(detectorBean))
		dataWriter.setDescriptions(descriptions);
		dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
		dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);
		# get the ascii file format configuration (if not set here then will get it from the Finder inside the Java class
		asciidatawriterconfig = self.outputPreparer.getAsciiDataWriterConfig(scanBean)
		if asciidatawriterconfig != None :
			dataWriter.setConfiguration(asciidatawriterconfig)
		thisscan.setDataWriter(dataWriter)
		return thisscan
	
	def _determineDetectorFilename(self,detectorBean):
		xmlFileName = None
		if detectorBean.getExperimentType() == "Fluorescence" :
			fluoresenceParameters = detectorBean.getFluorescenceParameters()
			xmlFileName = fluoresenceParameters.getConfigFileName()
		elif detectorBean.getExperimentType() == "XES" :
			fluoresenceParameters = detectorBean.getXesParameters()
			xmlFileName = fluoresenceParameters.getConfigFileName()
		return xmlFileName
	
	# Move to start energy so that harmonic can be set by gap_converter for i18 only
	def setupHarmonic(self, scanBean, gap_converter):
		initialEnergy = scanBean.getInitialEnergy()
		print "moving ", self.energy_scannable.getName(), " to start energy ", initialEnergy
		self.energyScannable(initialEnergy)
		print "move complete, diasabling harmonic change"
		if gap_converter != None:
			gap_converter.disableAutoConversion()

	#check if halt after current repetition set to true
	def checkForPause(self, numRepetitions,repetitionNumber):
		if numRepetitions > 1 and LocalProperties.get(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY) == "true":
			self.log("Paused scan after repetition",str(repetitionNumber),". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.")
			self.commandQueueProcessor.pause(500);
			LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY,"false")
			ScriptBase.checkForPauses()
		
	#check if the number of repetitions has been altered and we should now end the loop
	def checkIfRepetitionsFinished(self, numRepetitions, repetitionNumber):
		self.numRepsFromProperty = int(LocalProperties.get(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY))
		if self.numRepsFromProperty != numRepetitions and self.numRepsFromProperty <= (repetitionNumber):
			self.log("The number of repetitions has been reset to",str(self.numRepsFromProperty), ". As",str(repetitionNumber),"repetitions have been completed this scan will now end.")
			return True
		elif self.numRepsFromProperty <= (repetitionNumber):
			return True# normal end to loop
	
	def moveMonoToInitialPosition(self, scanBean):
		if self.energy_scannable != None and isinstance(scanBean,XasScanParameters):
			initialPosition = scanBean.getInitialEnergy()
		elif self.energy_scannable != None and isinstance(scanBean,XanesScanParameters):
			initialPosition = scanBean.getRegions().get(0).getEnergy()
		elif self.energy_scannable != None and isinstance(scanBean,XesScanParameters):
			xes_scanType = scanBean.getScanType()
			if xes_scanType == XesScanParameters.SCAN_XES_FIXED_MONO:
				initialPosition = scanBean.getMonoEnergy()
			else:
				initialPosition = scanBean.getMonoInitialEnergy()
		self.energy_scannable.waitWhileBusy()
		self.energy_scannable.asynchronousMoveTo(initialPosition)
		self.log("Moving mono to initial position...")
