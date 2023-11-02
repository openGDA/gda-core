/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.test.messaging;

import static org.eclipse.scanning.api.event.EventConstants.POSITIONER_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.POSITIONER_RESPONSE_TOPIC;

import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.PositionerRequest;
import org.eclipse.scanning.example.scannable.MockNeXusScannable;
import org.eclipse.scanning.example.scannable.MockScannable;
import org.eclipse.scanning.server.servlet.PositionerServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Class to test the API changes for PositionerRequest messaging.
 *
 * NOTE: Change Python messaging examples accordingly, if any of these
 * tests fail. The 'examples' package can be found in:
 * org.eclipse.scanning.example.messaging/scripts
 *
 * @author Martin Gaughran
 *
 */
public class PositionerRequestMessagingAPITest extends BrokerTest {

	private static IEventService eservice;

	private IRequester<PositionerRequest> requester;
	private PositionerServlet positionerServlet;

	@BeforeAll
	public static void setUpServices() {
		eservice = ServiceTestHelper.getEventService();
	}

	@BeforeEach
	public void setUp() throws Exception {
		setupScannableDevices();

		connect();
	}

	protected void setupScannableDevices() {

		registerScannableDevice(new MockScannable("drt_mock_scannable", 10d, 2, "µm"));

		MockScannable x = new MockNeXusScannable("drt_mock_nexus_scannable", 0d,  3, "mm");
		x.setRealisticMove(true);
		x.setRequireSleep(false);
		x.setMoveRate(10000);

		registerScannableDevice(x);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerScannableDevice(IScannable device) {
		ServiceTestHelper.getScannableDeviceService().register(device);
	}

	protected void connect() throws EventException, URISyntaxException {

		positionerServlet = new PositionerServlet();
		positionerServlet.setBroker(uri.toString());
		positionerServlet.connect();

		requester = eservice.createRequestor(uri, POSITIONER_REQUEST_TOPIC, POSITIONER_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.SECONDS);
	}

	@AfterEach
	public void stop() throws EventException {

	if (requester!=null) requester.disconnect();
	if (positionerServlet!=null) positionerServlet.disconnect();
	}

	public String getMessageResponse(String sentJson) throws Exception {

		PositionerRequest req = eservice.getEventConnectorService().unmarshal(sentJson, null);
		PositionerRequest res = requester.post(req);
		return eservice.getEventConnectorService().marshal(res);
	}

	@Test
	public void testPositioner() throws Exception {

		String sentJson = "{\"@type\":\"PositionerRequest\",\"requestType\":\"SET\",\"position\": {\"values\":{\"drt_mock_scannable\":290.0},\"indices\":{\"T\":0},\"stepIndex\": -1, \"dimensionNames\":[[\"drt_mock_scannable\"]]},\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\"}";
		String expectedJson = "{\"@type\":\"PositionerRequest\",\"requestType\":\"SET\",\"position\": {\"values\":{\"drt_mock_scannable\":290.0},\"indices\":{},\"stepIndex\": -1, \"dimensionNames\":[[\"drt_mock_scannable\"]]},\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\"}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return correct Positioner response.", returnedJson, expectedJson);

	}
}
