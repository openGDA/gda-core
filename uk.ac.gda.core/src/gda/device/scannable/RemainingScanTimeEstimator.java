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

package gda.device.scannable;

import static uk.ac.diamond.daq.api.messaging.messages.DestinationConstants.GDA_MESSAGES_SCAN_TOPIC;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.EventListener;
import java.util.Map;
import java.util.Optional;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.bean.BeanEvent;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.server.servlet.Services;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.monitor.MonitorBase;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.api.messaging.messages.ScanMessage;

/**
 * Subscribes to {@link ScanMessage} updates
 * and produces an estimated remaining scan time.
 */
public class RemainingScanTimeEstimator extends MonitorBase {

	private static final Logger logger = LoggerFactory.getLogger(RemainingScanTimeEstimator.class);

	/** Subscribed to receive {@link ScanMessage}s */
	private ISubscriber<EventListener> scanSubscriber;
	private Optional<ScanSpeedTracker> activeScanSpeedCalculations = Optional.empty();

	@Override
	public Object getPosition() {
		if (scanSubscriber.isConnected()) {
			if (activeScanSpeedCalculations.isPresent()) {
				return activeScanSpeedCalculations.get().estimateRemainingScanTime();
			} else {
				return "No active scan";
			}
		}
		return "Disconnected";
	}

	@Override
	public void configure() throws FactoryException {
		connect();
		setConfigured(true);
	}

	@Override
	public void close() throws DeviceException {
		disconnect();
		setConfigured(false);
	}

	private void connect() {
		try {
			URI activemqUri = new URI(LocalProperties.getActiveMQBrokerURI());

			scanSubscriber = Services.getEventService().createSubscriber(activemqUri, GDA_MESSAGES_SCAN_TOPIC);
			scanSubscriber.addListener(new ScanListener());

		} catch (URISyntaxException | EventException e) {
			logger.error("Failed to connect to the required resources", e);
		}
	}

	private void disconnect() {
		try {
			scanSubscriber.removeAllListeners();
			scanSubscriber.disconnect();
		} catch (EventException e) {
			logger.error("Error closing connection", e);
		}
	}

	/**
	 * Parses a {@link ScanMessage}, either creating a new {@link ScanSpeedTracker} instance
	 * when a new scan is detected, or registering an update to the existing scan.
	 */
	private class ScanListener implements IBeanListener<Map<String, Object>> {

		@Override
		public void beanChangePerformed(BeanEvent<Map<String, Object>> evt) {
			Map<String, Object> msg = evt.getBean();

			String status = msg.get("status").toString();

			if (status.equals("FINISHED")) {
				activeScanSpeedCalculations = Optional.empty();
			} else {
				String filePath = msg.get("filePath").toString();

				var now = Instant.now();

				Double percentageComplete = (Double) msg.get("percentageComplete");

				if (activeScanSpeedCalculations.isEmpty() || !activeScanSpeedCalculations.get().getFilePath().equals(filePath)) {
					activeScanSpeedCalculations = Optional.of(new ScanSpeedTracker(filePath, now, percentageComplete));
				} else {
					activeScanSpeedCalculations.get().registerUpdate(now, percentageComplete);
				}
			}
			notifyIObservers(RemainingScanTimeEstimator.this, getPosition());
		}

	}

	private class ScanSpeedTracker {

		private String filepath;
		private Instant initialTimestamp;
		private Double initialPercentage;
		private Double lastPercentage;
		private Double latestScanSpeed;


		public ScanSpeedTracker(String filepath, Instant timestamp, Double percentage) {
			this.filepath = filepath;
			this.initialTimestamp = timestamp;
			this.initialPercentage = percentage;
		}

		/** serves as a scan ID */
		public String getFilePath() {
			return filepath;
		}

		public void registerUpdate(Instant timestamp, Double percentageComplete) {
			if (percentageComplete - initialPercentage < 1e-6) {
				// too early, ignore this update
				return;
			}
			lastPercentage = percentageComplete;
			latestScanSpeed = estimateScanSpeed(timestamp, percentageComplete);
		}

		private double estimateScanSpeed(Instant timestamp, Double percentageComplete) {
			var elapsedPercentage = percentageComplete - initialPercentage;
			var elapsedDuration = Duration.between(initialTimestamp, timestamp).getSeconds();
			return elapsedDuration / elapsedPercentage;
		}


		public Object estimateRemainingScanTime() {
			var percentageRemaining = 100.0 - lastPercentage;
			return latestScanSpeed * percentageRemaining;
		}

	}

}
