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

package gda.device.detector.xmap.edxd;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EDXDElementTest {

	@Test
	public void testPolyFit() throws Exception {

		final EDXDElement e = new EDXDElement(null, 0);

		final double[] actual = {102.1,134.2,156.3};
		final double[] reported = {102, 134, 156};

		e.fitPolynomialToEnergyData(actual, reported);

		assertEquals(actual[0], e.createEnergyValue(reported[0]), 0.1);
		assertEquals(actual[1], e.createEnergyValue(reported[1]), 0.1);
		assertEquals(actual[2], e.createEnergyValue(reported[2]), 0.1);
	}

}
