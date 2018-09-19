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

@RunWith(MockitoJUnitRunner.class)
public class NumberElementProcessorTest {

	private NumberElementProcessor processor;

	private double tenPointFour = 10.4;

	@Mock
	private ClauseContext context;

	@Before
	public void setUp() throws Exception {
		processor = new NumberElementProcessor(tenPointFour);
		when(context.grammar()).thenCallRealMethod();
	}

	/**
	 * Number cannot be first element of clause
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void processLookUpTrapsNumberAtElementZero() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, 0);
	}

	@Test
	public void processAddsValidSuccessorToContextParametersList() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		when(context.paramsFull()).thenReturn(false);
		processor.process(context, 1);
		verify(context).addParam(tenPointFour);
	}

	@Test
	public void hasNumberIsCorrect() throws Exception {
		assertThat(processor.hasNumber(), is(true));
	}

	@Test
	public void getSource() throws Exception {
		assertThat(processor.getElement(), is(tenPointFour));
	}
}
