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
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class RasterTestLarge extends AbstractGeneratorTest {

	@Test
	void testApprox1millIteratorTimeCircle() throws Exception {

		// Create a simple bounding rectangle
		CircularROI roi = new CircularROI(500, 500, 500);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel();
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		testIteratorTime(model, roi, 785349, 10000, true);
	}

	@Test
	void test10millIteratorTimeRectangle() throws Exception {

		// Create a simple bounding rectangle
		RectangularROI roi = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel();
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		testIteratorTime(model, roi, 10011001, 5000, false); // TODO Is 10011001 correct?
	}


	private void testIteratorTime(TwoAxisGridStepModel model, IROI roi, int size, long tenMilTime, boolean testAllPoints) throws Exception {


		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, roi);
		if (testAllPoints) GeneratorUtil.testGeneratorPoints(gen);

		long start = System.currentTimeMillis();
        Iterator<IPosition>       it  = gen.iterator();

		long after1 = System.currentTimeMillis();

		assertTrue(start>(after1-50)); // Shouldn't take that long to make it!

		// Now iterate a few, shouldn't take that long
		int count = 0;
		while (it.hasNext()) {
			Point point = (Point) it.next();
			count++;
			if (count>10000) break;
		}

		long after2 = System.currentTimeMillis();
		assertTrue(after1>(after2-200)); // Shouldn't take that long to make it!

		while (it.hasNext()) { // 10mill!
			Point point = (Point) it.next();
			count++;
		}

		long after3 = System.currentTimeMillis();
		System.out.println("It took "+(after3-after2)+"ms to iterate "+size+" with "+roi.getClass().getSimpleName());
		assertTrue(after2>(after3-tenMilTime)); // Shouldn't take that long to make it!

		assertEquals(size, count);

	}

	@Disabled
	@Test
	void test10millTimeInMemory() throws Exception {

		long start = System.currentTimeMillis();

		// Create a simple bounding rectangle
		RectangularROI boundingRectangle = new RectangularROI(0, 0, 1000, 10000, 0);

		// Create a raster scan path
		TwoAxisGridStepModel model = new TwoAxisGridStepModel();
		model.setxAxisStep(1);
		model.setyAxisStep(1);

		// Get the point list
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createGenerator(model, boundingRectangle);
		List<IPosition> points = gen.createPoints();

		assertEquals(10011001, points.size()); // TODO Is 10011001 correct?

		long after = System.currentTimeMillis();

		System.out.println("It took "+(after-start)+"ms to make 10million Point and keep in memory");

	}
}
