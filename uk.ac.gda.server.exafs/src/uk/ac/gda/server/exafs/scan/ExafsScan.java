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

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.data.scan.datawriter.DataWriter;
import gda.data.scan.datawriter.DataWriterFactory;
import gda.data.scan.datawriter.DefaultDataWriterFactory;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.exafs.scan.RepetitionsProperties;
import gda.jython.InterfaceProvider;
import gda.jython.commands.GeneralCommands;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.jython.scriptcontroller.logging.XasLoggingMessage;
import gda.scan.ConcurrentScan;
import gda.scan.Scan;
import gda.scan.ScanInterruptedException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

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
public abstract class ExafsScan {

	private static Logger logger = LoggerFactory.getLogger(ExafsScan.class);

	final protected BeamlinePreparer beamlinePreparer;
	final protected DetectorPreparer detectorPreparer;
	final protected SampleEnvironmentPreparer samplePreparer;
	final protected OutputPreparer outputPreparer;

	final protected Processor commandQueueProcessor;
	final protected LoggingScriptController XASLoggingScriptController;
	final protected AsciiDataWriterConfiguration datawriterconfig;
	final protected ArrayList<AsciiMetadataConfig> original_header;
	final protected Scannable energy_scannable;
	final protected boolean includeSampleNameInNexusName;
	final protected NXMetaDataProvider metashop;

	// variables which will change for each experiment
	private String sampleFileName;
	private String scanFileName;
	private String detectorFileName;
	private String outputFileName;
	protected ISampleParameters sampleBean;
	protected IScanParameters scanBean;
	protected IDetectorParameters detectorBean;
	protected IOutputParameters outputBean;
	protected int currentRepetition;
	protected int numRepetitions;
	protected String experimentFullPath;
	protected String experimentFolderName;
	protected String scriptType;
	protected String scan_unique_id;
	protected long timeRepetitionsStarted;

	public ExafsScan(BeamlinePreparer beamlinePreparer, DetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable,
			boolean includeSampleNameInNexusName, NXMetaDataProvider metashop) {
		this.beamlinePreparer = beamlinePreparer;
		this.detectorPreparer = detectorPreparer;
		this.samplePreparer = samplePreparer;
		this.outputPreparer = outputPreparer;
		this.commandQueueProcessor = commandQueueProcessor;
		this.XASLoggingScriptController = XASLoggingScriptController;
		this.datawriterconfig = datawriterconfig;
		this.original_header = original_header;
		this.energy_scannable = energy_scannable;
		this.includeSampleNameInNexusName = includeSampleNameInNexusName;
		this.metashop = metashop;
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

		doCollection(sampleFileName, scanFileName, detectorFileName, outputFileName, experimentFullPath, numRepetitions);

		return new PyInteger(0);
	}

	public void doCollection(String sampleFileName, String scanFileName, String detectorFileName,
			String outputFileName, String experimentFullPath, int numRepetitions) throws Exception {

		determineExperimentPath(experimentFullPath);

		createBeans(sampleFileName, scanFileName, detectorFileName, outputFileName);

		doCollection(numRepetitions);
	}

	public void doCollection(ISampleParameters sampleBean, IScanParameters scanBean, IDetectorParameters detectorBean,
			IOutputParameters outputBean, String experimentFullPath, int numRepetitions) throws Exception {

		this.scanBean = scanBean;
		this.sampleBean = sampleBean;
		this.detectorBean = detectorBean;
		this.outputBean = outputBean;

		setXmlFileNames("", "", "", "");

		determineExperimentPath(experimentFullPath);

		doCollection(numRepetitions);
	}

	/**
	 * The implementation-specific data collection.
	 * 
	 * @param numRepetitions
	 * @throws Exception
	 */
	protected abstract void doCollection(int numRepetitions) throws Exception;

	protected void prepareForCollection(String scriptType) {
		this.scriptType = scriptType;
		scan_unique_id = LoggingScriptController.createUniqueID(scriptType);
		log("Starting " + scriptType + " scan...");
	}

	protected void determineExperimentPath(String experimentFullPath) {
		String experimentFolderName = experimentFullPath.substring(experimentFullPath.indexOf("xml") + 4,
				experimentFullPath.length());
		log("Using data folder: " + experimentFullPath);
		log("Using xml subfolder: " + experimentFolderName);
		this.experimentFullPath = experimentFullPath;
		this.experimentFolderName = experimentFolderName;
	}

	public void setXmlFileNames(String sampleFileName, String scanFileName, String detectorFileName,
			String outputFileName) {
		this.sampleFileName = sampleFileName;
		this.scanFileName = scanFileName;
		this.detectorFileName = detectorFileName;
		this.outputFileName = outputFileName;
	}

	protected void log(String msg) {
		InterfaceProvider.getTerminalPrinter().print(msg);
		logger.info(msg);
	}

	protected void resetHeader() {
		datawriterconfig.setHeader(original_header);
		outputPreparer.resetNexusStaticMetadataList();
	}

	protected Detector[] createDetArray(String[] names) throws Exception {
		Detector[] dets = new Detector[] {};
		for (String name : names) {
			Object detector = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			if (detector == null) {
				throw new Exception("detector named " + name + " not found!");
			}
			dets = (Detector[]) ArrayUtils.add(dets, detector);
		}
		log("Using detectors" + dets);
		return dets;
	}

	protected void createBeans(String sampleFileName, String scanFileName, String detectorFileName,
			String outputFileName) throws Exception {
		log("beans created based on " + experimentFullPath + ", " + sampleFileName + ", " + scanFileName + ", "
				+ detectorFileName + ", " + outputFileName);
		sampleBean = null;
		if (sampleFileName != null) {
			sampleBean = (ISampleParameters) BeansFactory.getBeanObject(experimentFullPath + "/", sampleFileName);
		}
		scanBean = (IScanParameters) BeansFactory.getBeanObject(experimentFullPath + "/", scanFileName);
		detectorBean = (IDetectorParameters) BeansFactory.getBeanObject(experimentFullPath + "/", detectorFileName);
		outputBean = (IOutputParameters) BeansFactory.getBeanObject(experimentFullPath + "/", outputFileName);

		setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName);
	}

	protected void configurePreparers() throws Exception {
		beamlinePreparer.configure(scanBean, detectorBean, sampleBean, outputBean, experimentFullPath);
		detectorPreparer.configure(scanBean, detectorBean, outputBean, experimentFullPath);
		samplePreparer.configure(sampleBean);
		outputPreparer.configure(outputBean, scanBean, detectorBean);
	}

	protected void runScript(String scriptName) throws Exception {
		if (scriptName != null && !scriptName.isEmpty()) {
			GeneralCommands.run(scriptName);
		}
	}

	protected Scan setUpDataWriterSetFilenames(ConcurrentScan thisscan, String sampleName, List<String> descriptions)
			throws Exception {
		return setUpDataWriter(thisscan, sampleName, descriptions);
	}

	protected Scan setUpDataWriter(Scan thisscan, String sampleName, List<String> descriptions) throws Exception {

		// use the Factory to enable unit testing - which would use a DummyDataWriter
		DataWriterFactory datawriterFactory = new DefaultDataWriterFactory();
		DataWriter datawriter = datawriterFactory.createDataWriter();
		if (datawriter instanceof XasAsciiNexusDataWriter) {
			XasAsciiNexusDataWriter dataWriter = (XasAsciiNexusDataWriter) datawriter;

			String[] filenameTemplates = deriveFilenametemplates(sampleName);

			dataWriter.setSampleName(sampleName);
			dataWriter.setDescriptions(descriptions);
			dataWriter.setNexusFileNameTemplate(filenameTemplates[0]);
			dataWriter.setAsciiFileNameTemplate(filenameTemplates[1]);
			dataWriter.setSampleName(sampleName);
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

			addMetadata();
		}
		thisscan.setDataWriter(datawriter);
		return thisscan;
	}

	private String[] deriveFilenametemplates(String sampleName) {
		String nexusSubFolder = experimentFolderName + "/" + outputBean.getNexusDirectory();
		String asciiSubFolder = experimentFolderName + "/" + outputBean.getAsciiDirectory();
		String nexusFileNameTemplate, asciiFileNameTemplate;

		sampleName = sampleName.replaceAll(" +", "_");

		if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX)) {
			if (sampleName != null && !sampleName.isEmpty()) {
				nexusFileNameTemplate = nexusSubFolder + "/%d_" + sampleName + "_" + currentRepetition + ".nxs";
				asciiFileNameTemplate = asciiSubFolder + "/%d_" + sampleName + "_" + currentRepetition + ".dat";
			} else {
				nexusFileNameTemplate = nexusSubFolder + "/%d_" + currentRepetition + ".nxs";
				asciiFileNameTemplate = asciiSubFolder + "/%d_" + currentRepetition + ".dat";
			}
		} else if (includeSampleNameInNexusName) {
			nexusFileNameTemplate = nexusSubFolder + "/" + sampleName + "_%d_" + currentRepetition + ".nxs";
			asciiFileNameTemplate = asciiSubFolder + "/" + sampleName + "_%d_" + currentRepetition + ".dat";
		} else {
			nexusFileNameTemplate = nexusSubFolder + "/" + "%d_" + currentRepetition + ".nxs";
			asciiFileNameTemplate = asciiSubFolder + "/" + sampleName + "_%d_" + currentRepetition + ".dat";
		}

		return new String[] { nexusFileNameTemplate, asciiFileNameTemplate };
	}

	private void addMetadata() throws Exception {
		metashop.add(scanFileName, BeansFactory.getXMLString(scanBean));
		metashop.add(detectorFileName, BeansFactory.getXMLString(detectorBean));
		metashop.add(sampleFileName, BeansFactory.getXMLString(sampleBean));
		metashop.add(outputFileName, BeansFactory.getXMLString(outputBean));

		String detectorFileName = determineDetectorFilename();
		if (!detectorFileName.isEmpty()) {
			metashop.add("DetectorConfigurationParameters",
					BeansFactory.getXMLString(experimentFullPath + File.separator + detectorFileName));
		}
	}

	protected String determineDetectorFilename() {
		String xmlFileName = "";
		if (detectorBean.getExperimentType().equalsIgnoreCase("Fluorescence")) {
			FluorescenceParameters fluoresenceParameters = detectorBean.getFluorescenceParameters();
			xmlFileName = fluoresenceParameters.getConfigFileName();
		} else if (detectorBean.getExperimentType() == "XES") {
			FluorescenceParameters fluoresenceParameters = detectorBean.getXesParameters();
			xmlFileName = fluoresenceParameters.getConfigFileName();
		}
		return xmlFileName;
	}

	protected String getMyVisitID() {
		return InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID();
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
			log("Starting repetition" + currentRepetition + "of" + numRepetitions);
		} else {
			log("Starting " + scriptType + " scan...");
		}
	}

	protected void runPreparers() throws Exception {
		detectorPreparer.beforeEachRepetition();
		outputPreparer.beforeEachRepetition();
	}

	protected String calcInitialPercent() {
		return ((currentRepetition - 1) / numRepetitions) * 100 + "%";
	}

	private long calcTimeSinceRepetitionsStarted() {
		return System.currentTimeMillis() - timeRepetitionsStarted;
	}

	protected XasLoggingMessage getLogMessage(String sampleName) throws Exception {
		String initialPercent = calcInitialPercent();
		long timeSinceRepetitionsStarted = calcTimeSinceRepetitionsStarted();
		return new XasLoggingMessage(getMyVisitID(), scan_unique_id, scriptType, "Starting " + scriptType + " scan...",
				Integer.toString(currentRepetition), Integer.toString(numRepetitions), Integer.toString(1),
				Integer.toString(1), initialPercent, Integer.toString(0), Long.toString(timeSinceRepetitionsStarted),
				scanBean, experimentFolderName, sampleName, 0);
	}

	protected void handleScanInterrupt(Exception exceptionObject) throws Exception {
		if (LocalProperties.get(RepetitionsProperties.SKIP_REPETITION_PROPERTY) == "true") {
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
		}
	}
}
