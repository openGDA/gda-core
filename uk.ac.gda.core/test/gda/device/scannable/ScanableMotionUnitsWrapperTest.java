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

package gda.device.scannable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import gda.device.ScannableMotionUnits;

import org.junit.Before;
import org.junit.Test;

public class ScanableMotionUnitsWrapperTest {

	
	private ScannableMotionUnitsWrapper wrapper;
	private ScannableMotionUnits delegate;

	@Before
	public void setUp() {
		wrapper = new ScannableMotionUnitsWrapper();
		delegate = mock(ScannableMotionUnits.class);
		wrapper.setDelegate(delegate);
	}

	@Test
	public void testSetGetExtraNames() {
		when(delegate.getExtraNames()).thenReturn(new String[] {"a","b"});
		assertEquals(new String[] {"a","b"}, wrapper.getExtraNames());
		wrapper.setExtraNames(new String[] {"c","d"});
		assertEquals(new String[] {"c","d"}, wrapper.getExtraNames());
	}

	@Test
	public void testSetGetInputNames() {
		when(delegate.getInputNames()).thenReturn(new String[] {"a","b"});
		assertEquals(new String[] {"a","b"}, wrapper.getInputNames());
		wrapper.setInputNames(new String[] {"c","d"});
		assertEquals(new String[] {"c","d"}, wrapper.getInputNames());
	}
	
	@Test
	public void testSetGetName() {
		when(delegate.getName()).thenReturn("ab");
		assertEquals("ab", wrapper.getName());
		wrapper.setName("cd");
		assertEquals("cd", wrapper.getName());
	}

}
