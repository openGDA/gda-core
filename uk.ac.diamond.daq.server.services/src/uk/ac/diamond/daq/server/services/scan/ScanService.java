/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.server.services.scan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConversionException;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import gda.data.ServiceHolder;
import gda.scan.IScanDataPoint;
import gda.scan.ScanEvent;
import uk.ac.diamond.daq.jms.scan.EndScanEvent;
import uk.ac.diamond.daq.jms.scan.NewScanEvent;
import uk.ac.diamond.daq.jms.scan.ScanPointEvent;

/**
 * Listen to scans and send events over JMS. Currently this is driven by the JythonService as all concurrent scan events
 * are obtainable by registering as a listener.
 */
public class ScanService {

	private static final Logger logger = LoggerFactory.getLogger(ScanService.class);

	private static final String SERVICE_ID = "scanService";
	private static final String SCAN_TOPIC = "SCANEVENT";
	private static final String NEW_SCAN_EVENT = "newScan";
	private static final String END_SCAN_EVENT = "endScan";
	private static final String SCAN_POINT = "scanPoint";

	private Session session;

	private MessageProducer scanEventTopic;

	private MessageConverter messageConveter = jacksonJmsMessageConverter();

	public ScanService() {
		try {
			session = ServiceHolder.getSessionService().getSession();
			var topic = session.createTopic(SCAN_TOPIC);
			scanEventTopic = session.createProducer(topic);
		} catch (Exception e) {
			logger.error("Could not create scan service topics");
		}
	}

	public MessageConverter jacksonJmsMessageConverter() {
		MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
		converter.setTargetType(MessageType.TEXT);
		converter.setTypeIdPropertyName("_type");
		return converter;
	}

	private Set<Integer> activeScans = new HashSet<>();

	public void scanDataPoint(IScanDataPoint isdp) {
		var scanNum = isdp.getScanIdentifier();
		var axes = Stream.concat(Arrays.stream(isdp.getScannableHeader()), isdp.getDetectorHeader().stream())
				.collect(Collectors.toList());
		var data = Arrays.stream(isdp.getAllValuesAsDoubles()).collect(Collectors.toList());
		var event = new ScanPointEvent(SERVICE_ID, SCAN_POINT, axes, scanNum, data);
		try {
			scanEventTopic.send(messageConveter.toMessage(event, session));
		} catch (JMSException | MessageConversionException e) {
			logger.error("Error sending scan data point message");
		}

	}

	public void scanEvent(ScanEvent scanEvent) {
		var scanNumber = scanEvent.getLatestInformation().getScanNumber();
		if (scanNumber == -1) {
			// Scan number not known yet
			return;
		}
		if (activeScans.add(scanNumber)) {
			newScanEvent(scanEvent, scanNumber);
		}

		if (scanEvent.getLatestStatus().isComplete()) {
			activeScans.remove(scanNumber);
			endScanEvent(scanNumber);
		}

	}

	private void endScanEvent(int scanNumber) {
		var end = new EndScanEvent(SERVICE_ID, END_SCAN_EVENT, scanNumber);
		try {
			scanEventTopic.send(messageConveter.toMessage(end, session));
		} catch (JMSException e) {
			logger.error("Error sending end scan event");
		}

	}

	private void newScanEvent(ScanEvent scanEvent, int scanNumber) {
		// extract info for plot line references
		// extract info for scan references
		var axes = Arrays.asList(scanEvent.getLatestInformation().getScannableNames());
		var s = new NewScanEvent(SERVICE_ID, NEW_SCAN_EVENT, axes, scanNumber);
		try {
			scanEventTopic.send(messageConveter.toMessage(s, session));
		} catch (JMSException e) {
			logger.error("Error sending start scan event");
		}
	}
}
