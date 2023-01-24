/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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


import static java.lang.Boolean.TRUE;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.detector.DetectorHdfFunctions;
import gda.device.detector.NXDetector;
import gda.device.detector.countertimer.TfgScaler;
import gda.device.detector.nxdetector.NXPluginBase;
import gda.device.detector.nxdetector.roi.ImutableRectangularIntegerROI;
import gda.device.detector.nxdetector.roi.MutableRectangularIntegerROI;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.device.detector.xmap.NexusXmapFluorescenceDetectorAdapter;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.commands.GeneralCommands;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.IonChamberParameters;
import uk.ac.gda.beans.exafs.IonChambersBean;
import uk.ac.gda.beans.medipix.MedipixParameters;
import uk.ac.gda.beans.medipix.ROIRegion;
import uk.ac.gda.devices.detector.DetectorWithConfigurationFile;
import uk.ac.gda.devices.detector.FluorescenceDetector;
import uk.ac.gda.devices.detector.FluorescenceDetectorParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

/**
 * Functionality common across {@link DetectorPreparer} implementations for different beamlines.
 */
public class DetectorPreparerFunctions {
	private static final Logger logger = LoggerFactory.getLogger(DetectorPreparerFunctions.class);

	private Scannable[] sensitivities;
	private Scannable[] sensitivityUnits;
	private Scannable[] offsets;
	private Scannable[] offsetUnits;

	/**
	 * Path to the directory containing the configuration (xml) files for configuring the detectors.
	 */
	private String configFileDirectory;

	/**
	 * Top level directory where data for scan should be written.
	 * Subdirectories within this will contain specific types of data e.g. nexus/ ascii/ xspress3/ etc.
	*/
	private String dataDirectory;

	/**
	 * Subdirectory in {@link #dataDirectory} where data file for each detector will be written
	 */
	private Map<String, String> directoryNamesForDetectorData = new HashMap<>();

	/**
	 * Initial hdf file paths for hdf plugin of NXdetector, Xspress3, Xspress4 detectors.
	 * These will point to {@link #dataDirectory} during the scan, and reset to initial values afterwards.
	 */
	private Map<Detector, String> initialHdfFilePaths = new HashMap<>();

	private Map<NXDetector, List<NXPluginBase>> initialNXPlugins = new HashMap<>();

	private List<NXPluginBase> mutableRoiPluginList;

	private MutableRectangularIntegerROI mutableRoiForMedipix;

	public DetectorPreparerFunctions() {
		// nothing to do here
	}

	/**
	 * Load a {@link FluorescenceDetectorParameters} bean from an XML file, and apply it to the detector.
	 * @param xmlFileName
	 * @throws Exception
	 */
	public Detector configureDetector(String xmlFileName) throws Exception {
		FluorescenceDetectorParameters params = getDetectorParametersBean(xmlFileName);
		Detector det = configureDetector(params);
		setConfigFilename(det, xmlFileName);
		return det;
	}

	public FluorescenceDetectorParameters getDetectorParametersBean(String xmlFileName) throws Exception {
		return (FluorescenceDetectorParameters) XMLHelpers.getBean(new File(xmlFileName));
	}

	public void setConfigFilename(Detector det, String xmlFilename) {
		if (det instanceof DetectorWithConfigurationFile) {
			((DetectorWithConfigurationFile) det).setConfigFileName(xmlFilename);
		}
	}

	/**
	 * Apply {@link FluorescenceDetectorParameters} to the detector named by {@link FluorescenceDetectorParameters#getDetectorName()}.
	 * @param params
	 * @throws Exception if named detector could not be found
	 */
	public Detector configureDetector(FluorescenceDetectorParameters params) throws Exception {
		FluorescenceDetector det = Finder.findOptionalOfType(params.getDetectorName(), FluorescenceDetector.class)
				.orElseThrow(() -> new NoSuchElementException("Unable to find detector called "+params.getDetectorName()+" on server\n") );

		return configureDetector(det, params);
	}

	public Detector configureDetector(FluorescenceDetector det, FluorescenceDetectorParameters params) throws Exception {
		det.applyConfigurationParameters(params);

		// For Xmap return NexusXmap detector actually used for scans.
		if (det instanceof NexusXmapFluorescenceDetectorAdapter) {
			return NexusXmapFluorescenceDetectorAdapter.class.cast(det).getXmap();
		}
		return Detector.class.cast(det);
	}

	public void setupAmplifierSensitivity(IonChamberParameters ionChamberParams, int index) throws DeviceException {
		if (!ionChamberParams.getChangeSensitivity()) {
			return;
		}

		if (ionChamberParams.getGain() == null || ionChamberParams.getGain().isEmpty()) {
			return;
		}

		try {
			showAndLogMessage("Changing sensitivity of " + ionChamberParams.getName() + " to " + ionChamberParams.getGain());
			String[] gainStringParts = ionChamberParams.getGain().split(" ");
			moveAmplifierScannable(sensitivities, index, gainStringParts[0]);
			moveAmplifierScannable(sensitivityUnits, index, gainStringParts[1]);

			if (offsets != null && offsetUnits != null) {
				showAndLogMessage("Changing offset of " + ionChamberParams.getName() + " to " + ionChamberParams.getOffset());
				String[] offsetStringParts = ionChamberParams.getOffset().split(" ");
				moveAmplifierScannable(offsets, index, offsetStringParts[0]);
				moveAmplifierScannable(offsetUnits, index, offsetStringParts[1]);
			}
		} catch (Exception e) {
			InterfaceProvider.getTerminalPrinter().print(
					"Exception while trying to change the sensitivity of ion chamber" + ionChamberParams.getName());
			InterfaceProvider.getTerminalPrinter().print(
					"Set the ion chamber sensitivity manually, uncheck the box in the Detector Parameters editor and restart the scan");
			InterfaceProvider.getTerminalPrinter().print("Please report this problem to Data Acquisition");
			throw e;
		}
	}

	private void showAndLogMessage(String message) {
		InterfaceProvider.getTerminalPrinter().print(message);
		logger.info(message);
	}

	private void moveAmplifierScannable(Scannable[] scnArray, int index, Object position) throws DeviceException {
		if (scnArray == null || scnArray.length < index) {
			return;
		}
		logger.info("Moving {} to {}", scnArray[index].getName(), position);
		scnArray[index].moveTo(position);
	}

	public Scannable[] getSensitivities() {
		return sensitivities;
	}

	public void setSensitivities(Scannable[] sensitivities) {
		this.sensitivities = sensitivities;
	}

	public Scannable[] getSensitivityUnits() {
		return sensitivityUnits;
	}

	public void setSensitivityUnits(Scannable[] sensitivityUnits) {
		this.sensitivityUnits = sensitivityUnits;
	}

	public Scannable[] getOffsets() {
		return offsets;
	}

	public void setOffsets(Scannable[] offsets) {
		this.offsets = offsets;
	}

	public Scannable[] getOffsetUnits() {
		return offsetUnits;
	}

	public void setOffsetUnits(Scannable[] offsetUnits) {
		this.offsetUnits = offsetUnits;
	}

	public void configure(List<DetectorConfig> detectorConfigs) {
		for(DetectorConfig detectorConfig : detectorConfigs) {
			try {
				configure(detectorConfig);
			} catch (Exception e) {
				logger.error("Problem configuring detector {}. {}", detectorConfig.getDetectorName(), e.getMessage(), e);
			}
		}
	}

	public void configure(DetectorConfig detectorConfig) throws Exception {
		if (!detectorConfig.isUseDetectorInScan()) {
			logger.info("Not configuring detector {} - not selected for use for this scan", detectorConfig.getDetectorName());
			return;
		}

		logger.info("Configuring detector {}", detectorConfig.getDescription());

		Detector det = Finder.findOptionalOfType(detectorConfig.getDetectorName(), Detector.class)
				.orElseThrow(() -> new NoSuchElementException("Unable to find detector called "+detectorConfig.getDetectorName()+" on server\n") );

		if (TRUE.equals(detectorConfig.isUseScriptCommand())) {
			String scriptOrCommand = detectorConfig.getScriptCommand();
			String pathToScript = getFullPathToScript(scriptOrCommand);

			if (pathToScript == null) {
				logger.info("Running Jython command : {}", scriptOrCommand);
				InterfaceProvider.getCommandRunner().runsource(scriptOrCommand);
			} else {
				logger.info("Running {} script ({})", scriptOrCommand, pathToScript);
				GeneralCommands.run(pathToScript);
			}
		}
		if (TRUE.equals(detectorConfig.isUseConfigFile())) {
			File xmlFile = Paths.get(configFileDirectory, detectorConfig.getConfigFileName()).toFile();
			if (!xmlFile.exists()) {
				throw new FileNotFoundException("Could not find xml file " + xmlFile.getAbsolutePath()
						+ " needed to configure detector '" + detectorConfig.getDetectorName() + "'");
			} else {
				configure(det, xmlFile.getAbsolutePath());
			}
		} else {
			configure(det, "");
		}
	}

	private String getFullPathToScript(String scriptName) {
		JythonServer server = Finder.findSingleton(JythonServer.class);
		return server.getJythonScriptPaths().pathToScript(scriptName);
	}

	public Detector configure(Detector detector, String fullPathToConfigFile) throws Exception {
		File configFile = Paths.get(fullPathToConfigFile).toFile();
		if (detector instanceof FluorescenceDetector) {
			// xspress2, 3, 4 or Xmap
			if (configFile.exists()) {
				FluorescenceDetectorParameters params = (FluorescenceDetectorParameters) XMLHelpers.getBean(configFile);
				configureDetector((FluorescenceDetector) detector, params);
			}
			setupHdfWriterPath(detector);
			setConfigFilename(detector, fullPathToConfigFile);
		} else if (detector instanceof TfgScaler) {
			// some sort of ionchambers, apply gain, offset parameters
			if (configFile.exists()) {
				IonChambersBean ionchambersBean = XMLHelpers.readBean(configFile, IonChambersBean.class);
				configureIonChambers(ionchambersBean.getIonChambers());
			}
		} else if (detector instanceof NXDetector) {
			// medipix detector, set the ROI
			if (configFile.exists()) {
				MedipixParameters medipixParameters = XMLHelpers.readBean(configFile, MedipixParameters.class);
				configureDetector((NXDetector) detector, medipixParameters);
			}
			setupHdfWriterPath((NXDetector) detector);
		}
		return detector;
	}

	/**
	 * Setup medipix using ROI using MedipixParameters object.
	 * Switches detector NXPlugin list to 'mutableRoiPluginList', and sets the mutable ROI to values given by medipixParams.
	 *
	 * @param medipixParams
	 * @throws Exception
	 */
	private void configureDetector(NXDetector medipix, MedipixParameters medipixParams) throws Exception {

		// Create region using first ROI only - currently camera uses only one ROI
		RectangularROI<Integer> roi = createRectangularRoi(medipixParams.getRegionList().get(0));

		// NXPluginBase is an interface that extends NXPlugin, *not* base class implementation of NXPlugin...
		initialNXPlugins.put(medipix, medipix.getAdditionalPluginList());

		medipix.setAdditionalPluginList(mutableRoiPluginList);

		// Set the roi from the MedipixParameters
		mutableRoiForMedipix.setROI(roi);
	}

	/**
	 * Create RectangularROI from ROIRegion object
	 * @param region
	 * @return RectangularRegion created from ROIRegion parameters
	 */
	private RectangularROI<Integer> createRectangularRoi(ROIRegion region) {
		int xstart = region.getXRoi().getRoiStart();
		int xsize = region.getXRoi().getRoiEnd() - xstart;
		int ystart = region.getYRoi().getRoiStart();
		int ysize = region.getYRoi().getRoiEnd() - ystart;
		return new ImutableRectangularIntegerROI(xstart, ystart, xsize, ysize, region.getRoiName());
	}

	/**
	 * Clear the maps used to store initial NXDetector plugin lists and current Detector hdf file path settings
	 */
	public void clear() {
		initialHdfFilePaths.clear();
		initialNXPlugins.clear();
	}

	public void restoreDetectorState() {
		// Restore the original plugin list to the NXDetector(s)
		for(Entry<NXDetector, List<NXPluginBase>> entry : initialNXPlugins.entrySet()) {
			entry.getKey().setAdditionalPluginList(entry.getValue());
		}
		restoreHdfWriterPaths();
	}

	public MutableRectangularIntegerROI getMutableRoiForMedipix() {
		return mutableRoiForMedipix;
	}

	public void setMutableRoiForMedipix(MutableRectangularIntegerROI mutableRoiForMedipix) {
		this.mutableRoiForMedipix = mutableRoiForMedipix;
	}

	public List<NXPluginBase> getMutableRoiPluginList() {
		return mutableRoiPluginList;
	}

	public void setMutableRoiPluginList(List<NXPluginBase> mutableRoiPluginList) {
		this.mutableRoiPluginList = mutableRoiPluginList;
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public String getConfigFileDirectory() {
		return configFileDirectory;
	}

	public void setConfigFileDirectory(String configFileDirectory) {
		this.configFileDirectory = configFileDirectory;
	}


	public String getDirForDetectorData(String detectorName) {
		if (directoryNamesForDetectorData.containsKey(detectorName)) {
			return directoryNamesForDetectorData.get(detectorName);
		}
		return "nexus";
	}

	public void setDirForDetectorData(String detectorName, String subDirectory) {
		directoryNamesForDetectorData.put(detectorName,  subDirectory);
	}

	/**
	 * Set the hdf output directory to be used during scan on detector.
	 * @param det
	 * @throws DeviceException
	 */
	private void setupHdfWriterPath(Detector det) throws DeviceException {
		String subDirName = getDirForDetectorData(det.getName());
		String fullpath = Paths.get(dataDirectory, subDirName).toString();
		logger.debug("Setting {} hdf file path to {}", det.getName(), fullpath);
		String origPath = DetectorHdfFunctions.setHdfFilePath(det, fullpath);
		initialHdfFilePaths.put(det, origPath);
	}

	private void setupHdfWriterPath(NXDetector medipix) throws DeviceException {
		// strip off first part of path pointing to xml directory and replace with $data$
		String defaultDataDir = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		String dataDir = dataDirectory.replaceFirst(defaultDataDir, "\\$datadir\\$");
		String newPathTemplate = Paths.get(dataDir, getDirForDetectorData(medipix.getName())).toString();
		logger.debug("Setting {} hdf file path template to {}", medipix.getName(), newPathTemplate);
		String origTemplate = DetectorHdfFunctions.setHdfFilePath(medipix, newPathTemplate);
		initialHdfFilePaths.put(medipix, origTemplate);
	}

	public Map<Detector, String> getInitialHdfFilePaths() {
		return initialHdfFilePaths;
	}

	private void restoreHdfWriterPaths() {
		// Set the Hdf paths for detector(s) used in scan back to the original values
		for(Entry<Detector, String> entry : initialHdfFilePaths.entrySet()) {
			try {
				logger.info("Restoring hdf file path for {} ({})", entry.getKey().getName(), entry.getValue());
				DetectorHdfFunctions.setHdfFilePath(entry.getKey(), entry.getValue());
			} catch (DeviceException e) {
				logger.error("Problem restoring hdf file path for {}", entry.getKey().getName(), e);
			}
		}
	}

	public void configureIonChambers(List<IonChamberParameters> parmeters) throws DeviceException {
		for (int i = 0; i < parmeters.size(); i++) {
			IonChamberParameters param = parmeters.get(i);
			if (TRUE.equals(param.getChangeSensitivity())) {
				setupAmplifierSensitivity(param, i);
			}

			if (TRUE.equals(param.getAutoFillGas())) {
				// auto fill gas
			}
		}
	}
}
