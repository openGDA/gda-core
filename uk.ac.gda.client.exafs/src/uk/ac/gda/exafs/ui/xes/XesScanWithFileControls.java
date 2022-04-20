/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.xes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.widgets.file.FileBox;
import org.eclipse.richbeans.widgets.file.FileBox.ChoiceType;
import org.eclipse.richbeans.widgets.scalebox.ScaleBoxAndFixedExpression;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.exafs.ui.NumberBoxWithUnits;
import uk.ac.gda.exafs.ui.XesScanParametersComposite;
import uk.ac.gda.util.beans.BeansFactory;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.io.FileUtils;

public class XesScanWithFileControls extends XesControlsBuilder {

	private static final Logger logger = LoggerFactory.getLogger(XesScanParametersComposite.class);

	private Composite mainComposite;
	private FileBox scanFileName;
	private ScaleBoxAndFixedExpression xesEnergy;
	private NumberBoxWithUnits monoEnergy;

	public boolean useMonoEnergyControl;
	private int scanTypeNum;
	private File editorFolder;

	private double minMonoEnergy = 2000;
	private double maxMonoEnergy = 35000;

	/**
	 * Map with key = 'XES scan type' (integer), value = template file name.
	 * <br> XES scan type is one of :
	 * <li> {@link XesScanParameters#FIXED_XES_SCAN_XAS}
	 * <li> {@link XesScanParameters#FIXED_XES_SCAN_XANES},
	 * <li> {@link XesScanParameters#SCAN_XES_REGION_FIXED_MONO}.
	 */
	private Map<Integer, String> templateFilenames = Collections.emptyMap();


	@Override
	public void createControls(Composite parent) {
		GridDataFactory gridFactory = GridDataFactory.createFrom(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayoutFactory layoutFactory = GridLayoutFactory.swtDefaults().equalWidth(false);
		mainComposite = new Composite(parent, SWT.NONE);
		gridFactory.hint(500, SWT.DEFAULT).applyTo(mainComposite);
		layoutFactory.numColumns(3).applyTo(mainComposite);

		Label label;
		Label lblFileName = new Label(mainComposite, SWT.NONE);
		lblFileName.setText("File Name");

		scanFileName = new FileBox(mainComposite, SWT.NONE);
		gridFactory.applyTo(scanFileName);
		scanFileName.setChoiceType(ChoiceType.NAME_ONLY);
		scanFileName.setFilterExtensions(new String[] { "*.xml" });

		scanFileName.addValueListener(this::checkScanParameterFileType);

		Link openFile = new Link(mainComposite, SWT.NONE);
		openFile.setText("    <a>Open</a>");
		openFile.setToolTipText("Open scan parameter file.");
		openFile.addListener(SWT.Selection, e -> openFile());

		Composite scanFileSpectrometerEnergyComp = new Composite(mainComposite, SWT.NONE);
		gridFactory.span(3, 1).applyTo(scanFileSpectrometerEnergyComp);
		layoutFactory.numColumns(2).applyTo(scanFileSpectrometerEnergyComp);

		label = new Label(scanFileSpectrometerEnergyComp, SWT.NONE);
		label.setText("Spectrometer Energy (Ef)");

		xesEnergy = new ScaleBoxAndFixedExpression(scanFileSpectrometerEnergyComp, SWT.NONE);
		xesEnergy.setPrefix("   θ");
		xesEnergy.setLabelUnit("°");
		xesEnergy.setUnit("eV");
		gridFactory.applyTo(xesEnergy);
		xesEnergy.setExpressionLabelTooltip("65° < θ < 85°");
		xesEnergy.setValue(3000);

		Composite scanFileMonoEnergyComp = new Composite(mainComposite, SWT.NONE);
		gridFactory.span(3, 1).applyTo(scanFileMonoEnergyComp);
		layoutFactory.numColumns(2).applyTo(scanFileMonoEnergyComp);

		label = new Label(scanFileMonoEnergyComp, SWT.NONE);
		label.setText("Mono energy (E0)");

		monoEnergy = new NumberBoxWithUnits(scanFileMonoEnergyComp, SWT.NONE);
		monoEnergy.setDisplayIntegers(false);
		monoEnergy.setFormat(new DecimalFormat("0.##"));
		monoEnergy.setUnits("eV");
		monoEnergy.setMinimum(minMonoEnergy);
		monoEnergy.setMaximum(maxMonoEnergy);

		// Notify observers when the mono energy widget changes value
		monoEnergy.getWidget().addListener(SWT.Modify, e -> {
			if (monoEnergy.modified()) {
				observableComponent.notifyIObservers(this, monoEnergy.getValue());
			}
		});

		gridFactory.applyTo(monoEnergy);
		gridFactory.applyTo(monoEnergy.getWidget());

		templateFilenames = generateTemplateFilenameMap();

		parent.addDisposeListener(l -> dispose());
	}

	public void dispose() {
		Stream.of(mainComposite, scanFileName, xesEnergy, monoEnergy).forEach(Composite::dispose);
		deleteIObservers();
	}

	/**
	 * Show/hide the Xes energy controls according to the state of 'show'
	 * @param show
	 */
	public void setShowXesEnergy(boolean show) {
		setVisible(xesEnergy, show);
		setVisible(xesEnergy.getParent(), show);
	}

	/**
	 * Show/hide the Mono energy controls according to the state of 'show'
	 * @param show
	 */
	public void setShowMonoEnergy(boolean show) {
		setVisible(monoEnergy, show);
		setVisible(monoEnergy.getParent(), show);
	}

	private void setVisible(Composite comp, boolean visible) {
		GridData gridData = (GridData) comp.getLayoutData();
		gridData.exclude = !visible;
		comp.setVisible(visible);
	}

	public Composite getMainComposite() {
		return mainComposite;
	}
	public NumberBoxWithUnits getMonoEnergy() {
		return monoEnergy;
	}
	public ScaleBoxAndFixedExpression getXesEnergy() {
		return xesEnergy;
	}
	public FileBox getScanFileName() {
		return scanFileName;
	}
	/**
	 * Try to open currently named scan parameter file in an editor.
	 * If the file doesn't exist, the user can optionally create a new one from the template settings.
	 *
	 */
	private void openFile() {
		String paramFileName = scanFileName.getText();
		Path paramPath = Paths.get(editorFolder.getPath(), paramFileName);

		// Create new file if it doesn't exist
		if (!paramPath.toFile().isFile() &&
			MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					"Create new scan parameter file",
					"Scan parameter file "+paramFileName+" was not found. Do you want to create a new one from the default parameters?")) {

			createNewParameterFile(paramPath, scanTypeNum);
		}

		IFolder folder = ExperimentFactory.getExperimentEditorManager().getIFolder(paramPath.getParent().toString());
		try {
			// refresh the folder so eclipse is aware of newly created file
			folder.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e1) {
			logger.warn("Problem updating directroy {}", paramPath.toAbsolutePath(), e1);
		}
		final IFile scanFile = folder.getFile(scanFileName.getText());
		if (scanFile.exists()) {
			ExperimentFactory.getExperimentEditorManager().openEditor(scanFile);
		}
	}


	/**
	 * Check that the currently named scan in scanFileName box is of the the correct type.
	 * (i.e. XAS for Xas scan, Region or XANES for Region or Xanes scan)
	 * The scanFileName box is turned red with tooltip showing a warning if type is incorrect.
	 * @param event
	 */
	private void checkScanParameterFileType(ValueEvent event) {
		String name = (String) event.getValue();
		File file = new File(scanFileName.getFolder(), name);
		if (!file.exists())
			return;
		try {

			if (BeansFactory.isBean(file, XasScanParameters.class)
					&& scanTypeNum == XesScanParameters.FIXED_XES_SCAN_XAS) {
				scanFileName.setError(false, null);
			} else if (BeansFactory.isBean(file, XanesScanParameters.class)
					&& (scanTypeNum == XesScanParameters.FIXED_XES_SCAN_XANES ||
					scanTypeNum == XesScanParameters.SCAN_XES_REGION_FIXED_MONO)) {
				scanFileName.setError(false, null);
			} else {
				String fileType = scanTypeNum == XesScanParameters.FIXED_XES_SCAN_XAS ? "XAS" : "XANES";
				scanFileName.setError(true,	"File chosen is not of a scan type. It must be a " + fileType + " file.");
			}
			if (file.getParent().equals(editorFolder.getPath())) {
				scanFileName.setError(true, "Please choose a detector file in the same folder.");
			}
		} catch (Exception e) {
			logger.error("Cannot get bean type of '{}'.", file.getName(), e);
		}
	}


	/**
	 * Create a new parameter file at the given path location.
	 * If the path is a directory, a new file name is generated from the template name
	 *
	 * @param paramPath
	 */
	private void createNewParameterFile(Path paramPath, int type) {
		if (checkTemplateExists(type)) {
			try {
				String templateFilename = templateFilenames.get(type);
				Path newFilePath = paramPath;
				if (paramPath.toFile().isDirectory()) {
					// generate name of new file from template filename
					String newFileName = getNewFileName(templateFilename, paramPath.toFile());
					// set path to new file
					newFilePath = paramPath.resolve(newFileName);
				}

				// copy the template file
				String newFileName = copyFile(templateFilename, newFilePath);

				logger.info("New scan parameters file written to {}", newFileName);
				scanFileName.setText(newFileName);
			} catch (Exception e) {
				logger.warn("Problem creating new scan parameters file", e);
			}
		}
	}


	/**
	 * Generate name of new file to go in specified directory, by converting the name to avoid name clashes
	 * with files already present using {@link FileUtils#getUnique(File, String, String)}.
	 * @param scanType
	 * @param folderName
	 * @return name of new file
	 * @throws Exception
	 */
	private String getNewFileName(String templateFullPath, File folderName) {
		String templateName = FilenameUtils.getBaseName(templateFullPath); // template name, minus full path and extension
		File file = FileUtils.getUnique(folderName, templateName, ".xml");
		return file.getName();
	}

	/**
	 * Copy a file
	 * @param sourceFileFullPath full path to source file
 	 * @param newFile Path of copy location
	 * @return name of the newly generated file
	 * @throws IOException
	 */
	private String copyFile(String sourceFileFullPath, Path newFile) throws IOException {
		File sourceFile = Paths.get(sourceFileFullPath).toFile();
		FileUtils.copy(sourceFile, newFile.toFile());
		return newFile.toFile().getName();
	}

	/**
	 * Check to see if template for scan of given type exists.
	 * Shows a Warning dialog box if the template cannot be found.
	 *
	 * @param xesScanTye
	 * @return true if file exists, false otherwise
	 */
	private boolean checkTemplateExists(int xesScanTye) {
		if (!templateFilenames.containsKey(xesScanTye)) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Problem reading default parameters",
					"No template file containing defaults was found - cannot create new parameters!");
			return false;
		}
		return true;
	}

	/**
	 * Create map from XES scan type to template name.
	 * (for {@link #templateFilenames}).
	 * @return Map with key scan type (integer), value = template file name.
	 */
	private Map<Integer,String> generateTemplateFilenameMap() {
		logger.info("Setting up template filenames map...");
		List<Integer> types = Arrays.asList(XesScanParameters.FIXED_XES_SCAN_XAS, XesScanParameters.FIXED_XES_SCAN_XANES,
				XesScanParameters.SCAN_XES_REGION_FIXED_MONO);

		File templateDir = new File(ExperimentFactory.getTemplatesFolderPath());
		Map<Integer, String> templateFiles = new HashMap<>();
		types.forEach(type -> {
			try {
				List<String> templateForType = getBeanFileNames(type, templateDir);
				if (!templateForType.isEmpty()) {
					templateFiles.put(type, templateDir.toPath().resolve(templateForType.get(0)).toString());
					logger.debug("Scan type : {}, filename : {}", type, templateFiles.get(type));
				} else {
					logger.warn("No template file found for scan type : {}", type);
				}
			}catch(Exception e) {
				logger.error("Problem getting template file for scan type {}", type, e);
			}
		});
		return templateFiles;
	}

	/**
	 * Return list of all scan bean files of scan type in specified folder
	 * @param xesScanType
	 * @param folder
	 * @return list of bean filenames sorted by length and alphabetical order.
	 * @throws Exception
	 */
	private List<String> getBeanFileNames(int xesScanType, File folder) throws Exception {
		// Set Xas/Xanes class type for the different scan types
		Class<? extends IScanParameters> beanClass = getScanBeanClass(xesScanType);

		// Get File object for each xml bean of particular type
		List<File> beanFiles = getBeanFiles(folder, beanClass);

		// Make list of filenames for scan type
		List<String> filenames = new ArrayList<>();
		for(File beanFile : beanFiles) {
			IScanParameters params = (IScanParameters) XMLHelpers.getBean(beanFile);
			boolean isRegionParams = isRegionParameters(params);

			if  ( (isRegionParams && xesScanType == XesScanParameters.SCAN_XES_REGION_FIXED_MONO) ||
				  (!isRegionParams && (xesScanType == XesScanParameters.FIXED_XES_SCAN_XAS || xesScanType == XesScanParameters.FIXED_XES_SCAN_XANES) ) ) {

				filenames.add(beanFile.getName());
			}
		}
		// Sort into length and alphabetical order
		Comparator<String> comparator = Comparator.comparing(String::length).thenComparing(String::compareTo);
		filenames.sort(comparator);
		return filenames;
	}

	private Class<? extends IScanParameters> getScanBeanClass(int xesScanType) {
		if (xesScanType == XesScanParameters.FIXED_XES_SCAN_XAS) {
			return XasScanParameters.class;
		} else if (xesScanType == XesScanParameters.FIXED_XES_SCAN_XANES ||
				xesScanType == XesScanParameters.SCAN_XES_REGION_FIXED_MONO) {
			return XanesScanParameters.class;
		}
		return null;
	}

	/**
	 * Return list of all files of particular bean type in a folder
	 * @param beanClass
	 * @param folder
	 * @return
	 * @throws Exception
	 */
	private List<File> getBeanFiles(File folder, Class<? extends IScanParameters> beanClass) throws Exception {
		List<File> beanFiles = new ArrayList<>();
		final File[] allFiles = folder.listFiles();
		for (int i = 0; i < allFiles.length; i++) {
			if (BeansFactory.isBean(allFiles[i], beanClass)) {
				beanFiles.add(allFiles[i]);
			}
		}
		return beanFiles;
	}

	private boolean isRegionParameters(IScanParameters params) {
		if (params instanceof XanesScanParameters) {
			XanesScanParameters xanesParams = (XanesScanParameters)params;
			return StringUtils.isEmpty(xanesParams.getElement()) && StringUtils.isEmpty(xanesParams.getEdge());
		}
		return false;
	}

	public void setEditorFolder(File editorFolder) {
		this.editorFolder = editorFolder;
	}

	public void autoSetFileName(int xesScanType) throws Exception {
		// Set filename to name of first suitable Xas/Xanes/Region XML file
		List<String> beanFileNames = getBeanFileNames(xesScanType, editorFolder);
		String fileName = "";
		if (!beanFileNames.isEmpty()) {
			fileName = beanFileNames.get(0);
		} else if (checkTemplateExists(xesScanType)) {

			// Get name of template file
			String templateFilename = templateFilenames.get(xesScanType);

			// generate name of new file to go in editor folder
			fileName = getNewFileName(templateFilename, editorFolder);
		}

		if (!fileName.isEmpty()) {
			scanFileName.setText(fileName);
		}
	}
}


