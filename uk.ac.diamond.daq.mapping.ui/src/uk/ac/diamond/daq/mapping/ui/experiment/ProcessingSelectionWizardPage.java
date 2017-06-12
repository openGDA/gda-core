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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.model.AbstractOperationSetupWizardPage;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.widgets.file.FileSelectionDialog;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;

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


		public RadioButtonHandler(Button radioButton, List<Control> controls) {
			this.radioButton = radioButton;
			this.controls = controls;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			setControlsEnabled(radioButton.getSelection());
			updateButtons();
		}

		public void setControlsEnabled(boolean enabled) {
			for (Control control : controls) {
				control.setEnabled(enabled);
			}
		}

	}

	private static final String NEXUS_FILE_EXTENSION = "nxs";

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSelectionWizardPage.class);

	private static String lastFilePath = null;

	private final IEclipseContext context;

	private final IClusterProcessingModelWrapper processingModelWrapper;

	private final List<IDetectorModelWrapper> detectors;

	private Text existingFileText;

	private ComboViewer templatesComboViewer;

	private ComboViewer detectorsComboViewer;

	private Button createNewButton;

	private Button useExistingButton;

	protected ProcessingSelectionWizardPage(IEclipseContext context,
			IClusterProcessingModelWrapper processingModelWrapper,
			List<IDetectorModelWrapper> detectors) {
		super(ProcessingSelectionWizardPage.class.getName());
		setTitle("Processing Template and Detector Selection");
		setDescription("Select the processing template file to use and the detector to apply it to.");

		this.context = context;
		this.processingModelWrapper = processingModelWrapper;
		this.detectors = detectors;
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
		createNewButton.addSelectionListener(new RadioButtonHandler(createNewButton, selectTemplateControls));
		createNewButton.setSelection(true);

		useExistingButton = new Button(composite, SWT.RADIO);
		useExistingButton.setText("Use an existing processing file:");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(useExistingButton);

		List<Control> useExistingControls = createExistingFileControls(composite);
		RadioButtonHandler existingControlsButtonHandler =
				new RadioButtonHandler(useExistingButton, useExistingControls);
		useExistingButton.addSelectionListener(existingControlsButtonHandler);
		useExistingButton.setSelection(false);
		existingControlsButtonHandler.setControlsEnabled(false);
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
			@Override
			public String getText(Object element) {
				return ((IDetectorModelWrapper) element).getName();
			}
		});
		detectorsComboViewer.setInput(detectors);
		if (!detectors.isEmpty()) {
			detectorsComboViewer.setSelection(new StructuredSelection(detectors.get(0)));
		}
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
		List<File> templateFiles = getTemplateFiles();
		templatesComboViewer.setContentProvider(ArrayContentProvider.getInstance());
		templatesComboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return ((File) element).getName();
			}
		});

		templatesComboViewer.setInput(templateFiles);
		if (!templateFiles.isEmpty()) {
			templatesComboViewer.setSelection(new StructuredSelection(templateFiles.get(0)));
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

	private void refreshTemplates() {
		// get the currently selected template
		File currentlySelectedFile = getSelectedTemplateFile();
		final List<File> newTemplateFiles = getTemplateFiles();
		templatesComboViewer.setInput(newTemplateFiles);
		File toSelect = newTemplateFiles.contains(currentlySelectedFile) ? currentlySelectedFile :
			(newTemplateFiles.isEmpty() ? null : newTemplateFiles.get(0));
		templatesComboViewer.setSelection(new StructuredSelection(toSelect));
	}

	private File getSelectedTemplateFile() {
		return (File) templatesComboViewer.getStructuredSelection().getFirstElement();
	}

	private IDetectorModelWrapper getSelectedDetector() {
		return (IDetectorModelWrapper) detectorsComboViewer.getStructuredSelection().getFirstElement();
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
		String fileExtn = "";
		int dotIndex = templateFileName.lastIndexOf('.');
		if (dotIndex != -1) {
			fileExtn = templateFileName.substring(dotIndex); // includes the dot
			templateFileName = templateFileName.substring(0, dotIndex);
		}

		final IFilePathService filePathService = context.get(IFilePathService.class);
		final String tempDir = filePathService.getTempDir();
		final String prefix = templateFileName + "-" + detectorName;

		// find a unique filename
		File file = null;
		int i = 0;
		do {
			String filename = prefix + (i == 0 ? "" : i) + fileExtn;
			file = new File(tempDir, filename);
			i++;
		} while (file.exists());

		return file;
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
		if (createNewButton.getSelection()) {
			pageComplete = true;
		} else {
			final String filePath = existingFileText.getText();
			if (!filePath.isEmpty()) {
				final File file = new File(filePath);
				pageComplete = file.exists() && file.isFile();
			}
		}

		setPageComplete(pageComplete);
		getContainer().updateButtons();
	}

	@Override
	public boolean shouldSkipRemainingPages() {
		// if we're using an existing processing file we skip the rest of the wizard
		return useExistingButton.getSelection();
	}

	@Override
	public boolean canFlipToNextPage() {
		// we can only move on to the next page if create new is selected
		// if use existing is selected, then this is the only page of the wizard
		return createNewButton.getSelection() && super.canFlipToNextPage();
	}

	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		if (buttonId == Window.OK && useExistingButton.getSelection()) {
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

	private void configureProcessingModel(IDetectorModel detectorModel) {
		final File processingFile;
		if (createNewButton.getSelection()) {
			final String templateFileName = getSelectedTemplateFile().getName();
			processingFile = getNewProcessingFile(templateFileName, detectorModel.getName());
		} else {
			processingFile = new File(existingFileText.getText().trim());
		}

		((ClusterProcessingModelWrapper) processingModelWrapper).setName(processingFile.getName());
		ClusterProcessingModel processingModel = processingModelWrapper.getModel();
		processingModel.setName(processingFile.getName());
		processingModel.setProcessingFilePath(processingFile.getAbsolutePath());
		processingModel.setDetectorName(detectorModel.getName());
	}

	@Override
	public void finishPage() {
		final IDetectorModel detectorModel = getSelectedDetector().getModel();
		configureProcessingModel(detectorModel);

		if (createNewButton.getSelection()) {
			// Clone the detector model - we don't want to change the one in the mapping bean
			IDetectorModel detectorModelCopy = null;
			try {
				detectorModelCopy = (IDetectorModel) BeanUtils.cloneBean(detectorModel);
			} catch (Exception e) {
				logger.error("Could not make a copy of the detector model: " + detectorModel.getName(), e);
			}
			((AcquireDataWizardPage) getNextPage()).setDetectorModel(detectorModelCopy);

			// set template file on wizard, so that other pages can be created appropriately
			final String templateFile = getSelectedTemplateFile().getPath();
			try {
				((IOperationModelWizard) getWizard()).setTemplateFile(templateFile);
			} catch (Exception e) {
				logger.error("Error setting template file on wizard. Could not create operations pages.", e);
			}
		}
	}

}
