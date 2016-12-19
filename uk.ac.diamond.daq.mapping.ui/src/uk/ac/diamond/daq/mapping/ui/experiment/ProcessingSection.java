/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.beanutils.BeanUtils;
import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.model.OperationModelWizardDialog;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.ClusterProcessingModel;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;

/**
 * Section to configure cluster processing for a mapping scan.
 */
public class ProcessingSection extends AbstractMappingSection {

	public static final String NEXUS_FILE_EXTENSION = "nxs";

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSection.class);

	private static final int TEMPLATE_ROW_NUM_COLUMNS = 3;

	private Composite processingChainsComposite;

	private final DataBindingContext dataBindingContext = new DataBindingContext();

	private Map<String, Control[]> rowControlsMap;
	private Map<String, Binding> includeCheckboxBindings;

	private File[] templateFiles = null;

	@Override
	public void createControls(Composite parent) {
		Composite processingComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(processingComposite);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(processingComposite);

		Label processingLabel = new Label(processingComposite, SWT.NONE);
		processingLabel.setText("Processing");
		GridDataFactory.fillDefaults().applyTo(processingLabel);

		createAddProcessingModelRow(processingComposite);
		createProcessingModelRows(processingComposite);
	}

	@Override
	public boolean shouldShow() {
		return getTemplateFiles().length > 0;
	}

	private void createAddProcessingModelRow(Composite parent) {
		Composite rowComposite = new Composite(parent, SWT.NONE);
		int numColumns = 5;
		GridLayoutFactory.swtDefaults().numColumns(numColumns).applyTo(rowComposite);
		GridDataFactory grabHorizontalGridData = GridDataFactory.fillDefaults().grab(true, false);
		grabHorizontalGridData.applyTo(rowComposite);

		// Label for select template combo
		Label selectTemplateLabel = new Label(rowComposite, SWT.NONE);
		selectTemplateLabel.setText("Select Template to Add:");
		GridDataFactory swtDefaultGridData = GridDataFactory.swtDefaults();
		swtDefaultGridData.applyTo(selectTemplateLabel);

		// Combo to select template
		Combo templateSelectionCombo = new Combo(rowComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		File[] templateFiles = getTemplateFiles();
		templateSelectionCombo.setItems(Arrays.stream(templateFiles).map(f -> f.getName()).toArray(String[]::new));
		if (templateSelectionCombo.getItems().length > 0) templateSelectionCombo.select(0);
		grabHorizontalGridData.applyTo(templateSelectionCombo);

		// Combo for detector name (TODO: show a display name?)
		Combo detectorCombo = new Combo(rowComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		detectorCombo.setItems(getDetectorNames());
		if (detectorCombo.getItems().length > 0) detectorCombo.select(0);
		grabHorizontalGridData.applyTo(detectorCombo);

		// Button to add template
		Button addTemplateButton = new Button(rowComposite, SWT.PUSH);
		addTemplateButton.setText("Add...");
		swtDefaultGridData.applyTo(addTemplateButton);

		// Button to refresh template list
		Button refreshTemplatesButton = new Button(rowComposite, SWT.PUSH);
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
		swtDefaultGridData.applyTo(refreshTemplatesButton);

		addTemplateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final File templateFile = templateFiles[templateSelectionCombo.getSelectionIndex()];
				addProcessingModel(templateFile, detectorCombo.getText());
			}
		});

		refreshTemplatesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// get the currently selected template
				final File currentTemplateFile = templateFiles[templateSelectionCombo.getSelectionIndex()];
				File[] newTemplateFiles = getTemplateFiles();
				templateSelectionCombo.setItems(Arrays.stream(newTemplateFiles).map(f -> f.getName()).toArray(String[]::new));
				if (templateSelectionCombo.getItemCount() > 0) {
					templateSelectionCombo.select(Arrays.asList(templateSelectionCombo.getItems()).indexOf(currentTemplateFile.getName()));
				}
			}
		});
	}

	private void createProcessingModelRows(Composite parent) {
		processingChainsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(processingChainsComposite);
		GridLayoutFactory.swtDefaults().numColumns(TEMPLATE_ROW_NUM_COLUMNS).applyTo(processingChainsComposite);

		rowControlsMap = new HashMap<>();
		includeCheckboxBindings = new HashMap<>();
		final List<IClusterProcessingModelWrapper> clusterProcessingChains =
			getMappingBean().getClusterProcessingConfiguration();
		if (clusterProcessingChains != null) {
			for (IClusterProcessingModelWrapper clusterProcessingChain : clusterProcessingChains) {
				addProcessingModelRow(processingChainsComposite, clusterProcessingChain);
			}
		}
	}

	private void addProcessingModelRow(Composite parent,
			IClusterProcessingModelWrapper clusterProcessingChain) {
		String processingChainName = clusterProcessingChain.getName();
		Control[] rowControls = new Control[TEMPLATE_ROW_NUM_COLUMNS];
		int controlIndex = 0;

		// Include in scan checkbox
		Button checkBox = new Button(parent, SWT.CHECK);
		rowControls[controlIndex++] = checkBox;
		checkBox.setText(clusterProcessingChain.getName());
		GridDataFactory fillDefaultGridData = GridDataFactory.fillDefaults().grab(true, false);
		fillDefaultGridData.applyTo(checkBox);
		IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
		IObservableValue activeValue = PojoProperties.value("includeInScan").observe(clusterProcessingChain);
		Binding includeInScanBinding = dataBindingContext.bindValue(checkBoxValue, activeValue);
		includeCheckboxBindings.put(processingChainName, includeInScanBinding);

		// Button to configure a processing chain
		Button configureButton = new Button(parent, SWT.PUSH);
		rowControls[controlIndex++] = configureButton;
		configureButton.setText("Configure...");
		GridDataFactory swtDefaultGridData = GridDataFactory.swtDefaults();
		swtDefaultGridData.applyTo(configureButton);
		configureButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configureProcessingModel(null, clusterProcessingChain);
			}
		});

		// Button to delete a processing chain
		Button deleteButton = new Button(parent, SWT.PUSH);
		rowControls[controlIndex++] = deleteButton;
		deleteButton.setText("Delete");
		swtDefaultGridData.applyTo(deleteButton);
		deleteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				deleteProcessingModel(clusterProcessingChain);
			}
		});

		rowControlsMap.put(processingChainName, rowControls);
	}

	private IDetectorModel getDetectorModel(String detectorName) {
		// first see if there is a model for this detector in the mapping bean
		Optional<IDetectorModel> detectorModel = getMappingBean().getDetectorParameters().stream().
				map(wrapper -> wrapper.getModel()).
				filter(model -> detectorName.equals(model.getName())).
				findFirst();
		if (detectorModel.isPresent()) {
			return detectorModel.get();
		}

		// otherwise find the detector from the runnable device service and get the model from that
		IRunnableDevice<?> detector = null;
		ScanningException exception = null;
		try {
			detector = getService(IRunnableDeviceService.class).getRunnableDevice(detectorName);
		} catch (ScanningException e) {
			exception = e;
		}

		if (detector == null) {
			MessageDialog.openError(getShell(), "Configure Processing Chain",
					String.format("Could not find a detector with the name '%s'.", detectorName));
			Object[] args = (exception == null) ? new Object[] { detectorName } :
				new Object[] { detectorName, exception };
			logger.error("Could not get find a detector with the name ''{}''.", args);
			return null;
		} else {
			return (IDetectorModel) detector.getModel();
		}
	}

	private boolean configureProcessingModel(File templateFile, IClusterProcessingModelWrapper processingChain) {
		IDetectorModel detectorModel = getDetectorModel(processingChain.getModel().getDetectorName());
		if (detectorModel == null) return false;

		if (templateFile == null) {
			logger.error("templateFile is null!");
			return false;
		}

		// Clone the detector model
		IDetectorModel detectorModelCopy = null;
		try {
			detectorModelCopy = (IDetectorModel) BeanUtils.cloneBean(detectorModel);
		} catch (Exception e) {
			logger.error("Could not make a copy of the detector model: " + detectorModel.getName(), e);
			return false;
		}

		try {
			List<IOperationSetupWizardPage> startPages = new ArrayList<>();
			startPages.add(new AcquireDataWizardPage(detectorModelCopy, getEclipseContext()));
			IOperationModelWizard wizard = ServiceHolder.getOperationUIService().getWizard(null,
					startPages, templateFile.getAbsolutePath(), null);

			OperationModelWizardDialog dialog = new OperationModelWizardDialog(getShell(), wizard);
			if (dialog.open() == Window.OK) {
				try {
					wizard.saveOutputFile(processingChain.getModel().getProcessingFilePath());
				} catch (Exception e) {
					logger.error("Could not save template file!", e);
				}
			}
			return dialog.getReturnCode() == Window.OK;
		} catch (Exception e) {
			logger.error("Could not open operation wizard", e);
			return false;
		}
	}

	private void deleteProcessingModel(IClusterProcessingModelWrapper processingChain) {
		// remove the processing chain from the mapping bean
		getMappingBean().getClusterProcessingConfiguration().remove(processingChain);

		// remove and dispose of the binding for the checkbox of the processing chain
		String processingChainName = processingChain.getName();
		Binding includeCheckboxBinding = includeCheckboxBindings.remove(processingChainName);
		dataBindingContext.removeBinding(includeCheckboxBinding);
		includeCheckboxBinding.dispose();

		// dispose of all the controls on the row for the mapping chain
		Control[] rowControls = rowControlsMap.remove(processingChainName);
		for (Control control : rowControls) {
			control.dispose();
		}

		// the size of the processing section has changed, so re-layout the whole view
		mappingView.relayout();
	}

	private void addProcessingModel(File templateFile, String detectorName) {
		final File processingFile = getNewProcessingFile(templateFile, detectorName);

		// create the new cluster processing model
		ClusterProcessingModel model = new ClusterProcessingModel();
		model.setName(processingFile.getName());
		model.setDetectorName(detectorName);
		try {
			model.setProcessingFilePath(processingFile.getCanonicalPath());
			IClusterProcessingModelWrapper modelWrapper = new ClusterProcessingModelWrapper(
					model.getName(), model, true);
			boolean ok = configureProcessingModel(templateFile, modelWrapper);
			if (ok) {
				// if the configure wizard wasn't cancelled, add the new processing model
				// to the list of models and create the new row for it in the UI
				IMappingExperimentBean mappingBean = getMappingBean();
				List<IClusterProcessingModelWrapper> processingModels = mappingBean.getClusterProcessingConfiguration();
				if (processingModels == null) {
					processingModels = new ArrayList<>();
					mappingBean.setClusterProcessingConfiguration(processingModels);
				}
				processingModels.add(modelWrapper);
				addProcessingModelRow(processingChainsComposite, modelWrapper);
				mappingView.relayout();
			}
		} catch (IOException e) {
			logger.error("Could not create canonical path of file " + processingFile, e);
		}
	}

	private File getNewProcessingFile(File templateFile, String detectorName) {
		// TODO: move this to a service?
		String templateFileName = templateFile.getName();
		String fileExtn = "";
		int dotIndex = templateFileName.lastIndexOf('.');
		if (dotIndex != -1) {
			fileExtn = templateFileName.substring(dotIndex); // includes the dot
			templateFileName = templateFileName.substring(0, dotIndex);
		}

		final IFilePathService filePathService = getService(IFilePathService.class);
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

	private File[] getTemplateFiles() {
		File templatesDir = new File(getService(IFilePathService.class).getProcessingTemplatesDir());
		String[] names = templatesDir.list((dir, name) -> name.endsWith("." + NEXUS_FILE_EXTENSION));
		if (names == null) {
			templateFiles = new File[0];
		} else {
			// TODO sort first?
			templateFiles = Arrays.stream(names).map(name -> new File(templatesDir, name)).toArray(File[]::new);
		}

		return templateFiles;
	}

	private String[] getDetectorNames() {
		final List<String> detectorNames = new ArrayList<>();
		final IRunnableDeviceService runnableDeviceService = getService(IRunnableDeviceService.class);
		try {
			for (DeviceInformation<?> deviceInfo : runnableDeviceService.getDeviceInformation()) {
				if (deviceInfo.getDeviceRole() == DeviceRole.HARDWARE && deviceInfo.getModel() instanceof IDetectorModel) {
					detectorNames.add(deviceInfo.getName());
				}
			}
		} catch (ScanningException e) {
			MessageDialog.openError(getShell(), "Error", "An error occurred getting the detector names.");
			logger.error("Unable to get detector names", e);
		}

		// TODO: temporary code for testing, delete when DeviceInformation is fully working
		if (detectorNames.isEmpty()) {
			List<IDetectorModelWrapper> detectorParams = getMappingBean().getDetectorParameters();
			if (detectorParams != null && !detectorParams.isEmpty()) {
				detectorParams.forEach(wrapper -> detectorNames.add(wrapper.getModel().getName()));
			}
		}

		return detectorNames.toArray(new String[detectorNames.size()]);
	}

	@Override
	protected void updateControls() {
		// Update the section controls to reflect the new bean
		// Any additional processing chains in the scan request are added at the end of the list
		// No existing processing chains are deleted, instead they are just deselected
		List<IClusterProcessingModelWrapper> processingChains = getMappingBean().getClusterProcessingConfiguration();
		if (processingChains != null) {
			for (IClusterProcessingModelWrapper processingChain : processingChains) {
				if (!rowControlsMap.containsKey(processingChain.getName())) {
					addProcessingModelRow(processingChainsComposite, processingChain);
				}
			}
		}

		dataBindingContext.updateTargets();
	}

}
