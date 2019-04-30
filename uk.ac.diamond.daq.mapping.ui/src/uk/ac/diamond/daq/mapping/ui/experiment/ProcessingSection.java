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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

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

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.ClusterProcessingModelWrapper;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * Section to configure cluster processing for a mapping scan.
 */
public class ProcessingSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(ProcessingSection.class);

	private static final File[] NO_TEMPLATE_FILES = new File[0];

	private static final int TEMPLATE_ROW_NUM_COLUMNS = 3;

	private Composite processingChainsComposite;

	private Map<String, Control[]> rowControlsMap;
	private Map<String, Binding> includeCheckboxBindings;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		dataBindingContext = new DataBindingContext();
		Composite processingComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(processingComposite);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(processingComposite);

		createTitleAndAddProcessingRow(processingComposite);
		createProcessingModelRows(processingComposite);
	}

	@Override
	public boolean shouldShow() {
		return getTemplateFiles().length > 0;
	}

	private void createTitleAndAddProcessingRow(Composite parent) {
		Composite rowComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(rowComposite);
		GridDataFactory grabHorizontalGridData = GridDataFactory.fillDefaults().grab(true, false);
		grabHorizontalGridData.applyTo(rowComposite);

		Label processingLabel = new Label(rowComposite, SWT.NONE);
		processingLabel.setText("Processing");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(processingLabel);

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
		final List<IScanModelWrapper<ClusterProcessingModel>> clusterProcessingChains =
			getMappingBean().getClusterProcessingConfiguration();
		if (clusterProcessingChains != null) {
			for (IScanModelWrapper<ClusterProcessingModel> clusterProcessingChain : clusterProcessingChains) {
				addProcessingModelRow(processingChainsComposite, clusterProcessingChain);
			}
		}
	}

	private void addProcessingModelRow(Composite parent,
			IScanModelWrapper<ClusterProcessingModel> clusterProcessingChain) {
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

	private IScanModelWrapper<ClusterProcessingModel> configureProcessingModel(IScanModelWrapper<ClusterProcessingModel> processingModelWrapper) {
		final List<IOperationSetupWizardPage> startPages = new ArrayList<>(2);

		final AcquireDataWizardPage acquirePage = new AcquireDataWizardPage(getEclipseContext());
		final Supplier<Boolean> useExisting;
		if (processingModelWrapper == null) {
			final ClusterProcessingModel model = new ClusterProcessingModel();
			processingModelWrapper = new ClusterProcessingModelWrapper(null, model, true);

			final ProcessingSelectionWizardPage selectionPage = new ProcessingSelectionWizardPage(getEclipseContext(),
					processingModelWrapper, getMappingBean().getDetectorParameters());
			startPages.add(selectionPage);
			useExisting = selectionPage::useExisting;
		} else {
			// get the IDetectorModel for the detector name as set in the processing model
			String detectorName = processingModelWrapper.getModel().getDetectorName();
			String malcolmDeviceName = processingModelWrapper.getModel().getMalcolmDeviceName();
			// if the malcolm device name is set, we use that as the detetor in the acquire scan instead
			String acquireDetectorName = malcolmDeviceName == null ? detectorName : malcolmDeviceName;

			final Optional<IDetectorModel> detectorWrapper =
					getMappingBean().getDetectorParameters().stream().
					map(IScanModelWrapper<IDetectorModel>::getModel).
					filter(model -> model.getName().equals(acquireDetectorName)).
					findFirst();
			if (!detectorWrapper.isPresent()) {
				logger.error("Could not get detector from mapping bean {}", acquireDetectorName);
				return null;
			}
			acquirePage.setAcquireDetectorModel(detectorWrapper.get());
			acquirePage.setDetectorDataGroupName(detectorName);
			useExisting = () -> false;
		}
		startPages.add(acquirePage);

		try {
			IOperationModelWizard wizard = ServiceHolder.getOperationUIService().getWizard(null,
					startPages, (String) null, null);

			OperationModelWizardDialog dialog = new OperationModelWizardDialog(getShell(), wizard);
			dialog.setTitle("Setup Processing");
			if (dialog.open() == Window.OK) {
				if (!useExisting.get()) {
					try {
						final Path processingFilePath = Paths.get(processingModelWrapper.getModel().getProcessingFilePath());
						Files.createDirectories(processingFilePath.getParent());
						wizard.saveOutputFile(processingFilePath.toString());
					} catch (Exception e) {
						logger.error("Could not save template file!", e);
					}
				}
				return processingModelWrapper;
			}
		} catch (Exception e) {
			logger.error("Could not open operation wizard", e);
		}
		return null;
	}

	private void deleteProcessingModel(IScanModelWrapper<ClusterProcessingModel> processingChain) {
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
		relayoutMappingView();
	}

	private void addProcessingModel() {
		IScanModelWrapper<ClusterProcessingModel> modelWrapper = configureProcessingModel(null);
		if (modelWrapper != null) {
			// if the configure wizard wasn't cancelled, add the new processing model
			// to the list of models and create the new row for it in the UI
			IMappingExperimentBean mappingBean = getMappingBean();
			List<IScanModelWrapper<ClusterProcessingModel>> processingModels = mappingBean.getClusterProcessingConfiguration();
			if (processingModels == null) {
				processingModels = new ArrayList<>();
				mappingBean.setClusterProcessingConfiguration(processingModels);
			}
			processingModels.add(modelWrapper);
			addProcessingModelRow(processingChainsComposite, modelWrapper);
			relayoutMappingView();
		}
	}

	private File[] getTemplateFiles() {
		// TODO: consider moving this method to a service
		File templatesDir = new File(getService(IFilePathService.class).getProcessingTemplatesDir());
		String[] names = templatesDir.list((dir, name) -> name.endsWith("." + MappingUIConstants.NEXUS_FILE_EXTENSION));
		if (names == null) {
			return NO_TEMPLATE_FILES;
		}

		return Arrays.stream(names).map(name -> new File(templatesDir, name)).toArray(File[]::new);
	}

	@Override
	public void updateControls() {
		// Update the section controls to reflect the new bean
		// Any additional processing chains in the scan request are added at the end of the list
		// No existing processing chains are deleted, instead they are just deselected
		List<IScanModelWrapper<ClusterProcessingModel>> processingChains = getMappingBean().getClusterProcessingConfiguration();
		if (processingChains != null) {
			for (IScanModelWrapper<ClusterProcessingModel> processingChain : processingChains) {
				if (!rowControlsMap.containsKey(processingChain.getName())) {
					addProcessingModelRow(processingChainsComposite, processingChain);
				}
			}
		}

		dataBindingContext.updateTargets();
	}

}
