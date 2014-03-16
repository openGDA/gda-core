/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.jython.logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.jython.IScanDataPointProvider;
import gda.scan.ScanDataPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ScanDataPointLoggerAdapterTest {

	@Mock
	private IScanDataPointProvider mockScanDataPointProvider;

	@Mock
	private LineLogger mockLogger;

	private ScanDataPointAdapter adapter;

	@Test
	public void testConstruction() {
		adapter = new ScanDataPointAdapter(mockLogger, mockScanDataPointProvider);
		verify(mockScanDataPointProvider).addIScanDataPointObserver(adapter);
	}

	@Test
	public void testUpdate() {
		adapter = new ScanDataPointAdapter(mockLogger, mockScanDataPointProvider);
		ScanDataPoint mockSDP = mock(ScanDataPoint.class);
		when(mockSDP.toFormattedString()).thenReturn("SDPFormattedString");
		adapter.update(null, mockSDP);
		verify(mockLogger).log("SDPFormattedString\n");
	}
}