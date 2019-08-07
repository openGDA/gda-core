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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Scannable;
import gda.mscan.ClauseContext;
import gda.mscan.element.RegionShape;

@RunWith(MockitoJUnitRunner.class)
public class RegionShapeElementProcessorTest {

	private RegionShapeElementProcessor processor;

	private RegionShape shape = RegionShape.CIRCLE;

	@Mock
	private ClauseContext context;

	@Before
	public void setUp() throws Exception {
		processor = new RegionShapeElementProcessor(shape);
		when(context.grammar()).thenCallRealMethod();
	}

	/**
	 * Roi cannot be first element of clause
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void processLookUpTrapsRoiAtElementZero() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, 0);
	}

	/**
	 * Number cannot be followed by Roi
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void processLookUpTrapsIncorrectGrammar() throws Exception {
		doReturn(Number.class).when(context).getPreviousType();
		processor.process(context, 1);
	}

	@Test
	public void processSetsValidSuccessorAsContextRoi() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, 1);
		verify(context).setRegionShape(shape);
	}

	@Test
	public void hasRoiIsCorrect() throws Exception {
		assertThat(processor.hasRoi(), is(true));
	}

	@Test
	public void getSource() throws Exception {
		assertThat(processor.getElement(), is(shape));
	}
}
