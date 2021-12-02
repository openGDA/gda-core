/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import gda.epics.util.PVNameUtil;

public class PVNameUtilTest {

	@Test
	public void testGetBasePvName_singleValue() {
		assertThat(PVNameUtil.getBasePvName("BL07I-AL-SLITS-03:SETRANGE"),
				is(equalTo("BL07I-AL-SLITS-03:SETRANGE")));
	}

	@Test
	public void testGetBasePvName_singleValue_trailingColon() {
		assertThat(PVNameUtil.getBasePvName("BL07I-AL-SLITS-03:"),
				is(equalTo("BL07I-AL-SLITS-03")));
	}

	@Test
	public void testGetBasePvName_singleNullValue() {
		assertThat(PVNameUtil.getBasePvName((String) null), is(nullValue()));
	}

	@Test
	public void testGetBasePvName_nullValues() {
		assertThat(PVNameUtil.getBasePvName(null, null, null, null), is(nullValue()));
	}

	@Test
	public void testGetBasePvName_nullValue() {
		assertThat(PVNameUtil.getBasePvName("BL07I-DI-PHDGN-05:BPMI1", "BL07I-DI-PHDGN-05:BPMI2", null),
				is(nullValue()));
	}

	@Test
	public void testGetBasePvName_emptyValue() {
		assertThat(PVNameUtil.getBasePvName("BL07I-DI-PHDGN-05:BPMI1", "BL07I-DI-PHDGN-05:BPMI2", ""),
				 is(nullValue()));
	}

	@Test
	public void testGetBasePv_sameValue() {
		assertThat(PVNameUtil.getBasePvName("BL05I-DI-PHDGN-07:IONC:Y", "BL05I-DI-PHDGN-07:IONC:Y"),
				is(equalTo("BL05I-DI-PHDGN-07:IONC:Y")));
	}

	@Test
	public void testGetBasePv_sameValue_trailingColon() {
		assertThat(PVNameUtil.getBasePvName("BL05I-DI-PHDGN-07:IONC:Y:", "BL05I-DI-PHDGN-07:IONC:Y:"),
				is(equalTo("BL05I-DI-PHDGN-07:IONC:Y")));
	}

	@Test
	public void testGetBasePvName_noCommonBasePV() {
		assertThat(PVNameUtil.getBasePvName("BL07I-DI-PHDGN-05:BPMI1", "BL07I-DI-PHDGN-05:BPMI2", "BL11I-DI-QBPM-03"),
				 is(nullValue()));
	}

	@Test
	public void testGetBasePvName_commonBasePV() {
		assertThat(PVNameUtil.getBasePvName("BL07I-DI-PHDGN-05:BPMI1", "BL07I-DI-PHDGN-05:BPMI2", "BL07I-DI-PHDGN-05:BPMI3"),
				 is(equalTo("BL07I-DI-PHDGN-05")));
	}

	@Test
	public void testGetBasePv_noBaseName_commonPrefix() {
		assertThat(PVNameUtil.getBasePvName("BL07I-AL-SLITS-03:BPMI1", "BL07I-AL-SLITS-03:BPMI2",
				"BL07I-AL-SLITS-03:BPMI3", "BL07I-AL-SLITS-04:XPOS"),
				 is(nullValue()));
	}

	@Test
	public void testGetBasePv_multiSegment_commonBasePV() {
		assertThat(PVNameUtil.getBasePvName("BL05I-DI-PHDGN-07:IONC:Y:MP:SELECT.VAL", "BL05I-DI-PHDGN-07:IONC:Y:MP:INPOS.VAL",
				"BL05I-DI-PHDGN-07:IONC:Y:MP:DMOV.VAL", "BL05I-DI-PHDGN-07:IONC:Y:MP:SELECT.STAT"),
				is(equalTo("BL05I-DI-PHDGN-07:IONC:Y:MP")));
	}

	@Test
	public void testGetBasePv_multiSegment_commonBasePV2() {
		assertThat(PVNameUtil.getBasePvName("BL05I-DI-PHDGN-07:IONC:Y:MP:SELECT.VAL", "BL05I-DI-PHDGN-07:IONC:Y:MP:INPOS.VAL",
				"BL05I-DI-PHDGN-07:IONC:X:MP:SELECT.VAL", "BL05I-DI-PHDGN-07:IONC:X:MP:INPOS.VAL"),
				is(equalTo("BL05I-DI-PHDGN-07:IONC")));
	}

	@Test
	public void testGetBasePv_differentNumSegments() {
		assertThat(PVNameUtil.getBasePvName("BL05I-DI-PHDGN-07:IONC:Y:MP:SELECT.VAL", "BL05I-DI-PHDGN-07:IONC:Y",
				"BL05I-DI-PHDGN-07:IONC:X:MP:SELECT.VAL", "BL05I-DI-PHDGN-07:IONC:X:MP"),
				is(equalTo("BL05I-DI-PHDGN-07:IONC")));
	}

}
