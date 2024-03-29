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

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.ValidationException;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class ScanRequestValidationTest extends AbstractValidationTest {

	private ProcessingRequest processingRequest;

	@BeforeEach
	public void setup() {
		for (int i = 1; i <= 3; i++) {
			final String name = "malcolm" + i;
			final DummyMalcolmModel malcolmModel = new DummyMalcolmModel();
			malcolmModel.setName(name);
			final DummyMalcolmDevice malcolmDevice = new DummyMalcolmDevice();
			malcolmDevice.setModel(malcolmModel);
			malcolmDevice.setName(name);
			ServiceProvider.getService(IRunnableDeviceService.class).register(malcolmDevice);
		}

		processingRequest = new ProcessingRequest();
		final Map<String, Collection<Object>> processingMap = Collections.singletonMap("dawn", Arrays.asList("/tmp/datafile1.json", "/tmp/datafile2.json"));
		processingRequest.setRequest(processingMap);
	}

	@Test
	public void emptyRequest() {
		assertThrows(ModelValidationException.class, () -> validatorService.validate(new ScanRequest()));
	}

	@Test
	public void nullDetectorModelsAllowed() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		validatorService.validate(new ScanRequest(gmodel, null, (String)null, null, null));
	}

	@Test
	public void standardScanRequestOkay() throws Exception {

		final ScanRequest req = createScanRequest();
		validatorService.validate(req);
	}

	public void emptyDetectorModelsAllowed() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, (String)null, null, null);
		req.setDetectors(Collections.emptyMap());
		validatorService.validate(req);
	}


	@Test
	public void legalDetectorModelList() throws Exception {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, null, null, null);
		req.putDetector("mandelbrot", new MandelbrotModel());
		validatorService.validate(req);
	}

	@Test
	public void nulledAxisName() {

		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel(null, "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final ScanRequest req = new ScanRequest(gmodel, null, (String)null, null, null);
		req.putDetector("mandelbrot", new MandelbrotModel());
		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}


	@Test
	public void collidingPointsModels() {

		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("stage_x", 10, 20, 1), new TwoAxisGridPointsModel("stage_x", "stage_y")));
		final ScanRequest req = new ScanRequest();
		req.putDetector("mandelbrot", new MandelbrotModel());
		req.setCompoundModel(cmodel);
		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	@Test
	public void emptyProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		req.setProcessingRequest(new ProcessingRequest());

		validatorService.validate(req);
	}

	@Test
	public void aCPUAndProcessing() throws Exception {

		ScanRequest req = createScanRequest();

		req.putDetector("mandelbrot", getDetectorModel("mandelbrot"));
		req.setProcessingRequest(processingRequest);

		validatorService.validate(req);
	}

	@Test
	public void twoCPUAndProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("mandelbrot", getDetectorModel("mandelbrot"));
		req.putDetector("dkExmpl", getDetectorModel("dkExmpl"));
		req.setProcessingRequest(processingRequest);

		validatorService.validate(req);
	}

	@Test
	public void aCPUAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("mandelbrot", getDetectorModel("mandelbrot"));
		req.putDetector("malcolm", getDetectorModel("malcolm"));

		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	@Test
	public void aProcessingAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm", getDetectorModel("malcolm"));
		req.setProcessingRequest(processingRequest);

		validatorService.validate(req);
	}

	@Test
	public void aTriggeredAndAMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm", getDetectorModel("malcolm"));
		req.putDetector("dummyMalcolmTriggered", getDetectorModel("dummyMalcolmTriggered"));

		validatorService.validate(req);
	}

	@Test
	public void aCPUaTriggeredAndAMalcolm() throws Exception  {

		final ScanRequest req = createScanRequest();

		req.putDetector("mandelbrot", getDetectorModel("mandelbrot"));
		req.putDetector("malcolm", getDetectorModel("malcolm"));
		req.putDetector("dummyMalcolmTriggered", getDetectorModel("dummyMalcolmTriggered"));

		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	@Test
	public void aTriggeredAMalcolmAndAProcessing() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm", getDetectorModel("malcolm"));
		req.putDetector("dummyMalcolmTriggered", getDetectorModel("dummyMalcolmTriggered"));
		req.setProcessingRequest(processingRequest);

		validatorService.validate(req);
	}

	@Test
	public void aTriggered() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("dummyMalcolmTriggered", getDetectorModel("dummyMalcolmTriggered"));

		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	@Test
	public void aHardwareOrSoftwareTriggered() throws Exception {
		final ScanRequest req = createScanRequest();

		req.putDetector("dummyHardwareOrSoftwareTriggered", getDetectorModel("dummyMalcolmTriggered"));

		validatorService.validate(req);
	}

	@Test
	public void aHardwareOrSoftwareTriggeredAndMalcolm() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm", getDetectorModel("malcolm"));
		req.putDetector("dummyHardwareOrSoftwareTriggered", getDetectorModel("dummyMalcolmTriggered"));

		validatorService.validate(req);
	}

	@Test
	public void twoMalcolms() throws Exception {
		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm1", getDetectorModel("malcolm"));
		req.putDetector("malcolm2", getDetectorModel("malcolm"));

		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	@Test
	public void threeMalcolms() throws Exception {

		final ScanRequest req = createScanRequest();

		req.putDetector("malcolm1", getDetectorModel("malcolm"));
		req.putDetector("malcolm2", getDetectorModel("malcolm"));
		req.putDetector("malcolm3", getDetectorModel("malcolm"));

		assertThrows(ValidationException.class, () -> validatorService.validate(req));
	}

	private ScanRequest createScanRequest() {
		final TwoAxisGridPointsModel gmodel = new TwoAxisGridPointsModel("stage_x", "stage_y");
		gmodel.setBoundingBox(new BoundingBox(10, -10, 100, -100));
		final CompoundModel cmodel = new CompoundModel(Arrays.asList(new AxialStepModel("fred", 10, 20, 1), gmodel));
		final ScanRequest req = new ScanRequest();
		req.setCompoundModel(cmodel);
		return req;
	}

	private IDetectorModel getDetectorModel(String name) throws Exception {
		return (IDetectorModel) ServiceProvider.getService(IRunnableDeviceService.class)
				.getDeviceInformation(name).getModel();
	}
}
