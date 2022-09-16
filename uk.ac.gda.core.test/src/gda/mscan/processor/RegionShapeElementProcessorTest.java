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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import gda.device.Scannable;
import gda.mscan.ClausesContext;
import gda.mscan.element.RegionShape;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RegionShapeElementProcessorTest {

	private RegionShapeElementProcessor processor;

	private RegionShape shape = RegionShape.CIRCLE;
	private List<IClauseElementProcessor> emptyList = new ArrayList<>();

	@Mock
	private ClausesContext context;

	@BeforeEach
	public void setUp() throws Exception {
		processor = new RegionShapeElementProcessor(shape);
		when(context.grammar()).thenCallRealMethod();
	}

	/**
	 * Roi cannot be first element of clause
	 */
	@Test
	public void processLookUpTrapsRoiAtElementZero() throws Exception {
		assertThrows(UnsupportedOperationException.class, () -> processor.process(context, emptyList, 0));
	}

	/**
	 * Number cannot be followed by Roi
	 */
	@Test
	public void processLookUpTrapsIncorrectGrammar() throws Exception {
		doReturn(Number.class).when(context).getPreviousType();
		assertThrows(UnsupportedOperationException.class, () -> processor.process(context, emptyList, 1));
	}

	@Test
	public void processSetsValidSuccessorAsContextRoi() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, emptyList, 1);
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
