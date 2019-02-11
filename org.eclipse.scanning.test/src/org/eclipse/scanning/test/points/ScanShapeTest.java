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

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.SpiralModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.api.points.models.StepModel;
import org.eclipse.scanning.api.scan.ScanInformation;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * Test that the datasets in the nexus file produced by the scan have the expected shape
 * for various types of scans.
 * TODO this could be more easily parameterized with JUnit 5 when we can use it, see DAQ-1478.
 * In particular the snake parameter is only required by grid scans, and {@link #testShapeStatic()}
 * doesn't use the nest count either
 */
@RunWith(value=Parameterized.class)
public class ScanShapeTest {

	private static IPointGeneratorService service;

	@BeforeClass
	public static void beforeClass() throws Exception {
		service = new PointGeneratorService();
	}

	@Parameters(name="nestCount= {0}, snake= {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ 0, false },
			{ 0, true },
			{ 1, false },
			{ 1, true },
			{ 2, false },
			{ 2, true },
			{ 3, false },
			{ 3, true },
			{ 6, false },
			{ 6, true }
		});
	}

	private int nestCount;

	private boolean snake;

	public ScanShapeTest(int nestCount, boolean snake) {
		this.nestCount = nestCount; // the number of outer dimensions
		this.snake = snake; // only relevent for grid scans
	}

	@Test
	public void testShapeGrid() throws Exception {
		ScanRequest<Object> req = createGridScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(service, req);
		final int expectedRank = nestCount + 2;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 1, shape[i]);
		}
		assertEquals(4, shape[shape.length - 2]);
		assertEquals(25, shape[shape.length - 1]);
	}

	@Test
	public void testShapeGridCircularRegion() throws Exception {
		ScanRequest<Object> req = createGridWithCircleRegionScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(service, req);
		int[] shape = scanInfo.getShape();
		assertEquals(nestCount + 1, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 1, shape[i]);
		}
		assertEquals(84, shape[shape.length - 1]);
	}

	@Test
	public void testShapeGridPolygonRegion() throws Exception {
		ScanRequest<Object> req = createGridWithPolygonRegionScanRequest(nestCount, snake);

		ScanInformation scanInfo = new ScanInformation(service, req);
		int[] shape = scanInfo.getShape();
		assertEquals(nestCount + 1, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 1, shape[i]);
		}
		assertEquals(52, shape[shape.length - 1]);
	}

	@Test
	public void testShapeSpiral() throws Exception {
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		SpiralModel spiralModel = new SpiralModel("x", "y");
		spiralModel.setBoundingBox(box);

		Object[] models = new Object[nestCount + 1];
		for (int i = 0; i < nestCount; i++) {
			models[i] = new StepModel("T" + (nestCount - 1- i), 100, 100 + (10 * i), 10);
		}
		models[nestCount] = spiralModel;
		CompoundModel<Object> compoundModel = new CompoundModel<>(models);

		ScanRequest<Object> req = new ScanRequest<>();
		req.setCompoundModel(compoundModel);
		ScanInformation scanInfo = new ScanInformation(service, req);

		final int expectedRank = nestCount + 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 1, shape[i]);
		}
		assertEquals(15, shape[shape.length - 1]);
	}

	@Test
	public void testShapeLine() throws Exception {
		LinearROI roi = new LinearROI(new double[] { 0, 0 }, new double [] { 3, 3 });
		ScanRegion<Object> region = new ScanRegion<>(roi, "x", "y");
		// TODO: we need to give the region to the point generator somehow, but the
		// scan estimator doesn't have it at present
		OneDEqualSpacingModel lineModel = new OneDEqualSpacingModel();
		lineModel.setPoints(10);
		lineModel.setFastAxisName("x");
		lineModel.setSlowAxisName("y");

		Object[] models = new Object[nestCount + 1];
		for (int i = 0; i < nestCount; i++) {
			models[i] = new StepModel("T" + (nestCount - 1- i), 100, 100 + (10 * i), 10);
		}
		models[nestCount] = lineModel;
		CompoundModel<Object> compoundModel = new CompoundModel<>(models);
		compoundModel.setRegions(Arrays.asList(region));

		ScanRequest<Object> req = new ScanRequest<>();
		req.setCompoundModel(compoundModel);
		ScanInformation scanInfo = new ScanInformation(service, req);

		final int expectedRank = nestCount + 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
		for (int i = 0; i < nestCount; i++) {
			assertEquals(i + 1, shape[i]);
		}
		assertEquals(10, shape[shape.length - 1]);
	}

	@Test
	public void testShapeStatic() throws Exception {
		// Note this test shouldn't be repeated
		StaticModel staticModel = new StaticModel();
		CompoundModel<Object> compoundModel = new CompoundModel<>(staticModel);

		ScanRequest<Object> req = new ScanRequest<>();
		req.setCompoundModel(compoundModel);

		ScanInformation scanInfo = new ScanInformation(service, req);

		// Note: a StaticModel of size 1 produces a scan of rank 1, size 1
		final int expectedRank = 1;
		assertEquals(expectedRank, scanInfo.getRank());
		int[] shape = scanInfo.getShape();
		assertEquals(expectedRank, shape.length);
	}

	private ScanRequest<Object> createGridScanRequest(int nestCount, boolean snake) {
		BoundingBox box = new BoundingBox();
		box.setFastAxisStart(0);
		box.setSlowAxisStart(0);
		box.setFastAxisLength(3);
		box.setSlowAxisLength(3);

		GridModel gridModel = new GridModel("x", "y");
		gridModel.setSlowAxisPoints(4);
		gridModel.setFastAxisPoints(25);
		gridModel.setBoundingBox(box);
		gridModel.setSnake(snake);

		Object[] models = new Object[nestCount + 1];
		for (int i = 0; i < nestCount; i++) {
			models[i] = new StepModel("T" + (nestCount - 1 - i), 100, 100 + (10 * i), 10);
		}
		models[nestCount] = gridModel;
		CompoundModel<Object> compoundModel = new CompoundModel<>(models);

		//System.out.println("The number of points will be: "+gen.size());

		ScanRequest<Object> req = new ScanRequest<>();
		req.setCompoundModel(compoundModel);
		return req;
	}

	private ScanRequest<Object> createGridWithCircleRegionScanRequest(int nestCount, boolean snake) {
		ScanRequest<Object> req = createGridScanRequest(nestCount, snake);

		CircularROI roi = new CircularROI(2, 1, 1);
		ScanRegion<Object> circleRegion = new ScanRegion<Object>(roi, "x", "y");
		req.getCompoundModel().setRegions(Arrays.asList(circleRegion));

		return req;
	}

	private ScanRequest<Object> createGridWithPolygonRegionScanRequest(int nestCount, boolean snake) {
		ScanRequest<Object> req = createGridScanRequest(nestCount, snake);

		PolygonalROI diamond = new PolygonalROI(new double[] { 1.5, 0 });
		diamond.insertPoint(new double[] { 3, 1.5 });
		diamond.insertPoint(new double[] { 1.5, 3 });
		diamond.insertPoint(new double[] { 0, 1.5 });

		ScanRegion<Object> circleRegion = new ScanRegion<Object>(diamond, "x", "y");
		req.getCompoundModel().setRegions(Arrays.asList(circleRegion));

		return req;
	}

}
