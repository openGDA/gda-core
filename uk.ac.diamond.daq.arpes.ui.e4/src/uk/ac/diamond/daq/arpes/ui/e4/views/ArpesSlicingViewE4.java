/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.arpes.ui.e4.views;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.dawnsci.multidimensional.ui.arpes.ArpesSlicePlotViewer;
import org.dawnsci.multidimensional.ui.arpes.ArpesSliceTrace;
import org.dawnsci.multidimensional.ui.arpes.IArpesSliceTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.january.MetadataException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.january.metadata.AxesMetadata;
import org.eclipse.january.metadata.MetadataFactory;
import org.eclipse.january.metadata.UnitMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;
import gda.scan.ScanEvent;
import gda.scan.ScanInformation;
import si.uom.NonSI;
import uk.ac.diamond.daq.arpes.ui.e4.constants.ArpesUiConstants;
import uk.ac.diamond.daq.pes.api.LiveDataPlotUpdate;
import uk.ac.gda.apres.ui.config.ArpesSlicingViewConfiguration;

public class ArpesSlicingViewE4 {
	private static final Logger logger = LoggerFactory.getLogger(ArpesSlicingViewE4.class);
	private IEclipseContext context;

	protected IPlottingSystem<Composite> plottingSystem;

	private String eventTopic;
	private String[] degScannables;
	private String analyserName;
	private String defaultScannable;

	private String scanCommand;
	private String currentScannableName;

	private boolean makeNewVolume;
	private boolean scanIsRunning = false;
	private boolean scanIsOneD = true;
	private boolean useAspectFromLabel = false;

	private AtomicInteger scansCounter;

	private int numberOfPoints;
	private int[] order;

	private double currentScannableRefPosition;

	private Dataset volume;
	private DoubleDataset zAxis;
	private IArpesSliceTrace t;
	private SliceND slice;
	private AxesMetadata md;

	UnitMetadata unitY;
	UnitMetadata unitX;


	@Inject
	IEventBroker broker;

	@Inject
	private UISynchronize uiSync;

	@Inject
	public ArpesSlicingViewE4 (IEclipseContext context, @Named("eventTopic") @Active @Optional String eventTopic) {
		this.context = context;
		this.eventTopic = ArpesUiConstants.getConstantValue(eventTopic);
	}

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout());

		// Get scan events from GDA server and fetch number of scan points
		InterfaceProvider.getScanDataPointProvider().addScanEventObserver(serverObserver);

		ArpesSlicingViewConfiguration viewConfig = Finder.findOptionalLocalSingleton(ArpesSlicingViewConfiguration.class).orElseThrow();
		degScannables 		= viewConfig.getDegreeScannableNames();
		analyserName 		= viewConfig.getAnalyserName();
		defaultScannable	= viewConfig.getDefaultScannableName();
		order 				= viewConfig.getOrder();

		subscribeToEventBroker();

		try {
			plottingSystem = context.get(IPlottingService.class).createPlottingSystem();
			plottingSystem.createPlotPart(parent, "ARPES Slicing View", null, PlotType.IMAGE, null);
			Composite pc = plottingSystem.getPlotComposite();
			pc.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

			/*
			 * Here org.dawnsci.multidimensional.ui.arpes.ArpesSlicePlotViewer viewer is chosen based on IArpesSliceTrace.class
			 * See org.dawnsci.plotting.system.PlottingSystemImpl.createTrace(String, Class<U>)
			 */
			t = plottingSystem.createTrace("ARPES Slicing View",IArpesSliceTrace.class);

			volume = DatasetFactory.zeros(viewConfig.getInitialImageDims());
			slice = new SliceND(volume.getShape());
			t.setData(volume, order, slice);
			plottingSystem.addTrace(t);

			setMainUseAspectFromLabel(isUseAspectFromLabel());

			scansCounter = new AtomicInteger(0);
			makeNewVolume = true;

			unitY = MetadataFactory.createMetadata(UnitMetadata.class, NonSI.DEGREE_ANGLE);
			unitX = MetadataFactory.createMetadata(UnitMetadata.class, NonSI.ELECTRON_VOLT);

			plottingSystem.setKeepAspect(false);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void subscribeToEventBroker() {
		broker.subscribe(eventTopic, updatePlot);
	}

	private EventHandler updatePlot = event -> {
		if ((event.getProperty(IEventBroker.DATA) instanceof LiveDataPlotUpdate dataUpdate) && (scanIsRunning) && (scanIsOneD)) {
			logger.debug("Plot data update received, accumulate? {}", dataUpdate.isUpdateSameFrame());
			if (makeNewVolume) initNewVolume(dataUpdate); else insertImageAndUpdate(dataUpdate);
		}
	};

	private void initNewVolume(LiveDataPlotUpdate dataUpdate) {
		makeNewVolume = false;
		scansCounter.set(0);

		//make initial dataset [scan_points, analyser_y, analyser_x]
		logger.info("MakeInitialVolume with total number of points {}", numberOfPoints);
		volume = DatasetFactory.zeros(numberOfPoints, dataUpdate.getData().getShape()[0], dataUpdate.getData().getShape()[1]);


		//a full slice describing the volume
		//The viewer can show subslices but we want the full dataset here
		slice = new SliceND(volume.getShape());

		//Put axes on the volume
		//Whatever than scan axis set values are for 0
		//analyser angles/energies for 1,2
		try {
			md = MetadataFactory.createMetadata(AxesMetadata.class, 3);

			zAxis = getScanAxis(scanCommand);
			zAxis.setName(currentScannableName);

			setUseAspectFromLabel(unitY);

			IDataset yAxis = dataUpdate.getyAxis();
			yAxis.setMetadata(unitY);

			IDataset xAxis = dataUpdate.getxAxis();
			xAxis.setMetadata(unitX);

			md.addAxis(0, zAxis);
			md.addAxis(1, yAxis);
			md.addAxis(2, xAxis);

			volume.addMetadata(md);

		} catch (MetadataException e) {
			logger.error("Metadata setup failed", e);
		}

		SliceND s1 = new SliceND(volume.getShape()); // for volume data
		s1.setSlice(0, 0, 1, 1);
		volume.setSlice(dataUpdate.getData(), s1);

		uiSync.asyncExec(this::resetVolumeDisplay);
	}

	private void setUseAspectFromLabel(UnitMetadata unitY) {
		if (Arrays.stream(degScannables).anyMatch(currentScannableName::equals)) {
			zAxis.setMetadata(unitY);
			useAspectFromLabel = true;
		} else {
			useAspectFromLabel = false;
		}
	}
	private void insertImageAndUpdate(LiveDataPlotUpdate dataUpdate) {
		if (Boolean.TRUE.equals(dataUpdate.isUpdateSameFrame())) {
			logger.info("Insert Image and DO NOT increase counter, slice {}", scansCounter.get());

			SliceND s1 = new SliceND(volume.getShape()); // for volume data
			s1.setSlice(0, scansCounter.get(), scansCounter.get()+1, 1);
			volume.setSlice(dataUpdate.getData(), s1);
		} else {
			scansCounter.getAndIncrement();
			logger.info("Insert Image and increase counter, slice {} ", scansCounter.get());

			SliceND s1 = new SliceND(volume.getShape()); // for volume data
			s1.setSlice(0, scansCounter.get(), scansCounter.get()+1, 1);
			volume.setSlice(dataUpdate.getData(), s1);
		}
		uiSync.asyncExec(this::updateVolumeDisplay);
	}

	private void updateVolumeDisplay() {
		logger.info("Updating ARPES slicing view!");
		t.setData(volume, order, slice);
	}

	private void resetVolumeDisplay() {
		logger.info("Resetting ARPES slicing view!");
		plottingSystem.removeTrace(t);
		t = new ArpesSliceTrace();
		t.setData(volume, order, slice);
		plottingSystem.addTrace(t);
		setMainUseAspectFromLabel(isUseAspectFromLabel());
	}

	private void setMainUseAspectFromLabel(boolean useAspectFromLabel) {
		if (plottingSystem.getActiveViewer() instanceof ArpesSlicePlotViewer activeViewer ) {
			activeViewer.setMainSystemUseAspectFromLabel(useAspectFromLabel);
		}
	}

	private DoubleDataset getScanAxis(String scanCommand) {
		DoubleDataset scanAxis;
		double tweak = 0.0001;

		try {
			String[] scanCommandArray = scanCommand.split(" ");
			scanAxis = DatasetFactory.createRange(
					currentScannableRefPosition+Double.parseDouble(scanCommandArray[2]),
					currentScannableRefPosition+Double.parseDouble(scanCommandArray[3])+tweak,
					Double.parseDouble(scanCommandArray[4]));

		} catch (Exception e) {
			scanAxis = DatasetFactory.createRange(volume.getShapeRef()[0]);
			logger.error("Failed to parse scan axis from command - setting integer range", e);
		}
		return scanAxis;
	}

	// Handle scan events
		private IObserver serverObserver = (source, arg) -> {
			if (!(arg instanceof ScanEvent scanEvent)) return;
			if (!(Arrays.asList(scanEvent.getLatestInformation().getDetectorNames()).contains(analyserName))) return;
			if (scanEvent.getLatestStatus().isComplete()) {
				// let the last frame be consumed
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.error("Failed to thread.sleep()", e);
				}
				makeNewVolume = true;
				scanIsRunning= false;
				return;
			} else {
				scanIsRunning = true;
			}

			if (scanEvent.getCurrentPointNumber()>-1) return;
			if (scanEvent.getLatestStatus()!=ScanStatus.NOTSTARTED) return;

			configureSlicingViewAtScanStart(scanEvent.getLatestInformation());
		};

		private void configureSlicingViewAtScanStart(ScanInformation info) {
			// Get total number of points for volume construction
			if (info.getDimensions().length>1) {
				logger.debug("Scan dimensions: {} more than 1 - slicing view is disabled!", Arrays.toString(info.getDimensions()));
				scanIsOneD = false;
				return;
			} else {
				scanIsOneD = true;
			}

			numberOfPoints = info.getNumberOfPoints();
			scanCommand = info.getScanCommand(); // requires DAQ-4918 change merged

			//getScannableName from scan info - rely on assumption that it is always first element!
			currentScannableName = (numberOfPoints>1)? info.getScannableNames()[0]: defaultScannable;

			//find scannable before scan starts and get position - used in rscan command
			Scannable currentScannable = Finder.find(currentScannableName);
			logger.debug("Got scan command {}",scanCommand);
			if (scanCommand.contains("rscan")) {
				try {
					if (currentScannable.getPosition().getClass().isArray()) {
						currentScannableRefPosition = (double) Array.get(currentScannable.getPosition(),0);
					} else {
						currentScannableRefPosition = (double) currentScannable.getPosition();
					}
				} catch (DeviceException e) {
					logger.error("Failed to get scannable position for rscan", e);
				}
			} else {
				currentScannableRefPosition = 0;
			}
		}

		private boolean isUseAspectFromLabel() {
			return useAspectFromLabel;
		}

		@Focus
		public void setFocus() {
			if (plottingSystem!=null) plottingSystem.setFocus();
		}

		@PreDestroy
		public void dispose() {
			if (plottingSystem!=null) plottingSystem.dispose();
		}
}
