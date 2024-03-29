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

import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_REQUEST_TOPIC;
import static org.eclipse.scanning.api.event.EventConstants.ACQUIRE_RESPONSE_TOPIC;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IRequester;
import org.eclipse.scanning.api.event.scan.AcquireRequest;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.server.servlet.AcquireServlet;
import org.eclipse.scanning.test.BrokerTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class to test the API changes for AcquireRequest messaging.
 *
 * NOTE: Change Python messaging examples accordingly, if any of these
 * tests fail. The 'examples' package can be found in:
 * org.eclipse.scanning.example.messaging/scripts
 *
 * @author Martin Gaughran
 *
 */
public class AcquireRequestMessagingAPITest extends BrokerTest {

	private IRequester<AcquireRequest> 	requester;
	private AcquireServlet acquireServlet;

	@BeforeEach
	public void setUp() throws Exception {
		setupRunnableDevices();
		connect();
	}

	protected void setupRunnableDevices() throws ScanningException {

		MandelbrotDetector mandy = new MandelbrotDetector();
		final DeviceInformation<MandelbrotModel> info = new DeviceInformation<MandelbrotModel>(); // This comes from extension point or spring in the real world.
		info.setName("drt_mock_mandelbrot_detector");
		info.setLabel("Example Mandelbrot");
		info.setDescription("Example mandelbrot device");
		info.setId("org.eclipse.scanning.example.detector.drtMandelbrotDetector");
		info.setIcon("org.eclipse.scanning.example/icon/mandelbrot.png");
		mandy.setName("drt_mock_mandelbrot_detector");
		mandy.setDeviceInformation(info);
		registerRunnableDevice(mandy);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void registerRunnableDevice(IRunnableDevice device) {
		ServiceProvider.getService(IRunnableDeviceService.class).register(device);
	}

	protected void connect() throws EventException, URISyntaxException {

		acquireServlet = new AcquireServlet();
		acquireServlet.setBroker(uri.toString());
		acquireServlet.connect();

		requester = ServiceProvider.getService(IEventService.class)
				.createRequestor(uri, ACQUIRE_REQUEST_TOPIC, ACQUIRE_RESPONSE_TOPIC);
		requester.setTimeout(10, TimeUnit.SECONDS);
	}

	@AfterEach
	public void stop() throws EventException {
		if (requester!=null) requester.disconnect();
		if (acquireServlet!=null) acquireServlet.disconnect();
	}

	public String getMessageResponse(String sentJson) throws Exception {
		final IEventService eventService = ServiceProvider.getService(IEventService.class);
		AcquireRequest req = eventService.getEventConnectorService().unmarshal(sentJson, null);
		AcquireRequest res = requester.post(req);
		return eventService.getEventConnectorService().marshal(res);
	}

	public File getTempFile() throws IOException {
		final File file = File.createTempFile("art_test", ".nxs");
		System.err.println("Writing to file " + file);
		file.deleteOnExit();
		return file;
	}

	@Test
	public void testAcquire() throws Exception {

		File tempfile = getTempFile();

		String sentJson = "{\"@type\":\"AcquireRequest\",\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\",\"detectorName\":\"drt_mock_mandelbrot_detector\",\"detectorModel\":{\"@type\":\"MandelbrotModel\",\"name\":\"mandelbrot\",\"exposureTime\":0.01,\"maxIterations\":500,\"escapeRadius\":10.0,\"columns\":301,\"rows\":241,\"points\":1000,\"maxRealCoordinate\":1.5,\"maxImaginaryCoordinate\":1.2,\"realAxisName\":\"xNex\",\"imaginaryAxisName\":\"yNex\",\"enableNoise\":false,\"noiseFreeExposureTime\":5.0,\"timeout\":-1},\"filePath\":\"" + tempfile.getAbsolutePath().replace('\\', '/') + "\",\"status\":\"NONE\"}";
		String expectedJson = "{\"@type\":\"AcquireRequest\",\"uniqueId\":\"c8f12aee-d56a-49f6-bc03-9c7de9415674\",\"detectorName\":\"drt_mock_mandelbrot_detector\",\"detectorModel\":{\"@type\":\"MandelbrotModel\",\"name\":\"mandelbrot\",\"exposureTime\":0.01,\"maxIterations\":500,\"escapeRadius\":10.0,\"columns\":301,\"rows\":241,\"points\":1000,\"maxRealCoordinate\":1.5,\"maxImaginaryCoordinate\":1.2,\"realAxisName\":\"xNex\",\"imaginaryAxisName\":\"yNex\",\"enableNoise\":false,\"noiseFreeExposureTime\":5.0,\"timeout\":-1},\"filePath\":\"" + tempfile.getAbsolutePath().replace('\\', '/') + "\",\"status\":\"COMPLETE\"}";

		String returnedJson = getMessageResponse(sentJson);

		SubsetStatus.assertJsonContains("Failed to return all expected scannable devices.", returnedJson, expectedJson);

		assertTrue("Nexus file has zero length.", tempfile.length() != 0);
	}
}
