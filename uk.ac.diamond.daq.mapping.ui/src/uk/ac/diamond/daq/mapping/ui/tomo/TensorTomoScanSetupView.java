/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static java.util.Objects.requireNonNullElse;
import static uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView.PATH_CALCULATION_TOPIC;
import static uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathMapper.mapRegionOntoModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.api.constants.RegionConstants;
import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.impl.ScanPathModelWrapper;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.AbstractSectionView;
import uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob;
import uk.ac.diamond.daq.mapping.ui.experiment.PlottingController;
import uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoPathInfo.StepSizes;

/**
 * An E4 view for setting up a I22 Tensor Tomo Scan, a specific kind of mapping scan
 * with a grid as the map path, and an outer scan that is also a 'grid' involving
 * two angles.
 * The purpose of this view is to present simplified and more specific UI for setting
 * up this kind of scan.
 */
public class TensorTomoScanSetupView extends AbstractSectionView<TensorTomoScanBean> {

	public static final String ID = "uk.ac.diamond.daq.mapping.ui.tomo.tensorTomoScanSetupView";

	private static final String STATE_KEY_TOMO_BEAN_JSON = TensorTomoScanBean.class.getSimpleName() + ".json";

	public static final String ANGLE_1_LABEL = "\u03c9"; // greek lower case letter omega
	public static final String ANGLE_2_LABEL = "\u03c6"; // greek lower case letter phi

	private static final Logger logger = LoggerFactory.getLogger(TensorTomoScanSetupView.class);

	protected static final double CALIBRATION_SCAN_STEP_SIZE = 10.0;

	@Inject
	private MappingStageInfo mappingStageInfo;

	// The plotting system for the 'map' plot
	@Inject
	private PlottingController plotter;

	@Inject
	private UISynchronize uiSync;

	@Inject
	private IEventBroker eventBroker;

	private TensorTomoScanBean tomoBean;

	@Inject
	private IScanBeanSubmitter submitter;

	private Composite mainComposite;

	private TensorTomoAnglePathInfoCalculator<?> pathInfoCalculator;
	private PathInfoCalculatorJob<TensorTomoPathRequest, TensorTomoPathInfo> pathInfoCalculationJob;
	private TensorTomoPathInfo pathInfo = null;

	private PropertyChangeListener mapRegionBeanPropertyChangeListener = this::mapRegionBeanPropertyChange;
	private PropertyChangeListener pathBeanPropertyChangeListener = event -> updatePoints();

	private boolean viewCreated = false;

	@Override
	@PostConstruct
	public void createView(Composite parent, MPart part) {
		initialize(part);

		mainComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(mainComposite);
		GridDataFactory.fillDefaults().applyTo(mainComposite);
		parent.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		parent.setBackgroundMode(SWT.INHERIT_FORCE);

		createSections();
		viewCreated = true;

		updatePlotRegion();
		updatePoints();
	}

	private void initialize(MPart part) {
		initializePathInfoCalculator();

		tomoBean = loadTomoBean(part);
		if (!checkTomoBean()) return;

		addTomoBeanListeners();
	}

	private void initializePathInfoCalculator() {
		pathInfoCalculator = new TensorTomoAnglePathInfoCalculator<>(getService(IPointGeneratorService.class));

		pathInfoCalculationJob = new PathInfoCalculatorJob<>(pathInfoCalculator, info -> eventBroker.post(PATH_CALCULATION_TOPIC, info));
		pathInfoCalculationJob.addJobChangeListener(new JobChangeAdapter() {

			@Override
			public void running(IJobChangeEvent event) {
				setStatusMessage("Scan path calculation in progress");
			}

			@Override
			public void done(IJobChangeEvent event) {
				final IStatus result = event.getResult();
				if (result.getSeverity() == IStatus.CANCEL) {
					setStatusMessage("Scan path calculation was cancelled");
				} else if (!result.isOK()) {
					setStatusMessage("Error in scan path calculation - see log for details");
					logger.warn("Error in scan path calculation", result.getException());
				}
			}
		});
	}

	@Override
	@PersistState
	public void saveState(MPart part) {
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		try {
			logger.trace("Saving the current state of the Tensor Tomography Scan Setup View");
			final String json = marshaller.marshal(getBean());
			part.getPersistedState().put(STATE_KEY_TOMO_BEAN_JSON, json);
		} catch (Exception e) {
			logger.error("Could not save the current state of the Tensor Tomography Scan Setup View", e);
		}
	}

	public boolean isViewCreated() {
		return viewCreated;
	}

	private void createSections() {
		final List<AbstractTomoViewSection> sectionsList = List.of(
				new MalcolmDeviceSection(),
				new MapRegionAndPathSection(),
				new TomoPathSection(),
				new ScanMetadataSection(),
				new StatusPanelSection(),
				new SubmitScanSection()
		);

		createSections(mainComposite, sectionsList, null);
	}

	private boolean checkTomoBean() {
		try {
			Objects.requireNonNull(tomoBean, "Could not find a bean of type: " + TensorTomoScanBean.class
					+ "\nThis should be defined in spring.");
			if (tomoBean.getGridRegionModel() == null) tomoBean.setGridRegionModel(new RectangularMappingRegion());
			if (tomoBean.getGridPathModel() == null) tomoBean.setGridPathModel(new TwoAxisGridPointsModel());

			// set the initial bounding boxes for the grid path models based on the region models
			tomoBean.getGridPathModel().setxAxisName(mappingStageInfo.getPlotXAxisName());
			tomoBean.getGridPathModel().setyAxisName(mappingStageInfo.getPlotYAxisName());

			mapRegionOntoModel(tomoBean.getGridRegionModel(), tomoBean.getGridPathModel());

			if (tomoBean.getAngle1Model() == null) {
				tomoBean.setAngle1Model(new ScanPathModelWrapper<>("stage_x", null, true));
			}
			if (tomoBean.getAngle1Model().getModel() == null) {
				tomoBean.getAngle1Model().setModel(new AxialStepModel(tomoBean.getAngle1Model().getName(), 0.0, 45.0, 5.0));
			}

			if (tomoBean.getAngle2Model() == null) {
				tomoBean.setAngle2Model(new ScanPathModelWrapper<>("stage_y", null, true));
			}
			if (tomoBean.getAngle2Model().getModel() == null) {
				tomoBean.getAngle2Model().setModel(new AxialPointsModel(tomoBean.getAngle2Model().getName(), 0.0, 360.0, 17));
			}
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			return false;
		}

		return true;
	}

	private TensorTomoScanBean loadTomoBean(MPart part) {
		 // load the previous tomo bean if possible
		final String json = part.getPersistedState().get(STATE_KEY_TOMO_BEAN_JSON);
		if (json != null) {
			// no previous bean, get the spring declared one from the eclipse context
			logger.trace("Restoring the previous state of the Tensor Tomo Scan Setup View");
			final IMarshallerService marshaller = getService(IMarshallerService.class);
			try {
				return marshaller.unmarshal(json, TensorTomoScanBean.class);
			} catch (Exception e) {
				logger.error("Failed to restore the state of the Tensor Temo Scan Setup View");
			}
		}

		return getService(TensorTomoScanBean.class);
	}

	private void mapRegionBeanPropertyChange(PropertyChangeEvent event) {
		updatePlotRegion();
		mapRegionOntoModel(tomoBean.getGridRegionModel(), tomoBean.getGridPathModel());
		if (event.getPropertyName().equals(RegionConstants.UPDATE_COMPLETE)) {
			updatePoints();
		}
	}

	private void updatePlotRegion() {
		plotter.updatePlotRegionFrom(tomoBean.getGridRegionModel());
	}

	protected void updatePoints() {
		// We only need the number of points, so no need for a PathInfoCalculatorJob
		// we can assume that the gridpath has been updated RegionAndPathMapper
		// TODO remove this method
		pathInfoCalculationJob.cancel();
		pathInfoCalculationJob.setPathInfoRequest(TensorTomoPathRequest.builder()
				.withMapPathModel(tomoBean.getGridPathModel())
				.withMapRegion(tomoBean.getGridRegionModel().toROI())
				.withAngle1PathModel(tomoBean.getAngle1Model().getModel())
				.withAngle2PathModel(tomoBean.getAngle2Model().getModel())
				.build());
		pathInfoCalculationJob.schedule();
	}

	protected void drawMappingRegion() {
		mapRegionOntoModel(tomoBean.getGridRegionModel(), tomoBean.getGridPathModel());
		plotter.createNewPlotRegion(tomoBean.getGridRegionModel());
	}

	@Inject
	@Optional
	private void setPathInfo(@UIEventTopic(PATH_CALCULATION_TOPIC) MappingPathInfo pathInfo) {
		if (ID.equals(pathInfo.getSourceId())) {

			this.pathInfo = (TensorTomoPathInfo) pathInfo;
			final StatusPanelSection statusPanel = getSection(StatusPanelSection.class);
			if (statusPanel != null) {
				statusPanel.setStatusMessage(null);
				uiSync.asyncExec(statusPanel::updateStatusLabel);
				uiSync.asyncExec(() -> getSection(TomoPathSection.class).updatePathInfo(this.pathInfo));
				uiSync.asyncExec(() -> plotter.plotPath(pathInfo));
			}
		}
	}

	public TensorTomoPathInfo getPathInfo() {
		return getPathInfo(false);
	}

	public TensorTomoPathInfo getPathInfo(boolean wait) {
		if (pathInfo == null && wait && pathInfoCalculationJob.getState() == Job.RUNNING) {
			try {
				pathInfoCalculationJob.join();
			} catch (InterruptedException e) {
				logger.error("Path info calculation job interrupted", e);
				// TODO set interrupt flag?
			}
		}
		return pathInfo;
	}


	@Override
	public void updateStatusLabel() {
		if (!isViewCreated()) return;
		getSection(StatusPanelSection.class).updateStatusLabel();
	}

	@Override
	public void setStatusMessage(String statusMessage) {
		if (!isViewCreated()) return;
		getSection(StatusPanelSection.class).setStatusMessage(statusMessage);
	}

	protected void submitTomoScans() {
		final TensorTomoPathInfo pathInfo = getPathInfo(true);
		if (pathInfo == null) {
			MessageDialog.openError(getShell(), "Error", "Could not calculate path information to create scan");
			return;
		}

		final List<ScanBean> scanBeans = createScanBeans(pathInfo);
		submitScans(scanBeans);
	}

	private void submitScans(final List<ScanBean> scanBeans) {
		try {
			for (ScanBean scanBean : scanBeans) {
				submitter.submitScan(scanBean);
			}
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(),
					"Error Submitting Scan",
					"The scan could not be submitted. See the error log for details");
		}
	}

	protected void submitCalibrationScan() {
		final String angle2ScannableName = getBean().getAngle2Model().getModel().getName();
		final IAxialModel angle2Model = new AxialStepModel(angle2ScannableName, 0, 180, CALIBRATION_SCAN_STEP_SIZE);
		final ScanBean scanBean = createScanBean(0, angle2Model);
		scanBean.setName("Tomo Calibration Scan");

		submitScans(List.of(scanBean));
	}

	private List<ScanBean> createScanBeans(TensorTomoPathInfo pathInfo) {
		final double[] angle1Positions = pathInfo.getAngle1Positions();
		final StepSizes angle2StepSizes = pathInfo.getAngle2StepSizes();

		return IntStream.range(0, angle1Positions.length)
				.mapToObj(i -> createScanBean(angle1Positions, angle2StepSizes, i))
				.toList();
	}

	private ScanBean createScanBean(double[] angle1Positions, StepSizes angle2StepSizes, int angle1Index) {
		final StepSizes angle2StepSize = angle2StepSizes.getStepSizeForIndex(angle1Index);
		final IAxialModel angle2ModelForInnerScan = getAngle2ModelForStepSize(angle2StepSize);
		return createScanBean(angle1Positions[angle1Index], angle2ModelForInnerScan);
	}

	private ScanBean createScanBean(double angle1Pos, IAxialModel angle2Model) {
		final ScanRequest scanRequest = new ScanRequest();
		// set angle1 position as the start position
		scanRequest.setStartPosition(new Scalar<>(getBean().getAngle1Model().getModel().getAxisName(), angle1Pos));
		// get the angle2 model for this angle 1 position
		final CompoundModel compoundModel = new CompoundModel(angle2Model, tomoBean.getGridPathModel());
		// TODO set units (get from scannable) (see ScanRequestConverter)?
		final ScanRegion scanRegion = new ScanRegion(tomoBean.getGridRegionModel().toROI(),
				tomoBean.getGridPathModel().getxAxisName(), tomoBean.getGridPathModel().getyAxisName());
		compoundModel.setRegions(List.of(scanRegion));
		scanRequest.setCompoundModel(compoundModel);

		// configure detectors
		final String malcolmDeviceName = tomoBean.getMalcolmDeviceName();
		final IMalcolmModel malcolmModel = tomoBean.getMalcolmModel();
		if (malcolmDeviceName != null && malcolmModel != null) {
			scanRequest.setDetectors(Map.of(malcolmDeviceName, malcolmModel));
		}

		// add sample metadata (name and background file)
		scanRequest.addScanMetadata(createSampleMetadata());

		// TODO add per-point and per-scan monitors (from mapping bean)?
		// TODO add configured processing (from mapping bean)?
		// TODO add template files (from mapping bean)?

		final ScanBean scanBean = new ScanBean();
		scanBean.setName("Tensor Tomo Scan, " + ANGLE_1_LABEL + " = " + angle1Pos);
		scanBean.setScanRequest(scanRequest);
		scanBean.setBeamline(System.getProperty("BEAMLINE"));
		return scanBean;
	}

	private IAxialModel getAngle2ModelForStepSize(StepSizes angle2StepSize) {
		return getAngle2ModelForStepSize(getBean().getAngle2Model().getModel(), angle2StepSize);
	}

	private IAxialModel getAngle2ModelForStepSize(IAxialModel angle2InitialModel, StepSizes angle2StepSize) {
		if (angle2InitialModel instanceof AxialArrayModel) {
			return angle2InitialModel; // same points for every angle1 position
		} else if (angle2InitialModel instanceof AxialPointsModel initialPointsModel) {
			return new AxialStepModel(initialPointsModel.getAxisName(), initialPointsModel.getStart(),
					initialPointsModel.getStop(), angle2StepSize.getStepSize());
		} else if (angle2InitialModel instanceof AxialStepModel initialStepModel) {
			return new AxialStepModel(angle2InitialModel.getAxisName(), initialStepModel.getStart(),
					initialStepModel.getStop(), initialStepModel.getStep());
		} else if (angle2InitialModel instanceof AxialMultiStepModel initialMultiStepModel) {
			final List<AxialStepModel> models = initialMultiStepModel.getModels();
			if (angle2StepSize.getLength() != models.size()) { // sanity check
				throw new IllegalArgumentException("angle2StepSize length must equal number of AxialStepModels");
			}

			final List<AxialStepModel> newModels = IntStream.range(0, angle2StepSize.getLength())
					.mapToObj(i -> (AxialStepModel) getAngle2ModelForStepSize(models.get(i), angle2StepSize.getStepSizeForIndex(i)))
					.toList();
			return new AxialMultiStepModel(angle2InitialModel.getAxisName(), newModels);
		} else {
			throw new IllegalArgumentException("Unexpected model class for second angle: " + angle2InitialModel.getClass());
		}
	}

	private ScanMetadata createSampleMetadata() {
		final ScanMetadata sampleMetadata = new ScanMetadata(MetadataType.SAMPLE);
		sampleMetadata.addField(NXsample.NX_NAME, requireNonNullElse(tomoBean.getSampleName(), "Unnamed sample"));
		sampleMetadata.addField("backgroundFile", requireNonNullElse(tomoBean.getBackgroundFilePath(), ""));

		return sampleMetadata;
	}

	private void addTomoBeanListeners() {
		tomoBean.getGridRegionModel().addPropertyChangeListener(mapRegionBeanPropertyChangeListener);
		tomoBean.getGridPathModel().addPropertyChangeListener(pathBeanPropertyChangeListener);
	}

	private void removeTomoBeanListeners() {
		tomoBean.getGridRegionModel().removePropertyChangeListener(mapRegionBeanPropertyChangeListener);
		tomoBean.getGridPathModel().removePropertyChangeListener(pathBeanPropertyChangeListener);
	}

	@Override
	public void relayout() {
		mainComposite.layout(true, true);
	}

	@Focus
	public void setFocus() {
//		mapRegionAndPathComposite.setFocus(); // TODO what to set as default focus control?
	}

	@Override
	@PreDestroy
	public void dispose() {
		removeTomoBeanListeners();
	}

	@Override
	public TensorTomoScanBean getBean() {
		return tomoBean;
	}

	public void setBean(TensorTomoScanBean tomoBean) {
		removeTomoBeanListeners();

		this.tomoBean = tomoBean;
		addTomoBeanListeners();
	}

	public void refreshView() {
		updateControls();
		updatePlotRegion();
		updatePoints();
	}

	public void redrawMapSection() {
		final MapRegionAndPathSection mapSection = getSection(MapRegionAndPathSection.class);
		if (mapSection != null) {
			mapSection.updateControls();
			relayout();
		}
	}

}
