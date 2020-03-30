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

import java.util.List;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.StaticPosition;
import org.eclipse.scanning.api.points.models.StaticModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Test;

public class StaticTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}

	@Test
	public void testSingleStatic() throws Exception {
		final StaticModel model = new StaticModel();
		final IPointGenerator<StaticModel> gen = service.createGenerator(model);
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
	public void testMultipleStatic() throws Exception {
		final int size = 8;
		final StaticModel model = new StaticModel(size);
		final IPointGenerator<StaticModel> gen = service.createGenerator(model);
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
		}
	}

	@Test(expected = GeneratorException.class)
	public void testInvalidZeroSize() throws Exception {
		StaticModel model = new StaticModel(0);
		service.createGenerator(model);
	}

	@Test(expected = GeneratorException.class)
	public void testInvalidNegativeSize() throws Exception {
		StaticModel model = new StaticModel(-3);
		service.createGenerator(model);
	}

}
