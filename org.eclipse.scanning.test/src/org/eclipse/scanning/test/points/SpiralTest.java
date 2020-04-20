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
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisSpiralModel;
import org.junit.Test;

public class SpiralTest extends AbstractGeneratorTest {

	@Test
	public void testSpiralNoROI() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-10);
		box.setyAxisStart(5);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		TwoAxisSpiralModel model = new TwoAxisSpiralModel("x", "y");
		model.setBoundingBox(box);

		// Get the point list
		IPointGenerator<TwoAxisSpiralModel> generator = service.createGenerator(model);

		final int expectedSize = 20;
		assertEquals(expectedSize, generator.size());
		assertEquals(1, generator.getRank());
		assertArrayEquals(new int[] { expectedSize }, generator.getShape());

		List<IPosition> pointList = generator.createPoints();
		assertEquals(expectedSize, pointList.size());

		assertTrue(pointList.get(0) instanceof Point);

		// Test a few points
		// TODO check x and y index values - currently these are not tested by AbstractPosition.equals()
		assertEquals(new Point("x", 0, -8.263367850554253, "y", 0, 6.678814432234913, 0, false), pointList.get(0));
		assertEquals(new Point("x", 3, -8.139330427516057, "y", 3, 7.991968780318976, 3, false), pointList.get(3));
		assertEquals(new Point("x", 15, -6.315009394139057, "y", 15, 7.399523826759042, 15, false), pointList.get(15));
	}

	@Test
	public void testSpiralNoROIWrtCompound() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-10);
		box.setyAxisStart(5);
		box.setxAxisLength(3);
		box.setyAxisLength(4);

		TwoAxisSpiralModel model = new TwoAxisSpiralModel("x", "y");
		model.setBoundingBox(box);

		checkWrtCompound(model, null, 20);
	}


	@Test
	public void testSpiralWrtCompound() throws Exception {

		RectangularROI roi = new RectangularROI(28.5684, 24.0729, 50.4328, 54.2378, 0.0);
		TwoAxisSpiralModel model = new TwoAxisSpiralModel("x", "y");
		model.setScale(2.0);

        checkWrtCompound(model, roi, 682);
	}

}
