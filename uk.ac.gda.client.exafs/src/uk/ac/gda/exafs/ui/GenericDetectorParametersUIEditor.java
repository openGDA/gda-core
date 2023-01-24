/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static java.lang.Boolean.TRUE;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.beans.exafs.DetectorConfig;
import uk.ac.gda.beans.exafs.DetectorGroupTemplateConfiguration;
import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.client.experimentdefinition.ExperimentFactory;
import uk.ac.gda.client.experimentdefinition.IExperimentEditorManager;
import uk.ac.gda.common.rcp.util.GridUtils;
import uk.ac.gda.richbeans.editors.DirtyContainer;
import uk.ac.gda.richbeans.editors.FauxRichBeansEditor;

public class GenericDetectorParametersUIEditor extends FauxRichBeansEditor<DetectorParameters> {

	private static final Logger logger = LoggerFactory.getLogger(GenericDetectorParametersUIEditor.class);

	private ScrolledComposite scrolledComposite;
	private List<DetectorConfig> detectorConfigs;
	private DetectorGroupTemplateConfiguration templateConfiguration;
	private static final GridDataFactory DESCRIPTION_GRID_DATA = GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT);
	private static final GridDataFactory FILENAME_GRID_DATA = GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT);
	private Shell shell;
	private String currentDirectory = ""; // directory of the scan, detector, output, sample xmls currently being edited

	public GenericDetectorParametersUIEditor(String path, URL mappingURL, DirtyContainer dirtyContainer,
			DetectorParameters editingBean) {
		super(path, mappingURL, dirtyContainer, editingBean);
		detectorConfigs = editingBean.getDetectorConfigurations();
	}

	private Composite parent;

	@Override
	public void createPartControl(Composite parent) {
		this.parent = parent;
		shell = parent.getShell();

		scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite detectorParametersComposite = new Composite(scrolledComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().applyTo(detectorParametersComposite);

		createDetectorDetectorSection(detectorParametersComposite);

		scrolledComposite.setContent(detectorParametersComposite);
		scrolledComposite.setMinSize(detectorParametersComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// Force full layout (required when called by linkUI)
		GridUtils.layoutFull(parent);

		currentDirectory = ExperimentFactory.getExperimentEditorManager().getSelectedFolder().getLocation().toString();

		if (templateConfiguration == null) {
			templateConfiguration = Finder.findSingleton(DetectorGroupTemplateConfiguration.class);
		}
	}

	private void createDetectorDetectorSection(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(comp);
		for(DetectorConfig detectorConfig : detectorConfigs) {
			Composite widgetComp = new Composite(parent, SWT.NONE);
			GridLayoutFactory.swtDefaults().numColumns(getNumWidgets(detectorConfig)).applyTo(widgetComp);
			addDetectorControls(widgetComp, detectorConfig);
		}
	}

	@Override
	public void linkUI(boolean tf) {
		// update GUI from bean when it has been changed.
		scrolledComposite.dispose();
		detectorConfigs = getBean().getDetectorConfigurations();
		createPartControl(parent);
	}

	/**
	 * Number of GUI widgets required for a particular detector config
	 * @param detectorConfig
	 * @return
	 */
	private int getNumWidgets(DetectorConfig detectorConfig) {
		int numColumnsRequired = 2;
		if (TRUE.equals(detectorConfig.isUseConfigFile())) {
			numColumnsRequired += 3;
		}
		if (TRUE.equals(detectorConfig.isUseScriptCommand())) {
			numColumnsRequired += 2;
		}
		return numColumnsRequired;
	}

	private void addDetectorControls(Composite parent, DetectorConfig detectorConfig) {

		Label descriptionLabel = new Label(parent, SWT.NONE | SWT.RIGHT) ;
		descriptionLabel.setText(StringUtils.defaultIfEmpty(detectorConfig.getDescription(), detectorConfig.getDetectorName()));
		descriptionLabel.setToolTipText(detectorConfig.getDetectorName());
		DESCRIPTION_GRID_DATA.applyTo(descriptionLabel);

		Button useInScanCheckbox = new Button(parent, SWT.CHECK);
		useInScanCheckbox.setToolTipText("Select to include the detector in the scan");
		useInScanCheckbox.setSelection(detectorConfig.isUseDetectorInScan());
		useInScanCheckbox.addListener(SWT.Selection, event -> {
			detectorConfig.setUseDetectorInScan(useInScanCheckbox.getSelection());
			beanChanged();
		});

		if (TRUE.equals(detectorConfig.isUseConfigFile())) {
			// add textbox with name of xml file, 'browse for file' button and 'configure' button
			addConfigfileControls(parent, detectorConfig);
		}

		if (TRUE.equals(detectorConfig.isUseScriptCommand())) {
			// add textbox for entering name of script/command, browse button
			addScriptCommandControls(parent, detectorConfig);
		}
	}

	/**
	 * Add controls for setting the detector config file : textbox with name of xml file, 'browse for file' button and 'configure' button
	 * @param parent
	 * @param detectorConfig
	 */
	private void addConfigfileControls(Composite parent, DetectorConfig detectorConfig) {
		Text filenameTextbox = new Text(parent, SWT.NONE);
		filenameTextbox.setText(StringUtils.defaultString(detectorConfig.getConfigFileName()));
		filenameTextbox.setEditable(false);
		FILENAME_GRID_DATA.applyTo(filenameTextbox);
		addTextboxListeners(detectorConfig::setConfigFileName, detectorConfig::getConfigFileName, filenameTextbox);

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
				String filename = configureDetector(detectorConfig.getDetectorName(), filenameTextbox.getText().trim());
				updateFilenameTextBox(detectorConfig, filename, filenameTextbox);
			} catch (IOException e) {
				logger.error("Problem configuring {} detector "+e.getMessage(), e);
				MessageDialog.openWarning(shell, "Problem configuring "+detectorConfig.getDetectorName()+"detctor", e.getMessage());
			}
		});
	}

	/**
	 * @return FileDialog for selecting detector Xml configuration file.
	 */
	private FileDialog getDetectorFileXmlBrowser() {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Select detector configuration file");
		dialog.setFilterNames(new String[] { "xml files", "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });
		dialog.setFilterPath(currentDirectory);
		return dialog;
	}

	/**
	 * Update textbox for detector XML configuration file if it's different from current value
	 * in the model. (Listeners should update model)
	 * @param detConfig detector configuration object
	 * @param filename absolute or relative path to XML file
	 * @param filenameTextbox
	 */
	private void updateFilenameTextBox(DetectorConfig detConfig, String filename, Text filenameTextbox) {
		String relativePath = getRelativePathToFile(filename);
		if (StringUtils.isNotEmpty(filename) && !StringUtils.equals(detConfig.getConfigFileName(), relativePath)) {
			// update the textbox; listeners take care of updating the model
			filenameTextbox.setText(relativePath);
			alignTextboxContents(filenameTextbox);
		}
	}

	/**
	 * Add textbox for setting the Jython script file name/Jython command and a 'browse for script' button
	 * @param parent
	 * @param detectorConfig
	 */
	private void addScriptCommandControls(Composite parent, DetectorConfig detectorConfig) {
		Text filenameCommandTextbox = new Text(parent, SWT.NONE);
		filenameCommandTextbox.setText(StringUtils.defaultString(detectorConfig.getScriptCommand()));
		filenameCommandTextbox.setToolTipText("Jython command/name of Jython script to run before configuring the detector");
		FILENAME_GRID_DATA.applyTo(filenameCommandTextbox);

		addTextboxListeners(detectorConfig::setScriptCommand, detectorConfig::getScriptCommand, filenameCommandTextbox);

		Button browseButton = new Button(parent, SWT.PUSH);
		browseButton.setText("Select script file...");
		browseButton.addListener(SWT.Selection, event -> {
			FileDialog dialog = OutputParametersUIEditor.getJythonScriptFileBrowser();
			String filename = dialog.open();
			if (filename != null) {
				// update the textbox; listeners take care of updating the model
				filenameCommandTextbox.setText(filename);
				alignTextboxContents(filenameCommandTextbox);
			}
		});
	}

	/**
	 * Add listeners to a textbox to update a model when the Textbox content
	 * changes (i.e. checks current content on modify, focus lost, enter key pressed events
	 * to see if model needs updating).
	 * @param setter - a consumer that will be used to set a value in the model from latest value from widget.
	 * @param getter - supplier to retrieve value from the model
	 * @param textbox user input widget
	 */
	private void addTextboxListeners(Consumer<String> setter, Supplier<String> getter, Text textbox) {

		Consumer<FocusEvent> updater = event -> {
			logger.debug("Textbox update : new value = {}", textbox.getText());
			if (StringUtils.defaultString(getter.get()).equals(textbox.getText())) {
				logger.debug("Text not modified");
			} else {
				logger.debug("Text content modified");
				setter.accept(textbox.getText());
				beanChanged();
			}
		};

		textbox.addListener(SWT.Modify, e -> updater.accept(null));

		textbox.addFocusListener(FocusListener.focusLostAdapter(updater));

		textbox.addListener(SWT.Traverse, event ->  {
			if (event.detail == SWT.TRAVERSE_RETURN) {
				updater.accept(null);
			}
		});
	}

	/**
	 * Try to open a GUI to configure the named detector using the XML file.
	 * @param detectorName name of detector object to be configured
	 * @param filenameFromUser path to the XML configuration file for the detector (absolute or relative);
	 * if this is empty (or non existent), user will be asked whether to create new one from detector template XML.
	 * @return path to configuration file (empty if requested file was not found and no new one was created).
	 * @throws IOException
	 */
	private String configureDetector(final String detectorName, final String filenameFromUser) throws IOException {
		Path filenamePath = getAbsolutePathToFile(filenameFromUser);
		if (!Files.isRegularFile(filenamePath)) {

			if ( !MessageDialog.openQuestion(shell, "Detector configuration file not found",
					"Detector configuration file "+filenameFromUser+" was not found.\n"+
					"Do you want to make a new one from the "+detectorName+" template XML?") ) {
				return "";
			}
			filenamePath = createTemplateFile(detectorName, filenamePath);
		}
		if (!checkBeanType(detectorName, filenamePath.toString())) {
			throw new IOException("File "+filenamePath.toString()+" contains wrong settings type for detector");
		}

		openDetectorEditor(filenamePath.toString(), detectorName);
		return filenamePath.toString();
	}

	/**
	 * Create a new template XML file for a detector by calling {@link DetectorGroupTemplateConfiguration#copyConfigFromTemplate(String, String, String)}.
	 * If specified path is a directory rather than a file,	the filename is derived from the template filename.
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

	/**
	 * Check whether content of detector XML is correct type for a detector.
	 * i.e. compare type string at start of template file with type string at start of specified file.
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
		if(f.size()>1) {
			String beanTypeLine = f.get(0);
			if (f.get(0).contains("<?xml version")) {
				beanTypeLine = f.get(1);
			}
			return beanTypeLine.replaceAll("[<>]","");
		}
		return "";
	}

	private void openDetectorEditor(String filename, String detectorName) {
		if (StringUtils.isEmpty(filename)) {
			logger.warn("No filename given");
		}

		try {
			ExperimentFactory.getExperimentEditorManager().getSelectedFolder().refreshLocal(IResource.DEPTH_ONE, null);
		} catch (CoreException e2) {
			logger.error("Problem updating project resources", e2);
		}


		logger.info("Trying to open detector editor for {} using file {}", detectorName, filename);

		// Create IFile eclipse object from path to detector XML file so we can open a GUI
		// for it using the EditorExperimentManager
		IExperimentEditorManager manager = ExperimentFactory.getExperimentEditorManager();
		String dataDirectory = InterfaceProvider.getPathConstructor().createFromDefaultProperty();
		IFile configFile;
		if (filename.startsWith(dataDirectory)) {
			// find the IFolder containing the file
			String[] subPath = filename.split("/");
			int last = subPath.length-1;
			String project = subPath[last-1];
			IFolder dir =  manager.getIFolder(project);
			configFile = dir.getFile(subPath[last]);
		} else {
			IFolder dir = manager.getSelectedFolder();
			configFile = dir.getFile(filename);
		}

		String problemMessage = "";
		if (configFile.exists()) {
			IEditorPart editorPart = ExperimentFactory.getExperimentEditorManager().openEditor(configFile);
			if (editorPart == null) {
				problemMessage =  "Could not open editor for "+configFile+" - no suitable editor was found";
				logger.warn("Could not open editor for {} - no suitable editor was found", configFile);
			}
		} else {
			problemMessage = "Cannot open detector view - cannot read "+filename;
		}

		if (!problemMessage.isEmpty()) {
			MessageDialog.openWarning(shell, "Problem copying opening view", problemMessage);
			logger.warn(problemMessage);
		}
	}

	private String getRelativePathToFile(String fullPath) {
		return fullPath.replace(currentDirectory+"/", "");
	}

	private Path getAbsolutePathToFile(String fullPath) {
		if (!Paths.get(fullPath).isAbsolute()) {
			return Paths.get(currentDirectory, fullPath);
		}
		return Paths.get(fullPath);
	}

	public String getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(String currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	public void setDetectorTemplateConfiguration(DetectorGroupTemplateConfiguration templateConfiguration) {
		this.templateConfiguration = templateConfiguration;
	}

	private void alignTextboxContents(Text textbox) {
		textbox.setSelection(textbox.getText().length());
	}

	@Override
	protected String getRichEditorTabText() {
		return "Detector Parameters";
	}

	@Override
	public void setFocus() {
		scrolledComposite.setFocus();
	}

	@Override
	public void dispose() {
		scrolledComposite.dispose();
	}
}
