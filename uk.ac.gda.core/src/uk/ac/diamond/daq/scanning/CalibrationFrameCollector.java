/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scanning;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.builder.NexusObjectWrapper;
import org.eclipse.scanning.api.AbstractScannable;
import org.eclipse.scanning.api.annotation.scan.PrepareScan;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

/**
 * A temporary solution to dark/flat field collection within the Solstice framework.
 * <p>
 * When included in a scan, this pseudoscannable will configure the beamline as instructed by the beamlineConfiguration
 * map, take a snapshot of the detector used for the main scan, then restore the beamline to its previous configuration.<br>
 * The snapshot is then appended to the overall NeXus file under /entry/instrument/ as an NXdetector.
 */
public class CalibrationFrameCollector extends AbstractScannable<Object> implements INexusDevice<NXdetector> {

	private static final Logger logger = LoggerFactory.getLogger(CalibrationFrameCollector.class);

	private static final String UNSUPPORTED_OPERATION_MESSAGE = "This isn't really a scannable";

	private IRequester<AcquireRequest> acquireRequester;

	/** Maps scannable names to target positions, applied before taking snapshot */
	private final Map<String, Object> beamlineConfiguration;

	/** Name by which the snapshot is linked in the main Nexus file */
	private final String nexusFieldName;

	/**
	 * Maps the name of the Malcolm detector used for the snapshot to the key (under /entry/instrument/) under which
	 * Malcolm will write the snapshot.
	 */
	private final Map<String, String> malcolmDetectorNames;

	/**
	 * Model of the detector to be used for the snapshot.<br>
	 * If this is not configured explicitly, this class will use the first detector in the "main" scan.
	 */
	private IRunnableDevice<? extends IDetectorModel> detector;

	/** File path of the NeXus file corresponding to the single frame scan */
	private String frameFilePath;

	/** NeXus node path to frame dataset */
	private String nexusNodePath;

	/** Stores the initial beamline configuration so it can be restored after taking the snapshot */
	private Map<String, Object> previousConfiguration;

	/**
	 * @param beamlineConfiguration
	 *            A map describing desired scannable positions to move to before acquiring a frame.<br>
	 *            e.g. "shutter1": "Closed", "sample_y": 5.4.
	 * @param nexusFieldName
	 *            The collected frame will appear in the main NeXus file under
	 *            /entry/instrument/nameOfThisScannable/nexusFieldName
	 * @param malcolmDetectorNames
	 *            For every malcolm scan this scannable could be used in, we need the name of the actual detector in
	 *            order to locate its dataset.
	 */
	public CalibrationFrameCollector(Map<String, Object> beamlineConfiguration,
										String nexusFieldName,
										Map<String, String> malcolmDetectorNames) {

		super(null, ScannableDeviceConnectorService.getInstance());

		this.beamlineConfiguration = beamlineConfiguration;
		this.nexusFieldName = nexusFieldName;
		this.malcolmDetectorNames = malcolmDetectorNames;
	}

	@PrepareScan
	public void setDetectorAndCollect(ScanModel model) throws ScanningException {
		// If no detector is set for the "main" scan, we can't do anything here
		if (model.getDetectors().isEmpty()) {
			return;
		}
		// At this point in the scan, the detector models directly in the ScanModel are not up-to-date, especially as
		// regards their exposure time. However, the the models in the ScanRequest are correct, so copy the exposure
		// time from there.
		final IRunnableDevice<? extends IDetectorModel> mainScanDetector = model.getDetectors().get(0);
		final String mainScanDetectorName = mainScanDetector.getName();
		final IDetectorModel modelFromRequest = model.getBean().getScanRequest().getDetectors().get(mainScanDetectorName);
		if (modelFromRequest == null) {
			logger.error("Cannot find detector model for {}", mainScanDetectorName);
			return;
		}
		final double exposureTime = modelFromRequest.getExposureTime();

		// If no detector has been explicitly configured, use the "main scan" detector
		final IRunnableDevice<? extends IDetectorModel> acquisitionDetector = (detector == null) ? mainScanDetector : detector;
		logger.debug("Setting exposure time on {} to {}", acquisitionDetector.getName(), exposureTime);
		acquisitionDetector.getModel().setExposureTime(exposureTime);
		acquire(acquisitionDetector);
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {

		// sanity check (if frame collection failed the failure should have escalated earlier)
		if (nexusFieldName == null || frameFilePath == null || nexusNodePath == null) {
			throw new NexusException("Calibration frame collection failed");
		}

		final NXdetector nxDet = NexusNodeFactory.createNXdetector();
		nxDet.addExternalLink(nexusFieldName, frameFilePath, nexusNodePath);

		final NexusObjectWrapper<NXdetector> prov = new NexusObjectWrapper<>(getName(), nxDet);
		prov.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return prov;
	}

	private void acquire(IRunnableDevice<? extends IDetectorModel> snapshotDetector) throws ScanningException {
		try {
			configureBeamline();
			frameFilePath = collectFrame(snapshotDetector);
			nexusNodePath = generateNodePath(snapshotDetector);
			restoreBeamline();
		} catch (EventException e) {
			throw new ScanningException("Problem taking snapshot", e);
		}
	}

	private void configureBeamline() throws ScanningException {
		if (previousConfiguration == null) previousConfiguration = new HashMap<>();
		previousConfiguration.clear();

		for (Map.Entry<String, Object> position : beamlineConfiguration.entrySet()) {
			// before moving each scannable to the configured position,
			// we cache its current position so we can restore afterwards
			previousConfiguration.put(position.getKey(), getScannablePosition(position.getKey()));
			move(position);
		}
	}

	private void move(Map.Entry<String, Object> scannableInstruction) throws ScanningException {
		ScannableDeviceConnectorService.getInstance()
			.getScannable(scannableInstruction.getKey())
			.setPosition(scannableInstruction.getValue());
	}

	private void restoreBeamline() throws ScanningException {
		for (Map.Entry<String, Object> position : previousConfiguration.entrySet()) {
			move(position);
		}
	}

	/**
	 * Performs an acquire request (single-frame scan); returns path of resulting NeXus file
	 */
	private String collectFrame(IRunnableDevice<? extends IDetectorModel> snapshotDetector) throws EventException {
		if (snapshotDetector == null) {
			logger.warn("Detector not set when getPosition() called");
			return null;
		}

		AcquireRequest request = new AcquireRequest();
		request.setDetectorName(snapshotDetector.getName());
		request.setDetectorModel(snapshotDetector.getModel());
		try {
			logger.info("Posting acquire request with detector {}", snapshotDetector.getName());
			request = getRequester().post(request);
		} catch (InterruptedException e) {
			logger.error("Acquisition interrupted!", e);
			Thread.currentThread().interrupt();
		}

		if (request.getStatus() == Status.COMPLETE) {
			return request.getFilePath();
		} else {
			logger.error("Request not processed successfully: {}", request.getMessage());
			return null;
		}
	}

	/**
	 * Based on the scan's configured detector, generates the node path to the frame dataset
	 */
	private String generateNodePath(IRunnableDevice<? extends IDetectorModel> snapshotDetector) {
		final String detectorName;
		if (snapshotDetector instanceof MalcolmDevice) {
			detectorName = malcolmDetectorNames.get(snapshotDetector.getName());
			Objects.requireNonNull(detectorName, "A detector name entry is required for malcolm scan " + snapshotDetector.getName());
		} else {
			detectorName = snapshotDetector.getName();
		}
		return "/entry/instrument/" + detectorName + "/data";
	}

	private IRequester<AcquireRequest> getRequester() throws EventException {
		if (acquireRequester == null) {
			final IEventService eventService = ServiceHolder.getEventService();
			try {
				final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());
				acquireRequester = eventService.createRequestor(uri, EventConstants.ACQUIRE_REQUEST_TOPIC, EventConstants.ACQUIRE_RESPONSE_TOPIC);
				acquireRequester.setTimeout(5, TimeUnit.SECONDS);
			} catch (URISyntaxException e) {
				throw new EventException(e);
			}
		}
		return acquireRequester;
	}

	private Object getScannablePosition(String scannableName) throws ScanningException {
		return ScannableDeviceConnectorService.getInstance().getScannable(scannableName).getPosition();
	}

	@Override
	public Object getPosition() throws ScanningException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	public void setDetector(IRunnableDevice<? extends IDetectorModel> detector) {
		this.detector = detector;
	}

	@Override
	public String toString() {
		return "CalibrationFrameCollector [acquireRequester=" + acquireRequester + ", beamlineConfiguration="
				+ beamlineConfiguration + ", nexusFieldName=" + nexusFieldName + ", malcolmDetectorNames="
				+ malcolmDetectorNames + ", detector=" + detector + ", frameFilePath=" + frameFilePath
				+ ", nexusNodePath=" + nexusNodePath + ", previousConfiguration=" + previousConfiguration + "]";
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		// Data scannable, nothing to abort.
	}
}
