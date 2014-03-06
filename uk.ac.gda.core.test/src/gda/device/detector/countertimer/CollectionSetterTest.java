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

package gda.device.detector.countertimer;


import junit.framework.TestCase;

/**
 * Tests that the newly-introduced setters for collection fields that are
 */
public class CollectionSetterTest extends TestCase {
	
	/**
	 */
	public void testCounterTimerBase() {
		gda.device.detector.DetectorBase ctb = new DummyCounterTimer();
		assertEquals(0, ctb.getExtraNames().length);
		ctb.setExtraNames(new String[]{"one", "two"});
		assertEquals(2, ctb.getExtraNames().length);
	}
	
}
