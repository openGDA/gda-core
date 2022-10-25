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

package uk.ac.gda.server.ncd.calibration;

import static java.util.Collections.emptyList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.DoubleFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.IMetadataEntry;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.jython.JythonServerFacade;
import gda.jython.PanicStopEvent;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import gda.scan.ConcurrentScan;
import gda.scan.ScanPositionProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.concurrent.Async.ListeningFuture;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(BraggCalibrationService.class)
public class CalibrationScanRunner implements BraggCalibrationService, IObserver {

	private static final Logger logger = LoggerFactory.getLogger(CalibrationScanRunner.class);

	/** The scannable to scan over when running calibration scans */
	private Scannable energy;

	/** The motor that is being calibrated - the offset will be adjusted with the new calibration */
	private ScannableMotor bragg;

	/** Scannables to be included in energy scans */
	private Collection<Scannable> additionalScannables = emptyList();

	/** The path in the collected scan files where the bragg position data is written */
	private String braggPath;
	/** The path in the collected scan files where the exafs data is written */
	private String exafsPath;

	private String name;

	/** The maximum expected error in the current configuration - any value greater than this cannot be set automatically */
	private double interceptLimit = 0.1;
	/** Map of edges to the positions required for the {@link #edgePositioner} to move the foil into the beam */
	private Map<CalibrationEdge, Double> edgePositions;
	/** The motor used to move the required edge into the beam */
	private Scannable edgePositioner;
	/** Parameters used to generate energy scans */
	private DoubleFunction<ScanPositionProvider> scanPositions = new CentroidScanParameters(0.06, 0.12, 0.0005);

	private ObservableComponent obs = new ObservableComponent();
	/** Main Jython server used to monitor the progress of scans */
	private JythonServerFacade server;
	/** The currently running scan */
	private ListeningFuture<Boolean> runningScans;

	/** Flag to mark when a scan (or series of scans) is running */
	private AtomicBoolean busy = new AtomicBoolean(false);

	/** Optimiser to maximise intensity for a given energy */
	private GaussianOptimisingScan pitchOptimiser;

	private IMetadataEntry scanTitle;
	private IMetadataEntry sampleName;

	@Override
	public void runEdgeScans(Collection<CalibrationEdge> edges) {
		runningScans = Async.submit(() -> scanTask(edges))
				.onSuccess(v -> {
					logger.info("Calibration scans complete");
					obs.notifyIObservers(this, CalibrationUpdate.Update.FINISHED);
				})
				.onFailure(e -> {
					logger.error("Calibration scans failed", e);
					obs.notifyIObservers(this, CalibrationUpdate.Update.FAILED);
				})
				.onComplete(() -> busy.set(false));
	}

	private boolean scanTask(Collection<CalibrationEdge> edges) throws Exception {
		if (!busy.compareAndSet(false, true)) {
			throw new IllegalStateException("Calibration scan already in progress");
		}
		obs.notifyIObservers(this, CalibrationUpdate.Update.STARTED);
		for (var edge: edges) {
			logger.info("Starting edge scan for {}", edge.getName());
			var ref = edge.getUid();
			obs.notifyIObservers(this, new ScanProgress(CalibrationUpdate.Update.STARTED, ref, null));

			// Move edge into position
			double edgePosition = edgePositions.get(edge);
			logger.debug("Moving {} to {} before edge scan for {}", edgePositioner.getName(), edgePosition, edge.getName());
			edgePositioner.moveTo(edgePosition);

			// Set energy
			energy.moveTo(edge.getEdgeEnergy());

			// Optimise pitch for energy and edge
			pitchOptimiser.optimise();

			// Run scan
			sampleName.setValue(edge.getName());
			scanTitle.setValue("Bragg calibration - Edge: " + edge.getName());
			var parameters = new ArrayList<>();
			parameters.add(energy);
			parameters.add(scanPositions.apply(edge.getEdgeEnergy()));
			parameters.add(bragg);
			parameters.addAll(additionalScannables);
			logger.debug("Running {} edge scan with parameters: {}", edge.getName(), parameters);
			var scan = new ConcurrentScan(parameters.toArray());
			scan.runScan();

			// Update client that scan is complete
			obs.notifyIObservers(this, new ScanProgress(CalibrationUpdate.Update.FINISHED, ref, scan.getScanInformation().getFilename()));
		}
		// unused return value but needed to make this Callable instead of Runnable so it can throw exceptions
		return true;
	}

	@Override
	public String toString() {
		return "CalibrationScanRunner(bragg=" + energy.getName() + ")";
	}

	public Scannable getEnergy() {
		return energy;
	}

	public void setEnergy(Scannable energy) {
		this.energy = energy;
	}

	public ScannableMotor getBragg() {
		return bragg;
	}

	public void setBragg(ScannableMotor bragg) {
		this.bragg = bragg;
	}

	@Override
	public Collection<CalibrationEdge> getEdges() {
		return new ArrayList<>(edgePositions.keySet());
	}

	public void setEdgePositions(Map<CalibrationEdge, Double> edges) {
		this.edgePositions = edges;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		obs.addIObserver(anIObserver);
	}

	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obs.deleteIObserver(anIObserver);
	}

	@Override
	public void deleteIObservers() {
		obs.deleteIObservers();
	}

	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof PanicStopEvent && runningScans != null) {
			runningScans.cancel(true);
		}
	}

	public JythonServerFacade getServer() {
		return server;
	}

	public void setServer(JythonServerFacade server) {
		if (this.server != null) {
			this.server.deleteIObserver(this);
		}
		this.server = server;
		server.addIObserver(this);
	}

	public Scannable getEdgePositioner() {
		return edgePositioner;
	}

	public void setEdgePositioner(Scannable edgePositioner) {
		this.edgePositioner = edgePositioner;
	}

	@Override
	public int braggProtectionLevel() {
		return energy.getProtectionLevel();
	}

	@Override
	public String exafsDataPath() {
		return exafsPath;
	}

	public void setExafsPath(String exafsPath) {
		this.exafsPath = exafsPath;
	}

	@Override
	public String braggDataPath() {
		return braggPath;
	}

	public void setBraggPath(String braggPath) {
		this.braggPath = braggPath;
	}

	public void setExpectedInterceptLimit(double limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("Expected intercept limit must be greater than 0");
		}
	}

	@Override
	public double expectedInterceptLimit() {
		return interceptLimit;
	}

	@Override
	public void setIntercept(double intercept) throws DeviceException {
		double currentOffset = bragg.getMotor().getUserOffset();
		double newOffset = currentOffset + intercept;
		logger.info("New offset should be {}", newOffset);
		server.print("New Offset should be " + newOffset);
	}

	public Collection<Scannable> getAdditionalScannables() {
		return additionalScannables;
	}

	public void setAdditionalScannables(Collection<Scannable> additionalScannables) {
		this.additionalScannables = additionalScannables;
	}

	public DoubleFunction<ScanPositionProvider> getScanPositions() {
		return scanPositions;
	}

	public void setScanPositions(DoubleFunction<ScanPositionProvider> scanPositions) {
		this.scanPositions = scanPositions;
	}

	public GaussianOptimisingScan getPitchOptimiser() {
		return pitchOptimiser;
	}

	public void setPitchOptimiser(GaussianOptimisingScan pitchOptimiser) {
		this.pitchOptimiser = pitchOptimiser;
	}

	public IMetadataEntry getScanTitle() {
		return scanTitle;
	}

	public void setScanTitle(IMetadataEntry scanTitle) {
		this.scanTitle = scanTitle;
	}

	public IMetadataEntry getSampleName() {
		return sampleName;
	}

	public void setSampleName(IMetadataEntry sampleName) {
		this.sampleName = sampleName;
	}
}
