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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
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
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel.ImageType;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmDatasetModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.jupiter.api.Test;

class MalcolmMultiScanTest extends AbstractMalcolmScanTest {

	private static final String FIELD_NAME_IMAGE_KEY = "image_key";
	private static final String SCANNABLE_NAME_SHUTTER = "portshutter";

	private static final int NUM_FLATS = 5;
	private static final int NUM_DARKS = 3;
	private static final int NUM_MAIN_SCAN_POINTS = 19;

	private boolean withDarksAndFlats = true;

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setExposureTime(0.001);
		model.setAxesToMove(List.of(ANGLE_AXIS_NAME));
		model.setPositionerNames(List.of(ANGLE_AXIS_NAME));

		return model;
	}

	@Test
	void testMalcolmMultiScan() throws Exception {
		testMalcolmMultiScan(true);
	}

	@Test
	void testMalcolmMultiScanNoDarksOrFlats() throws Exception {
		testMalcolmMultiScan(false);
	}

	private void testMalcolmMultiScan(boolean withDarksAndFlats) throws Exception {
		this.withDarksAndFlats = withDarksAndFlats;
		final CompoundModel compoundModel = createCompoundModel();

		final IRunnableDevice<ScanModel> scanner = createMalcolmMultiScan(compoundModel);
		final List<IPosition> positionsMovedTo = new ArrayList<>();
		((IPositionListenable) scanner).addPositionListener(
				IPositionListener.positionMovePerformed(e -> positionsMovedTo.add(e.getPosition())));

		scanner.run();

		// check the nexus file
		final int expectedScanSize = NUM_MAIN_SCAN_POINTS + (withDarksAndFlats ? NUM_FLATS * 2 + NUM_DARKS * 2 : 0);
		checkNexusFile(scanner, false, expectedScanSize);

		// check the interpolated (between scan) positions were moved to
		final InterpolatedMultiScanModel multiScanModel = (InterpolatedMultiScanModel) compoundModel.getModels().get(0);
		final List<IPosition> expectedPositions =  multiScanModel.getInterpolatedPositions();
		assertThat(positionsMovedTo, is(equalTo(expectedPositions)));
	}

	private IRunnableDevice<ScanModel> createMalcolmMultiScan(CompoundModel compoundModel) throws Exception {
		final IPointGenerator<CompoundModel> pointGen = pointGenService.createCompoundGenerator(compoundModel);

		// Create the model for a scan.
		final ScanModel scanModel = new ScanModel();
		scanModel.setScanPathModel(compoundModel);
		scanModel.setPointGenerator(pointGen);
		scanModel.setDetectors(List.of(malcolmDevice));

		// Create a file to scan into.
		scanModel.setFilePath(output.getAbsolutePath());

		// Cannot set the generator from @PreConfigure in this unit test.
		malcolmDevice.configureScan(scanModel);

		// Create a scan and run it without publishing events
		final IRunnableDevice<ScanModel> scanner = scanService.createScanDevice(scanModel);

		final IPointGenerator<?> fgen = pointGen;
		((IRunnableEventDevice<ScanModel>)scanner).addRunListener(IRunListener.createRunWillPerformListener(
				event -> System.out.println("Running acquisition scan of size "+fgen.size())));

		return scanner;
	}

	private CompoundModel createCompoundModel() {
		final InterpolatedMultiScanModel multiScanModel = new InterpolatedMultiScanModel();
		multiScanModel.setContinuous(true);
		final AxialStepModel mainScanModel = new AxialStepModel(ANGLE_AXIS_NAME, 0.0, 180.0, 10.0);

		createPointsModels(mainScanModel).forEach(multiScanModel::addModel);
		multiScanModel.setInterpolatedPositions(createInterpolationPositions());
		multiScanModel.setImageTypes(createImageTypes());

		return new CompoundModel(multiScanModel);
	}

	private List<IScanPointGeneratorModel> createPointsModels(final AxialStepModel mainScanModel) {
		if (!withDarksAndFlats) return List.of(mainScanModel);

		// darks and flats should be (step / 2) before the start of the main scan, and the same after
		final double posBeforeMainScan = mainScanModel.getStart() - mainScanModel.getStep() / 2;
		final double posAfterMainScan = mainScanModel.getStop() + mainScanModel.getStep() / 2;
		final List<IScanPointGeneratorModel> models = new ArrayList<>();
		models.add(new AxialPointsModel(ANGLE_AXIS_NAME, posBeforeMainScan, NUM_FLATS));
		models.add(new AxialPointsModel(ANGLE_AXIS_NAME, posBeforeMainScan, NUM_DARKS));
		models.add(mainScanModel);
		models.add(new AxialPointsModel(ANGLE_AXIS_NAME, posAfterMainScan, NUM_FLATS));
		models.add(new AxialPointsModel(ANGLE_AXIS_NAME, posAfterMainScan, NUM_DARKS));

		return models;
	}

	private List<ImageType> createImageTypes() {
		if (!withDarksAndFlats) {
			return List.of(ImageType.NORMAL);
		}

		return List.of(ImageType.FLAT, ImageType.DARK, ImageType.NORMAL, ImageType.FLAT, ImageType.DARK);
	}

	private List<IPosition> createInterpolationPositions() {
		if (!withDarksAndFlats) {
			return List.of(new Scalar<>(SCANNABLE_NAME_SHUTTER, "Open"));
		}

		// flat, dark, tomo, flat, dark
		final Map<String, Object> flatPosMap = new HashMap<>();
		flatPosMap.put("x_stage", -10.0);
		flatPosMap.put("y_stage", -10.0);
		final IPosition flatPos = new MapPosition(flatPosMap);

		final Map<String, Object> darkPosMap = new HashMap<>();
		darkPosMap.put("x_stage", 0.0);
		darkPosMap.put("y_stage", 0.0);
		darkPosMap.put(SCANNABLE_NAME_SHUTTER, "Closed");
		final IPosition darkPos = new MapPosition(darkPosMap);

		final IPosition normalPos = new Scalar<>(SCANNABLE_NAME_SHUTTER, "Open");

		return List.of(flatPos, darkPos, normalPos, flatPos, darkPos);
	}

	@Override
	protected void checkDetector(NXdetector detector, DummyMalcolmModel dummyMalcolmModel,
			IMalcolmDetectorModel detectorModel, ScanModel scanModel, boolean foldedGrid, NXentry entry,
			List<String> primaryDataFieldNames, int[] sizes) throws Exception {
		super.checkDetector(detector, dummyMalcolmModel, detectorModel, scanModel, foldedGrid, entry,
				primaryDataFieldNames, sizes);

		// check that the image_key dataset has been written
		final DataNode imageKeyDataNode = detector.getDataNode(FIELD_NAME_IMAGE_KEY);
		assertThat(imageKeyDataNode, is(notNullValue()));
		final IDataset imageKeyDataset = imageKeyDataNode.getDataset().getSlice();
		assertThat(imageKeyDataset, is(notNullValue()));
		assertThat(imageKeyDataset.getShape(), is(equalTo(sizes)));

		final List<Integer> expectedImageKeyValues = new ArrayList<>();
		if (withDarksAndFlats) {
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_FLATS, ImageType.FLAT.getImageKey()));
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_DARKS, ImageType.DARK.getImageKey()));
		}
		expectedImageKeyValues.addAll(Collections.nCopies(NUM_MAIN_SCAN_POINTS, ImageType.NORMAL.getImageKey()));
		if (withDarksAndFlats) {
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_FLATS, ImageType.FLAT.getImageKey()));
			expectedImageKeyValues.addAll(Collections.nCopies(NUM_DARKS, ImageType.DARK.getImageKey()));
		}
		int[] expectedArray = expectedImageKeyValues.stream().mapToInt(Integer::intValue).toArray();

		final Dataset expectedImageKeyDataset = DatasetFactory.createFromObject(expectedArray);
		assertDatasetsEqual("/entry/instrument/" + detectorModel.getName() + "/" + FIELD_NAME_IMAGE_KEY,
				expectedImageKeyDataset, imageKeyDataset);
	}

	@Override
	protected List<String> getExpectedDetectorDataNodeNames(final String detectorName,
			final List<DummyMalcolmDatasetModel> datasetModels) {
		final List<String> dataNodeNames = super.getExpectedDetectorDataNodeNames(detectorName, datasetModels);
		dataNodeNames.add(FIELD_NAME_IMAGE_KEY);
		return dataNodeNames;
	}

}
