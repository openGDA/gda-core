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

import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_AXES;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_MODELS;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.FIELD_NAME_SCAN_REQUEST;
import static org.eclipse.scanning.sequencer.nexus.SolsticeConstants.PROPERTY_NAME_UNIQUE_KEYS_PATH;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
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
				.map(n -> n.getPropertyValue(PROPERTY_NAME_UNIQUE_KEYS_PATH))
				.allMatch(Objects::nonNull);
	}

	@Override
	protected NXcollection createNexusObject(NexusScanInfo scanInfo) {
		final NXcollection scanMetadataCollection = super.createNexusObject(scanInfo);

		// write the scanRequest as a JSON string, if present
		final Optional<ScanRequest> scanRequest = getScanRequest();
		if (scanRequest.isPresent()) {
			try {
				// serialize ScanRequest as a JSON string and include in a field
				final String scanRequestJson = ServiceHolder.getMarshallerService().marshal(scanRequest.get());
				scanMetadataCollection.setField(FIELD_NAME_SCAN_REQUEST, scanRequestJson);
			} catch (Exception ne) {
				logger.debug("Unable to write scan request", ne);
			}
		}

		// write the scan path models as a json string, and the scan axes names, if possible
		final IScanPointGeneratorModel scanPathModel = scanModel.getScanPathModel();
		if (scanPathModel != null) {
			try {
				final List<IScanPointGeneratorModel> scanPathModels = scanPathModel instanceof CompoundModel compoundModel ?
						compoundModel.getModels() : List.of(scanPathModel);
				final String scanPathModelsJson = ServiceHolder.getMarshallerService().marshal(scanPathModels);
				scanMetadataCollection.setField(FIELD_NAME_SCAN_MODELS, scanPathModelsJson);
			} catch (Exception ne) {
				logger.debug("Unable to write point models", ne);
			}

			// write the scan axes field if possible
			final List<String> scanAxes = getScanAxes(scanPathModel);
			scanMetadataCollection.setField(FIELD_NAME_SCAN_AXES, scanAxes.toArray(String[]::new));
		}

		return scanMetadataCollection;
	}

	private List<String> getScanAxes(final IScanPointGeneratorModel scanPathModel) {
		if (scanPathModel instanceof CompoundModel compoundModel) {
			return compoundModel.getModels().stream().map(this::getScanAxes).flatMap(List::stream).toList();
		} else if (scanPathModel instanceof AbstractTwoAxisGridModel gridModel) {
			// return a list of the slow axes first, then the fast
			return switch (((AbstractTwoAxisGridModel) scanPathModel).getOrientation()) {
				case HORIZONTAL -> List.of(gridModel.getyAxisName(), gridModel.getxAxisName());
				case VERTICAL -> List.of(gridModel.getxAxisName(), gridModel.getyAxisName());
				default -> throw new IllegalArgumentException("Unexpected orientation: " + gridModel.getOrientation());
			};
		}

		return scanPathModel.getScannableNames();
	}

	private Optional<ScanRequest> getScanRequest() {
		return Optional.of(scanModel).map(ScanModel::getBean).map(ScanBean::getScanRequest);
	}

	@Override
	public void positionPerformed(PositionEvent event) throws ScanningException {
		// don't write unique keys or point timestamps for a malcolm scan, as malcolm performs the inner scan
		if (isHardwareScan()) return;

		final IPosition position = event.getPosition();
		final SliceND scanSlice = getSliceForPosition(position);
		writePosition(scanSlice, position.getStepIndex());
		pointFinished(scanSlice);
	}

	@Override
	public void positionMovePerformed(PositionEvent event) throws ScanningException {
		// don't write unique keys or point timestamps for a malcolm scan, as malcolm performs the inner scan
		if (isHardwareScan()) return;

		final IPosition position = event.getPosition();
		final SliceND scanSlice = getSliceForPosition(position);
		if (writeAfterMovePerformed) {
			writePosition(scanSlice, position.getStepIndex());
		}
		pointStarted(scanSlice);
	}

	private SliceND getSliceForPosition(IPosition scanPosition) {
		int[] scanShape = getScanShape();
		final IScanSlice scanSlice = IScanRankService.getScanRankService().createScanSlice(scanPosition);
		return new SliceND(scanShape, scanSlice.getStart(), scanSlice.getStop(), scanSlice.getStep());
	}

	private int[] getScanShape() {
		final int[] scanShape = scanModel.getScanInformation().getShape();
		return scanShape.length == 0 ? SINGLE_SHAPE : scanShape;
	}

	@Override
	public void scanFinished() throws NexusException {
		super.scanFinished();
		scanDevice.removePositionListener(this);
	}

}
