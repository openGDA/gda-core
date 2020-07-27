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

import static org.eclipse.scanning.api.points.models.AxialStepModel.createStaticAxialModel;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableEventDevice;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.InterpolatedMultiScanModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.event.IPositionListenable;
import org.eclipse.scanning.api.scan.event.IPositionListener;
import org.eclipse.scanning.api.scan.event.IRunListener;
import org.eclipse.scanning.api.scan.event.RunEvent;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.junit.Test;

public class MalcolmMultiScanTest extends AbstractMalcolmScanTest {

	private static final int NUM_FLATS = 5;
	private static final int NUM_DARKS = 3;
	private static final int NUM_MAIN_SCAN_POINTS = 19;

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
		final CompoundModel compoundModel = createCompoundModel();

		final IRunnableDevice<ScanModel> scanner = createMalcolmMultiScan(compoundModel);
		final List<IPosition> positionsMovedTo = new ArrayList<>();
		((IPositionListenable) scanner).addPositionListener(
				IPositionListener.positionMovePerformed(e -> positionsMovedTo.add(e.getPosition())));

		scanner.run();

		// check the nexus file
		final int expectedScanSize = NUM_MAIN_SCAN_POINTS + NUM_FLATS * 2 + NUM_DARKS * 2;
		checkNexusFile(scanner, false, new int[] { expectedScanSize });

		// check the interpolated (between scan) positions were moved to
		final InterpolatedMultiScanModel multiScanModel = (InterpolatedMultiScanModel) compoundModel.getModels().get(0);
		final List<IPosition> expectedPositions =  multiScanModel.getInterpolationPositions();
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
		IRunnableDevice<ScanModel> scanner = runnableDeviceService.createRunnableDevice(scanModel, null);

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
		multiScanModel.addModel(createStaticAxialModel("theta", posBeforeMainScan, NUM_FLATS));
		multiScanModel.addModel(createStaticAxialModel("theta", posBeforeMainScan, NUM_DARKS));
		multiScanModel.addModel(mainScanModel);
		multiScanModel.addModel(createStaticAxialModel("theta", posAfterMainScan, NUM_FLATS));
		multiScanModel.addModel(createStaticAxialModel("theta", posAfterMainScan, NUM_DARKS));

		// flat, dark, tomo, flat, dark
		final Map<String, Object> flatPosMap = new HashMap<>();
		flatPosMap.put("x_stage", -10.0);
		flatPosMap.put("y_stage", -10.0);
		final IPosition flatPos = new MapPosition(flatPosMap);

		final Map<String, Object> darkPosMap = new HashMap<>();
		darkPosMap.put("x_stage", 0.0);
		darkPosMap.put("y_stage", 0.0);
		darkPosMap.put("portshutter", "Closed");
		final IPosition darkPos = new MapPosition(darkPosMap);

		final List<IPosition> interpolationPositions = new ArrayList<>();
		interpolationPositions.add(flatPos);
		interpolationPositions.add(darkPos);
		interpolationPositions.add(new Scalar<>("portshutter", "Open"));
		interpolationPositions.add(flatPos);
		interpolationPositions.add(darkPos);
		multiScanModel.setInterpolationPositions(interpolationPositions);

		return new CompoundModel(multiScanModel);
	}

}
