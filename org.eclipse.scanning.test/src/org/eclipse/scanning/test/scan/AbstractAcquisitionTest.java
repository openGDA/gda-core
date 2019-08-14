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
import org.eclipse.scanning.api.device.IPausableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.server.application.Activator;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.eclipse.scanning.test.scan.mock.MockDetectorModel;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractAcquisitionTest {

	protected static IWritableDetector<MockDetectorModel> detector;

	protected static void setupServices() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		detector = (IWritableDetector) ServiceTestHelper.getRunnableDeviceService().getRunnableDevice("detector");
	}

	protected List<IPosition>            positions;
	protected IRunListener               runListener;

	@Before
	public void beforeTest() throws Exception {
		positions = new ArrayList<>(20);

		runListener = new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException{
               //System.out.println("Ran mock detector @ "+evt.getPosition());
               positions.add(evt.getPosition());
			}
		};

		detector.addRunListener(runListener);
	}

	@After
	public void afterTest() throws Exception {
		positions.clear();
		detector.removeRunListener(runListener);
	}

//	@AfterClass
//	public static void cleanup() throws Exception {
//		ServiceHolder.setRunnableDeviceService(null);
//		ServiceHolder.setWatchdogService(null);
//	}

	protected IDeviceController createTestScanner(IScannable<?> monitor) throws Exception {
		return createTestScanner(monitor, null, null, 2);
	}

	protected <T> IDeviceController createTestScanner(IScannable<?> monitor, IRunnableDevice<T> device, T dmodel, int dims) throws Exception {
		return createTestScanner(monitor, null, device, dmodel, dims, null, null);
	}

	protected <T> IDeviceController createTestScanner(IRunnableDevice<T> device, double exposureTime, List<String> axisNames, String filePath) throws Exception {

		if (device.getModel()!=null && device.getModel() instanceof IDetectorModel) {
			((IDetectorModel)device.getModel()).setExposureTime(exposureTime);
		}
		return createTestScanner(null, null, device, null, 2, axisNames, filePath);
	}

    protected <T> IDeviceController createTestScanner(IScannable<?>     monitorPerPoint,
		                                         IScannable<?>     monitorPerScan,
		                                         IRunnableDevice<T> device,
		                                         T dmodel,
		                                         int dims,
		                                         List<String> axisNames,
		                                         String filePath) throws Exception {

		List<IScanPathModel> models = new ArrayList<>();
		if (dims>2) {
			for (int i = dims; i>2; i--) {
				String axisName = axisNames != null ? axisNames.get(i - 1) : "T" + i;
				models.add(new StepModel(axisName, 290, 292, 1));
			}
		}
		// Create scan points for a grid and make a generator
		if (axisNames==null) axisNames = Arrays.asList("x", "y");
		GridModel gmodel = new GridModel(axisNames.get(0), axisNames.get(1));
		gmodel.setyAxisPoints(5);
		gmodel.setxAxisPoints(5);
		gmodel.setBoundingBox(new BoundingBox(0,0,3,3));
		models.add(gmodel);

		IPointGenerator<?> gen = ServiceTestHelper.getPointGeneratorService().createCompoundGenerator(new CompoundModel<>(models));

		if (dmodel!=null) {
			AnnotationManager manager = new AnnotationManager(Activator.createResolver());
			manager.addDevices(device);
			manager.addContext(new ScanInformation(gen, Arrays.asList(dmodel), filePath));

			manager.invoke(PreConfigure.class, dmodel, gen);
			if (device instanceof AbstractMalcolmDevice) {
				assertNotNull(((AbstractMalcolmDevice)device).getPointGenerator());
			}

			device.configure(dmodel);
			assertNotNull(device.getModel());
			assertEquals(dmodel, device.getModel());

			manager.invoke(PostConfigure.class, dmodel, gen);
		}

		// Create the model for a scan.
		final ScanModel  smodel = new ScanModel();
		smodel.setPointGenerator(gen);
        smodel.setFilePath(filePath);

		if (device==null) device = (IRunnableDevice<T>)detector;
		smodel.setDetectors(device);
		smodel.setMonitorsPerPoint(monitorPerPoint);
		smodel.setMonitorsPerScan(monitorPerScan);
		smodel.setBean(new ScanBean());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = ServiceTestHelper.getRunnableDeviceService().createRunnableDevice(smodel, null, false);
		IDeviceController controller = ServiceTestHelper.getDeviceWatchdogService().create((IPausableDevice<?>)scanner, smodel.getBean());
		smodel.setAnnotationParticipants(controller.getObjects());
		scanner.configure(smodel);

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
		scanner.addRunListener(new IRunListener() {
			@Override
			public void stateChanged(RunEvent evt) throws ScanningException {
				states.add(evt.getDeviceState());
			}
		});

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
