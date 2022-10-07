/*-
 * Copyright © 2016 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.diamond.daq.guigenerator.test;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.diamond.daq.guigenerator.ListStructuredContentProvider;

public class ListStructuredContentProviderTest {
	@Test
	public void testConvertsListToArray(){
		assertArrayEquals(
				new Object[]{1,2,3,4},
				new ListStructuredContentProvider().getElements(Arrays.asList(1,2,3,4))
		);
	}

	@Test
	public void testConvertsNullToEmptyArray(){
		assertArrayEquals(
				new Object[]{},
				new ListStructuredContentProvider().getElements(null)
		);
	}

	@Test
	public void testConvertsOtherThingsToEmptyArray(){
		assertArrayEquals(
				new Object[]{},
				new ListStructuredContentProvider().getElements("not a list")
		);
	}
}
