/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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
import org.eclipse.scanning.api.device.models.IMalcolmModel;
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
import gda.factory.Configurable;
import gda.factory.FactoryException;
import uk.ac.gda.api.acquisition.parameters.FrameRequestDocument;

/**
 * When included in a scan, this pseudoscannable will configure the beamline as instructed by the beamlineConfiguration
 * map, perform an {@link AcquireRequest} (i.e. capture a single frame), then restore the beamline to its previous configuration.<br>
 * The snapshot is then linked to from the overall NeXus file under /entry/instrument/ as an NXdetector.
 *
 * The detector parameters for the frame collection are specified via a {@link FrameRequestDocument} which must be set in advance.
 */
public class FrameCollectingScannable extends AbstractScannable<Object> implements INexusDevice<NXdetector>, Configurable {

	private static final Logger logger = LoggerFactory.getLogger(FrameCollectingScannable.class);

	private static final String UNSUPPORTED_OPERATION_MESSAGE = "This isn't really a scannable";

	/** Path of NeXus file */
	private String frameFilePath;

	/** NeXus node path to frame dataset */
	private String nexusNodePath;

	/**
	 * Name by which the frame data is linked in the main Nexus file<br>
	 * The collected frame will appear in the main NeXus file under <code>
	 * /entry/instrument/nameOfThisScannable/nexusFieldName</code>
	 */
	private String nexusFieldName;

	/**
	 * A map describing desired scannable positions to move to before acquiring a frame.<br>
	 * e.g. "shutter1": "Closed", "sample_y": 5.4.
	 */
	private Map<String, Object> beamlineConfiguration;

	/** Stores the initial beamline configuration so it can be restored after taking the snapshot */
	private Map<String, Object> previousConfiguration;

	/**
	 * By default (when {@link #useDetectorTable} {@code = false}) the exposure in the document
	 * is treated as the scan point generator duration i.e. as the 'exposure' of the {@link MalcolmDevice}.
	 * <p>
	 * When {@link #useDetectorTable} {@code = true}, the exposure in the document will be set
	 * as the exposure of the inner detector model in the Malcolm scan, and the MalcolmDevice will
	 * get an 'exposure' of 0 i.e. Malcolm calculates the duration.
	 */
	private boolean useDetectorTable = false;

	/**
	 * Maps the name of the Malcolm detector used for the snapshot to the key (under /entry/instrument/) under which
	 * Malcolm will write the snapshot.
	 */
	private Map<String, String> malcolmDetectorNames;

	private boolean configured;

	/**
	 * Requests the acquisition
	 */
	private IRequester<AcquireRequest> acquireRequester;

	/**
	 * Describes the acquisition; must be set before the scan.
	 */
	private FrameRequestDocument frameRequestDocument;



	public FrameCollectingScannable() {
		super(null, ScannableDeviceConnectorService.getInstance());
	}


	/**
	 * This is invoked shortly before the main scan is configured
	 */
	@PrepareScan
	public void configureAndCollect(ScanModel scanModel) throws ScanningException {
		configureCollection(scanModel);
		try {
			configureBeamline();
			frameFilePath = collectFrame();
			nexusNodePath = generateNodePath();
			restoreBeamline();
		} catch (EventException e) {
			throw new ScanningException("Problem taking snapshot", e);
		}
	}

	/**
	 * Subclasses may override to perform any final configuration ({@link #setFrameRequestDocument(FrameRequestDocument)}).
	 * The given scan model contains the configuration so far of the main scan,
	 * and may prove useful in configuring this collection.
	 */
	@SuppressWarnings("unused")
	void configureCollection(final ScanModel scanModel) throws ScanningException {
		// base implementation does nothing
	}

	/**
	 * This must be set prior before the frame is collected.
	 *
	 * If this is to be a Malcolm scan, malcolmDetectorName must also be set
	 * (the name of the real detector wrapped in the Malcolm scan).
	 */
	public void setFrameRequestDocument(FrameRequestDocument frameRequestDocument) {
		this.frameRequestDocument = frameRequestDocument;
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

	private Object getScannablePosition(String scannableName) throws ScanningException {
		return ScannableDeviceConnectorService.getInstance().getScannable(scannableName).getPosition();
	}

	private void move(Map.Entry<String, Object> scannableInstruction) throws ScanningException {
		ScannableDeviceConnectorService.getInstance()
			.getScannable(scannableInstruction.getKey())
			.setPosition(scannableInstruction.getValue());
	}

	private String collectFrame() throws EventException, ScanningException {
		if (frameRequestDocument == null) {
			throw new ScanningException("Frame request document not set!");
		}

		var request = new AcquireRequest();
		request.setDetectorName(frameRequestDocument.getName());

		var model = configureDetectorModel(getDetector(frameRequestDocument.getName()));

		request.setDetectorModel(model);

		try {
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

	private IRunnableDevice<? extends IDetectorModel> getDetector(String detectorName) throws ScanningException {
		return ServiceHolder.getRunnableDeviceService().getRunnableDevice(detectorName);
	}

	private IDetectorModel configureDetectorModel(IRunnableDevice<? extends IDetectorModel> detector) {
		if (detector instanceof MalcolmDevice && useDetectorTable) {
			var model = (IMalcolmModel) detector.getModel();
			var innerDetector = model.getDetectorModels().stream()
								.filter(det -> det.getName().equals(malcolmDetectorNames.get(detector.getName())))
								.findFirst().orElseThrow();
			innerDetector.setExposureTime(frameRequestDocument.getExposure());
			model.setExposureTime(0.0);
			return model;
		} else {
			var model = detector.getModel();
			model.setExposureTime(frameRequestDocument.getExposure());
			return model;
		}
	}

	private IRequester<AcquireRequest> getRequester() throws EventException {
		if (acquireRequester == null) {
			final IEventService eventService = org.eclipse.scanning.sequencer.ServiceHolder.getEventService();
			try {
				final var uri = new URI(LocalProperties.getActiveMQBrokerURI());
				acquireRequester = eventService.createRequestor(uri, EventConstants.ACQUIRE_REQUEST_TOPIC, EventConstants.ACQUIRE_RESPONSE_TOPIC);
				acquireRequester.setTimeout(5, TimeUnit.SECONDS);
			} catch (URISyntaxException e) {
				throw new EventException(e);
			}
		}
		return acquireRequester;
	}

	private String generateNodePath() {
		final String detectorName;
		if (frameRequestDocument.getMalcolmDetectorName() != null) {
			detectorName = frameRequestDocument.getMalcolmDetectorName();
		} else {
			detectorName = frameRequestDocument.getName();
		}
		return "/entry/instrument/" + detectorName + "/data";
	}

	private void restoreBeamline() throws ScanningException {
		for (Map.Entry<String, Object> position : previousConfiguration.entrySet()) {
			move(position);
		}
	}

	@Override
	public NexusObjectProvider<NXdetector> getNexusProvider(NexusScanInfo info) throws NexusException {

		// sanity check (if frame collection failed the failure should have escalated earlier)
		if (frameFilePath == null) {
			throw new NexusException("Frame collection failed");
		}

		final var nxDet = NexusNodeFactory.createNXdetector();
		nxDet.addExternalLink(nexusFieldName, frameFilePath, nexusNodePath);

		final NexusObjectWrapper<NXdetector> nexusObjectProvider = new NexusObjectWrapper<>(getName(), nxDet);
		nexusObjectProvider.setCategory(NexusBaseClass.NX_INSTRUMENT);
		return nexusObjectProvider;
	}

	/**
	 * A map describing the beamline configuration required for the frame collection.
	 * The previous configuration will be restored after the frame is collected.
	 * May be empty if no particular configuration is required (but not {@code null}).
	 */
	public void setBeamlineConfiguration(Map<String, Object> beamlineConfiguration) {
		this.beamlineConfiguration = beamlineConfiguration;
	}

	/**
	 * Name by which the frame data is linked in the main Nexus file
	 */
	public void setNexusFieldName(String nexusFieldName) {
		this.nexusFieldName = nexusFieldName;
	}

	protected String getMalcolmDetectorName(IRunnableDevice<? extends IDetectorModel> detector) {
		return detector instanceof MalcolmDevice ? malcolmDetectorNames.get(detector.getName()) : null;
	}

	public void setMalcolmDetectorNames(Map<String, String> malcolmDetectorNames) {
		this.malcolmDetectorNames = malcolmDetectorNames;
	}

	public void setUseDetectorTable(boolean useDetectorTable) {
		this.useDetectorTable = useDetectorTable;
	}

	@Override
	public void configure() throws FactoryException {
		if (configured) {
			return;
		}
		if (beamlineConfiguration == null) {
			throw new FactoryException("beamlineConfiguration not set");
		}
		if (nexusFieldName == null) {
			throw new FactoryException("nexusFieldName not set");
		}
		configured = true;
	}


	@Override
	public void reconfigure() throws FactoryException {
		// nothing to do
	}

	@Override
	public boolean isConfigured() {
		return configured;
	}

	@Override
	public boolean isConfigureAtStartup() {
		return true;
	}

	@Override
	public Object getPosition() throws ScanningException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public Object setPosition(Object value, IPosition position) throws ScanningException {
		throw new UnsupportedOperationException(UNSUPPORTED_OPERATION_MESSAGE);
	}

	@Override
	public void abort() throws ScanningException, InterruptedException {
		// no movement to abort
	}

}
