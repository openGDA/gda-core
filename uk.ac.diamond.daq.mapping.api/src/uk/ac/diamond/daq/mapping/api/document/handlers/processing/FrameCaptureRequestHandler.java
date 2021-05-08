/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.handlers.processing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.scanning.FrameCollectingScannable;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.gda.api.acquisition.configuration.processing.FrameCaptureRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;

/**
 * Handler for {@link FrameCaptureRequest} instances.
 *
 * Depends on a {@link FrameCollectingScannable} with name given by the property {@value #FRAME_COLLECTOR_NAME_PROPERTY}.
 *
 * @author Maurizio Nagni
 */
@Component
class FrameCaptureRequestHandler implements ProcessingRequestHandler {

	public static final String FRAME_COLLECTOR_NAME_PROPERTY = "frame.capture.decorator";

	private static final Logger logger = LoggerFactory.getLogger(FrameCaptureRequestHandler.class);

	@Override
	public boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		if (!(requestingPair instanceof FrameCaptureRequest)) {
			return false;
		}

		internalHandling((FrameCaptureRequest) requestingPair, scanRequest);

		return true;
	}

	private void internalHandling(FrameCaptureRequest frameCaptureRequest, ScanRequest scanRequest) {

		List<DetectorDocument> detectorDocuments = frameCaptureRequest.getValue();
		String monitorName = LocalProperties.get(FRAME_COLLECTOR_NAME_PROPERTY);

		try {
			FrameCollectingScannable scn = (FrameCollectingScannable) ScannableDeviceConnectorService.getInstance().getScannable(monitorName);
			scn.setDetectorDocument(detectorDocuments.iterator().next());
		} catch (ScanningException e) {
			logger.error("Error retrieving {} '{}'", FrameCollectingScannable.class.getName(), monitorName, e);
			return;
		}

		List<String> monitors = new ArrayList<>(scanRequest.getMonitorNamesPerScan());
		monitors.add(monitorName);
		scanRequest.setMonitorNamesPerScan(monitors);
	}

}
