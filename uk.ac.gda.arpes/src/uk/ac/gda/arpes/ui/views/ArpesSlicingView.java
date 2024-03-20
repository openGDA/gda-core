/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.arpes.ui.views;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.dawnsci.multidimensional.ui.arpes.ArpesSlicePlotViewer;
import org.dawnsci.multidimensional.ui.arpes.ArpesSliceTrace;
import org.dawnsci.multidimensional.ui.arpes.IArpesSliceTrace;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
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
import uk.ac.diamond.daq.devices.mbs.MbsAnalyserClientLiveDataDispatcher;
import uk.ac.diamond.daq.devices.mbs.MbsLiveDataUpdate;

public class ArpesSlicingView extends ViewPart implements IObserver{

	private static final Logger logger = LoggerFactory.getLogger(ArpesSlicingView.class);

	private Dataset volume;
	private DoubleDataset zAxis;
	private IArpesSliceTrace t;
	private SliceND slice;
	private AxesMetadata md;
	private int[] order = new int[] {0, 1, 2}; //ordering of the dimensions in the view

	private Display display;
	protected IPlottingSystem<Composite> plottingSystem;

	private MbsAnalyserClientLiveDataDispatcher dataDispatcher;

	private int numberOfPoints;
	private boolean makeNewVolume;
	private String currentScannableName;
	private AtomicInteger scansCounter;

	public static final String ID = "uk.ac.gda.arpes.ui.views.ArpesSlicingView";
	static final String DEFLECTOR = "deflector_x";
	static final String DETECTOR = "analyser";
	private static final String SWEPT_MODE="Swept";
	private String scanCommand;

	private boolean scanIsRunning = false;
	private boolean scanIsOneD = true;
	private boolean useAspectFromLabel = false;
	private Scannable currentScannable;
	private double currentScannableRefPosition;

	private static final String[] DEG_SCANNABLES = new String[] {DEFLECTOR,"sapolar","satilt","saazimuth"};

	@Override
	public void createPartControl(Composite parent) {
		display = PlatformUI.getWorkbench().getDisplay();
		parent.setLayout(new GridLayout());

		// Get scan events from GDA server and fetch number of scan points
		InterfaceProvider.getScanDataPointProvider().addScanEventObserver(serverObserver);

		// Get DataDispatcher from GDA client to act when new data comes
		dataDispatcher = Finder.findLocalSingleton(MbsAnalyserClientLiveDataDispatcher.class);
		dataDispatcher.addIObserver(this);

		try {
			plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.createPlotPart(parent, "ARPES Slicing View", null, PlotType.IMAGE, null);
			Composite pc = plottingSystem.getPlotComposite();
			pc.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

			/*
			 * Here org.dawnsci.multidimensional.ui.arpes.ArpesSlicePlotViewer viewer is chosen based on IArpesSliceTrace.class
			 * See org.dawnsci.plotting.system.PlottingSystemImpl.createTrace(String, Class<U>)
			 */
			t = plottingSystem.createTrace("ARPES Slicing View",IArpesSliceTrace.class);

			volume = DatasetFactory.zeros(new int[] {5,492,657});
			slice = new SliceND(volume.getShape());
			t.setData(volume, order, slice);
			plottingSystem.addTrace(t);
			setMainUseAspectFromLabel(isUseAspectFromLabel());

			scansCounter = new AtomicInteger(0);
			makeNewVolume = true;

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// Handle scan events
	private IObserver serverObserver = (source, arg) -> {
		if (!(arg instanceof ScanEvent scanEvent)) return;
		if (!(Arrays.asList(scanEvent.getLatestInformation().getDetectorNames()).contains(DETECTOR))) return;
		if (scanEvent.getLatestStatus().isComplete()) {
			makeNewVolume = true;
			scanIsRunning = false;
			return;
		} else {
			scanIsRunning = true;
		}

		if (scanEvent.getCurrentPointNumber()>-1) return;
		if (scanEvent.getLatestStatus()!=ScanStatus.NOTSTARTED) return;

		// Get total number of points for volume construction
		ScanInformation info = scanEvent.getLatestInformation();
		if (info.getDimensions().length>1) {
			logger.debug("Scan dimensions: {} more than 1 - slicing view is disabled!", Arrays.toString(info.getDimensions()));
			scanIsOneD = false;
			return;
		}

		numberOfPoints = info.getNumberOfPoints();
		scanCommand = info.getScanCommand(); // requires DAQ-4918 change merged

		//getScannableName from scan info - rely on assumption that it is always first element!
		currentScannableName = (numberOfPoints>1)? info.getScannableNames()[0]: DEFLECTOR;

		//find scannable before scan starts and get position - used in rscan command
		currentScannable = Finder.find(currentScannableName);
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
	};

	// Handle Data update events
	@Override
	public void update(Object source, Object arg) {
		if ((arg instanceof MbsLiveDataUpdate dataUpdate) && (scanIsRunning) && (scanIsOneD)) {
			logger.info("MBS data update received, accumulate? {}", dataUpdate.getAccumulate());
			if (makeNewVolume) initNewVolume(dataUpdate); else insertImageAndUpdate(dataUpdate);
		}
	}

	private void initNewVolume(MbsLiveDataUpdate dataUpdate) {
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
			UnitMetadata unitY = MetadataFactory.createMetadata(UnitMetadata.class, NonSI.DEGREE_ANGLE);
			UnitMetadata unitX = MetadataFactory.createMetadata(UnitMetadata.class, NonSI.ELECTRON_VOLT);

			zAxis = getScanAxis(scanCommand);
			zAxis.setName(currentScannableName);

			if (Arrays.stream(DEG_SCANNABLES).anyMatch(currentScannableName::equals)) {
				zAxis.setMetadata(unitY);
				useAspectFromLabel = true;
			} else {
				useAspectFromLabel = false;
			}

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

	    display.asyncExec(this::resetVolumeDisplay);
	}

	private void insertImageAndUpdate(MbsLiveDataUpdate dataUpdate) {
		if (Boolean.TRUE.equals(dataUpdate.getAccumulate())) {
			logger.info("Insert Image and DO NOT increase counter, slice {}", scansCounter.get());

			SliceND s1 = new SliceND(volume.getShape()); // for volume data
			s1.setSlice(0, scansCounter.get(), scansCounter.get()+1, 1);
			if (Objects.equals(dataUpdate.getAcquisitionMode(), SWEPT_MODE)) {
				volume.setSlice(dataUpdate.getData(), s1);
			} else {
				Dataset temp = volume.getSlice(s1);
				temp.iadd(dataUpdate.getData());
				volume.setSlice(temp, s1);
			}
		} else {
			scansCounter.getAndIncrement();
			logger.info("Insert Image and increase counter, slice {} ", scansCounter.get());

			SliceND s1 = new SliceND(volume.getShape()); // for volume data
			s1.setSlice(0, scansCounter.get(), scansCounter.get()+1, 1);
			volume.setSlice(dataUpdate.getData(), s1);
		}
		display.asyncExec(this::updateVolumeDisplay);
	}

	private void updateVolumeDisplay() {
		logger.info("Updating the ARPES slicing view!");
		t.setData(volume, order, slice);
	}

	private void resetVolumeDisplay() {
		logger.info("Resetting the ARPES slicing view!");
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

	@Override
	public void dispose() {
		plottingSystem.dispose();
		dataDispatcher.deleteIObserver(this);
		InterfaceProvider.getScanDataPointProvider().deleteScanEventObserver(serverObserver);
		super.dispose();
	}

	@Override
	public void setFocus() {
		// Noop
	}

	private boolean isUseAspectFromLabel() {
		return useAspectFromLabel;
	}
}
