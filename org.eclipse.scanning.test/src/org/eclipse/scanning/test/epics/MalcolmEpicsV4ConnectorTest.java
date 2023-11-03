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
package org.eclipse.scanning.test.epics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.IValidatorService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmDeviceException;
import org.eclipse.scanning.api.malcolm.message.MalcolmMessage;
import org.eclipse.scanning.api.malcolm.message.Type;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.connector.epics.MalcolmEpicsV4Connection;
import org.eclipse.scanning.example.malcolm.EPICSv4EvilDevice;
import org.eclipse.scanning.example.malcolm.IEPICSv4Device;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

/**
 * Class for testing the Epics V4 Connection to Malcolm.
 * @author Matt Taylor
 *
 */
public class MalcolmEpicsV4ConnectorTest {

	private IScanService runnableDeviceService;
	private IEPICSv4Device epicsv4Device;
	private MalcolmEpicsV4Connection malcolmConnection;

	@BeforeAll
	public static void setUpServices() {
		IPointGeneratorService pointGenService = new PointGeneratorService();
		ServiceProvider.setService(IValidatorService.class, new ValidatorService());
		ServiceProvider.setService(IPointGeneratorService.class, pointGenService);
	}

	@AfterAll
	public static void tearDownServices() {
		ServiceProvider.reset();
	}

	@BeforeEach
	void before() {
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.malcolmConnection = new MalcolmEpicsV4Connection();
		this.runnableDeviceService = new RunnableDeviceServiceImpl();
	}

	@AfterEach
	void after() {
		// Stop the device
		if (epicsv4Device!=null) epicsv4Device.stop();
	}

	private IMalcolmDevice createMalcolmDevice(String name) {
		MalcolmDevice malcolmDevice = new MalcolmDevice(name, malcolmConnection, runnableDeviceService);
		malcolmDevice.setModel(createMalcolmModel());
		return malcolmDevice;
	}

	protected MalcolmModel createMalcolmModel() {
		final MalcolmModel malcolmModel = new MalcolmModel();
		malcolmModel.setExposureTime(0.1);
		final List<IMalcolmDetectorModel> detectorModels = new ArrayList<>();
		detectorModels.add(new MalcolmDetectorModel("DET", 0.1, 1, true));
		detectorModels.add(new MalcolmDetectorModel("DIFF", 0.05, 2, true));
		detectorModels.add(new MalcolmDetectorModel("PANDA-01", 0.1, 1, false));
		detectorModels.add(new MalcolmDetectorModel("PANDA-02", 0.02, 5, true));
		malcolmModel.setDetectorModels(detectorModels);
		return malcolmModel;
	}

	@Test
	void connectToNonExistentDevice() {

		IMalcolmDevice modelledDevice = createMalcolmDevice("fred");

		// Get the device state.
		assertThrows(MalcolmDeviceException.class, modelledDevice::getDeviceState);
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to get the device state from
	 * it to check that the Epics V4 connection mechanism is working.
	 * @throws Exception
	 */
	@Test
	void connectToValidDevice() throws Exception {

		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();

		// Get the device
		IMalcolmDevice malcolmDevice = createMalcolmDevice(epicsv4Device.getRecordName());

		// Get the device state.
		DeviceState deviceState = malcolmDevice.getDeviceState();

		assertEquals(DeviceState.READY, deviceState);

	}

	@Test
	void connectToEvilDevice() throws Exception {

		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner(EPICSv4EvilDevice.class);
		epicsv4Device = runner.start();

		// Get the device
		IMalcolmDevice malcolmDevice = createMalcolmDevice(epicsv4Device.getRecordName());

		// Get the device state.
		assertThrows(MalcolmDeviceException.class, malcolmDevice::getDeviceState);

	}

	/**
	 * This device is designed to reproduce a hang which happens with the GDA Server
	 * if malcolm has got into an error state. This happened on I18!
	 */
	@Test
	void connectToHangingService() throws Exception {

		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner(EPICSv4EvilDevice.class);
		epicsv4Device = runner.start();

		try {
			System.setProperty("org.eclipse.scanning.malcolm.core.timeout", String.valueOf(100));

			MalcolmEpicsV4Connection hangingConnectorService = new HangingGetConnectorService();
			// Create the device
			IMalcolmDevice malcolmDevice = new MalcolmDevice(epicsv4Device.getRecordName(),
					hangingConnectorService, runnableDeviceService);

			// Get the device state.
			assertThrows(MalcolmDeviceException.class, malcolmDevice::getDeviceState); // Hangs unless timeout is working
		} finally {
			System.setProperty("org.eclipse.scanning.malcolm.core.timeout", String.valueOf(5000));
		}
	}

	/**
	 * Attempts to get the state of a device that doesn't exist. This should throw an exception with a message
	 * detailing that the channel is unavailable.
	 */
	@Test
	void connectToInvalidDevice() {

		try {
			// Get the device
			IMalcolmDevice invalidDevice = createMalcolmDevice("INVALID_DEVICE");

			// Get the device state. This should fail as the device does not exist
			invalidDevice.getDeviceState();

			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue(ex.getMessage().contains("Failed to connect to device 'INVALID_DEVICE'"), ex.getMessage());
			assertTrue(ex.getMessage().contains("channel not connected"), ex.getMessage());
		}
	}

	@Test
	void connectToInvalidDeviceTimeout() {

		try {
			System.setProperty("org.eclipse.scanning.malcolm.core.timeout", String.valueOf(50));
			// Get the device
			IMalcolmDevice modelledDevice = createMalcolmDevice("INVALID_DEVICE");

			// Get the device state. This should fail as the device does not exist
			modelledDevice.getDeviceState();

			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue(ex.getMessage().contains("Failed to connect to device 'INVALID_DEVICE'"));
		} finally {
			System.setProperty("org.eclipse.scanning.malcolm.core.timeout", String.valueOf(5000));
		}
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to get an attribute that doesn't exist.
	 * This should throw an exception with a message detailing that the attribute is not accessible.
	 * Note: this method uses the connector service directly as Malcolm devices now encapsulate their
	 * attributes rather than allowing access to arbitrarily named attributes.
	 * @throws Exception
	 */
	@Test
	void getNonExistantAttribute() throws Exception {
		final String attrName = "nonExistant";
		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();
		// Get the device
		IMalcolmDevice malcolmDevice = createMalcolmDevice(epicsv4Device.getRecordName());

		// Send a message to the connector service to get a non-existent attribute
		// An error message will be returned
		MalcolmMessage msg = malcolmConnection.getMessageGenerator().createGetMessage(attrName);
		MalcolmMessage reply = malcolmConnection.send(malcolmDevice, msg);
		assertEquals(Type.ERROR, reply.getType());
		assertTrue(reply.getMessage().contains("CreateGet failed for '" + attrName + "'"));
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to configure the device after having stopped the device.
	 * Expect to get an error message saying it can't connect to the device.
	 * @throws Exception
	 */
	@Test
	void connectToValidDeviceButOfflineWhenConfigure() throws Exception {
		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();

		// Get the device
		IMalcolmDevice malcolmDevice = createMalcolmDevice(epicsv4Device.getRecordName());

		// Get the device state.
		DeviceState deviceState = malcolmDevice.getDeviceState();

		assertEquals(DeviceState.READY, deviceState);

		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 6, 1));

		IPointGeneratorService pgService = new PointGeneratorService();
		IPointGenerator<CompoundModel> scan = pgService.createGenerator(
				new TwoAxisSpiralModel("stage_x", "stage_y", 1, new BoundingBox(0, -5, 8, 3)), regions);

		MalcolmModel pmac1 = new MalcolmModel();
		pmac1.setExposureTime(23.1);

		// Set the generator on the device
		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.setPointGenerator(scan);
		malcolmDevice.setOutputDir("/TestFile/Dir");
		epicsv4Device.stop();

		try {
			// Call configure
			malcolmDevice.configure(pmac1);
			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue(ex.getMessage().contains("Failed to connect to device"), "Message was: " + ex.getMessage());
			assertTrue(ex.getMessage().contains(epicsv4Device.getRecordName()), "Message was: " + ex.getMessage());
		}
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then attempts to run the device after having stopped the device.
	 * Expect to get an error message saying it can't connect to the device.
	 * @throws Exception
	 */
	@Test
	void connectToValidDeviceButOfflineWhenRun() throws Exception {
		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();

		// Get the device
		IMalcolmDevice modelledDevice = createMalcolmDevice(epicsv4Device.getRecordName());

		// Get the device state.
		DeviceState deviceState = modelledDevice.getDeviceState();

		assertEquals(DeviceState.READY, deviceState);

		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 6, 1));

		IPointGeneratorService pgService = new PointGeneratorService();
		IPointGenerator<CompoundModel> scan = pgService
				.createGenerator(new TwoAxisSpiralModel("stage_x", "stage_y", 1, new BoundingBox(0, -5, 8, 3)), regions);

		MalcolmModel pmac1 = createMalcolmModel();
		pmac1.setExposureTime(23.1);

		// Set the generator on the device
		// Cannot set the generator from @PreConfigure in this unit test.
		modelledDevice.setPointGenerator(scan);
		modelledDevice.setOutputDir("/TestFile/Dir");
		// Call configure
		modelledDevice.configure(pmac1);

		epicsv4Device.stop();

		try {
			modelledDevice.run(null);
			fail("No exception thrown but one was expected");

		} catch (Exception ex) {
			assertEquals(MalcolmDeviceException.class, ex.getClass());
			assertTrue(ex.getMessage().contains("ERROR: channel not connected"), "Message was: " + ex.getMessage());
		}
	}

}
