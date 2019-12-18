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

package gda.mscan.processor;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import gda.device.Detector;
import gda.device.Scannable;
import gda.mscan.ClausesContext;

@RunWith(MockitoJUnitRunner.class)
public class ScannableDetectorElementProcessorTest {
	private ScannableDetectorElementProcessor processor;

	@Mock
	private Detector device;

	@Mock
	private NumberElementProcessor numproc;

	@Mock
	private ClausesContext context;

	@Mock
	private IRunnableDeviceService rds;

	@Mock
	private IRunnableDevice<Object> rDevice;

	@Rule
	public final ExpectedException exception = ExpectedException.none();


	@Before
	public void setUp() throws Exception {
		processor = new ScannableDetectorElementProcessor(device);
		when(context.grammar()).thenCallRealMethod();
		when(context.isScanPathSeen()).thenReturn(true);
		when(device.getName()).thenReturn("det");
		when(rds.getRunnableDevice("det")).thenReturn(rDevice);
	}

	@Test
	public void contextWithoutScanPathIsRejected() throws Exception {
		when(context.isScanPathSeen()).thenReturn(false);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No scan path defined");
		processor.process(context, new ArrayList<IClauseElementProcessor>(), 0);
	}

	@Test
	public void tooManyClauseProcessorsAreRejected() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("too many elements");
		processor.process(context, Arrays.asList(processor, processor, processor), 0);
	}

	@Test
	public void detectorWithoutParamIsAddedWithZeroExposure() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, Arrays.asList(processor), 0);
		verify(context).addDetector(device.getName(), 0);
	}

	@Test
	public void detectorWithParamIsAddedWithCorrectExposure() throws Exception {
		when(numproc.getElementValue()).thenReturn("0.6");
		when(numproc.hasNumber()).thenReturn(true);
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, Arrays.asList(processor, numproc), 0);
		verify(context).addDetector(device.getName(), 0.6);
	}

	@Test
	public void nonNumericDetectorParamProcessorsAreRejected() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("2nd element of unexpected type");
		processor.process(context, Arrays.asList(processor, numproc), 0);
	}
}
