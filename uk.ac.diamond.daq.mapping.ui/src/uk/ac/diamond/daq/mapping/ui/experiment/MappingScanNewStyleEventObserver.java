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
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.mapping.ui.datamodel.LiveDataBean;
import org.eclipse.scanning.api.event.EventConstants;
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

	// TODO These constants would ideally be defined somewhere in the Dawn mapping UI code
	private static final String DAWNSCI_MAPPING_FILE_OPEN = "org/dawnsci/events/file/OPEN";
	private static final String DAWNSCI_MAPPING_FILE_CLOSE = "org/dawnsci/events/file/CLOSE";

	private IEventService eventService;
	private ISubscriber<IScanListener> subscriber;
	private EventAdmin eventAdmin;

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
			subscriber = eventService.createSubscriber(uri, EventConstants.STATUS_TOPIC);
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
		final String filePath = scanBean.getFilePath();
		// Scan started
		if (scanBean.scanStart() == true) {
			logger.info("Pushing data to live visualisation from SWMR file: {}", filePath);

			// Create the LiveDataBean
			LiveDataBean liveDataBean = new LiveDataBean();

			// Configure the liveDataBean with a host and port to reach a dataserver
			liveDataBean.setHost(LocalProperties.get(LocalProperties.GDA_DATASERVER_HOST));
			// Default the port to -1 so it can be checked next. An Integer is unboxed here to int
			liveDataBean.setPort(LocalProperties.getAsInt(LocalProperties.GDA_DATASERVER_PORT, -1));
			// Check the liveDataBean is valid
			if (liveDataBean.getHost() == null || liveDataBean.getPort() == -1) {
				logger.error("Live visualisation failed. The properties: {} or {} have not been set",
						LocalProperties.GDA_DATASERVER_HOST, LocalProperties.GDA_DATASERVER_PORT);
				// We can't do anything live at this point so return
				return;
			}

			// Create map holding the info needed to display the map
			Map<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("path", filePath);
			eventMap.put("live_bean", liveDataBean);

			// Send the event
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_OPEN, eventMap));
		}
		// Scan ended swap out remote SWMR file access for direct file access
		if (scanBean.scanEnd() == true) {
			logger.info("Switching from remote SWMR file to direct access: {}", filePath);
			Map<String, Object> eventMap = new HashMap<String, Object>();
			eventMap.put("path", filePath);
			// Close the old remote file
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_CLOSE, eventMap));
			// Reopen the file
			eventAdmin.postEvent(new Event(DAWNSCI_MAPPING_FILE_OPEN, eventMap));
		}
	}
}
