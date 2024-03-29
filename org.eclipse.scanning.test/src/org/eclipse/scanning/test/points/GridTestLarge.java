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
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.junit.jupiter.api.Test;

class GridTestLarge extends AbstractGeneratorTest {

	@Test
	void testApprox10millIteratorTimeCircle() throws Exception {

		// Create a simple bounding rectangle
		CircularROI roi = new CircularROI(500, 500, 500);

		// Create a raster scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel();
		model.setyAxisPoints(3162);
		model.setxAxisPoints(3162);

		testIteratorTime(model, roi, 7852632, 20000);
	}

	@Test
	void test10millIteratorTimeRectangle() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		TwoAxisGridPointsModel model = new TwoAxisGridPointsModel();
		model.setyAxisPoints(1000);
		model.setxAxisPoints(10000);

		testIteratorTime(model, roi, 10000000, 20000);
	}


	private void testIteratorTime(TwoAxisGridPointsModel model, IROI roi, int size, long tenMilTime) throws Exception {

		long start = System.currentTimeMillis();

		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
        Iterator<IPosition>       it  = gen.iterator();

		long after1 = System.currentTimeMillis();

		assertTrue(start>(after1-50)); // Shouldn't take that long to make it!

		// Now iterate a few, shouldn't take that long
		int count = 0;
		while (it.hasNext()) {
			it.next();
			count++;
			if (count>10000) break;
		}

		long after2 = System.currentTimeMillis();
		assertTrue(after1>(after2-200)); // Shouldn't take that long to make it!

		while (it.hasNext()) { // 10mill!
			it.next();
			count++;
		}

		long after3 = System.currentTimeMillis();
		System.out.println("It took "+(after3-after2)+"ms to iterate "+size+" with "+roi.getClass().getSimpleName());
		if ((after3-after2)>tenMilTime) throw new Exception(" The time was longer than "+tenMilTime+" to iterate "+size+". It took "+(after3-after2));

		assertEquals(size, count);

	}

	@Test
	void test10millTimeInMemory() throws Exception {

		long start = System.currentTimeMillis();

		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		TwoAxisGridPointsModel gridScanPath = new TwoAxisGridPointsModel();
		gridScanPath.setyAxisPoints(1000);
		gridScanPath.setxAxisPoints(10000);

		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(gridScanPath, boundingRectangle);
		List<IPosition> points = gen.createPoints();

		assertEquals(10000000, points.size());

		long after = System.currentTimeMillis();

		System.out.println("It took "+(after-start)+"ms to make 10million Point and keep in memory");

	}

}
