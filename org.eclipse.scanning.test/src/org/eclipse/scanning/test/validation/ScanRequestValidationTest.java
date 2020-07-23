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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.Before;
import org.junit.Test;

public class ScanRequestValidationTest extends AbstractValidationTest {

	private ProcessingRequest processingRequest;

	@Before
	public void setup() {
		final RunnableDeviceServiceImpl runnableDeviceServiceImpl = (RunnableDeviceServiceImpl) ServiceTestHelper
				.getRunnableDeviceService();

		for (int i = 1; i <= 3; i++) {
			final String name = "malcolm" + i;
			final DummyMalcolmModel malcolmModel = new DummyMalcolmModel();
			malcolmModel.setName(name);
			final DummyMalcolmDevice malcolmDevice = new DummyMalcolmDevice();
			malcolmDevice.setModel(malcolmModel);
			malcolmDevice.setName(name);
			runnableDeviceServiceImpl._register(name, malcolmDevice);
		}

		processingRequest = new ProcessingRequest();
		final Map<String, Collection<Object>> processingMap = Collections.singletonMap("dawn", Arrays.asList("/tmp/datafile1.json", "/tmp/datafile2.json"));
		processingRequest.setRequest(processingMap);
	}

	@Test(expected=ModelValidationException.class)
	public void emptyRequest() throws Exception {
		validator.validate(new ScanRequest());
	}

	@Test
	public void nullDetectorModelsAllowed() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		validator.validate(new ScanRequest(gmodel, null, (String)null, null, null));
	}

	@Test
	public void standardScanRequestOkay() throws Exception {

		final ScanRequest req = createScanRequest();
		validator.validate(req);
	}

	public void emptyDetectorModelsAllowed() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, (String)null, null, null);
		req.setDetectors(Collections.emptyMap());
		validator.validate(req);
	}


	@Test
	public void legalDetectorModelList() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, (String)null, null, null);
		req.putDetector("mandelbrot", new MandelbrotModel());
		validator.validate(req);
	}

	@Test(expected=ValidationException.class)
	public void nulledAxisName() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel(null, "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, (String)null, null, null);
		req.putDetector("mandelbrot", new MandelbrotModel());
		validator.validate(req);
	}


	@Test(expected=ValidationException.class)
	public void collidingPointsModels() throws Exception {

		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("stage_x", 10, 20, 1), new TwoAxisGridPointsModel("stage_x", "stage_y")));
		final ScanRequest req = new ScanRequest();
		req.putDetector("mandelbrot", new MandelbrotModel());
		req.setCompoundModel(cmodel);
		validator.validate(req);
	}

	@Test
	public void emptyProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		req.setProcessingRequest(new ProcessingRequest());

		validator.validate(req);
	}

	@Test
	public void aCPUAndProcessing() throws Exception {

		ScanRequest req = createScanRequest();

		IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("mandelbrot", dservice.getDeviceInformation("mandelbrot").getModel());
		req.setProcessingRequest(processingRequest);

		validator.validate(req);
	}

	@Test
	public void twoCPUAndProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("mandelbrot", dservice.getDeviceInformation("mandelbrot").getModel());
		req.putDetector("dkExmpl", dservice.getDeviceInformation("dkExmpl").getModel());
		req.setProcessingRequest(processingRequest);

		validator.validate(req);
	}

	@Test(expected=ValidationException.class)
	public void aCPUAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("mandelbrot", dservice.getDeviceInformation("mandelbrot").getModel());
		req.putDetector("malcolm",    dservice.getDeviceInformation("malcolm").getModel());

		validator.validate(req);
	}

	@Test
	public void aProcessingAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("malcolm",    dservice.getDeviceInformation("malcolm").getModel());
		req.setProcessingRequest(processingRequest);

		validator.validate(req);
	}

	@Test
	public void aTriggeredAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("malcolm", dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("dummyMalcolmTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());

		validator.validate(req);
	}

	@Test(expected = ValidationException.class)
	public void aCPUaTriggeredAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("mandelbrot", dservice.getDeviceInformation("mandelbrot").getModel());
		req.putDetector("malcolm", dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("dummyMalcolmTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());

		validator.validate(req);
	}

	@Test
	public void aTriggeredAMalcolmAndAProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("malcolm", dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("dummyMalcolmTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());
		req.setProcessingRequest(processingRequest);

		validator.validate(req);
	}

	@Test(expected=ValidationException.class)
	public void aTriggered() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("dummyMalcolmTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());

		validator.validate(req);
	}

	@Test
	public void aHardwareOrSoftwareTriggered() throws Exception {
		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("dummyHardwareOrSoftwareTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());

		validator.validate(req);
	}

	@Test
	public void aHardwareOrSoftwareTriggeredAndMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();

		req.putDetector("malcolm", dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("dummyHardwareOrSoftwareTriggered", dservice.getDeviceInformation("dummyMalcolmTriggered").getModel());

		validator.validate(req);
	}

	@Test(expected=ValidationException.class)
	public void twoMalcolms() throws Exception {
		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("malcolm1",    dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("malcolm2",    dservice.getDeviceInformation("malcolm").getModel());

		validator.validate(req);
	}

	@Test(expected=ValidationException.class)
	public void threeMalcolms() throws Exception {

		final ScanRequest req = createScanRequest();

		final IRunnableDeviceService dservice = ServiceTestHelper.getRunnableDeviceService();
		req.putDetector("malcolm1",    dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("malcolm2",    dservice.getDeviceInformation("malcolm").getModel());
		req.putDetector("malcolm3",    dservice.getDeviceInformation("malcolm").getModel());

		validator.validate(req);
	}

	private ScanRequest createScanRequest() {
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("fred", 10, 20, 1), gmodel));
		final ScanRequest req = new ScanRequest();
		req.setCompoundModel(cmodel);
        return req;
	}
}
