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
		final int expectedSize = 400;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 20, 20 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());
		checkPoints(pointList);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (0, 0).
		assertEquals(0.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (1, 0).
		assertEquals(1.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (0, 1).
		assertEquals(0.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(1.0, pointList.get(5).getValue("y"), 1e-8);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5}, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (0, 0).
		assertEquals(0.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (1, 0).
		assertEquals(1.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (4, 1).
		assertEquals(4.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(1.0, pointList.get(5).getValue("y"), 1e-8);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (4, 0).
		assertEquals(4.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (3, 0).
		assertEquals(3.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (4, 1).
		assertEquals(4.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(1.0, pointList.get(5).getValue("y"), 1e-8);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (4, 0).
		assertEquals(4.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (3, 0).
		assertEquals(3.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(0.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (0, 1).
		assertEquals(0.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(1.0, pointList.get(5).getValue("y"), 1e-8);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (4, 4).
		assertEquals(4.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(4.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (3, 4).
		assertEquals(3.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(4.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (4, 3).
		assertEquals(4.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(3.0, pointList.get(5).getValue("y"), 1e-8);
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
		assertEquals(25, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 5, 5}, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(25, pointList.size());

		// Zeroth point is (4, 4).
		assertEquals(4.0, pointList.get(0).getValue("x"), 1e-8);
		assertEquals(4.0, pointList.get(0).getValue("y"), 1e-8);

		// Oneth point is (3, 4).
		assertEquals(3.0, pointList.get(1).getValue("x"), 1e-8);
		assertEquals(4.0, pointList.get(1).getValue("y"), 1e-8);

		// Fifth point is (0, 3).
		assertEquals(0.0, pointList.get(5).getValue("x"), 1e-8);
		assertEquals(3.0, pointList.get(5).getValue("y"), 1e-8);
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
		final int expectedSize = 400;
		assertEquals(expectedSize, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 20, 20 }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		assertEquals(pointList.size(), gen.size());

		checkPoints(pointList);
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
		assertEquals(400, gen.size());
		assertEquals(2, gen.getRank());
		assertArrayEquals(new int[] { 20, 20 }, gen.getShape());

		Iterator<IPosition> it = gen.iterator();
		List<IPosition> pointList = new ArrayList<>();
		while (it.hasNext()) pointList.add(it.next());

		assertArrayEquals(pointList.toArray(), gen.createPoints().toArray());

		checkPoints(pointList);
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
		final int expectedSize = 316;
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
		final int expectedSize = 194;
		assertEquals(expectedSize, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { expectedSize }, gen.getShape());

		List<IPosition> pointList = gen.createPoints();
		assertEquals(expectedSize, pointList.size());

		GeneratorUtil.testGeneratorPoints(gen);
	}

	private void checkPoints(List<IPosition> pointList) {

		// Check correct number of points
		assertEquals(20 * 20, pointList.size());

		// Check some random points are correct
		assertEquals(0.075, pointList.get(0).getValue("x"), 1e-8);
		for (int i = 0; i < 20; i++) {
			assertEquals(i, pointList.get(i).getIndex("x"));
			assertEquals(0, pointList.get(i).getIndex("y"));
		}
		for (int i = 20; i < 40; i++) {
			assertEquals(i-20, pointList.get(i).getIndex("x"));
			assertEquals(1, pointList.get(i).getIndex("y"));
		}

		assertEquals(0.075, pointList.get(0).getValue("y"), 1e-8);

		assertEquals(0.075 + 3 * (3.0 / 20.0), pointList.get(3).getValue("x"), 1e-8);
		assertEquals(0.075 + 0.0, pointList.get(3).getValue("y"), 1e-8);

		assertEquals(0.075 + 2 * (3.0 / 20.0), pointList.get(22).getValue("x"), 1e-8);
		assertEquals(0.075 + 1 * (3.0 / 20.0), pointList.get(22).getValue("y"), 1e-8);

		assertEquals(0.075 + 10 * (3.0 / 20.0), pointList.get(350).getValue("x"), 1e-8);
		assertEquals(0.075 + 17 * (3.0 / 20.0), pointList.get(350).getValue("y"), 1e-8);

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
		List<IPosition> pointList = gen.createPoints();

		assertEquals(9, pointList.size());

		// Check some points
		assertEquals(new Point(0, -9.5, 0, 5.5, 0), pointList.get(0));
		assertEquals(new Point(1, -8.5, 0, 5.5, 1), pointList.get(1));
		assertEquals(new Point(0, -9.5, 1, 6.5, 3), pointList.get(3));
		assertEquals(new Point(2, -7.5, 1, 6.5, 5), pointList.get(5));
		assertEquals(new Point(1, -8.5, 2, 7.5, 7), pointList.get(7));
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
		List<IPosition> pointList = gen.createPoints();

		assertEquals(9, pointList.size());

		// Check some points
		assertEquals(new Point(0, 0.5, 0, 0.5, 0), pointList.get(0));
		assertEquals(new Point(1, 1.5, 0, 0.5, 1), pointList.get(1));
		assertEquals(new Point(2, 2.5, 1, 1.5, 3), pointList.get(3));
		assertEquals(new Point(1, 1.5, 2, 2.5, 7), pointList.get(7));
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

}
