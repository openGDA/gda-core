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
import org.eclipse.scanning.api.script.ScriptRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.scanning.FrameCollectingScannable;
import uk.ac.diamond.daq.scanning.ScannableDeviceConnectorService;
import uk.ac.gda.api.acquisition.configuration.processing.FrameCaptureRequest;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.api.acquisition.parameters.FrameRequestDocument;
import uk.ac.gda.core.tool.spring.ServerSpringProperties;
import uk.ac.gda.core.tool.spring.properties.processing.ProcessingRequestProperties;

/**
 * Handles the request by adding the {@link FrameCollectingScannable}
 * referenced by {@link ProcessingRequestProperties#getFrameCaptureDecorator()}
 * as a per-scan monitor to the main scan.
 * <p>
 * Streaming is resumed by a post-acquisition script referenced by {@link #CAMERA_STREAM_SCRIPT_PROPERTY}
 */
@Component
class FrameCaptureRequestHandler implements ProcessingRequestHandler {

	public static final String CAMERA_STREAM_SCRIPT_PROPERTY = "camera.stream.script";

	private static final Logger logger = LoggerFactory.getLogger(FrameCaptureRequestHandler.class);

	@Autowired
	private ServerSpringProperties serverProperties;

	@Override
	public boolean handle(ProcessingRequestPair<?> requestingPair, ScanRequest scanRequest) {
		if (requestingPair instanceof FrameCaptureRequest) {
			return internalHandling((FrameCaptureRequest) requestingPair, scanRequest);
		} else {
			return false;
		}
	}

	private boolean internalHandling(FrameCaptureRequest frameCaptureRequest, ScanRequest scanRequest) {
		List<FrameRequestDocument> detectorDocuments = frameCaptureRequest.getValue();

		String monitorName = serverProperties.getProcessingRequests().getFrameCaptureDecorator();
		if (monitorName == null) {
			logger.error("Frame capture decorator not defined");
			return false;
		}

		try {
			FrameCollectingScannable scn = (FrameCollectingScannable) ScannableDeviceConnectorService.getInstance().getScannable(monitorName);
			scn.setFrameRequestDocument(detectorDocuments.iterator().next());
		} catch (ScanningException e) {
			logger.error("Error retrieving {} '{}'", FrameCollectingScannable.class.getName(), monitorName, e);
			return false;
		}

		List<String> monitors = new ArrayList<>(scanRequest.getMonitorNamesPerScan());
		monitors.add(monitorName);
		scanRequest.setMonitorNamesPerScan(monitors);


		var script = LocalProperties.get(CAMERA_STREAM_SCRIPT_PROPERTY);
		if (script != null) {
			var scriptRequest = new ScriptRequest(script);
			scanRequest.setAfterScript(scriptRequest);
		}

		return true;
	}

}
