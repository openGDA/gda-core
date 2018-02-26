/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class JythonStatusTest {

	@Test
	public void testEnumOrder() {
		// Since the ordinal values of JythonStatus are used in the CORBA Jython interface,
		// we must ensure that the order does not change.
		JythonStatus[] stati = JythonStatus.values();
		assertEquals(stati[0], JythonStatus.IDLE);
		assertEquals(stati[1], JythonStatus.PAUSED);
		assertEquals(stati[2], JythonStatus.RUNNING);
	}

}
