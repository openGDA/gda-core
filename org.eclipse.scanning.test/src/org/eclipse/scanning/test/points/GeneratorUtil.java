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

import java.util.Collections;

import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.scan.ScanInformation;

/**
 * @author Matthew Gerring
 *
 */
class GeneratorUtil {

	private GeneratorUtil() {
		// Private constructor to hide implicit public one
	}

	/**
	 * Checks the points list vs the iterator
	 * @param gen
	 * @param expectedShape
	 * @throws GeneratorException
	 */
	public static void testGeneratorPoints(IPointGenerator<?> gen, int... expectedShape) throws GeneratorException {

		// Check the estimator. In this case it is not doing anything
		// that we don't already know, so we can test it.
		final ScanInformation scanInfo = new ScanInformation(gen, Collections.emptySet(), null);
		assertThat("Different size from shape estimator!", scanInfo.getSize(), is(equalTo(gen.size())));

		if (expectedShape!=null && expectedShape.length>0) {// They set one
		    int[] shape = gen.getShape();
		    int size = 1;
			for (int dim : expectedShape) {
				size *= dim;
			}
			assertThat("Different size from expected!", gen.size(), is(equalTo(size)));
		    assertArrayEquals(expectedShape, shape);
		}
	}

}
