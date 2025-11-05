/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.WorkbenchPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import gda.util.JsonHelper;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroupTemplateConfiguration;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;
import uk.ac.gda.richbeans.editors.RichBeanMultiPageEditorPart;

public class DetectorConfigFileControls {

	private static final Logger logger = LoggerFactory.getLogger(DetectorConfigFileControls.class);

	private Text filenameTextbox;
	private DetectorGroupTemplateConfiguration templateConfiguration;
	private String currentDirectory;
	private boolean updateDetectorFilenameOnSaveAs = ExafsActivator.getDefault().getPreferenceStore().getBoolean(ExafsPreferenceConstants.DETECTOR_PARAMS_UPDATE_ON_SAVEAS);

	private static final GridDataFactory FILENAME_GRID_DATA = GridDataFactory.swtDefaults().hint(450, SWT.DEFAULT);

	private DetectorConfig detectorConfig;

	public DetectorConfigFileControls() {
	}

	public void addControls(Composite parent, DetectorConfig detectorConfig) {
		this.detectorConfig = detectorConfig;
		filenameTextbox = new Text(parent, SWT.FILL);
		filenameTextbox.setText(StringUtils.defaultString(detectorConfig.getConfigFileName()));
		filenameTextbox.setEditable(true);
		FILENAME_GRID_DATA.applyTo(filenameTextbox);

		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setText("Select file...");
		browseButton.addListener(SWT.Selection, event -> {
			FileDialog dialog = getDetectorFileXmlBrowser();
			String filename = dialog.open();
			updateFilenameTextBox(detectorConfig, filename, filenameTextbox);
		});

		Button configureButton = new Button(parent, SWT.PUSH);
		configureButton.setText("Configure detector");
		configureButton.addListener(SWT.Selection, event -> {
			try {
				String filename = configureDetector(detectorConfig, filenameTextbox);
				updateFilenameTextBox(detectorConfig, filename, filenameTextbox);
			} catch (IOException e) {
				logger.error("Problem configuring {} detector " + e.getMessage(), e);
				MessageDialog.openWarning(getShell(),
						"Problem configuring " + detectorConfig.getDetectorName() + "detctor", e.getMessage());
			}
		});
	}

	/**
	 * @return FileDialog for selecting detector Xml configuration file.
	 */
	private FileDialog getDetectorFileXmlBrowser() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText("Select detector configuration file");
		dialog.setFilterNames(new String[] { "xml files", "json files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.xml", "*.json", "*.*" });
		dialog.setFilterPath(currentDirectory);
		return dialog;
	}

	/**
	 * Update textbox for detector XML configuration file if it's different from current value in the model. (Listeners should update model)
	 * @param detConfig detector configuration object
	 * @param filename absolute or relative path to XML file
	 * @param filenameTextbox
	 */
	private void updateFilenameTextBox(DetectorConfig detConfig, String filename, Text filenameTextbox) {
		String relativePath = getRelativePathToFile(filename);
		if (StringUtils.isNotEmpty(filename) && !StringUtils.equals(detConfig.getConfigFileName(), relativePath)) {
			// update the textbox; listeners take care of updating the model
			filenameTextbox.setText(relativePath);

			// alignTextboxContents
			filenameTextbox.setSelection(filenameTextbox.getText().length());
		}
	}

	private Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	private String getRelativePathToFile(String absolutePath) {
		return absolutePath.replace(currentDirectory + "/", "");
	}

	private Path getAbsolutePathToFile(String fullPath) {
		if (!Paths.get(fullPath).isAbsolute()) {
			return Paths.get(currentDirectory, fullPath);
		}
		return Paths.get(fullPath);
	}

	/**
	 * Try to open a GUI to configure the named detector using the XML file.
	 *
	 * @param detectorName name of detector object to be configured
	 * @param filenameFromUser
	 *            path to the XML configuration file for the detector (absolute or relative); if this is empty (or non
	 *            existent), user will be asked whether to create new one from detector template XML.
	 * @return path to configuration file (empty if requested file was not found and no new one was created).
	 * @throws IOException
	 */
	private String configureDetector(DetectorConfig detectorConfig, Text filenameTextbox) throws IOException {
		String detectorName = detectorConfig.getDetectorName();
		String filenameFromUser = filenameTextbox.getText().trim();

		Path filenamePath = getAbsolutePathToFile(filenameFromUser);
		if (!Files.isRegularFile(filenamePath)) {

			if (!MessageDialog.openQuestion(getShell(), "Detector configuration file not found",
					"Detector configuration file " + filenameFromUser + " was not found.\n"
							+ "Do you want to make a new one from the " + detectorName + " template XML/Json?")) {
				return "";
			}
			filenamePath = createTemplateFile(detectorName, filenamePath);
		}

		// check the bean type in XML file matches type required for detector parameters
		if (filenamePath.endsWith(".xml") && !checkBeanType(detectorName, filenamePath.toString())) {
			throw new IOException("File " + filenamePath.toString() + " contains wrong settings type for detector");
		}

		var editorRef = openDetectorEditor(filenamePath.toString(), detectorName);

		// Add listener to update the filename textbox after the detector editor 'save as' action
		if (editorRef != null && updateDetectorFilenameOnSaveAs) {
			editorRef.addPartPropertyListener(
					event -> updateDetectorConfigurationFile(event, detectorConfig, filenameTextbox));
		}

		return filenamePath.toString();
	}

	/**
	 * Update the filename textbox if the detector configuration XML file has been saved to a new file
	 * Asks the user to confirm they want to update the file name.
	 * @param event
	 * @param detectorConfig
	 * @param filenameTextbox
	 */
	private void updateDetectorConfigurationFile(PropertyChangeEvent event, DetectorConfig detectorConfig, Text filenameTextbox) {
		if (!event.getProperty().equals(RichBeanMultiPageEditorPart.FILE_NAME_CHANGE_PROPERTY)) {
			return;
		}
		logger.debug("File name change event {} : {} = {}", event, event.getOldValue(), event.getNewValue());
		String oldName = event.getOldValue().toString();
		String newName = event.getNewValue().toString();
		if (oldName.equals(newName)) {
			return;
		}

		Display.getDefault().asyncExec(() -> {
			if (MessageDialog.openQuestion(getShell(), "Update the detector parameters",
					"Do you want to change the detector configuration file name for " + detectorConfig.getDescription()
							+ " to " + newName + " ?")) {
				updateFilenameTextBox(detectorConfig, newName, filenameTextbox);
			}
		});
	}

	/**
	 * Check whether content of detector XML is correct type for a detector. i.e. compare type string at start of
	 * template file with type string at start of specified file.
	 *
	 * @param detectorName name of detector
	 * @param filename name of XML file to be checked
	 * @return true if bean type in XML file is correct type for the detector.
	 * @throws IOException
	 */
	private boolean checkBeanType(String detectorName, String filename) throws IOException {
		String templateFile = templateConfiguration.getDetectorTemplateMap().get(detectorName);
		return getBeanTypeFromFile(templateFile).equals(getBeanTypeFromFile(filename));
	}

	private String getBeanTypeFromFile(String filename) throws IOException {
		List<String> f = FileUtils.readLines(Paths.get(filename).toFile(), Charset.defaultCharset());
		if (f.size() > 1) {
			String beanTypeLine = f.get(0);
			if (f.get(0).contains("<?xml version")) {
				beanTypeLine = f.get(1);
			}
			return beanTypeLine.replaceAll("[<>]", "");
		}
		return "";
	}

	/**
	 * Create a new template XML file for a detector by calling
	 * {@link DetectorGroupTemplateConfiguration#copyConfigFromTemplate(String, String, String)}. If specified path is a
	 * directory rather than a file, the filename is derived from the template filename.
	 *
	 * @param detectorName
	 * @param path path to new file to be created/directory in which to create new file
	 * @return
	 * @throws IOException
	 */
	private Path createTemplateFile(String detectorName, Path path) throws IOException {
		String outputDir;
		String fileName;
		if (Files.isDirectory(path)) {
			outputDir = path.toString();
			fileName = "";
		} else {
			outputDir = path.getParent().toString();
			fileName = path.getFileName().toString();
		}

		String filenameFull = templateConfiguration.copyConfigFromTemplate(detectorName, outputDir, fileName);
		return Paths.get(filenameFull);
	}

	private void refreshLocal(IFolder path) {
		try {
			logger.info("Refresh resources at {}", path.getFullPath());
			path.refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e2) {
			logger.error("Problem updating project resources", e2);
		}
	}
	/**
	 * Open detector editor view for the named detector file
	 *
	 * @param filename
	 * @param detectorName
	 * @return
	 */
	private WorkbenchPart openDetectorEditor(String filename, String detectorName) {
		if (StringUtils.isEmpty(filename)) {
			logger.warn("No filename given");
		}

		logger.info("Trying to open detector editor for {} using file {}", detectorName, filename);

		// Create IFile eclipse object from path to detector XML file so we can open a GUI
		// for it using the EditorExperimentManager
		IExperimentEditorManager manager = ExperimentFactory.getExperimentEditorManager();
		String dataDirectory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		IFile configFile;
		IFolder dir;
		if (filename.startsWith(dataDirectory)) {
			// find the IFolder containing the file
			String[] subPath = filename.split("/");
			int last = subPath.length - 1;
			String project = subPath[last - 1];
			dir = manager.getIFolder(project);
			configFile = dir.getFile(subPath[last]);
		} else {
			dir = manager.getSelectedFolder();
			configFile = dir.getFile(filename);
		}
		// refresh the resource folder containing the file to be loaded
		// (need to do this to pick up newly created files)
		refreshLocal(dir);

		String problemMessage = "";
		if (configFile.exists()) {
			WorkbenchPart editorPart;
			if (configFile.getFullPath().getFileExtension().equals("xml")) {
				editorPart = (WorkbenchPart) ExperimentFactory.getExperimentEditorManager().openEditor(configFile);
			} else {
				String editorClassName = getEditorClassFromJsonObject(filename);
				editorPart = (WorkbenchPart) ExperimentFactory.getExperimentEditorManager().openEditor(configFile,
						editorClassName, false);
			}
			if (editorPart != null) {
				return editorPart;
			} else {
				problemMessage = "Could not open editor for " + configFile + " - no suitable editor was found";
				logger.warn("Could not open editor for {} - no suitable editor was found", configFile);
			}

		} else {
			problemMessage = "Cannot open detector view - cannot read " + filename;
		}

		if (!problemMessage.isEmpty()) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Problem copying opening view",
					problemMessage);
			logger.warn(problemMessage);
		}
		return null;
	}

	private String getEditorClassFromJsonObject(String jsonFileName) {
		try {
			String jsonString = Files.readString(Paths.get(jsonFileName));
			return JsonHelper.getEditorClass(jsonString);
		} catch (IOException e) {
			logger.error("Problem reading from file {}", jsonFileName, e);
		}
		return null;
	}

	public Text getFilenameTextbox() {
		return filenameTextbox;
	}

	/**
	 *
	 * @return Absolute path to the configuration file
	 */
	public String getFilename() {
		String userfileName = filenameTextbox.getText();
		if (StringUtils.isEmpty(userfileName)) {
			return "";
		}
		return getAbsolutePathToFile(filenameTextbox.getText()).toString();
	}

	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	public void setTemplateConfiguration(DetectorGroupTemplateConfiguration templateConfiguration) {
		this.templateConfiguration = templateConfiguration;
	}

	public DetectorConfig getDetectorConfig() {
		return detectorConfig;
	}
}
