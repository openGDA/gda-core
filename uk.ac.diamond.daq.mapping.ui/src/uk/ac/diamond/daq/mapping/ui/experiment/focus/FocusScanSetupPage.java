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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.EnergyFocusBean;
import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import uk.ac.diamond.daq.mapping.api.ILineMappingRegion;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.impl.DetectorModelWrapper;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.ui.experiment.EditDetectorParametersDialog;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentUtils;
import uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob;
import uk.ac.diamond.daq.mapping.ui.experiment.PlottingController;
import uk.ac.gda.client.NumberAndUnitsComposite;
import uk.ac.gda.client.NumberUnitsWidgetProperty;

/**
 * Wizard page to set-up a focus scan.
 */
class FocusScanSetupPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(FocusScanSetupPage.class);

	@Inject
	private IEclipseContext injectionContext;

	@Inject
	private UISynchronize uiSync;

	@Inject
	private IScannableDeviceService scannableService;

	@Inject
	private IEventService eventService;

	@Inject
	private IMapFileController mapFileController;

	@Inject
	private FocusScanBean focusScanBean;

	@Inject
	private IMappingExperimentBeanProvider mappingBeanProvider;

	private OneDEqualSpacingModel linePathModel;

	private Label linePathLabel;

	private PathInfoCalculatorJob pathCalculationJob;

	private PlottingController plottingController;

	private PropertyChangeListener regionBeanPropertyChangeListener;

	private PropertyChangeListener pathBeanPropertyChangeListener;

	private DataBindingContext bindingContext = new DataBindingContext();

	private Binding exposureTimeBinding;

	private EnergyFocusEditor energyFocusEditor;

	FocusScanSetupPage() {
		super(FocusScanSetupPage.class.getSimpleName());
		setTitle("Setup Focus Scan");
		setDescription("Draw the line to scan and configure the parameters");
	}

	private void initializePage() {
		ILineMappingRegion lineRegion = focusScanBean.getLineRegion();
		if (lineRegion == null) {
			lineRegion = new LineMappingRegion();
			focusScanBean.setLineRegion(lineRegion);
		}

		regionBeanPropertyChangeListener = event -> updateLineRegion();

		lineRegion.addPropertyChangeListener(regionBeanPropertyChangeListener);

		linePathModel = new OneDEqualSpacingModel();
		pathBeanPropertyChangeListener = event -> updatePoints();
		linePathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);

		pathCalculationJob = createPathCalculationJob();
	}

	private void updateLineRegion() {
		plottingController.updatePlotRegionFrom(focusScanBean.getLineRegion());
		updatePoints();
		updateLinePathLabel();

		// once a line has been drawn the page is complete
		setPageComplete(true);
	}

	private PathInfoCalculatorJob createPathCalculationJob() {
		final PathInfoCalculatorJob job = ContextInjectionFactory.make(PathInfoCalculatorJob.class,
				injectionContext);
		job.setPathInfoConsumer(pathInfo -> uiSync.asyncExec(() -> plottingController.plotPath(pathInfo)));
		job.setScanRegion(focusScanBean.getLineRegion());
		job.setScanPathModel(linePathModel);

		job.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void running(IJobChangeEvent event) {
				uiSync.asyncExec(() -> plottingController.removePath()); // TODO: set status message?
			}

			@Override
			public void done(IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					if (!event.getResult().isOK()) { // TODO: set status message?
						logger.warn("Error in scan path calculation", event.getResult().getException());
					}
				});
			}
		});

		return job;
	}

	@Override
	public void createControl(Composite parent) {
		initializePage();

		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);

		createDataPlotControl(sashForm);

		final Composite composite = new Composite(sashForm, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(composite);

		// Create the controls to setup the focus (zone plate) to scan
		createFocusControls(composite);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL));

		// Create the controls to setup the focus line
		createFocusLineControls(composite);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(
				new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL));

		// Create the controls to show the detector
		createDetectorControls(composite);

		// Create editor for an energy/focus mapping function if there is one
		final EnergyFocusBean energyFocusBean = focusScanBean.getEnergyFocusBean();
		if (energyFocusBean != null) {
			try {
				GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL));
				energyFocusEditor = new EnergyFocusEditor(composite, energyFocusBean);
			} catch (Exception e) {
				logger.error("Error creating energy focus function editor", e);
			}
		}

		sashForm.setWeights(new int[] { 9, 5 });
		setControl(sashForm);
		setPageComplete(false);
	}

	@Override
	public void dispose() {
		plottingController.dispose();

		focusScanBean.getLineRegion().removePropertyChangeListener(regionBeanPropertyChangeListener);
		linePathModel.removePropertyChangeListener(pathBeanPropertyChangeListener);

		exposureTimeBinding.dispose();

		super.dispose();
	}

	@Override
	public void setVisible(boolean visible) {
		if (energyFocusEditor != null) {
			if (visible) {
				energyFocusEditor.refresh();
			} else {
				energyFocusEditor.save();
			}
		}
		super.setVisible(visible);
	}

	private Control createDataPlotControl(Composite parent) {
		try {
			final IPlottingSystem<Composite> plottingSystem = PlottingFactory.createPlottingSystem();
			Control plotControl = MappingExperimentUtils.createDataPlotControl(parent, plottingSystem, getTitle());
			mapFileController.getPlottedObjects().stream().forEach(plot -> drawMapPlot(plottingSystem, plot));
			plottingController = new PlottingController(plottingSystem);
			return plotControl;
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return MappingExperimentUtils.createErrorLabel(parent, message, e);
		}
	}

	private void drawMapPlot(IPlottingSystem<Composite> plottingSystem, PlottableMapObject mapPlot) {
		try {
			final IDataset mapDataset = mapPlot.getMap();
			final IImageTrace trace = MetadataPlotUtils.buildTrace(mapDataset, plottingSystem);
			trace.setGlobalRange(getRange(mapPlot));
			trace.setAlpha(mapPlot.getTransparency());
			plottingSystem.addTrace(trace);
		} catch (Exception e) {
			logger.error("Could not add plot to map: {}", mapPlot.getPath(), e);
		}
	}

	private double[] getRange(PlottableMapObject mapObject) {
		final int[] shape = mapObject.getMap().getShape();

		final double[] range = mapObject.getRange().clone();
		if (range.length != 4) throw new IllegalArgumentException("Range array expected to have size 4");
		if (range[0] == range[1] && range[2] == range[3]) return range;

		// sanitize the range, this code adapted from MapPlotManager.sanizeRange
		if (range[0] == range[1]) {
			range[1] = range[0] + (range[3] - range[2]) / shape[1];
		} else if (range[2] == range[3]) {
			range[3] = range[2] + (range[1] - range[0]) / shape[0];
		}
		return range;
	}

	private void createFocusControls(final Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Focus motion:");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(parent, SWT.NONE);
		label.setText("Centre position:");
		GridDataFactory.swtDefaults().applyTo(label);

		final NumberAndUnitsComposite focusCentre = new NumberAndUnitsComposite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(focusCentre);
		setCurrentFocusPosition();
		bindControlToProperty(focusCentre, "focusCentre", focusScanBean);

		label = new Label(parent, SWT.NONE);
		label.setText("Range (+/-):");
		GridDataFactory.swtDefaults().applyTo(label);

		final NumberAndUnitsComposite focusRange = new NumberAndUnitsComposite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(focusRange);
		bindControlToProperty(focusRange, "focusRange", focusScanBean);

		label = new Label(parent, SWT.NONE);
		label.setText("Number of focus steps:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Spinner numberFocusStepsSpinner = new Spinner(parent, SWT.BORDER);
		numberFocusStepsSpinner.setMinimum(1);
		GridDataFactory.fillDefaults().applyTo(numberFocusStepsSpinner);
		bindControlToProperty(numberFocusStepsSpinner, "numberOfFocusSteps", focusScanBean);
	}

	private void setCurrentFocusPosition() {
		try {
			final IScannable<?> scannable = scannableService.getScannable(focusScanBean.getFocusScannableName());
			final Double focusPos = (Double) scannable.getPosition();
			focusScanBean.setFocusCentre(focusPos);
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error",
					MessageFormat.format("Could not get position of {0}. See error log for details.",
					focusScanBean.getFocusScannableName()));
			logger.error("Could not get position of {}", focusScanBean.getFocusScannableName(), e);
		}
	}

	private void createFocusLineControls(final Composite parent) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText("Focus line:");
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		label = new Label(parent, SWT.SINGLE);
		label.setText("Line Path:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Composite linePathComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(linePathComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(linePathComposite);

		linePathLabel = new Label(linePathComposite, SWT.SINGLE);
		linePathLabel.setText("Draw a line over the feature to focus on");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(label);

		final Button drawLineButton = new Button(linePathComposite, SWT.NONE);
		drawLineButton.setImage(new Image(Display.getCurrent(), getClass().getResourceAsStream("/icons/map--pencil.png")));
		drawLineButton.setToolTipText("Draw/Redraw line");
		GridDataFactory.swtDefaults().applyTo(drawLineButton);
		drawLineButton.addListener(SWT.Selection, e -> drawLine());

		label = new Label(parent, SWT.NONE);
		label.setText("Number of points:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Spinner numPointsSpinner = new Spinner(parent, SWT.BORDER);
		numPointsSpinner.setMinimum(1);
		GridDataFactory.fillDefaults().applyTo(numPointsSpinner);
		bindControlToProperty(numPointsSpinner, "points", linePathModel);
		bindControlToProperty(numPointsSpinner, "numberOfLinePoints", focusScanBean);
	}

	private void drawLine() {
		linePathModel.setBoundingLine(null);
		plottingController.createNewPlotRegion(focusScanBean.getLineRegion());

		updatePoints();
	}

	private void updateLinePathLabel() {
		final ILineMappingRegion lineRegion = focusScanBean.getLineRegion();
		final String labelText = String.format("%.3f, %.3f  ->  %.3f, %.3f",
				lineRegion.getxStart(), lineRegion.getyStart(), lineRegion.getxStop(), lineRegion.getyStop());
		linePathLabel.setText(labelText);
	}

	private void updatePoints() {
		pathCalculationJob.cancel();
		pathCalculationJob.schedule();
	}

	private void createDetectorControls(Composite parent) {
		Label label = new Label(parent, SWT.SINGLE);
		label.setText("Detector:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Composite detectorComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(detectorComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(detectorComposite);

		// Combo to choose detector
		final ComboViewer comboViewer = new ComboViewer(detectorComposite, SWT.READ_ONLY);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((IScanModelWrapper<IDetectorModel>) element).getName();
			}
		});

		// Get detector wrappers from mapping bean and add as input to combo
		comboViewer.addSelectionChangedListener(
				evt -> focusScanBean.setDetector(getDetectorWrapperForSelection(evt.getSelection()).getModel()));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboViewer.getControl());

		final Button configureDetectorButton = new Button(detectorComposite, SWT.PUSH);
		configureDetectorButton.setImage(MappingExperimentUtils.getImage("icons/pencil.png"));
		configureDetectorButton.setToolTipText("Edit parameters");
		configureDetectorButton.addListener(SWT.Selection,
				event -> editDetectorParameters(getDetectorWrapperForSelection(comboViewer.getSelection())));

		final Label exposureTimeLabel = new Label(parent, SWT.NONE);
		exposureTimeLabel.setText("Exposure Time:");

		final Text exposureTimeText = new Text(parent, SWT.BORDER);
		exposureTimeText.setToolTipText("Set the exposure time for this detector");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);

		final IObservableValue exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		comboViewer.addSelectionChangedListener(event -> {
			if (exposureTimeBinding != null) {
				exposureTimeBinding.dispose();
				bindingContext.removeBinding(exposureTimeBinding);
			}

			final Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
			@SuppressWarnings("unchecked")
			final IDetectorModel model = ((IScanModelWrapper<IDetectorModel>) selected).getModel();

			final IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(model);
			exposureTimeBinding = bindingContext.bindValue(exposureTextValue, exposureTimeValue);
		});

		populateDetectorCombo(comboViewer);
	}

	private void populateDetectorCombo(ComboViewer comboViewer) {
		final List<IScanModelWrapper<IDetectorModel>> mappingDetectors =
				mappingBeanProvider.getMappingExperimentBean().getDetectorParameters();

		// get the available detectors for a focus scan
		final List<IScanModelWrapper<IDetectorModel>> availableFocusDetectors;
		// filter out all malcolm devices except the focus malcolm device
		availableFocusDetectors = mappingDetectors.stream()
				.filter(wrapper -> !(wrapper.getModel() instanceof IMalcolmModel))
				.collect(Collectors.toCollection(ArrayList::new));

		final String focusMalcolmDeviceName = focusScanBean.getFocusMalcolmDeviceName();
		IScanModelWrapper<IDetectorModel> focusMalcolmDevice = null;
		if (focusMalcolmDeviceName != null) {
			try {
				@SuppressWarnings("unchecked")
				final DeviceInformation<? extends IMalcolmModel> focusMalcolmDeviceInfo =
						(DeviceInformation<? extends IMalcolmModel>) getRunnableDeviceService().getDeviceInformation(
								focusMalcolmDeviceName);
				focusMalcolmDevice = new DetectorModelWrapper(focusMalcolmDeviceInfo.getLabel(),
						focusMalcolmDeviceInfo.getModel(), true);
				availableFocusDetectors.add(focusMalcolmDevice);
			} catch (ScanningException | EventException e) {
				logger.error("Cannot get focus malcolm device: " + focusMalcolmDeviceName, e);
				MessageDialog.openError(getShell(), "Focus Scan", "Cannot get focus malcolm device: " + focusMalcolmDeviceName +
						". See error log for details.");
			}
		}

		comboViewer.setInput(availableFocusDetectors);

		// get the detector to select as default
		// first get the first included detector in the mapping bean
		final Optional<IScanModelWrapper<IDetectorModel>> firstSelected = mappingDetectors.stream()
				.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
				.findFirst();
		// use that as the default detector as long as it's not a malcolm device
		Optional<IScanModelWrapper<IDetectorModel>> optDefaultDetector =
				firstSelected.filter(wrapper -> !(wrapper.getModel() instanceof IMalcolmModel));

		if (!optDefaultDetector.isPresent()) {
			// if the selected device is a malcolm device, use the focus malcolm device if present
			if (firstSelected.map(IScanModelWrapper<IDetectorModel>::getModel).filter(IMalcolmModel.class::isInstance).isPresent()
					&& focusMalcolmDevice != null) {
				optDefaultDetector = Optional.of(focusMalcolmDevice);
			}
			// if we still don't have a detector use the first available one
			if (!optDefaultDetector.isPresent()) {
				optDefaultDetector = availableFocusDetectors.isEmpty() ? Optional.empty() : Optional.of(availableFocusDetectors.get(0));
			}
		}

		// set the initially selected detector
		if (optDefaultDetector.isPresent()) {
			IScanModelWrapper<IDetectorModel> defaultDectector = optDefaultDetector.get();
			comboViewer.setSelection(new StructuredSelection(defaultDectector));

			// If we're using the focus malcolm device, compare the axes with the selected malcolm device
			if (defaultDectector.getModel() instanceof IMalcolmModel &&
					firstSelected.isPresent() && firstSelected.get().getModel() instanceof IMalcolmModel) {
				checkMalcolmDeviceAxes(firstSelected.get(), defaultDectector);
			}
		}
	}

	/**
	 * Compare the axes to move of the focus malcolm device with the selected malcolm device in the mapping bean.
	 * l
	 * @param selectedMappingMalcolmDevice
	 * @param focusMalcolmDevice
	 */
	private void checkMalcolmDeviceAxes(IScanModelWrapper<IDetectorModel> selectedMappingMalcolmDevice, IScanModelWrapper<IDetectorModel> focusMalcolmDevice) {
		try {
			final IRunnableDeviceService runnableDeviceService = getRunnableDeviceService();
			final List<String> focusDeviceAxes = getMalcolmAxes(focusMalcolmDevice, runnableDeviceService);
			final List<String> mappingDeviceAxes = getMalcolmAxes(selectedMappingMalcolmDevice, runnableDeviceService);
			if (mappingDeviceAxes.stream().anyMatch(axis -> !focusDeviceAxes.contains(axis))) {
				MessageDialog.openWarning(getShell(), "Focus Scan",
						MessageFormat.format("Note: the selected malcolm device is configured for different axes that the currently selected malcolm device in the Mapping Experiment View:\n"
						+ "Axes for the mapping device ''{0}'': {1}\n"
						+ "Axes for the focus scan device ''{2}'': {3}",
						selectedMappingMalcolmDevice.getName(), mappingDeviceAxes, focusMalcolmDevice.getName(), focusDeviceAxes));
			}
			if (!focusDeviceAxes.contains(focusScanBean.getFocusScannableName())) {
				MessageDialog.openWarning(getShell(), "Focus Scan", "The selected malcolm device does not include the focus scannable as an axis");
			}
		} catch (ScanningException | EventException e) {
			logger.error("Could not get malcolm axes", e);
			MessageDialog.openError(getShell(), "Focus Scan", "Could not get axes for malcolm device. See error log for more details");
		}
	}

	public IRunnableDeviceService getRunnableDeviceService() throws EventException {
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return  eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
		} catch (URISyntaxException e) {
			throw new EventException("Malformed URI for activemq", e);
		}
	}

	private List<String> getMalcolmAxes(IScanModelWrapper<IDetectorModel> wrapper, IRunnableDeviceService runnableDeviceService) throws ScanningException {
		final String deviceName = wrapper.getModel().getName();
		final IMalcolmDevice<?> malcolmDevice = (IMalcolmDevice<?>) runnableDeviceService.getRunnableDevice(deviceName);
		return malcolmDevice.getAvailableAxes();
	}

	private void editDetectorParameters(IScanModelWrapper<IDetectorModel> detectorModelWrapper) {
		final EditDetectorParametersDialog editDialog = new EditDetectorParametersDialog(
				getShell(), injectionContext, detectorModelWrapper);
		editDialog.create();
		editDialog.open();
		// The dialog updates the model live, using , so we don't have to do anything here
	}

	@SuppressWarnings("unchecked")
	private static IScanModelWrapper<IDetectorModel> getDetectorWrapperForSelection(ISelection selection) {
		final IStructuredSelection sel = (IStructuredSelection) selection;
		return (IScanModelWrapper<IDetectorModel>) sel.getFirstElement();
	}

	private void bindControlToProperty(Control control, String propertyName, Object bean) {
		final IObservableValue model = BeanProperties.value(propertyName).observe(bean);
		final IObservableValue target;
		if (control instanceof NumberAndUnitsComposite) {
			target = new NumberUnitsWidgetProperty().observe(control);
		} else if (control instanceof Text) {
			target = WidgetProperties.text(SWT.Modify).observe(control);
		} else {
			target = WidgetProperties.selection().observe(control);
		}
		bindingContext.bindValue(target, model);
	}

}
