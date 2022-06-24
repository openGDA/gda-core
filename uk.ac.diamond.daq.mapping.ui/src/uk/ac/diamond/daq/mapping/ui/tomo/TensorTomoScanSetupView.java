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

import static uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView.PATH_CALCULATION_TOPIC;
import static uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathMapper.mapRegionOntoModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
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

import uk.ac.diamond.daq.mapping.api.IPathInfoCalculator;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.TensorTomoScanBean;
import uk.ac.diamond.daq.mapping.api.constants.RegionConstants;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfoRequest;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;
import uk.ac.diamond.daq.mapping.ui.AbstractSectionView;
import uk.ac.diamond.daq.mapping.ui.experiment.PathInfoCalculatorJob;
import uk.ac.diamond.daq.mapping.ui.experiment.PlottingController;
import uk.ac.diamond.daq.mapping.ui.path.PointGeneratorPathInfoCalculator;

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

	private static final Logger logger = LoggerFactory.getLogger(TensorTomoScanSetupView.class);

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

	private final IPathInfoCalculator<PathInfoRequest> pathInfoCalculator;
	private final PathInfoCalculatorJob pathInfoCalculationJob;

	private PropertyChangeListener mapRegionBeanPropertyChangeListener = this::mapRegionBeanPropertyChange;
	private PropertyChangeListener pathBeanPropertyChangeListener = event -> updatePoints();

	private boolean viewCreated = false;

	public TensorTomoScanSetupView() {
		pathInfoCalculator = new PointGeneratorPathInfoCalculator();
		pathInfoCalculationJob = new PathInfoCalculatorJob(pathInfoCalculator,
				pathInfo -> eventBroker.post(PATH_CALCULATION_TOPIC, pathInfo));
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
	@PostConstruct
	public void createView(Composite parent, MPart part) {
		tomoBean = loadTomoBean(part);
		if (!checkTomoBean()) return;

		addTomoBeanListeners();

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
			Objects.requireNonNull(tomoBean.getAngle1Model(), "angle 1 model not set");
			Objects.requireNonNull(tomoBean.getAngle1Model().getName(), "angle 1 name not set");
			Objects.requireNonNull(tomoBean.getAngle2Model(), "angle 2 model not set");
			Objects.requireNonNull(tomoBean.getAngle2Model().getName(), "angle 2 name not set");

			initializeTomoBean();
		} catch (Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			return false;
		}

		return true;
	}

	private void initializeTomoBean() {
		if (tomoBean == null) return;
		if (tomoBean.getGridRegionModel() == null) tomoBean.setGridRegionModel(new RectangularMappingRegion());
		if (tomoBean.getGridPathModel() == null) tomoBean.setGridPathModel(new TwoAxisGridPointsModel());

		// set the initial bounding boxes for the grid path models based on the region models
		tomoBean.getGridPathModel().setxAxisName(mappingStageInfo.getPlotXAxisName());
		tomoBean.getGridPathModel().setyAxisName(mappingStageInfo.getPlotYAxisName());

		mapRegionOntoModel(tomoBean.getGridRegionModel(), tomoBean.getGridPathModel());

		final IScanModelWrapper<IAxialModel> angle1 = tomoBean.getAngle1Model();
		if (angle1.getModel() == null) angle1.setModel(new AxialStepModel(angle1.getName(), 0.0, 180.0, 10.0)); // TODO check defaults
		final IScanModelWrapper<IAxialModel> angle2 = tomoBean.getAngle2Model();
		if (angle2.getModel() == null) angle2.setModel(new AxialStepModel(angle2.getName(), 0.0, 90.0, 10.0));
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
		if (event.getPropertyName().equals(RegionConstants.CALC_POINTS)) {
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
		pathInfoCalculationJob.setPathInfoRequest(PathInfoRequest.builder()
				.withEventId(UUID.randomUUID())
				.withSourceId(ID)
				.withScanPathModel(tomoBean.getGridPathModel())
				.withScanRegion(tomoBean.getGridRegionModel().toROI())
				.withOuterScannables(List.of(tomoBean.getAngle1Model().getModel(), tomoBean.getAngle2Model().getModel()))
				.build());
		pathInfoCalculationJob.schedule();
	}

	protected void drawMappingRegion() {
		mapRegionOntoModel(tomoBean.getGridRegionModel(), tomoBean.getGridPathModel());
		plotter.createNewPlotRegion(tomoBean.getGridRegionModel());
	}

	@Inject
	@Optional
	private void setPathInfo(@UIEventTopic(PATH_CALCULATION_TOPIC) PathInfo pathInfo) {
		if (ID.equals(pathInfo.getSourceId())) {
			final StatusPanelSection statusPanel = getSection(StatusPanelSection.class);
			if (statusPanel != null) {
				statusPanel.setPathInfo(pathInfo);
				uiSync.asyncExec(() -> plotter.plotPath(pathInfo));
			}
		}
	}

	@Override
	public void updateStatusLabel() {
		getSection(StatusPanelSection.class).updateStatusLabel();
	}

	@Override
	public void setStatusMessage(String statusMessage) {
		if (!isViewCreated()) return;
		getSection(StatusPanelSection.class).setStatusMessage(statusMessage);
	}

	protected void submitScan() {
		try {
			final ScanBean scanBean = createScanBean();
			submitter.submitScan(scanBean);
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(),
					"Error Submitting Scan",
					"The scan could not be submitted. See the error log for details");
		}
	}

	protected ScanBean createScanBean() {
		// TODO use a converter class, similar to ScanRequestConverter?
		final ScanRequest scanRequest = new ScanRequest();
		final CompoundModel compoundModel = new CompoundModel(
				tomoBean.getAngle1Model().getModel(),
				tomoBean.getAngle2Model().getModel(),
				tomoBean.getGridPathModel());
		// TODO set units (get from scannable) (see ScanRequestConverter)?
		compoundModel.setRegions(List.of(new ScanRegion(tomoBean.getGridRegionModel().toROI())));
		scanRequest.setCompoundModel(compoundModel);

		// configure detectors
		final String malcolmDeviceName = tomoBean.getMalcolmDeviceName();
		final IMalcolmModel malcolmModel = tomoBean.getMalcolmModel();
		if (malcolmDeviceName != null && malcolmModel != null) {
			scanRequest.setDetectors(Map.of(malcolmDeviceName, malcolmModel));
		}

		// add sample name
		final ScanMetadata sampleMetadata = new ScanMetadata(MetadataType.SAMPLE);
		sampleMetadata.addField(NXsample.NX_NAME, tomoBean.getSampleName());
		// TODO - where should the background file be written to in the nexus file? Existing NcdStatus doesn't seem to do anything with it.
		sampleMetadata.addField("backgroundFile", tomoBean.getBackgroundFilePath());
		scanRequest.setScanMetadata(Arrays.asList(sampleMetadata));

		// TODO add per-point and per-scan monitors (from mapping bean)?
		// TODO add configured processing (from mapping bean)?
		// TODO add template files (from mapping bean)?

		final ScanBean scanBean = new ScanBean();
		scanBean.setScanRequest(scanRequest);
		scanBean.setBeamline(System.getProperty("BEAMLINE"));
		return scanBean;
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
		getSection(MapRegionAndPathSection.class).updateControls();
		relayout();
	}

}
