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

package org.eclipse.scanning.sequencer.nexus;

import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_MODELS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_REQUEST;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXcollection;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusScanInfo;
import org.eclipse.dawnsci.nexus.NexusScanInfo.ScanRole;
import org.eclipse.dawnsci.nexus.builder.NexusObjectProvider;
import org.eclipse.dawnsci.nexus.scan.NexusScanMetadataWriter;
import org.eclipse.january.dataset.SliceND;
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.scan.PositionEvent;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.api.scan.rank.IScanRankService;
import org.eclipse.scanning.api.scan.rank.IScanSlice;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The scan points writer writes scan metadata to a collection within the entry.
 * Most of the data written is time-related information about the scan, such as scan start
 * and end times, duration, dead time, and the start and end times for each point.
 * It also and create a unique keys dataset which is written to at the end of each point
 * in the scan, this can be used to track how far the scan has progressed.
 */
public class SolsticeScanMetadataWriter extends NexusScanMetadataWriter implements IPositionListener, INexusDevice<NXcollection> {

	private static final Logger logger = LoggerFactory.getLogger(SolsticeScanMetadataWriter.class);

	private IScanDevice scanDevice;

	private final ScanModel scanModel;

	private boolean writeAfterMovePerformed = false;

	public SolsticeScanMetadataWriter(IScanDevice scanDevice, ScanModel scanModel) {
		this.scanDevice = scanDevice;
		this.scanModel = scanModel;
		scanDevice.addPositionListener(this);
		setHardwareScan(scanModel.isMalcolmScan());
	}

	@Override
	public void setNexusObjectProviders(Map<ScanRole, List<NexusObjectProvider<?>>> nexusObjectProviders) {
		super.setNexusObjectProviders(nexusObjectProviders);

		final List<NexusObjectProvider<?>> detectors = nexusObjectProviders.get(ScanRole.DETECTOR);
		writeAfterMovePerformed = detectors != null && detectors.stream()
				.map(n -> n.getPropertyValue(SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH))
				.allMatch(Objects::nonNull);
	}

	@Override
	protected NXcollection createNexusObject(NexusScanInfo scanInfo) {
		final NXcollection scanPointsCollection = super.createNexusObject(scanInfo);
		getScanRequest().ifPresent(request -> writeScanRequestAndPointsModel(scanPointsCollection, request));
		return scanPointsCollection;
	}

	private void writeScanRequestAndPointsModel(final NXcollection scanPointsCollection, ScanRequest request) {
		final IMarshallerService marshallerService = ServiceHolder.getMarshallerService();
		try {
			// serialize ScanRequest as a JSON string and include in a field
			final String scanRequestJson = marshallerService.marshal(request);
			scanPointsCollection.setField(FIELD_NAME_SCAN_REQUEST, scanRequestJson);
		} catch (Exception ne) {
			logger.debug("Unable to write scan request", ne);
		}
		try {
			List<IScanPointGeneratorModel> models = request.getCompoundModel().getModels();
			String json = marshallerService.marshal(models);
			scanPointsCollection.setField(FIELD_NAME_SCAN_MODELS, json);
		} catch (Exception ne) {
			logger.debug("Unable to write point models", ne);
		}
	}

	private Optional<ScanRequest> getScanRequest() {
		// First check the bean to see if null, then check scan request.
		// Return an empty optional unless both are present.
		return Optional.ofNullable(scanModel.getBean())
				.map(ScanBean::getScanRequest)
				.flatMap(Optional::ofNullable);
	}

	@Override
	public void positionPerformed(PositionEvent event) throws ScanningException {
		final IPosition position = event.getPosition();
		final SliceND scanSlice = getSliceForPosition(position);
		writePosition(scanSlice, position.getStepIndex());
		pointFinished(scanSlice);
	}

	@Override
	public void positionMovePerformed(PositionEvent event) throws ScanningException {
		final IPosition position = event.getPosition();
		final SliceND scanSlice = getSliceForPosition(position);
		if (writeAfterMovePerformed) {
			writePosition(scanSlice, position.getStepIndex());
		}
		pointStarted(scanSlice);
	}

	private SliceND getSliceForPosition(IPosition scanPosition) {
		final int[] scanShape = scanModel.getScanInformation().getShape();
		final IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(scanPosition);
		return new SliceND(scanShape, scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
	}

	@Override
	public void scanFinished() throws NexusException {
		super.scanFinished();
		scanDevice.removePositionListener(this);
	}

}
