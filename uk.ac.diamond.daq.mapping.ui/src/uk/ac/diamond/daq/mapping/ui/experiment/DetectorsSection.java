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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;

/**
 * A section for choosing which detectors should be included in the scan, and for
 * configuring their parameters.
 */
public class DetectorsSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(DetectorsSection.class);

	private static final int DETECTORS_COLUMNS = 3;
	private static final String DETECTOR_SELECTION_KEY_JSON = "detectorSelection.json";

	private DataBindingContext dataBindingContext;

	private Map<String, Button> detectorSelectionCheckboxes;
	private Map<String, Binding> exposureTimeBindings;
	private List<IDetectorModelWrapper> chosenDetectors;
	private Composite controlsComposite;

	@Override
	public void createControls(Composite parent) {
		Composite detectorsComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(detectorsComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorsComposite);
		Label detectorsLabel = new Label(detectorsComposite, SWT.NONE);
		detectorsLabel.setText("Detectors");
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(detectorsLabel);

		Button configure = new Button(detectorsComposite, SWT.PUSH);
		configure.setImage(MappingExperimentUtils.getImage("icons/gear.png"));
		configure.setToolTipText("Select detectors to show");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(configure);
		configure.addListener(SWT.Selection, event -> addDetector(detectorsComposite));

		if (chosenDetectors!=null) createDetectorControls(detectorsComposite, chosenDetectors);
	}

	private void addDetector(Composite parent) {

		DetectorChooser dialog = new DetectorChooser(getShell(),getDetectorParameters(), chosenDetectors);

		if (dialog.open() == Window.OK) {
			chosenDetectors = dialog.getSelectedDetectors();
			createDetectorControls(parent, chosenDetectors);
			mappingView.relayout();
			mappingView.recalculateMinimumSize();
		}
	}

	private void createDetectorControls(Composite parent, List<IDetectorModelWrapper> detectorParametersList) {

		if (controlsComposite!=null) controlsComposite.dispose();
		dataBindingContext = new DataBindingContext();
		int listSize = getDetectorParameters().size();
		detectorSelectionCheckboxes = new HashMap<>(listSize);
		exposureTimeBindings = new HashMap<>(listSize);

		controlsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(controlsComposite);
		GridLayoutFactory.swtDefaults().numColumns(DETECTORS_COLUMNS).margins(0, 0).applyTo(controlsComposite);

		for (IDetectorModelWrapper detectorParameters : detectorParametersList) {
			// create the detector selection checkbox and bind it to the includeInScan property of the wrapper
			Button checkBox = new Button(controlsComposite, SWT.CHECK);
			detectorSelectionCheckboxes.put(detectorParameters.getName(), checkBox);
			checkBox.setText(detectorParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(detectorParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBox.addListener(SWT.Selection, event -> {
				updateStatusLabel();

				if (detectorParameters.getModel() instanceof IMalcolmModel) {
					malcolmDeviceSelectionChanged(detectorParameters.getName());
				}
			});

			// create the exposure time text control and bind it the exposure time property of the wrapper
			Text exposureTimeText = new Text(controlsComposite, SWT.BORDER);
			exposureTimeText.setToolTipText("Exposure time");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);
			IObservableValue exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
			IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(detectorParameters.getModel());
			Binding exposureTimeBinding = dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
			exposureTimeBindings.put(detectorParameters.getName(), exposureTimeBinding);
			exposureTimeText.addListener(SWT.Modify, event -> updateStatusLabel());

			// Edit configuration
			final Button configButton = new Button(controlsComposite, SWT.PUSH);
			configButton.setImage(MappingExperimentUtils.getImage("icons/pencil.png"));
			configButton.setToolTipText("Edit parameters");

			IGuiGeneratorService guiGenerator = getService(IGuiGeneratorService.class);
			configButton.addListener(SWT.Selection, event -> {
				guiGenerator.openDialog(detectorParameters.getModel(), getShell(),
						detectorParameters.getName() + " Parameters");
				dataBindingContext.updateTargets();
			});

		}

		mappingView.relayout();
	}

	/**
	 * Update detector checkboxes based on malcolm device selection being (un)checked.
	 * When the malcolm device is selected, all other detectors should be unchecked and disabled.
	 * @param selectionCheckBoxes
	 * @param malcolmDeviceCheckBox
	 */
	private void malcolmDeviceSelectionChanged(String name) {
		boolean malcolmDeviceSelected = detectorSelectionCheckboxes.get(name).getSelection();
		detectorSelectionCheckboxes.keySet().stream()
											.filter(detName -> !detName.equals(name))
											.map(detName -> detectorSelectionCheckboxes.get(detName))
											.forEach(cb -> {
												cb.setEnabled(!malcolmDeviceSelected);
												if (malcolmDeviceSelected) cb.setSelection(false);
											});

		// set all other detectors as not included in scan? TODO why doesn't jface binding do this automatically?
		if (malcolmDeviceSelected) {
			getMappingBean().getDetectorParameters().stream()
				.filter(detParams -> !detParams.getName().equals(name))
				.forEach(detParams -> ((DetectorModelWrapper) detParams).setIncludeInScan(false));
		}
	}

	private List<IDetectorModelWrapper> getDetectorParameters() {
		// a function to convert DeviceInformations to IDetectorModelWrappers
		Function<DeviceInformation<?>, IDetectorModelWrapper> malcolmInfoToWrapper =
				info -> new DetectorModelWrapper(info.getLabel(), (IDetectorModel) info.getModel(), false);

		// get the DeviceInformation objects for the malcolm devices and apply the function
		// above to create DetectorModelWrappers for them.
		Map<String, IDetectorModelWrapper> malcolmParams = getMalcolmDeviceInfo().stream()
				.map(malcolmInfoToWrapper::apply)
				.collect(Collectors.toMap(IDetectorModelWrapper::getName, Function.identity()));

		// a function to collect Malcolm model names
		final Function<IDetectorModelWrapper, String> getMalcolmModel =	param -> param.getModel().getName();

		// get the set of Malcolm models
		final Set<String> malcolmModels = malcolmParams.values().stream()
				.map(getMalcolmModel::apply)
				.collect(Collectors.toSet());

		// a predicate to filter out malcolm devices which no longer exist
		Predicate<IDetectorModelWrapper> nonExistantMalcolmFilter =
				wrapper -> !(wrapper.getModel() instanceof IMalcolmModel) || malcolmModels.contains(wrapper.getModel().getName());

		// create a name-keyed map from the existing detector parameters in the bean, filtering out those for
		// malcolm devices which no longer exist using the predicate above
		Map<String, IDetectorModelWrapper> detectorParams = getMappingBean().getDetectorParameters().stream().
				filter(nonExistantMalcolmFilter). // filter out malcolm device which no longer exist
				collect(Collectors.toMap(IDetectorModelWrapper::getName, // key by name
						Function.identity(), // the value is the wrapper itself
						(v1, v2) -> v1, // merge function not used as there should be no duplicate keys
						LinkedHashMap::new)); // create a linked hash map to maintain the order

		// merge in the wrappers for the malcolm devices. The merge function here keeps the original
		// wrapper if the mapping bean already contained one for a device with this name
		malcolmParams.forEach((name, params) -> detectorParams.merge(name, params, (v1, v2) -> v1));

		// convert to a list and set this as the detector parameters in the bean
		List<IDetectorModelWrapper> detectorParamList = new ArrayList<>(detectorParams.values());
		getMappingBean().setDetectorParameters(detectorParamList);

		return detectorParamList;
	}

	private Collection<DeviceInformation<?>> getMalcolmDeviceInfo() {
		try {
			IEventService eventService = getService(IEventService.class);
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			IRunnableDeviceService runnableDeviceService = eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
			return runnableDeviceService.getDeviceInformation(DeviceRole.MALCOLM);
		} catch (Exception e) {
			logger.error("Could not get malcolm devices.", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void updateControls() {
		// update the bindings for exposure time as we may have new detector models
		for (IDetectorModelWrapper detectorParams : getMappingBean().getDetectorParameters()) {
			Binding oldBinding = exposureTimeBindings.get(detectorParams.getName());
			IObservableValue exposureTextValue = (IObservableValue) oldBinding.getTarget();
			dataBindingContext.removeBinding(oldBinding);
			oldBinding.dispose();

			IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(detectorParams.getModel());
			Binding newBinding = dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
			exposureTimeBindings.put(detectorParams.getName(), newBinding);
		}

		// update the GUI based on the updated model
		dataBindingContext.updateTargets();

		// if a malcolm device has been de/selected we need to update the checkbox enablement
		Collection<DeviceInformation<?>> malcolmDevices = getMalcolmDeviceInfo();
		if (malcolmDevices != null && !malcolmDevices.isEmpty()) {
			for (DeviceInformation<?> devInfo : malcolmDevices) {
				String name = devInfo.getLabel();
				malcolmDeviceSelectionChanged(name);
			}
		}
	}

	@Override
	protected void saveState(Map<String, String> persistedState) {
		IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
		try {
			persistedState.put(DETECTOR_SELECTION_KEY_JSON, marshaller.marshal(chosenDetectors));
		} catch (Exception e) {
			logger.error("Error saving detector selection", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void loadState(Map<String, String> persistedState) {
		String json = persistedState.get(DETECTOR_SELECTION_KEY_JSON);
		if (json == null || json.isEmpty()) { // This happens when client is reset || if no detectors are configured.
			return;
		}
		IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
		try {
			chosenDetectors = marshaller.unmarshal(json, List.class);
		} catch (Exception e) {
			logger.error("Error loading detector selection", e);
		}
	}

}
