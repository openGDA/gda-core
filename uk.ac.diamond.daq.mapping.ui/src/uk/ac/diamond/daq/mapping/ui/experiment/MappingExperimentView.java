package uk.ac.diamond.daq.mapping.ui.experiment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.richbeans.api.generator.IGuiGeneratorService;
import org.eclipse.scanning.api.points.models.ArrayModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegionShape;
import uk.ac.diamond.daq.mapping.api.IScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.api.MappingExperimentStatusBean;
import uk.ac.diamond.daq.mapping.impl.ExampleMappingExperimentBean;

/**
 * An E4-style POJO class for the mapping experiment view. This allows all dependencies to be injected (currently by a ViewPart instance until we have
 * annotation-based injection available). Ideally that would make this class unit-testable, but usage of the GuiGeneratorService is currently too extensive to
 * allow easy mocking, and the real service cannot be obtained without breaking encapsulation or running in an OSGi framework.
 */
public class MappingExperimentView {

	private class RegionSelectorListener implements ISelectionChangedListener {

		private final ComboViewer pathSelector;
		private final MappingRegionManager mappingRegionManager;
		private final PropertyChangeListener regionBeanPropertyChangeListener;

		private RegionSelectorListener(ComboViewer pathSelector, MappingRegionManager mappingRegionManager) {
			this.pathSelector = pathSelector;
			this.mappingRegionManager = mappingRegionManager;
			this.regionBeanPropertyChangeListener = new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					plotter.updatePlotRegionFrom(scanRegion);
					updatePoints();
				}
			};
		}

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			logger.debug("Region selection event: {}", event);

			// Get the new selection.
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
			IMappingScanRegionShape selectedRegion = (IMappingScanRegionShape) selection.getFirstElement();

			changeRegion(selectedRegion);
		}

		private void changeRegion(IMappingScanRegionShape newRegion) {
			// We're going to replace the scan region with a new one
			// If the existing one is non-null, remove the property change listener from it
			if (scanRegion != null) {
				scanRegion.removePropertyChangeListener(regionBeanPropertyChangeListener);
			}

			// Set the new scan region
			scanRegion = newRegion;
			experimentBean.getScanDefinition().getMappingScanRegion().setRegion(scanRegion);

			// Update the path selector with paths valid for the new region type
			// (The listener on the path selector will take care of propagating the change appropriately, and updating the GUI)
			// Do this before starting drawing the region (+ path ) with the plotting system because changing path after breaks the region drawing
			List<IScanPathModel> scanPathList = mappingRegionManager.getValidPaths(scanRegion);
			pathSelector.setInput(scanPathList);
			if (scanPathList.contains(scanPathModel)) {
				pathSelector.setSelection(new StructuredSelection(scanPathModel), true);
			} else if (scanPathList.size() > 0) {
				// Select the first path by default
				pathSelector.setSelection(new StructuredSelection(scanPathList.get(0)), true);
			} else {
				pathSelector.setSelection(StructuredSelection.EMPTY, true);
			}

			// If new scan region is non-null, add it to the plot and add the property change listener
			if (scanRegion != null) {
				plotter.createNewPlotRegion(scanRegion);
				scanRegion.addPropertyChangeListener(regionBeanPropertyChangeListener);
			}
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(MappingExperimentView.class);

	private final IMappingExperimentBean experimentBean;

	private Composite regionAndPathComposite;
	private Composite regionComposite;
	private Composite pathComposite;
	private StatusPanel statusPanel;

	private IMappingScanRegionShape scanRegion = null;
	private IScanPathModel scanPathModel = null;
	private PathInfoCalculatorJob pathCalculationJob;

	@Inject
	private PlottingController plotter;
	@Inject
	private IGuiGeneratorService guiGenerator;
	@Inject
	private IEclipseContext injectionContext;
	@Inject
	private UISynchronize uiSync;

	private final PropertyChangeListener pathBeanPropertyChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			updatePoints();
		}
	};

	@Focus
	public void setFocus() {
		if (regionAndPathComposite != null) {
			regionAndPathComposite.setFocus();
		}
	}

	public MappingExperimentView() {
		this.experimentBean = new ExampleMappingExperimentBean();
	}

	@PostConstruct
	public void createView(Composite parent) {

		pathCalculationJob = ContextInjectionFactory.make(PathInfoCalculatorJob.class, injectionContext);
		pathCalculationJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void running(IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					statusPanel.setMessage("Scan path calculation in progress");
					plotter.removePath();
				});
			}
			@Override
			public void done(final IJobChangeEvent event) {
				uiSync.asyncExec(() -> {
					IStatus result = event.getResult();
					if (result.getSeverity() == IStatus.CANCEL) {
						statusPanel.setMessage("Scan path calculation was cancelled");
					} else if (!result.isOK()) {
						statusPanel.setMessage("Error in scan path calculation - see log for details");
						logger.warn("Error in scan path calculation", result.getException());
					}
					// else, calculation completed normally and the status text will be updated from the new PathInfo
				});
			}
		});

		createViewControls(parent);
	}

	@PreDestroy
	public void dispose() {
		plotter.dispose();
	}

	private void createViewControls(Composite parent) {
		logger.trace("Starting to build the mapping experiment view");

		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(parent);

		// Make the status bar label
		statusPanel = new StatusPanel(parent, SWT.NONE, this);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(statusPanel);

		// Make a custom section for handling the mapping region
		regionAndPathComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(regionAndPathComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).spacing(0, 0).applyTo(regionAndPathComposite);

		// Prepare a grid data factory for controls which will need to grab space horizontally
		GridDataFactory horizontalGrabGridData = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// Make the region selection
		Composite regionComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(regionComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(regionComposite);
		Label regionLabel = new Label(regionComposite, SWT.NONE);
		regionLabel.setText("Region shape:");
		ComboViewer regionSelector = new ComboViewer(regionComposite);
		horizontalGrabGridData.applyTo(regionSelector.getControl());
		regionSelector.getCombo().setToolTipText("Select a scan region shape. The shape can then be drawn on the map, or you can type numbers below.");

		// Make the path selection
		Composite pathComposite = new Composite(regionAndPathComposite, SWT.NONE);
		horizontalGrabGridData.applyTo(pathComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(pathComposite);
		Label pathLabel = new Label(pathComposite, SWT.NONE);
		pathLabel.setText("Scan path:");
		final ComboViewer pathSelector = new ComboViewer(pathComposite);
		horizontalGrabGridData.applyTo(pathSelector.getControl());

		// Add logic
		regionSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IMappingScanRegionShape) {
					IMappingScanRegionShape mappingRegion = (IMappingScanRegionShape) element;
					return mappingRegion.getName();
				}
				return super.getText(element);
			}
		});

		MappingRegionManager mappingRegionManager = new MappingRegionManager();

		regionSelector.setContentProvider(ArrayContentProvider.getInstance());
		List<IMappingScanRegionShape> regionList = mappingRegionManager.getRegions();
		regionSelector.setInput(regionList.toArray());

		regionSelector.addSelectionChangedListener(new RegionSelectorListener(pathSelector, mappingRegionManager));

		pathSelector.setContentProvider(ArrayContentProvider.getInstance());
		pathSelector.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IScanPathModel) {
					IScanPathModel scanPath = (IScanPathModel) element;
					return scanPath.getName();
				}
				return super.getText(element);
			}
		});

		pathSelector.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				logger.debug("Path selection event: {}", event);

				// Get the new selection.
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				IScanPathModel selectedPath = (IScanPathModel) selection.getFirstElement();
				changePath(selectedPath);
			}
		});

		// Setting the first region will trigger the listeners to update the mapping section and point count
		regionSelector.setSelection(new StructuredSelection(regionList.get(0)), true);

		// Plot the scan region. This will cancel the region drawing event in the plotting system to avoid user confusion at startup
		// TODO check if this is the preferred behaviour, or better to leave the drawing event active
		plotter.updatePlotRegionFrom(scanRegion);

		final DataBindingContext dataBindingContext = new DataBindingContext();

		Composite essentialParametersComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(essentialParametersComposite);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(essentialParametersComposite);
		// FIXME not good to be hard-coding things here to look like the GUI generator - can we auto-generate these fields?
		Label sampleNameLabel = new Label(essentialParametersComposite, SWT.NONE);
		sampleNameLabel.setText("Sample Name:");
		Text sampleNameText = new Text(essentialParametersComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sampleNameText);
		IObservableValue sampleNameTextValue = WidgetProperties.text(SWT.Modify).observe(sampleNameText);
		IObservableValue sampleNameModelValue = PojoProperties.value("sampleName").observe(experimentBean.getSampleMetadata());
		dataBindingContext.bindValue(sampleNameTextValue, sampleNameModelValue);
		Button editMetadataButton = new Button(essentialParametersComposite, SWT.PUSH);
		editMetadataButton.setText("Edit metadata");
		editMetadataButton.addListener(SWT.Selection, event -> {
			showDialogToEdit(experimentBean.getSampleMetadata(), "Sample Metadata");
			// Ensure that any changes to metadata in the dialog are reflected in the main GUI
			dataBindingContext.updateTargets();
		});

		// TODO handle outer scannables properly
		Composite otherScanAxesComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(otherScanAxesComposite);
		final int axesColumns = 2;
		GridLayoutFactory.swtDefaults().numColumns(axesColumns).spacing(8, 5).applyTo(otherScanAxesComposite);
		GridDataFactory.fillDefaults().span(axesColumns, 1).grab(true, false)
				.applyTo(new Label(otherScanAxesComposite, SWT.SEPARATOR | SWT.HORIZONTAL));
		Label otherScanAxesLabel = new Label(otherScanAxesComposite, SWT.NONE);
		otherScanAxesLabel.setText("Other Scan Axes");
		GridDataFactory.fillDefaults().span(axesColumns, 1).applyTo(otherScanAxesLabel);
		for (IScanPathModelWrapper scannableAxisParameters : experimentBean.getScanDefinition().getOuterScannables()) {
			Button checkBox = new Button(otherScanAxesComposite, SWT.CHECK);
			checkBox.setText(scannableAxisParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(scannableAxisParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);

			// FIXME make a proper widget for this?
			Text axisText = new Text(otherScanAxesComposite, SWT.BORDER);
			axisText.setToolTipText("<start stop step> or <pos1,pos2,pos3,pos4...>");
			GridDataFactory.fillDefaults().grab(true, false).applyTo(axisText);
			IObservableValue axisTextValue = WidgetProperties.text(SWT.Modify).observe(axisText);
			IObservableValue axisValue = PojoProperties.value("model").observe(scannableAxisParameters);
			UpdateValueStrategy axisTextToModelStrategy = new UpdateValueStrategy();
			axisTextToModelStrategy.setConverter(new Converter(String.class, IScanPathModel.class) {
				@Override
				public Object convert(Object fromObject) {
					try {
						String text = (String) fromObject;
						String[] startStopStep = text.split(" ");
						if (startStopStep.length == 3) {
							StepModel stepModel = new StepModel();
							stepModel.setName(scannableAxisParameters.getName());
							stepModel.setStart(Double.parseDouble(startStopStep[0]));
							stepModel.setStop(Double.parseDouble(startStopStep[1]));
							stepModel.setStep(Double.parseDouble(startStopStep[2]));
							return stepModel;
						} else {
							String[] strings = text.split(",");
							double[] positions = new double[strings.length];
							for (int index = 0; index < strings.length; index++) {
								positions[index] = Double.parseDouble(strings[index]);
							}
							ArrayModel arrayModel = new ArrayModel();
							arrayModel.setName(scannableAxisParameters.getName());
							arrayModel.setPositions(positions);
							return arrayModel;
						}
					} catch (NumberFormatException nfe) {
						return null;
					}
				}
			});
			axisTextToModelStrategy.setBeforeSetValidator(value -> {
				if (value instanceof IScanPathModel) {
					return ValidationStatus.ok();
				}
				String message = "Text is incorrectly formatted";
				if (scannableAxisParameters.isIncludeInScan()) {
					return ValidationStatus.error(message);
				} else {
					return ValidationStatus.warning(message);
				}
			});
			Binding bindValue = dataBindingContext.bindValue(axisTextValue, axisValue, axisTextToModelStrategy, new UpdateValueStrategy());
			ControlDecorationSupport.create(bindValue, SWT.LEFT | SWT.TOP);
		}

		Composite detectorsComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(detectorsComposite);
		final int detectorsColumns = 3;
		GridLayoutFactory.swtDefaults().numColumns(detectorsColumns).applyTo(detectorsComposite);
		GridDataFactory.fillDefaults().span(detectorsColumns, 1).grab(true, false)
				.applyTo(new Label(detectorsComposite, SWT.SEPARATOR | SWT.HORIZONTAL));
		Label detectorsLabel = new Label(detectorsComposite, SWT.NONE);
		detectorsLabel.setText("Detectors");
		GridDataFactory.fillDefaults().span(detectorsColumns, 1).applyTo(detectorsLabel);
		for (IDetectorModelWrapper detectorParameters : experimentBean.getDetectorParameters()) {
			Button checkBox = new Button(detectorsComposite, SWT.CHECK);
			checkBox.setText(detectorParameters.getName());
			IObservableValue checkBoxValue = WidgetProperties.selection().observe(checkBox);
			IObservableValue activeValue = PojoProperties.value("includeInScan").observe(detectorParameters);
			dataBindingContext.bindValue(checkBoxValue, activeValue);
			checkBox.addListener(SWT.Selection, event -> {
				statusPanel.updateStatusLabel();
			});
			Text exposureTimeText = new Text(detectorsComposite, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(exposureTimeText);
			IObservableValue exposureTextValue = WidgetProperties.text(SWT.Modify).observe(exposureTimeText);
			IObservableValue exposureTimeValue = PojoProperties.value("exposureTime").observe(detectorParameters.getModel());
			dataBindingContext.bindValue(exposureTextValue, exposureTimeValue);
			exposureTimeText.addListener(SWT.Modify, event -> {
				statusPanel.updateStatusLabel();
			});
			Button configButton = new Button(detectorsComposite, SWT.PUSH);
			configButton.setText("Edit parameters");
			configButton.addListener(SWT.Selection, event -> {
				showDialogToEdit(detectorParameters.getModel(), detectorParameters.getName() + " Parameters");
				dataBindingContext.updateTargets();
			});
		}

		Composite validateScanSomposite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BOTTOM).applyTo(validateScanSomposite);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(validateScanSomposite);

		Button scanButton = new Button(validateScanSomposite, SWT.NONE);
		scanButton.setText("Scan!");
		scanButton.addSelectionListener(new SelectionAdapter() {
			MappingScanSubmitter submitter;
			@Override
			public void widgetSelected(SelectionEvent event) {
				if (submitter == null) {
					submitter = new MappingScanSubmitter();
					submitter.init();
				}
				try {
					MappingExperimentStatusBean experimentStatusBean = new MappingExperimentStatusBean();
					experimentStatusBean.setMappingExperimentBean(experimentBean);
					System.out.println(experimentStatusBean);
					submitter.submitScan(experimentStatusBean);
				} catch (Exception e) {
					logger.warn("Scan submission failed", e);
				}
			}
		});

		logger.trace("Finished building the mapping experiment view");
	}

	private void showDialogToEdit(Object bean, String title) {
		guiGenerator.openDialog(bean, (Shell) injectionContext.get(IServiceConstants.ACTIVE_SHELL), title);
	}

	private void changePath(IScanPathModel newPath) {
		logger.debug("Changing path to {}", newPath);

		// We're going to replace the scan path with a new one
		// If the existing one is non-null, remove the property change listener from it
		if (scanPathModel != null) {
			scanPathModel.removePropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Set the new scan path. If non-null, add the property change listener
		scanPathModel = newPath;
		experimentBean.getScanDefinition().getMappingScanRegion().setScanPath(scanPathModel);
		if (scanPathModel != null) {
			scanPathModel.addPropertyChangeListener(pathBeanPropertyChangeListener);
		}

		// Update the GUI to reflect the path changes
		rebuildMappingSection();
		updatePoints();
	}

	/**
	 * Call this to rebuild the mapping section. Only required when the underlying beans are swapped.
	 */
	private void rebuildMappingSection() {
		// Remove the old controls
		if (regionComposite != null) {
			regionComposite.dispose();
			regionComposite = null;
		}
		if (pathComposite != null) {
			pathComposite.dispose();
			pathComposite = null;
		}
		// Scan Region
		Object mappingScanRegion = experimentBean.getScanDefinition().getMappingScanRegion().getRegion();
		regionComposite = (Composite) guiGenerator.generateGui(mappingScanRegion, regionAndPathComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(regionComposite);
		// Scan Path
		Object scanPath = experimentBean.getScanDefinition().getMappingScanRegion().getScanPath();
		pathComposite = (Composite) guiGenerator.generateGui(scanPath, regionAndPathComposite);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(pathComposite);
		regionAndPathComposite.layout(true, true);
	}

	private void updatePoints() {
		pathCalculationJob.cancel();
		if (scanPathModel != null && scanRegion != null) {
			pathCalculationJob.setScanPathModel(scanPathModel);
			pathCalculationJob.setScanRegion(scanRegion);
			pathCalculationJob.schedule();
		}
	}

	@Inject
	@Optional
	private void updateUiWithPathInfo(@UIEventTopic(PathInfoCalculatorJob.PATH_CALCULATION_TOPIC) PathInfo pathInfo) {
		statusPanel.setPathInfo(pathInfo);
		plotter.plotPath(pathInfo);
	}

	double getPointExposureTime() {
		double exposure = 0.0;
		for (IDetectorModelWrapper detectorParameters : experimentBean.getDetectorParameters()) {
			if (detectorParameters.isIncludeInScan()) {
				exposure = Math.max(exposure, detectorParameters.getModel().getExposureTime());
			}
		}
		return exposure;
	}
}
