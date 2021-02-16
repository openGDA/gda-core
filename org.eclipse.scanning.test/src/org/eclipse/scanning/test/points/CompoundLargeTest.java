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

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.IPosition;
import org.eclipse.scanning.api.points.models.AxialStepModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.points.PointGeneratorService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Appears to hang - or to run very slowly")
public class CompoundLargeTest {

	private IPointGeneratorService service;

	@Before
	public void before() throws Exception {
		service = new PointGeneratorService();
	}

	@Test
	public void test2Pow24() throws Exception {

		IPointGenerator<AxialStepModel> two = service.createGenerator(new AxialStepModel("Temperature", 290,291,1));
		assertEquals(2, two.size());
		assertEquals(1, two.getRank());
		assertArrayEquals(new int[] { 2 }, two.getShape());

		CompoundModel cModel = new CompoundModel();

		for (int i = 0; i < 24; i++) {
			cModel.addModel(new AxialStepModel("Temperature"+i, 290,291,1));
		}

		long start = System.currentTimeMillis();
		IPointGenerator<CompoundModel> scan = service.createCompoundGenerator(cModel);
		int size = scan.size();
		int expectedSize = (int) Math.pow(2, 24);
		assertEquals(expectedSize, size);
		assertEquals(24, scan.getRank());
		final int[] expectedShape = Collections.nCopies(24, 2).stream().mapToInt(Integer::intValue).toArray();
		assertArrayEquals(expectedShape, scan.getShape());

		long stage1 = System.currentTimeMillis();
		System.out.println("Size of "+size+" returned in "+(stage1-start)+" ms");

		Iterator<IPosition> it = scan.iterator();
		long stage2 = System.currentTimeMillis();
		System.out.println("Iterator returned in "+(stage2-stage1)+" ms");

		int sz=0;
		while(it.hasNext()) {
			it.next();
			sz++;
			if (sz>size) throw new Exception("Iterator grew too large!");
		}
		long stage3 = System.currentTimeMillis();
		System.out.println("Iterator size "+sz+" ran over in "+(stage3-stage2)+" ms");

	}

}
