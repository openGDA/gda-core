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
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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

	private static final String NEXUS_FILE_EXTENSION = "nxs";

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSelectionWizardPage.class);

	private final IEclipseContext context;

	private final IClusterProcessingModelWrapper processingModelWrapper;

	private final List<IDetectorModelWrapper> detectors;

	private ComboViewer templatesComboViewer;

	private ComboViewer detectorsComboViewer;

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
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

		createTemplateSelectionControls(composite);
		createDetectorSelectionControls(composite);
	}

	private void createTemplateSelectionControls(Composite parent) {
		// Label for select template combo
		Label label = new Label(parent, SWT.NONE);
		label.setText("Processing Template File:");
		GridDataFactory.swtDefaults().applyTo(label);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

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

	private void createDetectorSelectionControls(Composite parent) {
		// Label for select detector combo
		Label label = new Label(parent, SWT.NONE);
		label.setText("Detector:");
		GridDataFactory.swtDefaults().applyTo(label);

		// Combo viewer for detector selection
		detectorsComboViewer = new ComboViewer(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
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

	@Override
	public void wizardTerminatingButtonPressed(int buttonId) {
		// nothing to do
	}

	@Override
	public void setInputData(OperationData id) {
		// since this wizard page will always be before the operations pages and will have no initial data, this method does nothing
	}

	@Override
	protected void update() {
		// we don't need to do anything here
	}

	@Override
	public void finishPage() {
		final IDetectorModel detectorModel = getSelectedDetector().getModel();
		final String templateFile = getSelectedTemplateFile().getPath();

		final File processingFile = getNewProcessingFile(templateFile, detectorModel.getName());
		((ClusterProcessingModelWrapper) processingModelWrapper).setName(processingFile.getName());
		ClusterProcessingModel processingModel = processingModelWrapper.getModel();
		processingModel.setName(processingFile.getName());
		processingModel.setProcessingFilePath(processingFile.getAbsolutePath());
		processingModel.setDetectorName(detectorModel.getName());

		// Clone the detector model - we don't want to change the one in the mapping bean
		IDetectorModel detectorModelCopy = null;
		try {
			detectorModelCopy = (IDetectorModel) BeanUtils.cloneBean(detectorModel);
		} catch (Exception e) {
			logger.error("Could not make a copy of the detector model: " + detectorModel.getName(), e);
		}
		((AcquireDataWizardPage) getNextPage()).setDetectorModel(detectorModelCopy);

		// set template file on wizard, so that other pages can be created appropriately
		try {
			((IOperationModelWizard) getWizard()).setTemplateFile(templateFile);
		} catch (Exception e) {
			logger.error("Error setting template file on wizard. Could not create operations pages.", e);
		}
	}

}
