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

package gda.device.enumpositioner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import gda.device.DeviceException;

public class EnumPositionerBaseTest {

	private TestEnumPositionerBase positioner;

	class TestEnumPositionerBase extends EnumPositionerBase {
		private String position;

		public TestEnumPositionerBase() throws Exception {
			setPositionsInternal(Arrays.asList("pos1", "pos2", "pos3"));
			setName("positionerName");
			this.position = "pos1";
			configure();
		}

		@Override
		public Object getPosition() throws DeviceException {
			return position;
		}

		public void setPosition(String newPos) {
			this.position = newPos;
		}
	}

	@Before
	public void before() throws Exception {
		positioner = new TestEnumPositionerBase();
	}

	@Test
	public void testOutputFormatting() throws Exception {
		assertThat(positioner.toFormattedString(), is("positionerName : pos1 ('pos1' 'pos2' 'pos3')"));
	}

	@Test
	public void testCreateFormattedListAcceptablePositions() throws Exception {
		assertThat(positioner.createFormattedListAcceptablePositions(), is("('pos1' 'pos2' 'pos3')"));
	}

	@Test
	public void testOutputFormattingForNullPosition() throws Exception {
		positioner.setPosition(null);
		assertThat(positioner.toFormattedString(), is("positionerName : UNAVAILABLE"));
	}

}
