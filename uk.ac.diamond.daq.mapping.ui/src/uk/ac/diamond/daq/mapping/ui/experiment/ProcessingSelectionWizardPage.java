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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.beanutils.BeanUtils;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.model.AbstractOperationSetupWizardPage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.ConfigWrapper;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.ProcessingSetupConfiguration;

/**
 * A wizard page to setup which the dataset to be processed and the
 * processing template file to use for live processing.
 *
 * @author Matthew Dickie
 */
class ProcessingSelectionWizardPage extends AbstractOperationSetupWizardPage {

	private class RadioButtonHandler extends SelectionAdapter {

		private Button radioButton;
		private List<Control> controls;
		private ProcessingMode handlerMode;


		public RadioButtonHandler(Button radioButton, List<Control> controls, ProcessingMode m) {
			this.radioButton = radioButton;
			this.controls = controls;
			this.handlerMode = m;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {

			if (radioButton.getSelection()) {
				mode = handlerMode;
			}

			setControlsEnabled(radioButton.getSelection());
			updateButtons();
		}

		public void setControlsEnabled(boolean enabled) {
			for (Control control : controls) {
				control.setEnabled(enabled);
			}
		}

	}

	public enum ProcessingMode {
		NEW_DAWN, EXISTING_DAWN, OTHER;
	}

	public static final String PROPERTY_NAME_MALCOLM_ACQUIRE_SUPPORT = "org.eclipse.scanning.malcolm.supports.acquire";

	private static final String NEXUS_FILE_EXTENSION = "nxs";

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSelectionWizardPage.class);

	private static String lastFilePath = null;
	private static String lastConfigPath = null;

	private final IEclipseContext context;

	private final DawnConfigBean processingConfig;
	private final ConfigWrapper configWrapper;

	private final List<IScanModelWrapper<IDetectorModel>> detectors;

	private Text existingFileText;
	private Text existingConfigText;
	private Text appText;

	private ComboViewer templatesComboViewer;

	private ComboViewer detectorsComboViewer;

	private Button createNewButton;

	private Button useExistingButton;

	private Button useOtherButton;

	private IRunnableDeviceService runnableDeviceService = null;

	private ProcessingSetupConfiguration processingSetupConfiguration = null;

	private ProcessingMode mode = ProcessingMode.NEW_DAWN;

	/**
	 * A map from the name of a malcolm device to the name of the main primary dataset for that malcolm device.
	 */
	private Map<String, String> malcolmDetectorDatasetNames = null;

	protected ProcessingSelectionWizardPage(IEclipseContext context,
			DawnConfigBean processingConfig,
			ConfigWrapper configWrapper,
			List<IScanModelWrapper<IDetectorModel>> detectors) {
		super(ProcessingSelectionWizardPage.class.getName());
		setTitle("Processing Template and Detector Selection");
		setDescription("Select the processing template file to use and the detector to apply it to.");

		this.context = context;
		this.processingConfig = processingConfig;
		this.configWrapper = configWrapper;
		this.processingSetupConfiguration = PlatformUI.getWorkbench().getService(ProcessingSetupConfiguration.class);

		this.detectors = detectors.stream().
				filter(ProcessingSelectionWizardPage::supportsAcquire).
				sorted((d1, d2) -> d1.getName().compareTo(d2.getName())).
				collect(toList());
	}

	private static boolean supportsAcquire(IScanModelWrapper<IDetectorModel> wrapper) {
		if (wrapper.getModel() instanceof IMalcolmModel) {
			// This property, if set, returns the name of a malcolm device that supports acquire.
			return wrapper.getModel().getName().equals(LocalProperties.get(PROPERTY_NAME_MALCOLM_ACQUIRE_SUPPORT));
		}

		return true;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		setControl(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);

		createDetectorSelectionControls(composite);

		createNewButton = new Button(composite, SWT.RADIO);
		createNewButton.setText("Create a new processing file from a template:");
		GridDataFactory.swtDefaults().applyTo(createNewButton);

		List<Control> selectTemplateControls = createSelectTemplateControls(composite);
		createNewButton.addSelectionListener(new RadioButtonHandler(createNewButton, selectTemplateControls, ProcessingMode.NEW_DAWN));
		createNewButton.setSelection(!getTemplateFiles().isEmpty());
		createNewButton.setEnabled(!getTemplateFiles().isEmpty());

		useExistingButton = new Button(composite, SWT.RADIO);
		useExistingButton.setText("Use an existing processing file:");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(useExistingButton);

		List<Control> useExistingControls = createExistingFileControls(composite);
		RadioButtonHandler existingControlsButtonHandler =
				new RadioButtonHandler(useExistingButton, useExistingControls, ProcessingMode.EXISTING_DAWN);
		useExistingButton.addSelectionListener(existingControlsButtonHandler);
		useExistingButton.setSelection(getTemplateFiles().isEmpty());
		existingControlsButtonHandler.setControlsEnabled(false);

		useOtherButton = new Button(composite, SWT.RADIO);
		useOtherButton.setText("Specify application and config. file:");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(useOtherButton);
		List<Control> otherControls = createOtherApplicationControls(composite);
		RadioButtonHandler existingOtherButtonHandler =
				new RadioButtonHandler(useOtherButton, otherControls, ProcessingMode.OTHER);
		useOtherButton.addSelectionListener(existingOtherButtonHandler);
		useOtherButton.setSelection(false);
		existingOtherButtonHandler.setControlsEnabled(false);
	}

	private void createDetectorSelectionControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);
		GridDataFactory.fillDefaults().applyTo(composite);

		// Label for select detector combo
		Label label = new Label(composite, SWT.NONE);
		label.setText("Detector:");
		GridDataFactory.swtDefaults().applyTo(label);

		// Combo viewer for detector selection
		detectorsComboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().applyTo(detectorsComboViewer.getControl());
		detectorsComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		detectorsComboViewer.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((IScanModelWrapper<IDetectorModel>) element).getName();
			}
		});
		detectorsComboViewer.setInput(detectors);
		if (!detectors.isEmpty()) {
			detectorsComboViewer.setSelection(new StructuredSelection(getDefaultDetector()));
		}
		detectorsComboViewer.addSelectionChangedListener(evt -> {
			@SuppressWarnings("unchecked")
			IScanModelWrapper<IDetectorModel> selectedWrapper = (IScanModelWrapper<IDetectorModel>) ((IStructuredSelection) evt.getSelection()).getFirstElement();
			IDetectorModel detectorModel = selectedWrapper.getModel();
			if (detectorModel instanceof IMalcolmModel &&
					!getDetectorDatasetNameForMalcolm((IMalcolmModel) detectorModel).isPresent()) {
				MessageDialog.openError(getShell(), "Setup Processing",
						"Could not get primary dataset for malcolm device: " + detectorModel.getName());
				setPageComplete(false);
			} else {
				setPageComplete(true);
			}
		});
	}

	/**
	 * Get the detector to be selected by default in the drop-down box
	 * <p>
	 * This will be
	 * <ul>
	 * <li>the detector specified in the {@link ProcessingSetupConfiguration}, if the configuration exists and the
	 * detector specified exists</li>
	 * <li>otherwise, the first detector in the list</li>
	 * </ul>
	 *
	 * @return the detector to be selected
	 */
	private IScanModelWrapper<IDetectorModel> getDefaultDetector() {
		// Select the first detector in the list, or the one specified in the config
		IScanModelWrapper<IDetectorModel> result = detectors.get(0);
		if (processingSetupConfiguration != null) {
			final String defaultDetector = processingSetupConfiguration.getDefaultDetector();
			if (defaultDetector != null && defaultDetector.length() > 0) {
				final Optional<IScanModelWrapper<IDetectorModel>> det = detectors.stream()
						.filter(d -> d.getName().equals(defaultDetector)).findFirst();
				if (det.isPresent()) {
					result = det.get();
				}
			}
		}
		return result;
	}

	private IRunnableDeviceService getRunnableDeviceService() throws EventException, URISyntaxException {
		if (runnableDeviceService == null) {
			IEventService eventService = context.get(IEventService.class);
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			runnableDeviceService = eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
		}

		return runnableDeviceService;
	}

	private List<Control> createSelectTemplateControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(composite);
		GridDataFactory.fillDefaults().indent(20, 0).applyTo(composite);

		// Label for select template combo
		Label label = new Label(composite, SWT.NONE);
		label.setText("Processing Template File:");
		GridDataFactory.swtDefaults().applyTo(label);

		templatesComboViewer = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridDataFactory.swtDefaults().applyTo(templatesComboViewer.getControl());
		final List<File> templateFiles = getTemplateFiles();
		Collections.sort(templateFiles, (File f1, File f2) -> f1.getName().compareTo(f2.getName()));
		templatesComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		templatesComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((File) element).getName();
			}
		});

		templatesComboViewer.setInput(templateFiles);
		if (!templateFiles.isEmpty()) {
			templatesComboViewer.setSelection(new StructuredSelection(getDefaultProcessingFile(templateFiles)));
		}

		// refresh templates button
		Button refreshTemplatesButton = new Button(composite, SWT.PUSH);
		refreshTemplatesButton.setToolTipText("Refresh the list or processing template files");
		try {
			IPath uriPath = new Path("/plugin")
					.append("uk.ac.diamond.daq.mapping.ui")
					.append("icons").append("page_refresh.png");
			URI uri = new URI("platform", null, uriPath.toString(), null);
			URL url = uri.toURL();
			refreshTemplatesButton.setImage(ImageDescriptor.createFromURL(url).createImage(true));
		} catch (Exception e1) {
			refreshTemplatesButton.setText("Reload");
		}
		GridDataFactory.swtDefaults().applyTo(refreshTemplatesButton);

		refreshTemplatesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				refreshTemplates();
			}
		});

		return Arrays.asList(label, templatesComboViewer.getCombo(), refreshTemplatesButton);
	}

	/**
	 * Get the processing file to be selected by default in the drop-down list
	 * <p>
	 * This will be:
	 * <ul>
	 * <li>the file specified in the {@link ProcessingSetupConfiguration}, if the configuration exists and the file
	 * specified exists</li>
	 * <li>otherwise, the first file in the input list</li>
	 * </ul>
	 *
	 * @param templateFiles
	 *            list of available template files
	 * @return file to be selected
	 */
	private File getDefaultProcessingFile(List<File> templateFiles) {
		// Get the file specified in the config, or the first in the list if none is specified
		File result = templateFiles.get(0);
		if (processingSetupConfiguration != null) {
			final String defaultFile = processingSetupConfiguration.getDefaultProcessingFile();
			if (defaultFile != null && defaultFile.length() > 0) {
				final Optional<File> file = templateFiles.stream()
						.filter(f -> f.getName().equals(defaultFile)).findFirst();
				if (file.isPresent()) {
					result = file.get();
				}
			}
		}
		return result;
	}

	private void refreshTemplates() {
		// get the currently selected template
		File currentlySelectedFile = getSelectedTemplateFile();
		final List<File> newTemplateFiles = getTemplateFiles();
		templatesComboViewer.setInput(newTemplateFiles);
		File toSelect = newTemplateFiles.contains(currentlySelectedFile) ? currentlySelectedFile :
			(newTemplateFiles.isEmpty() ? null : newTemplateFiles.get(0));
		templatesComboViewer.setSelection(new StructuredSelection(toSelect));
		createNewButton.setEnabled(!newTemplateFiles.isEmpty());
	}

	private File getSelectedTemplateFile() {
		return (File) templatesComboViewer.getStructuredSelection().getFirstElement();
	}

	@SuppressWarnings("unchecked")
	private IScanModelWrapper<IDetectorModel> getSelectedDetector() {
		return (IScanModelWrapper<IDetectorModel>) detectorsComboViewer.getStructuredSelection().getFirstElement();
	}

	private List<File> getTemplateFiles() {
		IFilePathService filePathService = context.get(IFilePathService.class);
		File templatesDir = new File(filePathService.getProcessingTemplatesDir());
		String[] names = templatesDir.list((dir, name) -> name.endsWith("." + NEXUS_FILE_EXTENSION));
		if (names == null) return Collections.emptyList();
		return Arrays.stream(names).map(name -> new File(templatesDir, name)).collect(Collectors.toList());
	}

	private File getNewProcessingFile(String templateFileName, String detectorName) {
		// TODO: move this to a service?
		String[] fex = getNameAndExtension(templateFileName);

		final IFilePathService filePathService = context.get(IFilePathService.class);
		final String tempDir = filePathService.getTempDir();
		final String prefix = fex[0] + "-" + detectorName;

		// find a unique filename
		File file = null;
		int i = 0;
		do {
			String filename = prefix + (i == 0 ? "" : i) + fex[1];
			file = new File(tempDir, filename);
			i++;
		} while (file.exists());

		return file;
	}

	private String[] getNameAndExtension(String fileName) {
		int dotIndex = fileName.lastIndexOf('.');
		if (dotIndex != -1) {
			return new String[] {fileName.substring(0, dotIndex),fileName.substring(dotIndex)};
		} else {
			return new String[] {fileName, ""};
		}
	}

	private File getNewConfigFile(File processingFile) {

		final IFilePathService filePathService = context.get(IFilePathService.class);
		final String tempDir = filePathService.getTempDir();

		String[] fex = getNameAndExtension(processingFile.getName());

		Date date = new Date() ;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd_HHmmss") ;
		String timeStamp = "_" +dateFormat.format(date);


		return new File(tempDir, fex[0] + timeStamp + ".json");
	}

	private List<Control> createExistingFileControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().indent(20, 0).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Processing File:");
		GridDataFactory.swtDefaults().applyTo(label);

		existingFileText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(existingFileText);
		existingFileText.addModifyListener(e -> updateButtons());

		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.swtDefaults().applyTo(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				chooseExistingProcessingFile();
			}

		});

		return Arrays.asList(label, existingFileText, browseButton);
	}

	private List<Control> createOtherApplicationControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().indent(20, 0).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

		Label labelName = new Label(composite, SWT.NONE);
		labelName.setText("App Name:");
		GridDataFactory.swtDefaults().applyTo(labelName);

		appText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(appText);
		appText.addFocusListener(new FocusAdapter()  {

			@Override
			public void focusLost(FocusEvent e) {
				updateButtons();

			}
		});

		GridDataFactory.swtDefaults().applyTo(new Label(composite, SWT.NONE));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Config File:");
		GridDataFactory.swtDefaults().applyTo(label);

		existingConfigText = new Text(composite, SWT.BORDER | SWT.SINGLE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(existingConfigText);
		existingConfigText.addModifyListener(e -> updateButtons());

		Button browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText("Browse...");
		GridDataFactory.swtDefaults().applyTo(browseButton);
		browseButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				chooseExistingConfigFile();
			}

		});

		return Arrays.asList(label, existingConfigText, browseButton, labelName, appText);
	}

	private void chooseExistingConfigFile() {
		FileSelectionDialog dialog = new FileSelectionDialog(getShell());
		dialog.setFolderSelector(false);
		dialog.setFiles(new String[] { "Config files" });
		dialog.setHasResourceButton(false);
		dialog.setNewFile(false);
		if (lastFilePath != null) {
			dialog.setPath(lastConfigPath);
		}

		dialog.create();
		if (dialog.open() != Window.OK) {
			return;
		}

		String filePath = dialog.getPath();
		existingConfigText.setText(filePath);
		lastConfigPath = filePath;
	}

	private void chooseExistingProcessingFile() {
		FileSelectionDialog dialog = new FileSelectionDialog(getShell());
		dialog.setFolderSelector(false);
		dialog.setExtensions(new String[] { NEXUS_FILE_EXTENSION });
		dialog.setFiles(new String[] { "Nexus files" });
		dialog.setHasResourceButton(false);
		dialog.setNewFile(false);
		if (lastFilePath != null) {
			dialog.setPath(lastFilePath);
		}

		dialog.create();
		if (dialog.open() != Window.OK) {
			return;
		}

		String filePath = dialog.getPath();
		existingFileText.setText(filePath);
		lastFilePath = filePath;
	}

	private void updateButtons() {
		boolean pageComplete = false;

		switch (mode) {
		case NEW_DAWN:
			pageComplete = true;
			break;
		case EXISTING_DAWN:
			final String filePath = existingFileText.getText();
			if (!filePath.isEmpty()) {
				final File file = new File(filePath);
				pageComplete = file.exists() && file.isFile();
			}
			break;
		case OTHER:
			final String conf = existingConfigText.getText();
			final String app = appText.getText();
			if (!app.isEmpty()) {
				if (!conf.isEmpty()) {
					final File file = new File(conf);
					pageComplete = file.exists() && file.isFile();
				} else {
					pageComplete = true;
				}
			}
			break;
		}

		setPageComplete(pageComplete);
		getContainer().updateButtons();
	}

	@Override
	public boolean shouldSkipRemainingPages() {
		// if we're using an existing processing file we skip the rest of the wizard
		return ProcessingMode.EXISTING_DAWN.equals(mode) || ProcessingMode.OTHER.equals(mode);
	}

	@Override
	public boolean canFlipToNextPage() {
		// we can only move on to the next page if create new is selected
		// if use existing is selected, then this is the only page of the wizard
		return createNewButton.getSelection() && super.canFlipToNextPage();
	}

	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		if (buttonId == Window.OK && (ProcessingMode.EXISTING_DAWN.equals(mode) || ProcessingMode.OTHER.equals(mode))) {
			// when we're using an existing processing file, this method
			// gets called instead of finishPage being called directly by the wizard
			finishPage();
		}
	}

	@Override
	public void setInputData(OperationData id) {
		// since this wizard page will always be before the operations pages and will have no initial data, this method does nothing
	}

	@Override
	protected void update() {
		// we don't need to do anything here
	}

	private void configureProcessingModel(IDetectorModel acquireDetectorModel, Optional<String> malcolmDetectorDatasetName) {

		final File processingFile;
		if (createNewButton.getSelection()) {
			final String templateFileName = getSelectedTemplateFile().getName();
			processingFile = getNewProcessingFile(templateFileName, acquireDetectorModel.getName());
		} else {
			processingFile = new File(existingFileText.getText().trim());
		}

		configWrapper.setAppName(DawnConfigBean.getAppname());
		configWrapper.setName(processingFile.getName());
		configWrapper.setPathToConfig(getNewConfigFile(processingFile).getAbsolutePath());
		processingConfig.setProcessingFile(processingFile.getAbsolutePath());
		processingConfig.setDetectorName(malcolmDetectorDatasetName.orElse(acquireDetectorModel.getName()));

	}

	/**
	 * Get the name of the dataset to use for the malcolm device with the given name. This is the
	 * first dataset of type 'primary' in the {@link MalcolmTable} of the {@link MalcolmConstants#ATTRIBUTE_NAME_DATASETS}
	 * attribute.
	 * @param malcolmModel
	 * @return
	 */
	private Optional<String> getDetectorDatasetNameForMalcolm(IMalcolmModel malcolmModel) {
		if (malcolmDetectorDatasetNames != null && malcolmDetectorDatasetNames.containsKey(malcolmModel.getName())) {
			return Optional.of(malcolmDetectorDatasetNames.get(malcolmModel.getName()));
		}

		Optional<String> datasetName = Optional.empty();
		try {
			final IRunnableDevice<IMalcolmModel> malcolmDevice = getRunnableDeviceService().getRunnableDevice(malcolmModel.getName());
			if (malcolmDevice.getDeviceState() != DeviceState.READY) {
				throw new ScanningException("The malcolm device is not ready. A scan may be running.");
			}

			try {
				malcolmDevice.configure(malcolmModel); // configure the malcolm device, puts it in 'Armed' state
				if (malcolmDevice instanceof IMalcolmDevice) {
					final MalcolmTable datasetsTable = ((IMalcolmDevice) malcolmDevice).getDatasets();
					if (datasetsTable != null) {
						datasetName = getPrimaryDatasetNameForMalcolm(datasetsTable);
					}
				}

				if (datasetName.isPresent()) { // if we found the dataset name, cache it
					logger.debug("Got ''{}'' as dataset for processing for malcolm device ''{}''", datasetName.get(), malcolmModel.getName());
					if (malcolmDetectorDatasetNames == null) {
						malcolmDetectorDatasetNames = new HashMap<>(4);
					}
					malcolmDetectorDatasetNames.put(malcolmModel.getName(), datasetName.get());
				} else {
					logger.error("Could not get primary dataset for malcolm device ''{}''. The dataset for the malcolm device may not be set correctly.", malcolmModel.getName());
				}
			} finally {
				malcolmDevice.reset(); // Reset the malcolm device back to the 'Ready' state
			}
		} catch (Exception e) {
			logger.error("Could not get primary dataset for malcolm device: " + malcolmModel.getName(), e);
		}

		return datasetName;
	}

	private Optional<String> getPrimaryDatasetNameForMalcolm(MalcolmTable table) {
		final Predicate<Map<String, Object>> primaryDatasetFilter = row ->
			MalcolmDatasetType.fromString((String) row.get(MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE)) == MalcolmDatasetType.PRIMARY;

		return StreamSupport.stream(table.spliterator(), false). // get a stream from the iterable table
				filter(primaryDatasetFilter). // filter for primary datasets
				map(row -> row.get(MalcolmConstants.DATASETS_TABLE_COLUMN_NAME)). // get the name of the dataset, e.g.
				map(String.class::cast). // cast to string
				filter(name -> name.contains(".")). // sanity check, the name should be made of 2 dot separated parts, e.g. 'detector.data'
				map(name -> name.split("\\.")[0]). // get the first part, e.g. 'detector'. This will be the name of the NXdata group in the nexus file to process
				findFirst();
	}

	protected ProcessingMode selectedMode() {
		return mode;
	}

	@Override
	public void finishPage() {

		if (ProcessingMode.OTHER.equals(mode)) {
			String path = existingConfigText.getText();
			configWrapper.setAppName(appText.getText());
			configWrapper.setName(new File(path).getName());
			configWrapper.setPathToConfig(path);
			return;
		}

		final IDetectorModel detectorModel = getSelectedDetector().getModel();
		final Optional<String> malcolmDetectorDatasetName = detectorModel instanceof IMalcolmModel ? getDetectorDatasetNameForMalcolm((IMalcolmModel) detectorModel) : Optional.empty();
		configureProcessingModel(detectorModel, malcolmDetectorDatasetName);

		if (!ProcessingMode.NEW_DAWN.equals(selectedMode())) {
			return;
		}

		// Clone the detector model - we don't want to change the one in the mapping bean
		IDetectorModel detectorModelCopy = null;
		try {
			detectorModelCopy = (IDetectorModel) BeanUtils.cloneBean(detectorModel);
		} catch (Exception e) {
			logger.error("Could not make a copy of the detector model: " + detectorModel.getName(), e);
			return;
		}

		// setup the acquire page with the detector model for the appropriate detector and
		// the name of the detector group (may be different if the detector is a malcolm detector).
		AcquireDataWizardPage acquirePage = (AcquireDataWizardPage) getNextPage();
		acquirePage.setAcquireDetectorModel(detectorModelCopy);
		acquirePage.setDetectorDataGroupName(malcolmDetectorDatasetName.orElse(detectorModel.getName()));

		// set template file on wizard, so that other pages can be created appropriately
		final String templateFile = getSelectedTemplateFile().getPath();
		try {
			((IOperationModelWizard) getWizard()).setTemplateFile(templateFile);
		} catch (Exception e) {
			final String exceptionMessage = "Error setting template file on wizard. Could not create operations pages.";
			final String userMessage = "Could not open template file, please contact beamline representative";
			logger.error(exceptionMessage, e);

			final MessageBox messageBox = new MessageBox(getShell(), SWT.ERROR);
			messageBox.setMessage(userMessage);
			messageBox.open();

			throw new RuntimeException(exceptionMessage, e);
		}

	}

}
