/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.scan;

import java.util.Vector;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.scan.ScanBase;
import junit.framework.TestCase;

public class ScanBaseTest extends TestCase {
	
	public void testReorderScannables() {
		
		// create out-of-order list of Scannables
		Vector<Scannable> scannables = new Vector<Scannable>();
		scannables.add(new DummyScannable(5));
		scannables.add(new DummyScannable(3));
		scannables.add(new DummyScannable(4));
		scannables.add(new DummyScannable(1));
		scannables.add(new DummyScannable(2));
		
		// create scan containing scannables
		DummyScan scan = new DummyScan();
		scan.setScannables(scannables);
		
		// reorder the scannables
		scan.reorderScannables();

		// ensure scannables are now ordered by level
		for (int i=0; i<scan.getScannables().size(); i++) {
			assertEquals(scan.getScannables().get(i).getLevel(), i+1);
		}
	}
	
	/**
	 * A simple {@link Scannable} implementation for testing purposes.
	 */
	static class DummyScannable extends ScannableBase {

		/**
		 * Creates a {@link DummyScannable} with the specified level.
		 * 
		 * @param level the level
		 */
		public DummyScannable(int level) {
			setLevel(level);
		}
		
		@Override
		public void asynchronousMoveTo(Object position) throws DeviceException {
			// do nothing
		}

		@Override
		public Object getPosition() throws DeviceException {
			return null;
		}

		@Override
		public boolean isBusy() throws DeviceException {
			return false;
		}
		
	}
	
	/**
	 * A simple {@link Scan} implementation for testing purposes.
	 */
	static class DummyScan extends ScanBase {
		
		private static final long serialVersionUID = 1L;

		@Override
		public void doCollection() throws Exception {
			// do nothing
		}
		
	}

}
