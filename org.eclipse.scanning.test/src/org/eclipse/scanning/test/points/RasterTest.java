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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.eclipse.scanning.points.ServiceHolder;
import org.eclipse.scanning.points.validation.ValidatorService;
import org.junit.BeforeClass;
import org.junit.Test;

public class RasterTest {

	private static final IPointGeneratorService pointGeneratorService = new PointGeneratorService();

	@BeforeClass
	public static void setUpClass() {
		final ServiceHolder serviceHolder = new ServiceHolder();
		serviceHolder.setValidatorService(new ValidatorService());
		serviceHolder.setPointGeneratorService(pointGeneratorService);
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
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, boundingRectangle);

		final int expectedSize = 16;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 4, 4 }, gen.getShape());

		checkRasterPoints(gen);
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

		IPointGenerator<TwoAxisGridStepModel> gen = pointGeneratorService.createGenerator(model);
		checkRasterPoints(gen);
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

		IPointGenerator<TwoAxisGridStepModel> gen = pointGeneratorService.createGenerator(model);
		checkRasterPoints(gen);
	}

	@Test(expected=GeneratorException.class)
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

		pointGeneratorService.createGenerator(model);
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
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);

		int rows = (int) (Math.floor((xStop - xStart) / xStep) + 1);
		int cols = (int) (Math.floor((yStop - yStart) / yStep) + 1);

		checkRasterPoints(gen);
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
		IPointGenerator<?> gen = pointGeneratorService.createGenerator(model, roi);
		// By argument of symmetry, radius 1 and step 1 can only have points on either (centre + 4 axes crossings) or (4
		// points at +-0.5 in x,y)
		int expectedSize = 5;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		// Check the points are correct and the order is maintained
        GeneratorUtil.testGeneratorPoints(gen, expectedSize);

        model.setBoundsToFit(true);
        gen = pointGeneratorService.createGenerator(model, roi);
		expectedSize = 4;

		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		// Check the points are correct and the order is maintained
		GeneratorUtil.testGeneratorPoints(gen, expectedSize);

        gen = pointGeneratorService.createGenerator(model);

        // Applying non-rectangular ROI flattens points, manually check points of
        checkRasterPoints(gen);

	}


	@Test
	public void testNestedNeXus() throws Exception {

		int[] sizes = {8,5};

		// Create scan points for a grid and make a generator
		TwoAxisGridStepModel rmodel = new TwoAxisGridStepModel("x", "y");
		rmodel.setxAxisName("xNex");
		// Allow for 1 point on end: 3/N gives N+1 points: 0, 3/N, 6/N ... 3(N/N), so 3/(N-1) -> 0, 3/(N-1)... 3(N-1/N-1) gives N points
		rmodel.setxAxisStep(3d/(sizes[1] - 1));
		rmodel.setyAxisName("yNex");
		rmodel.setyAxisStep(3d/(sizes[0] - 1));
		rmodel.setBoundingBox(new BoundingBox(0,0,3,3));

		IPointGenerator<TwoAxisGridStepModel> gen = pointGeneratorService.createGenerator(rmodel);
		checkRasterPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, sizes);
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
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
		final int expectedSize = 12;
		assertEquals(expectedSize , gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 4, 3 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		checkRasterPoints(gen);
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
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
		final int expectedSize = 9;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 3, 3 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		checkRasterPoints(gen);
	}

	void checkRasterPoints(IPointGenerator<?> gen) {

		final TwoAxisGridStepModel model;
		if (gen.getModel() instanceof CompoundModel) {
			model = (TwoAxisGridStepModel) ((CompoundModel) gen.getModel()).getModels().get(0);
		} else {
			model = (TwoAxisGridStepModel) gen.getModel();
		}

		final int fastAxisPoints = gen.getShape()[1];
		final int slowAxisPoints = gen.getShape()[0];
		assertThat(gen.size(), is(equalTo(fastAxisPoints * slowAxisPoints)));
		final double xStep = model.getxAxisStep();
		final double yStep = model.getyAxisStep();
		final double xStart = model.isBoundsToFit() ?
				model.getBoundingBox().getxAxisStart() + xStep / 2 : model.getBoundingBox().getxAxisStart();
		final double yStart = model.isBoundsToFit() ?
				model.getBoundingBox().getyAxisStart() + yStep / 2 : model.getBoundingBox().getyAxisStart();
		final String xName = model.getxAxisName();
		final String yName = model.getyAxisName();
		final List<IPosition> expectedPositions = new ArrayList<>();
		int index = 0;
		for (int slowAxisIndex = 0; slowAxisIndex < slowAxisPoints; slowAxisIndex ++) {
			final boolean isBackwards = model.isAlternating() && slowAxisIndex % 2 == 1;
			for (int fastAxisIndex = 0; fastAxisIndex < fastAxisPoints; fastAxisIndex ++) {
				if (isBackwards) {
					final int backwardsIndex = fastAxisPoints - 1 - fastAxisIndex;
					expectedPositions.add(new Point(xName, backwardsIndex, xStart + backwardsIndex * xStep, yName, slowAxisIndex, yStart + slowAxisIndex * yStep, index, true));
				} else {
					expectedPositions.add(new Point(xName, fastAxisIndex, xStart + fastAxisIndex * xStep, yName, slowAxisIndex, yStart + slowAxisIndex * yStep, index, true));
				}
				index ++;
			}
		}
		final Iterator<IPosition> generatedPositions = gen.iterator();
		gen.createPoints();
		for (int i = 0; i < expectedPositions.size(); i++) {
			assertThat(generatedPositions.next(), is(equalTo(expectedPositions.get(i))));
		}
	}

}
