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
package org.eclipse.scanning.test.malcolm.real;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.attributes.IDeviceAttribute;
import org.eclipse.scanning.api.malcolm.attributes.StringAttribute;
import org.eclipse.scanning.api.malcolm.attributes.TableAttribute;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.connector.epics.MalcolmEpicsV4Connection;
import org.eclipse.scanning.example.malcolm.IEPICSv4Device;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.sequencer.RunnableDeviceServiceImpl;
import org.eclipse.scanning.test.epics.DeviceRunner;
import org.epics.pvdata.factory.FieldFactory;
import org.epics.pvdata.factory.PVDataFactory;
import org.epics.pvdata.pv.PVDouble;
import org.epics.pvdata.pv.PVDoubleArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStringArray;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.PVUnionArray;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Structure;
import org.epics.pvdata.pv.Union;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ExampleMalcolmDeviceTest {

	private IScanService service;
	private IEPICSv4Device epicsv4Device;
	private MalcolmEpicsV4Connection connectorService;
	private IMalcolmDevice<MalcolmModel> malcolmDevice;
	private IPointGeneratorService pointGenService;

	@Before
	public void setUp() throws Exception {
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.service = new RunnableDeviceServiceImpl();
		this.connectorService = new MalcolmEpicsV4Connection();

		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();

		// Create the device
		malcolmDevice = new MalcolmDevice<>(epicsv4Device.getRecordName(), connectorService, service);
		pointGenService = new PointGeneratorService();
	}

	@After
	public void tearDown() throws Exception {
		malcolmDevice.dispose();
		// Stop the device
		epicsv4Device.stop();
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then probes it with the configure() and call() methods
	 * as well as getting a list of all attributes and several specific attributes.
	 * This method thoroughly checks the malcolm attributes and the PV structures created by the EpicsV4
	 * connector sevice
	 * @throws Exception
	 */
	@Test
	public void configureAndRunDummyMalcolm() throws Exception {
		configureMalcolmDevice();

		// Test that malcolm attributes have the expected value
		List<IDeviceAttribute<?>> attribs = malcolmDevice.getAllAttributes();
		assertEquals(11, attribs.size());
		Set<String> attributeNames = attribs.stream().map(IDeviceAttribute::getName).collect(Collectors.toSet());
		assertThat(attributeNames, containsInAnyOrder("state", "health", "busy", "totalSteps", "A", "B", "axesToMove",
				"datasets", "generator", "completedSteps", "layout"));

		assertEquals("ARMED", malcolmDevice.getAttributeValue("state"));
		assertEquals("Test Health", malcolmDevice.getAttributeValue("health"));
		assertEquals(false, malcolmDevice.getAttributeValue("busy"));
		assertEquals(Integer.valueOf(123), malcolmDevice.getAttributeValue("totalSteps"));
		StringAttribute healthAttributeValue = (StringAttribute) (Object) malcolmDevice.getAttribute("health"); // TODO why is this double cast necessary?
		assertEquals("health", healthAttributeValue.getName());
		assertEquals("Test Health", healthAttributeValue.getValue());
		assertEquals(false, malcolmDevice.getAttribute("A").isWriteable());
		assertEquals(true, malcolmDevice.getAttribute("B").isWriteable());

		// Test the 'datasets' attribute
		TableAttribute datasetAttributeValue = (TableAttribute) (Object) malcolmDevice.getAttribute("datasets"); // TODO why is this double cast necessary?
		assertEquals("datasets", datasetAttributeValue.getName());
		MalcolmTable malcolmTable = datasetAttributeValue.getValue();

		assertEquals(4, malcolmTable.getHeadings().size());
		assertEquals("detector", malcolmTable.getHeadings().get(0));
		assertEquals("filename", malcolmTable.getHeadings().get(1));
		assertEquals("dataset", malcolmTable.getHeadings().get(2));
		assertEquals("users", malcolmTable.getHeadings().get(3));
		assertEquals(3, malcolmTable.getColumn("dataset").size());
		assertEquals("/entry/detector/I200", malcolmTable.getColumn("dataset").get(0));
		assertEquals("/entry/detector/Iref", malcolmTable.getColumn("dataset").get(1));
		assertEquals("/entry/detector/det1", malcolmTable.getColumn("dataset").get(2));

		// Call run
		malcolmDevice.run(null);

		// Check seek method works
		malcolmDevice.seek(4);


		// Check the RPC calls were received correctly by the device
		Map<String, PVStructure> rpcCalls = epicsv4Device.getReceivedRPCCalls();
		assertEquals(4, rpcCalls.size());

		// Check the 'configure' call is as expected
		// first create the expected structure (it's quite large)
		Union union = FieldFactory.getFieldCreate().createVariantUnion();
		Structure expectedGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray("mutators", union)
				.add("duration", ScalarType.pvDouble)
				.add("continuous", ScalarType.pvBoolean)
				.addArray("generators", union)
				.addArray("excluders", union)
				.setId("scanpointgenerator:generator/CompoundGenerator:1.0").createStructure();

		Structure expectedSpiralGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray("axes", ScalarType.pvString)
				.addArray("centre", ScalarType.pvDouble)
				.add("scale", ScalarType.pvDouble)
				.add("alternate", ScalarType.pvBoolean)
				.addArray("units", ScalarType.pvString)
				.add("radius", ScalarType.pvDouble)
				.setId("scanpointgenerator:generator/SpiralGenerator:1.0").createStructure();

		Structure expectedCircularRoiStructure = FieldFactory.getFieldCreate().createFieldBuilder().
				addArray("centre", ScalarType.pvDouble).
				add("radius", ScalarType.pvDouble).
				setId("scanpointgenerator:roi/CircularROI:1.0").
				createStructure();

		Structure expectedExcluderStructure = FieldFactory.getFieldCreate().createFieldBuilder().
				addArray("axes", ScalarType.pvString).
				addArray("rois", union).
				setId("scanpointgenerator:excluder/ROIExcluder:1.0").
				createStructure();

		Structure expectedConfigureStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.add("generator", expectedGeneratorStructure)
				.addArray("axesToMove", ScalarType.pvString)
				.add("fileDir", ScalarType.pvString)
				.add("fileTemplate", ScalarType.pvString)
				.createStructure();

		PVStructure expectedSpiralGeneratorPVStructure = PVDataFactory.getPVDataCreate()
				.createPVStructure(expectedSpiralGeneratorStructure);
		double[] centre = new double[] { 1.5, -2.0 };
		expectedSpiralGeneratorPVStructure.getSubField(PVDoubleArray.class, "centre").put(0, centre.length, centre, 0);
		expectedSpiralGeneratorPVStructure.getDoubleField("scale").put(1.0);
		String[] units = new String[] {"mm", "mm"};
		expectedSpiralGeneratorPVStructure.getSubField(PVStringArray.class, "units").put(0, units.length, units, 0);
		String[] names = new String[] { "stage_x", "stage_y" };
		expectedSpiralGeneratorPVStructure.getSubField(PVStringArray.class, "axes").put(0, names.length, names, 0);
		expectedSpiralGeneratorPVStructure.getBooleanField("alternate").put(false);
		expectedSpiralGeneratorPVStructure.getDoubleField("radius").put(7.632168761236874);


		PVStructure expectedConfigurePVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedConfigureStructure);
		PVUnion pvu1 = PVDataFactory.getPVDataCreate().createPVVariantUnion();
		pvu1.set(expectedSpiralGeneratorPVStructure);
		PVUnion[] unionArray = new PVUnion[1];
		unionArray[0] = pvu1;
		expectedConfigurePVStructure.getUnionArrayField("generator.generators").put(0, unionArray.length, unionArray, 0);

		PVStructure expectedExcluderPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedExcluderStructure);
		PVStringArray scannablesVal = expectedExcluderPVStructure.getSubField(PVStringArray.class, "axes");
		String[] scannables = new String[] {"stage_x", "stage_y"};
		scannablesVal.put(0, scannables.length, scannables, 0);
		PVUnionArray rois = expectedExcluderPVStructure.getSubField(PVUnionArray.class, "rois");

		PVStructure expectedROIPVStructure1 = PVDataFactory.getPVDataCreate().createPVStructure(expectedCircularRoiStructure);
		PVDoubleArray cr1CentreVal = expectedROIPVStructure1.getSubField(PVDoubleArray.class, "centre");
		double[] cr1Centre = new double[] {0, 0};
		cr1CentreVal.put(0, cr1Centre.length, cr1Centre, 0);
		PVDouble radius1Val = expectedROIPVStructure1.getSubField(PVDouble.class, "radius");
		radius1Val.put(2);

		PVStructure expectedROIPVStructure2 = PVDataFactory.getPVDataCreate().createPVStructure(expectedCircularRoiStructure);
		PVDoubleArray cr2CentreVal = expectedROIPVStructure2.getSubField(PVDoubleArray.class, "centre");
		double[] cr2Centre = new double[] {-1, -2};
		cr2CentreVal.put(0, cr2Centre.length, cr2Centre, 0);
		PVDouble radius2Val = expectedROIPVStructure2.getSubField(PVDouble.class, "radius");
		radius2Val.put(4);

		PVUnion[] roiArray = new PVUnion[2];
		roiArray[0] = PVDataFactory.getPVDataCreate().createPVUnion(union);
		roiArray[0].set(expectedROIPVStructure1);
		roiArray[1] = PVDataFactory.getPVDataCreate().createPVUnion(union);
		roiArray[1].set(expectedROIPVStructure2);
		rois.put(0, roiArray.length, roiArray, 0);

		PVUnion[] crUnionArray = new PVUnion[1];
		crUnionArray[0] = PVDataFactory.getPVDataCreate().createPVUnion(union);
		crUnionArray[0].set(expectedExcluderPVStructure);

		expectedConfigurePVStructure.getUnionArrayField("generator.excluders").put(0, crUnionArray.length, crUnionArray, 0);
		expectedConfigurePVStructure.getSubField(PVDouble.class, "generator.duration").put(23.1);
		PVStringArray axesToMoveArray = expectedConfigurePVStructure.getSubField(PVStringArray.class, "axesToMove");
		String[] axesToMove = new String[] { "stage_x", "stage_y" };
		axesToMoveArray.put(0, axesToMove.length, axesToMove, 0);

		PVString fileDirVal = expectedConfigurePVStructure.getSubField(PVString.class, "fileDir");
		fileDirVal.put("/path/to/ixx-1234");
		PVString fileTemplateVal = expectedConfigurePVStructure.getSubField(PVString.class, "fileTemplate");
		fileTemplateVal.put("ixx-1234-%s.h5");

		PVStructure actualConfigureStructure = rpcCalls.get("configure");
		assertEquals(expectedConfigureStructure, actualConfigureStructure.getStructure());
		assertEquals(expectedConfigurePVStructure, actualConfigureStructure);


		// Check the 'run' call is as expected
		Structure expectedRunStructure = FieldFactory.getFieldCreate().createFieldBuilder().createStructure();
		PVStructure expectedRunPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedRunStructure);

		PVStructure actualRunStructure = rpcCalls.get("run");
		assertEquals(expectedRunStructure, actualRunStructure.getStructure());
		assertEquals(expectedRunPVStructure, actualRunStructure);


		// Check the 'seek' call is as expected
		Structure expectedSeekStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.add("completedSteps", ScalarType.pvInt).createStructure();
		PVStructure expectedSeekPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedSeekStructure);
		expectedSeekPVStructure.getIntField("completedSteps").put(4);

		PVStructure actualSeekStructure = rpcCalls.get("pause");
		assertEquals(expectedSeekStructure, actualSeekStructure.getStructure());
		assertEquals(expectedSeekPVStructure, actualSeekStructure);
	}

	private void configureMalcolmDevice() throws GeneratorException, ScanningException {
		// Setup the model and other configuration items
		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 0, 0));
		regions.add(new CircularROI(4, -1, -2));

		IPointGenerator<SpiralModel> spiralGen = pointGenService.createGenerator(
				new SpiralModel("stage_x", "stage_y", 1, new BoundingBox(0, -5, 8, 3)), regions);
		IPointGenerator<?> pointGen = pointGenService.createCompoundGenerator(spiralGen);

		MalcolmModel malcolmModel = new MalcolmModel();
		malcolmModel.setExposureTime(23.1);

		// Set the generator on the device
		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.setPointGenerator(pointGen);
		// Set the file directory on the device
		malcolmDevice.setFileDir("/path/to/ixx-1234");

		// Call configure
		malcolmDevice.configure(malcolmModel);
	}

}
