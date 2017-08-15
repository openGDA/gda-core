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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyLookupFactory;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
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
import uk.ac.gda.beans.vortex.Xspress3Parameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.ui.handlers.RefreshProjectCommandHandler;
import uk.ac.gda.exafs.ui.dialogs.OverridesForParametersFile.ParameterOverride;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class OverrideTableClasses {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OverrideTableClasses.class);

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
	 * @param name
	 * @return class matching given name; null if ClassNotFoundException
	 */
	public static Class<?> getClassWithName(String name) {
		Class<?> classForName = null;
		try {
			classForName = Class.forName(name);
		} catch (ClassNotFoundException e) {
			logger.error("Problem creating class for {}", name, e);
		}
		return classForName;
	}

	/**
	 * Create new Integer, Double or String from supplied object depending on its contents.
	 * @param value
	 * @return New instance of Integer, Double, or String
	 */
	public static Object createNumberOrString(Object value, Class<?> requiredType) {
		String stringValue = value.toString().trim();
		String stringDigits = stringValue.replace(".", "");
		boolean valueIsNumeric = StringUtils.isNumericSpace(stringDigits);

		if (valueIsNumeric) {
			if (requiredType==Integer.class || requiredType==int.class) {
				int decimalPlaceIndex = stringValue.indexOf(".");
				int lastPos = decimalPlaceIndex>0 ? decimalPlaceIndex : stringValue.length();
				return Integer.parseInt(stringValue.substring(0, lastPos));
			} else if (requiredType==Double.class || requiredType==double.class) {
				return Double.parseDouble(stringValue);
			}
		}
		//Not a number, assume it's a string
		return stringValue;
	}

	/**
	 * Invoke named method on supplied object. The method name can chain together several method calls by separating the parts by dots.
	 * (i.e. as one would type on Jython console)
	 * {@code valueToSet} will be used to create a new Integer, Double or String object to pass to the method.
	 * e.g. for a {@link DetectorParameters} object with {@code pathToMethod} = getSoftXRaysParameters.setConfigFileName, it will
	 * first invoke getSoftXRaysParameters() and then setConfigName(valueToSet) on the returned object.
	 *
	 * @param obj
	 * @param pathToMethod
	 * @param valueToSet
	 * @return Final return value of called method
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object invokeMethodFromName(Object obj, String pathToMethod, Object valueToSet) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
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
	 * @param beanObject
	 * @param overrides
	 */
	public static void updateBeanWIthOverrides(Object beanObject, OverridesForParametersFile overrides) {
		for(ParameterOverride paramOverride : overrides.getOverrides() ) {
			String fullPathToGetter = paramOverride.getFullPathToGetter();
			int index = fullPathToGetter.lastIndexOf("get");

			// Make path to setter function
			StringBuilder setterMethodPath = new StringBuilder();
			setterMethodPath.append( fullPathToGetter.substring(0,index) );
			setterMethodPath.append("set");
			setterMethodPath.append( fullPathToGetter.substring(index+3));
			String fullPathToSetter = setterMethodPath.toString();

			try {
				logger.debug("Calling method {} with value {}",  fullPathToSetter, paramOverride.getNewValue());
				invokeMethodFromName(beanObject, fullPathToSetter, paramOverride.getNewValue());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
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
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader( new FileReader(filename) );
			String elementLineString = bufferedReader.readLine();
			if (elementLineString.startsWith("<?xml")) { //skip the xml header line
				elementLineString = bufferedReader.readLine();
			}

			return elementLineString.replaceAll("[<>]", "");
		} catch (IOException e) {
			logger.error("Problem extracting element from file {}", filename, e);
			return "";
		} finally {
			if (bufferedReader!=null) {
				bufferedReader.close();
			}
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
		return filesOfCorrectType;
	}

	// Magic tweaks so to allow cursor keys to be used to navigate between elements in the table...
	// (from Snippet035TableCursorCellHighlighter)
	public static void setupForCursorNavigation(TableViewer v) {
		TableViewerFocusCellManager focusCellManager = new TableViewerFocusCellManager(v, new FocusCellOwnerDrawHighlighter(v));
		ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(v) {
			@Override
			protected boolean isEditorActivationEvent(ColumnViewerEditorActivationEvent event) {
				// TODO see AbstractComboBoxCellEditor for how list is made visible
				return super.isEditorActivationEvent(event)
						|| (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED
						&& (event.keyCode == KeyLookupFactory.getDefault().formalKeyLookup(IKeyLookup.ENTER_NAME)));
			}
		};

		int features = ColumnViewerEditor.TABBING_HORIZONTAL | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
					 | ColumnViewerEditor.TABBING_VERTICAL | ColumnViewerEditor.KEYBOARD_ACTIVATION;

		TableViewerEditor.create(v, focusCellManager, actSupport, features);
	}

//	public static void refreshExperimentEditorTree() {
//
//		final IExperimentEditorManager man = ExperimentFactory.getExperimentEditorManager();
//		if (man == null)
//			return;
//
//		final IProject project = man.getCurrentProject();
//		try {
//			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
//		} catch (CoreException e) {
//		}
//
//		// If ExperimentExperimentView selected, refresh that.
//		final ExperimentExperimentView ev = man.getViewer();
//		if (ev != null)
//			ev.refreshTree();
//
//		final ExperimentFolderEditor fe = man.getActiveFolderEditor();
//		if (fe != null)
//			fe.refresh();
//	}

	public static void generateNewScans(Composite parent, List<OverridesForScan> overridesForScanFiles, String outputDirectory) {
		OverrideTableClasses.setupBeansFactory();
		// Iterate over each scan in the list...
		int scanNumber=1;
		boolean forceReplaceExistingFiles = false;
		String multiscanFilePath = outputDirectory+"/MultipleScan_1.scan";

		String multiScanFileContents = "";

		File outputDir = new File(outputDirectory);
		File[] filesInDirectory = outputDir.listFiles();

		if (outputDir.exists() && filesInDirectory.length>0) {
			boolean deleteFiles = MessageDialog.openQuestion(parent.getShell(), "Output directory is not empty", "Output directory already has files in it.\nDelete the files before continuing?");
			if (deleteFiles) {
				// Delete the files
				for(File file : outputDir.listFiles()) {
					file.delete();
				}
				outputDir.mkdir();
			}
		}


		// MultiScan file format :
		// <scan name> <sampleFileName> <scanFileName> <detFileName> <outputFileName> <numRepetitions>

		for(OverridesForScan overrideForScan : overridesForScanFiles) {
			String sampleFileName = "", scanFileName="", detFileName="", outputFileName="";

			//Loop over each parameter file for the scan
			for(OverridesForParametersFile overrideForFile : overrideForScan.getOverrides()) {
				try {
						// Try to load xml bean from file
					String fullXmlPath = overrideForFile.getXmlFileName();
						logger.info("Reading base xml file : {}", fullXmlPath);
						Object beanObject = XMLHelpers.getBeanObject(null, fullXmlPath);
						if (beanObject.getClass().getName().equals(overrideForFile.getContainingClassType())) {
							logger.debug("File matches expected type {}. Setting new values.", overrideForFile.getContainingClassType());
							//replace values in bean with user specified values
							OverrideTableClasses.updateBeanWIthOverrides(beanObject, overrideForFile);
						}
						String baseName = FilenameUtils.getBaseName(fullXmlPath);
						String extension = FilenameUtils.getExtension(fullXmlPath);
						String fileName = String.format("%s_%d.%s", baseName, scanNumber, extension);
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
								if(result==1) {
									forceReplaceExistingFiles=true;
								} else if (result==2){
									writeFile=false;
								}
							}
						}

						if (writeFile) {
							logger.info("Saving modified xml file : {}", fullPathToNewXmlFile);
							XMLHelpers.saveBean(newFile, beanObject);
						}

						if (beanObject instanceof ISampleParameters) {
							sampleFileName=fileName;
						} else if (beanObject instanceof IDetectorParameters) {
							detFileName = fileName;
						}else if (beanObject instanceof IScanParameters) {
							scanFileName = fileName;
						}else if (beanObject instanceof IOutputParameters) {
							outputFileName = fileName;
						}

				} catch (Exception e1) {
						logger.error("Problem when creating new xml file", e1);
				}
			}

			// Add line for scan
			if (sampleFileName.isEmpty() || detFileName.isEmpty() || scanFileName.isEmpty() || outputFileName.isEmpty()) {
				logger.warn("Problem setting up multiscan for Scan_{} - parameter filename is empty!", scanNumber);
			}
			multiScanFileContents += String.format("Scan_%d %s %s %s %s %d\n", scanNumber, sampleFileName, scanFileName, detFileName, outputFileName, 1);

			scanNumber++;
		}

		try {
			BufferedWriter bufWriter = new BufferedWriter( new FileWriter(multiscanFilePath) );
			bufWriter.write(multiScanFileContents);
			bufWriter.close();
		} catch (IOException e) {
			logger.error("Problem writing multi-scan file {}", multiscanFilePath, e);
		}
		try {
			// Clear everything out before starting, otherwise refresh for folders already in the view are not consistent with disk & file contents.
			ExperimentFactory.emptyManagers();
			RefreshProjectCommandHandler refreshCommand = new RefreshProjectCommandHandler();
			refreshCommand.execute(null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			logger.error("TODO put description of error here", e);
		}
		ExperimentFactory.getExperimentEditorManager().refreshViewers();
	}

	/**
	 * Sort all model modifiers into alphabetical order
	 */
	public static void sortModelModifiers(List<OverridesForScan> overridesForScanFiles) {
		for(OverridesForScan scanOverride : overridesForScanFiles) {
			for(OverridesForParametersFile paramFile : scanOverride.getOverrides()) {
				paramFile.sort();
			}
		}
	}

	/**
	 * Update overrides for each scan to match those in {@code newOverrideParams}.
	 * i.e. remove ones not in {@code newOverrideParams} and add new ones (setting with default value).
	 * @param typeIndex index in override list (i.e. which params are being modified)
	 * @param newOverrideParams
	 */
	public static void updateModelModifiers(List<OverridesForScan> overridesForScanFiles, int typeIndex, OverridesForParametersFile newOverrideParams) {
		// Make string list of getter for all override params
		List<String> getterNameList = new ArrayList<String>();
		for (ParameterOverride p : newOverrideParams.getOverrides()) {
			getterNameList.add(p.getFullPathToGetter());
		}

		for (OverridesForScan overrideForScan : overridesForScanFiles) {
			OverridesForParametersFile overrideForFile = overrideForScan.getOverrides().get(typeIndex);
			List<ParameterOverride> currentOverrideParams = overrideForFile.getOverrides();

			// Remove overrides *not* selected by user...
			// Use iterator to avoid 'Comodification exception' when removing elements
			if (currentOverrideParams!=null) {
				Iterator<ParameterOverride> iterator =  currentOverrideParams.iterator();
				while (iterator.hasNext()) {
					ParameterOverride p = iterator.next();
					// remove if path to 'getter' function doesn't match
					if (!getterNameList.contains(p.getFullPathToGetter())) {
						iterator.remove();
					}
				}
			}

			// Add any missing ones, fill with default value
			for (String getter : getterNameList) {
				boolean found = false;
				if (currentOverrideParams!=null) {
					for (ParameterOverride p : currentOverrideParams) {
						if (p.getFullPathToGetter().equals(getter)) {
							found = true;
							break;
						}
					}
				}
				if (found == false) {
					overrideForFile.addOverride(getter, "0");
				}
			}
		}
	}

	private static String getNameForClassType(String name) {
		String niceName = "";
		Class<?> clazz = OverrideTableClasses.getClassWithName(name);
		if (clazz!=null){
			if (ISampleParameters.class.isAssignableFrom(clazz)) {
				niceName="Sample xml";
			} else if (IDetectorParameters.class.isAssignableFrom(clazz)) {
				niceName = "Detector xml";
			} else if (IScanParameters.class.isAssignableFrom(clazz)) {
				niceName = "Scan xml";
			} else if (IOutputParameters.class.isAssignableFrom(clazz)) {
				niceName = "Output xml";
			}
		}
		return niceName;
	}

	/**
	 * Return a copy of selected overrides from model, set xml name to Scan, Sample etc. depending on class type of parameter.
	 *  (used for template).
	 */
	public static OverridesForScan getSelectedOverridesFromModel(List<OverridesForScan> overridesForScans) {
		OverridesForScan overridesForScan = overridesForScans.get(0);

		OverridesForScan scanOverridesTemplate = new OverridesForScan();
		for(OverridesForParametersFile overrideForFile : overridesForScan.getOverrides()) {
			OverridesForParametersFile templateOverride = new OverridesForParametersFile();
			templateOverride.copyFrom(overrideForFile);

			// Set the xml filename to Scan, Sample, Detector etc. (this is used for column label)
			String name = getNameForClassType(overrideForFile.getContainingClassType());
			templateOverride.setXmlFileName(name);

			scanOverridesTemplate.addOverride(templateOverride);
		}
		return scanOverridesTemplate;
	}
}
