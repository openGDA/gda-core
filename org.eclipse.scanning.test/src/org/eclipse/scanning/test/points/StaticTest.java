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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.junit.jupiter.api.Test;

class StaticTest extends AbstractGeneratorTest {


	@Test
	void testSingleStatic() throws Exception {
		final StaticModel model = new StaticModel();
		final IPointGenerator<StaticModel> gen = pointGeneratorService.createGenerator(model);
		assertEquals(1, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[0], gen.getShape());

		final List<IPosition> positionList = gen.createPoints();
		assertEquals(1, positionList.size());
		final IPosition position = positionList.get(0);
		final IPosition expectedPosition = new StaticPosition();
		expectedPosition.setStepIndex(0);
		assertEquals(0, position.size());
		assertEquals(expectedPosition, position);
	}

	@Test
	void testMultipleStatic() throws Exception {
		final int size = 8;
		final StaticModel model = new StaticModel(size);
		final IPointGenerator<StaticModel> gen = pointGeneratorService.createGenerator(model);
		assertEquals(size, gen.size());
		assertEquals(1, gen.getRank());
		assertArrayEquals(new int[] { size }, gen.getShape());

		final List<IPosition> positionList = gen.createPoints();
		assertEquals(size, positionList.size());
		int i = 0;
		for (IPosition position : positionList) {
			StaticPosition expectedPosition = new StaticPosition(i);
			expectedPosition.setStepIndex(i++);
			assertEquals(0, position.size());
			assertEquals(expectedPosition, position);
			assertTrue(position instanceof StaticPosition);
		}
	}

	@Test
	void testCompoundStaticScanRank() throws Exception {
		final double exposureTime = 0.123;
		final IPointGenerator<?> outerGen = pointGeneratorService.createGenerator(new StaticModel());
		final CompoundModel innerCompoundModel = new CompoundModel(new StaticModel(10));
		innerCompoundModel.setDuration(exposureTime);
		IPointGenerator<?> innerGen = pointGeneratorService.createCompoundGenerator(innerCompoundModel);

		final IPosition outerPos = outerGen.getFirstPoint();
		assertEquals(1, outerPos.getScanRank());
		final IPosition innerPos = innerGen.createPoints().get(7);
		assertEquals(1, innerPos.getScanRank());
		assertEquals(7, innerPos.getIndex(0));
		assertEquals(exposureTime, innerPos.getExposureTime(), 1e-15);
		final IPosition overallPos = innerPos.compound(outerPos);
		assertTrue(outerPos instanceof StaticPosition);
		assertTrue(innerPos instanceof StaticPosition);
		assertTrue(overallPos instanceof StaticPosition);
		assertEquals(1, overallPos.getScanRank());
		assertEquals(7, overallPos.getIndex(0));
		assertEquals(exposureTime, overallPos.getExposureTime(), 1e-15);
	}

	@Test
	void testInvalidZeroSize() {
		StaticModel model = new StaticModel(0);
		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

	@Test
	void testInvalidNegativeSize() {
		StaticModel model = new StaticModel(-3);
		assertThrows(GeneratorException.class, () -> pointGeneratorService.createGenerator(model));
	}

}
