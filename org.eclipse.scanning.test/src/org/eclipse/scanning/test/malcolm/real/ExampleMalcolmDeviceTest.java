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

import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_FILENAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_PATH;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_RANK;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_TYPE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DATASETS_TABLE_COLUMN_UNIQUEID;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_EXPOSURE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_MRI;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.DETECTORS_TABLE_COLUMN_NAME;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_AXES_TO_MOVE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_DETECTORS;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_DIR;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_FILE_TEMPLATE;
import static org.eclipse.scanning.api.malcolm.MalcolmConstants.FIELD_NAME_GENERATOR;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.CONFIGURE;
import static org.eclipse.scanning.api.malcolm.connector.MalcolmMethod.PAUSE;
import static org.eclipse.scanning.connector.epics.EpicsConnectionConstants.TYPE_ID_TABLE;
import static org.eclipse.scanning.malcolm.core.MalcolmDevice.ATTRIBUTE_NAME_COMPLETED_STEPS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.device.models.MalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.MalcolmModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmConstants;
import org.eclipse.scanning.api.malcolm.MalcolmTable;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.malcolm.connector.MalcolmMethod;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.connector.epics.MalcolmEpicsV4Connection;
import org.eclipse.scanning.example.file.MockFilePathService;
import org.eclipse.scanning.example.malcolm.IEPICSv4Device;
import org.eclipse.scanning.malcolm.core.MalcolmDevice;
import org.eclipse.scanning.malcolm.core.Services;
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

/**
 * A test for malcolm device that uses the real PV deserialization with <code>DummyMalcolmRecord</code> set up as
 * a mock to return the expected results.
 */
public class ExampleMalcolmDeviceTest {

	private IScanService service;
	private IEPICSv4Device epicsv4Device;
	private MalcolmEpicsV4Connection connectorService;
	private IMalcolmDevice malcolmDevice;
	private IPointGeneratorService pointGenService;

	@Before
	public void setUp() throws Exception {
		// The real service, get it from OSGi outside this test!
		// Not required in OSGi mode (do not add this to your real code GET THE SERVICE FROM OSGi!)
		this.service = new RunnableDeviceServiceImpl();
		this.connectorService = new MalcolmEpicsV4Connection();
		new Services().setFilePathService(new MockFilePathService());

		// Start the dummy test device
		DeviceRunner runner = new DeviceRunner();
		epicsv4Device = runner.start();

		// Create the device
		malcolmDevice = new MalcolmDevice(epicsv4Device.getRecordName(), connectorService, service);
		pointGenService = new PointGeneratorService();
	}

	@After
	public void tearDown() throws Exception {
		malcolmDevice.dispose();
		// Stop the device
		epicsv4Device.stop();
	}

	@Test
	public void testGetMalcolmVersion() throws Exception {
		final MalcolmVersion version = malcolmDevice.getVersion();
		assertThat(version, is(equalTo(MalcolmVersion.VERSION_4_2)));
	}

	/**
	 * Starts an instance of the ExampleMalcolmDevice and then probes it with the configure() and call() methods
	 * as well as getting a list of all attributes and several specific attributes.
	 * This method thoroughly checks the malcolm attributes and the PV structures created by the EpicsV4
	 * connector sevice.
	 * TODO: split the @Test method for this class into one test method for each method of IMalcolmDevice, see JIRA DAQ-2548.
	 * @throws Exception
	 */
	@Test
	public void testMalcolmDevice() throws Exception {
		configureMalcolmDevice();

		// Test that malcolm attributes have the expected value
		assertThat(malcolmDevice.getDeviceState(), is(DeviceState.ARMED));
		assertThat(malcolmDevice.getDeviceHealth(), is(equalTo("Test Health")));
		assertThat(malcolmDevice.isDeviceBusy(), is(false));

		// Test the 'datasets' attribute
		MalcolmTable malcolmTable = malcolmDevice.getDatasets();

		assertThat(malcolmTable.getHeadings(), contains(
				DATASETS_TABLE_COLUMN_NAME, DATASETS_TABLE_COLUMN_FILENAME, DATASETS_TABLE_COLUMN_TYPE,
				DATASETS_TABLE_COLUMN_RANK, DATASETS_TABLE_COLUMN_PATH, DATASETS_TABLE_COLUMN_UNIQUEID));
		assertThat(malcolmTable.getColumn(DATASETS_TABLE_COLUMN_NAME),
				contains("DET1.data", "DET1.sum", "DET2.data", "DET2.sum",
						"stagey.value_set", "stagex.value_set", "stagey.value", "stagex.value"));

		// Call run
		malcolmDevice.run(null);

		// Check seek method works
		malcolmDevice.seek(4);

		// Check the RPC calls were received correctly by the device
		Map<String, PVStructure> rpcCalls = epicsv4Device.getReceivedRPCCalls();
		assertThat(rpcCalls.keySet(), hasSize(6)); // validate, configure (which calls abort, reset and configure), run and seek (which calls pause)

		// Check the 'configure' call is as expected
		// first create the expected structure (it's quite large)
		Union union = FieldFactory.getFieldCreate().createVariantUnion();
		Structure expectedGeneratorStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray("mutators", union)
				.add("duration", ScalarType.pvDouble)
				.add("delay_after", ScalarType.pvDouble)
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

		Structure expectedCircularRoiStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray("centre", ScalarType.pvDouble)
				.add("radius", ScalarType.pvDouble)
				.setId("scanpointgenerator:roi/CircularROI:1.0")
				.createStructure();

		Structure expectedExcluderStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray("axes", ScalarType.pvString)
				.addArray("rois", union)
				.setId("scanpointgenerator:excluder/ROIExcluder:1.0")
				.createStructure();

		Structure expectedDetectorsStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.addArray(MalcolmConstants.DETECTORS_TABLE_COLUMN_ENABLE, ScalarType.pvBoolean)
				.addArray(DETECTORS_TABLE_COLUMN_NAME, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_MRI, ScalarType.pvString)
				.addArray(DETECTORS_TABLE_COLUMN_EXPOSURE, ScalarType.pvDouble)
				.addArray(DETECTORS_TABLE_COLUMN_FRAMES_PER_STEP, ScalarType.pvInt)
				.setId(TYPE_ID_TABLE)
				.createStructure();

		Structure expectedConfigureValidateStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.add(FIELD_NAME_GENERATOR, expectedGeneratorStructure)
				.addArray(FIELD_NAME_AXES_TO_MOVE, ScalarType.pvString)
				.add(FIELD_NAME_FILE_DIR, ScalarType.pvString)
				.add(FIELD_NAME_FILE_TEMPLATE, ScalarType.pvString)
				.add(FIELD_NAME_DETECTORS, expectedDetectorsStructure)
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


		PVStructure expectedConfigurePVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedConfigureValidateStructure);
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
		expectedConfigurePVStructure.getSubField(PVDouble.class, "generator.delay_after").put(0);
		PVStringArray axesToMoveArray = expectedConfigurePVStructure.getSubField(PVStringArray.class, "axesToMove");
		String[] axesToMove = new String[] { "stage_x", "stage_y" };
		axesToMoveArray.put(0, axesToMove.length, axesToMove, 0);

		PVString fileDirVal = expectedConfigurePVStructure.getSubField(PVString.class, "fileDir");
		fileDirVal.put("/path/to/ixx-1234");
		PVString fileTemplateVal = expectedConfigurePVStructure.getSubField(PVString.class, "fileTemplate");
		fileTemplateVal.put("ixx-1234-%s.h5");

		// check that the 'validate' call is as expected
		PVStructure actualValidateStructure = rpcCalls.get(MalcolmMethod.VALIDATE.name().toLowerCase());
		assertThat(actualValidateStructure.getStructure(), is(equalTo(expectedConfigureValidateStructure)));

		// check that the 'configure' call is as expected
		PVStructure actualConfigureStructure = rpcCalls.get(CONFIGURE.name().toLowerCase());
		assertThat(actualConfigureStructure.getStructure(), is(equalTo(expectedConfigureValidateStructure)));

		// Check the 'run' call is as expected
		Structure expectedRunStructure = FieldFactory.getFieldCreate().createFieldBuilder().createStructure();
		PVStructure expectedRunPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedRunStructure);

		PVStructure actualRunStructure = rpcCalls.get("run");

		assertThat(actualRunStructure.getStructure(), is(equalTo(expectedRunStructure)));
		assertThat(actualRunStructure, is(equalTo(expectedRunPVStructure)));

		// Check the 'seek' call is as expected
		Structure expectedSeekStructure = FieldFactory.getFieldCreate().createFieldBuilder()
				.add(ATTRIBUTE_NAME_COMPLETED_STEPS, ScalarType.pvInt).createStructure();
		PVStructure expectedSeekPVStructure = PVDataFactory.getPVDataCreate().createPVStructure(expectedSeekStructure);
		expectedSeekPVStructure.getIntField(ATTRIBUTE_NAME_COMPLETED_STEPS).put(4);

		PVStructure actualSeekStructure = rpcCalls.get(PAUSE.name().toLowerCase());
		assertThat(actualSeekStructure.getStructure(), is(equalTo(expectedSeekStructure)));
		assertThat(actualSeekStructure, is(equalTo(expectedSeekPVStructure)));
	}

	private IMalcolmModel createExpectedMalcolmModel() {
		final IMalcolmModel malcolmModel = new MalcolmModel();
		malcolmModel.setExposureTime(0.1);

		final List<IMalcolmDetectorModel> detectorModels = new ArrayList<>(4);
		detectorModels.add(new MalcolmDetectorModel("DET", 0, 1, true));
		detectorModels.add(new MalcolmDetectorModel("DIFF", 0, 1, true));
		detectorModels.add(new MalcolmDetectorModel("PANDA-01", 0, 1, false));
		detectorModels.add(new MalcolmDetectorModel("PANDA-02", 0, 1, true));
		malcolmModel.setDetectorModels(detectorModels);

		return malcolmModel;
	}

	private void configureMalcolmDevice() throws Exception {
		// create a new model and set it on the malcolm device (will be null before being set)
		IMalcolmModel model = new MalcolmModel();
		model.setExposureTime(0.1);
		malcolmDevice.setModel(model);

		IMalcolmModel expectedModel = createExpectedMalcolmModel();

		// get the model, this populates the malcolm detector models by communicating over the epics connection
		// with the real malcolm device (or the DummyMalcolmRecord in the case of this test)
		IMalcolmModel actualModel = malcolmDevice.getModel();

		assertThat(actualModel, is(equalTo(expectedModel)));

		// Create and set a point generator
		List<IROI> regions = new LinkedList<>();
		regions.add(new CircularROI(2, 0, 0));
		regions.add(new CircularROI(4, -1, -2));

		TwoAxisSpiralModel spiral = new TwoAxisSpiralModel("stage_x", "stage_y", 1, new BoundingBox(0, -5, 8, 3));
		spiral.setContinuous(false);

		IPointGenerator<CompoundModel> pointGen = pointGenService.createGenerator(spiral, regions);

		// Set the generator on the device
		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.setPointGenerator(pointGen);

		// Set the file directory on the device
		malcolmDevice.setOutputDir("/path/to/ixx-1234");

		// create a new expected malcolm model to match the version as modified by DummyMalcolmRecord
		expectedModel = createExpectedMalcolmModel(expectedModel);

		// Call validate on the malcolm device
		actualModel = malcolmDevice.validateWithReturn(model);

		// check that the returned model has been modified as expected
		assertThat(actualModel, is(equalTo(expectedModel)));

		// configure the malcolm device with a scan model (in a scan this is done by due to the @ScanFinally annotation on configureScan)
		final ScanModel scanModel = new ScanModel(pointGen);
		scanModel.setScanPathModel(pointGen.getModel());
		((MalcolmDevice) malcolmDevice).configureScan(scanModel);

		// Call configure
		malcolmDevice.configure(model);

		// check that the point generator has been updated in the ScanModel
		final IPointGenerator<?> modifiedPointGen = scanModel.getPointGenerator();

		// modify the model to match the changes made in DummyMalcolmRecord.RPCServiceAsyncImpl.modifyConfigureValidatePVStructure
		spiral.setScale(1.5);
		spiral.setContinuous(true);
		final IPointGenerator<?> expectedPointGen = pointGenService.createGenerator(spiral, regions);

		// check that the modified point is as expected. Note: we can't directly compare two generators for equality
		assertThat(modifiedPointGen.size(), is(equalTo(expectedPointGen.size())));
		assertThat(modifiedPointGen.getRank(), is(equalTo(expectedPointGen.getRank())));
		assertThat(modifiedPointGen.getShape(), is(equalTo(expectedPointGen.getShape())));
		assertThat(modifiedPointGen.getNames(), is(equalTo(expectedPointGen.getNames())));
		final Iterator<IPosition> newPointGenIter = modifiedPointGen.iterator();
		final Iterator<IPosition> expectedPointGenIter = expectedPointGen.iterator();
		while (expectedPointGenIter.hasNext()) {
			assertThat(newPointGenIter.hasNext(), is(true));
			assertThat(newPointGenIter.next(), is(equalTo(expectedPointGenIter.next())));
		}
		assertThat(newPointGenIter.hasNext(), is(false));
		// TODO, add a check that continuous is true, this is not currently possible
	}

	private IMalcolmModel createExpectedMalcolmModel(IMalcolmModel expectedModel) {
		expectedModel = new MalcolmModel(expectedModel);
		expectedModel.setAxesToMove(Arrays.asList("stage_x", "stage_y"));
		expectedModel.setExposureTime(0.1);
		expectedModel.getDetectorModels().get(0).setExposureTime(0.1);
		expectedModel.getDetectorModels().get(0).setFramesPerStep(1);
		expectedModel.getDetectorModels().get(1).setEnabled(false);
		expectedModel.getDetectorModels().get(1).setExposureTime(0.025);
		expectedModel.getDetectorModels().get(1).setFramesPerStep(4);
		expectedModel.getDetectorModels().get(2).setEnabled(true);
		return expectedModel;
	}

}
