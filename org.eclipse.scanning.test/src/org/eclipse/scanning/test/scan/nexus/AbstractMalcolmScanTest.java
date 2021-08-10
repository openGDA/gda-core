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
package org.eclipse.scanning.test.scan.nexus;

import static java.util.stream.Collectors.toList;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractMalcolmScanTest extends NexusTest {

	protected String malcolmOutputDir;

	protected DummyMalcolmDevice malcolmDevice;

	protected MockScanParticpiant participant;

	@Before
	public void before() throws Exception {
		// create a temp directory for the dummy malcolm device to write hdf files into
		malcolmOutputDir = ServiceTestHelper.getFilePathService().createFolderForLinkedFiles(output.getName());
		final DummyMalcolmModel model = createMalcolmModel();

		malcolmDevice = new DummyMalcolmDevice();
		malcolmDevice.setAvailableAxes(model.getAxesToMove()); // set the available axes to those of the model
		malcolmDevice.configure(model);
		malcolmDevice.setName(model.getName());
		malcolmDevice.register();

		malcolmDevice.setOutputDir(malcolmOutputDir);
		assertNotNull(malcolmDevice);
		((AbstractMalcolmDevice) malcolmDevice).addRunListener(new IRunListener() {
			@Override
			public void runPerformed(RunEvent evt) throws ScanningException {
				System.out.println("Ran test malcolm device @ " + evt.getPosition());
			}
		});
		participant = new MockScanParticpiant();
		scanService.addScanParticipant(participant);
	}

	@After
	public void teardown() throws Exception {
		// delete the temp directory and all its files
		for (File file : new File(malcolmOutputDir).listFiles()) {
			file.delete();
		}
		new File(malcolmOutputDir).delete();
		participant.clear();
	}

	protected abstract DummyMalcolmModel createMalcolmModel();

	// Create model, but do not set axes etc.
	protected DummyMalcolmModel createMalcolmModelTwoDetectors() {
		DummyMalcolmModel model = new DummyMalcolmModel();
		model.setTimeout(10 * 60); // increased timeout for debugging purposes
		model.setExposureTime(0.1);

		DummyMalcolmDetectorModel det1Model = new DummyMalcolmDetectorModel();
		det1Model.setName("detector");
		det1Model.setFramesPerStep(1);
		det1Model.setExposureTime(0.08);

		DummyMalcolmDatasetModel detector1dataset1 = new DummyMalcolmDatasetModel();
		detector1dataset1.setName("detector");
		detector1dataset1.setRank(2);
		detector1dataset1.setDtype(Double.class);

		DummyMalcolmDatasetModel detector1dataset2 = new DummyMalcolmDatasetModel();
		detector1dataset2.setName("sum");
		detector1dataset2.setRank(1);
		detector1dataset2.setDtype(Double.class);
		det1Model.setDatasets(Arrays.asList(detector1dataset1, detector1dataset2));

		DummyMalcolmDetectorModel det2Model = new DummyMalcolmDetectorModel();
		det2Model.setName("detector2");
		det1Model.setFramesPerStep(2);
		det1Model.setExposureTime(0.4);

		DummyMalcolmDatasetModel detector2dataset = new DummyMalcolmDatasetModel();
		detector2dataset.setName("detector2");
		detector2dataset.setRank(2);
		detector2dataset.setDtype(Double.class);
		det2Model.setDatasets(Arrays.asList(detector2dataset));

		model.setDetectorModels(Arrays.asList(det1Model, det2Model));

		return model;
	}
	protected void checkSize(IRunnableDevice<ScanModel> scanner, int[] shape) {
		ScanBean bean =  ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getBean();
		int expectedSize = Arrays.stream(shape).reduce(1, (x, y) -> x * y);
		assertEquals(expectedSize, bean.getSize());
	}

	private Map<String, List<String>> getExpectedPrimaryDataFieldsPerDetector() {
		Map<String, List<String>> primaryDataFieldsPerDetector = new HashMap<>();
		DummyMalcolmModel model = malcolmDevice.getModel();
		for (IMalcolmDetectorModel detectorModel : model.getDetectorModels()) {
			List<String> list = ((DummyMalcolmDetectorModel) detectorModel).getDatasets().stream().map(d -> d.getName())
				.collect(Collectors.toCollection(ArrayList::new));
			list.set(0, NXdata.NX_DATA); // the first dataset is the primary one, so the field is called 'data' in the nexus tree
			primaryDataFieldsPerDetector.put(detectorModel.getName(), list);
		}

		return primaryDataFieldsPerDetector;
	}

	private List<String> getExpectedUniqueKeysPath(DummyMalcolmModel dummyMalcolmModel) {
		List<String> expectedUniqueKeyPaths = dummyMalcolmModel.getDetectorModels().stream()
			.map(IMalcolmDetectorModel::getName)
			.collect(Collectors.toCollection(ArrayList::new));
		if (!dummyMalcolmModel.getPositionerNames().isEmpty()) {
			expectedUniqueKeyPaths.add("panda");
		}

		return expectedUniqueKeyPaths;
	}

	@Override
	protected NXroot checkNexusFile(IRunnableDevice<ScanModel> scanner, boolean snake, int... sizes) throws Exception {
		final DummyMalcolmModel dummyMalcolmModel = malcolmDevice.getModel();
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		final List<String> expectedUniqueKeysPath = getExpectedUniqueKeysPath(dummyMalcolmModel);
		assertDiamondScanGroup(entry, true, snake, false, expectedUniqueKeysPath, sizes);

		// map from detector name -> primary data fields
		final Map<String, List<String>> primaryDataFieldNamesPerDetector = getExpectedPrimaryDataFieldsPerDetector();
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertEquals(primaryDataFieldNamesPerDetector.values().stream().flatMap(Collection::stream).count(),
				nxDataGroups.size());

		for (IMalcolmDetectorModel detectorModel : dummyMalcolmModel.getDetectorModels()) {
			final String detectorName = detectorModel.getName();
			final NXdetector detector = instrument.getDetector(detectorName);
			assertNotNull(detector);

			final List<String> primaryDataFieldNames = primaryDataFieldNamesPerDetector.get(detectorName);
			checkDetector(detector, dummyMalcolmModel, detectorModel, scanModel, entry,
					primaryDataFieldNames, nxDataGroups, sizes);
		}

		return rootNode;
	}

	protected void checkDetector(NXdetector detector, DummyMalcolmModel dummyMalcolmModel,
			IMalcolmDetectorModel detectorModel, ScanModel scanModel, NXentry entry,
			List<String> primaryDataFieldNames, Map<String, NXdata> nxDataGroups, int[] sizes)
			throws DatasetException {
		assertEquals(detectorModel.getExposureTime(), detector.getCount_timeScalar().doubleValue(), 1e-15);

		final String detectorName = detectorModel.getName();
		final Map<String, String> expectedDataGroupNames = primaryDataFieldNames.stream().collect(Collectors.toMap(
				Function.identity(),
				fieldName -> detectorName + (fieldName.equals(NXdetector.NX_DATA) ? "" : "_" + fieldName)));

		assertTrue(nxDataGroups.keySet().containsAll(expectedDataGroupNames.values()));

		boolean isFirst = true;
		for (DummyMalcolmDatasetModel datasetModel : ((DummyMalcolmDetectorModel) detectorModel).getDatasets()) {
			final String fieldName = datasetModel.getName();
			final String nxDataGroupName = isFirst ? detectorName : detectorName + "_" + fieldName;
			final NXdata nxData = entry.getData(nxDataGroupName);

			final String sourceFieldName = fieldName.equals(detectorName) ? NXdetector.NX_DATA :
				fieldName.substring(fieldName.indexOf('_') + 1);

			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = detector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();
			// test the data nodes for equality instead of identity as they both come from external links
			assertDataNodesEqual("/entry/instrument/"+detectorName+"/"+sourceFieldName,
					dataNode, nxData.getDataNode(sourceFieldName));
//				assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
//						+ "/" + sourceFieldName);

			// check that other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : primaryDataFieldNames) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertNull(nxData.getDataNode(primaryDataFieldName));
				}
			}

			int[] shape = dataset.getShape();
			for (int i = 0; i < sizes.length; i++)
				assertEquals(sizes[i], shape[i]);

			// Make sure none of the numbers are NaNs. The detector is expected
			// to fill this scan with non-nulls
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertFalse(Double.isNaN(dataset.getDouble(next)));
			}

			// Check axes
			final IPosition pos = scanModel.getPointGenerator().iterator().next();
			final List<String> axisNames = pos.getNames();

			// Append _value_set to each name in list, then add detector axes fields to result
			// stage_y doesn't have _value_set as to mimic the real i18 malcolm device
			// malcolm doesn't know this value (as instead it has three jacks j1,j2,j3 for the y position)
			int additionalRank = datasetModel.getRank(); // i.e. rank per position, e.g. 2 for images
			final List<String> expectedAxesNames = Stream.concat(
					axisNames.stream().map(axisName -> axisName +
							(!axisName.equals("stage_y") ? "_value_set" : "")),
					Collections.nCopies(additionalRank, ".").stream()).collect(toList());
			if (sizes.length == 0) {
				// prepend "." for scans of rank 0. A scan with a single StaticModel of size 1
				// produces datasets of rank 1 and shape { 1 } due to a limitation of area detector
				expectedAxesNames.add(0, ".");
			}
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
			for (int axisIndex = 0; axisIndex < axisNames.size(); axisIndex++) {
				final String axisName = axisNames.get(axisIndex);
				final NXpositioner positioner = entry.getInstrument().getPositioner(axisName);
				assertNotNull(positioner);

				checkPositioner(positioner, dummyMalcolmModel, nxData, defaultDimensionMappings,
						axisIndex, axisName, sizes);
			}
			isFirst = false;
		}
	}

	private void checkPositioner(final NXpositioner positioner, DummyMalcolmModel dummyMalcolmModel,
			final NXdata nxData, int[] defaultDimensionMappings, int axisIndex, String axisName, int[] sizes)
			throws DatasetException {
		DataNode dataNode = positioner.getDataNode("value_set");
		IDataset dataset = dataNode.getDataset().getSlice();
		int[] shape = dataset.getShape();
		assertEquals(1, shape.length);
		assertEquals(sizes[axisIndex], shape[0]);

		String nxDataFieldName = axisName + (!axisName.equals("stage_y") ? "_value_set" : "");
		assertDataNodesEqual("", dataNode, nxData.getDataNode(nxDataFieldName));
		assertIndices(nxData, nxDataFieldName, axisIndex);
		// The value of the target attribute seems to come from the external file
//					assertTarget(nxData, nxDataFieldName, rootNode,
//							"/entry/" + firstDetectorName + "/" + nxDataFieldName);

		// value field (a.k.a rbv) only created if in list of positioners in model
		if (dummyMalcolmModel.getPositionerNames().contains(axisName)) {
			// Actual values should be scanD
			dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
			assertNotNull(dataNode);
			dataset = dataNode.getDataset().getSlice();
			shape = dataset.getShape();
			assertArrayEquals(sizes, shape);

			nxDataFieldName = axisName + "_" + NXpositioner.NX_VALUE;
//						assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
			assertDataNodesEqual("", dataNode, nxData.getDataNode(nxDataFieldName));
			assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
//					assertTarget(nxData, nxDataFieldName, rootNode,
//							"/entry/instrument/" + axisName + "/" + NXpositioner.NX_VALUE);
		}
	}

	protected IRunnableDevice<ScanModel> createMalcolmGridScan(final IMalcolmDevice malcolmDevice, File file, boolean snake, int... size) throws Exception {

		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel(); // Note stage_x and stage_y scannables controlled by malcolm
		gridModel.setxAxisName("stage_x");
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName("stage_y");
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));
		gridModel.setAlternating(snake);

		final List<IScanPointGeneratorModel> models = new ArrayList<>();
		for (int dim = 0; dim < size.length - 2; dim++) {
			final double step = size[dim] > 1 ? 9.99d / (size[dim] - 1) : 30;
			models.add(new AxialStepModel("neXusScannable"+(dim+1), 10, 20, step));
		}

		models.add(gridModel);
		final CompoundModel compoundModel = new CompoundModel(models);

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setScanPathModel(compoundModel);
		scanModel.setPointGenerator(pointGen);
		scanModel.setDetector(malcolmDevice);
		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.setPointGenerator(pointGen);

		// Create a file to scan into.
		scanModel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// Create a scan and run it without publishing events
		IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(new IRunListener() {
			@Override
			public void runWillPerform(RunEvent evt) throws ScanningException {
				System.out.println("Running acquisition scan of size "+fgen.size());
			}
		});

		return scanner;
	}
}
