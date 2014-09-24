package uk.ac.gda.server.exafs.scan;

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.scannable.JEPScannable;
import gda.device.scannable.XasScannable;
import gda.exafs.scan.ExafsScanPointCreator;
import gda.exafs.scan.RepetitionsProperties;
import gda.exafs.scan.ScanStartedMessage;
import gda.exafs.scan.XanesScanPointCreator;
import gda.factory.Finder;
import gda.jython.scriptcontroller.Scriptcontroller;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ConcurrentScan;
import gda.scan.ScanInterruptedException;
import gda.scan.ScanPlotSettings;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.nfunk.jep.ParseException;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.python.core.PyTuple;

import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class XasScan extends ExafsScan {

	// # public ExafsScan(DetectorPreparer detectorPreparer, SampleEnvironmentPreparer samplePreparer,
	// # OutputPreparer outputPreparer, Processor commandQueueProcessor, Scriptcontroller ExafsScriptObserver,
	// # Scriptcontroller XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
	// # ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, Detector ionchambers,
	// # boolean includeSampleNameInNexusName) {

	private boolean moveMonoToStartBeforeScan;
	private boolean useIterator;
	private boolean handleGapConverter;
	private long timeRepetitionsStarted;

	public XasScan(DetectorPreparer detectorPreparer, SampleEnvironmentPreparer samplePreparer,
			OutputPreparer outputPreparer, Processor commandQueueProcessor, Scriptcontroller ExafsScriptObserver,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable,
			TfgScalerWithFrames ionchambers, boolean moveMonoToStartBeforeScan, boolean useItterator,
			boolean handleGapConverter, boolean includeSampleNameInNexusName) {
		super(detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, ExafsScriptObserver,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable, ionchambers,
				includeSampleNameInNexusName);
		this.moveMonoToStartBeforeScan = moveMonoToStartBeforeScan;
		this.useIterator = useItterator;
		this.handleGapConverter = handleGapConverter;
	}

	public PyObject __call__(PyObject pyArgs) throws Exception {

		// List<Object> args = new ArrayList<Object>();
		// int numberArgs = ((PySequence)pyArgs).__len__();

		String sampleFileName = ((PySequence) pyArgs).__finditem__(0).asString();
		String scanFileName = ((PySequence) pyArgs).__finditem__(1).asString();
		String detectorFileName = ((PySequence) pyArgs).__finditem__(2).asString();
		String outputFileName = ((PySequence) pyArgs).__finditem__(3).asString();
		String experimentFullPath = ((PySequence) pyArgs).__finditem__(4).asString();
		numRepetitions = ((PySequence) pyArgs).__finditem__(5).asInt();
		// boolean validation = ((PySequence)pyArgs).__finditem__(6).asInt() != 0;

		determineExperimentPath(experimentFullPath);

		_createBeans(sampleFileName, scanFileName, detectorFileName, outputFileName);
		// runFromBeans(experimentFullPath, numRepetitions, validation);

		scriptType = "Exafs";
		if (scanBean instanceof XanesScanParameters) {
			scriptType = "Xanes";
		}
		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
		_doLooping();
		return new PyInteger(0);
	}

	//
	//
	// public void runFromBeansAndScanXml(ISampleParameters sampleBean, Object scanFileName, IDetectorParameters
	// detectorBean,IOutputParameters outputBean, String experimentFullPath, int numRepetitions, boolean validation)
	// throws Exception{
	// IScanParameters scanBean = (IScanParameters) BeansFactory.getBeanObject(experimentFullPath, scanFileName);
	// runFromBeans(sampleBean, scanBean, detectorBean, outputBean, experimentFullPath, numRepetitions, validation);
	// }
	//
	// public void runFromBeans(String experimentFullPath, int numRepetitions, boolean validation) throws Exception{
	// // # create unique ID for this scan (all repetitions will share the same ID)
	// String scriptType = "Exafs";
	// if (scanBean instanceof XanesScanParameters){
	// scriptType = "Xanes";
	// }
	// String scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
	// // controller = new ExafsScriptObserver();
	// String[] paths /*experimentFullPath, experimentFolderName*/ = determineExperimentPath(experimentFullPath);
	// // TODO nulls should be script controller
	// // _createBeanGroup(paths[1], validation, null, paths[0], sampleBean, scanBean, detectorBean, outputBean);
	// _createBeans(paths[1], sampleBean, scanBean, detectorBean, outputBean);
	// _doLooping(scriptType,scan_unique_id, numRepetitions, experimentFullPath, paths[1], null);
	// }

	// # reset the properties used to control repetition behaviour
	private void setQueuePropertiesStart() {
		LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
		LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
		LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY, Integer.toString(numRepetitions));
	}

	private void setQueuePropertiesEnd() {
		LocalProperties.set("gda.scan.useScanPlotSettings", "false");
		LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false");
	}

	private void printRepetition() {
		if (numRepetitions > 1) {
			// print "";
			log("Starting repetition" + currentRepetition + "of" + numRepetitions);
		} else {
			// print "";
			log("Starting " + scriptType + " scan...");
		}
	}

	private String calcInitialPercent() {
		return ((currentRepetition - 1) / numRepetitions) * 100 + "%";
	}

	private long calcTimeSinceRepetitionsStarted() {
		return System.currentTimeMillis() - timeRepetitionsStarted;
	}

	private XasLoggingMessage getLogMessage(String sampleName) throws Exception {
		String initialPercent = calcInitialPercent();
		long timeSinceRepetitionsStarted = calcTimeSinceRepetitionsStarted();
		return new XasLoggingMessage(_getMyVisitID(), scan_unique_id, scriptType,
				"Starting " + scriptType + " scan...", Integer.toString(currentRepetition),
				Integer.toString(numRepetitions), Integer.toString(1), Integer.toString(1), initialPercent,
				Integer.toString(0), Long.toString(timeSinceRepetitionsStarted), scanBean, experimentFolderName,
				sampleName, 0);
	}

	private void handleScanInterrupt(Exception exceptionObject) throws Exception {
		if (LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true") {
			LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
			// # check if a panic stop has been issued, so the whole script should stop?
			if (Thread.currentThread().isInterrupted()) {
				throw new ScanInterruptedException();
			}
			// # only wanted to skip this repetition, so absorb the exception and continue the loop
			if (numRepetitions > 1) {
				log("Repetition" + currentRepetition + "skipped.");
			} else {
				// print exceptionObject
				throw exceptionObject;// # any other exception we are not expecting so raise whatever this is to abort
										// the script
			}
		}
	}

	private void _doItterator(SampleEnvironmentIterator iterator) throws Exception {
		// iterator.resetIterator(); TODO should we be resetting here?
		int num_sample_repeats = iterator.getNumberOfRepeats();
		int total_repeats = num_sample_repeats * numRepetitions;
		// TODO this may not work, should we loop over num_sample_repeats inside loop over numRepetitions and use
		// iterator.resetIterator()?
		for (int i = 0; i < num_sample_repeats; i++) {
			iterator.next();
			String sampleName = iterator.getNextSampleName();
			List<String> descriptions = iterator.getNextSampleDescriptions();
			int this_repeat = ((-1) * num_sample_repeats) + (i + 1);
			String initialPercent = calcInitialPercent();
			// # print
			// "initialPercent",str(initialPercent),"% of repeat",str(i+1),"of repetition",str(repetitionNumber);
			long timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted;
			// FIXME predicted time?
			XasLoggingMessage logmsg = new XasLoggingMessage(_getMyVisitID(), scan_unique_id, scriptType, "Starting "
					+ scriptType + " scan...", Integer.toString(currentRepetition), Integer.toString(numRepetitions), Integer.toString(i + 1), Integer.toString(num_sample_repeats),
					initialPercent, Integer.toString(0), Long.toString(timeSinceRepetitionsStarted), Long.toString(timeSinceRepetitionsStarted), experimentFolderName, sampleName, 0);

			if (num_sample_repeats == 1) {
				printRepetition();
			}

			// # the iterator has already printed a message if num_sample_repeats > 1
			_doScan(sampleName, descriptions, logmsg);
		}
	}

	private void _doLooping() throws Exception {
		/*
		 * """ This is the basic looping based on the number of repetitions set in the UI. Beamlines should override
		 * this method if extra looping logic is required e.g. from sample environment settings """
		 */
		if (moveMonoToStartBeforeScan) {
			moveMonoToInitialPosition();// # I20 always moves things back to initial positions after each scan. To save
										// time, move mono to initial position here
			energy_scannable.waitWhileBusy();
		}

		setQueuePropertiesStart();
		currentRepetition = 0;
		timeRepetitionsStarted = System.currentTimeMillis();
		try {
			while (true) {
				currentRepetition++;
				_beforeEachRepetition();
				if (handleGapConverter) {
					setupHarmonic();
				}
				try {
					if (useIterator) {
						SampleEnvironmentIterator iterator = samplePreparer.createIterator(detectorBean
								.getExperimentType());
						_doItterator(iterator);
					} else {
						// # resolve these two values here as they will vary when using iterators
						String sampleName = sampleBean.getName();
						List<String> descriptions = sampleBean.getDescriptions();
						printRepetition();
						XasLoggingMessage logmsg = getLogMessage(sampleName);
						_doScan(sampleName, descriptions, logmsg);
					}
				} catch (Exception e) {
					handleScanInterrupt(e);
				}
				_runScript(outputBean.getAfterScriptName());// # run the after scan script
				checkForPause();
				if (checkIfRepetitionsFinished()) {
					break;
				}
				int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
				numRepetitions = numRepsFromProperty;
			}
		} finally {
			if (moveMonoToStartBeforeScan) {
				energy_scannable.stop();
			}
			if (handleGapConverter) {
				// print "enabling gap converter"
				Object auto_mDeg_idGap_mm_converter = Finder.getInstance().find("auto_mDeg_idGap_mm_converter");
				// auto_mDeg_idGap_mm_converter.enableAutoConversion();
			}
			// # repetition loop completed, so reset things
			setQueuePropertiesEnd();
			_resetHeader();
			detectorPreparer.completeCollection();
			// ScriptBase.checkForPauses()
		}
	}

	// # Runs a single XAS/XANES scan.
	private void _doScan(String sampleName, List<String> descriptions, XasLoggingMessage logmsg) throws Exception {
		// #self.loggingcontroller.update(None,logmsg)
		XASLoggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));// # informs parts of
																								// the UI about current
																								// scan
		// # run the before scan script
		_runScript(outputBean.getBeforeScriptName());
		// # work out which detectors to use (they will need to have been configured already by the GUI)
		Detector[] detectorList = _getDetectors();
		log("Using detectors" + detectorList);
		// # work out extra scannables to include
		ScanPlotSettings scanPlotSettings /* sampleScannables, outputScannables, scanPlotSettings */= runPreparers();
		List<Scannable> signalParameters = _getSignalList();
		XasProgressUpdater loggingbean = new XasProgressUpdater(XASLoggingScriptController, logmsg,
				timeRepetitionsStarted);
		// # build the scan command arguments
		Object[] args = buildScanArguments(detectorList, signalParameters, loggingbean);
		// # run the scan
		((LoggingScriptController) exafsScriptObserver).update(null, new ScriptProgressEvent("Running scan"));
		ConcurrentScan thisscan = createScan(args, sampleName, descriptions);
		((LoggingScriptController) exafsScriptObserver).update(null, new ScanCreationEvent(thisscan.getName()));
		if (scanPlotSettings != null) {
			log("Setting the filter for columns to plot...");
			thisscan.setScanPlotSettings(scanPlotSettings);
		}
		thisscan.runScan();
		((LoggingScriptController) exafsScriptObserver).update(null, new ScanFinishEvent(thisscan.getName(),
				ScanFinishEvent.FinishType.OK));
	}

	private XasScannable _createAndconfigureXASScannable() {
		XasScannable xas_scannable = new XasScannable();
		xas_scannable.setName("xas_scannable");
		// # to be consistent with the rest of this object, no longer use the scannable defined in the bean but
		// # use the energy scannable injected in the constructor
		xas_scannable.setEnergyScannable(energy_scannable);
		return xas_scannable;
	}

	private ConcurrentScan createScan(Object[] args, String sampleName, List<String> descriptions) {
		ConcurrentScan thisscan = new ConcurrentScan(args);
		_setUpDataWriterSetFilenames(thisscan, sampleName, descriptions);
		thisscan.setReturnScannablesToOrginalPositions(false);
		return thisscan;
	}

	// # run the beamline specific preparers
	public ScanPlotSettings runPreparers() throws Exception {
		detectorPreparer.prepare(scanBean, detectorBean, outputBean, experimentFullPath);
		// meta_clear_alldynamical()
		samplePreparer.prepare(sampleBean);
		outputPreparer.prepare(outputBean, scanBean);
		return outputPreparer.getPlotSettings(detectorBean, outputBean);
	}

	private Object[] buildScanArguments(Detector[] detectorList, List<Scannable> signalParameters,
			XasProgressUpdater loggingbean) throws Exception {
		XasScannable xas_scannable = _createAndconfigureXASScannable();
		xas_scannable.setDetectors(detectorList);
		return addScannableArgs(xas_scannable, resolveEnergiesFromScanBean(), detectorList, signalParameters,
				loggingbean);
		// return args;
	}

	private Object[] addScannableArgs(XasScannable xas_scannable, PyTuple energies, Detector[] detectorList,
			List<Scannable> signalParameters, Object loggingbean) {
		Object[] args = new Object[] { xas_scannable, energies };
		args = ArrayUtils.addAll(args, detectorList);
		args = ArrayUtils.addAll(args, signalParameters.toArray());
		args = ArrayUtils.add(args, loggingbean);
		return args;
	}

	public PyTuple resolveEnergiesFromScanBean() throws Exception {
		if (scanBean instanceof XanesScanParameters) {
			return XanesScanPointCreator.calculateEnergies((XanesScanParameters) scanBean);
		}
		return ExafsScanPointCreator.calculateEnergies((XasScanParameters) scanBean);
	}

	private void _beforeEachRepetition() throws Exception {
		Double[] times = new Double[] {};
		if (scanBean instanceof XasScanParameters) {
			times = ExafsScanPointCreator.getScanTimeArray((XasScanParameters) scanBean);
		} else if (scanBean instanceof XanesScanParameters) {
			times = XanesScanPointCreator.getScanTimeArray((XanesScanParameters) scanBean);
		}
		if (times.length > 0) {
			// print times;
			// print "ic", self.ionchambers;
			ionchambers.setTimes(times);
			log("Setting detector frame times, using array of length " + times.length + "...");
		}
		return;
	}

	// # TODO this should be in output preparer.
	private List<Scannable> _getSignalList() throws ParseException {
		List<Scannable> signalList = new ArrayList<Scannable>();
		for (SignalParameters signal : outputBean.getSignalList()) {
			int dp = signal.getDecimalPlaces();
			String dataFormat = "%6." + dp + 'f';// # construct data format from dp e.g. "%6.2f"
			Scannable scannable = JEPScannable.createJEPScannable(signal.getLabel(), signal.getScannableName(),
					dataFormat, signal.getName(), signal.getExpression());
			signalList.add(scannable);
		}
		return signalList;
	}

	private Detector[] _getDetectors() throws Exception {
		String expt_type = detectorBean.getExperimentType();
		// detectorList = []
		if (expt_type.equals("Transmission")) {
			log("This is a transmission scan");
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName().compareTo(detectorBean.getTransmissionParameters().getDetectorType()) == 0) {
					return _createDetArray(group.getDetector());
				}
			}
		} else if (expt_type.equals("XES")) {
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName().equals("XES")) {
					return _createDetArray(group.getDetector());
				}
			}
		}

		log("This is a fluoresence scan");
		for (DetectorGroup group : detectorBean.getDetectorGroups()) {
			if (group.getName().equals(detectorBean.getFluorescenceParameters().getDetectorType())) {
				// #print detectorBean.getFluorescenceParameters().getDetectorType(), "experiment"
				return _createDetArray(group.getDetector());
			}
		}
		return null;
	}

	// # Move to start energy so that harmonic can be set by gap_converter for i18 only
	private void setupHarmonic() throws DeviceException {// : #, gap_converter):
		double initialEnergy = ((ExafsScanPointCreator) scanBean).getInitialEnergy();
		// print "moving ", self.energy_scannable.getName(), " to start energy ", initialEnergy
		energy_scannable.moveTo(initialEnergy);
		// print "move complete, disabling harmonic change"
		// print "disabling harmonic converter";
		// TODO re-implement
		// auto_mDeg_idGap_mm_converter = Finder.getInstance().find("auto_mDeg_idGap_mm_converter");
		// auto_mDeg_idGap_mm_converter.disableAutoConversion();
		// #gap_converter.disableAutoConversion() #auto_mDeg_idGap-mm-converter
	}

	// #check if halt after current repetition set to true
	private void checkForPause() {
		if (numRepetitions > 1 && LocalProperties.check(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY)) {
			log("Paused scan after repetition"
					+ currentRepetition
					+ ". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.");
			// # should not operate the Command Queue here, as it will simply make the queue pause once this scan has
			// completed
			// # self.commandQueueProcessor.pause(500);
			// # instead set the Script pause flag and wait until someone clears it to resume this scan
			LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
		}
	}

	// #check if the number of repetitions has been altered and we should now end the loop
	private boolean checkIfRepetitionsFinished() {
		int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
		if (numRepsFromProperty < currentRepetition) {
			log("The number of repetitions has been reset to" + numRepsFromProperty + ". As" + currentRepetition
					+ "repetitions have been completed this scan will now end.");
			return true;
		} else if (numRepsFromProperty <= (currentRepetition)) {
			return true;// True# normal end to loop
		}
		return false;
	}

	private void moveMonoToInitialPosition() throws DeviceException, InterruptedException {
		Double initialPosition = null;
		if (scanBean instanceof XasScanParameters) {
			initialPosition = ((XasScanParameters) scanBean).getInitialEnergy();
		} else if (scanBean instanceof XanesScanParameters) {
			initialPosition = ((XanesScanParameters) scanBean).getRegions().get(0).getEnergy();
		} else if (scanBean instanceof XesScanParameters) {
			int xes_scanType = ((XesScanParameters) scanBean).getScanType();
			if (xes_scanType == XesScanParameters.SCAN_XES_FIXED_MONO) {
				initialPosition = ((XesScanParameters) scanBean).getMonoEnergy();
			} else {
				initialPosition = ((XesScanParameters) scanBean).getMonoInitialEnergy();
			}
		}

		if (energy_scannable != null && initialPosition != null) {
			energy_scannable.waitWhileBusy();
			energy_scannable.asynchronousMoveTo(initialPosition);
			log("Moving mono to initial position...");
		}
	}
}
