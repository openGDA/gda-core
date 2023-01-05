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

import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.BoundingBox;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridPointsRandomOffsetModel;
import org.junit.jupiter.api.Test;

class RandomOffsetGridTest extends AbstractGeneratorTest {

	@Test
	void testSimpleBox() throws Exception {

		BoundingBox box = new BoundingBox();
		box.setxAxisStart(-2);
		box.setyAxisStart(-2);
		box.setxAxisLength(4);
		box.setyAxisLength(10);

		final double offsetScale = 0.25;

		TwoAxisGridPointsRandomOffsetModel rm = new TwoAxisGridPointsRandomOffsetModel("x", "y");
		rm.setyAxisPoints(5);
		rm.setxAxisPoints(5);
		rm.setBoundingBox(box);
		rm.setSeed(10);
		rm.setOffset(offsetScale * 100);

		TwoAxisGridPointsModel gm = new TwoAxisGridPointsModel("x", "y");
		gm.setyAxisPoints(5);
		gm.setxAxisPoints(5);
		gm.setBoundingBox(box);

		IPointGenerator<TwoAxisGridPointsRandomOffsetModel> r = pointGeneratorService.createGenerator(rm);
		IPointGenerator<TwoAxisGridPointsModel> g = pointGeneratorService.createGenerator(gm);
		final int expectedSize = 25;
		assertEquals(expectedSize, g.size());
		assertEquals(2, g.getRank());
		assertArrayEquals(new int[] { 5, 5 }, g.getShape());

		for (Iterator<IPosition> it1 = r.iterator(), it2 = g.iterator(); it1.hasNext() && it2.hasNext();) {
			IPosition t1 = it1.next();
			IPosition t2 = it2.next();
			assertEquals(t1.getIndices(), t2.getIndices());
			assertEquals(t1.getNames(), t2.getNames());
			assertEquals(t1.getValue("x"), t2.getValue("x"), offsetScale); // Steps in fast axis have length 1, so offset <= offsetScale
			assertEquals(t1.getValue("y"), t2.getValue("y"), offsetScale);
		}
	}

}
