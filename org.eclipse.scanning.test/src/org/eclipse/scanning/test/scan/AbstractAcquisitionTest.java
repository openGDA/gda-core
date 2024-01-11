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
package org.eclipse.scanning.test.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.scanning.api.IScannable;
import org.eclipse.scanning.api.annotation.scan.AnnotationManager;
import org.eclipse.scanning.api.annotation.scan.PostConfigure;
import org.eclipse.scanning.api.annotation.scan.PreConfigure;
import org.eclipse.scanning.api.device.IDeviceController;
import org.eclipse.scanning.api.device.IDeviceWatchdogService;
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.server.application.Activator;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.utilities.scan.mock.MockDetectorModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractAcquisitionTest {

	protected static IWritableDetector<MockDetectorModel> detector;

	protected static void setupServices() throws Exception {
		// Note, this method is not annotated with @BeforeAll as it is not used by all subclasses
		// Those that require this method call it explicitly from their own @BeforeAll annotated methods
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		final IRunnableDevice<MockDetectorModel> det = ServiceProvider.getService(IRunnableDeviceService.class)
				.getRunnableDevice("detector");
		detector = (IWritableDetector<MockDetectorModel>) det;
	}

	@AfterAll
	protected static void tearDownServices() {
		ServiceProvider.reset();
	}

	protected List<IPosition>            positions;
	protected IRunListener               runListener;

	@BeforeEach
	public void beforeTest() throws Exception {
		positions = new ArrayList<>(20);

		runListener = IRunListener.createRunPerformedListener(event -> positions.add(event.getPosition()));
		detector.addRunListener(runListener);
		detector.getModel().setExposureTime(0.08);
	}

	@AfterEach
	public void afterTest() throws Exception {
		positions.clear();
		detector.removeRunListener(runListener);
	}

	protected IDeviceController createTestScanner(IScannable<?> monitor) throws Exception {
		return createTestScanner(monitor, null, null, 2);
	}

	protected <T extends IDetectorModel> IDeviceController createTestScanner(IScannable<?> monitor, IRunnableDevice<T> device, T dmodel, int dims) throws Exception {
		return createTestScanner(monitor, null, device, dmodel, dims, null, null);
	}

	protected <T extends IDetectorModel> IDeviceController createTestScanner(IRunnableDevice<T> device, double exposureTime, List<String> axisNames, String filePath) throws Exception {

		if (device.getModel() != null) {
			device.getModel().setExposureTime(exposureTime);
		}
		return createTestScanner(null, null, device, null, 2, axisNames, filePath);
	}

    protected <T extends IDetectorModel> IDeviceController createTestScanner(IScannable<?> monitorPerPoint,
		                                         IScannable<?> monitorPerScan,
		                                         IRunnableDevice<T> device,
		                                         T detModel,
		                                         int dims,
		                                         List<String> axisNames,
		                                         String filePath) throws Exception {

		List<IScanPointGeneratorModel> models = new ArrayList<>();
		if (dims>2) {
			for (int i = dims; i>2; i--) {
				String axisName = axisNames != null ? axisNames.get(i - 1) : "T" + i;
				models.add(new AxialStepModel(axisName, 290, 292, 1));
			}
		}
		// Create scan points for a grid and make a generator
		if (axisNames==null) axisNames = Arrays.asList("x", "y");
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel(axisNames.get(0), axisNames.get(1));
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		models.add(gmodel);

		final CompoundModel compoundModel = new CompoundModel(models);
		final IPointGenerator<CompoundModel> gen = ServiceProvider.getService(IPointGeneratorService.class)
				.createCompoundGenerator(compoundModel);


		// Create the model for a scan.
		final ScanModel  scanModel = new ScanModel();
		scanModel.setPointGenerator(gen);
		scanModel.setScanPathModel(compoundModel);
        scanModel.setFilePath(filePath);

		if (device==null) device = (IRunnableDevice<T>)detector;
		scanModel.setDetector(device);
		scanModel.setMonitorsPerPoint(monitorPerPoint);
		scanModel.setMonitorsPerScan(monitorPerScan);
		scanModel.setBean(new ScanBean());

		if (detModel!=null) {
			AnnotationManager manager = new AnnotationManager(Activator.createResolver());
			manager.addDevices(device);
			manager.addContext(new ScanInformation(gen, Arrays.asList(detModel), filePath));

			manager.invoke(PreConfigure.class, scanModel, detModel, gen);
			if (device instanceof AbstractMalcolmDevice) {
				assertNotNull(((AbstractMalcolmDevice)device).getPointGenerator());
			}

			device.configure(detModel);
			assertNotNull(device.getModel());
			assertEquals(detModel, device.getModel());

			manager.invoke(PostConfigure.class, scanModel, detModel, gen);
		}

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = ServiceProvider.getService(IScanService.class).createScanDevice(scanModel, false);
		IDeviceController controller = ServiceProvider.getService(IDeviceWatchdogService.class)
				.create((IPausableDevice<?>)scanner, scanModel.getBean());
		scanModel.setAdditionalScanObjects(controller.getObjects());
		scanner.configure(scanModel);

		return controller;
	}

	protected IRunnableEventDevice<?> runQuickie() throws Exception {
		return runQuickie(false);
	}

	protected IRunnableEventDevice<?> runQuickie(boolean asych) throws Exception {

		// x and y are level 3
		IDeviceController controller = createTestScanner(null);
		IRunnableEventDevice<?> scanner = (IRunnableEventDevice<?>)controller.getDevice();

		List<DeviceState> states = new ArrayList<>();
		// This run should get paused for beam and restarted.
		scanner.addRunListener(IRunListener.createStateChangedListener(evt -> states.add(evt.getDeviceState())));

		if (asych) {
			scanner.start(null);
		} else {
			scanner.run(null);

			assertFalse(states.contains(DeviceState.PAUSED));
			assertTrue(states.contains(DeviceState.RUNNING));
			assertFalse(states.contains(DeviceState.SEEKING));
		}
		return scanner;
	}

}
