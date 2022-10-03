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

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toMap;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertAxes;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDiamondScanGroup;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertIndices;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertNXentryMetadata;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertSignal;
import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertTarget;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertSame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.PositionIterator;
import org.eclipse.scanning.api.device.AbstractRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.scan.IFilePathService;
import org.eclipse.scanning.api.scan.IScanService;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.test.ScanningTestUtils;
import org.eclipse.scanning.test.ServiceTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Attempts to mock out a few services so that we can run them in junit
 * not plugin tests.
 *
 * @author Matthew Gerring
 *
 */
public abstract class NexusTest {

	private static Logger logger = LoggerFactory.getLogger(NexusTest.class);

	protected static final String MANDELBROT_DETECTOR_NAME = "mandelbrot";
	protected static final String X_AXIS_NAME = "xNex";
	protected static final String Y_AXIS_NAME = "yNex";

	protected static IScannableDeviceService connector;
	protected static IScanService            scanService;
	protected static IPointGeneratorService  pointGenService;
	protected static INexusFileFactory       fileFactory;
	protected static IFilePathService        filePathService;


	@BeforeAll
	public static void setServices() throws Exception {
		ServiceTestHelper.setupServices();
		ServiceTestHelper.registerTestDevices();

		scanService = ServiceTestHelper.getScanService();
		pointGenService = ServiceTestHelper.getPointGeneratorService();
		fileFactory = ServiceTestHelper.getNexusFileFactory();
		connector = ServiceTestHelper.getScannableDeviceService();
		filePathService = ServiceTestHelper.getFilePathService();

	    ScanningTestUtils.clearTmp();
	}


	protected File output;

	@BeforeEach
	public void createFile() throws IOException {
		output = Files.createTempFile(Paths.get(filePathService.getVisitDir()), "test_nexus", ".nxs").toFile();
		output.deleteOnExit();
	}

	@AfterEach
	public void deleteFile() {
		try {
			output.delete();
		} catch (Exception ne) {
			logger.trace("Cannot delete file!", ne);
		}
	}

	protected static MandelbrotModel createMandelbrotModel() {
		final MandelbrotModel model = new MandelbrotModel();
		model.setName(MANDELBROT_DETECTOR_NAME);
		model.setRealAxisName(X_AXIS_NAME);
		model.setImaginaryAxisName(Y_AXIS_NAME);
		model.setColumns(64);
		model.setRows(64);
		model.setExposureTime(0.001);
		return model;
	}

	protected int getScanSize(int[] shape) {
		return Arrays.stream(shape).reduce(1, (x, y) -> x * y); // the size is the product of the elements of the shape array
	}

	protected NXroot getNexusRoot(IRunnableDevice<ScanModel> scanner) throws Exception {
		String filePath = ((AbstractRunnableDevice<ScanModel>) scanner).getModel().getFilePath();

		try (NexusFile nf =  fileFactory.newNexusFile(filePath)) {
			nf.openToRead();

			TreeFile nexusTree = NexusUtils.loadNexusTree(nf);
			return (NXroot) nexusTree.getGroupNode();
		}
	}

	protected NXroot checkNexusFile(IRunnableDevice<ScanModel> scanner, boolean snake, int... sizes) throws Exception {
		return checkNexusFile(scanner, snake, false, sizes);
	}

	/**
	 * A folded grid is where a non-rectangular region is applied, meaning that the two grid
     * dimensions are flattened into one. In this case the sizes array passed in should be
     * the expected dataset size.
	 */
	protected NXroot checkNexusFile(IRunnableDevice<ScanModel> scanner, boolean snake,
			boolean foldedGrid, int[] sizes) throws Exception {
		final ScanModel scanModel = ((AbstractRunnableDevice<ScanModel>) scanner).getModel();
		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));

		final NXroot rootNode = getNexusRoot(scanner);
		final NXentry entry = rootNode.getEntry();
		final NXinstrument instrument = entry.getInstrument();

		// check that the scan points have been written correctly
		assertNXentryMetadata(entry);
		assertDiamondScanGroup(entry, snake, foldedGrid, sizes);

		final LinkedHashMap<String, List<String>> signalFieldAxes = new LinkedHashMap<>();
		// axis for additional dimensions of a datafield, e.g. image
		signalFieldAxes.put(NXdetector.NX_DATA, Arrays.asList("real", "imaginary"));
		signalFieldAxes.put("spectrum", Arrays.asList("spectrum_axis"));
		signalFieldAxes.put("value", emptyList());

		final String detectorName = scanModel.getDetectors().get(0).getName();
		final NXdetector nxDetector = instrument.getDetector(detectorName);
		assertThat(nxDetector.getCount_timeScalar().doubleValue(),
				is(closeTo(((IDetectorModel)scanner.getModel().getDetectors().get(0).getModel()).getExposureTime(), 1e-15)));

		// map of detector data field to name of nxData group where that field is the @signal field
		final Map<String, String> expectedDataGroupNames =
				signalFieldAxes.keySet().stream().collect(toMap(Function.identity(),
				x -> detectorName + (x.equals(NXdetector.NX_DATA) ? "" : "_" + x)));

		// validate the main NXdata generated by the NexusDataBuilder
		final Map<String, NXdata> nxDataGroups = entry.getChildren(NXdata.class);
		assertThat(nxDataGroups.size(), is(equalTo(signalFieldAxes.size())));
		assertThat(nxDataGroups.keySet(), containsInAnyOrder(expectedDataGroupNames.values().toArray(String[]::new)));
		for (String nxDataGroupName : nxDataGroups.keySet()) {
			final NXdata nxData = entry.getData(nxDataGroupName);

			final String sourceFieldName = nxDataGroupName.equals(detectorName) ? NXdetector.NX_DATA :
				nxDataGroupName.substring(nxDataGroupName.indexOf('_') + 1);
			assertSignal(nxData, sourceFieldName);
			// check the nxData's signal field is a link to the appropriate source data node of the detector
			DataNode dataNode = nxDetector.getDataNode(sourceFieldName);
			IDataset dataset = dataNode.getDataset().getSlice();
			assertSame(dataNode, nxData.getDataNode(sourceFieldName));
			assertTarget(nxData, sourceFieldName, rootNode, "/entry/instrument/" + detectorName
					+ "/" + sourceFieldName);

			// check that the other primary data fields of the detector haven't been added to this NXdata
			for (String primaryDataFieldName : signalFieldAxes.keySet()) {
				if (!primaryDataFieldName.equals(sourceFieldName)) {
					assertThat(nxData.getDataNode(primaryDataFieldName), is(nullValue()));
				}
			}

			int[] shape = dataset.getShape();
			for (int i = 0; i < sizes.length; i++)
				assertThat(shape[i], is(equalTo(sizes[i])));

			// Make sure none of the numbers are NaNs. The detector
			// is expected to fill this scan with non-nulls.
			final PositionIterator it = new PositionIterator(shape);
			while (it.hasNext()) {
				int[] next = it.getPos();
				assertThat(Double.isNaN(dataset.getDouble(next)), is(false));
			}

			// Check axes
			final IPosition pos = scanModel.getPointGenerator().iterator().next();
			final List<String> scannableNames = pos.getNames();

			// Append _value_demand to each name in list, then add detector axis fields to result
			final List<String> expectedAxesNames = Stream.concat(
					scannableNames.stream().
					filter(scannableName -> !(foldedGrid && scannableName.equals(scannableNames.get(scannableNames.size() - 2)))). // filter out inner grid scannable for folded grids
					map(x -> x + "_value_set"),
					signalFieldAxes.get(sourceFieldName).stream()).collect(Collectors.toList());
			assertAxes(nxData, expectedAxesNames.toArray(new String[expectedAxesNames.size()]));

			int[] defaultDimensionMappings = IntStream.range(0, sizes.length).toArray();
			int i = -1;
			for (String  scannableName : scannableNames) {
				if (!foldedGrid || i != scannableNames.size() - 2) {
					i++; // don't increment if this is the last scannable of a folded grid scan
				}

				final NXpositioner positioner = instrument.getPositioner(scannableName);
				assertThat(positioner, is(notNullValue()));

				dataNode = positioner.getDataNode("value_set");
				dataset = dataNode.getDataset().getSlice();
				assertThat(dataset.getShape(), is(equalTo(new int[] { sizes[i] })));

				String nxDataFieldName = scannableName + "_value_set";
				assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
				assertIndices(nxData, nxDataFieldName, i);
				assertTarget(nxData, nxDataFieldName, rootNode,
						"/entry/instrument/" + scannableName + "/value_set");

				// Actual values should be scanD
				dataNode = positioner.getDataNode(NXpositioner.NX_VALUE);
				dataset = dataNode.getDataset().getSlice();
				assertThat(dataset.getShape(), is(equalTo(sizes)));

				nxDataFieldName = scannableName + "_" + NXpositioner.NX_VALUE;
				assertSame(dataNode, nxData.getDataNode(nxDataFieldName));
				assertIndices(nxData, nxDataFieldName, defaultDimensionMappings);
				assertTarget(nxData, nxDataFieldName, rootNode, "/entry/instrument/" + scannableName + "/" + NXpositioner.NX_VALUE);
			}
		}

		return rootNode;
	}

	protected IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector, File file, boolean snake, int... size) throws Exception {
		final ScanModel smodel = createGridScanModel(detector, file, snake, size);

		// Create a scan and run it without publishing events
		return scanService.createScanDevice(smodel);
	}

	protected IRunnableDevice<ScanModel> createGridScan(final IRunnableDevice<? extends IDetectorModel> detector, File file, IROI region, boolean snake, int... size) throws Exception {
		final ScanModel smodel = createGridScanModel(detector, file, region, snake, size);

		// Create a scan and run it without publishing events
		return scanService.createScanDevice(smodel);
	}

	protected ScanModel createGridScanModel(final IRunnableDevice<? extends IDetectorModel> detector, File file, boolean snake, int... size) throws Exception {
		return createGridScanModel(detector, file, null, snake, size);
	}

	protected ScanModel createGridScanModel(final IRunnableDevice<? extends IDetectorModel> detector, File file, IROI region, boolean snake, int... size) throws Exception {
		// Create scan points for a grid and make a generator
		final TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel();
		gridModel.setxAxisName(X_AXIS_NAME);
		gridModel.setxAxisPoints(size[size.length-1]);
		gridModel.setyAxisName(Y_AXIS_NAME);
		gridModel.setyAxisPoints(size[size.length-2]);
		gridModel.setBoundingBox(new BoundingBox(0,0,3,3));
		gridModel.setAlternating(snake);

		final CompoundModel compoundModel = createNestedStepScans(2, size);
		compoundModel.addData(gridModel, Arrays.asList(region));

		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);

		// Create a file to scan into.
		scanModel.setFilePath(file.getAbsolutePath());
		System.out.println("File writing to "+scanModel.getFilePath());

		return scanModel;
	}

	protected IRunnableDevice<ScanModel> createSpiralScan(final IRunnableDevice<? extends IDetectorModel> detector, File file) throws Exception {
		final ScanModel smodel = createSpiralScanModel(detector, file);
		// Create a scan and run it without publishing events
		return scanService.createScanDevice(smodel);
	}

	protected ScanModel createSpiralScanModel(final IRunnableDevice<? extends IDetectorModel> detector, File file) throws Exception {
		final TwoAxisSpiralModel spiralModel = new TwoAxisSpiralModel("xNex","yNex");
		spiralModel.setScale(2.0);
		spiralModel.setBoundingBox(new BoundingBox(0,0,1,1));
		final AxialStepModel stepModel = new AxialStepModel("neXusScannable1", 0,3,1);

		final CompoundModel compoundModel = new CompoundModel(stepModel, spiralModel);
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setPointGenerator(pointGen);
		scanModel.setScanPathModel(compoundModel);
		scanModel.setDetector(detector);

		// Create a file to scan into.
		scanModel.setFilePath(file.getAbsolutePath());

		return scanModel;
	}

	/*
	 * A utility method for tests extending this test that returns a CompoundModel with size.length - remainingAxes nested AxialStepModels
	 * In a test that calls for a GridScan nested in N axial step models of size f(i) for example, this could be called with (2, [f(0), f(1)...])
	 */
	protected CompoundModel createNestedStepScans(int remainingAxes, int... size) {
		final CompoundModel cModel = new CompoundModel();
		for (int dim = 0; dim < size.length - remainingAxes; dim++) {
			cModel.addModel(new AxialStepModel("neXusScannable"+(dim+1), 10,20,
					size[dim] > 1 ? 9.9d/(size[dim] - 1) : 30)); // Either N many points or 1 point at 10
		}
		return cModel;
	}

}
