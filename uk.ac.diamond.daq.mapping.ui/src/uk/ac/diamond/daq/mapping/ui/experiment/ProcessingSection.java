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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.dawnsci.processing.ui.ServiceHolder;
import org.dawnsci.processing.ui.api.IOperationModelWizard;
import org.dawnsci.processing.ui.api.IOperationSetupWizardPage;
import org.dawnsci.processing.ui.model.OperationModelWizardDialog;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IClusterProcessingModelWrapper;
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
		GridLayoutFactory.fillDefaults().applyTo(rowComposite);
		GridDataFactory grabHorizontalGridData = GridDataFactory.fillDefaults().grab(true, false);
		grabHorizontalGridData.applyTo(rowComposite);

		// Button to add a processing model
		Button addProcessingModelButton = new Button(rowComposite, SWT.PUSH);
		addProcessingModelButton.setText("Add Processing...");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).grab(true, false).applyTo(addProcessingModelButton);

		addProcessingModelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addProcessingModel();
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
		String name = clusterProcessingChain.getName();
		checkBox.setText(name == null ? "(Unnamed)" : name);
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
				configureProcessingModel(clusterProcessingChain);
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

	private IClusterProcessingModelWrapper configureProcessingModel(IClusterProcessingModelWrapper processingModelWrapper) {
		List<IOperationSetupWizardPage> startPages = new ArrayList<>(2);

		AcquireDataWizardPage acquirePage = new AcquireDataWizardPage(getEclipseContext());
		if (processingModelWrapper == null) {
			ClusterProcessingModel model = new ClusterProcessingModel();
			processingModelWrapper = new ClusterProcessingModelWrapper(null, model, true);

			startPages.add(new ProcessingSelectionWizardPage(getEclipseContext(),
					processingModelWrapper, getMappingBean().getDetectorParameters()));
		} else {
			// get the IDetectorModel for the detector name as set in the processing model
			String detectorName = processingModelWrapper.getModel().getDetectorName();
			Optional<IDetectorModel> detectorWrapper =
					getMappingBean().getDetectorParameters().stream().
					map(wrapper -> wrapper.getModel()).
					filter(model -> model.getName().equals(detectorName)).
					findFirst();
			acquirePage.setDetectorModel(detectorWrapper.get());
		}
		startPages.add(acquirePage);

		try {
			IOperationModelWizard wizard = ServiceHolder.getOperationUIService().getWizard(null,
					startPages, (String) null, null);

			OperationModelWizardDialog dialog = new OperationModelWizardDialog(getShell(), wizard);
			dialog.setTitle("Setup Processing");
			if (dialog.open() == Window.OK) {
				try {
					wizard.saveOutputFile(processingModelWrapper.getModel().getProcessingFilePath());
				} catch (Exception e) {
					logger.error("Could not save template file!", e);
				}
			}
			if (dialog.getReturnCode() == Window.OK) {
				return processingModelWrapper;
			}
		} catch (Exception e) {
			logger.error("Could not open operation wizard", e);
		}
		return null;
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

	private void addProcessingModel() {
		IClusterProcessingModelWrapper modelWrapper = configureProcessingModel(null);
		if (modelWrapper != null) {
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
	}

	private File[] getTemplateFiles() {
		// TODO: move getting templates to a service
		File templatesDir = new File(getService(IFilePathService.class).getProcessingTemplatesDir());
		String[] names = templatesDir.list((dir, name) -> name.endsWith("." + NEXUS_FILE_EXTENSION));
		File[] templateFiles;
		if (names == null) {
			templateFiles = new File[0];
		} else {
			// TODO sort first?
			templateFiles = Arrays.stream(names).map(name -> new File(templatesDir, name)).toArray(File[]::new);
		}

		return templateFiles;
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
