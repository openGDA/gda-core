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
package org.eclipse.scanning.test.points;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;


/**
 * Test that the datasets in the nexus file produced by the scan have the expected shape
 * for various types of scans.
 */
class ScanShapeTest extends AbstractGeneratorTest {

	static Stream<Arguments> bothParams() {
		return nestParams().flatMap(nestParam -> snakeParams().map(snakeParam -> Arguments.of(nestParam, snakeParam)));
	}

	/** number of outer dimensions */
	static Stream<Integer> nestParams() {
		return Stream.of(0, 1, 2, 3, 6);
	}

	/** only relevant for grid scans */
	static Stream<Boolean> snakeParams() {
		return Stream.of(false, true);
	}

	private int circularSize = 48;
	private int polygonSize = 32;

	private int nestSize(int nest) {
		int size = 1;
		for (int i = 1; i < nest + 1; i++) {
			size *= i;
		}
		return size;
	}

	@ParameterizedTest(name = "nestCount: {0}, snake: {1}")
	@MethodSource("bothParams")
	void testShapeGrid(int nestCount, boolean snake) throws Exception {
		ScanRequest req = createGridScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);
		final int expectedRank = nestCount + 2;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 2, shape[i]);
		}
		assertEquals(4, shape[shape.length - 2]);
		assertEquals(25, shape[shape.length - 1]);
	}

	@ParameterizedTest(name = "nestCount: {0}, snake: {1}")
	@MethodSource("bothParams")
	void testShapeGridCircularRegion(int nestCount, boolean snake) throws Exception {
		ScanRequest req = createGridWithCircleRegionScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);
		int[] shape = scanInfo.getShape();
		assertEquals(nestSize(nestCount + 1) * circularSize, scanInfo.getSize());
		assertEquals(nestCount + 1, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 2, shape[i]);
		}
		assertEquals(circularSize, shape[shape.length - 1]);
	}

	@ParameterizedTest(name = "nestCount: {0}, snake: {1}")
	@MethodSource("bothParams")
	void testShapeGridPolygonRegion(int nestCount, boolean snake) throws Exception {
		ScanRequest req = createGridWithPolygonRegionScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);
		int[] shape = scanInfo.getShape();
		assertEquals(nestCount + 1, shape.length);
		assertEquals(nestSize(nestCount + 1) * polygonSize, scanInfo.getSize());
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 2, shape[i]);
		}
		assertEquals(polygonSize, shape[shape.length - 1]);
	}

	@ParameterizedTest(name = "nestCount: {0}")
	@MethodSource("nestParams")
	void testShapeSpiral(int nestCount) throws Exception {
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisSpiralModel spiralModel = new TwoAxisSpiralModel("x", "y");
		spiralModel.setBoundingBox(box);

		List<IScanPointGeneratorModel> models = new ArrayList<>();
		for (int i = 1; i < nestCount + 1; i++) {
			models.add(new AxialStepModel("T" + (nestCount - 1- i), 100, 100 + (10 * i), 10));
		}
		models.add(spiralModel);
		CompoundModel compoundModel = new CompoundModel(models);

		ScanRequest req = new ScanRequest();
		req.setCompoundModel(compoundModel);
		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);

		final int expectedRank = nestCount + 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 2, shape[i]);
		}
		assertEquals(15, shape[shape.length - 1]);
	}

	@ParameterizedTest(name = "nestCount: {0}")
	@MethodSource("nestParams")
	void testShapeLine(int nestCount) throws Exception {
		LinearROI roi = new LinearROI(new double[] { 0, 0 }, new double [] { 3, 3 });
		ScanRegion region = new ScanRegion(roi, "x", "y");
		// TODO: we need to give the region to the point generator somehow, but the
		// scan estimator doesn't have it at present
		TwoAxisLinePointsModel lineModel = new TwoAxisLinePointsModel();
		lineModel.setPoints(10);
		lineModel.setxAxisName("x");
		lineModel.setyAxisName("y");

		List<IScanPointGeneratorModel> models = new ArrayList<>();
		for (int i = 1; i < nestCount + 1; i++) {
			models.add(new AxialStepModel("T" + (nestCount - 1- i), 100, 100 + (10 * i), 10));
		}
		models.add(lineModel);
		CompoundModel compoundModel = new CompoundModel(models);
		compoundModel.setRegions(Arrays.asList(region));

		ScanRequest req = new ScanRequest();
		req.setCompoundModel(compoundModel);
		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);

		final int expectedRank = nestCount + 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 2, shape[i]);
		}
		assertEquals(10, shape[shape.length - 1]);
	}

	@Test
	void testShapeStatic() throws Exception {
		StaticModel staticModel = new StaticModel();
		CompoundModel compoundModel = new CompoundModel(staticModel);

		ScanRequest req = new ScanRequest();
		req.setCompoundModel(compoundModel);

		ScanInformation scanInfo = new ScanInformation(pointGeneratorService, req);

		// Note: a StaticModel of size 1 produces a scan of rank 1, size 1
		final int expectedRank = 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
	}

	private ScanRequest createGridScanRequest(int nestCount, boolean snake) {
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel gridModel = new TwoAxisGridPointsModel("x", "y");
		gridModel.setyAxisPoints(4);
		gridModel.setxAxisPoints(25);
		gridModel.setBoundingBox(box);
		gridModel.setAlternating(snake);

		List<IScanPointGeneratorModel> models = new ArrayList<>();
		for (int i = 1; i < nestCount + 1; i++) {
			models.add(new AxialStepModel("T" + (nestCount - 1 - i), 100, 100 + (10 * i), 10));
		}
		models.add(gridModel);
		CompoundModel compoundModel = new CompoundModel(models);

		//System.out.println("The number of points will be: "+gen.size());

		ScanRequest req = new ScanRequest();
		req.setCompoundModel(compoundModel);
		return req;
	}

	private ScanRequest createGridWithCircleRegionScanRequest(int nestCount, boolean snake) {
		ScanRequest req = createGridScanRequest(nestCount, snake);

		CircularROI roi = new CircularROI(2, 1, 1);
		ScanRegion circleRegion = new ScanRegion(roi, "x", "y");
		req.getCompoundModel().setRegions(Arrays.asList(circleRegion));

		return req;
	}

	private ScanRequest createGridWithPolygonRegionScanRequest(int nestCount, boolean snake) {
		ScanRequest req = createGridScanRequest(nestCount, snake);

		PolygonalROI diamond = new PolygonalROI(new double[] { 1.5, 0 });
		diamond.insertPoint(new double[] { 3, 1.5 });
		diamond.insertPoint(new double[] { 1.5, 3 });
		diamond.insertPoint(new double[] { 0, 1.5 });

		ScanRegion circleRegion = new ScanRegion(diamond, "x", "y");
		req.getCompoundModel().setRegions(Arrays.asList(circleRegion));

		return req;
	}

}
