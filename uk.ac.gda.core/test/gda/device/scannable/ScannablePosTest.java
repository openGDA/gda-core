/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import gda.device.DeviceException;

import org.junit.Test;

/**
 *
 */
public class ScannablePosTest {

	private class BareScannable extends ScannableBase {
		int getPosUsage = 0;
		
		@Override
		public void asynchronousMoveTo(Object position) throws DeviceException {
			return;
		}

		@Override
		public Object getPosition() throws DeviceException {
			getPosUsage++;
			return 0;
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
		
		public int reportGetPosUsage() {
			return getPosUsage;
		}
	}


	/**
	 * This test ensures that getPosition is called exactly once per pos call
	 * (getPosition is a potentially expensive operation).
	 * 
	 */
	@Test
	public void testPos() {
		try {
			BareScannable scannable = new BareScannable();
			scannable.setName("value");
			gda.jython.commands.ScannableCommands.pos(scannable);
			assertEquals(1,scannable.reportGetPosUsage());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
}
