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
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.PolygonalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.AbstractPosition;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.junit.Test;

public class GridTest extends AbstractGeneratorTest {

	/**
	 * When passed no ROI but are passed a BoundingBox and not alternating, use limits of bounding box.
	 * @throws GeneratorException
	 */
	@Test
	public void testFillingRectangleNoROI() throws GeneratorException {

		// Create a grid scan model
		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		// Get the point list
		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		double step = 3/19.0;
		List<IPosition> knownExpectedPositions = IntStream.range(0, 41).mapToObj(i ->
		// i % xAxisPoints = n for x = start + n * step; i / 20 = n for y = start + n * step: start(x,y) = (0, 0)
			new Point("x", i % 20, i % 20 * step, "y", i / 20, (i / 20) * step, i, true)).collect(Collectors.toList());
		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		double boundsStep = 3/20.0;
		double start = boundsStep / 2;
		knownExpectedPositions = IntStream.range(0, 41).mapToObj(i ->
		// i % xAxisPoints = n for x = start + n * step; i / 20 = n for y = start + n * step: start(x,y) = (step/2, step/2)
			new Point("x", i % 20, start + (i % 20) * boundsStep, "y", i / 20, start + (i / 20) * boundsStep, i, true)).collect(Collectors.toList());
		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);
	}


	/**
	 * Negative numbers of points has no physical meaning, we should throw an exception before we throw a PyException
	 * @throws Exception
	 */
	@Test(expected = GeneratorException.class)
	public void testNegativeRowCount() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(-20);  // An unsigned integer type would solve this problem...
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		service.createGenerator(model);
	}

	/**
	 * Test with known points to ensure testPoints behaviour proper
	 */
	@Test
	public void testSimpleBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-0.5);
		box.setyAxisStart(-0.5);
		box.setxAxisLength(5);
		box.setyAxisLength(5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		List<IPosition> knownExpectedPositions = IntStream.range(0, 24).mapToObj(i ->
		// start(x, y) = (-0.5, -0.5), step(x, y) = (5/4, 5/4), stop = (4.5, 4.5). 5x5=25 points
		new Point("x", i % 5, -0.5 + (i % 5) * 1.25, "y", i / 5, -0.5 + (i / 5) * 1.25, i, true)).collect(Collectors.toList());
		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model);
		knownExpectedPositions = IntStream.range(0, 24).mapToObj(i ->
		// start(x, y) = (-0.5 + step/2, -0.5 + step/2), step(x, y) = (5/5, 5/5), stop = (4.5 - step/2, 4.5 - step/2). 5x5=25 points
		new Point("x", i % 5, 0 + (i % 5) * 1, "y", i / 5, 0 + (i / 5) * 1, i, true)).collect(Collectors.toList());
		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * Test with alternating behaviour
	 */
	@Test
	public void testSimpleBoxSnake() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-0.5);
		box.setyAxisStart(-0.5);
		box.setxAxisLength(5);
		box.setyAxisLength(5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setAlternating(true);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);

		// Stepping forward
		List<IPosition> expectedPositions = new ArrayList<>(IntStream.range(0, 5).mapToObj(i ->
		// start(x, y) = (-0.5, -0.5), step(x, y) = (5/4, 5/4), stop = (4.5, 4.5). 5x5=25 points
		new Point("x", i, -0.5 + (i % 5) * 1.25, "y", 0, -0.5, i, true)).collect(Collectors.toList()));
		// Stepping backwards
		expectedPositions.addAll(IntStream.range(0, 5).mapToObj(i ->
		// x-index and position descending, point index increasing.
		new Point("x", 4 - i, 4.5 - i * 1.25, "y", 1, -0.5 + 1.25, 5 + i, true)).collect(Collectors.toList()));


		checkPoints(gen, expectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * Test behaviour consistent when stepping towards negative infinity
	 */
	@Test
	public void testBackwardsBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(4.5);
		box.setyAxisStart(-0.5);
		box.setxAxisLength(-5);
		box.setyAxisLength(5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * Test behaviour consistent when stepping towards negative infinity and alternating successive passes
	 */
	@Test
	public void testBackwardsBoxSnake() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(4.5);
		box.setyAxisStart(-0.5);
		box.setxAxisLength(-5);
		box.setyAxisLength(5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setAlternating(true);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * Behaviour with box axes moving towards negative
	 */
	@Test
	public void testDoublyBackwardsBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(4.5);
		box.setyAxisStart(4.5);
		box.setxAxisLength(-5);
		box.setyAxisLength(-5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * And snaked
	 */
	@Test
	public void testDoublyBackwardsBoxSnake() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(4.5);
		box.setyAxisStart(4.5);
		box.setxAxisLength(-5);
		box.setyAxisLength(-5);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(5);
		model.setxAxisPoints(5);
		model.setAlternating(true);
		model.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

	/**
	 * Test that when passed a ROI instead of a boundingBox, an equivalent bounding box is created
	 */
	@Test
	public void testFillingRectangle() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setyAxisName("y");
		model.setxAxisName("x");

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, roi);

		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);
	}

	/**
	 * Ensure points are same when generated by "bare" grid generator (python compound with two lines)
	 * And wrapped in Java "compound" generator (python compound with two lines)
	 */
	@Test
	public void testGridWrtCompound() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setyAxisName("y");
		model.setxAxisName("x");

		checkWrtCompound(model, roi, 400);
	}

	/**
	 * Ensure gen.createPoints() and gen.iterator() give same positions
	 */
	@Test
	public void testFillingRectangleIterator() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a raster scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setyAxisName("y");
		model.setxAxisName("x");

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, roi);

		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);
	}

	/**
	 * Test for non-rectangular ROI: bounding box should be minimum rectangle to fit ROI
	 * And then points outside of circle removes
	 */
	@Test
	public void testFillingCircle() throws Exception {

		// Create a circle region
		CircularROI circle = new CircularROI(1.5, 1.5, 1.5);

		// Create a raster scan path
		TwoAxisGridPointsModel gridScanPath = new TwoAxisGridPointsModel("x", "y");
		gridScanPath.setyAxisPoints(20);
		gridScanPath.setxAxisPoints(20);
		gridScanPath.setyAxisName("y");
		gridScanPath.setxAxisName("x");

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(gridScanPath, circle);
		final int expectedSize = 276;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		GeneratorUtil.testGeneratorPoints(gen);
	}

	/**
	 * Behaves consistently when spacing different between axes
	 */
	@Test
	public void testFillingBoundingCircleSkewed() throws Exception {

		// Create a circle region
		CircularROI circle = new CircularROI(1.5, 1.5, 15);

		// Create a raster scan path
		TwoAxisGridPointsModel gridScanPath = new TwoAxisGridPointsModel("x", "y");
		gridScanPath.setBoundsToFit(true);
		gridScanPath.setyAxisPoints(20);
		gridScanPath.setxAxisPoints(200);

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(gridScanPath, circle);
		final int expectedSize = 3156;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
//		GeneratorUtil.testGeneratorPoints(gen); // Rounding error in here causing test to fail
		Point[] points = {new Point(0, 1.0425, 0, 13.575),
				new Point(1, 1.0575, 1, 13.575),
				new Point(1000, 0.2625, 1000, 14.625),
				new Point(2000, 0.4125, 2000, 15.375),
				new Point(3100, 1.1325, 3100, 16.425) };
		for (Point p : points) {
			ArrayList<Set<String>> names = new ArrayList<Set<String>>();
			names.add(new LinkedHashSet<>(Arrays.asList(new String[] {"x", "y"})));
			p.setDimensionNames(names);
			p.setStepIndex(p.getIndex(0));
		}
		assertEquals(points[0], pointList.get(0));
		assertEquals(points[1], pointList.get(1));
		assertEquals(points[2], pointList.get(1000));
		assertEquals(points[3], pointList.get(2000));
		assertEquals(points[4], pointList.get(3100));
	}

	/**
	 * BoundingBox = minimum bounding rectangle of ROI
	 * Remove points outside of ROI
	 */
	@Test
	public void testFillingPolygon() throws Exception {

		PolygonalROI diamond = new PolygonalROI(new double[] { 1.5, 0 });
		diamond.insertPoint(new double[] { 3, 1.5 });
		diamond.insertPoint(new double[] { 1.5, 3 });
		diamond.insertPoint(new double[] { 0, 1.5 });

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(0);
		box.setyAxisStart(0);
		box.setxAxisLength(3);
		box.setyAxisLength(3);

		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(20);
		model.setxAxisPoints(20);
		model.setBoundingBox(box);

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, diamond);
		final int expectedSize = 180;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		GeneratorUtil.testGeneratorPoints(gen);
	}

	/**
	 * Known behaviour and points for testing testPoints
	 */
	@Test
	public void testFillingRectangleAwayFromOrigin() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(-10.0, 5.0, 3.0, 3.0, 0.0);

		// Create a grid scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(3);
		model.setxAxisPoints(3);
		model.setyAxisName("y");
		model.setxAxisName("x");

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, roi);
		List<IPosition> knownExpectedPositions = List.of(
				new Point("x", 0, -10, 			"y", 0, 5, 			0, true),
				new Point("x", 1, -10 + 3/2.0, 	"y", 0, 5, 			1, true),
				new Point("x", 2, -7, 			"y", 0, 5, 			2, true),
				new Point("x", 0, -10, 			"y", 1, 5 + 3/2.0, 	3, true),
				new Point("x", 1, -10 + 3/2.0, 	"y", 1, 5 + 3/2.0, 	4, true),
				new Point("x", 2, -7, 			"y", 1, 5 + 3/2.0, 	5, true),
				new Point("x", 0, -10, 			"y", 2, 8, 			6, true));
		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 3, 3);
	}

	@Test
	public void testFillingRectangleWithSnake() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 3, 3, 0);

		// Create a grid scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setyAxisPoints(3);
		model.setxAxisPoints(3);
		model.setAlternating(true);

		// Get the point list
		IPointGenerator<CompoundModel> gen = service.createGenerator(model, roi);
		List<IPosition> knownExpectedPositions = List.of(
				new Point("x", 0, 0, 		"y", 0, 0, 0, true),
				new Point("x", 1, 3/2.0, 	"y", 0, 0, 1, true),
				new Point("x", 2, 3, 		"y", 0, 0, 2, true),
				new Point("x", 2, 3, 		"y", 1, 3/2.0, 3, true),
				new Point("x", 1, 3/2.0, 	"y", 1, 3/2.0, 4, true),
				new Point("x", 0, 0, 		"y", 1, 3/2.0, 5, true),
				new Point("x", 0, 0, 		"y", 2, 3, 6, true));

		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 3, 3);

		model.setBoundsToFit(true);
		gen = service.createGenerator(model, roi);
		knownExpectedPositions = List.of(
				new Point("x", 0, 0.5, 	"y", 0, 0.5, 0, true),
				new Point("x", 1, 1.5, 	"y", 0, 0.5, 1, true),
				new Point("x", 2, 2.5, 	"y", 0, 0.5, 2, true),
				new Point("x", 2, 2.5, 	"y", 1, 1.5, 3, true),
				new Point("x", 1, 1.5, 	"y", 1, 1.5, 4, true),
				new Point("x", 0, 0.5, 	"y", 1, 1.5, 5, true),
				new Point("x", 0, 0.5, 	"y", 2, 2.5, 6, true));

		checkPoints(gen, knownExpectedPositions);
		GeneratorUtil.testGeneratorPoints(gen, 3, 3);
	}

	/**
	 * Odin detector requires that when a scan is alternating, only the innermost axis is alternating, while
	 * otherwise we want the inner 2 axes to alternate to so that we don't have to move those axes while the
	 * outer axis is moving.
	 * @throws GeneratorException
	 */
	@Test
	public void testAlternatingBothAxes() throws GeneratorException {

		// Create a grid scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel("x", "y");
		model.setBoundingBox(new BoundingBox(0, 0, 5, 5));
		model.setyAxisPoints(3);
		model.setxAxisPoints(3);
		model.setAlternating(true);

		// Create an outer axis
		AxialArrayModel outerModel = new AxialArrayModel("z", 1, 2, 3);
		CompoundModel compoundModel = new CompoundModel(outerModel, model);

		// 1->2->3 z-move 9<-8<-7 z-move 1->2->3
		// 6<-5<-4        4->5->6        6<-5<-4
		// 7->8->9        3<-2<-1        7->8->9
		List<IPosition> pointListBothAlternating = service.createGenerator(compoundModel).createPoints();
		model.setAlternateBothAxes(false);
		// 1->2->3 z-move 3<-2<-1 z-move 1->2->3
		// 6<-5<-4        4->5->6        6<-5<-4
		// 7->8->9        9<-8<-7        7->8->9
		// L-> R @ end => R->L @ start
		List<IPosition> pointListInnerAlternating = service.createGenerator(compoundModel).createPoints();
		List<IPosition> expected = new ArrayList<>();

		for (int i : new int[] {15, 16, 17, 12, 13, 14, 9, 10, 11}) {
			expected.add(pointListInnerAlternating.get(i));
		}

		for (int i = 0; i < 27; i++) {
			// First 9  and last 9 points same
			if (i > 8 && i < 18) {
				// StepIndex won't match
				assertTrue(((AbstractPosition) pointListBothAlternating.get(i)).equals(expected.get(i - 9), false));
			}
			else assertThat(pointListBothAlternating.get(i), is(equalTo(pointListInnerAlternating.get(i))));
		}
	}

	/**
	 *
	 * @param gen
	 */
	void checkPoints(IPointGenerator<?> gen) {
		final TwoAxisGridPointsModel model;
		if (gen.getModel() instanceof CompoundModel) {
			model = (TwoAxisGridPointsModel) ((CompoundModel) gen.getModel()).getModels().get(0);
		} else {
			model = (TwoAxisGridPointsModel) gen.getModel();
		}

		final int fastAxisPoints = model.getxAxisPoints();
		final int slowAxisPoints = model.getyAxisPoints();
		assertArrayEquals(new int[]{ fastAxisPoints, slowAxisPoints }, gen.getShape());
		assertThat(gen.size(), is(equalTo(fastAxisPoints * slowAxisPoints)));
	}

	/**
	 * Check the first N positions of a generator are a known series of points
	 * @param gen
	 * @param firstNpositions
	 */
	void checkPoints(IPointGenerator<?> gen, List<IPosition> firstNpositions) {
		checkPoints(gen);
		checkPoints(firstNpositions, gen.createPoints(), true);

	}

	void checkPoints(List<? extends IPosition> expected, List<? extends IPosition> actual, boolean hasNext) {
		if (hasNext) {
			assertTrue(actual.containsAll(expected));
		} else {
			assertEquals(expected, actual);
		}
	}

}
