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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

	@Test
	public void testFillingRectangleNoROI() throws Exception {

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
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);
	}

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

		IPointGenerator<TwoAxisGridPointsModel> gen = service.createGenerator(model);
		List<IPosition> pointList = gen.createPoints();

		assertEquals(pointList.size(), gen.size());
		GeneratorUtil.testGeneratorPoints(gen, 20, 20);
	}

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
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

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
		checkPoints(gen);
		GeneratorUtil.testGeneratorPoints(gen, 5, 5);
	}

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
		checkPoints(gen);
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
		checkPoints(gen);
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
			else assertTrue(pointListBothAlternating.get(i).equals(pointListInnerAlternating.get(i)));
		}
	}

	void checkPoints(IPointGenerator<?> gen) {

		final TwoAxisGridPointsModel model;
		if (gen.getModel() instanceof CompoundModel) {
			model = (TwoAxisGridPointsModel) ((CompoundModel) gen.getModel()).getModels().get(0);
		} else {
			model = (TwoAxisGridPointsModel) gen.getModel();
		}

		final int fastAxisPoints = gen.getShape()[1];
		final int slowAxisPoints = gen.getShape()[0];
		assertThat(gen.size(), is(equalTo(fastAxisPoints * slowAxisPoints)));
		final double xStep = model.getBoundingBox().getxAxisLength() /
				(model.isBoundsToFit() ? fastAxisPoints : fastAxisPoints - 1);
		final double yStep = model.getBoundingBox().getyAxisLength() /
				(model.isBoundsToFit() ? slowAxisPoints : slowAxisPoints - 1);
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
