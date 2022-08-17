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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import gda.device.Monitor;
import gda.device.Scannable;
import gda.mscan.ClausesContext;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ScannableMonitorElementProcessorTest {
	private ScannableMonitorElementProcessor processor;

	@Mock
	private Monitor device;

	@Mock
	private NumberElementProcessor numproc;

	@Mock
	private ClausesContext context;

	@Mock
	private IRunnableDeviceService rds;

	@Mock
	private IRunnableDevice<Object> rDevice;

	@BeforeEach
	public void setUp() throws Exception {
		processor = new ScannableMonitorElementProcessor(device);
		when(context.grammar()).thenCallRealMethod();
		when(context.isScanPathSeen()).thenReturn(true);
		when(device.getName()).thenReturn("mon");
	}

	@Test
	public void contextWithoutScanPathIsRejected() throws Exception {
		when(context.isScanPathSeen()).thenReturn(false);
		var e = assertThrows(IllegalArgumentException.class,
				() -> processor.process(context, new ArrayList<>(), 0));
		assertThat(e.getMessage(), containsString("No scan path defined"));
	}

	@Test
	public void tooManyClauseProcessorsAreRejected() throws Exception {
		var e = assertThrows(IllegalArgumentException.class,
				() -> processor.process(context, Arrays.asList(processor, processor), 0));
		assertThat(e.getMessage(), containsString("too many elements"));
	}

	@Test
	public void validMonitorIsAdded() throws Exception {
		doReturn(Scannable.class).when(context).getPreviousType();
		processor.process(context, Arrays.asList(processor), 0);
		verify(context).addMonitor(device.getName(), false);
	}

}
