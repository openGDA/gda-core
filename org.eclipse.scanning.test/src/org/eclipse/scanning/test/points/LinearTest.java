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
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.OneDStepModel;
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

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model);
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

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        final int numPoints = 10;
        model.setPoints(numPoints);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model);
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

		OneDEqualSpacingModel model = new OneDEqualSpacingModel();
		final int numPoints = 10;
		model.setPoints(numPoints);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model);
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

        OneDEqualSpacingModel model = new OneDEqualSpacingModel();
        model.setPoints(0);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<OneDEqualSpacingModel> gen = service.createGenerator(model);
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

        OneDStepModel model = new OneDStepModel();
        model.setStep(0.3);
        model.setBoundingLine(line);

		// TODO: These expected values match current behaviour, not behaviour expected by users
		//       They go outside of the given BoundingLine and do not step the required step distance.
		double[] expected_xs = new double[] {0.0, 0.32, 0.64, 0.96, 1.28, 1.6, 1.92, 2.25, 2.57, 2.89, 3.21, 3.53, 3.85, 4.17, 4.5};
		double[] expected_ys = new double[] {0.0, 0.0,  0.0,  0.0,  0.0,  0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0};
		assertEquals(expected_xs.length, expected_ys.length);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model);
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

		OneDStepModel model = new OneDStepModel();
		model.setStep(1);
		BoundingLine bl = new BoundingLine();
		bl.setxStart(0);
		bl.setyStart(0);
		bl.setAngle(0);
		bl.setLength(10);
		model.setBoundingLine(bl);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model);
		final int expectedSize = 11;
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

        OneDStepModel model = new OneDStepModel();
        model.setStep(0);
        model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model);

		List<IPosition> pointList = gen.createPoints();
        GeneratorUtil.testGeneratorPoints(gen);
	}

	@Test(expected = ModelValidationException.class)
	public void testOneDStepNegativeStep() throws Exception {

        BoundingLine line = new BoundingLine();
        line.setxStart(0.0);
        line.setyStart(0.0);
        line.setLength(Math.hypot(3.0, 3.0));

		OneDStepModel model = new OneDStepModel();
		model.setStep(-0.3);
		model.setBoundingLine(line);

		// Get the point list
		IPointGenerator<OneDStepModel> gen = service.createGenerator(model);
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

	        OneDStepModel model = new OneDStepModel();
	        model.setStep(0);
	        model.setBoundingLine(line);

			// Get the point list
			IPointGenerator<OneDStepModel> gen = service.createGenerator(model, roi);
			List<IPosition> pointList = gen.createPoints();
	        GeneratorUtil.testGeneratorPoints(gen);
		} catch (ModelValidationException | GeneratorException e) {
			return;
		}
		throw new Exception("testOneDStepWrongROI did not throw an exception as expected!");
	}
}
