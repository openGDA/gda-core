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

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Scannable;
import gda.device.scannable.ScannableMotor;
import gda.mscan.ClausesContext;
import gda.mscan.element.Scanpath;
import gda.mscan.element.RegionShape;

/**
 * Tests the ScannableElementProcessor and it's abstract base ElementProcessorBase as this
 * can't be instantiated on its own.
 */
@RunWith(MockitoJUnitRunner.class)
public class ScannableElementProcessorAndElementProcessorBaseTest {

	private ScannableElementProcessor processor;
	private List<IClauseElementProcessor> validClause = new ArrayList<>();
	private List<IClauseElementProcessor> inValidClause = new ArrayList<>();


	@Mock
	private ScannableMotor scannable;

	@Mock
	private ClausesContext context;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		processor = new ScannableElementProcessor(scannable);
		when(context.grammar()).thenCallRealMethod();
		inValidClause.add(processor);
		inValidClause.add(new NumberElementProcessor(1));
		validClause.add(processor);
		validClause.add(new RegionShapeElementProcessor(RegionShape.AXIAL));
		validClause.add(new NumberElementProcessor(1));
		validClause.add(new NumberElementProcessor(3));
		validClause.add(new ScanpathElementProcessor(Scanpath.AXIS_STEP));
		validClause.add(new NumberElementProcessor(1));
	}

	@Test
	public void specStyleScansAreRejected() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("SPEC style scans not yet supported.");
		processor.process(context, inValidClause, 0);
	}

	@Test
	public void processLooksUpPreviousTypeAndSuccessors() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, validClause, 0);
		verify(context).getPreviousType();
		verify(context).grammar();
	}

	@Test(expected = IllegalStateException.class)
	public void processLookUpTrapsNullPreviousType() throws Exception {
		doReturn(null).when(context).getPreviousType();
		processor.process(context, validClause, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void processLookUpTrapsIncorrectGrammarKeys() throws Exception {
		doReturn(String.class).when(context).getPreviousType();
		processor.process(context, validClause, 0);
	}

	/**
	 * Roi cannot be followed by Scannable
	 */
	@Test(expected = UnsupportedOperationException.class)
	public void processLookUpTrapsIncorrectGrammar() throws Exception {
		doReturn(RegionShape.class).when(context).getPreviousType();
		processor.process(context, validClause, 1);
	}

	@Test
	public void processAddsValidSuccessorToContextScannablesList() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, validClause, 0);
		verify(context).addScannable(scannable);
	}

	/**
	 * Test method for {@link gda.mscan.processor.ScannableElementProcessor#hasScannable()}.
	 */
	@Test
	public void hasScannableIsCorrect() throws Exception {
		assertThat(processor.hasScannable(), is(true));
	}

	/**
	 * Test method for {@link gda.mscan.processor.ScannableElementProcessor#hasDetector()}.
	 */
	@Test
	public void hasDetectorIsCorrect() throws Exception {
		assertThat(processor.hasDetector(), is(false));
	}

	/**
	 * Test method for {@link gda.mscan.processor.ScannableElementProcessor#hasMonitor()}.
	 */
	@Test
	public void hasMonitorIsCorrect() throws Exception {
		assertThat(processor.hasMonitor(), is(false));
	}

	/**
	 * Test method for {@link gda.mscan.processor.ScannableElementProcessor#getElement()}.
	 */
	@Test
	public void getSourceReturnSuppliedObject() throws Exception {
		assertThat(processor.getElement(), is(scannable));
	}

}
