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

package uk.ac.gda.client.microfocus.scan;

import gda.commandqueue.Processor;
import gda.configuration.properties.LocalProperties;
import gda.data.metadata.NXMetaDataProvider;
import gda.data.scan.datawriter.AsciiDataWriterConfiguration;
import gda.data.scan.datawriter.AsciiMetadataConfig;
import gda.data.scan.datawriter.NexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDataWriter;
import gda.data.scan.datawriter.XasAsciiNexusDatapointCompletingDataWriter;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.Scannable;
import gda.device.detector.countertimer.TfgScalerWithFrames;
import gda.device.detector.xmap.NexusXmap;
import gda.device.detector.xspress.Xspress2System;
import gda.device.scannable.ScannableUtils;
import gda.gui.RCPController;
import gda.jython.commands.ScannableCommands;
import gda.jython.scriptcontroller.logging.LoggingScriptController;
import gda.scan.Scan;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import uk.ac.gda.beans.exafs.DetectorGroup;
import uk.ac.gda.beans.exafs.i18.AttenuatorParameters;
import uk.ac.gda.beans.exafs.i18.I18SampleParameters;
import uk.ac.gda.beans.microfocus.MicroFocusScanParameters;
import uk.ac.gda.client.microfocus.scan.datawriter.MicroFocusWriterExtender;
import uk.ac.gda.server.exafs.scan.BeamlinePreparer;
import uk.ac.gda.server.exafs.scan.DetectorPreparer;
import uk.ac.gda.server.exafs.scan.ExafsScan;
import uk.ac.gda.server.exafs.scan.OutputPreparer;
import uk.ac.gda.server.exafs.scan.SampleEnvironmentPreparer;

public class MapScan extends ExafsScan {

	private final Xspress2System xspress2ssytem;
	private final NexusXmap xmapMca;
	private final EnumPositioner d7a;
	private final EnumPositioner d7b;
	private final TfgScalerWithFrames counterTimer01;
	private final RCPController rcpController;
	private final LoggingScriptController exafsScriptObserver;
	private final Scannable xScan;
	private final Scannable yScan;

	private MicroFocusWriterExtender mfd;
	private MicroFocusScanParameters mapScanParameters;
	private Scannable energyScannable;
	private Scannable zScannable;

	public MapScan(BeamlinePreparer beamlinePreparer, DetectorPreparer detectorPreparer,
			SampleEnvironmentPreparer samplePreparer, OutputPreparer outputPreparer, Processor commandQueueProcessor,
			LoggingScriptController XASLoggingScriptController, AsciiDataWriterConfiguration datawriterconfig,
			ArrayList<AsciiMetadataConfig> original_header, Scannable energy_scannable,
			boolean includeSampleNameInNexusName, NXMetaDataProvider metashop, Xspress2System xspress2ssytem,
			NexusXmap xmapMca, EnumPositioner d7a, EnumPositioner d7b, TfgScalerWithFrames counterTimer01,
			RCPController rcpController, LoggingScriptController ExafsScriptObserver, Scannable xScan, Scannable yScan,
			Scannable zScannable) {

		super(beamlinePreparer, detectorPreparer, samplePreparer, outputPreparer, commandQueueProcessor,
				XASLoggingScriptController, datawriterconfig, original_header, energy_scannable,
				includeSampleNameInNexusName, metashop);

		this.xspress2ssytem = xspress2ssytem;
		this.xmapMca = xmapMca;
		this.d7a = d7a;
		this.d7b = d7b;
		this.counterTimer01 = counterTimer01;
		this.rcpController = rcpController;
		this.exafsScriptObserver = ExafsScriptObserver;
		this.xScan = xScan;
		this.yScan = yScan;
		this.zScannable = zScannable;

		// finder = Finder.getInstance()
		// mfd = None
		// detectorBeanFileName = ""
		// sampleFilename= None
		// scanFilename= None
		// detectorFilename= None
		// outputFilename= None
	}

	// def getMFD(self):
	// return mfd

	// public void doCollection( sampleFileName, scanFileName, detectorFileName, outputFileName, folderName=None,
	// scanNumber= -1, validation=True):
	//
	// print ""
	// print "*********************"
	// log("Preparing for map...")
	//
	boolean origScanPlotSettings = LocalProperties.check("gda.scan.useScanPlotSettings");

	//
	// experimentFullPath, experimentFolderName = determineExperimentPath(folderName)
	// setXmlFileNames(sampleFileName, scanFileName, detectorFileName, outputFileName)
	//
	// if(sampleFileName == None or sampleFileName == 'None'):
	// sampleBean = None
	// else:
	// sampleBean = BeansFactory.getBeanObject(experimentFullPath, sampleFileName)
	//
	// scanBean = BeansFactory.getBeanObject(experimentFullPath, scanFileName)
	// detectorBean = BeansFactory.getBeanObject(experimentFullPath, detectorFileName)
	// outputBean = BeansFactory.getBeanObject(experimentFullPath, outputFileName)
	//
	//
	// # sanity check
	// if detectorBean.getFluorescenceParameters().getConfigFileName() == None or
	// detectorBean.getFluorescenceParameters().getConfigFileName() == "":
	// raise
	// Exception(" No Fluoresence parameters file supplied: have you selected the Fluoresence option in Detector Parameters?")
	//
	// beanGroup = BeanGroup()
	// beanGroup.setController(ExafsScriptObserver)
	// beanGroup.setXmlFolder(experimentFullPath)
	// beanGroup.setScannable(finder.find(scanBean.getXScannableName())) #TODO
	// beanGroup.setExperimentFolderName(experimentFolderName)
	// beanGroup.setScanNumber(scanNumber)
	// if(sampleBean != None):
	// beanGroup.setSample(sampleBean)
	// beanGroup.setDetector(detectorBean)
	// beanGroup.setOutput(outputBean)
	// beanGroup.setValidate(validation)
	// beanGroup.setScan(scanBean)

	@Override
	protected void doCollection(int numRepetitions) throws Exception {

		mapScanParameters = (MicroFocusScanParameters) scanBean;

		Detector[] detectorList = getDetectors();
		// log("Detectors: " + str(detectorList))

		// # *************** DIFFERENT
		_setupForMap();

		// xScannable = finder.find(scanBean.getXScannableName())
		// yScannable = finder.find(scanBean.getYScannableName())

		// if xScannable==None:
		// from gdascripts.parameters import uk.ac.gda.server.exafs.scan.ExafsScan;
		// import beamline_parameters
		// jythonNameMap = beamline_parameters.JythonNameSpaceMapping()
		// xScannable=jythonNameMap.__getitem__(scanBean.getXScannableName())
		// yScannable=jythonNameMap.__getitem__(scanBean.getYScannableName())
		//

		int nx = ScannableUtils.getNumberSteps(xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize()) + 1;
		int ny = ScannableUtils.getNumberSteps(yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize()) + 1;
		log("Number x points: " + nx);
		log("Number y points: " + ny);

		double[] energyList = new double[] { mapScanParameters.getEnergy() };
		double zScannablePos = mapScanParameters.getZValue();

		// FIXME is this the right place?
		detectorPreparer.configure(mapScanParameters, detectorBean, outputBean, experimentFullPath);
		detectorPreparer.beforeEachRepetition();

		String detectorBeanFileName = experimentFullPath + detectorBean.getFluorescenceParameters().getConfigFileName();
		_createMFD(nx, ny, mapScanParameters.getXStepSize(), mapScanParameters.getYStepSize(), detectorList);

		for (double energy : energyList) {

			// Scannable energyScannable = finder.find(scanBean.getEnergyScannableName());
			log("Energy: " + energy);
			energyScannable.moveTo(energy);
			mfd.setEnergyValue(energy);

			// zScannable = finder.find(scanBean.getZScannableName());
			mfd.setZValue(zScannablePos);
			log("From xml, using: " + mapScanParameters.getXScannableName() + ", "
					+ mapScanParameters.getYScannableName() + ", " + zScannable.getName());
			// if(zScannablePos != None){
			log("Moving " + zScannable.getName() + " to " + zScannablePos);
			zScannable.moveTo(zScannablePos);
			// }

			Date scanStart = new Date();

			// # redefineNexusMetadataForMaps(beanGroup)

			try {
				// FIXME what is scanNumber here?

				_runMap(detectorList, 1, nx, ny);
			} finally {
				Date scanEnd = new Date();
				if (origScanPlotSettings) {
					LocalProperties.set("gda.scan.useScanPlotSettings", "true");
				} else {
					LocalProperties.set("gda.scan.useScanPlotSettings", "false");
				}
				log("Map start time " + scanStart);
				log("Map end time " + scanEnd);
				finish();
			}
		}
	}

	private void _createMFD(int nx, int ny, double xStepSize, double yStepSize, Detector[] detectorList) {
		mfd = new MicroFocusWriterExtender(nx, ny, xStepSize, yStepSize, detectorBean, detectorList);
	}

	private void _runMap(Detector[] detectorList, int scanNumber, int nx, int ny) throws Exception {

		// scanBean.setCollectionTime(scanBean.getCollectionTime());???

		Object[] args = new Object[] { yScan, mapScanParameters.getYStart(), mapScanParameters.getYEnd(),
				mapScanParameters.getYStepSize(), xScan, mapScanParameters.getXStart(), mapScanParameters.getXEnd(),
				mapScanParameters.getXStepSize(), zScannable };

		counterTimer01.setCollectionTime(mapScanParameters.getCollectionTime());

		// # what does this do? why is it not in raster map? Adding this to raster map does not set live time.
		boolean useFrames = LocalProperties.check("gda.microfocus.scans.useFrames");
		log("Using frames: " + useFrames);
		if (detectorBean.getExperimentType().equals("Fluorescence") && useFrames) {
			args = ArrayUtils.add(args, detectorList);
			counterTimer01.clearFrameSets();
			log("Frame collection time: " + mapScanParameters.getCollectionTime());
			counterTimer01.addFrameSet(nx, 1.0E-4, mapScanParameters.getCollectionTime() * 1000.0, 0, 7, -1, 0);
		} else {
			for (Detector detector : detectorList) {
				args = ArrayUtils.add(args, detector);
				args = ArrayUtils.add(args, mapScanParameters.getCollectionTime());
			}
		}
		Scan mapscan = ScannableCommands.createConcurrentScan(args);
		String sampleName = sampleBean.getName();
		List<String> descriptions = sampleBean.getDescriptions();
		_setUpDataWriter(mapscan, sampleName, descriptions, scanNumber);
		mapscan.getScanPlotSettings().setIgnore(true);
		// finder.find("elementListScriptController").update(None, detectorBeanFileName); FIXME
		log("Starting step map...");
		mapscan.runScan();
	}

	// # should merge with method in xas_scan but keeping here while developing to see what differences required
	// # Here _setUpData method is a repetition of the same method in the class Scan except for the name of the nexus
	// and ascii files

	private void _setUpDataWriter(Scan thisscan, String sampleName, List<String> descriptions, int repetition) {
		String nexusSubFolder = experimentFolderName + "/" + outputBean.getNexusDirectory();
		String asciiSubFolder = experimentFolderName + "/" + outputBean.getAsciiDirectory();

		String nexusFileNameTemplate = nexusSubFolder + "/" + sampleName + "_%d_" + repetition + ".nxs";
		String asciiFileNameTemplate = asciiSubFolder + "/" + sampleName + "_%d_" + repetition + ".dat";
		if (LocalProperties.check(NexusDataWriter.GDA_NEXUS_BEAMLINE_PREFIX)) {
			nexusFileNameTemplate = nexusSubFolder + "/%d_" + sampleName + "_" + repetition + ".nxs";
			asciiFileNameTemplate = asciiSubFolder + "/%d_" + sampleName + "_" + repetition + ".dat";
		}

		// # Create DataWriter object and give it the parameters.
		// # Use XasAsciiNexusDatapointCompletingDataWriter as we will use a PositionCallable for raster
		// # scans to return the real motor positions which are not available until the end of each row.
		XasAsciiNexusDatapointCompletingDataWriter twoDWriter = new XasAsciiNexusDatapointCompletingDataWriter();
		twoDWriter.addDataWriterExtender(mfd);
		XasAsciiNexusDataWriter dataWriter = twoDWriter.getXasDataWriter();

		// FIXME is done by ExafsScan class?
		// if (Finder.getInstance().find("metashop") != None){
		// meta_add(detectorFileName, BeansFactory.getXMLString(detectorBean));
		// meta_add(outputFileName, BeansFactory.getXMLString(outputBean));
		// meta_add(sampleFileName, BeansFactory.getXMLString(sampleBean));
		// meta_add(scanFileName, BeansFactory.getXMLString(scanBean));
		// meta_add("xmlFolderName", experimentFullPath);
		// xmlFilename = _determineDetectorFilename(detectorBean);
		// if ((xmlFilename != null) && (experimentFullPath != None)){
		// detectorConfigurationBean = BeansFactory.getBeanObject(experimentFullPath, xmlFilename);
		// meta_add("DetectorConfigurationParameters", BeansFactory.getXMLString(detectorConfigurationBean)) ;
		// }
		// }
		// else{
		// logger.info("Metashop not found");
		// }

		// FIXME is done by ExafsScan class?
		// dataWriter.setFolderName(experimentFullPath);
		// dataWriter.setScanParametersName(scanFileName);
		// dataWriter.setDetectorParametersName(detectorFileName);
		// dataWriter.setSampleParametersName(sampleFileName);
		// dataWriter.setOutputParametersName(outputFileName);

		// # add the detector configuration file to the metadata
		// #dataWriter.setXmlFileName(_determineDetectorFilename(detectorBean))

		dataWriter.setDescriptions(descriptions);
		dataWriter.setNexusFileNameTemplate(nexusFileNameTemplate);
		dataWriter.setAsciiFileNameTemplate(asciiFileNameTemplate);

		// # get the ascii file format configuration (if not set here then will get it from the Finder inside the Java
		// class
		AsciiDataWriterConfiguration asciidatawriterconfig = outputPreparer.getAsciiDataWriterConfig(scanBean);
		if (asciidatawriterconfig != null) {
			dataWriter.setConfiguration(asciidatawriterconfig);
		}
		thisscan.setDataWriter(twoDWriter);
	}

	private void _setupFromSampleParameters() throws DeviceException {
		outputBean.setAsciiFileName(sampleBean.getName());
		log("Ascii file prefix: " + sampleBean.getName());
		AttenuatorParameters att1 = ((I18SampleParameters) sampleBean).getAttenuatorParameter1();
		AttenuatorParameters att2 = ((I18SampleParameters) sampleBean).getAttenuatorParameter2();
		log("Moving: " + d7a.getName() + " to " + att1.getSelectedPosition());
		log("Moving: " + d7b.getName() + " to " + att2.getSelectedPosition());
		d7a.moveTo(att1.getSelectedPosition());
		d7b.moveTo(att2.getSelectedPosition());
		LocalProperties.set("gda.scan.useScanPlotSettings", "true");
	}

	private void _setupForMap() throws DeviceException {

		// FIXME all should be done in a preparer???
		// if (LocalProperties.get("gda.mode").equals("live")){
		// collectionTime = beanGroup.getScan().getCollectionTime();
		// command_server = finder.find("command_server") ;
		// topupMonitor = command_server.getFromJythonNamespace("topupMonitor", None) ;
		// beamMonitor = command_server.getFromJythonNamespace("beamMonitor", None);
		// detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None);
		//
		// topupMonitor.setPauseBeforePoint(True);
		// topupMonitor.setCollectionTime(collectionTime);
		// topupMonitor.setPauseBeforeLine(False);
		//
		// beamMonitor.setPauseBeforePoint(True);
		// beamMonitor.setPauseBeforeLine(True);
		//
		// if(beanGroup.getDetector().getExperimentType().equals("Fluorescence") &&
		// beanGroup.getDetector().getFluorescenceParameters().getDetectorType().equals("Germanium")){
		// finder.find("command_server").addDefault(detectorFillingMonitor);
		// detectorFillingMonitor.setPauseBeforePoint(True);
		// detectorFillingMonitor.setPauseBeforeLine(False);
		// }
		// }

		_setupFromSampleParameters();

		rcpController.openPerspective("uk.ac.gda.microfocus.ui.MicroFocusPerspective");
	}

	private Detector[] getDetectors() throws Exception {
		String expt_type = detectorBean.getExperimentType();
		if (expt_type.equals("Transmission")) {
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName() == detectorBean.getTransmissionParameters().getDetectorType()) {
					return createDetArray(group.getDetector());
				}
			}
		} else {
			for (DetectorGroup group : detectorBean.getDetectorGroups()) {
				if (group.getName().equals(detectorBean.getFluorescenceParameters().getDetectorType())) {
					return createDetArray(group.getDetector());
				}
			}
		}
		return new Detector[] {};
	}

	private void finish() throws Exception {
		// command_server = finder.find("command_server");
		// beam = command_server.getFromJythonNamespace("beam", None);
		// detectorFillingMonitor = command_server.getFromJythonNamespace("detectorFillingMonitor", None);
		// finder.find("command_server").removeDefault(beam);
		// finder.find("command_server").removeDefault(detectorFillingMonitor);

		// FIXME this should be done in a preparer!
		// Vector<Scannable> defaultScannables = InterfaceProvider.getDefaultScannableProvider().getDefaultScannables();
		// defaultScannables.remove(beam);
		// defaultScannables.remove(detectorFillingMonitor);

		if (mfd != null) {
			try {
				mfd.closeWriter();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				// logger.error("TODO put description of error here", e);
			}
		}
	}

}
