/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.dawnsci.mapping.ui.datamodel.MapBean;
import org.dawnsci.mapping.ui.datamodel.MappedBlockBean;
import org.dawnsci.mapping.ui.datamodel.MappedDataFileBean;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.scan.IScanListener;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;

//FIXME This needs to be renamed once things settle down
public class MappingScanNewStyleEventObserver implements IScanListener {

	private static final Logger logger = LoggerFactory.getLogger(MappingScanNewStyleEventObserver.class);

	private IEventService eventService;
	private ISubscriber<IScanListener> subscriber;
	private EventAdmin eventAdmin;

	private static final String NEXUS_PREFIX = "/entry/instrument/";



	public void bindIEventService(IEventService eventService) {
		logger.debug("bindIEventService called with {}", eventService.toString());
		this.eventService = eventService;
	}

	public void unbindIEventService(IEventService eventService) {
		logger.debug("unbindIEventService called with {}", eventService.toString());
		if (eventService == this.eventService) {
			this.eventService = null;
		}
	}

	public void bindEventAdmin(EventAdmin eventAdmin) {
		logger.debug("bindEventAdmin called with {}", eventAdmin.toString());
		this.eventAdmin = eventAdmin;
	}

	public void unbindEventAdmin(EventAdmin eventAdmin) {
		logger.debug("unbindEventAdmin called with {}", eventAdmin.toString());
		if (eventAdmin == this.eventAdmin) {
			this.eventAdmin = null;
		}
	}

	public void start() {
		logger.info("Starting the Mapping Scan Event Observer");

		// Check the service is available this should always be true!
		if (eventService == null) {
			logger.error("Tried to start Mapping Scan Event Observer but required services are not available");
			return;
		}

		try {
			final URI uri = new URI(LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, ""));
			subscriber = eventService.createSubscriber(uri, IEventService.STATUS_TOPIC);
			subscriber.addListener(this);
		} catch (URISyntaxException | EventException e) {
			logger.error("Could not subscribe to the event service", e);
		}
		logger.info("Created subscriber");
	}

	@Override
	public void scanEventPerformed(ScanEvent evt) {
		// Don't do anything
	}

	@Override
	public void scanStateChanged(ScanEvent event) {
		ScanBean scanBean = event.getBean();
		// FIXME This fails at the moment the scanbean doesn't have the file path (hardcoded in MappingScanSubmitter)
		final String filePath = scanBean.getFilePath();
		// Scan started
		if (scanBean.scanStart() == true) {
			logger.info("Pushing data to live visualisation from SWMR file: {}", filePath);
			final MappedDataFileBean mappedDataFileBean = createMappingBeanDescription(scanBean, true);
			if (mappedDataFileBean != null) {
				// Create map holding the info needed to display the map
				Map<String, Object> eventMap = new HashMap<String, Object>();
				eventMap.put("path", filePath);
				eventMap.put("map_bean", mappedDataFileBean);
				eventAdmin.postEvent(new Event("org/dawnsci/events/file/OPEN", eventMap));
			}
		}
		// Scan Ended swap out SWMR file for non-SWMR
		if (scanBean.scanEnd() == true) {
			logger.info("Switching SWMR file to non-SWMR: {}", filePath);
			Map<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("path", filePath);
			// Close the old file
			eventAdmin.postEvent(new Event("org/dawnsci/events/file/CLOSE", eventMap));
			// Reopen the non-SWMR file
			eventAdmin.postEvent(new Event("org/dawnsci/events/file/OPEN", eventMap));
		}
	}

	// TODO This method shouldn't be needed once we have self describing files
	private MappedDataFileBean createMappingBeanDescription(ScanBean scanBean, boolean liveFile) {

		MappedDataFileBean mappedDataFileBean = new MappedDataFileBean();

		// Find all the detectors in the scan
		Map<String, ?> detectors = scanBean.getScanRequest().getDetectors();
		for (String detectorName : detectors.keySet()) {

			MappedBlockBean mappedBlockBean = new MappedBlockBean();
			String nexusDetectorPrefix = NEXUS_PREFIX + detectorName + "/";

			List<String> axisNames = new ArrayList<>();
			// Handle 2D rectangle scan
			// FIXME Not sure if this is the best way to get the dimensions or if it is reliable
			mappedBlockBean.setyDim(0);
			mappedBlockBean.setxDim(1);
			// FIXME This is using hard coded strings hopefully the correct NeXus wont need this
			axisNames.add(NEXUS_PREFIX + MappingScanRequestHandler.Y_AXIS_NAME + "/value_demand");
			axisNames.add(NEXUS_PREFIX + MappingScanRequestHandler.X_AXIS_NAME + "/value_demand");
			axisNames.add(null);
			axisNames.add(null);

			mappedBlockBean.setAxes(axisNames.toArray(new String[axisNames.size()]));

			// FIXME Hardcode to 4D ie 2D from scanning axis and 2D from detector
			int scanDimensions = 2;
			int detectorDimensions = 2;
			mappedBlockBean.setRank(scanDimensions + detectorDimensions);
			mappedBlockBean.setName(nexusDetectorPrefix + "data");

			mappedDataFileBean.addBlock(mappedBlockBean);

			MapBean mapBean = new MapBean();
			mapBean.setName(nexusDetectorPrefix + "value"); // FIXME "value" is hard-coded to match MandelbrotDetector only!
			mapBean.setParent(nexusDetectorPrefix + "data");
			mappedDataFileBean.addMap(mapBean);
		}

		// This section add the live info to allow the SWMR visualisation
		if (liveFile) {
			// Check if the required properties are set
			// FIXME There is a issue here that OSGi starts services before the properties are loaded so you don't get them
			if (LocalProperties.get(LocalProperties.GDA_DATASERVER_HOST) == null || LocalProperties.getAsInt(LocalProperties.GDA_DATASERVER_PORT) == null) {
				logger.error("Properties {} or {} are not set not live data can be provided!", LocalProperties.GDA_DATASERVER_HOST, LocalProperties.GDA_DATASERVER_PORT);
			}
			// else { // FIXME this else should handle the case where the properties are available
			// Create the LiveDataBean
			LiveDataBean liveDataBean = new LiveDataBean();

			// TODO have coded defaults here which will work for testing but they should be removed
			liveDataBean.setHost(LocalProperties.get(LocalProperties.GDA_DATASERVER_HOST, "localhost"));
			liveDataBean.setPort(LocalProperties.getAsInt(LocalProperties.GDA_DATASERVER_PORT, 8690));
			mappedDataFileBean.setLiveBean(liveDataBean);
			// }
		}

		return mappedDataFileBean;
	}

}
