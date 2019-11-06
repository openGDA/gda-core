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

package gda.mscan.processor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.mscan.ClauseContext;
import gda.mscan.element.RegionShape;


@RunWith(MockitoJUnitRunner.class)
public class ScannableGroupElementProcessorTest {

	private ScannableGroupElementProcessor processor;

	@Mock
	private ScannableMotor scannable1;

	@Mock
	private ScannableMotor scannable2;

	@Mock
	private ScannableGroup sGroup;

	@Mock
	private ClauseContext context;

	@Before
	public void setUp() throws Exception {
		when((sGroup.getGroupMembersAsList())).thenReturn(Arrays.asList(scannable1, scannable2));
		processor = new ScannableGroupElementProcessor(sGroup);
		when(context.grammar()).thenCallRealMethod();
	}

	/**
	 * Roi cannot be followed by Scannable
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void processLookUpTrapsIncorrectGrammar() throws Exception {
		doReturn(RegionShape.class).when(context).getPreviousType();
		processor.process(context, 1);
	}

	@Test
	public void processAddsValidSuccessorMembersToContextScannablesList() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, 0);
		verify(context).addScannable(sGroup.getGroupMembersAsList().get(0));
		verify(context).addScannable(sGroup.getGroupMembersAsList().get(1));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void processRejectsMoreThanTwoScannableGroupMembers() throws Exception {
		when((sGroup.getGroupMembersAsList())).thenReturn(Arrays.asList(scannable1, scannable2, scannable1));
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, 0);
	}

	@Test
	public void hasScannableIsCorrect() throws Exception {
		assertThat(processor.hasScannable(), is(true));
	}

	@Test
	public void hasScannableGroupIsCorrect() throws Exception {
		assertThat(processor.hasScannableGroup(), is(true));
	}

	/**
	 * Test method for {@link gda.mscan.processor.ScannableElementProcessor#getElement()}.
	 */
	@Test
	public void getSourceReturnSuppliedObject() throws Exception {
		assertThat(processor.getElement(), is(sGroup));
	}
}
