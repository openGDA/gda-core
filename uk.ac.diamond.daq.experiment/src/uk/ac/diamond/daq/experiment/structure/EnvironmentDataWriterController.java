/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.experiment.structure;

import static uk.ac.diamond.daq.experiment.api.EventConstants.EXTERNAL_STATIC_FILE_PUBLISHED_TOPIC;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.ethercat.SoftwareStartStopHDF5Writer;
import uk.ac.diamond.daq.experiment.api.EventConstants;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentEvent.Transition;
import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Receives experiment start and stop events
 * and starts and stops the configured writer accordingly.
 */
public class EnvironmentDataWriterController {

	private static final Logger logger = LoggerFactory.getLogger(EnvironmentDataWriterController.class);

	private SoftwareStartStopHDF5Writer writer;

	/** directory name within experiment directory */
	private String relativeDirectory;

	/** receives experiment start and stop events */
	private ISubscriber<IBeanListener<ExperimentEvent>> experimentListener;

	/** hdf filepath will be broadcast here - experiment controller will link to experiment file */
	private IPublisher<Map<String, Object>> dataFilePublisher;

	public EnvironmentDataWriterController(SoftwareStartStopHDF5Writer writer, String relativeDirectory) {
		this.writer = writer;
		this.relativeDirectory = relativeDirectory.startsWith("/") ? relativeDirectory.substring(1) : relativeDirectory;

		try {
			createConnections();
		} catch (Exception e) {
			logger.error("Failed to create event listeners", e);
		}
	}

	private void createConnections() throws URISyntaxException, EventException {
		URI jmsURI = new URI(LocalProperties.getBrokerURI());
		var service = ServiceProvider.getService(IEventService.class);

		experimentListener = service.createSubscriber(jmsURI, EventConstants.EXPERIMENT_CONTROLLER_TOPIC);
		experimentListener.addListener(this::experimentListener);

		dataFilePublisher = service.createPublisher(jmsURI, EXTERNAL_STATIC_FILE_PUBLISHED_TOPIC);
	}

	private void experimentListener(BeanEvent<ExperimentEvent> beanEvent) {
		var event = beanEvent.getBean();
		if (event.getTransition() == Transition.STARTED) {
			var location = event.getLocation();
			String experimentDir = new File(location.getFile()).getParent();
			String fileDir = String.format("%s/%s", experimentDir, relativeDirectory);
			startFileWriter(fileDir);
		} else {
			try {
				writer.stop();
			} catch (DeviceException e) {
				logger.error("Failed to stop environment writer", e);
			}
		}
	}

	private void startFileWriter(String fileDir) {
		try {
			writer.start(fileDir);
			var dataFile = writer.getFilePath();
			dataFilePublisher.broadcast(Map.of("filePath", dataFile));
			logger.info("Writing environment data to file {}", dataFile);
		} catch (Exception e) {
			logger.error("Failed to start environment writer", e);
		}
	}

}
