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

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.EMPTY_SHAPE;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDataNodesEqual;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
import org.eclipse.scanning.api.device.IScanDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.malcolm.attributes.MalcolmDatasetType;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel.Orientation;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDetectorModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDevice;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.malcolm.core.AbstractMalcolmDevice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import uk.ac.diamond.osgi.services.ServiceProvider;

public abstract class AbstractMalcolmScanTest extends NexusTest {

	protected static final String X_AXIS_NAME = "stage_x";
	protected static final String Y_AXIS_NAME = "stage_y";
	protected static final String[] Y_AXIS_JACK_NAMES = { "j1", "j2", "j3" };
	protected static final String ANGLE_AXIS_NAME = "theta";
	protected static final String MONITOR_NAME = "i0";

	protected String malcolmOutputDir;

	protected DummyMalcolmDevice malcolmDevice;

	protected MockScanParticpiant participant;

	@BeforeEach
	public void before() throws Exception {
		// create a temp directory for the dummy malcolm device to write hdf files into
		malcolmOutputDir = ServiceProvider.getService(IFilePathService.class).createFolderForLinkedFiles(output.getName());
		final DummyMalcolmModel model = createMalcolmModel();

		malcolmDevice = new DummyMalcolmDevice();
		malcolmDevice.setAvailableAxes(model.getAxesToMove()); // set the available axes to those of the model
		malcolmDevice.configure(model);
		malcolmDevice.setName(model.getName());
		malcolmDevice.register();

		malcolmDevice.setOutputDir(malcolmOutputDir);
		assertThat(malcolmDevice, is(notNullValue()));
		((AbstractMalcolmDevice) malcolmDevice).addRunListener(IRunListener.createRunPerformedListener(
				event -> System.out.println("Ran test malcolm device @ " + event.getPosition())));
		participant = new MockScanParticpiant();
		scanService.addScanParticipant(participant);
	}

	@AfterEach
	public void teardown() {
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
		final DummyMalcolmModel model = new DummyMalcolmModel();
		model.setTimeout(10 * 60); // increased timeout for debugging purposes
		model.setExposureTime(0.1);

		final DummyMalcolmDetectorModel det1Model = new DummyMalcolmDetectorModel();
		det1Model.setName("detector");
		det1Model.setFramesPerStep(1);
		det1Model.setExposureTime(0.08);

		final DummyMalcolmDatasetModel detector1dataset1 = new DummyMalcolmDatasetModel();
		detector1dataset1.setName("detector");
		detector1dataset1.setRank(2);
		detector1dataset1.setDtype(Double.class);

		final DummyMalcolmDatasetModel detector1dataset2 = new DummyMalcolmDatasetModel();
		detector1dataset2.setName("sum");
		detector1dataset2.setRank(1);
		detector1dataset2.setDtype(Double.class);
		det1Model.setDatasets(Arrays.asList(detector1dataset1, detector1dataset2));

		final DummyMalcolmDetectorModel det2Model = new DummyMalcolmDetectorModel();
		det2Model.setName("detector2");
		det1Model.setFramesPerStep(2);
		det1Model.setExposureTime(0.4);

		final DummyMalcolmDatasetModel detector2dataset = new DummyMalcolmDatasetModel();
		detector2dataset.setName("detector2");
		detector2dataset.setRank(2);
		detector2dataset.setDtype(Double.class);
		det2Model.setDatasets(Arrays.asList(detector2dataset));

		model.setDetectorModels(Arrays.asList(det1Model, det2Model));

		return model;
	}

	protected void checkSize(IRunnableDevice<ScanModel> scanner, int[] shape) {
		final ScanBean bean =  ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getBean();
		int expectedSize = Arrays.stream(shape).reduce(1, (x, y) -> x * y);
		assertThat(bean.getSize(), is(equalTo(expectedSize)));
	}

	protected Map<String, List<String>> getExpectedPrimaryDataFieldsPerDetector() {
		final Map<String, List<String>> primaryDataFieldsPerDetector = new HashMap<>();
		final DummyMalcolmModel model = malcolmDevice.getModel();
		for (IMalcolmDetectorModel detectorModel : model.getDetectorModels()) {
			final List<String> list = ((DummyMalcolmDetectorModel) detectorModel).getDatasets().stream()
					.map(d -> d.getName())
					.collect(toCollection(ArrayList::new));
			list.set(0, NXdata.NX_DATA); // the first dataset is the primary one, so the field is called 'data' in the nexus tree
			primaryDataFieldsPerDetector.put(detectorModel.getName(), list);
		}

		return primaryDataFieldsPerDetector;
	}

	protected List<String> getExpectedUniqueKeysPath(DummyMalcolmModel dummyMalcolmModel) {
		final List<String> expectedUniqueKeyPaths = dummyMalcolmModel.getDetectorModels().stream()
			.map(IMalcolmDetectorModel::getName)
			.collect(toList());
		if (!dummyMalcolmModel.getPositionerNames().isEmpty()) {
			expectedUniqueKeyPaths.add("panda");
		}

		return expectedUniqueKeyPaths;
	}

	@Override
	protected NXroot checkNexusFile(IRunnableDevice<ScanModel> scanner, boolean snake, int... sizes) throws Exception {
		return checkNexusFile(scanner, snake, false, sizes);
	}

	@Override
	protected NXroot checkNexusFile(IRunnableDevice<ScanModel> scanner, boolean snake, boolean foldedGrid, int... sizes) throws Exception {
		final DummyMalcolmModel dummyMalcolmModel = malcolmDevice.getModel();
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		final List<String> expectedUniqueKeysPath = getExpectedUniqueKeysPath(dummyMalcolmModel);
		assertDiamondScanGroup(entry, true, snake, foldedGrid, expectedUniqueKeysPath, sizes);
		assertSolsticeScanMetadata(entry, scanner.getModel().getScanPathModel());

		// map from detector name -> primary data fields
		final Map<String, List<String>> primaryDataFieldNamesPerDetector = getExpectedPrimaryDataFieldsPerDetector();
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertThat(nxDataGroups.size(), is(equalTo(
				(int) primaryDataFieldNamesPerDetector.values().stream().flatMap(Collection::stream).count())));

		for (IMalcolmDetectorModel detectorModel : dummyMalcolmModel.getDetectorModels()) {
			final String detectorName = detectorModel.getName();
			final NXdetector detector = instrument.getDetector(detectorName);
			assertThat(detector, is(notNullValue()));

			final List<String> primaryDataFieldNames = primaryDataFieldNamesPerDetector.get(detectorName);
			checkDetector(detector, dummyMalcolmModel, detectorModel, scanModel, foldedGrid,
					entry, primaryDataFieldNames, nxDataGroups, sizes);
		}

		return rootNode;
	}

	protected void checkDetector(NXdetector detector, DummyMalcolmModel dummyMalcolmModel,
			IMalcolmDetectorModel detectorModel, ScanModel scanModel, boolean foldedGrid,
			NXentry entry, List<String> primaryDataFieldNames, Map<String, NXdata> nxDataGroups, int[] sizes)
			throws Exception {
		assertThat(detector.getCount_timeScalar().doubleValue(), is(closeTo(detectorModel.getExposureTime(), 1e-15)));

		final String detectorName = detectorModel.getName();
		final Map<String, String> expectedDataGroupNames = primaryDataFieldNames.stream()
				.collect(toMap(Function.identity(),
				fieldName -> detectorName + (fieldName.equals(NXdetector.NX_DATA) ? "" : "_" + fieldName)));

		assertThat(nxDataGroups.keySet().containsAll(expectedDataGroupNames.values()), is(true));

		boolean isFirst = true;
		for (DummyMalcolmDatasetModel datasetModel : ((DummyMalcolmDetectorModel) detectorModel).getDatasets()) {
			final String fieldName = datasetModel.getName();
			final String nxDataGroupName = isFirst ? detectorName : detectorName + "_" + fieldName;
			final NXdata nxData = entry.getData(nxDataGroupName);

			final String sourceFieldName = fieldName.equals(detectorName) ? NXdetector.NX_DATA :
				fieldName.substring(fieldName.indexOf('_') + 1);

			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			final DataNode dataNode = detector.getDataNode(sourceFieldName);
			final IDataset dataset = dataNode.getDataset().getSlice();
			// test the data nodes for equality instead of identity as they both come from external links
			assertDataNodesEqual("/entry/instrument/"+detectorName+"/"+sourceFieldName,
					dataNode, nxData.getDataNode(sourceFieldName));
//				assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
//						+ "/" + sourceFieldName);

			// check that other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : primaryDataFieldNames) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertThat(nxData.getDataNode(primaryDataFieldName), is(nullValue()));
				}
			}

			int[] shape = dataset.getShape();

			for (int i = 0; i < sizes.length; i++) {
				assertThat(shape[i], is(equalTo(sizes[i])));
			}

			// Make sure none of the numbers are NaNs. The detector is expected
			// to fill this scan with non-nulls
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] value = it.getPos();
				assertThat(Double.isNaN(dataset.getDouble(value)), is(false));
			}

			// Check axes
			final IPosition pos = scanModel.getPointGenerator().iterator().next();
			assertThat(pos.getScanRank(), is(sizes.length == 0 ? 1 : sizes.length)); // sanity check
			final List<List<String>> dimensionNames = pos.getDimensionNames();
			final int[] expectedNumAxesPerDimension =  sizes.length == 0 ? EMPTY_SHAPE :
				IntStream.range(0, pos.getScanRank()).map(i -> foldedGrid && i == pos.getScanRank() - 1 ? 2 : 1).toArray();
			final int[] numAxesPerDimension = dimensionNames.stream().mapToInt(names -> names.size()).toArray();
			final int numMalcolmControlledDims = (int) dimensionNames.stream().filter(axesForDim -> getMalcolmAxes().containsAll(axesForDim)).count();
			assertThat(numAxesPerDimension, is(equalTo(expectedNumAxesPerDimension)));

			// Append _value_set to each name in list, then add detector axes fields to result
			final int additionalRank = datasetModel.getRank(); // i.e. rank per position, e.g. 2 for images
			final List<String> expectedAxesNames = getExpectedAxisNames(dimensionNames, additionalRank);

			if (sizes.length == 0) {
				// prepend "." for scans of rank 0. A scan with a single StaticModel of size 1
				// produces datasets of rank 1 and shape { 1 } due to a limitation of area detector
				expectedAxesNames.add(0, ".");
			}
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			for (int dimensionIndex = 0; dimensionIndex < dimensionNames.size(); dimensionIndex++) {
				for (String axisName : dimensionNames.get(dimensionIndex)) {
					final NXpositioner positioner = entry.getInstrument().getPositioner(axisName);
					assertThat(positioner, is(notNullValue()));
					checkPositioner(positioner, dummyMalcolmModel, nxData, axisName, dimensionIndex, numMalcolmControlledDims, sizes);
				}
			}
			isFirst = false;
		}
	}

	private List<String> getExpectedAxisNames(final List<List<String>> dimensionNames, final int additionalRank) {
		// always use the first axis name for each dataset dimension (the slow axis for flattened grid scans)
		final List<String> axisNames = dimensionNames.stream().map(axesNames -> axesNames.get(0)).toList();
		return Stream.concat(axisNames.stream().map(axisName -> getActualAxisName(axisName)),
			Collections.nCopies(additionalRank, ".").stream()).collect(toList());
	}

	private String getActualAxisName(String axisName) {
		// most axes have both a set value and a read-back value dataset. In this case, we want to use
		// the set value dataset, which has the suffix '_value_set'.
		// The exception is the stage_y axes, where for most tests in order to simulate the real malcolm device
		// on i18, there is no read-back value dataset, (instead it has datasets j1, j2 and j3, simulating the
		// jacks that are used). To detect this generically, we look for malcolm controlled axes for which
		// the dummy malcolm model does not have as a positioner name.
		return axisName + ((!isMalcolmAxis(axisName) ||
				malcolmDevice.getModel().getPositionerNames().contains(axisName)) ? "_value_set" : "");
	}

	private void checkPositioner(NXpositioner positioner, DummyMalcolmModel dummyMalcolmModel,
			NXdata dataGroup, String axisName, int axisIndex, int numMalcolmControlledDims, int[] sizes) throws Exception{
		// value field is not created for malcolm controlled axis for which there is no positioner
		// (this is stage_y, since its actual motors are j1, j2 and j3 (jacks)).
		final boolean hasValueField = !isMalcolmAxis(axisName) || dummyMalcolmModel.getPositionerNames().contains(axisName);

		if (hasValueField) {
			checkDataset(positioner, dataGroup, axisName, axisIndex, numMalcolmControlledDims, sizes, true, MalcolmDatasetType.POSITION_VALUE);
		}
		checkDataset(positioner, dataGroup, axisName, axisIndex, numMalcolmControlledDims, sizes, hasValueField, MalcolmDatasetType.POSITION_SET);
	}

	protected List<String> getMalcolmAxes() {
		try {
			return malcolmDevice.getModel().getAxesToMove() != null ?
				malcolmDevice.getModel().getAxesToMove() : malcolmDevice.getAvailableAxes();
		} catch (ScanningException e) { // throw RuntimeException so we can use this method with streams
			throw new RuntimeException("Could not get malcolm axes", e);
		}
	}

	protected boolean isMalcolmAxis(String axisName) {
			return getMalcolmAxes().contains(axisName);
	}

	private void checkDataset(NXpositioner positioner, NXdata dataGroup, String axisName, int axisIndex,
			int numMalcolmControlledDims, int[] sizes, boolean hasValueField, MalcolmDatasetType type) throws DatasetException {
		// check the value_set field
		final List<String> malcolmAxes = malcolmDevice.getModel().getAxesToMove();
		final boolean isMalcolmAxis = malcolmAxes.contains(axisName);
		final String dataNodeName = NXpositioner.NX_VALUE + (type == MalcolmDatasetType.POSITION_SET ? "_set" : "");
		final DataNode dataNode = positioner.getDataNode(dataNodeName);
		assertThat(dataNode, is(notNullValue()));
		final IDataset dataset = dataNode.getDataset().getSlice();

		final int expectedRank;
		final int[] expectedShape;
		final int[] expectedIndices;
		switch (type) {
			case POSITION_VALUE:
				expectedRank = isMalcolmAxis ? sizes.length : sizes.length - numMalcolmControlledDims;
				expectedShape = isMalcolmAxis ? sizes : Arrays.copyOfRange(sizes, 0, expectedRank);
				expectedIndices = IntStream.range(0, expectedShape.length).toArray();
				break;
			case POSITION_SET:
				expectedRank = 1;
				expectedShape = new int[] { sizes[axisIndex] };
				expectedIndices = new int[] { axisIndex };
				break;
			default:
				throw new IllegalArgumentException("Invalid dataset type: " + type);
		}

		assertThat(dataset.getRank(), is(expectedRank));
		assertThat(dataset.getShape(), is(equalTo(expectedShape)));

		final String dataGroupFieldName = type == MalcolmDatasetType.POSITION_SET && !hasValueField ? axisName : axisName + "_" + dataNodeName;
		if (isMalcolmAxis) {
			assertDataNodesEqual("", dataNode, dataGroup.getDataNode(dataGroupFieldName));
		} else {
			assertThat(dataGroup.getDataNode(dataGroupFieldName), is(sameInstance(dataNode)));
		}
		assertIndices(dataGroup, dataGroupFieldName, expectedIndices);
	}

	protected IScanDevice createMalcolmGridScan(final DummyMalcolmDevice malcolmDevice, File file, boolean snake, int... size) throws Exception {
		return createMalcolmGridScan(malcolmDevice, file, X_AXIS_NAME, Y_AXIS_NAME, snake, false, Orientation.HORIZONTAL, size);
	}

	protected IScanDevice createMalcolmGridScan(final DummyMalcolmDevice malcolmDevice, File file,
			String xAxisName, String yAxisName, boolean snake, boolean flattenGrid, Orientation orientation, int... size) throws Exception {
		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel(); // Note stage_x and stage_y scannables controlled by malcolm
		gridModel.setxAxisName(xAxisName);
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName(yAxisName);
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));
		gridModel.setAlternating(snake);
		gridModel.setOrientation(orientation);

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

		// Create a file to scan into.
		scanModel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to " + scanModel.getFilePath());

		// we need to configure the malcolm device with the scan before creating the scan device,
		// so that if the grid is flattened, the point generator in the ScanModel will be replaced
		malcolmDevice.setFlattenGridScan(flattenGrid);
		malcolmDevice.configureScan(scanModel);
		malcolmDevice.configure(malcolmDevice.getModel()); // where malcolm flattens the point generator

		// Create a scan and run it without publishing events
		final IScanDevice scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = scanModel.getPointGenerator();
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}
}
