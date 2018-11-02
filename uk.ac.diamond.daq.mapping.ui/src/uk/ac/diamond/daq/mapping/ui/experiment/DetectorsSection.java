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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.ui.Activator;

/**
 * A section for choosing which detectors should be included in the scan, and for
 * configuring their parameters.
 */
public class DetectorsSection extends AbstractMappingSection {

	private static final Logger logger = LoggerFactory.getLogger(DetectorsSection.class);

	private static final int DETECTORS_COLUMNS = 3;
	private static final String DETECTOR_SELECTION_KEY_JSON = "detectorSelection.json";

	private static final String PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG = "showMappingStageChangeDialog";

	private Map<String, Button> detectorSelectionCheckboxes;
	private List<IScanModelWrapper<IDetectorModel>> visibleDetectors; // the detectors
	private Composite sectionComposite; // parent composite for all controls in the section
	private Composite detectorsComposite;

	@Override
	public void createControls(Composite parent) {
		sectionComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(sectionComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		Label detectorsLabel = new Label(sectionComposite, SWT.NONE);
		detectorsLabel.setText("Detectors");
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(detectorsLabel);

		// button to open the detector chooser dialog
		Button configure = new Button(sectionComposite, SWT.PUSH);
		configure.setImage(MappingExperimentUtils.getImage("icons/gear.png"));
		configure.setToolTipText("Select detectors to show");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(configure);
		configure.addListener(SWT.Selection, event -> chooseDetectors());

		if (visibleDetectors == null) {
			// this will only be null if loadState() has not been called, i.e. on a workspace reset
			// in this case show all available detectors
			updateDetectorParameters(); // update the detectors in the bean based on the available malcolm devices
			visibleDetectors = getMappingBean().getDetectorParameters();
		}
		createDetectorControls(visibleDetectors);
	}

	private void chooseDetectors() {
		ChooseDevicesDialog<IDetectorModel> dialog = new ChooseDevicesDialog<>(getShell(),
				getMappingBean().getDetectorParameters(), visibleDetectors);
		dialog.setTitle("Choose from available detectors");

		if (dialog.open() == Window.OK) {
			visibleDetectors = dialog.getSelectedDevices();

			// set any detectors not in the new selection to not be included in the scan
			getMappingBean().getDetectorParameters().stream().
				filter(w -> !visibleDetectors.contains(w)).
				forEach(w -> w.setIncludeInScan(false));

			createDetectorControls(visibleDetectors);
			relayoutMappingView();
		}
	}

	private void createDetectorControls(List<IScanModelWrapper<IDetectorModel>> detectorParametersList) {
		removeOldBindings(); // remove any old bindings

		if (detectorsComposite != null) detectorsComposite.dispose();
		dataBindingContext = new DataBindingContext();
		detectorSelectionCheckboxes = new HashMap<>();

		detectorsComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(detectorsComposite);
		GridLayoutFactory.swtDefaults().numColumns(DETECTORS_COLUMNS).margins(0, 0).applyTo(detectorsComposite);

		Optional<IScanModelWrapper<IDetectorModel>> selectedMalcolmDevice = Optional.empty();
		for (IScanModelWrapper<IDetectorModel> detectorParameters : detectorParametersList) {
			// create the detector selection checkbox and bind it to the includeInScan property of the wrapper
			Button checkBox = new Button(detectorsComposite, SWT.CHECK);
			detectorSelectionCheckboxes.put(detectorParameters.getName(), checkBox);
			checkBox.setText(detectorParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(detectorParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);
			dataBindingContext.updateTargets(); // sets the checkbox checked if the detector was previously selected
			checkBox.addListener(SWT.Selection, event -> detectorSelectionChanged(detectorParameters));

			// create the exposure time text control and bind it the exposure time property of the wrapper
			Text exposureTimeText = new Text(detectorsComposite, SWT.BORDER);
			exposureTimeText.setToolTipText("Exposure time");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);
			IObservableValue exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
			IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(detectorParameters.getModel());
			dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
			exposureTimeText.addListener(SWT.Modify, event -> updateStatusLabel());

			// Edit configuration
			final Composite configComposite = new Composite(detectorsComposite, SWT.NONE);
			GridLayoutFactory.fillDefaults().applyTo(configComposite);
			final Button configButton = new Button(configComposite, SWT.PUSH);
			configButton.setImage(MappingExperimentUtils.getImage("icons/camera.png"));
			if (detectorParameters.getModel() instanceof IMalcolmModel) {
				// DAQ-1531 AcquireRequest does not work for MalcolmDevices
				// so there is no point in allowing the configButton to be used
				configButton.setEnabled(false);
				configComposite.setToolTipText("Configuration dialog for Malcolm detectors unsupported");

				if (detectorParameters.isIncludeInScan()) {
					selectedMalcolmDevice = Optional.of(detectorParameters);
				}

			} else {
				configButton.setToolTipText("Edit parameters");
				configButton.addListener(SWT.Selection, event -> editDetectorParameters(detectorParameters));
			}
		}

		// if a malcolm device is selected already, deselect and disable the checkboxes for the other detectors
		selectedMalcolmDevice.ifPresent(this::detectorSelectionChanged);
	}

	private void editDetectorParameters(final IScanModelWrapper<IDetectorModel> detectorParameters) {
		final EditDetectorParametersDialog editDialog = new EditDetectorParametersDialog(getShell(), getEclipseContext(), detectorParameters);
		editDialog.create();
		if (editDialog.open() == Window.OK) {
			dataBindingContext.updateTargets();
		}
	}

	/**
	 * Update detector checkboxes based on malcolm device selection being (un)checked.
	 * When the malcolm device is selected, all other detectors should be unchecked and disabled.
	 * @param selected
	 * @param selectionCheckBoxes
	 * @param malcolmDeviceCheckBox
	 */
	private void detectorSelectionChanged(IScanModelWrapper<IDetectorModel> wrapper) {
		if (wrapper.getModel() instanceof IMalcolmModel) {
			final String label = wrapper.getName();
			final boolean selected = wrapper.isIncludeInScan();

			detectorSelectionCheckboxes.keySet().stream().
				filter(detName -> !detName.equals(label)).
				map(detName -> detectorSelectionCheckboxes.get(detName)).
				forEach(cb -> {
					cb.setEnabled(!selected);
					if (selected) cb.setSelection(false);
				});

			dataBindingContext.updateModels();

			// update the mapping stage info
			if (selected) {
				updateMappingStage(wrapper);
			}
		}

		getMappingView().detectorSelectionChanged(visibleDetectors.stream()
													.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
													.collect(Collectors.toList()));
		updateStatusLabel();
	}

	/**
	 * Update the mapping stage scannable names based on the given detector.
	 * This method will only make any changes if the given detector is a malcolm device,
	 * and the value of the {@code axesToMove} attribute of that malcolm device is
	 * a {@link StringArrayAttribute} with a length of exactly 2.
	 * In this case the fast and slow axes will be changed to the
	 * first and second elements of that array.
	 *
	 * @param wrapper detetor model wrapper of the selected detector
	 */
	private void updateMappingStage(IScanModelWrapper<IDetectorModel> wrapper) {
		final String deviceName = wrapper.getModel().getName();
		try {
			// get the axesToMove from the malcolm device
			final MappingStageInfo stageInfo = getEclipseContext().get(MappingStageInfo.class);
			final List<String> malcolmAxes = getMalcolmDeviceAxes(deviceName);

			// only update the mapping stage if the malcolm device is configured to move at least two axes.
			if (malcolmAxes.size() < 2) return;

			// if the current fast and slow axes are contained in the malcolm axes, then the mapping stage
			// is already set correctly for the malcolm device, no update is required
			// note if new (2018) malcolm, malcolmAxes will contain all
			boolean updatedFastAndSlowAxes = false;
			if (!malcolmAxes.contains(stageInfo.getActiveFastScanAxis()) || !malcolmAxes.contains(stageInfo.getActiveSlowScanAxis())) {
				// we assume the order is fast-axis, slow-axes. Malcolm devices must be configured to have this order
				stageInfo.setActiveFastScanAxis(malcolmAxes.get(0));
				stageInfo.setActiveSlowScanAxis(malcolmAxes.get(1));
				updatedFastAndSlowAxes = true;
			}

			boolean updatedAssociatedAxes = false;
			if (malcolmAxes.size() > 2 && !malcolmAxes.contains(stageInfo.getAssociatedAxis())) {
				// for a 3+ dimension malcolm device, we can set the z-axis as well
				stageInfo.setAssociatedAxis(malcolmAxes.get(2));
				updatedAssociatedAxes = true;
			}

			if (updatedFastAndSlowAxes || updatedAssociatedAxes) {
				// show a dialog to inform the user of the change (unless overridden in the preferences)
				IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
				if (prefs.getBoolean(PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG, true)) {
					String message = "";
					if (updatedFastAndSlowAxes) {
						message += MessageFormat.format("The active fast scan axis for mapping scans has been updated to ''{0}'' and the active slow scan axis to ''{1}''.",
							stageInfo.getActiveFastScanAxis(), stageInfo.getActiveSlowScanAxis());
					}
					if (updatedAssociatedAxes) {
						message += MessageFormat.format(" The associated axis has been updated to ''{0}''.", stageInfo.getAssociatedAxis());
					} else {
						message += MessageFormat.format(" The associated axis is ''{0}'' and has not been changed.", stageInfo.getAssociatedAxis());
					}
					MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(getShell(), "Mapping Stage", message,
							"Don't show this dialog again", false, null, null);
					prefs.putBoolean(PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG, !dialog.getToggleState());
				}

				// Region and path composites need updating to reflect this change.
				getMappingView().redrawRegionAndPathComposites();
			}
		} catch (ScanningException | EventException e) {
			logger.error("Could not get axes of malcolm device: {}", deviceName, e);
		}
	}

	private List<String> getMalcolmDeviceAxes(final String malcolmDeviceName) throws ScanningException, EventException {
		IRunnableDevice<?> runnableDevice = getRunnableDeviceService().getRunnableDevice(malcolmDeviceName);
		if (!(runnableDevice instanceof IMalcolmDevice)) {
			throw new ScanningException("Device " + malcolmDeviceName + " is not a malcolm device");
		}

		return ((IMalcolmDevice<?>) runnableDevice).getAvailableAxes();
	}

	private boolean isNewMalcolmVersion(final String malcolmDeviceName) throws ScanningException, EventException {
		IRunnableDevice<?> runnableDevice = getRunnableDeviceService().getRunnableDevice(malcolmDeviceName);
		if (!(runnableDevice instanceof IMalcolmDevice)) {
			throw new ScanningException("Device " + malcolmDeviceName + " is not a malcolm device");
		}

		return ((IMalcolmDevice<?>) runnableDevice).isNewMalcolmVersion();
	}

	private Map<String, IScanModelWrapper<IDetectorModel>> updateDetectorParameters() {
		// a function to convert DeviceInformations to IDetectorModelWrappers
		final Function<DeviceInformation<?>, IScanModelWrapper<IDetectorModel>> malcolmInfoToWrapper =
				info -> new DetectorModelWrapper(info.getLabel(), (IDetectorModel) info.getModel(), false);

		// get the DeviceInformation objects for the malcolm devices and apply the function
		// above to create DetectorModelWrappers for them.
		final Map<String, IScanModelWrapper<IDetectorModel>> malcolmParams = getMappingMalcolmDeviceInfos().stream()
				.map(malcolmInfoToWrapper::apply)
				.collect(toMap(IScanModelWrapper<IDetectorModel>::getName, identity()));

		// get the set of names of Malcolm devices
		final Set<String> malcolmDeviceNames = malcolmParams.values().stream()
				.map(IScanModelWrapper<IDetectorModel>::getModel).map(IDetectorModel::getName)
				.collect(Collectors.toSet());

		// a predicate to filter out malcolm devices which no longer exist
		final Predicate<IScanModelWrapper<IDetectorModel>> nonExistantMalcolmFilter =
				wrapper -> !(wrapper.getModel() instanceof IMalcolmModel) || malcolmDeviceNames.contains(wrapper.getModel().getName());

		// create a name-keyed map from the existing detector parameters in the bean, filtering out those for
		// malcolm devices which no longer exist using the predicate above
		final Map<String, IScanModelWrapper<IDetectorModel>> detectorParamsByName = getMappingBean().getDetectorParameters().stream().
				filter(nonExistantMalcolmFilter). // filter out malcolm device which no longer exist
				collect(toMap(IScanModelWrapper<IDetectorModel>::getName, // key by name
						identity(), // the value is the wrapper itself
						(v1, v2) -> v1, // merge function not used as there should be no duplicate keys
						LinkedHashMap::new)); // create a linked hash map to maintain the order

		// merge in the wrappers for the malcolm devices. The merge function here keeps the original
		// wrapper if the mapping bean already contained one for a device with this name
		malcolmParams.forEach((name, params) -> detectorParamsByName.merge(name, params, (v1, v2) -> v1));

		// convert to a list and set this as the detector parameters in the bean
		final List<IScanModelWrapper<IDetectorModel>> detectorParamList = new ArrayList<>(detectorParamsByName.values());
		getMappingBean().setDetectorParameters(detectorParamList);

		return detectorParamsByName;
	}

	/**
	 * Get the {@link DeviceInformation} object describing malcolm devices that can be used
	 * for mapping, i.e. ones that have 2 axesToMove.
	 * @return
	 */
	private Collection<DeviceInformation<?>> getMappingMalcolmDeviceInfos() {
		try {
			// Filter out malcolm devices with more than 2 axes, unless we're using new malcolm (note: this will show all malcolm device we can't connect to)
			return getRunnableDeviceService().getDeviceInformation(DeviceRole.MALCOLM).stream()
					.filter(info -> info.isNewMalcolm() || (info.getAvailableAxes() != null && info.getAvailableAxes().size() <= 2))
					.collect(toList());
		} catch (Exception e) {
			logger.error("Could not get malcolm devices.", e);
			return Collections.emptyList();
		}
	}

	@Override
	protected void updateControls() {
		// add any detectors in the bean to the list of chosen detectors if not present
		// first create a map of detectors in the mapping bean keyed by name
		final Map<String, IScanModelWrapper<IDetectorModel>> wrappersByName =
				getMappingBean().getDetectorParameters().stream().collect(toMap(
				IScanModelWrapper<IDetectorModel>::getName, identity()));

		// take the list of chosen detectors and replace them with the ones in the mapping bean
		visibleDetectors = visibleDetectors.stream().
			map(wrapper -> wrappersByName.containsKey(wrapper.getName()) ? // replace the wrapper with the one
					wrappersByName.get(wrapper.getName()) : wrapper). // from the map with the same name, if exists
					collect(toCollection(ArrayList::new));

		// add any detectors that are in the bean but not in the list of chosen detectors
		final Set<String> detectorNames = visibleDetectors.stream().map(IScanModelWrapper<IDetectorModel>::getName).collect(toSet());
		visibleDetectors.addAll(
				getMappingBean().getDetectorParameters().stream().
					filter(IScanModelWrapper::isIncludeInScan).
					filter(wrapper -> !detectorNames.contains(wrapper.getName())).
					collect(Collectors.toList()));

		// update the detector controls
		createDetectorControls(visibleDetectors);
	}

	@Override
	protected void saveState(Map<String, String> persistedState) {
		IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
		try {
			List<String> chosenDetectorNames = visibleDetectors.stream().
					map(IScanModelWrapper<IDetectorModel>::getName).
					collect(Collectors.toList());
			persistedState.put(DETECTOR_SELECTION_KEY_JSON, marshaller.marshal(chosenDetectorNames));
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
			final Map<String, IScanModelWrapper<IDetectorModel>> detectorParamsByName = updateDetectorParameters();
			final List<String> chosenDetectorNames = marshaller.unmarshal(json, ArrayList.class);

			visibleDetectors = chosenDetectorNames.stream().
				map(detectorParamsByName::get).
				filter(Objects::nonNull).
				collect(toList());
		} catch (Exception e) {
			logger.error("Error loading detector selection", e);
		}
	}

}
