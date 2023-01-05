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

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.Scalar;
import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.junit.jupiter.api.Test;

class ArrayTest extends AbstractGeneratorTest {

	void testArray() throws GeneratorException {
		var generator = pointGeneratorService.createGenerator(new AxialArrayModel("x", 1.0, 2.0 ,3.0, 4.0, 5.0));

		// Get the point list
		assertEquals(5, generator.size());
		assertEquals(1, generator.getRank());
		assertArrayEquals(new int[] { 5 }, generator.getShape());
		List<IPosition> pointList = generator.createPoints();

		assertEquals(5, pointList.size());

		// Test a few points
		assertEquals(new Scalar<>("x", 0, 1.0), pointList.get(0));
		assertEquals(new Scalar<>("x", 1, 2.0), pointList.get(1));
		assertEquals(new Scalar<>("x", 2, 3.0), pointList.get(2));
		assertEquals(new Scalar<>("x", 3, 4.0), pointList.get(3));
		assertEquals(new Scalar<>("x", 4, 5.0), pointList.get(4));
		assertTrue(pointList.get(0) instanceof Scalar);
	}

	@Test
	void testExposureSet() throws GeneratorException {
		var compoundModel = new CompoundModel(new AxialArrayModel("x", 1, 4, 6));
		compoundModel.setDuration(8);
		IPointGenerator<CompoundModel> gen = pointGeneratorService.createCompoundGenerator(compoundModel);
		IPosition firstPos = gen.getFirstPoint();
		assertEquals(8, firstPos.getExposureTime(), 0.01);
	}

}
