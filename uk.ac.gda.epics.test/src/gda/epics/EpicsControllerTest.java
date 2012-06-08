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

package gda.epics;

import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsControllerForScript;
import junit.framework.TestCase;

/**
 *
 */
public class EpicsControllerTest extends TestCase {

	/**
	 * 
	 */
	public void testEpicsControllerInstance() {
		EpicsController con = EpicsController.getInstance(false);
		EpicsController con2 = EpicsController.getInstance(false);
		assertTrue(con == con2);
	}

	/**
	 * 
	 */
	public void testEpicsControllerForScriptInstance() {
		EpicsControllerForScript con = EpicsControllerForScript.getInstance(false);
		EpicsControllerForScript con2 = EpicsControllerForScript.getInstance(false);
		assertTrue(con == con2);
	}

	/**
	 * 
	 */
	public void testEpicsControllerSynergy() {
		EpicsController con = EpicsController.getInstance(false);
		EpicsControllerForScript con2 = EpicsControllerForScript.getInstance(false);
		assertTrue(con != con2);
	}
}
