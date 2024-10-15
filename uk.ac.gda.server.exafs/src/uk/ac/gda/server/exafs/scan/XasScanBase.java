/*-
 * Copyright © 2014 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.server.exafs.scan;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.data.fileregistrar.FileRegistrar;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterFactory;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.NexusDataWriterConfiguration;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.exafs.scan.RepetitionsProperties;
import gda.exafs.scan.ScanStartedMessage;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServerFacade;
import gda.jython.JythonStatus;
import gda.jython.ScriptBase;
import gda.jython.batoncontrol.ClientDetails;
import gda.jython.commands.GeneralCommands;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.event.ScanCreationEvent;
import gda.jython.scriptcontroller.event.ScanFinishEvent;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.jython.scriptcontroller.logging.XasProgressUpdater;
import gda.scan.ConcurrentScan;
import gda.scan.Scan;
import gda.scan.ScanInterruptedException;
import gda.scan.ScanPlotSettings;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorConfigurationParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.server.exafs.scan.iterators.SampleEnvironmentIterator;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Base class for all Spectroscopy "UI" scans. These scan objects are created in localStation and are used for every UI
 * scan.
 * <p>
 * Each individual experiment is configured using for XML files which are given to this object through one of the
 * doCollection methods.
 * <p>
 * This class prepares sample environments, detectors, the beamline and output options before creating and then running
 * a regular GDA Concurrent or Continuous Scan to preform the actual data collection and recording.
 * <p>
 * This class is also responsible for creating logging objects which report scan progress to the UI via the
 * LogginScriptController.
 * <p>
 * Beamline customisation is provided by the 'Preparer' objects and the ascii header configured in Spring using a
 * AsciiDataWriterConfiguration object.
 */
public abstract class XasScanBase implements XasScan {

	private static Logger logger = LoggerFactory.getLogger(XasScanBase.class);

	private BeamlinePreparer beamlinePreparer;
	protected DetectorPreparer detectorPreparer;
	private SampleEnvironmentPreparer samplePreparer;
	private OutputPreparer outputPreparer;

	protected LoggingScriptController loggingScriptController;
	private boolean includeSampleNameInNexusName;
	private NXMetaDataProvider metashop;
	private String scanName;

	// variables which will change for each experiment
	private String sampleFileName;
	private String scanFileName;
	private String detectorFileName;
	private String outputFileName;
	private String detectorConfigurationFilename;

	protected ISampleParameters sampleBean;
	protected IScanParameters scanBean;
	protected IDetectorParameters detectorBean;
	protected IOutputParameters outputBean;
	protected IDetectorConfigurationParameters detectorConfigurationBean;
	protected int currentRepetition;
	protected int numRepetitions;
	protected String experimentFullPath;
	protected String experimentFolderName;
	protected String scriptType;
	protected String scan_unique_id;
	protected long timeRepetitionsStarted;
	protected XasProgressUpdater loggingbean;

	/** Name of Metadata entry : to contain list of scan Nexus files generated so far in the set of repetitions */
	private String filesInRepetitionEntry = "files_in_repetition_scan";
	private String totalNumRepetitionsEntry = "total_num_repetitions";
	private String currentRepetitionEntry = "current_repetition";

	private List<Detector> detectorOrder = Collections.emptyList();

	private List<String> nexusTemplateFiles = Collections.emptyList();


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

		configureCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions);

		doCollection();

		return new PyInteger(0);
	}

	public void configureCollection(String sampleFileName, String scanFileName, String detectorFileName,
			String outputFileName, String experimentFullPath, int numRepetitions) throws Exception {

		this.numRepetitions = numRepetitions;

		determineExperimentPath(experimentFullPath);

		createBeans(sampleFileName, scanFileName, detectorFileName, outputFileName);
	}

	@Override
	public void configureCollection(ISampleParameters sampleBean, IScanParameters scanBean, IDetectorParameters detectorBean,
			IOutputParameters outputBean, IDetectorConfigurationParameters detectorConfigurationBean,
			String experimentFullPath, int numRepetitions) throws Exception {

		this.scanBean = scanBean;
		this.sampleBean = sampleBean;
		this.detectorBean = detectorBean;
		this.outputBean = outputBean;
		this.detectorConfigurationBean = detectorConfigurationBean;
		this.numRepetitions = numRepetitions;

		setXmlFileNames("", "", "", "", "");

		determineExperimentPath(experimentFullPath);
	}

	@Override
	public void doCollection() throws Exception {

		configurePreparers();

		prepareForCollection(getScanType());

		doRepetitions();
	}

	private void doRepetitions() throws Exception {

		prepareRepetitions();

		SampleEnvironmentIterator iterator = samplePreparer.createIterator(detectorBean.getExperimentType());
		try {

			beamlinePreparer.prepareForExperiment();

			while (true) {
				currentRepetition++;
				try {
					doSampleEnvironmentIterator(iterator);
				} catch (Exception e) {
					handleScanInterruptAndLogIt(e);
				}
				checkForPause();
				if (checkIfRepetitionsFinished()) {
					break;
				}
				int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
				numRepetitions = numRepsFromProperty;
			}
		} finally {
			finishRepetitions();
		}
	}

	protected void prepareRepetitions() {
		setQueuePropertiesStart();
		currentRepetition = 0;
		timeRepetitionsStarted = System.currentTimeMillis();
		loggingbean = new XasProgressUpdater(loggingScriptController, timeRepetitionsStarted);
	}

	protected void finishRepetitions() throws Exception {
		setQueuePropertiesEnd();
		resetHeader();
		tidyUpNexusTemplates();
		detectorPreparer.completeCollection();
		beamlinePreparer.completeExperiment();
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
			loggingbean.setFromMessage(logmsg);

			if (num_sample_repeats == 1) {
				printRepetition();
			}

			// Execute the 'before first repetition' scan/script
			if (currentRepetition == 1 && i == 0) {
				runScriptOrCommand(outputBean.getBeforeFirstRepetition());
			}
			doSingleScan(sampleName, descriptions);
		}
	}

	// Runs a single energy (XAS/XANES) scan once the beamline and sample environment has been set up
	private void doSingleScan(String sampleName, List<String> descriptions) throws Exception {

		runScriptOrCommand(outputBean.getBeforeScriptName());

		runDetectorAndOutputPreparers();

		setupNexusTemplates();

		createAndRunScan(sampleName, descriptions);

		runScriptOrCommand(outputBean.getAfterScriptName());
	}

	protected void createAndRunScan(String sampleName, List<String> descriptions)
			throws Exception {
		// this will be the bespoke bit for each scan type
		Object[] scanArgs = createScanArguments(sampleName, descriptions);
		scanArgs = ArrayUtils.add(scanArgs, loggingbean);
		Set<Scannable> scannablesToBeAddedAsColumnInDataFile = outputPreparer.getScannablesToBeAddedAsColumnInDataFile();
		if (!scannablesToBeAddedAsColumnInDataFile.isEmpty()) {
			scanArgs = ArrayUtils.addAll(scanArgs, scannablesToBeAddedAsColumnInDataFile.toArray());
		}
		ConcurrentScan theScan = ScannableCommands.createConcurrentScan(scanArgs);
		setUpDataWriter(theScan, sampleName, descriptions);

		ScanPlotSettings scanPlotSettings = outputPreparer.getPlotSettings();
		if (scanPlotSettings != null) {
			log("Setting the filter for columns to plot...");
			theScan.setScanPlotSettings(scanPlotSettings);
		}

		loggingScriptController.update(null, new ScanStartedMessage(scanBean, detectorBean));
		loggingScriptController.update(null, new ScriptProgressEvent("Running scan"));
		loggingScriptController.update(null, new ScanCreationEvent(theScan.getName()));
		theScan.runScan();
		loggingScriptController.update(null, new ScanFinishEvent(theScan.getName(), ScanFinishEvent.FinishType.OK));
	}

	public abstract Object[] createScanArguments(String sampleName, List<String> descriptions) throws Exception;

	private void checkForPause() throws InterruptedException {
		if (LocalProperties.check(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY)) {
			log("** Paused scan after repetition "
					+ currentRepetition
					+ ". To resume the scan, press the Start button in the Command Queue view. To abort this scan, press the Skip Task button.");

			try {
				LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
				ScriptBase.setPaused(true);
				// will now wait here indefinitely until the Command Queue is resumed or aborted
				ScriptBase.checkForPauses();
			} catch (InterruptedException e) {
				//Set script status back to running, to avoid getting left in idle state and hanging the command queue.
				JythonServerFacade.getInstance().setScriptStatus(JythonStatus.RUNNING);
				throw new InterruptedException("Abort or Stop task button pressed while paused at end of repetition "+currentRepetition);
			}
		}
	}

	private boolean checkIfRepetitionsFinished() {
		int numRepsFromProperty = LocalProperties.getAsInt(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY);
		if (numRepsFromProperty < currentRepetition) {
			log("** The number of repetitions has been reset to" + numRepsFromProperty + ". As" + currentRepetition
					+ "repetitions have been completed this scan will now end.");
			return true;
		} else if (numRepsFromProperty <= (currentRepetition)) {
			return true;// True# normal end to loop
		}
		return false;
	}

	private void prepareForCollection(String scriptType) {
		this.scriptType = scriptType;
		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
		// log("Starting " + scriptType + " scan...");
	}

	protected void determineExperimentPath(String experimentFullPath) {

		if (!experimentFullPath.endsWith(File.separator)) {
			experimentFullPath = experimentFullPath + File.separator;
		}
		String experimentFolderName = experimentFullPath.substring(experimentFullPath.indexOf("xml") + 4,
				experimentFullPath.length());
		log("Using data folder: " + experimentFullPath);
		log("Using xml subfolder: " + experimentFolderName);

		this.experimentFullPath = experimentFullPath;
		this.experimentFolderName = experimentFolderName;
	}

	public void setXmlFileNames(String sampleFileName, String scanFileName, String detectorFileName,
			String outputFileName, String detectorConfigFileName) {
		this.sampleFileName = sampleFileName;
		this.scanFileName = scanFileName;
		this.detectorFileName = detectorFileName;
		this.outputFileName = outputFileName;
		this.detectorConfigurationFilename = detectorConfigFileName;
	}

	/** @return array of xml bean filenames containing scan settings : sample, scan, detector, output, detector config.
	 */
	public String[] getXmlFileNames() {
		return new String[] {sampleFileName, scanFileName, detectorFileName, outputFileName, detectorConfigurationFilename};
	}

	protected void log(String msg) {
		InterfaceProvider.getTerminalPrinter().print(msg);
		logger.info(msg);
	}

	protected void resetHeader() {
		// datawriterconfig.setHeader(original_header);
		outputPreparer.resetStaticMetadataList();
		metashop.clear();
	}

	protected Detector[] createDetArray(String[] names) throws Exception {
		Detector[] dets = new Detector[] {};
		for (String name : names) {
			Object detector = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			if (detector == null) {
				throw new Exception("Problem setting up detector list from parameters - detector named " + name + " was not found!");
			}
			dets = (Detector[]) ArrayUtils.add(dets, detector);
		}
		log("Using detectors: " + ArrayUtils.toString(names));
		return dets;
	}

	private void createBeans(String sampleFileName, String scanFileName, String detectorFileName, String outputFileName)
			throws Exception {
		log("beans created based on " + experimentFullPath + ", " + sampleFileName + ", " + scanFileName + ", "
				+ detectorFileName + ", " + outputFileName);
		sampleBean = null;
		if (sampleFileName != null) {
			sampleBean = (ISampleParameters) XMLHelpers.getBeanObject(experimentFullPath, sampleFileName);
		}
		scanBean = (IScanParameters) XMLHelpers.getBeanObject(experimentFullPath, scanFileName);
		detectorBean = (IDetectorParameters) XMLHelpers.getBeanObject(experimentFullPath, detectorFileName);
		outputBean = (IOutputParameters) XMLHelpers.getBeanObject(experimentFullPath, outputFileName);

		// get the xml for the specific detector in use e.g. vortex, xspress2 or xspress3
		// TODO these beans should have their own interface for clarity
		String configName = determineDetectorFilenames();
		if (StringUtils.isNotEmpty(configName)) {
			detectorConfigurationFilename = configName;
			detectorConfigurationBean = (IDetectorConfigurationParameters) XMLHelpers.getBeanObject(experimentFullPath, detectorConfigurationFilename);
		}

		setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName, configName);
	}

	private void configurePreparers() throws Exception {
		beamlinePreparer.configure(scanBean, detectorBean, sampleBean, outputBean, experimentFullPath);
		detectorPreparer.configure(scanBean, detectorBean, outputBean, experimentFullPath);
		samplePreparer.configure(scanBean, sampleBean);
		outputPreparer.configure(outputBean, scanBean, detectorBean, sampleBean);
	}

	/**
	 * Run Jython script or execute command on Jython console : Run script if corresponding file exists, otherwise run string as command on Jython console
	 *
	 * @param scriptNameOrCommand Name of Jython script or command
	 * @throws Exception
	 * @since 15/4/2016
	 */
	private void runScriptOrCommand(String scriptNameOrCommand) throws Exception {
		if ( scriptNameOrCommand == null || scriptNameOrCommand.isEmpty() )
			return;
		File scriptFile = new File(scriptNameOrCommand);
		if (scriptFile.isFile()) {
			logger.debug("Running script {}", scriptNameOrCommand);
			runScript(scriptNameOrCommand);
		} else {
			logger.debug("Running jython command : {}", scriptNameOrCommand);
			InterfaceProvider.getCommandRunner().runsource(scriptNameOrCommand);

		}
	}

	private void runScript(String scriptName) throws Exception {
		if (scriptName != null && !scriptName.isEmpty()) {
			GeneralCommands.run(scriptName);
		}
	}

	protected Scan setUpDataWriter(Scan thisscan, String sampleName, List<String> descriptions) throws Exception {
		DataWriter writer = createAndConfigureDataWriter(sampleName, descriptions);
		thisscan.setDataWriter(writer);
		return thisscan;
	}

	protected DataWriter createAndConfigureDataWriter(String sampleName, List<String> descriptions) throws Exception {

		addMetadata();
		addDetectorMetadata(detectorBean.getDetectorConfigurations());

		DataWriterFactory datawriterFactory = new DefaultDataWriterFactory();
		DataWriter datawriter = datawriterFactory.createDataWriter();

		if (datawriter instanceof XasAsciiNexusDataWriter) {
			XasAsciiNexusDataWriter dataWriter = (XasAsciiNexusDataWriter) datawriter;
			setupXasAsciiNexusDataWriter(sampleName, descriptions, dataWriter);
		}

		return datawriter;
	}

	protected void setupXasAsciiNexusDataWriter(String sampleName, List<String> descriptions,
			XasAsciiNexusDataWriter dataWriter) throws Exception {
		String[] filenameTemplates = deriveFilenametemplates(sampleName);

		dataWriter.setSampleName(sampleName);
		dataWriter.setDescriptions(descriptions);
		dataWriter.setNexusFileNameTemplate(filenameTemplates[0]);
		dataWriter.setAsciiFileNameTemplate(filenameTemplates[1]);
		dataWriter.setRunFromExperimentDefinition(true);
		dataWriter.setFolderName(experimentFullPath);
		dataWriter.setScanParametersName(scanFileName);
		dataWriter.setDetectorParametersName(detectorFileName);
		dataWriter.setSampleParametersName(sampleFileName);
		dataWriter.setOutputParametersName(outputFileName);

		AsciiDataWriterConfiguration asciidatawriterconfig = outputPreparer.getAsciiDataWriterConfig(scanBean);
		if (asciidatawriterconfig != null) {
			dataWriter.setConfiguration(asciidatawriterconfig);
		}

		Map<String, FileRegistrar> fileRegistrar = Finder.getFindablesOfType(FileRegistrar.class);
		if (fileRegistrar != null && fileRegistrar.size() > 0) {
			if (fileRegistrar.size() > 1) {
				logger.warn("{} FileRegistar objects found on server. Only one expected.", fileRegistrar.size());
			}
			String name = fileRegistrar.keySet().iterator().next();
			dataWriter.addDataWriterExtender(fileRegistrar.get(name));
			logger.info("Adding FileRegistar {} as DataWriterExtender for XasAsciiNexusScan", name);
		}
	}

	private void addMetadata() throws Exception {
		metashop.add(scanFileName, getXMLString(scanBean));
		metashop.add(detectorFileName, getXMLString(detectorBean));
		metashop.add(sampleFileName, getXMLString(sampleBean));
		metashop.add(outputFileName, getXMLString(outputBean));

		// Add newline separated list of scans completed so far in the series of repetitions.
		String completedScanNames = loggingbean.getCompletedScanFileNames()
			.stream()
			.collect(Collectors.joining("\n"));
		metashop.add(filesInRepetitionEntry, completedScanNames);

		// Add repetition information
		metashop.add(currentRepetitionEntry, currentRepetition);
		metashop.add(totalNumRepetitionsEntry, numRepetitions);

		if (StringUtils.isNotEmpty(detectorConfigurationFilename)) {
			metashop.add("DetectorConfigurationParameters", getXMLString(experimentFullPath + File.separator
					+ detectorConfigurationFilename));
		}
	}

	/**
	 * Add Detector configuration XML files content to the metadata.
	 * These are the configuration files for the detectors to be used in the scan extracted from a DetectorConfig list.
	 *
	 * @param detConfigurations list of {@link DetectorConfig} objects
	 * @throws Exception
	 */
	private void addDetectorMetadata(List<DetectorConfig> detConfigurations) throws Exception {
		if (detConfigurations == null || detConfigurations.isEmpty()) {
			return;
		}
		for(DetectorConfig config : detConfigurations) {
			if (config.isUseDetectorInScan() && Boolean.TRUE.equals(config.isUseConfigFile())) {
				String filePath = Paths.get(experimentFullPath, config.getConfigFileName()).toString();
				String entryName = config.getDetectorName()+" configuration";
				metashop.add(entryName, getFileContent(filePath));
			}
		}
	}

	private String getFileContent(final String filepath) throws Exception {
		return FileUtils.readFileToString(Paths.get(filepath).toFile(), Charset.defaultCharset());
	}

	private String getXMLString(final Object beanOrPath) throws Exception {
		Object bean = beanOrPath;
		if (beanOrPath instanceof String) {
			bean = XMLHelpers.getBean(new File((String) beanOrPath));
		}

		URL mapping = null;
		final Field[] fa = bean.getClass().getFields();
		for (int i = 0; i < fa.length; i++) {
			if (fa[i].getName().equalsIgnoreCase("mappingurl")) {
				mapping = (URL) fa[i].get(null);
			}
		}
		return XMLHelpers.toXMLString(mapping, bean);
	}

	private String[] deriveFilenametemplates(String sampleName) {

		String nexusSubFolder = experimentFolderName + outputBean.getNexusDirectory();
		String asciiSubFolder = experimentFolderName + outputBean.getAsciiDirectory();
		String nexusFileNameTemplate, asciiFileNameTemplate;

		sampleName = sampleName.replaceAll(" +", "_");

		if (!nexusSubFolder.endsWith(File.separator)) {
			nexusSubFolder += File.separator;
		}
		if (!asciiSubFolder.endsWith(File.separator)) {
			asciiSubFolder += File.separator;
		}
		if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX)) {
			if (sampleName != null && !sampleName.isEmpty()) {
				nexusFileNameTemplate = nexusSubFolder + "%d_" + sampleName + "_" + currentRepetition + ".nxs";
				asciiFileNameTemplate = asciiSubFolder + "%d_" + sampleName + "_" + currentRepetition + ".dat";
			} else {
				nexusFileNameTemplate = nexusSubFolder + "%d_" + currentRepetition + ".nxs";
				asciiFileNameTemplate = asciiSubFolder + "%d_" + currentRepetition + ".dat";
			}
		} else if (includeSampleNameInNexusName) {
			nexusFileNameTemplate = nexusSubFolder + sampleName + "_%d_" + currentRepetition + ".nxs";
			asciiFileNameTemplate = asciiSubFolder + sampleName + "_%d_" + currentRepetition + ".dat";
		} else {
			nexusFileNameTemplate = nexusSubFolder + "%d_" + currentRepetition + ".nxs";
			asciiFileNameTemplate = asciiSubFolder + sampleName + "_%d_" + currentRepetition + ".dat";
		}

		return new String[] { nexusFileNameTemplate, asciiFileNameTemplate };
	}

	private String determineDetectorFilenames() {
		if (!detectorBean.getDetectorConfigurations().isEmpty()) {
			return "";
		}

		if (detectorBean.getExperimentType().equalsIgnoreCase("Fluorescence")) {
			FluorescenceParameters fluorescenceParameters = detectorBean.getFluorescenceParameters();
			return fluorescenceParameters.getConfigFileName();
		} else if (detectorBean.getExperimentType().equals("XES")) {
			FluorescenceParameters fluorescenceParameters = detectorBean.getXesParameters();
			return fluorescenceParameters.getConfigFileName();
		}
		return "";
	}

	/** Return visit id from baton holder.
	 * If there is no batonHolder, client has lost the baton for some reason - probably due to network communication timeout.
	 * In this case, set the visit id to default value rather than throwing exception so that the scan can continue.
	 * (This function is used when creating the logging message, and not critical for creating or running a scan)
	 * @return Visit id, or 'unknown-visit' if baton holder is unavailable
	 */
	protected String getMyVisitID() {
		String visitId = "unknown-visit";
		ClientDetails batonHolder = InterfaceProvider.getBatonStateProvider().getBatonHolder();
		if (batonHolder!=null) {
			visitId = batonHolder.getVisitID();
		}
		return visitId;
	}

	protected void setQueuePropertiesStart() {
		LocalProperties.set(RepetitionsProperties.PAUSE_AFTER_REP_PROPERTY, "false");
		LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
		LocalProperties.set(RepetitionsProperties.NUMBER_REPETITIONS_PROPERTY, Integer.toString(numRepetitions));
	}

	protected void setQueuePropertiesEnd() {
		LocalProperties.set("gda.scan.useScanPlotSettings", "false");
		LocalProperties.set("gda.plot.ScanPlotSettings.fromUserList", "false");
	}

	protected void printRepetition() {
		if (numRepetitions > 1) {
			log("**** Starting repetition " + currentRepetition + " of " + numRepetitions + " ****");
		} else {
			log("**** Starting " + scriptType + " scan...");
		}
	}

	protected void runDetectorAndOutputPreparers() throws Exception {
		detectorPreparer.beforeEachRepetition();
		outputPreparer.beforeEachRepetition();
	}

	protected String calcInitialPercent() {
		return ((currentRepetition - 1) / numRepetitions) * 100 + "%";
	}

	protected void handleScanInterruptAndLogIt(Exception exceptionObject) throws Exception {
		try {
			handleScanInterrupt(exceptionObject);
		} catch(Exception e) {
			loggingbean.atCommandFailure();
			throw exceptionObject;
		}
	}

	protected void handleScanInterrupt(Exception exceptionObject) throws Exception {
		if (LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY).equals("true")) {
			LocalProperties.set(RepetitionsProperties.SKIP_REPETITION_PROPERTY, "false");
			// check if a panic stop has been issued, so the whole script should stop?
			if (Thread.currentThread().isInterrupted()) {
				throw new ScanInterruptedException();
			}
			// only wanted to skip this repetition, so absorb the exception and continue the loop
			if (numRepetitions > 1) {
				log("Repetition" + currentRepetition + "skipped.");
			} else {
				throw exceptionObject;
			}
		} else {
			throw exceptionObject;
		}
	}

	/**
	 * Generate a list of detector objects to be used for the scan from the detector bean -
	 * either from detector configuration list (if present) or the selected detector group.
	 *
	 * @return Array of detector objects
	 * @throws Exception
	 */
	protected Detector[] getDetectors() throws Exception {

		String[] detectorNames;

		if (detectorBean.getDetectorConfigurations() != null && !detectorBean.getDetectorConfigurations().isEmpty()) {
			// Make list with the names of all the detectors to be used
			List<String> detectorNameList = detectorBean.getDetectorConfigurations()
					.stream()
					.filter(DetectorConfig::isUseDetectorInScan)
					.map(DetectorConfig::getAllDetectorNames)
					.flatMap(List::stream)
					.toList();
			// Convert to array
			detectorNames = detectorNameList.toArray(new String[] {});

		} else {
			// Get the detector group name for the experiment type
			String exptType = detectorBean.getExperimentType();
			String detectorGroupName = "";
			if (exptType.equalsIgnoreCase(DetectorParameters.FLUORESCENCE_TYPE)) {
				detectorGroupName = detectorBean.getFluorescenceParameters().getDetectorType();
			} else if (exptType.equalsIgnoreCase(DetectorParameters.XES_TYPE)) {
				detectorGroupName = detectorBean.getXesParameters().getDetectorType();
			} else {
				detectorGroupName = detectorBean.getTransmissionParameters().getDetectorType();
			}

			// group name needs to be final for the stream.
			final String groupName = detectorGroupName;

			// Get the detector names from the detector group
			detectorNames = detectorBean.getDetectorGroups()
				.stream()
				.filter(g -> g.getName().equalsIgnoreCase(groupName))
				.map(DetectorGroup::getDetector)
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Could not build the list of detectors as no group of detectors named "
						+ groupName + " was found in the XML file."));

		}

		// Convert the names to detector objects
		Detector[] detectors = createDetArray(detectorNames);

		if (detectors == null) {
			throw new IllegalArgumentException("Could not build the list of detector object from "+Arrays.asList(detectorNames));
		}

		Detector[] extraDetectors = detectorPreparer.getExtraDetectors();
		if (extraDetectors != null) {
			detectors = (Detector[]) ArrayUtils.addAll(detectors, extraDetectors);
		}
		return detectors;
	}

	public BeamlinePreparer getBeamlinePreparer() {
		return beamlinePreparer;
	}

	public void setBeamlinePreparer(BeamlinePreparer beamlinePreparer) {
		this.beamlinePreparer = beamlinePreparer;
	}

	public DetectorPreparer getDetectorPreparer() {
		return detectorPreparer;
	}

	public void setDetectorPreparer(DetectorPreparer detectorPreparer) {
		this.detectorPreparer = detectorPreparer;
	}

	public void setOutputPreparer(OutputPreparer outputPreparer) {
		this.outputPreparer = outputPreparer;
	}

	public OutputPreparer getOutputPreparer() {
		return outputPreparer;
	}

	public void setSamplePreparer(SampleEnvironmentPreparer samplePreparer) {
		this.samplePreparer = samplePreparer;
	}

	public void setLoggingScriptController(LoggingScriptController loggingScriptController) {
		this.loggingScriptController = loggingScriptController;
	}

	public void setMetashop(NXMetaDataProvider metashop) {
		this.metashop = metashop;
	}

	public void setIncludeSampleNameInNexusName(boolean includeSampleNameInNexusName) {
		this.includeSampleNameInNexusName = includeSampleNameInNexusName;
	}

	public String getScanName() {
		return scanName;
	}

	public void setScanName(String scanName) {
		this.scanName = scanName;
	}

	public String getFilesInRepetitionEntry() {
		return filesInRepetitionEntry;
	}

	/**
	 * Set the name of the entry in the before_scan metadata used to
	 * record the names of scans completed in a series of repetitions.
	 *
	 * @param filesInRepetitionEntry
	 */
	public void setFilesInRepetitionEntry(String filesInRepetitionEntry) {
		this.filesInRepetitionEntry = filesInRepetitionEntry;
	}

	/**
	 *
	 * @return Name of the entry in before_scan metadata that records the total number of repetitions
	 */
	public String getTotalNumRepetitionsEntry() {
		return totalNumRepetitionsEntry;
	}

	/**
	 *
	 * @return Name of the entry in before_scan metadata that records the current repetition number for the scan
	 */
	public String getCurrentRepetitionEntry() {
		return currentRepetitionEntry;
	}


	public List<Detector> getDetectorOrder() {
		return detectorOrder;
	}

	/**
	 * Set a specific order to be used when adding detector objects to the scan command
	 *
	 * @param detectorOrder
	 */
	public void setDetectorOrder(List<Detector> detectorOrder) {
		this.detectorOrder = detectorOrder;
	}

	/**
	 * Generate an ordered list of datector objects from an array of detectors.
	 * These are added in the same order as the list set using {@link #setDetectorOrder}.
	 *
	 * @param detectors
	 * @return reordered list of detectors
	 */
	protected Detector[] getOrderedDetectors(Detector[] detectors) {

		if (detectorOrder == null || detectorOrder.isEmpty()) {
			return detectors;
		}
		List<Detector> detectorsForScan = Arrays.asList(detectors);

		// Make list of items from 'detectors' array in the same order as objects in 'detectorOrder'
		List<Detector> orderedDetectors = new ArrayList<>();
		detectorOrder.stream()
			.filter(detectorsForScan::contains)
			.forEach(orderedDetectors::add);

		// Add any detectors not in the detectorOrder list
		detectorsForScan.stream()
			.filter(det -> !orderedDetectors.contains(det))
			.forEach(orderedDetectors::add);

		return orderedDetectors.toArray(new Detector[] {});
	}

	public List<String> getNexusTemplateFiles() {
		return nexusTemplateFiles;
	}

	public void setNexusTemplateFiles(List<String> nexusTemplateFiles) {
		this.nexusTemplateFiles = nexusTemplateFiles;
	}

	protected void setupNexusTemplates() {
		// Get reference to Nexus template file list
		List<String> configTemplateFileList = NexusDataWriterConfiguration.getInstance().getNexusTemplateFiles();

		// Any any new filenames to the list of files in the config
		nexusTemplateFiles.forEach(name -> {
			if (!configTemplateFileList.contains(name)) {
				logger.debug("Adding Nexus template file name to configuration list : {}", name);
				configTemplateFileList.add(name);
			}
		});
	}

	protected void tidyUpNexusTemplates() {
		logger.debug("Removing Nexus template files from configuration list : {}", nexusTemplateFiles);
		List<String> configTemplateFileList = NexusDataWriterConfiguration.getInstance().getNexusTemplateFiles();
		configTemplateFileList.removeAll(nexusTemplateFiles);
	}
}
