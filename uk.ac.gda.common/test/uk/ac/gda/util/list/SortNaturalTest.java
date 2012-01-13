/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.gda.util.list;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

public class SortNaturalTest {

	@Test
	public void testBasicSort() {
		String[] array = {"c", "b", "a"};
		String[] expected = {"a", "b", "c"};
		Collections.sort(Arrays.asList(array), new SortNatural<String>(false));	
		Assert.assertArrayEquals(expected, array);
	}
	
	@Test
	public void testNumericSort() {
		String[] array = new String[20];
		String[] expected = new String[20];
		for (int i = 0; i < 20; i++) {
			expected[i] = array[i] = Integer.toString(i);
		}
		// shuffle list by sorting in character order
		Collections.sort(Arrays.asList(array));	
		// sort the list in the natural way
		Collections.sort(Arrays.asList(array), new SortNatural<String>(false));	
		Assert.assertArrayEquals(expected, array);
		
	}
	
	@Test
	public void testStringsSort() {
		String[] array = {"element", "edge", "preEdgeTime", "exafsStepType", "b", "initialEnergy", "a", "preEdgeStep", "edgeStep", "exafsToTime", "exafsFromTime", "gaf2", "gaf1", "finalEnergy", "exafsTime", "KStart", "edgeTime", "KWeighting"};
		String[] expected = {"a", "b", "edge", "edgeStep", "edgeTime", "element", "exafsFromTime", "exafsStepType", "exafsTime", "exafsToTime", "finalEnergy", "gaf1", "gaf2", "initialEnergy", "KStart", "KWeighting", "preEdgeStep", "preEdgeTime"};
		Collections.sort(Arrays.asList(array), new SortNatural<String>(false));	
		Assert.assertArrayEquals(expected, array);
	}
	
	@Test
	public void testStringsSortCaseSensitive() {
		String[] array = {"element", "edge", "preEdgeTime", "exafsStepType", "b", "initialEnergy", "a", "preEdgeStep", "edgeStep", "exafsToTime", "exafsFromTime", "gaf2", "gaf1", "finalEnergy", "exafsTime", "KStart", "edgeTime", "KWeighting"};
		String[] expected = {"KStart", "KWeighting", "a", "b", "edge", "edgeStep", "edgeTime", "element", "exafsFromTime", "exafsStepType", "exafsTime", "exafsToTime", "finalEnergy", "gaf1", "gaf2", "initialEnergy", "preEdgeStep", "preEdgeTime"};
		Collections.sort(Arrays.asList(array), new SortNatural<String>(true));	
		Assert.assertArrayEquals(expected, array);
	}

}
