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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class RasterTest {


	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}

	@Test
	public void testFillingBoundingRectangle() throws Exception {
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model, boundingRectangle);

		final int expectedSize = 9;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 3, 3 }, gen.getShape());

		// Check correct number of points
		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		// Check some random points are correct
		assertEquals(0.5, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.5, pointList.get(0).getValue("y"), 1e-8);

		assertEquals(0.5, pointList.get(3).getValue("x"), 1e-8);
		assertEquals(1.5, pointList.get(3).getValue("y"), 1e-8);

		assertEquals(1.5, pointList.get(7).getValue("x"), 1e-8);
		assertEquals(2.5, pointList.get(7).getValue("y"), 1e-8);

        GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	public void testSimpleBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(5);
		box.setyAxisLength(5);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);
		model.setBoundingBox(box);



		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();

		// Zeroth point is half a step in from each side, i.e. (0.5, 0.5).
		assertEquals(0.5, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.5, pointList.get(0).getValue("y"), 1e-8);

		// First point is (1.5, 0.5).
		assertEquals(1.5, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.5, pointList.get(1).getValue("y"), 1e-8);
	}

	@Test
	public void testNegativeStep() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(5);
		box.setyAxisStart(0);
		box.setxAxisLength(-5);
		box.setyAxisLength(5);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");

		model.setxAxisStep(-1);
		// Okay to do this here because there is "negative width"
		// for the points to protrude into.

		model.setyAxisStep(1);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();

		// Zeroth point is (4.5, 0.5).
		assertEquals(4.5, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.5, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (3.5, 0.5).
		assertEquals(3.5, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.5, pointList.get(1).getValue("y"), 1e-8);
	}

	@Test(expected=ModelValidationException.class)
	public void testBackwardsStep() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(5);
		box.setyAxisLength(5);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");

		model.setxAxisStep(-1);
		// Not okay to do this here because there is no "negative width"
		// for the points to protrude into.

		model.setyAxisStep(1);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();
	}

	// Note this is a bit of a integration test not a strict unit test
	@Test
	public void testFillingAMoreComplicatedBoundingRectangle() throws Exception {
		double xStart = 0.0;
		double xStop = 25.5;
		double yStart = 0.0;
		double yStop = 33.33;

		double xStep = 0.4;
		double yStep = 0.6;

		RectangularROI roi = new RectangularROI();
		roi.setPoint(Math.min(xStart, xStop), Math.min(yStart, yStop));
		roi.setLengths(Math.abs(xStop - xStart), Math.abs(yStop - yStart));


		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(xStep);
		model.setyAxisStep(yStep);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model, roi);
		List<IPosition> pointList = gen.createPoints();

		int rows = (int) (Math.floor((xStop - xStart) / xStep));
		int cols = (int) (Math.floor((yStop - yStart) / yStep));
		// Check the list size
		assertEquals("Point list size should be correct", rows * cols, pointList.size());

		// Offset by half a step
		xStart += xStep/2;
		yStart += yStep/2;
		// Check some points
		assertEquals(new Point(0, xStart, 0, yStart, 0), pointList.get(0));
		assertEquals(xStart + 3 * xStep, pointList.get(3).getValue("x"), 1e-8);
		assertEquals(yStart + 0 * yStep, pointList.get(3).getValue("y"), 1e-8);

		assertEquals(xStart + xStep, pointList.get(1+1*rows).getValue("x"), 1e-8);
		assertEquals(yStart + yStep, pointList.get(1+1*rows).getValue("y"), 1e-8);

		assertEquals(xStart + 5*xStep, pointList.get(5+2*rows).getValue("x"), 1e-8);
		assertEquals(yStart + 2*yStep, pointList.get(5+2*rows).getValue("y"), 1e-8);

		GeneratorUtil.testGeneratorPoints(gen, cols, rows);
	}

	// Note this is a bit of a integration test not a strict unit test
	@Test
	public void testFillingACircle() throws Exception {
		double xCentre = 0;
		double yCentre = 0;
		double radius = 1;

		CircularROI roi = new CircularROI();
		roi.setPoint(xCentre, yCentre);
		roi.setRadius(radius);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model, roi);
		final int expectedSize = 4;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		// Check the length of the points list
		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		// Check the points are correct and the order is maintained
        assertEquals(new Point(0, -0.5, 0, -0.5, 0, false), pointList.get(0));
        assertEquals(new Point(1, 0.5, 1, -0.5, 1, false), pointList.get(1));
        assertEquals(new Point(2, -0.5, 2, 0.5, 2, false), pointList.get(2));
        assertEquals(new Point(3, 0.5, 3, 0.5, 3, false), pointList.get(3));

        GeneratorUtil.testGeneratorPoints(gen, 4);
	}


	@Test
	public void testNestedNeXus() throws Exception {

		int[] sizes = {8,5};

		// Create scan points for a grid and make a generator
		TwoAxisGridStepModel rmodel = new TwoAxisGridStepModel("x", "y");
		rmodel.setxAxisName("xNex");
		rmodel.setxAxisStep(3d/sizes[1]);
		rmodel.setyAxisName("yNex");
		rmodel.setyAxisStep(3d/sizes[0]);
		rmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		final int[] expectedShape = new int[] { sizes[0], sizes[1] };
		IPointGenerator<?> gen = service.createGenerator(rmodel);
		final int expectedSize = expectedShape[0] * expectedShape[1];
		assertEquals(expectedSize, gen.size());
		assertEquals(sizes.length, gen.getRank());
		assertArrayEquals(expectedShape, gen.getShape());

		IPosition first = gen.iterator().next();
		assertEquals(1.5/sizes[1], first.get("xNex"));
		assertEquals(1.5/sizes[0], first.get("yNex"));

		IPosition last = null;
		Iterator<IPosition> it = gen.iterator();
		while(it.hasNext()) last = it.next();

		assertEquals(3d-1.5/sizes[1], (double) last.get("xNex"), 0.001);
		assertEquals(3d-1.5/sizes[0], (double) last.get("yNex"), 0.001);
	}

	@Test
	public void testFillingRectangleAwayFromOrigin() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(-10, 5, 2.5, 3.0, 0.0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model, roi);
		final int expectedSize = 6;
		assertEquals(expectedSize , gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 3, 2 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		// Check some points
		assertEquals(new Point(0, -9.5, 0, 5.5, 0), pointList.get(0));
		assertEquals(new Point(1, -8.5, 0, 5.5, 1), pointList.get(1));
		assertEquals(new Point(1, -8.5, 1, 6.5, 3), pointList.get(3));
		assertEquals(new Point(1, -8.5, 2, 7.5, 5), pointList.get(5));
	}

	@Test
	public void testFillingRectangleWithSnake() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(1, 1, 2, 2, 0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);
		model.setAlternating(true);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = service.createGenerator(model, roi);
		final int expectedSize = 4;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 2, 2 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		// Check some points
		assertEquals(new Point(0, 1.5, 0, 1.5, 0), pointList.get(0));
		assertEquals(new Point(1, 2.5, 0, 1.5, 1), pointList.get(1));
		assertEquals(new Point(1, 2.5, 1, 2.5, 2), pointList.get(2));
		assertEquals(new Point(0, 1.5, 1, 2.5, 3), pointList.get(3));
	}

}
