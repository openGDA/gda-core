/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import gda.device.DeviceException;
import gda.device.scannable.scannablegroup.ScannableGroupNamed;

public class ScannableGroupNamedTest extends ScannableGroupTest {

	private static final String[] EMPTY_SCANNABLE_NAME_ARRAY = new String[0];

	private ScannableGroupNamed sg2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		sg2 = new ScannableGroupNamed();
		sg = sg2;
	}

	@Test
	@Override
	public void testNewlyInstantiatedScannableGroup() throws DeviceException {
		assertEquals(EMPTY_SCANNABLE_LIST, sg2.getGroupMembers());
		assertArrayEquals(EMPTY_SCANNABLE_NAME_ARRAY, sg2.getGroupMembersNamesAsArray());
	}
}
