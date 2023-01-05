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
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.MapPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundsToFit;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.points.AbstractScanPointGenerator;
import org.junit.jupiter.api.Test;

class RasterTest extends AbstractGeneratorTest {

	/**
	 * Can create a model either with a BoundingBox, or with one or more ROIs, in which case the bounding box is
	 * constructed as the minimum bounding rectangle for all ROIs. Therefore, a boundingRectangle with corners (x1, y1),
	 * (x2, y2) should generate the same points as a bounding box with the same corners.
	 *
	 * @throws Exception
	 */
	@Test
	void testFillingBoundingRectangle() throws Exception {
		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");

		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, boundingRectangle);

		checkRasterPoints(gen);
        GeneratorUtil.testGeneratorPoints(gen, 4, 4);

		model.setBoundingBox(new BoundingBox(0, 0, 3, 3));
        assertThat(pointGeneratorService.createGenerator(model).createPoints(), is(equalTo(gen.createPoints())));

        // And for box that crosses origin
        boundingRectangle = new RectangularROI(-1.5, -1.5, 3, 3, 0);
        model.setxAxisStep(1);
        model.setyAxisStep(1);
        model.setBoundingBox(null);
        gen = pointGeneratorService.createGenerator(model, boundingRectangle);
        checkRasterPoints(gen);
        GeneratorUtil.testGeneratorPoints(gen, 4, 4);

		model.setBoundingBox(new BoundingBox(-1.5, -1.5, 3, 3));
        assertThat(pointGeneratorService.createGenerator(model).createPoints(), is(equalTo(gen.createPoints())));
	}

	/**
	 * Test behaviour with known series of points, to ensure behaviour of checkRasterPoints is as expected.
	 * @throws Exception
	 */
	@Test
	void testSimpleBox() throws Exception {

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

		List<Point> knownExpectedPositions = List.of(
				new Point("x", 0, 0, "y", 0, 0, 0, true),
				new Point("x", 1, 1, "y", 0, 0, 1, true),
				new Point("x", 2, 2, "y", 0, 0, 2, true),
				new Point("x", 3, 3, "y", 0, 0, 3, true),
				new Point("x", 4, 4, "y", 0, 0, 4, true),
				new Point("x", 5, 5, "y", 0, 0, 5, true),
				new Point("x", 0, 0, "y", 1, 1, 6, true));

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));
        GeneratorUtil.testGeneratorPoints(gen, 6, 6);
		checkRasterPoints(gen);

		model.setAlternating(true);
		gen = pointGeneratorService.createGenerator(model);
		knownExpectedPositions = List.of(
				new Point("x", 0, 0, "y", 0, 0, 0, true),
				new Point("x", 1, 1, "y", 0, 0, 1, true),
				new Point("x", 2, 2, "y", 0, 0, 2, true),
				new Point("x", 3, 3, "y", 0, 0, 3, true),
				new Point("x", 4, 4, "y", 0, 0, 4, true),
				new Point("x", 5, 5, "y", 0, 0, 5, true),
				new Point("x", 5, 5, "y", 1, 1, 6, true)); // Different from above!

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));

        GeneratorUtil.testGeneratorPoints(gen, 6, 6);
		checkRasterPoints(gen);

		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		knownExpectedPositions = List.of(
				new Point("x", 0, 0.5, "y", 0, 0.5, 0, true),
				new Point("x", 1, 1.5, "y", 0, 0.5, 1, true),
				new Point("x", 2, 2.5, "y", 0, 0.5, 2, true),
				new Point("x", 3, 3.5, "y", 0, 0.5, 3, true),
				new Point("x", 4, 4.5, "y", 0, 0.5, 4, true),
				new Point("x", 4, 4.5, "y", 1, 1.5, 5, true),
				new Point("x", 3, 3.5, "y", 1, 1.5, 6, true));

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));

        GeneratorUtil.testGeneratorPoints(gen, 5, 5);
		checkRasterPoints(gen);

	}

	/**
	 * Negative step should be allowed for a "negative" length, with points generated correct from the start -> stop and not in ascending order
	 * @throws Exception
	 */
	@Test
	void testNegativeStep() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(5);
		box.setyAxisStart(0);
		box.setxAxisLength(-5);
		box.setyAxisLength(5);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");

		model.setxAxisStep(-1);
		// Okay to do this here because there is "negative width"
		// for the points to protrude into.

		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridStepModel> gen = pointGeneratorService.createGenerator(model);
		checkRasterPoints(gen);
        GeneratorUtil.testGeneratorPoints(gen, 6, 6);

		List<Point> knownExpectedPositions = List.of(
				new Point("x", 0, 5, "y", 0, 0, 0, true),
				new Point("x", 1, 4, "y", 0, 0, 1, true),
				new Point("x", 2, 3, "y", 0, 0, 2, true),
				new Point("x", 3, 2, "y", 0, 0, 3, true),
				new Point("x", 4, 1, "y", 0, 0, 4, true),
				new Point("x", 5, 0, "y", 0, 0, 5, true),
				new Point("x", 0, 5, "y", 1, 1, 6, true));

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));

		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);

		knownExpectedPositions = List.of(
				new Point("x", 0, 4.5, "y", 0, 0.5, 0, true),
				new Point("x", 1, 3.5, "y", 0, 0.5, 1, true),
				new Point("x", 2, 2.5, "y", 0, 0.5, 2, true),
				new Point("x", 3, 1.5, "y", 0, 0.5, 3, true),
				new Point("x", 4, 0.5, "y", 0, 0.5, 4, true),
				new Point("x", 0, 4.5, "y", 1, 1.5, 5, true),
				new Point("x", 1, 3.5, "y", 1, 1.5, 6, true));

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));

		model.setAlternating(true);
		gen = pointGeneratorService.createGenerator(model);

		knownExpectedPositions = List.of(
				new Point("x", 0, 4.5, "y", 0, 0.5, 0, true),
				new Point("x", 1, 3.5, "y", 0, 0.5, 1, true),
				new Point("x", 2, 2.5, "y", 0, 0.5, 2, true),
				new Point("x", 3, 1.5, "y", 0, 0.5, 3, true),
				new Point("x", 4, 0.5, "y", 0, 0.5, 4, true),
				new Point("x", 4, 0.5, "y", 1, 1.5, 5, true),
				new Point("x", 3, 1.5, "y", 1, 1.5, 6, true));

		assertThat(gen.createPoints().subList(0, 7), contains(knownExpectedPositions.toArray()));

	}

	/**
	 * Negative step should not be allowed for region that is in positive direction, as Start + N * Step would never be >= Stop
	 * @throws Exception
	 */
	@Test
	void testBackwardsStep() {

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

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	/**
	 * Positive step should not be allowed for region that is in negative direction, as abs(Start + N * Step) would never be >= abs(Stop)
	 * @throws Exception
	 */
	@Test
	void testBackwardsBox() {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(-5);
		box.setyAxisLength(5);

		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");

		model.setxAxisStep(1);
		// Not okay to do this here because there is no "positive width"
		// for the points to protrude into.

		model.setyAxisStep(1);
		model.setBoundingBox(box);

		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	// Note this is a bit of a integration test not a strict unit test
	@Test
	void testFillingAMoreComplicatedBoundingRectangle() throws Exception {
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
	void testFillingACircle() throws Exception {
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

		// Check the points are correct and the order is maintained
        GeneratorUtil.testGeneratorPoints(gen, expectedSize);

        model.setBoundsToFit(true);
        gen = pointGeneratorService.createGenerator(model, roi);
		expectedSize = 4;

		// Check the points are correct and the order is maintained
		GeneratorUtil.testGeneratorPoints(gen, expectedSize);

        gen = pointGeneratorService.createGenerator(model);

        // Applying non-rectangular ROI flattens points, manually check points of
        checkRasterPoints(gen);

	}


	@Test
	// FIXME: Doesn't actually test NeXus? Appears never to have done, since initial git import
	void testNestedNeXus() throws Exception {

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
	void testFillingRectangleAwayFromOrigin() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(-10, 5, 2.5, 3.0, 0.0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
		final int expectedSize = 12;
		GeneratorUtil.testGeneratorPoints(gen, 4, 3);

		List<IPosition> pointList = gen.createPoints();
		assertThat(pointList.size(), is(equalTo(expectedSize)));

		checkRasterPoints(gen);
	}

	@Test
	void testFillingRectangleWithSnake() throws Exception {

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
		GeneratorUtil.testGeneratorPoints(gen, 3, 3);

		List<IPosition> pointList = gen.createPoints();
		assertThat(pointList.size(), is(equalTo(expectedSize)));

		checkRasterPoints(gen);
	}

	@Test
	void testStepLongerThanLength() throws Exception {

		// Create a simple bounding box
		BoundingBox box = new BoundingBox(0, 0, 3, 3);

		// Create a grid scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel("x", "y");
		model.setxAxisStep(2);
		model.setyAxisStep(3.5);
		model.setBoundingBox(box);

		// Get the point list
		IPointGenerator<TwoAxisGridStepModel> gen = pointGeneratorService.createGenerator(model);
		AbstractScanPointGenerator<TwoAxisGridStepModel> pointgen = (AbstractScanPointGenerator<TwoAxisGridStepModel>) gen;
		List<IPosition> knownExpectedPositions = List.of(
				new Point("x", 0, 0, 		"y", 0, 0, 0, true),
				new Point("x", 1, 2, 		"y", 0, 0, 1, true));
		assertThat(pointgen.initialBounds(), is(equalTo(new MapPosition(Map.of("x", -1.0, "y", 0.0)))));
		assertThat(pointgen.finalBounds(), is(equalTo(new MapPosition(Map.of("x", 3.0, "y", 0.0)))));

		assertThat(gen.createPoints(), is(equalTo(knownExpectedPositions)));
		GeneratorUtil.testGeneratorPoints(gen, 1, 2);

		model.setBoundsToFit(true);
		gen = pointGeneratorService.createGenerator(model);
		knownExpectedPositions = List.of(new Point("x", 0, 1, "y", 0, 1.5, 0, true));
		assertThat(gen.createPoints(), is(equalTo(knownExpectedPositions)));
		GeneratorUtil.testGeneratorPoints(gen, 1, 1);
		pointgen = (AbstractScanPointGenerator<TwoAxisGridStepModel>) gen;
		assertThat(pointgen.initialBounds(), is(equalTo(new MapPosition(Map.of("x", 0.0, "y", 1.5)))));
		assertThat(pointgen.finalBounds(), is(equalTo(new MapPosition(Map.of("x", 2.0, "y", 1.5)))));
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
		final double xStart = IBoundsToFit.getFirstPoint(model.getBoundingBox().getxAxisStart(), fastAxisPoints == 1, xStep, model.isBoundsToFit());
		final double yStart = IBoundsToFit.getFirstPoint(model.getBoundingBox().getyAxisStart(), slowAxisPoints == 1, yStep, model.isBoundsToFit());
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
		assertThat(gen.createPoints(), is(equalTo(expectedPositions)));
	}

}
