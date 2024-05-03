/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.dialogs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.LoggerFactory;

import gda.exafs.scan.ScanObject;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.beans.exafs.FluorescenceParameters;
import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IOutputParameters;
import uk.ac.gda.beans.exafs.ISampleParameters;
import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.exafs.QEXAFSParameters;
import uk.ac.gda.beans.exafs.b18.B18SampleParameters;
import uk.ac.gda.beans.validation.AbstractValidator;
import uk.ac.gda.beans.validation.InvalidBeanException;
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ui.handlers.RefreshProjectCommandHandler;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.io.FileUtils;

public class SpreadsheetViewHelperClasses {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetViewHelperClasses.class);

	/** Names of 'getter' methods in {@link OutputParameters} bean that return names of before/after scan scripts */
	public static final String BEFORE_SCAN_SCRIPT = "getBeforeScriptName";
	public static final String AFTER_SCAN_SCRIPT = "getAfterScriptName";
	private static final String BEFORE_FIRST_REPETITION_SCRIPT = "getBeforeFirstRepetition";
	public static final String GETTER_FOR_SCRIPT_COMMAND = "getScriptCommand";

	public static final String SCRIPT_NAME_GETTER_REGEX = "(" + SpreadsheetViewHelperClasses.BEFORE_SCAN_SCRIPT + "|"
			+ SpreadsheetViewHelperClasses.AFTER_SCAN_SCRIPT + "|"
			+ SpreadsheetViewHelperClasses.BEFORE_FIRST_REPETITION_SCRIPT + "|"
			+ "\\S+" + SpreadsheetViewHelperClasses.GETTER_FOR_SCRIPT_COMMAND + ")";

	/** Name of the 'getter' method in {@link FluorescenceParameters} or {@link DetectorConfig}
	 * bean that returns name of the detector configuration file */
	public static final String GETTER_FOR_DETECTOR_FILE = ".getConfigFileName";

	private SpreadsheetViewHelperClasses() {
	}

	/**
	 * Setup BeansFactory so that {@link XMLHelpers} functions can be used marshall, unmarshall objects to/from xml files.
	 */
	public static void setupBeansFactory() {
		try{
			// if not initialised, throws null pointer exception...
			BeansFactory.getClasses();
		}catch(NullPointerException npe) {
			// Setup the classes to be used
			Class<?>[] classes = new Class<?>[]{B18SampleParameters.class, QEXAFSParameters.class, DetectorParameters.class,
				OutputParameters.class, XspressParameters.class, Xspress3Parameters.class};
			BeansFactory.setClasses(classes);
		}
	}

	/**
	 * Examine named file and return name of first xml element (ignoring the header)
	 * @param filename
	 * @return name of first xml element
	 * @throws IOException
	 */
	public static String getFirstXmlElementNameFromFile(String filename) throws IOException {
		try(BufferedReader bufferedReader = new BufferedReader( new FileReader(filename) ) ) {
			String elementLineString = bufferedReader.readLine();
			if (!StringUtils.isEmpty(elementLineString) && elementLineString.startsWith("<?xml")) { //skip the xml header line
				elementLineString = bufferedReader.readLine();
				return elementLineString.replaceAll("[<>]", "");
			}
			return "";
		} catch (IOException e) {
			logger.error("Problem extracting element from file {}", filename, e);
			return "";
		}
	}

	/**
	 * Get list of files in named directory that end with specified extension
	 * @param dirPath
	 * @param extension
	 * @return list of matching files
	 */
	public static List<String> getListOfFilesMatchingExtension(String dirPath, String extension) {
		File folder = new File(dirPath);
		if (!folder.exists()) {
			logger.warn("Unable to find directory {}",dirPath);
		}
		List<String> fileNames = new ArrayList<>();
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles!=null) {
			for (File file : listOfFiles) {
				if (file.getName().endsWith(extension)) {
					fileNames.add(file.getPath());
				}
			}
		}
		return fileNames;
	}

	/**
	 * Examine each of named files to see if name of main xml element matches specified classType.
	 * See also {@link #getFirstXmlElementNameFromFile(String)}.
	 * @param fileNames string list with full path to each xml
	 * @param classType
	 * @return classType string list of files with bean matching specified type
	 */
	public static List<String> getListOfFilesMatchingType(List<String> fileNames, String classType) {
		return getListOfFilesMatchingTypes(fileNames, Arrays.asList(classType));
	}

	public static List<String> getListOfFilesMatchingTypes(List<String> fileNames, List<String> classTypes) {
		List<String> filesOfCorrectType = new ArrayList<>();
		if (fileNames != null) {
			for (String fileName : fileNames) {
				try {
					String classTypeFromFile = getFirstXmlElementNameFromFile(fileName);
					Optional<String> result = classTypes
							.stream()
							.filter(classType -> classType.endsWith(classTypeFromFile))
							.findFirst();

					if (result.isPresent()) {
						filesOfCorrectType.add(fileName);
					}
				} catch (Exception e) {
					logger.warn("Problem getting list of files for types {}", Arrays.asList(classTypes), e);
				}
			}
		}
		Collections.sort(filesOfCorrectType); // sort to alphabetical order
		return filesOfCorrectType;
	}

	/**
	 * Copy fluorescence detector xml settings file; name of file taken from value in detector parameters.xml
	 * @param detParameters
	 * @param sourceDir
	 * @param destDir
	 * @throws IOException
	 */
	private static void copyDetectorXmlFile(IDetectorParameters detParameters, String sourceDir, String destDir) throws IOException {
		List<String> detectorConfigFiles = new ArrayList<>();

		if (!detParameters.getDetectorConfigurations().isEmpty()) {
			// Create list of files from selected detectors that use a config file
			detectorConfigFiles = detParameters.getDetectorConfigurations().stream()
									.filter(DetectorConfig::isUseDetectorInScan)
									.filter(DetectorConfig::isUseConfigFile)
									.map(DetectorConfig::getConfigFileName)
									.toList();
		} else {
			// Get the config filename from the selected experiment type
			String expType = detParameters.getExperimentType();
			String filename = "";
			if (expType.equals(DetectorParameters.FLUORESCENCE_TYPE)) {
				filename = detParameters.getFluorescenceParameters().getConfigFileName();
			} else if (expType.equals(DetectorParameters.XES_TYPE)) {
				filename = detParameters.getXesParameters().getConfigFileName();
			} else if (expType.equals(DetectorParameters.SOFTXRAYS_TYPE)) {
				filename = detParameters.getSoftXRaysParameters().getConfigFileName();
			}
			detectorConfigFiles = List.of(filename);
		}

		if (detectorConfigFiles.isEmpty()) {
			return;
		}

		// Copy the detector config file(s)
		for(String filename : detectorConfigFiles) {
			File sourceFile = Paths.get(sourceDir, filename).toFile();
			File destFile = Paths.get(destDir, filename).toFile();
			// Avoid overwriting source file if paths are the same...
			if (!sourceFile.getAbsolutePath().equals(destFile.getAbsolutePath())) {
				logger.info("Copying detector file from {} to {}", sourceFile, destFile);
				FileUtils.copy(sourceFile, destFile);
			}else {
				logger.info("Not copying detector file - source and destination directories ({}) are the same.", sourceFile, destFile);
			}
		}
	}

	/**
	 * Return integer counter used when generating next set of xml scan files.
	 * Examines files in output directory named according to format convention :
	 * <p> xml name_%d_%d.xml <p>
	 * (Where 1st number = number of scan in spreadsheet table, 2nd number = integer 'counter' for each set of xmls files generated from spreadsheet view)
	 * to find the largest counter (2nd number).
	 * Value returned is largest counter value parsed from filenames + 1.
	 * @param outputDirectory
	 * @return integer
	 */
	private static int getXmlScanIdentifier(String outputDirectory) {
		// Find maximum index of files in output directory
		//scanxmlfile_%d_%d.xml :  1st number = number of scan in spreadsheet table, 2nd number = counter to identify each set of xmls files generated
		List<String> allXmlFiles = getListOfFilesMatchingExtension(outputDirectory, ".xml");
		int maxCount = 0;
		String regexForXml = ".*_\\d+_\\d+.xml";
		for(String filename : allXmlFiles) {
			if (filename.matches(regexForXml)) {
				// extract integer identifier from filename (i.e. integer after last '_')
				String name = FilenameUtils.getBaseName(filename);
				int ind = name.lastIndexOf("_");
				String stringNum = name.substring(ind+1);
				try {
					int intNum = Integer.parseInt(stringNum);
					maxCount = Math.max(maxCount, intNum);
					logger.debug("Number from {} = {}", name, intNum);
				}catch(NumberFormatException nfe) {
					logger.debug("Problem extracting integer number from {}", name, nfe);
				}
			}
		}
		return maxCount+1;
	}

	/**
	 * Create new set of XML files for each scan defined in List of ParametersFoScan, along with corresponding 'multi scan' file.
	 * For each scan :
	 * <li> Read the base xml files and generate  beans with Scan, Detector, Sample, Output settings).
	 * <li> Set the new values on the beans from ParametersForScan list.
	 * <li> Save the modified beans to new xml files. Also copy the detector xml file to the new directory location. </li>
	 * The output filenames are named using the format {@code <base xml name>_<scan index>_<counter>.xml }, where 'scan index' is
	 * number of scan in the list being processed, and 'counter' is an integer to make the filename unique wrt other
	 * sets of scan xml files in the output directory.  This makes it possible to output to the same directory location many
	 * times and not overwrite any files.
	 * A new 'multi' scan file is also generated - this is a list of scans and their xml files for the set of scans. The experiment explorer tree view
	 * is also refreshed, so that the newly generated scans appear in the gui.
	 * @param parent
	 * @param parametersForAllScans
	 * @param outputDirectory
	 */
	public static void generateNewScans(Composite parent, List<ParametersForScan> parametersForAllScans, String outputDirectory) {
		SpreadsheetViewHelperClasses.setupBeansFactory();
		// Iterate over each scan in the list...
		boolean forceReplaceExistingFiles = false;
		boolean useSampleNameForMultiscan = true; // include sample name in scan name (might want to set this from the GUI)

		try {
			ExperimentFactory.getExperimentEditorManager().getCurrentProject().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e2) {
			logger.error("Problem updating project resources", e2);
		}

		int counter = getXmlScanIdentifier(outputDirectory);
		List<ScanObject> scanObjects = new ArrayList<>();
		StringBuilder creationErrorMessages = new StringBuilder();
		for(int scanNumberIndex=0; scanNumberIndex<parametersForAllScans.size(); scanNumberIndex++) {

			ParametersForScan parametersForScan  = parametersForAllScans.get(scanNumberIndex);

			String sampleName = "";
			Map<String, Object> scanBeans = new HashMap<>();

			// Make new xml files using modified parameters
			for(ParameterValuesForBean parameterForScanBean : parametersForScan.getParameterValuesForScanBeans()) {
				// Full path to source xml bean file
				String fullPathToSourceXmlFile = parameterForScanBean.getBeanFileName();

				// Make full path to new xml file
				String baseName = FilenameUtils.getBaseName(fullPathToSourceXmlFile);
				String extension = FilenameUtils.getExtension(fullPathToSourceXmlFile);
				String fileName = String.format("%s_%d_%d.%s", baseName, scanNumberIndex+1, counter, extension);
				String fullPathToNewXmlFile = Paths.get(outputDirectory, fileName).toString();

				// Check if xml file already exists at output location
				boolean writeFile = true;
				int result = forceReplaceExistingFiles ? 0 : fileExistsDialog(fullPathToNewXmlFile, parent);
				if (result == 0 ) {
					writeFile = true;
				} else if (result == 1) {
					forceReplaceExistingFiles = true;
				} else if (result == 2) {
					writeFile = false;
				}

				try {
					if (writeFile) {
						logger.info("Reading base xml file from {} and setting new values", fullPathToSourceXmlFile);
						Object beanObject = parameterForScanBean.getModifiedBeanObject();

						logger.info("Saving modified xml file to {}", fullPathToNewXmlFile);
						XMLHelpers.saveBean(new File(fullPathToNewXmlFile), beanObject);

						// Copy the detector config. file to the new directory if necessary
						if (beanObject instanceof IDetectorParameters detParams) {
							copyDetectorXmlFile(detParams, FilenameUtils.getFullPath(fullPathToSourceXmlFile), outputDirectory);
						}
						scanBeans.put(fileName, beanObject);
					}

				} catch (Exception e1) {
					String message = "Problem when creating new xml file from "+ fullPathToSourceXmlFile +
							" : "+ e1.getMessage();
					creationErrorMessages.append(message +"\n");
					logger.error(message, e1);
				}
			}

			// Create and store a ScanObject for the scan : used for creating command used in multi scan file, and validating parameters
			ScanObject scanObject = createScanObject(scanBeans);
			// Set sample name, so we can use it in the scan name in the multi-scan file
			if (useSampleNameForMultiscan) {
				ISampleParameters sampleParameters = (ISampleParameters) scanBeans.get(scanObject.getSampleFileName());
				if (sampleParameters != null) {
					sampleName = "_"+sampleParameters.getName();
				}
			}
			scanObject.setRunName(String.format("Scan_%d%s", scanNumberIndex + 1, sampleName));
			scanObject.setNumberRepetitions(parametersForScan.getNumberOfRepetitions());
			scanObjects.add(scanObject);
		}

		if (!creationErrorMessages.isEmpty()) {
			MessageDialog.openInformation(parent.getShell(), "Error(s) found creating new XML file(s)", creationErrorMessages.toString());
		}
		//Write multiscan file
		String multiscanFilePath = String.format("%s/%s_%d.scan", outputDirectory, "MultipleScan_spreadsheet", counter);
		createMultiScanFile(scanObjects, multiscanFilePath);

		// Validate the scan parameters
		String messages = SpreadsheetViewHelperClasses.validateScanBeans(scanObjects, outputDirectory);
		if (!messages.isEmpty()) {
			MessageDialog.openInformation(parent.getShell(), "Error(s) found validating XML file(s)", messages);
		}

		// Refresh experiment explorer view to show the newly created files
		try {
			// Clear everything out before starting, otherwise refresh for folders already in the view are not consistent with disk & file contents.
			ExperimentFactory.emptyManagers();
			RefreshProjectCommandHandler refreshCommand = new RefreshProjectCommandHandler();
			refreshCommand.execute(null);
		} catch (ExecutionException e) {
			logger.error("Problem refreshing experiment view", e);
		}
		ExperimentFactory.getExperimentEditorManager().refreshViewers();
	}

	private static void createMultiScanFile(List<ScanObject> scanObjects, String multiscanFilePath) {
		try (BufferedWriter bufWriter = new BufferedWriter(new FileWriter(multiscanFilePath))) {
			for (ScanObject scanObject : scanObjects) {
				String scanString = scanObject.toPersistenceString() + "\n";
				if (scanString.split(" ").length != 6) {
					logger.warn("Possible problem adding scan {}. Incorrect number of parameters ({})",	scanObject.getRunName(), scanString);
				}
				bufWriter.write(scanString);
			}
		} catch (IOException e) {
			logger.error("Problem writing multi-scan file {}", multiscanFilePath, e);
		}
	}

	/**
	 * Populate fields of ScanBean object. Uses 'instanceof' to determine
	 * which filename to set for each bean object.
	 * @param scanBeans key = bean object, value = name of xml file
	 * @return ScanBean
	 */
	private static ScanObject createScanObject(Map<String, Object> scanBeans) {
		ScanObject scanObject = new ScanObject();

		for(Entry<String, Object> e : scanBeans.entrySet()) {
			String fileName = e.getKey();
			Object beanObject = e.getValue();
			// Set xml filename for each type of bean
			if (beanObject instanceof ISampleParameters) {
				scanObject.setSampleFileName(fileName);
			} else if (beanObject instanceof IDetectorParameters) {
				scanObject.setDetectorFileName(fileName);
			} else if (beanObject instanceof IScanParameters) {
				scanObject.setScanFileName(fileName);
			} else if (beanObject instanceof IOutputParameters) {
				scanObject.setOutputFileName(fileName);
			}
		}
		return scanObject;
	}

	/**
	 * Open MessageDialog to ask user what to do if named file already exists on filesystem
	 * @param fullPathToNewXmlFile
	 * @param parent
	 * @return 0 = write file, 1 = write all files, 2 = don't write
	 */
	private static int fileExistsDialog(String fullPathToNewXmlFile, Composite parent) {
		File newFile = new File(fullPathToNewXmlFile);
		if (newFile.exists() && parent != null) {
			// customised MessageDialog with configured buttons
			MessageDialog dialog = new MessageDialog(parent.getShell(), "Warning file exists", null,
					"File "+fullPathToNewXmlFile+" already exists. Replace it?", MessageDialog.QUESTION,
					new String[] { "Yes", "Yes to all", "No" }, 1);
			return dialog.open();
		} else {
			return 0;
		}
	}
	/**
	 * Validate the scan objects (i.e. performs same checks on scan beans as when adding scan to command queue)
	 * @param scanObjects
	 * @return
	 */
	private static String validateScanBeans(List<ScanObject> scanObjects, String outputFolder) {
		if (scanObjects.isEmpty()) {
			return "";
		}

		AbstractValidator validator = ExperimentFactory.getValidator();
		StringBuilder messages = new StringBuilder();

		// Get the IFolder object of directory containing the scan beans - required for running 'validate' method
		String projectFolder = ExperimentFactory.getExperimentEditorManager().getProjectFolder().toString();
		String folderInProject = outputFolder.replace(projectFolder, ""); // relative path to output directory
		IFolder folder = ExperimentFactory.getExperimentEditorManager().getIFolder(folderInProject);

		// Folder might not view viewable to eclipse if it was created outside of resource path.
		// In this case the validator won't be able to see the xml files...
		if (validator != null && folder.exists()) {

			// Refresh directory so that resources is aware of the newly created files
			try {
				folder.refreshLocal(IResource.DEPTH_ONE, null);
			} catch (CoreException e1) {
				logger.error("Problem updating project resources", e1);
			}

			int scanNumber = 0;
			for(ScanObject scnObject : scanObjects) {
				scanNumber++;
				try {
					scnObject.setFolder(folder);
					validator.validate(scnObject);
				} catch (InvalidBeanException e) {
					messages.append("Scan "+scanNumber+":"+e.getMessage()+"\n");
				}
			}
		}
		return messages.toString();
	}

	/**
	 * Sort all model modifiers into alphabetical order
	 */
	public static void sortModelModifiers(List<ParametersForScan> overridesForScanFiles) {
		for(ParametersForScan scanOverride : overridesForScanFiles) {
			for(ParameterValuesForBean paramFile : scanOverride.getParameterValuesForScanBeans()) {
				paramFile.sort();
			}
		}
	}

	public static void addRemoveParameters(List<ParametersForScan> parametersForAllScans, List<ParameterValuesForBean> newSelectedParameters) {
		for(ParametersForScan paramForScan : parametersForAllScans) {
			addRemoveParameters(paramForScan, newSelectedParameters);
		}
	}

	/**
	 * Add/remove current ParameterValues for beans in each scan based on list in 'newParametersForScan'
	 * Remove parameters not in newParametersForScan and add any new ones.
	 * @param currentParametersForScan
	 * @param newParametersForScan
	 */
	private static void addRemoveParameters(ParametersForScan currentParametersForScan, List<ParameterValuesForBean> newParametersForScan) {
		for (ParameterValuesForBean currentBeanParams : currentParametersForScan.getParameterValuesForScanBeans()) {

			// Find scan bean settings object with type matching new selected parameter
			Optional<ParameterValuesForBean> result = newParametersForScan
					.stream()
					.filter( currentParamForBean -> currentBeanParams.getBeanType().equalsIgnoreCase(currentParamForBean.getBeanType()))
					.findFirst();

			if (result.isPresent()) {
				logger.debug("addRemoveParameters : Adding parameters of type {} to current setting.", currentBeanParams.getBeanType());
				addRemoveParameters(currentBeanParams, result.get());
			} else {
				// clear parameters if no new parameters specified for this bean type
				logger.debug("addRemoveParameters : No new parameters of type {} given - clearing parameters from current setting.", currentBeanParams.getBeanType());
				currentBeanParams.getParameterValues().clear();
			}
		}
	}

	private static void addRemoveParameters(ParameterValuesForBean currentParametersForBean, ParameterValuesForBean newParametersForBean) {

		// Return early if bean types do not match
		if (!currentParametersForBean.getBeanType().equals(newParametersForBean.getBeanType())) {
			logger.warn("Cannot adjust list of parameters for bean. Bean type {} does not match type supplied parameter type {}",
					currentParametersForBean.getBeanType(), newParametersForBean.getBeanType());
			return;
		}

		// Get default values of new parameters to be set (i.e. values from bean)
		Map<String, Object> currentBeanValues = new HashMap<>(); //key = path to getter, value = value from bean
		try {
			Object beanObject = currentParametersForBean.getBeanObject();
			currentBeanValues = newParametersForBean.getValuesFromBean(beanObject);
		} catch (Exception e) {
			logger.warn("Problem getting values from {} in updateTableParameters", currentParametersForBean.getBeanFileName(), e);
		}

		// Remove any parameter not present in the new ParameterValues list
		currentParametersForBean.getParameterValues()
			.removeIf(paramValue -> newParametersForBean.getParameterValue(paramValue)==null);

		// Add new parameter if not already present.
		final Map<String, Object> beanValues = currentBeanValues;
		newParametersForBean.getParameterValues()
				.stream()
				.filter(newParam -> currentParametersForBean.getParameterValue(newParam) == null)
				.forEach(newParam -> {
					ParameterValue newParamToBeAdded = new ParameterValue(newParam);
					// Set the value of parameter using current value from bean.
					Object defaultValue = beanValues.get(newParam.getFullPathToGetter());
					if (defaultValue != null) {
						newParamToBeAdded.setNewValue(defaultValue);
					}
					logger.debug("updateTableParameters on {} bean : parameter = {}, value = {}",
							currentParametersForBean.getBeanType(), newParamToBeAdded.getFullPathToGetter(), newParamToBeAdded.getNewValue());
					currentParametersForBean.addParameterValue(newParamToBeAdded);
			});
	}
}
