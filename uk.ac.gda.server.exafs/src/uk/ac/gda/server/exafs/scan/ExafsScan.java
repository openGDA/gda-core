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
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.device.Detector;
import gda.device.Scannable;
import gda.jython.InterfaceProvider;
import gda.jython.commands.GeneralCommands;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.ConcurrentScan;
import gda.scan.Scan;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.BeansFactory;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

public class ExafsScan {

	private static Logger logger = LoggerFactory.getLogger(ExafsScan.class);

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

	public ExafsScan(DetectorPreparer detectorPreparer, SampleEnvironmentPreparer samplePreparer,
			OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable, boolean includeSampleNameInNexusName, NXMetaDataProvider metashop) {
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

	protected void _resetHeader() {
		datawriterconfig.setHeader(original_header);
		outputPreparer._resetNexusStaticMetadataList();
	}

	protected Detector[] _createDetArray(String[] names) throws Exception {
		Detector[] dets = new Detector[] {};
		for (String name : names) {
			Object detector = InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name);
			if (detector == null) {
				throw new Exception("detector named " + name + " not found!");
			}
			dets = (Detector[]) ArrayUtils.add(dets, detector);
		}
		return dets;
	}

	protected void _createBeans(String sampleFileName, String scanFileName, String detectorFileName,
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

	protected void _runScript(String scriptName) throws Exception {
		if (scriptName != null && !scriptName.isEmpty()) {
			GeneralCommands.run(scriptName);
		}
	}

	protected Scan _setUpDataWriterSetFilenames(ConcurrentScan thisscan, String sampleName, List<String> descriptions)
			throws Exception {
		return _setUpDataWriter(thisscan, sampleName, descriptions);
	}

	protected Scan _setUpDataWriter(Scan thisscan, String sampleName, List<String> descriptions) throws Exception {

		String[] filenameTemplates = deriveFilenametemplates(sampleName);

		XasAsciiNexusDataWriter dataWriter = new XasAsciiNexusDataWriter();
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

		thisscan.setDataWriter(dataWriter);
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

		String detectorFileName = _determineDetectorFilename();
		if (!detectorFileName.isEmpty()) {
			metashop.add("DetectorConfigurationParameters", BeansFactory.getXMLString(detectorFileName));
		}
	}

	protected String _determineDetectorFilename() {
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

	protected String _getMyVisitID() {
		return InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID();
	}
}
