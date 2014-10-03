package uk.ac.gda.server.exafs.scan;

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.device.Detector;
import gda.device.Scannable;
import gda.device.scannable.JEPScannable;
import gda.device.scannable.XasScannable;
import gda.exafs.scan.ExafsScanPointCreator;
import gda.exafs.scan.RepetitionsProperties;
import gda.exafs.scan.ScanStartedMessage;
import gda.exafs.scan.XanesScanPointCreator;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ConcurrentScan;
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
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.SignalParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;

public class XasScan extends ExafsScan {

	public XasScan(BeamlinePreparer beamlinePreparer, DetectorPreparer detectorPreparer, SampleEnvironmentPreparer samplePreparer,
			OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, NXMetaDataProvider metashop, boolean includeSampleNameInNexusName) {
		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor, XASLoggingScriptController,
				datawriterconfig, original_header, energy_scannable, includeSampleNameInNexusName, metashop);
	}

	/**
	 * For convenience when calling from Jython.
	 * 
	 * @param pyArgs
	 * @return 0 - normal completion 
	 * @throws Exception
	 */
	public PyObject __call__(PyObject pyArgs) throws Exception {

		String sampleFileName = ((PySequence) pyArgs).__finditem__(0).asString();
		String scanFileName = ((PySequence) pyArgs).__finditem__(1).asString();
		String detectorFileName = ((PySequence) pyArgs).__finditem__(2).asString();
		String outputFileName = ((PySequence) pyArgs).__finditem__(3).asString();
		String experimentFullPath = ((PySequence) pyArgs).__finditem__(4).asString();
		int numRepetitions = ((PySequence) pyArgs).__finditem__(5).asInt();
		
		doCollection(sampleFileName,scanFileName,detectorFileName,outputFileName, experimentFullPath,numRepetitions);
		
		return new PyInteger(0);
	}
	
	public void doCollection(String sampleFileName, String scanFileName, String detectorFileName, String outputFileName, String experimentFullPath, int numRepetitions) throws Exception {
		
		determineExperimentPath(experimentFullPath);

		createBeans(sampleFileName, scanFileName, detectorFileName, outputFileName);
		
		doCollection(numRepetitions);
	}
	
	public void doCollection(ISampleParameters sampleBean, IScanParameters scanBean, IDetectorParameters detectorBean, IOutputParameters outputBean, String experimentFullPath, int numRepetitions) throws Exception {
		
		this.scanBean = scanBean;
		this.sampleBean = sampleBean;
		this.detectorBean = detectorBean;
		this.outputBean = outputBean;
		
		setXmlFileNames("","","","");
		
		determineExperimentPath(experimentFullPath);
		
		doCollection(numRepetitions);
	}
	
	private void doCollection(int numRepetitions) throws Exception {
		this.numRepetitions = numRepetitions;

		configurePreparers();

		scriptType = "Exafs";
		if (scanBean instanceof XanesScanParameters) {
			scriptType = "Xanes";
		}

		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);

		doRepetitions();
	}


	private void doRepetitions() throws Exception {

		setQueuePropertiesStart();
		currentRepetition = 0;
		timeRepetitionsStarted = System.currentTimeMillis();
		SampleEnvironmentIterator iterator = samplePreparer.createIterator(detectorBean
				.getExperimentType());
		try {
			
			beamlinePreparer.prepareForExperiment();
			
			while (true) {
				currentRepetition++;
				try {
					doSampleEnvironmentIterator(iterator);
				} catch (Exception e) {
					handleScanInterrupt(e);
				}
				checkForPause();
				if (checkIfRepetitionsFinished()) {
					break;
				}
				int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
				numRepetitions = numRepsFromProperty;
			}
		} finally {
//			if (moveMonoToStartBeforeScan) {
//				energy_scannable.stop();
//			}
			// if (handleGapConverter) {
			// TODO move to I18's detectorPreparer.completeCollection() call one of the preparers here to do some
			// beamline specific reset
			// print "enabling gap converter"
			// Object auto_mDeg_idGap_mm_converter = Finder.getInstance().find("auto_mDeg_idGap_mm_converter");
			// auto_mDeg_idGap_mm_converter.enableAutoConversion();
			// }
			setQueuePropertiesEnd();
			resetHeader();
			detectorPreparer.completeCollection();
			beamlinePreparer.completeExperiment();
		}
	}

	private void doSampleEnvironmentIterator(SampleEnvironmentIterator iterator) throws Exception {
		iterator.resetIterator();
		int num_sample_repeats = iterator.getNumberOfRepeats();
		for (int i = 0; i < num_sample_repeats; i++) {
			iterator.next();
			String sampleName = iterator.getNextSampleName();
			List<String> descriptions = iterator.getNextSampleDescriptions();
			String initialPercent = calcInitialPercent();
			long timeSinceRepetitionsStarted = System.currentTimeMillis() - timeRepetitionsStarted;
			XasLoggingMessage logmsg = new XasLoggingMessage(getMyVisitID(), scan_unique_id, scriptType, "Starting "
					+ scriptType + " scan...", Integer.toString(currentRepetition), Integer.toString(numRepetitions),
					Integer.toString(i + 1), Integer.toString(num_sample_repeats), initialPercent, Integer.toString(0),
					Long.toString(timeSinceRepetitionsStarted), Long.toString(timeSinceRepetitionsStarted),
					experimentFolderName, sampleName, 0);

			if (num_sample_repeats == 1) {
				printRepetition();
			}

			doSingleScan(sampleName, descriptions, logmsg);
		}
	}

	// Runs a single energy (XAS/XANES) scan once the sample environment has been set up
	private void doSingleScan(String sampleName, List<String> descriptions, XasLoggingMessage logmsg) throws Exception {
		
		runScript(outputBean.getBeforeScriptName());
		
		runPreparers();

		XasProgressUpdater loggingbean = new XasProgressUpdater(XASLoggingScriptController, logmsg,
				timeRepetitionsStarted);
		ScanPlotSettings scanPlotSettings = outputPreparer.getPlotSettings();
		List<Scannable> signalParameters = getSignalList();
		Detector[] detectorList = getDetectors();
		Object[] args = buildScanArguments(detectorList, signalParameters, loggingbean);
		
		ConcurrentScan thisscan = createScan(args, sampleName, descriptions, scanPlotSettings);
		
		// TODO need to review why different observers need different event types here
		XASLoggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));
		XASLoggingScriptController.update(null, new ScriptProgressEvent("Running scan"));
		XASLoggingScriptController.update(null, new ScanCreationEvent(thisscan.getName()));

		thisscan.runScan();
		
		runScript(outputBean.getAfterScriptName());
		
		XASLoggingScriptController.update(null, new ScanFinishEvent(thisscan.getName(), ScanFinishEvent.FinishType.OK));
	}

	private XasScannable createAndconfigureXASScannable() {
		XasScannable xas_scannable = new XasScannable();
		xas_scannable.setName("xas_scannable");
		xas_scannable.setEnergyScannable(energy_scannable);
		return xas_scannable;
	}

	private ConcurrentScan createScan(Object[] args, String sampleName, List<String> descriptions,
			ScanPlotSettings scanPlotSettings) throws Exception {
		ConcurrentScan thisscan = new ConcurrentScan(args);
		setUpDataWriterSetFilenames(thisscan, sampleName, descriptions);
		thisscan.setReturnScannablesToOrginalPositions(false);

		if (scanPlotSettings != null) {
			log("Setting the filter for columns to plot...");
			thisscan.setScanPlotSettings(scanPlotSettings);
		}
		return thisscan;
	}

	private Object[] buildScanArguments(Detector[] detectorList, List<Scannable> signalParameters,
			XasProgressUpdater loggingbean) throws Exception {
		XasScannable xas_scannable = createAndconfigureXASScannable();
		xas_scannable.setDetectors(detectorList);
		return addScannableArgs(xas_scannable, resolveEnergiesFromScanBean(), detectorList, signalParameters,
				loggingbean);
	}

	private Object[] addScannableArgs(XasScannable xas_scannable, PyTuple energies, Detector[] detectorList,
			List<Scannable> signalParameters, Object loggingbean) {
		Object[] args = new Object[] { xas_scannable, energies };
		args = ArrayUtils.addAll(args, detectorList);
		args = ArrayUtils.addAll(args, signalParameters.toArray());
		args = ArrayUtils.add(args, loggingbean);
		return args;
	}

	protected PyTuple resolveEnergiesFromScanBean() throws Exception {
		if (scanBean instanceof XanesScanParameters) {
			return XanesScanPointCreator.calculateEnergies((XanesScanParameters) scanBean);
		}
		return ExafsScanPointCreator.calculateEnergies((XasScanParameters) scanBean);
	}

	private List<Scannable> getSignalList() throws ParseException {
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

	private Detector[] getDetectors() throws Exception {
		String expt_type = detectorBean.getExperimentType();
		if (expt_type.equalsIgnoreCase("Transmission")) {
			log("This is a transmission scan");
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName().equalsIgnoreCase(detectorBean.getTransmissionParameters().getDetectorType())) {
					return createDetArray(group.getDetector());
				}
			}
		} else if (expt_type.equalsIgnoreCase("XES")) {
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName().equalsIgnoreCase("XES")) {
					return createDetArray(group.getDetector());
				}
			}
		}

		log("This is a fluoresence scan");
		for (DetectorGroup group : detectorBean.getDetectorGroups()) {
			if (group.getName().equalsIgnoreCase(detectorBean.getFluorescenceParameters().getDetectorType())) {
				return createDetArray(group.getDetector());
			}
		}
		return null;
	}

	// # Move to start energy so that harmonic can be set by gap_converter for i18 only
	// TODO must add this to the i18 detector preparer before each repeptition
	// private void setupHarmonic() throws DeviceException {// : #, gap_converter):
	// double initialEnergy = ((ExafsScanPointCreator) scanBean).getInitialEnergy();
	// print "moving ", self.energy_scannable.getName(), " to start energy ", initialEnergy
	// energy_scannable.moveTo(initialEnergy);
	// print "move complete, disabling harmonic change"
	// print "disabling harmonic converter";
	// auto_mDeg_idGap_mm_converter = Finder.getInstance().find("auto_mDeg_idGap_mm_converter");
	// auto_mDeg_idGap_mm_converter.disableAutoConversion();
	// #gap_converter.disableAutoConversion() #auto_mDeg_idGap-mm-converter
	// }

	private void checkForPause() {
		if (numRepetitions > 1 && LocalProperties.check(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY)) {
			log("Paused scan after repetition"
					+ currentRepetition
					+ ". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.");
			LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
		}
	}

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

//	private void moveMonoToInitialPosition() throws DeviceException, InterruptedException {
//		Double initialPosition = null;
//		if (scanBean instanceof XasScanParameters) {
//			initialPosition = ((XasScanParameters) scanBean).getInitialEnergy();
//		} else if (scanBean instanceof XanesScanParameters) {
//			initialPosition = ((XanesScanParameters) scanBean).getRegions().get(0).getEnergy();
//		} else if (scanBean instanceof XesScanParameters) {
//			int xes_scanType = ((XesScanParameters) scanBean).getScanType();
//			if (xes_scanType == XesScanParameters.SCAN_XES_FIXED_MONO) {
//				initialPosition = ((XesScanParameters) scanBean).getMonoEnergy();
//			} else {
//				initialPosition = ((XesScanParameters) scanBean).getMonoInitialEnergy();
//			}
//		}
//
//		if (energy_scannable != null && initialPosition != null) {
//			energy_scannable.waitWhileBusy();
//			energy_scannable.asynchronousMoveTo(initialPosition);
//			log("Moving mono to initial position...");
//		}
//	}
}
