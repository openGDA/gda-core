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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.DetectorParameters;
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
import uk.ac.gda.exafs.ui.data.ScanObject;
import uk.ac.gda.exafs.ui.dialogs.ParameterValuesForBean.ParameterValue;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.io.FileUtils;

public class SpreadsheetViewHelperClasses {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SpreadsheetViewHelperClasses.class);

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
	 * Return method with given name from class object; returns null if method was not found.
	 * Alternative to using clazz.getMethod(methodName) to avoid throwing exceptions.
	 * @param clazz
	 * @param methodName
	 * @return method object matching methodName; null if no matching method was found.
	 */
	public static Method getMethodWithName(Class<?> clazz, String methodName) {
		for(Method meth : clazz.getMethods()) {
			if (meth.getName().equals(methodName)) {
				return meth;
			}
		}
		return null;
	}

	/**
	 * Create new Integer, Double, Boolean, List<String> or String from supplied object depending on its contents.
	 *
	 * @param value
	 * @return New instance of object
	 */
	public static Object createNumberOrString(Object value, Class<?> requiredType) {
		String stringValue = value.toString().trim();

		if (requiredType == Integer.class || requiredType == int.class) {
			int decimalPlaceIndex = stringValue.indexOf(".");
			int lastPos = decimalPlaceIndex > 0 ? decimalPlaceIndex : stringValue.length();
			return Integer.parseInt(stringValue.substring(0, lastPos));
		} else if (requiredType == Double.class || requiredType == double.class) {
			return Double.parseDouble(stringValue);
		} else if (requiredType == Boolean.class || requiredType == boolean.class) {
			return Boolean.parseBoolean(stringValue);
		} else if (requiredType == List.class) {
			// list of strings from space separated values
			return Arrays.asList(stringValue.split("[ ]"));
		} else {
			// Not a number, assume it's a string
			return stringValue;
		}
	}

	/**
	 * Invoke named method on supplied object. The method name can chain together several method calls by separating the parts by dots.
	 * (i.e. as would be typed on Jython console to invoke method)
	 * {@code valueToSet} will be used to create a new Integer, Double or String object to pass to the method.
	 * e.g. for a {@link DetectorParameters} object with {@code pathToMethod} = getSoftXRaysParameters.setConfigFileName, it will
	 * first invoke getSoftXRaysParameters() and then setConfigName(valueToSet) on the returned object.
	 *
	 * @param obj
	 * @param pathToMethod
	 * @param valueToSet (null if not needed)
	 * @return Final return value of called method
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	public static Object invokeMethodFromName(Object obj, String pathToMethod, Object valueToSet) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Method method = null;
		String[] splitPath = pathToMethod.split("[.]");
		Object parentObject = obj;
		Object returnValue = null;
		for(int i=0; i<splitPath.length; i++) {
			method = getMethodWithName(parentObject.getClass(), splitPath[i]);
			if (method!=null) {
				if(method.getParameterCount()==1 && valueToSet!=null) {
					Object value = createNumberOrString(valueToSet, method.getParameterTypes()[0]);
					returnValue = method.invoke(parentObject, value);
				} else {
					returnValue = method.invoke(parentObject);
				}
				if (returnValue!=null) {
					parentObject = returnValue;
				}
			} else {
				logger.warn("No method found {} in class {}", splitPath[i], parentObject.getClass());
				break;
			}
		}

		return returnValue;
	}

	/**
	 * Update bean object with values from override parameters
	 *
	 * @param beanObject
	 * @param overrides
	 */
	public static void updateBeanWIthOverrides(Object beanObject, ParameterValuesForBean overrides) {
		for (ParameterValue paramOverride : overrides.getParameterValues()) {
			String fullPathToGetter = paramOverride.getFullPathToGetter();

			String fullPathToSetter;
			if (fullPathToGetter.contains(".is")) {
				fullPathToSetter = fullPathToGetter.replaceFirst(".is", ".set");
			} else if (fullPathToGetter.contains(".get")) {
				fullPathToSetter = fullPathToGetter.replaceFirst(".get", ".set");
			} else {
				fullPathToSetter = fullPathToGetter.replaceFirst("get", "set");
			}

			try {
				logger.debug("Calling method {} with value {}",  fullPathToGetter, paramOverride.getNewValue());
				invokeMethodFromName(beanObject, fullPathToSetter, paramOverride.getNewValue());
			} catch (Exception e) {
				logger.error("Problem calling method {} with value {}",  fullPathToSetter, paramOverride.getNewValue(), e);
			}
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
		List<String> fileNames = new ArrayList<String>();
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
		List<String> filesOfCorrectType = new ArrayList<String>();
		if (fileNames != null) {
			for (String fileName : fileNames) {
				try {
					String classTypeFromFile = getFirstXmlElementNameFromFile(fileName);
					if (classType.endsWith(classTypeFromFile)) {
						filesOfCorrectType.add(fileName);
					}
				} catch (Exception e) {
					logger.warn("Problem getting list of files for type {}", classType, e);
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
		String expType = detParameters.getExperimentType();
		String filename = "";
		if (expType.equals(DetectorParameters.FLUORESCENCE_TYPE)) {
			filename = detParameters.getFluorescenceParameters().getConfigFileName();
		} else if (expType.equals(DetectorParameters.XES_TYPE)) {
			filename = detParameters.getXesParameters().getConfigFileName();
		} else if (expType.equals(DetectorParameters.SOFTXRAYS_TYPE)) {
			filename = detParameters.getSoftXRaysParameters().getConfigFileName();
		}

		if (!StringUtils.isEmpty(filename)) {
			File sourceFile = new File(FilenameUtils.normalize(sourceDir+"/"+filename));
			File destFile = new File(FilenameUtils.normalize(destDir+"/"+filename));
			// Avoid overwriting source file if paths are the same...
			if (!sourceFile.getAbsolutePath().equals(destFile.getAbsolutePath())) {
				FileUtils.copy(sourceFile, destFile);
			}
		}
	}

	/**
	 * Return integer counter used when generating next set of xml scan files.
	 * Examines files in output directory with following format convention : xml name_%d_%d.xml <p>
	 *  Where 1st number = number of scan in spreadsheet table, 2nd number = integer 'counter' for each set of xmls files generated from spreadsheet view <p>
	 * and determines next integer identifier to use.
	 * @param parametersForAllScans
	 * @param outputDirectory
	 * @return integer
	 */
	public static int getXmlScanIdentifier(List<ParametersForScan> parametersForAllScans, String outputDirectory) {
		// Find maximum index of files in output directory
		//scanxmlfile_%d_%d.xml :  1st number = number of scan in spreadsheet table, 2nd number = counter to identify each set of xmls files generated
		List<String> allXmlFiles = getListOfFilesMatchingExtension(outputDirectory, ".xml");
		int maxCount = 0;
		String regexForXml = ".*_\\w_\\w.xml";
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
	 * is also refreshed, so that the newly generated multi scan scan appears in the gui.
	 * @param parent
	 * @param parametersForAllScans
	 * @param outputDirectory
	 */
	public static void generateNewScans(Composite parent, List<ParametersForScan> parametersForAllScans, String outputDirectory) {
		SpreadsheetViewHelperClasses.setupBeansFactory();
		// Iterate over each scan in the list...
		boolean forceReplaceExistingFiles = false;

		String multiScanFileContents = "";

		List<ScanObject> scanObjects = new ArrayList<>();
		try {
			ExperimentFactory.getExperimentEditorManager().getCurrentProject().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e2) {
			logger.error("Problem updating project resources", e2);
		}

		String projectFolder = ExperimentFactory.getExperimentEditorManager().getProjectFolder().toString();
		String folderInProject = outputDirectory.replace(projectFolder, "");
		IFolder folder = ExperimentFactory.getExperimentEditorManager().getIFolder(folderInProject);

		// MultiScan file format :
		// <scan name> <sampleFileName> <scanFileName> <detFileName> <outputFileName> <numRepetitions>
		int counter = getXmlScanIdentifier(parametersForAllScans, outputDirectory);

		for(int scanIndex=0; scanIndex<parametersForAllScans.size(); scanIndex++) {

			ParametersForScan parametersForScan  = parametersForAllScans.get(scanIndex);

			String sampleFileName = "", scanFileName="", detFileName="", outputFileName="";

			//Loop over each parameter file for the scan
			for(ParameterValuesForBean parameterForScanBean : parametersForScan.getParameterValuesForScanBeans()) {
				try {
					// Try to load xml bean from file
					String fullXmlPath = parameterForScanBean.getBeanFileName();
					logger.info("Reading base xml file : {}", fullXmlPath);
					Object beanObject = parameterForScanBean.getBeanObject();
					if (beanObject.getClass().getName().equals(parameterForScanBean.getBeanType())) {
						logger.debug("File matches expected type {}. Setting new values.",	parameterForScanBean.getBeanType());
						// replace values in bean with user specified values
						SpreadsheetViewHelperClasses.updateBeanWIthOverrides(beanObject, parameterForScanBean);
					}

					String baseName = FilenameUtils.getBaseName(fullXmlPath);
					String extension = FilenameUtils.getExtension(fullXmlPath);
					String fileName = String.format("%s_%d_%d.%s", baseName, scanIndex+1, counter, extension);
					String fullPathToNewXmlFile = String.format("%s/%s", outputDirectory, fileName);

					boolean writeFile = true;
					File newFile = new File(fullPathToNewXmlFile);
					if (newFile.exists() && !forceReplaceExistingFiles) {
						// customised MessageDialog with configured buttons
						if (parent!=null) {
							MessageDialog dialog = new MessageDialog(parent.getShell(), "Warning file exists", null,
									"File "+fullPathToNewXmlFile+" already exists. Replace it?", MessageDialog.QUESTION,
									new String[] { "Yes", "Yes to all", "No" }, 1);
							int result = dialog.open();
							if (result == 1) {
								forceReplaceExistingFiles = true;
							} else if (result == 2) {
								writeFile = false;
							}
						}
					}

					if (writeFile) {
						logger.info("Saving modified xml file : {}", fullPathToNewXmlFile);
						XMLHelpers.saveBean(newFile, beanObject);

						if (beanObject instanceof IDetectorParameters) {
							copyDetectorXmlFile((IDetectorParameters)beanObject, FilenameUtils.getFullPath(fullXmlPath), outputDirectory);
						}
					}

					if (beanObject instanceof ISampleParameters) {
						sampleFileName = fileName;
					} else if (beanObject instanceof IDetectorParameters) {
						detFileName = fileName;
					} else if (beanObject instanceof IScanParameters) {
						scanFileName = fileName;
					} else if (beanObject instanceof IOutputParameters) {
						outputFileName = fileName;
					}

				} catch (Exception e1) {
					logger.error("Problem when creating new xml file", e1);
				}
			}

			// Save line for writing to multi scan file. MultiScan file format :
			// <scan name> <sampleFileName> <scanFileName> <detFileName> <outputFileName> <numRepetitions>
			if (sampleFileName.isEmpty() || detFileName.isEmpty() || scanFileName.isEmpty() || outputFileName.isEmpty()) {
				logger.warn("Problem setting up multiscan for Scan_{} - parameter filename is empty!", scanIndex+1);
			}
			multiScanFileContents += String.format("Scan_%d %s %s %s %s %d\n", scanIndex+1, sampleFileName, scanFileName, detFileName, outputFileName, parametersForScan.getNumberOfRepetitions());

			// Create and store a ScanObject for the scan, so parameters can be validated
			ScanObject scanObject = new ScanObject();
			scanObject.setDetectorFileName(detFileName);
			scanObject.setSampleFileName(sampleFileName);
			scanObject.setScanFileName(scanFileName);
			scanObject.setOutputFileName(outputFileName);
			scanObject.setNumberRepetitions(1);
			scanObject.setFolder(folder);
			scanObjects.add(scanObject);
		}

		//Write multiscan file
		String multiscanFilePath = String.format("%s/%s_%d.scan", outputDirectory, "MultipleScan_spreadsheet", counter);
		try (BufferedWriter bufWriter = new BufferedWriter(new FileWriter(multiscanFilePath))) {
			bufWriter.write(multiScanFileContents);
			bufWriter.close();
		} catch (IOException e) {
			logger.error("Problem writing multi-scan file {}", multiscanFilePath, e);
		}

		AbstractValidator validator = ExperimentFactory.getValidator();
		// folder might not view viewable to eclipse if it was created outside of resource path.
		// In this case the validator won't be able to see the xml files...
		if (validator != null && folder.exists()) {

			// Refresh directory so that resources is aware of the newly created files
			try {
				folder.refreshLocal(IResource.DEPTH_ONE, null);
			} catch (CoreException e1) {
				logger.error("Problem updating project resources", e1);
			}

			String messages = "";
			int scanNumber = 0;
			for(ScanObject scnObject : scanObjects) {
				scanNumber++;
				try {
					validator.validate(scnObject);
				} catch (InvalidBeanException e) {
					messages += "Scan "+scanNumber+":"+e.getMessage()+"\n";
				}
			}
			if (messages.length()>0) {
				MessageDialog.openInformation(parent.getShell(), "Error(s) in XML file(s)", messages);
			}
		}

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

	/**
	 * Determine which sample stages to be moved based on selected parameters.
	 * e.g. Axis2 means that userstage sample stage should be added to list of selected sample stages.
	 * @param selectedParams
	 * @return
	 */
	private static ParameterValue getSampleStageListOverride(ParameterValuesForBean selectedParams) {
		if (selectedParams.getBeanType().equals(B18SampleParameters.class.getName())) {
			ParameterValue selectedSampleStageParam = new ParameterValue("getSelectedSampleStages", "");
			selectedSampleStageParam.setEditable(false);
			String selectedSampleStages = "";
			for(ParameterValue val : selectedParams.getParameterValues()) {
				String stage = SpreadsheetViewHelperClasses.containsIgnoreCase(val.getFullPathToGetter(), B18SampleParameters.STAGE);
				if (!selectedSampleStages.contains(stage)) {
					selectedSampleStages +=" "+stage;
				}
			}
			if (!selectedSampleStages.isEmpty()) {
				selectedSampleStageParam.setNewValue(selectedSampleStages.trim());
			}
			return selectedSampleStageParam;
		}
		return null;
	}

	/**
	 * Set temperature controller to be used for scan by looking which temperature control parameters have been selected.
	 *
	 * @param paramValuesForScans
	 * @return
	 */
	private static ParameterValue getTemperatureControlOverride(ParameterValuesForBean paramValuesForScans) {
		if (paramValuesForScans.getBeanType().equals(B18SampleParameters.class.getName())) {
			ParameterValue selectedSampleStageParam = new ParameterValue("getTemperatureControl", "");
			selectedSampleStageParam.setEditable(false);
			for(ParameterValue val : paramValuesForScans.getParameterValues()) {
				String control = SpreadsheetViewHelperClasses.containsIgnoreCase(val.getFullPathToGetter(), B18SampleParameters.TEMP_CONTROL);
				if (!control.isEmpty()) {
					selectedSampleStageParam.setNewValue(control.trim());
					break;
				}
			}
			return selectedSampleStageParam;
		}
		return null;
	}

	public static void addRemoveParameters(List<ParametersForScan> parametersForAllScans, List<ParameterValuesForBean> newSelectedParameters) {
		for(ParametersForScan paramForScan : parametersForAllScans) {
			addRemoveParmeters(paramForScan, newSelectedParameters);
		}
	}

	/**
	 * Add/remove
	 * @param parametersForScan
	 * @param newSelectedParameters
	 */
	public static void addRemoveParmeters(ParametersForScan parametersForScan, List<ParameterValuesForBean> newSelectedParameters) {
		if (newSelectedParameters.size() == 0) {
			for(ParameterValuesForBean selectedParam : parametersForScan.getParameterValuesForScanBeans()) {
				selectedParam.getParameterValues().clear();
			}
		} else {
			for (ParameterValuesForBean selectedParam : newSelectedParameters) {
				for(ParameterValuesForBean paramForScanBean : parametersForScan.getParameterValuesForScanBeans()) {
					addRemoveParameters(paramForScanBean, selectedParam);
				}
			}
		}
	}

	public static void addRemoveParameters(ParameterValuesForBean paramsForScanBean, ParameterValuesForBean newSelectedParameters) {

		// only update matching type of bean
		if (paramsForScanBean.getBeanType().equals(newSelectedParameters.getBeanType())) {

			Map<String, Object> beanValues = new HashMap<>();
			try {
				Object beanObject = paramsForScanBean.getBeanObject();
				beanValues = newSelectedParameters.getValuesFromBean(beanObject);
			} catch (Exception e) {
				logger.warn("Problem getting values from {} in updateTableParameters", paramsForScanBean.getBeanFileName(), e);
			}

			// Remove any *bean parameters* not in *selected parameter* list
			Iterator<ParameterValue> iteratorForBean = paramsForScanBean.getParameterValues().iterator();
			while(iteratorForBean.hasNext()) {
				ParameterValue p = iteratorForBean.next();
				if (newSelectedParameters.getParameterValue(p.getFullPathToGetter())==null) {
					iteratorForBean.remove();
				}
			}
			// Add new parameter to bean, (i.e. for selected parameter not already in bean)
			for(ParameterValue selectedParam : newSelectedParameters.getParameterValues()) {
				if( paramsForScanBean.getParameterValue(selectedParam.getFullPathToGetter()) == null ) {
					ParameterValue newParam = new ParameterValue(selectedParam);
					Object valueFromBean = beanValues.get(selectedParam.getFullPathToGetter());
					if (valueFromBean != null) {
						newParam.setNewValue(valueFromBean);
					}
					logger.debug("updateTableParameters on {} bean : parameter = {}, value = {}", paramsForScanBean.getBeanType(),selectedParam.getFullPathToGetter(), valueFromBean);
					paramsForScanBean.addParameterValue(newParam);
				}
			}
			addSampleStageTemperatureParams(paramsForScanBean, newSelectedParameters);
		}
	}

	/**
	 * Add parameters with string with list of selected sample stages, name of temperature controller
	 * @param parametersForScan
	 * @param newSelectedParameters
	 */
	public static void addSampleStageTemperatureParams(ParameterValuesForBean parametersForScan, ParameterValuesForBean newSelectedParameters){

		if (!parametersForScan.getBeanType().equals(B18SampleParameters.class.getName())) {
			return;
		}

		ParameterValue sampleStageList = getSampleStageListOverride(newSelectedParameters);
		ParameterValue temperatureControl = getTemperatureControlOverride(newSelectedParameters);

		// remove any current samplestage list(s)
		Iterator<ParameterValue> iterator =  parametersForScan.getParameterValues().iterator();
		while (iterator.hasNext()) {
			ParameterValue p = iterator.next();
			String getter = p.getFullPathToGetter();
			if (getter.equals(sampleStageList.getFullPathToGetter()) ||
				getter.equals(temperatureControl.getFullPathToGetter()) ) {
				iterator.remove();
			}
		}
		// set the new value (empty string means no sample stages selected, don't override anything)
		if (!sampleStageList.getNewValue().equals("")) {
			parametersForScan.addParameterValue(sampleStageList);
		}
		if (!temperatureControl.getNewValue().equals("")) {
			parametersForScan.addParameterValue(temperatureControl);
		}
	}

	/**
	 * @param str string to be checked
	 * @param searchStrs array of strings to find
	 * @return matching string from searchStrs array found by repeatedly applying {@link StringUtils#containsIgnoreCase(String, String)}.
	 */
	public static String containsIgnoreCase(String str, String[] searchStrs) {
		for(int i=0; i<searchStrs.length; i++) {
			if (StringUtils.containsIgnoreCase(str, searchStrs[i])) {
				return searchStrs[i];
			}
		}
		return "";
	}

	public static String endsWith(String str, String[] searchStrs) {
		for(int i=0; i<searchStrs.length; i++) {
			if (searchStrs[i].endsWith(str)) {
				return searchStrs[i];
			}
		}
		return "";
	}

	/**
	 * Scan all required xml files for the scans and make sure they exist
	 * @param parametersForAllScans
	 * @return Warning message if required files are missing, empty string otherwise
	 */
	public static String checkRequiredXmlsExist(List<ParametersForScan> parametersForAllScans) {
		String warningMessage = "";
		for(int scanIndex=0; scanIndex<parametersForAllScans.size(); scanIndex++) {
			ParametersForScan parametersForScan  = parametersForAllScans.get(scanIndex);
			//Loop over each parameter file for the scan
			for(ParameterValuesForBean parameterForScanBean : parametersForScan.getParameterValuesForScanBeans()) {
				String fullXmlPath = parameterForScanBean.getBeanFileName();
				File xmlFile = new File(fullXmlPath);
				if ( !xmlFile.exists() || !xmlFile.isFile() ) {
					warningMessage += "Scan "+scanIndex+" : file '"+fullXmlPath+"' cannot be read\n";
				}
			}
		}
		return warningMessage;
	}
}
