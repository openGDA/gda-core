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
import static uk.ac.diamond.daq.mapping.ui.MappingUIConstants.PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG;
import static uk.ac.gda.ui.tool.ClientMessages.DETECTOR_PARAMETERS_EDIT_TP;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import java.text.DecimalFormat;
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
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.DeviceRole;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.attributes.StringArrayAttribute;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.device.EditDetectorModelDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import gda.rcp.GDAClientActivator;
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

	/** Prefix for the property ids we want to listen to for changes*/
	private static final String PROPERTY_DETECTORS = "uk.ac.diamond.daq.mapping.ui.experiment.detectorssection.detectors";

	private final IPropertyChangeListener propertyChangeListener = this::handlePropertyChange;

	private Map<String, Button> detectorSelectionCheckboxes;
	private List<IScanModelWrapper<IDetectorModel>> visibleDetectors; // the detectors
	private Composite sectionComposite; // parent composite for all controls in the section
	private Composite detectorsComposite;

	private IRunnableDeviceService runnableDeviceService;

	@Override
	public void initialize(MappingExperimentView view) {
		super.initialize(view);
		runnableDeviceService = getRemoteService(IRunnableDeviceService.class);
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		sectionComposite = createComposite(parent, 2, true);
		final Label detectorsLabel = new Label(sectionComposite, SWT.NONE);
		detectorsLabel.setText("Detectors");
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(detectorsLabel);

		// button to open the detector chooser dialog
		final Button configure = new Button(sectionComposite, SWT.PUSH);
		configure.setImage(getImage("icons/gear.png"));
		configure.setToolTipText("Select detectors to show");
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(configure);
		configure.addListener(SWT.Selection, event -> chooseDetectors());

		if (visibleDetectors == null) {
			// this will only be null if loadState() has not been called, i.e. on a workspace reset
			// in this case show all available detectors
			updateDetectorParameters(); // update the detectors in the bean based on the available malcolm devices
			visibleDetectors = getBean().getDetectorParameters().stream()
					.map(DetectorModelWrapper.class::cast)
					.filter(DetectorModelWrapper::isShownByDefault)
					.filter(this::isValidMappingDetector)
					.collect(Collectors.toList());
		}
		createDetectorControls(visibleDetectors);

		// Listen for property changes that affect the selection of detectors
		GDAClientActivator.getDefault().getPreferenceStore().addPropertyChangeListener(propertyChangeListener);
	}

	/*
	 * Majority of MalcolmModels in GDA have their axesToMove not set, so we need to get the device to ask.
	 * If this situation changes we could ask the model directly.
	 */
	private boolean isValidMappingDetector(DetectorModelWrapper<?> model) {
		if (!(model.getModel() instanceof IMalcolmModel)) return true;
		IMalcolmModel malcolmModel = (IMalcolmModel) model.getModel();
		try {
			List<String> malcolmAxes = getMalcolmDevice(malcolmModel.getName()).getAvailableAxes();
			// 1-D Malcolm devices (e.g. Tomography stage) not valid for 2-D mapping scans.
			return malcolmAxes.size() > 1;
		} catch (ScanningException e) {
			logger.error("Could not get axes of malcolm device: {}", malcolmModel.getName(), e);
			return false;
		}
	}

	private void chooseDetectors() {
		final ChooseDevicesDialog<IDetectorModel> dialog = new ChooseDevicesDialog<>(getShell(),
				getBean().getDetectorParameters(), visibleDetectors);
		dialog.setTitle("Choose from available detectors");

		if (dialog.open() == Window.OK) {
			visibleDetectors = dialog.getSelectedDevices();

			// set any detectors not in the new selection to not be included in the scan
			getBean().getDetectorParameters().stream().
				filter(w -> !visibleDetectors.contains(w)).
				forEach(w -> w.setIncludeInScan(false));

			createDetectorControls(visibleDetectors);
			relayoutView();
		}
	}

	/**
	 * Create detector controls
	 *
	 * Warnings are related to binding a text widget with the detector's model exposure time
	 * with a custom {@link NumberToStringConverter} so the exposure time can have 5 decimal places
	 *
	 * @param detectorParametersList
	 */
	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	private void createDetectorControls(List<IScanModelWrapper<IDetectorModel>> detectorParametersList) {
		removeOldBindings(); // remove any old bindings

		if (detectorsComposite != null) detectorsComposite.dispose();
		final DataBindingContext dataBindingContext = getDataBindingContext();
		detectorSelectionCheckboxes = new HashMap<>();

		detectorsComposite = new Composite(sectionComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(detectorsComposite);
		GridLayoutFactory.swtDefaults().numColumns(DETECTORS_COLUMNS).margins(0, 0).applyTo(detectorsComposite);

		final Optional<IScanModelWrapper<IDetectorModel>> selectedMalcolmDevice = Optional.empty();
		for (IScanModelWrapper<IDetectorModel> detectorParameters : detectorParametersList) {
			// create the detector selection checkbox and bind it to the includeInScan property of the wrapper
			final Button checkBox = new Button(detectorsComposite, SWT.CHECK);
			detectorSelectionCheckboxes.put(detectorParameters.getName(), checkBox);
			checkBox.setText(detectorParameters.getName());
			final IObservableValue<Boolean> checkBoxValue = WidgetProperties.buttonSelection().observe(checkBox);
			final IObservableValue<Boolean> activeValue = PojoProperties.value("includeInScan", Boolean.class).observe(detectorParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);
			dataBindingContext.updateTargets(); // sets the checkbox checked if the detector was previously selected
			checkBox.addListener(SWT.Selection, event -> detectorSelectionChanged(detectorParameters));

			final Text exposureTimeText = new Text(detectorsComposite, SWT.BORDER);
			exposureTimeText.setToolTipText("Exposure time");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);

			// add custom converter to allow to set a exposure time value with 5 decimal places
			DecimalFormat decimalFormat = new DecimalFormat("#.#####");
			IConverter<Object, String> decimalConverter = NumberToStringConverter.fromDouble(decimalFormat, false);
			// create the exposure time text control and bind it the exposure time property of the wrapper
			final IObservableValue<String> exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
			final IObservableValue exposureTimeValue = org.eclipse.core.databinding.beans.PojoProperties.value("exposureTime", Double.class).observe(detectorParameters.getModel());
			dataBindingContext.bindValue(exposureTextValue, exposureTimeValue, null, UpdateValueStrategy.create(decimalConverter));
			exposureTimeText.addListener(SWT.Modify, event -> updateStatusLabel());

			// Edit configuration
			final Composite configComposite = new Composite(detectorsComposite, SWT.NONE);
			GridLayoutFactory.fillDefaults().applyTo(configComposite);
			final Button configButton = new Button(configComposite, SWT.PUSH);
			configButton.setImage(getImage("icons/camera.png"));
			configButton.setToolTipText(getMessage(DETECTOR_PARAMETERS_EDIT_TP));
			configButton.addListener(SWT.Selection, event -> editDetectorParameters(detectorParameters));
		}

		// if a malcolm device is selected already, deselect and disable the checkboxes for the other detectors
		selectedMalcolmDevice.ifPresent(this::detectorSelectionChanged);
	}

	private void editDetectorParameters(final IScanModelWrapper<IDetectorModel> detectorParameters) {
		try {
			if (detectorParameters.getModel() instanceof IMalcolmModel && runnableDeviceService.getRunnableDevice(
					detectorParameters.getModel().getName()).getDeviceState() == DeviceState.OFFLINE) {
				MessageDialog.openError(getShell(), "Malcolm Device " + detectorParameters.getModel().getName(),
						"Cannot edit malcolm device " + detectorParameters.getModel().getName() + " as it is offline.");
				return;
			}
		} catch (ScanningException e) {
			logger.error("Cannot get malcolm device", e);
		}

		final Dialog editModelDialog = new EditDetectorModelDialog(getShell(), runnableDeviceService,
				detectorParameters.getModel(), detectorParameters.getName());
		editModelDialog.create();
		if (editModelDialog.open() == Window.OK) {
			getDataBindingContext().updateTargets();
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

			getDataBindingContext().updateModels();

			// update the mapping stage info
			if (selected) {
				updateMappingStage(wrapper);
			}
		} else {
			detectorSelectionCheckboxes.values().stream().forEach(cb -> cb.setEnabled(true));
		}

		updateMappingView();
		updateStatusLabel();
	}

	/**
	 * Update the mapping view when the detector selection has changed
	 */
	private void updateMappingView() {
		getView().detectorSelectionChanged(visibleDetectors.stream()
													.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
													.collect(Collectors.toList()));
	}

	/**
	 * Update the mapping stage scannable names based on the given detector.
	 * This method will only make any changes if the given detector is a malcolm device,
	 * and the value of the {@code axesToMove} attribute of that malcolm device is
	 * a {@link StringArrayAttribute} with a length of 2 or greater.
	 * In this case the fast and slow axes will be changed to the
	 * first and second elements of that array, and if the array is of length 3 or greater,
	 * the associated axis is set to the third element.
	 *
	 * @param wrapper detector model wrapper of the selected detector
	 */
	private void updateMappingStage(IScanModelWrapper<IDetectorModel> wrapper) {
		final String deviceName = wrapper.getModel().getName();
		try {
			// get the axesToMove from the malcolm device
			final MappingStageInfo stageInfo = getEclipseContext().get(MappingStageInfo.class);
			final List<String> malcolmAxes = getMalcolmDevice(deviceName).getAvailableAxes();

			// only update the mapping stage if the malcolm device is configured to move at least two axes.
			if (malcolmAxes.size() < 2) return;

			// if the current fast and slow axes are contained in the malcolm axes, then the mapping stage
			// is already set correctly for the malcolm device, no update is required
			// note if new (2018) malcolm, malcolmAxes will contain all
			boolean updatedFastAndSlowAxes = false;
			if (!malcolmAxes.contains(stageInfo.getPlotXAxisName()) || !malcolmAxes.contains(stageInfo.getPlotYAxisName())) {
				// we assume the order is fast-axis, slow-axes. Malcolm devices must be configured to have this order
				stageInfo.setPlotXAxisName(malcolmAxes.get(0));
				stageInfo.setPlotYAxisName(malcolmAxes.get(1));
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
				final IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
				if (prefs.getBoolean(PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG, true)) {
					String message = "";
					if (updatedFastAndSlowAxes) {
						message += MessageFormat.format("The active fast scan axis for mapping scans has been updated to ''{0}'' and the active slow scan axis to ''{1}''.",
							stageInfo.getPlotXAxisName(), stageInfo.getPlotYAxisName());
					}
					if (updatedAssociatedAxes) {
						message += MessageFormat.format(" The associated axis has been updated to ''{0}''.", stageInfo.getAssociatedAxis());
					} else {
						message += MessageFormat.format(" The associated axis is ''{0}'' and has not been changed.", stageInfo.getAssociatedAxis());
					}
					final MessageDialogWithToggle dialog = MessageDialogWithToggle.openInformation(getShell(), "Mapping Stage", message,
							"Don't show this dialog again", false, null, null);
					prefs.putBoolean(PREFERENCE_KEY_SHOW_MAPPING_STAGE_CHANGED_DIALOG, !dialog.getToggleState());
				}

				// Region and path composites need updating to reflect this change.
				getView().redrawRegionAndPathComposites();
			}
		} catch (ScanningException e) {
			logger.error("Could not get axes of malcolm device: {}", deviceName, e);
		}
	}

	private IMalcolmDevice getMalcolmDevice(final String malcolmDeviceName) throws ScanningException {
		final IRunnableDevice<?> runnableDevice = runnableDeviceService.getRunnableDevice(malcolmDeviceName);
		if (!(runnableDevice instanceof IMalcolmDevice)) {
			throw new ScanningException("Device " + malcolmDeviceName + " is not a malcolm device");
		}

		return (IMalcolmDevice) runnableDevice;
	}

	private Map<String, IScanModelWrapper<IDetectorModel>> updateDetectorParameters() {
		final Function<DeviceInformation<?>, String> infoToOfflineMarker =
				info ->  info.getLabel() + (info.getState().equals(DeviceState.OFFLINE) ? " [*]" : "");
		// a function to convert DeviceInformations to IDetectorModelWrappers
		final Function<DeviceInformation<?>, IScanModelWrapper<IDetectorModel>> malcolmInfoToWrapper =
				info -> {
					final DetectorModelWrapper<IDetectorModel> wrapper = new DetectorModelWrapper<>(infoToOfflineMarker.apply(info), (IDetectorModel) info.getModel(), false);
					wrapper.setShownByDefault(info.isShownByDefault());
					return wrapper;
				};

		// get the DeviceInformation objects for the malcolm devices and apply the function
		// above to create DetectorModelWrappers for them.
		final Map<String, IScanModelWrapper<IDetectorModel>> malcolmParams = getMalcolmDeviceInfos().stream()
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
		final Map<String, IScanModelWrapper<IDetectorModel>> detectorParamsByName = getBean().getDetectorParameters().stream().
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
		getBean().setDetectorParameters(detectorParamList);

		return detectorParamsByName;
	}

	/**
	 * Get the {@link DeviceInformation} object describing malcolm devices that can be used
	 * for mapping, i.e. ones that have 2 axesToMove.
	 * @return
	 */
	private Collection<DeviceInformation<?>> getMalcolmDeviceInfos() {
		try {
			return runnableDeviceService.getDeviceInformation(DeviceRole.MALCOLM);
		} catch (Exception e) {
			logger.error("Could not get malcolm devices.", e);
			return Collections.emptyList();
		}
	}

	@Override
	public void updateControls() {
		// add any detectors in the bean to the list of chosen detectors if not present
		// first create a map of detectors in the mapping bean keyed by name
		final Map<String, IScanModelWrapper<IDetectorModel>> wrappersByName =
				getBean().getDetectorParameters().stream().collect(toMap(
				IScanModelWrapper<IDetectorModel>::getName, identity()));

		// take the list of chosen detectors and replace them with the ones in the mapping bean
		visibleDetectors = visibleDetectors.stream().
			map(wrapper -> wrappersByName.containsKey(wrapper.getName()) ? // replace the wrapper with the one
					wrappersByName.get(wrapper.getName()) : wrapper). // from the map with the same name, if exists
					collect(toCollection(ArrayList::new));

		// add any detectors that are in the bean but not in the list of chosen detectors
		final Set<String> detectorNames = visibleDetectors.stream().map(IScanModelWrapper<IDetectorModel>::getName).collect(toSet());
		visibleDetectors.addAll(
				getBean().getDetectorParameters().stream().
					filter(IScanModelWrapper::isIncludeInScan).
					filter(wrapper -> !detectorNames.contains(wrapper.getName())).
					collect(Collectors.toList()));

		// update the detector controls
		createDetectorControls(visibleDetectors);
	}

	/**
	 * Listen for property changes that may affect detector selection
	 */
	private void handlePropertyChange(PropertyChangeEvent event) {
		final String propertyId = event.getProperty();
		if (propertyId.equals(PROPERTY_DETECTORS)) {
			final Object newValue = event.getNewValue();
			logger.debug("Detector change event received: {}", newValue);
			if (newValue instanceof String && !((String) newValue).isEmpty()) {
				handleDetectorsEvent((String) newValue);
			}
		}
	}

	/**
	 * Handle a change to the "detectors" property
	 * <p>
	 * The property value must be a JSON string that can be parsed to a map of detector name (as shown in GUI) to
	 * exposure time (as double)<br>
	 * In most cases, there will be only one entry in this map, but in principle multiple software-triggered detectors
	 * can be selected.<br>
	 * <p>
	 * The detector controls will be updated to select the appropriate detector(s), set the corresponding exposure time
	 * and deselect all other detectors.
	 *
	 * @param value
	 *            The "detectors" property value (as described above)
	 */
	private void handleDetectorsEvent(String value) {
		try {
			final Gson gson = new Gson();
			@SuppressWarnings("unchecked")
			final Map<String, Double> detectorMap = gson.fromJson(value, HashMap.class);

			// Validate the detectors map.
			validateDetectorMap(detectorMap);

			// Map is valid - update the GUI
			asyncExec(() -> updateFromDetectorMap(detectorMap));
		} catch (Exception e) {
			logger.error("Invalid detectors parameter: {}", value, e);
		}
	}

	/**
	 * Validate the map of detector -> exposure passed as a property change.<br>
	 * This function will throw an exception if:
	 * <ul>
	 * <li>the map does not contain the correct data types</li>
	 * <li>an exposure time is invalid</li>
	 * <li>it contains multiple detectors, if one is a Malcolm detector</li>
	 * </ul>
	 * It will log a warning (but not throw an exception) if the map contains a detector which does not exist or is not
	 * currently visible.
	 *
	 * @param detectorMap
	 *            the map to be checked
	 */
	@SuppressWarnings("cast")
	private void validateDetectorMap(final Map<String, Double> detectorMap) {
		for (Map.Entry<String, Double> e : detectorMap.entrySet()) {
			// The JSON parsing does not check the data types of the map, so do it here
			if (!(e.getKey() instanceof String)) {
				throw new IllegalArgumentException("Invalid detector name: " + e.getKey());
			}
			if (!(e.getValue() instanceof Number)) {
				throw new IllegalArgumentException("Invalid exposure time: " + e.getValue());
			}

			// Check that the detector is visible
			final String detectorName = e.getKey();
			final Optional<IScanModelWrapper<IDetectorModel>> detectorModelWrapper = visibleDetectors.stream()
					.filter(w -> w.getName().equals(detectorName)).findFirst();
			if (!detectorModelWrapper.isPresent()) {
				logger.warn("Detector {} does not exist in this view", detectorName);
				continue;
			}

			// Check exposure time is positive
			final Double exposureTime = e.getValue();
			if (exposureTime.doubleValue() <= 0.0) {
				throw new IllegalArgumentException("Invalid exposure time: " + exposureTime);
			}

			// If one detector is a Malcolm device, it should be the only one set.
			if (detectorModelWrapper.get().getModel() instanceof MalcolmModel && detectorMap.size() > 1) {
				throw new IllegalArgumentException("If a Malcolm detector is selected, no other detector can be selected");
			}
		}
	}

	/**
	 * Update the GUI from the detector map passed as a property
	 * <p>
	 * <ul>
	 * <li>mark the appropriate detector(s) as included in the scan</li>
	 * <li>set the exposure time</li>
	 * </ul>
	 * Call {@link #detectorSelectionChanged(IScanModelWrapper)} & {@link #updateMappingView()} to do the actual GUI
	 * update.
	 *
	 * @param detectorMap
	 *            the detector map to be used
	 */
	private void updateFromDetectorMap(final Map<String, Double> detectorMap) {
		// Update the detector controls
		IScanModelWrapper<IDetectorModel> selectedDetector = null;
		for (IScanModelWrapper<IDetectorModel> wrapper : getBean().getDetectorParameters()) {
			final String detectorName = wrapper.getName();
			final Object exposure = detectorMap.get(detectorName);

			if (exposure instanceof Double) {
				// Detector is to be included in the scan: enable & set exposure time
				wrapper.setIncludeInScan(true);
				wrapper.getModel().setExposureTime((Double) exposure);
				if (wrapper.getModel() instanceof MalcolmModel) {
					updateMappingStage(wrapper);
				}
				selectedDetector = wrapper;
			} else {
				// Detector not included in scan: disable if a Malcolm detector is selected
				wrapper.setIncludeInScan(false);
			}
		}
		getDataBindingContext().updateTargets();
		if (selectedDetector != null) {
			detectorSelectionChanged(selectedDetector);
		}
		updateMappingView();
	}

	@Override
	public void saveState(Map<String, String> persistedState) {
		final IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
		try {
			final List<String> chosenDetectorNames = visibleDetectors.stream().
					map(IScanModelWrapper<IDetectorModel>::getName).
					collect(Collectors.toList());
			persistedState.put(DETECTOR_SELECTION_KEY_JSON, marshaller.marshal(chosenDetectorNames));
		} catch (Exception e) {
			logger.error("Error saving detector selection", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadState(Map<String, String> persistedState) {
		final String json = persistedState.get(DETECTOR_SELECTION_KEY_JSON);
		if (json == null || json.isEmpty()) { // This happens when client is reset || if no detectors are configured.
			return;
		}

		final IMarshallerService marshaller = getEclipseContext().get(IMarshallerService.class);
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

	@Override
	public void dispose() {
		GDAClientActivator.getDefault().getPreferenceStore().removePropertyChangeListener(propertyChangeListener);
		super.dispose();
	}
}
