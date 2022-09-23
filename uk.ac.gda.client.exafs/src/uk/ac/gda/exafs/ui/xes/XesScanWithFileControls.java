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
import java.nio.file.Files;
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
import java.util.Optional;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.IScanParameters;
import uk.ac.gda.beans.exafs.XanesScanParameters;
import uk.ac.gda.beans.exafs.XasScanParameters;
import uk.ac.gda.beans.exafs.XesScanParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.common.rcp.util.EclipseUtils;
import uk.ac.gda.exafs.ui.NumberBoxWithUnits;
import uk.ac.gda.exafs.ui.XesScanParametersComposite;
import uk.ac.gda.util.beans.xml.XMLHelpers;
import uk.ac.gda.util.beans.xml.XMLRichBean;
import uk.ac.gda.util.io.FileUtils;

public class XesScanWithFileControls extends XesControlsBuilder {

	private static final Logger logger = LoggerFactory.getLogger(XesScanParametersComposite.class);

	private Composite mainComposite;
	private FileBox scanFileNameMono;
	private FileBox scanFileNameRow1;
	private FileBox scanFileNameRow2;

	private ScaleBoxAndFixedExpression xesEnergyRow1;
	private ScaleBoxAndFixedExpression xesEnergyRow2;
	private boolean showRow2Controls =  false;

	private NumberBoxWithUnits monoEnergy;

	private int scanTypeNum;

	private File editorFolder;
	private IFile editingFile;

	private double minMonoEnergy = 2000;
	private double maxMonoEnergy = 35000;

	private String fileLabelPattern = "File Name %s";
	private String energyLabelPattern = "Spectrometer Energy %s";

	private String row1Suffix = "(Ef, upper)";
	private String row2Suffix = "(Ef, lower)";
	private String monoSuffix = "(E0, mono)";

	/**
	 * Map with key = 'XES scan type' (integer), value = template file name.
	 * <br> XES scan type is one of :
	 * <li> {@link XesScanParameters#FIXED_XES_SCAN_XAS}
	 * <li> {@link XesScanParameters#FIXED_XES_SCAN_XANES},
	 * <li> {@link XesScanParameters#SCAN_XES_REGION_FIXED_MONO}.
	 */
	private Map<Integer, String> templateFilenames = Collections.emptyMap();

	private GridLayoutFactory layoutFactory = GridLayoutFactory.swtDefaults().equalWidth(false);
	private GridDataFactory gridFactory = GridDataFactory.createFrom(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1)).hint(500, SWT.DEFAULT);

	@Override
	public void createControls(Composite parent) {
		mainComposite = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(1).applyTo(mainComposite);

		scanFileNameRow1 = createFileWidget(mainComposite, String.format(fileLabelPattern, row1Suffix));
		gridFactory.applyTo(scanFileNameRow1);

		scanFileNameRow2 = createFileWidget(mainComposite, String.format(fileLabelPattern, row2Suffix));
		gridFactory.applyTo(scanFileNameRow2);

		scanFileNameMono = createFileWidget(mainComposite, String.format(fileLabelPattern, monoSuffix));
		gridFactory.applyTo(scanFileNameMono);

		Composite scanFileSpectrometerEnergyComp = new Composite(mainComposite, SWT.NONE);
		gridFactory.span(3, 1).applyTo(scanFileSpectrometerEnergyComp);
		layoutFactory.numColumns(2).applyTo(scanFileSpectrometerEnergyComp);

		xesEnergyRow1 = createXesEnergyWidget(mainComposite, String.format(energyLabelPattern, row1Suffix));
		gridFactory.applyTo(xesEnergyRow1);

		xesEnergyRow2 = createXesEnergyWidget(mainComposite, String.format(energyLabelPattern, row2Suffix));
		gridFactory.applyTo(xesEnergyRow2);

		monoEnergy = createMonoEnergyWidget(mainComposite);
		gridFactory.applyTo(monoEnergy);
		gridFactory.applyTo(monoEnergy.getWidget());

		templateFilenames = generateTemplateFilenameMap();
		setupFieldWidgets(Arrays.asList(scanFileNameRow1, scanFileNameRow2, scanFileNameMono, xesEnergyRow1, xesEnergyRow2));
		parent.addDisposeListener(l -> dispose());
	}

	private FileBox createFileWidget(Composite parent, String labelText) {
		Composite container = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(3).applyTo(container);
		gridFactory.applyTo(container);

		Label lblFileName = new Label(container, SWT.NONE);
		lblFileName.setText(labelText);

		FileBox scanFileWidget = new FileBox(container, SWT.NONE);
		scanFileWidget.setChoiceType(ChoiceType.NAME_ONLY);
		scanFileWidget.setFilterExtensions(new String[] { "*.xml" });
		scanFileWidget.addValueListener(e -> checkScanParameterFileType(e, scanFileWidget));

		Button openFile = new Button(container, SWT.PUSH);
		openFile.setText("Open");
		openFile.setToolTipText("Open scan parameter file");

		openFile.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> openFile(scanFileWidget)));

		return scanFileWidget;
	}

	private ScaleBoxAndFixedExpression createXesEnergyWidget(Composite parent, String textLabel) {
		Composite container = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(3).applyTo(container);
		gridFactory.applyTo(container);

		Label label = new Label(container, SWT.NONE);
		label.setText(textLabel);

		ScaleBoxAndFixedExpression xesEnergyWidget = new ScaleBoxAndFixedExpression(container, SWT.NONE);
		xesEnergyWidget.setPrefix("   θ");
		xesEnergyWidget.setLabelUnit("°");
		xesEnergyWidget.setUnit("eV");
		xesEnergyWidget.setExpressionLabelTooltip("65° < θ < 85°");
		xesEnergyWidget.setValue(3000);

		return xesEnergyWidget;
	}

	private NumberBoxWithUnits createMonoEnergyWidget(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		layoutFactory.numColumns(2).applyTo(container);
		gridFactory.applyTo(container);

		Label label = new Label(container, SWT.NONE);
		label.setText("Mono energy (E0)");

		NumberBoxWithUnits energyWidget = new NumberBoxWithUnits(container, SWT.NONE);
		energyWidget.setDisplayIntegers(false);
		energyWidget.setFormat(new DecimalFormat("0.##"));
		energyWidget.setUnits("eV");
		energyWidget.setMinimum(minMonoEnergy);
		energyWidget.setMaximum(maxMonoEnergy);

		// Notify observers when the mono energy widget changes value
		energyWidget.getWidget().addListener(SWT.Modify, e -> {
			if (energyWidget.modified()) {
				observableComponent.notifyIObservers(this, energyWidget.getValue());
			}
		});

		return energyWidget;
	}

	public void dispose() {
		Stream.of(mainComposite, scanFileNameRow1, scanFileNameRow2, scanFileNameMono, xesEnergyRow1, monoEnergy).forEach(Composite::dispose);
		deleteIObservers();
	}

	public void enableXesFileControls(boolean enable1, boolean enable2) {
		scanFileNameRow1.setEnabled(enable1);
		scanFileNameRow2.setEnabled(enable2);
	}

	public void showMain(boolean show) {
		setVisible(mainComposite, show);
	}

	/**
	 * Display widgets for the currently set scan type :
	 * <li> Mono energy and XES region scan file controls (if scanType = SCAN_XES_REGION_FIXED_MONO)
	 * or
	 * <li> Mono XAS/XANES region and XES energy controls
	 */
	public void setupWidgetsForScanType() {

		boolean showMonoEnergy = scanTypeNum == XesScanParameters.SCAN_XES_REGION_FIXED_MONO;

		// hide/show the mono energy control and XES region file
		showWidget(monoEnergy, showMonoEnergy);
		showWidget(scanFileNameRow1, showMonoEnergy);

		// hide/show the XES fixed energy and mono file controls
		showWidget(xesEnergyRow1, !showMonoEnergy);
		showWidget(scanFileNameMono, !showMonoEnergy);

		showWidget(xesEnergyRow2, showRow2Controls && !showMonoEnergy);
		showWidget(scanFileNameRow2, showRow2Controls && showMonoEnergy);
	}

	@Override
	protected void showWidget(Composite widget, boolean show) {
		if (widget != null) {
			super.showWidget(widget, show);
		}
	}

	public Composite getMainComposite() {
		return mainComposite;
	}
	public NumberBoxWithUnits getMonoEnergy() {
		return monoEnergy;
	}
	public ScaleBoxAndFixedExpression getXesEnergy() {
		return xesEnergyRow1;
	}
	public FileBox getScanFileName() {
		return scanFileNameRow1;
	}

	public FileBox getMonoScanFileName() {
		return scanFileNameMono;
	}

	/**
	 * Try to open currently named scan parameter file in an editor.
	 * If the file doesn't exist, the user can optionally create a new one from the template settings.
	 *
	 * @param fileNameWidget FileBox widget with the filename
	 */
	private void openFile(FileBox fileNameWidget) {
		String paramFileName = fileNameWidget.getText();
		Path paramPath = Paths.get(editorFolder.getPath(), paramFileName);

		// Create new file if it doesn't exist
		if (!paramPath.toFile().isFile() &&
			MessageDialog.openQuestion(Display.getDefault().getActiveShell(),
					"Create new scan parameter file",
					"Scan parameter file "+paramFileName+" was not found. Do you want to create a new one from the default parameters?")) {

			String newFileName = createNewParameterFile(paramPath, scanTypeNum);
			if (!newFileName.isEmpty()) {
				fileNameWidget.setText(newFileName);
			}
		}

		IFolder folder = (IFolder) editingFile.getParent();
		try {
			// refresh the folder so eclipse is aware of newly created file
			folder.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e1) {
			logger.warn("Problem updating directroy {}", paramPath.toAbsolutePath(), e1);
		}
		final IFile scanFile = folder.getFile(fileNameWidget.getText());
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
	private void checkScanParameterFileType(ValueEvent event, FileBox fileBox) {
		fileBox.setFolder(false);
		String name = (String) event.getValue();
		File file = new File(fileBox.getFolder(), name);
		if (!file.exists() || !file.isFile())
			return;
		try {
			var bean = XMLHelpers.getBean(file);
			if (checkRegionXasXanesTypeIsCorrect(bean, scanTypeNum)) {
				fileBox.setError(false, null);
			} else {
				String fileType = scanTypeNum == XesScanParameters.FIXED_XES_SCAN_XAS ? "XAS" : "XANES";
				fileBox.setError(true,	"File chosen is not of a scan type. It must be a " + fileType + " file.");
			}
			if (!file.getParent().equals(editorFolder.getPath())) {
				fileBox.setError(true, "Please choose a detector file in the same folder.");
			}
		} catch (Exception e) {
			logger.error("Cannot get bean type of '{}'.", file.getName(), e);
		}
	}

	/**
	 * Create a new parameter file at the given path location.
	 * If the path is a directory, a new file name is generated from the template name
	 *
	 * Warning dialog box is shown if there is no template file for the scan type, or
	 * and exception is thrown when generating the file
	 *
	 * @param paramPath
	 *
	 * @return newFileName - the name of the newly generated file. This will be empty if one wasn't created due to an error.
	 */
	private String createNewParameterFile(Path paramPath, int type) {
		if (!checkTemplateExists(type)) {
			return "";
		}
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
			return newFileName;
		} catch (Exception e) {
			logger.warn("Problem creating new scan parameters file", e);
			MessageDialog.openWarning(Display.getDefault().getActiveShell(),
					"Problem creating new scan parameters file",
					"Problem creating new scan parameters file : "+e.getMessage());
			return "";
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

		if (beanClass == null) {
			return Collections.emptyList();
		}

		// Get File object for each xml bean of particular type
		List<File> beanFiles = getBeanFiles(folder, beanClass);

		// Make list of filenames for scan type
		List<String> filenames = new ArrayList<>();
		for(File beanFile : beanFiles) {
			IScanParameters params = (IScanParameters) XMLHelpers.getBean(beanFile);
			if (checkRegionXasXanesTypeIsCorrect(params, xesScanType)) {
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
			Optional<String> beanType = getBeanType(allFiles[i]);
			if (beanType.isPresent() && beanType.get().equals(beanClass.getSimpleName())) {
				beanFiles.add(allFiles[i]);
			}
		}
		return beanFiles;
	}

	/**
	 * Return the object type string from start of an XML file, removing the <> characters.
	 * Skips over the header line if it's present; the object type line is next line after the header.
	 *
	 * @param f file
	 * @return type string
	 * @throws IOException
	 */
	private Optional<String> getBeanType(File f) throws IOException {
		try(Stream<String> stream = Files.lines(f.toPath())) {
			return stream.filter(str -> !str.contains(("xml version")))
				.filter(str -> str.startsWith("<"))
				.map(str -> str.replaceAll("[<>]", ""))
				.findFirst();
		}
	}

	public void setEditingFile(final IEditorInput editing) {
		this.editingFile = EclipseUtils.getIFile(editing);
		this.editorFolder = EclipseUtils.getFile(editing).getParentFile();

		// Set the starting folder for the file selection widgets
		Stream.of(scanFileNameMono, scanFileNameRow1, scanFileNameRow2)
			.forEach(w -> w.setFolder(editorFolder));
	}

	private boolean checkRegionXasXanesTypeIsCorrect(XMLRichBean bean, int xesScanType) {
		switch(xesScanType) {
		case XesScanParameters.SCAN_XES_REGION_FIXED_MONO :
			return isRegionParameters(bean);
		case XesScanParameters.FIXED_XES_SCAN_XANES :
			return bean instanceof XanesScanParameters;
		case XesScanParameters.FIXED_XES_SCAN_XAS :
			return bean instanceof XasScanParameters;
		default :
			return false;
		}
	}

	private boolean isRegionParameters(XMLRichBean params) {
		if (params instanceof XanesScanParameters) {
			XanesScanParameters xanesParams = (XanesScanParameters)params;
			return StringUtils.isEmpty(xanesParams.getElement()) && StringUtils.isEmpty(xanesParams.getEdge());
		}
		return false;
	}

	public void autoSetFileName(int xesScanType, String fileName) throws Exception {
		if (!fileName.isEmpty()) {
			File file = Paths.get(editorFolder.getPath(),fileName).toFile();
			if (file.exists()) {
				logger.debug("Checking type of file : {}", fileName);
				XMLRichBean params = XMLHelpers.getBean(file);
				if (!checkRegionXasXanesTypeIsCorrect(params, xesScanType)) {
					fileName = "";
				}
			} else {
				logger.debug("File {} doesn't exist", fileName);
			}

		}
		// Set file name from template
		if (fileName.isEmpty() && checkTemplateExists(xesScanType)) {
			// Get name of template file
			String templateFilename = templateFilenames.get(xesScanType);
			logger.info("Setting filename from template : {}", templateFilename);

			// generate name of new file to go in editor folder
			fileName = getNewFileName(templateFilename, editorFolder);
		}

		if (!fileName.isEmpty()) {
			// Update the Region file name(s)
			if (xesScanType == XesScanParameters.SCAN_XES_REGION_FIXED_MONO) {
				scanFileNameRow1.setText(fileName);
				scanFileNameRow2.setText(fileName);
			} else {
				// Update the XAS/XANES name
				scanFileNameMono.setText(fileName);
			}
		}
	}
	public void autoSetFileName(int xesScanType) throws Exception {
		// Set filename to name of first suitable Xas/Xanes/Region XML file
		List<String> beanFileNames = getBeanFileNames(xesScanType, editorFolder);
		autoSetFileName(xesScanType, beanFileNames.get(0));

	}

	public int getScanTypeNum() {
		return scanTypeNum;
	}

	public void setScanTypeNum(int scanTypeNum) {
		this.scanTypeNum = scanTypeNum;
	}

	public void setShowRow2Controls(boolean showRow2Controls) {
		this.showRow2Controls = showRow2Controls;
	}

	public void setRow1Suffix(String row1Suffix) {
		this.row1Suffix = row1Suffix;
	}

	public void setRow2Suffix(String row2Suffix) {
		this.row2Suffix = row2Suffix;
	}
}


