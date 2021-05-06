/*-
 * Copyright © 2020 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.eclipse.scanning.test.scan.nexus;

import static org.eclipse.dawnsci.nexus.test.utilities.NexusAssert.assertDatasetsEqual;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel.ImageType;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.Test;

public class MalcolmMultiScanTest extends AbstractMalcolmScanTest {

	private static final String FIELD_NAME_IMAGE_KEY = "image_key";

	private static final int NUM_FLATS = 5;
	private static final int NUM_DARKS = 3;
	private static final int NUM_MAIN_SCAN_POINTS = 19;

	private boolean withDarkAndFlat = true;

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setExposureTime(0.001);
		model.setAxesToMove(Arrays.asList("theta"));
		model.setPositionerNames(Arrays.asList("theta"));

		return model;
	}

	@Test
	public void testMalcolmMultiScan() throws Exception {
		testMalcolmMultiScanTemplate(true, NUM_MAIN_SCAN_POINTS + NUM_FLATS * 2 + NUM_DARKS * 2);
	}

	@Test
	public void testMalcolmMultiScanWithoutDarAndFlat() throws Exception {
		testMalcolmMultiScanTemplate(false, NUM_MAIN_SCAN_POINTS);
	}

	private void testMalcolmMultiScanTemplate(boolean withDarkFlat, int expectedScanSize) throws Exception {
		this.withDarkAndFlat = withDarkFlat;
		final CompoundModel compoundModel = createCompoundModel();

		final IRunnableDevice<ScanModel> scanner = createMalcolmMultiScan(compoundModel);
		final List<IPosition> positionsMovedTo = new ArrayList<>();
		((IPositionListenable) scanner).addPositionListener(
				IPositionListener.positionMovePerformed(e -> positionsMovedTo.add(e.getPosition())));

		scanner.run();

		// check the nexus file
		checkNexusFile(scanner, false, expectedScanSize);

		// check the interpolated (between scan) positions were moved to
		final InterpolatedMultiScanModel multiScanModel = (InterpolatedMultiScanModel) compoundModel.getModels().get(0);
		final List<IPosition> expectedPositions =  multiScanModel.getInterpolatedPositions();
		assertEquals(expectedPositions, positionsMovedTo);
	}

	private IRunnableDevice<ScanModel> createMalcolmMultiScan(CompoundModel compoundModel) throws Exception {
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setScanPathModel(compoundModel);
		scanModel.setPointGenerator(pointGen);
		scanModel.setDetectors(Arrays.asList(malcolmDevice));

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());

		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.configureScan(scanModel);

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

	private CompoundModel createCompoundModel() {
		final InterpolatedMultiScanModel multiScanModel = new InterpolatedMultiScanModel();
		multiScanModel.setContinuous(true);
		final AxialStepModel mainScanModel = new AxialStepModel("theta", 0.0, 180.0, 10.0);
		 // darks and flats should be (step / 2) before the start of the main scan, and the same after
		final double posBeforeMainScan = mainScanModel.getStart() - mainScanModel.getStep() / 2;
		final double posAfterMainScan = mainScanModel.getStop() + mainScanModel.getStep() / 2;
		if (withDarkAndFlat) {
			multiScanModel.addModel(new AxialPointsModel("theta", posBeforeMainScan, NUM_FLATS));
			multiScanModel.addModel(new AxialPointsModel("theta", posBeforeMainScan, NUM_DARKS));
		}
		multiScanModel.addModel(mainScanModel);
		if (withDarkAndFlat) {
			multiScanModel.addModel(new AxialPointsModel("theta", posAfterMainScan, NUM_FLATS));
			multiScanModel.addModel(new AxialPointsModel("theta", posAfterMainScan, NUM_DARKS));
		}

		final List<IPosition> interpolationPositions = new ArrayList<>();
		IPosition flatPos = null;
		IPosition darkPos = null;
		if (withDarkAndFlat) {
			// flat, dark, tomo, flat, dark
			final Map<String, Object> flatPosMap = new HashMap<>();
			flatPosMap.put("x_stage", -10.0);
			flatPosMap.put("y_stage", -10.0);
			flatPos = new MapPosition(flatPosMap);

			final Map<String, Object> darkPosMap = new HashMap<>();
			darkPosMap.put("x_stage", 0.0);
			darkPosMap.put("y_stage", 0.0);
			darkPosMap.put("portshutter", "Closed");
			darkPos = new MapPosition(darkPosMap);
		}

		if (withDarkAndFlat) {
			interpolationPositions.add(flatPos);
			interpolationPositions.add(darkPos);
		}
		interpolationPositions.add(new Scalar<>("portshutter", "Open"));
		if (withDarkAndFlat) {
			interpolationPositions.add(flatPos);
			interpolationPositions.add(darkPos);
		}
		multiScanModel.setInterpolatedPositions(interpolationPositions);

		final List<ImageType> imageTypes = new ArrayList<>();
		if (withDarkAndFlat) {
			imageTypes.add(ImageType.FLAT);
			imageTypes.add(ImageType.DARK);
		}
		imageTypes.add(ImageType.NORMAL);
		if (withDarkAndFlat) {
			imageTypes.add(ImageType.FLAT);
			imageTypes.add(ImageType.DARK);
		}
		multiScanModel.setImageTypes(imageTypes);

		return new CompoundModel(multiScanModel);
	}

	@Override
	protected void checkDetector(NXdetector detector, DummyMalcolmModel dummyMalcolmModel,
			IMalcolmDetectorModel detectorModel, ScanModel scanModel, NXentry entry, List<String> primaryDataFieldNames,
			Map<String, NXdata> nxDataGroups, int[] sizes) throws DatasetException {
		super.checkDetector(detector, dummyMalcolmModel, detectorModel, scanModel, entry, primaryDataFieldNames, nxDataGroups, sizes);

		// check that the image_key dataset has been written
		final DataNode imageKeyDataNode = detector.getDataNode(FIELD_NAME_IMAGE_KEY);
		assertNotNull(imageKeyDataNode);
		final IDataset imageKeyDataset = imageKeyDataNode.getDataset().getSlice();
		assertNotNull(imageKeyDataset);
		assertArrayEquals(sizes, imageKeyDataset.getShape());

		final List<Integer> expectedImageKeyValues = new ArrayList<>();
		if (withDarkAndFlat) {
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_FLATS, ImageType.FLAT.getImageKey()));
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_DARKS, ImageType.DARK.getImageKey()));
		}
		expectedImageKeyValues.addAll(Collections.nCopies(NUM_MAIN_SCAN_POINTS, ImageType.NORMAL.getImageKey()));
		if (withDarkAndFlat) {
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_FLATS, ImageType.FLAT.getImageKey()));
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_DARKS, ImageType.DARK.getImageKey()));
		}
		int[] expectedArray = expectedImageKeyValues.stream().mapToInt(Integer::intValue).toArray();

		final Dataset expectedImageKeyDataset = DatasetFactory.createFromObject(expectedArray);
		assertDatasetsEqual("/entry/instrument/" + detectorModel.getName() + "/" + FIELD_NAME_IMAGE_KEY,
				expectedImageKeyDataset, imageKeyDataset);
	}

}
