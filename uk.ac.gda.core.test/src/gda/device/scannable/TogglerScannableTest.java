/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.enumpositioner.DummyEnumPositioner;
import gda.device.scannable.TogglerScannable.Hook;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class TogglerScannableTest {

	Object posStart;
	Object posEnd;
	TogglerScannable toggler;
	private Scannable scn;

	@Before
	public void setup() {
		scn = mock(Scannable.class);
		posStart = new Object();
		posEnd = new Object();

		toggler = Mockito.spy( new TogglerScannable(scn) );
		toggler.setStartValue(posStart);
		toggler.setEndValue(posEnd);
	}

	@Test
	public void testHook() {
		Hook expected = Hook.AT_LEVEL;
		toggler.setHook(expected);
		Hook actual = toggler.getHook();
		assertEquals(expected, actual);
	}

	@Test
	public void testStartValue() {
		String expected = "test";
		toggler.setStartValue(expected);
		String actual = (String) toggler.getStartValue();
		assertEquals(expected, actual);
	}

	@Test
	public void testEndValue() {
		String expected = posEnd + "_testing";
		toggler.setEndValue(expected);
		String actual = (String) toggler.getEndValue();
		assertEquals(expected, actual);
	}

	@Test
	public void testScanHook() throws DeviceException {
		toggler.setHook(Hook.AT_LINE);
		toggler.atScanStart();
		verify(toggler, never()).togglePositionStart();
		reset(toggler);
		toggler.atScanLineStart();
		verify(toggler, times(1)).togglePositionStart();
		reset(toggler);
		toggler.atPointStart();
		verify(toggler, never()).togglePositionStart();
		reset(toggler);
		toggler.atLevelStart();
		verify(toggler, never()).togglePositionStart();
		reset(toggler);

		toggler.atLevelEnd();
		verify(toggler, never()).togglePositionEnd();
		reset(toggler);
		toggler.atPointEnd();
		verify(toggler, never()).togglePositionEnd();
		reset(toggler);
		toggler.atScanLineEnd();
		verify(toggler, times(1)).togglePositionEnd();
		reset(toggler);
		toggler.atScanEnd();
		verify(toggler, never()).togglePositionEnd();
		reset(toggler);
	}

	@Test
	public void testScanStartHooks() throws Exception {
		toggler.setHook(Hook.AT_SCAN);
		toggler.atScanStart();
		verify(scn).moveTo(posStart);
	}

	@Test
	public void testScanEndHooks() throws DeviceException {
		toggler.setHook(Hook.AT_SCAN);
		toggler.atScanEnd();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testLineStartHooks() throws DeviceException {
		toggler.setHook(Hook.AT_LINE);
		toggler.atScanLineStart();
		verify(scn).moveTo(posStart);
	}

	@Test
	public void testLineEndHooks() throws DeviceException {
		toggler.setHook(Hook.AT_LINE);
		toggler.atScanLineEnd();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testPointStartHooks() throws DeviceException {
		toggler.setHook(Hook.AT_POINT);
		toggler.atPointStart();
		verify(scn).moveTo(posStart);
	}

	@Test
	public void testPointEndHooks() throws DeviceException {
		toggler.setHook(Hook.AT_POINT);
		toggler.atPointEnd();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testLevelStartHooks() throws DeviceException {
		toggler.setHook(Hook.AT_LEVEL);
		toggler.atLevelStart();
		verify(scn).moveTo(posStart);
	}

	@Test
	public void testLevelEndHooks() throws DeviceException {
		toggler.setHook(Hook.AT_LEVEL);
		toggler.atLevelEnd();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testCommandFailure() throws DeviceException {
		toggler.atCommandFailure();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testStop() throws DeviceException {
		toggler.stop();
		verify(scn).moveTo(posEnd);
	}

	@Test
	public void testGetPosition() throws DeviceException {
		Object actual = toggler.getPosition();
		assertNull(actual);
	}

	@Test
	public void testAsynchronousMoveTo() throws DeviceException {
		try {
			toggler.asynchronousMoveTo(posStart);
			fail("Require DeviceException with message indicating unsupported operation");
		} catch (DeviceException e) {
			if ( ! e.getMessage().toLowerCase().contains( "unsupported" ) ) throw e;
		}
	}

	@Test
	public void testExtraNames() {
		String[] names = toggler.getExtraNames();
		assert( names == null || names.length == 0 );
	}

	@Test
	public void testInputNames() {
		String[] names = toggler.getInputNames();
		assert( names == null || names.length == 0 );
	}

}
