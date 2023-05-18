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

import static uk.ac.diamond.daq.mapping.api.constants.RegionConstants.UPDATE_COMPLETE;
import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.createNumberAndUnitsLengthComposite;

import java.beans.PropertyChangeListener;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.measure.quantity.Length;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.dawnsci.mapping.ui.datamodel.PlottableMapObject;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.conversion.text.NumberToStringConverter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.MetadataPlotUtils;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
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
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.device.EditDetectorModelDialog;
import org.eclipse.scanning.device.ui.util.ScanningUiUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
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

import com.ibm.icu.text.DecimalFormat;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.EnergyFocusBean;
import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import uk.ac.diamond.daq.mapping.api.ILineMappingRegion;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfoRequest;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.ui.Activator;
import uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob;
import uk.ac.diamond.daq.mapping.ui.experiment.PlottingController;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathMapper;
import uk.ac.diamond.daq.mapping.ui.path.MappingPathInfoCalculator;
import uk.ac.gda.client.NumberAndUnitsComposite;
import uk.ac.gda.client.NumberUnitsWidgetProperty;

/**
 * Wizard page to set-up a focus scan.
 */
class FocusScanSetupPage extends WizardPage {

	private static final Logger logger = LoggerFactory.getLogger(FocusScanSetupPage.class);

	private static final String AXES_ALL_MALCOLM = "Malcolm moving all axes";
	private static final String AXES_ALL_GDA = "GDA moving all axes";
	private static final String AXES_MALCOLM_GDA = "Malcolm moving mapping axes, GDA moving focus axis";
	private static final String AXES_GDA_MALCOLM = "GDA moving mapping axes, Malcolm moving focus axis";

	@Inject
	private UISynchronize uiSync;

	@Inject
	private IScannableDeviceService scannableService;

	@Inject
	private IEventService eventService;

	@Inject
	private IPointGeneratorService pointGenService;

	@Inject
	private IMapFileController mapFileController;

	@Inject
	private FocusScanBean focusScanBean;

	@Inject
	private IMappingExperimentBeanProvider mappingBeanProvider;

	private TwoAxisLinePointsModel linePathModel;

	/**
	 * Instructions for drawing a line over the desired feature
	 */
	private CLabel linePathLabel;

	/**
	 *  Show whether the scan uses GDA or Malcolm scannables, or both
	 */
	private Label axesLabel;

	/**
	 * Text boxes showing the end points of the current line
	 */
	private Composite linePathEndPointsComposite;

	/**
	 * Stack composite that shows either {@link #linePathLabel} or {@link #linePathEndPointsComposite} depending on
	 * whether the user has drawn a line
	 */
	private Composite linePathStackComposite;

	/**
	 * Layout for {@link #linePathStackComposite}
	 */
	private StackLayout linePathStackLayout;

	private PathInfoCalculatorJob<MappingPathInfoRequest, MappingPathInfo> pathCalculationJob;

	private PlottingController plottingController;

	private PropertyChangeListener regionBeanPropertyChangeListener;

	private PropertyChangeListener pathBeanPropertyChangeListener;

	private DataBindingContext bindingContext = new DataBindingContext();

	private Binding exposureTimeBinding;

	private EnergyFocusEditor energyFocusEditor;

	private UpdateValueStrategy<Object, String> endPointTextUpdateStrategy;

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

		// Only trigger path calculation once region limits has been adjusted
		regionBeanPropertyChangeListener = event -> {
			if (event.getPropertyName().equals(UPDATE_COMPLETE)) {
				updateLineRegion();
			}
		};

		lineRegion.addPropertyChangeListener(regionBeanPropertyChangeListener);

		linePathModel = new TwoAxisLinePointsModel();
		pathBeanPropertyChangeListener = event -> updatePoints();
		linePathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);
		// Ensure the line boundaries are kept up to date with the model
		linePathModel.addPropertyChangeListener(evt -> mapRegionOntoModel());

		pathCalculationJob = createPathCalculationJob();

		endPointTextUpdateStrategy = new UpdateValueStrategy<Object, String>();
		endPointTextUpdateStrategy.setConverter(new EndPointNumberConverter());
	}

	private void mapRegionOntoModel() {
		RegionAndPathMapper.mapRegionOntoModel(focusScanBean.getLineRegion(), linePathModel);
	}

	private void updateLineRegion() {
		plottingController.updatePlotRegionFrom(focusScanBean.getLineRegion());
		updatePoints();

		// Show the end point text boxes
		linePathStackLayout.topControl = linePathEndPointsComposite;
		linePathStackComposite.layout();

		// once a line has been drawn the page is complete
		setPageComplete(true);
	}

	private PathInfoCalculatorJob<MappingPathInfoRequest, MappingPathInfo> createPathCalculationJob() {

		final MappingPathInfoCalculator pathInfoCalculator = new MappingPathInfoCalculator(pointGenService);
		final PathInfoCalculatorJob<MappingPathInfoRequest, MappingPathInfo> job =
				new PathInfoCalculatorJob<>(pathInfoCalculator, this::plotPath);

		job.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void running(IJobChangeEvent event) {
				uiSync.asyncExec(() -> plottingController.removePath()); // TODO: set status message?
			}

			@Override
			public void done(IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					if (event.getResult() == Status.CANCEL_STATUS)
						logger.info("Scan path calculation cancelled");
					else if (!event.getResult().isOK())  // TODO: set status message?
						logger.warn("Error in scan path calculation", event.getResult().getException());
				});
			}
		});

		return job;
	}

	private void plotPath(MappingPathInfo pathInfo) {
		uiSync.asyncExec(() -> plottingController.plotPath(pathInfo));
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
				final Composite energyFocusComposite = new Composite(composite, SWT.NONE);
				GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(energyFocusComposite);
				GridLayoutFactory.fillDefaults().applyTo(energyFocusComposite);

				GridDataFactory.fillDefaults().grab(true, false).applyTo(new Label(energyFocusComposite, SWT.SEPARATOR | SWT.HORIZONTAL));
				energyFocusEditor = new EnergyFocusEditor(energyFocusComposite, energyFocusBean);
			} catch (Exception e) {
				logger.error("Error creating energy focus function editor", e);
			}
		}

		sashForm.setWeights(new int[] { 20, 15 });
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
			Control plotControl = ScanningUiUtils.createDataPlotControl(parent, plottingSystem, getTitle());
			mapFileController.getPlottedObjects().stream().forEach(plot -> drawMapPlot(plottingSystem, plot));
			plottingController = new PlottingController(plottingSystem);
			return plotControl;
		} catch (Exception e) {
			final String message = "Could not create plotting system";
			logger.error(message, e);
			return ScanningUiUtils.createErrorLabel(parent, message, e);
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

		final NumberAndUnitsComposite<Length> focusCentre = createNumberAndUnitsLengthComposite(parent);
		GridDataFactory.fillDefaults().applyTo(focusCentre);
		setCurrentFocusPosition();
		bindControlToProperty(focusCentre, "focusCentre", focusScanBean);

		label = new Label(parent, SWT.NONE);
		label.setText("Range (+/-):");
		GridDataFactory.swtDefaults().applyTo(label);

		final NumberAndUnitsComposite<Length> focusRange = createNumberAndUnitsLengthComposite(parent);
		GridDataFactory.fillDefaults().applyTo(focusRange);
		bindControlToProperty(focusRange, "focusRange", focusScanBean);

		label = new Label(parent, SWT.NONE);
		label.setText("Number of focus steps:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Spinner numberFocusStepsSpinner = new Spinner(parent, SWT.BORDER);
		numberFocusStepsSpinner.setMinimum(1);
		GridDataFactory.fillDefaults().applyTo(numberFocusStepsSpinner);
		bindControlToProperty(numberFocusStepsSpinner, "numberOfFocusSteps", focusScanBean);

		axesLabel = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(axesLabel);
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
		GridDataFactory.fillDefaults().grab(true, false).applyTo(linePathComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(linePathComposite);

		createLineComposite(linePathComposite);

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

	/**
	 * Creates a {@link Composite} with a {@link StackLayout}<br>
	 * This initially shows a message to the user, but when a line is drawn, it shows the x & y coordinates of the ends
	 * of the line.
	 */
	private void createLineComposite(Composite parent) {
		linePathStackComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(linePathStackComposite);
		linePathStackLayout = new StackLayout();
		linePathStackComposite.setLayout(linePathStackLayout);

		// Initial message
		linePathLabel = new CLabel(linePathStackComposite, SWT.CENTER);
		linePathLabel.setAlignment(SWT.LEFT);
		GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(linePathLabel);
		linePathLabel.setText("Draw a line over the feature to focus on");

		// x & y coordinates of the line
		linePathEndPointsComposite = new Composite(linePathStackComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(6).applyTo(linePathEndPointsComposite);

		createEndPointLabel(linePathEndPointsComposite, "From:");
		final Text xStartText = createEndPointText(linePathEndPointsComposite);
		final Text yStartText = createEndPointText(linePathEndPointsComposite);
		createEndPointLabel(linePathEndPointsComposite, "to:");
		final Text xStopText = createEndPointText(linePathEndPointsComposite);
		final Text yStopText = createEndPointText(linePathEndPointsComposite);

		// Bind the end point text boxes to the FocusScanBean, and thereby to the line plot
		final ILineMappingRegion lineMappingRegion = focusScanBean.getLineRegion();
		bindEndPointTextToProperty(xStartText, "xStart", lineMappingRegion);
		bindEndPointTextToProperty(yStartText, "yStart", lineMappingRegion);
		bindEndPointTextToProperty(xStopText, "xStop", lineMappingRegion);
		bindEndPointTextToProperty(yStopText, "yStop", lineMappingRegion);

		// Initially show the instructions for drawing the line
		linePathStackLayout.topControl = linePathLabel;
	}

	private void bindEndPointTextToProperty(Text control, String propertyName, Object bean) {
		final IObservableValue<Object> model = BeanProperties.value(propertyName).observe(bean);
		final IObservableValue<String> target = WidgetProperties.text(SWT.Modify).observe(control);
		bindingContext.bindValue(target, model, null, endPointTextUpdateStrategy);
	}

	private static void createEndPointLabel(Composite parent, String text) {
		final Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
		label.setText(text);
	}

	private static Text createEndPointText(Composite parent) {
		final Text text = new Text(parent, SWT.BORDER);
		GridDataFactory.fillDefaults().hint(85, SWT.DEFAULT).applyTo(text);
		return text;
	}

	private void drawLine() {
		linePathModel.setBoundingLine(null);
		plottingController.createNewPlotRegion(focusScanBean.getLineRegion());

		// Restore the instructions for drawing the line
		linePathStackLayout.topControl = linePathLabel;
		linePathStackComposite.layout();

		updatePoints();
	}

	private void updatePoints() {
		pathCalculationJob.cancel();
		// Ensure the job is using the latest model and ROI
		pathCalculationJob.setPathInfoRequest(MappingPathInfoRequest.builder()
				.withSourceId(FocusScanSetupPage.class.getName())
				.withScanPathModel(linePathModel)
				.withScanRegion(focusScanBean.getLineRegion().toROI())
				.build());
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
		final ComboViewer detectorCombo = new ComboViewer(detectorComposite, SWT.READ_ONLY);
		detectorCombo.setContentProvider(ArrayContentProvider.getInstance());
		detectorCombo.setLabelProvider(new LabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getText(Object element) {
				return ((IScanModelWrapper<IDetectorModel>) element).getName();
			}
		});

		// Get detector wrappers from mapping bean and add as input to combo
		detectorCombo.addSelectionChangedListener(evt -> handleDetectorSelectionChange(evt.getSelection()));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorCombo.getControl());

		final Button configureDetectorButton = new Button(detectorComposite, SWT.PUSH);
		configureDetectorButton.setImage(Activator.getImage("icons/pencil.png"));
		configureDetectorButton.setToolTipText("Edit parameters");
		configureDetectorButton.addListener(SWT.Selection,
				event -> editDetectorParameters(getDetectorWrapperForSelection(detectorCombo.getSelection())));

		final Label exposureTimeLabel = new Label(parent, SWT.NONE);
		exposureTimeLabel.setText("Exposure Time:");

		final Text exposureTimeText = new Text(parent, SWT.BORDER);
		exposureTimeText.setToolTipText("Set the exposure time for this detector");
		GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);

		@SuppressWarnings("unchecked")
		final IObservableValue<String> exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
		detectorCombo.addSelectionChangedListener(event -> {
			if (exposureTimeBinding != null) {
				exposureTimeBinding.dispose();
				bindingContext.removeBinding(exposureTimeBinding);
			}

			final Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
			@SuppressWarnings("unchecked")
			final IDetectorModel model = ((IScanModelWrapper<IDetectorModel>) selected).getModel();

			@SuppressWarnings("unchecked")
			final IObservableValue<?> exposureTimeValue = PojoProperties.value("exposureTime").observe(model);
			exposureTimeBinding = bindingContext.bindValue(exposureTextValue, exposureTimeValue);
		});

		populateDetectorCombo(detectorCombo);
	}

	/**
	 * Handle a change in the combo box that selects the detector to use.
	 *
	 * @param selection
	 *            the new selection in the combo box
	 */
	private void handleDetectorSelectionChange(ISelection selection) {
		uiSync.asyncExec(() -> {
			final IDetectorModel selectedModel = getDetectorWrapperForSelection(selection).getModel();
			axesLabel.setText(getAxesMessage(selectedModel));
			focusScanBean.setDetector(selectedModel);
		});
	}

	private void populateDetectorCombo(ComboViewer comboViewer) {
		final List<IScanModelWrapper<IDetectorModel>> mappingDetectors =
				mappingBeanProvider.getMappingExperimentBean().getDetectorParameters();

		// Get the detectors that can be chosen for a focus scan
		// These are listed in the focus scan bean
		final List<String> focusScannableDevices = focusScanBean.getFocusScanDevices();
		final List<IScanModelWrapper<IDetectorModel>> availableFocusDetectors = mappingDetectors.stream()
				.filter(wrapper -> focusScannableDevices.contains(wrapper.getName()))
				.sorted(Comparator.comparing(IScanModelWrapper::getName))
				.collect(Collectors.toCollection(ArrayList::new));

		comboViewer.setInput(availableFocusDetectors);

		// The detector to be selected by default in the combo box
		// If unspecified or invalid, use the first one in the list
		Optional<IScanModelWrapper<IDetectorModel>> selectedDetector = Optional.empty();
		final String defaultFocusScanDevice = focusScanBean.getDefaultFocusScanDevice();
		if (defaultFocusScanDevice != null && !defaultFocusScanDevice.isEmpty()) {
			selectedDetector = availableFocusDetectors.stream()
				.filter(wrapper -> wrapper.getName().equals(defaultFocusScanDevice))
				.findFirst();
		}

		final IScanModelWrapper<IDetectorModel> selection = selectedDetector.isPresent() ? selectedDetector.get() : availableFocusDetectors.get(0);
		comboViewer.setSelection(new StructuredSelection(selection));
	}

	/**
	 * Return a message indicating whether the axes are moved by Malcolm, GDA or a combination
	 *
	 * @param model
	 *            the model for the detector to be used for the scan. If this is a Malcolm detector, the model will
	 *            indicate which axes it can move
	 */
	private String getAxesMessage(IDetectorModel model) {
		// Determine which axes (if any) Malcolm can move in this scan
		List<String> axesToMove = Collections.emptyList();
		if (model instanceof IMalcolmModel) {
			try {
				axesToMove = getMalcolmAxes((IMalcolmModel) model);
			} catch (ScanningException e) {
				final String message = "Could not get malcolm axes";
				logger.error(message, e);
				MessageDialog.openError(getShell(), "Focus Scan", "Could not get axes for malcolm device. See error log for more details");
				return message;
			}
		}
		final boolean malcolmMovesMappingAxes = !axesToMove.isEmpty();
		final boolean malcolmMovesFocusAxis = axesToMove.contains(focusScanBean.getFocusScannableName());

		if (malcolmMovesMappingAxes) {
			return malcolmMovesFocusAxis ? AXES_ALL_MALCOLM : AXES_MALCOLM_GDA;
		}
		return malcolmMovesFocusAxis ? AXES_GDA_MALCOLM : AXES_ALL_GDA;
	}

	public IRunnableDeviceService getRunnableDeviceService() {
		try {
			URI jmsURI = new URI(LocalProperties.getActiveMQBrokerURI());
			return eventService.createRemoteService(jmsURI, IRunnableDeviceService.class);
		} catch (Exception e) {
			logger.error("Could not get runnable device service", e);
			return null;
		}
	}

	private List<String> getMalcolmAxes(IMalcolmModel malcolmModel) throws ScanningException {
		final String deviceName = malcolmModel.getName();
		final IMalcolmDevice malcolmDevice = (IMalcolmDevice) (IRunnableDevice<?>) getRunnableDeviceService().getRunnableDevice(deviceName);
		return malcolmDevice.getAvailableAxes();
	}

	private void editDetectorParameters(IScanModelWrapper<IDetectorModel> detectorModelWrapper) {
		final EditDetectorModelDialog editDialog = new EditDetectorModelDialog(
				getShell(), getRunnableDeviceService(), detectorModelWrapper.getModel(), detectorModelWrapper.getName());
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
		@SuppressWarnings("unchecked")
		final IObservableValue<?> model = BeanProperties.value(propertyName).observe(bean);
		final IObservableValue<?> target;
		if (control instanceof NumberAndUnitsComposite) {
			target = new NumberUnitsWidgetProperty<Length, Control>().observe(control);
		} else if (control instanceof Text) {
			target = WidgetProperties.text(SWT.Modify).observe(control);
		} else {
			target = WidgetProperties.widgetSelection().observe(control);
		}
		bindingContext.bindValue(target, model);
	}

	/**
	 * Convert the x or y value of a focus line end point to a string for display
	 * <p>
	 * This uses the same formats that {@link NumberAndUnitsComposite} uses for the mapping scan setup
	 */
	private static class EndPointNumberConverter extends Converter<Object, String> {

		/** Use scientific format when the absolute number in the current units is <=1e-3 or >=1e3 */
		private final DecimalFormat scientificFormat = new DecimalFormat("0.#####E0");
		private final Converter<Object, String> scientificConverter = NumberToStringConverter.fromDouble(scientificFormat, false);

		/** Use decimal when the absolute number in the current units is 1e-3< number <1e3 */
		private final DecimalFormat decimalFormat = new DecimalFormat("0.#####");
		private final Converter<Object, String> decimalConverter = NumberToStringConverter.fromDouble(decimalFormat, false);

		public EndPointNumberConverter() {
			super(Double.class, String.class);
		}

		@Override
		public String convert(Object fromObject) {
			final double value = (double) fromObject;
			if (value != 0.0 && (value <= 1e-3 || value >= 1e3)) {
				return scientificConverter.convert(fromObject);
			} else {
				return decimalConverter.convert(fromObject);
			}
		}
	}

}
