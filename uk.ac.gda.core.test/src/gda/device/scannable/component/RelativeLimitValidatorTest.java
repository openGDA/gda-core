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

package gda.device.scannable.component;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableMotionBase;

import org.junit.Before;
import org.junit.Test;
//import static org.junit.Assert.*;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
public class RelativeLimitValidatorTest {
	
	ScannableMotionBase reference;
	private RelativeLimitValidator validator;
	private Scannable mockedHost;
	
	
	@Before
	public void setUp() {
		validator = new RelativeLimitValidator();
		mockedHost = mock(Scannable.class);
		when(mockedHost.getInputNames()).thenReturn(new String[] {"a", "b", "c", "d", "e", "f"});
		validator.setaName("c");
		validator.setaIndex(2);
		validator.setbName("e");
		validator.setbIndex(4);
	} 
	
	@Test
	public void testGetaIndex() {
		assertEquals(2, validator.getaIndex());
	}
	
	@Test
	public void testGetBIndex() {
		assertEquals(4, validator.getbIndex());
	}
	
	@Test
	public void testToStringNoLimits() {
		assertEquals("", validator.toString());
	}
	@Test
	public void testToStringLowerLimits() {
		validator.setMinimumDifference(5.);
		assertEquals("5.0 <= c - e", validator.toString());
	}
	@Test
	public void testToStringUpperLimits() {
		validator.setMaximumDifference(10.);
		assertEquals("c - e <= 10.0", validator.toString());
	}
	@Test
	public void testToStringBothLimits() {
		validator.setMinimumDifference(5.);
		validator.setMaximumDifference(10.);
		assertEquals("5.0 <= c - e <= 10.0", validator.toString());
	}
	
	@Test
	public void testCheckNoLimits() throws DeviceException {
		assertEquals(null, validator.checkInternalPosition(new Double[] {0., 1., 2., 3., 4., 5.}));
	}
	
	@Test
	public void testCheckLowerLimits() throws DeviceException {
		validator.setMinimumDifference(5.);
		assertEquals(null, validator.checkInternalPosition(new Double[] {0., 1., 20., 3., 4., 5.}));
		assertEquals(null, validator.checkInternalPosition(new Double[] {0., 1., 9., 3., 4., 5.}));
		assertEquals("Lower relative limit violation of '5.0 <= c - e', where c = 8.9 and e = 4", validator.checkInternalPosition(new Double[] {0., 1., 8.9, 3., 4., 5.}));
	}

	@Test
	public void testCheckUpperLimits() throws DeviceException {
		validator.setMinimumDifference(5.);
		validator.setMaximumDifference(10.);
		assertEquals(null, validator.checkInternalPosition(new Double[] {0., 1., 9., 3., 4., 5.}));
		assertEquals("Upper relative limit violation of '5.0 <= c - e <= 10.0', where c = 14.1 and e = 4", validator.checkInternalPosition(new Double[] {0., 1., 14.1, 3., 4., 5.}));
	}
	
}
