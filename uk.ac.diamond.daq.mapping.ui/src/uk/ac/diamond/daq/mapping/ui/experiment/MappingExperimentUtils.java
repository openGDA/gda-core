/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.dawnsci.analysis.dataset.slicer.SourceInformation;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.MappingUIConstants;

/**
 * Functions used in mapping experiment setup
 */
class MappingExperimentUtils {

	private static final String DEFAULT_ENTRY_PATH = "/entry/";
	private static final String DEFAULT_DATASET_NAME = "data"; // NXdetector.data field

	// Prevent instantiation
	private MappingExperimentUtils() {
	}

	// Create a requestor to which acquire requests can be posted to ActiveMQ
	static IRequester<AcquireRequest> getAcquireRequestor(IEclipseContext context) throws Exception {
		final IEventService eventService = context.get(IEventService.class);
		final URI uri = new URI(LocalProperties.getActiveMQBrokerURI());
		final IRequester<AcquireRequest> acquireRequestor = eventService.createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
		acquireRequestor.setTimeout(15, TimeUnit.SECONDS);
		return acquireRequestor;
	}

	static AcquireRequest acquireData(IDetectorModel detectorModel, IRequester<AcquireRequest> acquireRequestor) throws Exception {
		final AcquireRequest request = new AcquireRequest();
		request.setDetectorName(detectorModel.getName());
		request.setDetectorModel(detectorModel);
		return acquireRequestor.post(request);
	}

	static Image getImage(String imagePath) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(MappingUIConstants.PLUGIN_ID, imagePath).createImage();
	}

	static String getDatasetPath(IDetectorModel detectorModel) {
		return DEFAULT_ENTRY_PATH + detectorModel.getName() + "/" + DEFAULT_DATASET_NAME;
	}

	static SourceInformation getSourceInformation(IDetectorModel detectorModel, IDataset dataset) {
		return new SourceInformation("/", DEFAULT_ENTRY_PATH + detectorModel.getName() + "/" + DEFAULT_DATASET_NAME, dataset);
	}
}
