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
package org.eclipse.scanning.test.validation;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.ScanMode;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.example.detector.ConstantVelocityDevice;
import org.eclipse.scanning.example.detector.ConstantVelocityModel;
import org.eclipse.scanning.example.detector.DarkImageDetector;
import org.eclipse.scanning.example.detector.DarkImageModel;
import org.eclipse.scanning.example.detector.MandelbrotDetector;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmTriggeredDetector;
import org.eclipse.scanning.example.malcolm.DummyMalcolmTriggeredModel;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;

public abstract class AbstractValidationTest {

    protected ValidatorService validator;

	@Before
	public void before() throws Exception {
		ServiceTestHelper.setupServices();

		validator = ServiceTestHelper.getValidatorService();

		makeDetectorsAndModels();

		IRunnableDevice<?> device = ServiceTestHelper.getRunnableDeviceService().getRunnableDevice("malcolm");
		IMalcolmDevice mdevice = (IMalcolmDevice) device;

		// Just for testing we give it a dir.
		File dir = File.createTempFile("fred", ".nxs").getParentFile();
		dir.deleteOnExit();
		mdevice.setOutputDir(dir.getAbsolutePath());

		// Just for testing, we make the detector legal.
		TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		// Cannot set the generator from @PreConfigure in this unit test.
		mdevice.setPointGenerator(ServiceTestHelper.getPointGeneratorService().createGenerator(gmodel));
	}

	private void makeDetectorsAndModels() throws ScanningException, IOException {
		// Mandelbrot Example
		MandelbrotModel mandelbrotModel = new MandelbrotModel();
		mandelbrotModel.setMaxIterations(500);
		mandelbrotModel.setEscapeRadius(10.0);
		mandelbrotModel.setColumns(301);
		mandelbrotModel.setRows(241);
		mandelbrotModel.setPoints(1000);
		mandelbrotModel.setMaxRealCoordinate(1.5);
		mandelbrotModel.setMaxImaginaryCoordinate(1.2);
		mandelbrotModel.setName("mandlebrot");
		mandelbrotModel.setExposureTime(0.1);
		mandelbrotModel.setRealAxisName("stage_x");
		mandelbrotModel.setImaginaryAxisName("stage_y");
		DeviceInformation<MandelbrotModel> mandelbrotInfo = new DeviceInformation<>();
		mandelbrotInfo.setId("org.eclipse.scanning.example.detector.mandelbrotDetectorSpring");
		mandelbrotInfo.setLabel("Mandelbrot Example Detector");
		mandelbrotInfo.setDescription("A Mandelbrot detector which is only used for testing");
		mandelbrotInfo.setIcon("org.eclipse.scanning.example/icons/mandelbrot.png");
		MandelbrotDetector mandelbrotDetector = new MandelbrotDetector();
		mandelbrotDetector.setName("mandelbrot");
		mandelbrotDetector.setModel(mandelbrotModel);
		mandelbrotDetector.setDeviceInformation(mandelbrotInfo);
		mandelbrotDetector.setActivated(true);
		mandelbrotDetector.register();

		//Dark Image Example
		DarkImageModel darkImageModel = new DarkImageModel();
		darkImageModel.setName("dkExmpl");
		darkImageModel.setColumns(64);
		darkImageModel.setRows(60);
		darkImageModel.setFrequency(10);
		DeviceInformation<DarkImageModel> darkImageInfo = new DeviceInformation<>();
		darkImageInfo.setId("org.eclipse.scanning.example.detector.darkImageDetector");
		darkImageInfo.setLabel("Dark Current Example Detector");
		darkImageInfo.setDescription("A detector which takes images at a frequency lower than the scan numbner to simulate a dark current reading.");
		darkImageInfo.setIcon("org.eclipse.scanning.example/icons/darkcurrent.png");
		DarkImageDetector darkImageDetector = new DarkImageDetector();
		darkImageDetector.setName("dkExmpl");
		darkImageDetector.setModel(darkImageModel);
		darkImageDetector.setDeviceInformation(darkImageInfo);
		darkImageDetector.register();

		//Constant Velocity Example
		ConstantVelocityModel constantVelocityModel = new ConstantVelocityModel();
		constantVelocityModel.setName("cvExmpl");
		constantVelocityModel.setExposureTime(0.1);
		constantVelocityModel.setLineSize(64);
		constantVelocityModel.setChannelCount(60);
		constantVelocityModel.setSpectraSize(10);
		constantVelocityModel.setTimeout(100);
		DeviceInformation<ConstantVelocityModel> constantVelocityInfo = new DeviceInformation<>();
		constantVelocityInfo.setId("org.eclipse.scanning.example.detector.constantVelocityDevice");
		constantVelocityInfo.setLabel("Constant Velocity Example Detector");
		constantVelocityInfo.setDescription("A detector which runs line scans within the arbitrary outer scan which it is given.");
		ConstantVelocityDevice constantVelocityDevice = new ConstantVelocityDevice();
		constantVelocityDevice.setName("cvExmpl");
		constantVelocityDevice.setModel(constantVelocityModel);
		constantVelocityDevice.setDeviceInformation(constantVelocityInfo);
		constantVelocityDevice.register();

		// Malcolm Example
		DummyMalcolmModel dummyMalcolmModel = new DummyMalcolmModel();
		dummyMalcolmModel.setName("malcolm");
		dummyMalcolmModel.setExposureTime(0.1);
		dummyMalcolmModel.setAxesToMove(Arrays.asList("stage_x", "stage_y"));
		DeviceInformation<IMalcolmModel> dummyMalcolmInfo = new DeviceInformation<>();
		dummyMalcolmInfo.setId("org.eclipse.scanning.example.malcolm.dummyMalcolmDetectorSpring");
		dummyMalcolmInfo.setLabel("Dummy Malcolm Detector");
		dummyMalcolmInfo.setDescription("A dummy malcolm detector which is only used for testing");
		dummyMalcolmInfo.setIcon("org.eclipse.scanning.example/icons/alarm-clock-select.png");
		DummyMalcolmDevice dummyMalcolmDetector = new DummyMalcolmDevice();
		dummyMalcolmDetector.setName("malcolm");
		dummyMalcolmDetector.setModel(dummyMalcolmModel);
		dummyMalcolmDetector.setDeviceInformation(dummyMalcolmInfo);
		dummyMalcolmDetector.setActivated(false);
		dummyMalcolmDetector.register();

		// Dummy Malcolm Triggered Detector Example
		DummyMalcolmTriggeredModel dummyMalcolmTriggeredModel = new DummyMalcolmTriggeredModel();
		dummyMalcolmTriggeredModel.setName("dummyMalcolmTriggered");
		DeviceInformation<DummyMalcolmTriggeredModel> dummyMalcolmTriggeredInfo = new DeviceInformation<>();
		dummyMalcolmTriggeredInfo.setId("org.eclipse.scanning.example.malcolm.dummyMalcolmTriggered");
		dummyMalcolmTriggeredInfo.setLabel("A Dummy Malcolm Triggered Detector");
		dummyMalcolmTriggeredInfo.setDescription("A dummy detector that is triggered by a dummy malcolm device, only used for testing");
		DummyMalcolmTriggeredDetector<DummyMalcolmTriggeredModel> dummyMalcolmTriggeredDetector = new DummyMalcolmTriggeredDetector<DummyMalcolmTriggeredModel>();
		dummyMalcolmTriggeredDetector.setName("dummyMalcolmTriggered");
		dummyMalcolmTriggeredDetector.setModel(dummyMalcolmTriggeredModel);
		dummyMalcolmTriggeredDetector.setDeviceInformation(dummyMalcolmTriggeredInfo);
		dummyMalcolmTriggeredDetector.setActivated(false);
		dummyMalcolmTriggeredDetector.register();


		// Dummy Software or Hardware Triggered Detector Example
		DummyMalcolmTriggeredModel dummyHardwareOrSoftwareTriggeredModel = new DummyMalcolmTriggeredModel();
		dummyHardwareOrSoftwareTriggeredModel.setName("dummyHardwareOrSoftwareTriggered");
		DeviceInformation<DummyMalcolmTriggeredModel> dummyHardwareOrSoftwareTriggeredInfo = new DeviceInformation<>();
		dummyHardwareOrSoftwareTriggeredInfo.setId("org.eclipse.scanning.example.malcolm.dummyHarwareOrSoftwareTriggered");
		dummyHardwareOrSoftwareTriggeredInfo.setLabel("A Dummy Hardware or Software Triggered Detector");
		dummyHardwareOrSoftwareTriggeredInfo.setDescription("A dummy detector that can be triggered by a dummy malcolm device or directly by GDA, only used for testing");
		DummyMalcolmTriggeredDetector<DummyMalcolmTriggeredModel> dummyHardwareOrSoftwareTriggeredDetector = new DummyMalcolmTriggeredDetector<DummyMalcolmTriggeredModel>()  ;
		dummyHardwareOrSoftwareTriggeredDetector.setName("dummyHardwareOrSoftwareTriggered");
		dummyHardwareOrSoftwareTriggeredDetector.setModel(dummyHardwareOrSoftwareTriggeredModel);
		dummyHardwareOrSoftwareTriggeredDetector.setDeviceInformation(dummyHardwareOrSoftwareTriggeredInfo);
		dummyHardwareOrSoftwareTriggeredDetector.setSupportedScanModes(new HashSet<>(Arrays.asList(ScanMode.SOFTWARE, ScanMode.HARDWARE)));
		dummyHardwareOrSoftwareTriggeredDetector.setActivated(false);
		dummyHardwareOrSoftwareTriggeredDetector.register();
	}

}
