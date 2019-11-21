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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.ModelValidationException;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.TwoAxisLinePointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class LinearTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}

	@Test
	public void testOneDEqualSpacing() throws Exception {

		BoundingLine line = new BoundingLine();
		line.setxStart(0.0);
		line.setyStart(0.0);
		line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = service.createGenerator(model);
		assertEquals(numPoints, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { numPoints }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();

		assertEquals(numPoints, pointList.size());
        GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	public void testIndicesOneDEqualSpacing() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = service.createGenerator(model);
		assertEquals(numPoints, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { numPoints }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();

		assertEquals(numPoints, pointList.size());
        GeneratorUtil.testGeneratorPoints(gen);

        for (int i = 0; i < pointList.size(); i++) {
		    IPosition pos = pointList.get(i);
		    int xIndex = pos.getIndex(model.getxAxisName());
		    int yIndex = pos.getIndex(model.getyAxisName());

		    assertEquals(i, xIndex);
		    assertEquals(i, yIndex);
		    assertTrue(pos.getScanRank()==1);
		}
	}


	@Test
	public void testOneDEqualSpacingNoROI() throws GeneratorException {

		TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
		final int numPoints = 10;
		model.setPoints(numPoints);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = service.createGenerator(model);
		int expectedSize = 10;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
	}

	@Test(expected = ModelValidationException.class)
	public void testOneDEqualSpacingNoPoints() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLinePointsModel model = new TwoAxisLinePointsModel();
        model.setPoints(0);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLinePointsModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
	}


	@Test
	public void testOneDStep() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
		//implicit line.setAngle(0);
		line.setLength(Math.hypot(3.0, 3.0)); // 4.24264

        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
        model.setStep(0.3);
        model.setBoundingLine(line);
        // Current behaviour now that line has both ends trimmed by 0.5 steps each [as with two axis Grids] so that continuous operation
        // would 'prepare bounds' within the line.
		double[] expected_xs = new double[] {0.15, 0.45, 0.75, 1.05, 1.35, 1.65, 1.95, 2.25, 2.55, 2.85, 3.15, 3.45, 3.75, 4.05};
		double[] expected_ys = new double[] {0.0, 0.0,  0.0,  0.0,  0.0,  0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0};
		assertEquals(expected_xs.length, expected_ys.length);

		// Get the point list
		IPointGenerator<TwoAxisLineStepModel> gen = service.createGenerator(model);
		final int expectedSize = expected_xs.length;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		double[] actual_xs = pointList.stream().mapToDouble(p -> p.getValue("stage_x")).toArray();
		double[] actual_ys = pointList.stream().mapToDouble(p -> p.getValue("stage_y")).toArray();

		assertArrayEquals(expected_xs, actual_xs, 0.01);
		assertArrayEquals(expected_ys, actual_ys, 0.01);
		GeneratorUtil.testGeneratorPoints(gen, expectedSize);
	}

	@Test
	public void testOneDStepNoROI() throws GeneratorException {

		TwoAxisLineStepModel model = new TwoAxisLineStepModel();
		model.setStep(1);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<TwoAxisLineStepModel> gen = service.createGenerator(model);
		final int expectedSize = 10;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		assertEquals(expectedSize, gen.createPoints().size());
	}

	@Test(expected = ModelValidationException.class)
	public void testOneDStepNoStep() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
        model.setStep(0);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLineStepModel> gen = service.createGenerator(model);

		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test(expected = ModelValidationException.class)
	public void testOneDStepNegativeStep() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

		TwoAxisLineStepModel model = new TwoAxisLineStepModel();
		model.setStep(-0.3);
		model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<TwoAxisLineStepModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();
		GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test
	public void testOneDStepWrongROI() throws Exception {

		try {
			RectangularROI roi = new RectangularROI(new double[]{0,0}, new double[]{3,3});

	        BoundingLine line = new BoundingLine();
	        line.setxStart(0.0);
	        line.setyStart(0.0);
	        line.setLength(Math.hypot(3.0, 3.0));

	        TwoAxisLineStepModel model = new TwoAxisLineStepModel();
	        model.setStep(0);
	        model.setBoundingLine(line);

			// Get the point list
			IPointGenerator<TwoAxisLineStepModel> gen = service.createGenerator(model, roi);
			List<IPosition> pointList = gen.createPoints();
	        GeneratorUtil.testGeneratorPoints(gen);
		} catch (ModelValidationException | GeneratorException e) {
			return;
		}
		throw new Exception("testOneDStepWrongROI did not throw an exception as expected!");
	}
}
