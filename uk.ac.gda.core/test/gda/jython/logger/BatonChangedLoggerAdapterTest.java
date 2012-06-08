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
import gda.jython.IBatonStateProvider;
import gda.jython.batoncontrol.BatonChanged;
import gda.jython.batoncontrol.ClientDetails;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class BatonChangedLoggerAdapterTest {

	@Mock
	private IBatonStateProvider mockBatonStateProvider;

	@Mock
	private LineLogger mockLogger;

	private BatonChangedAdapter adapter;

	@Test
	public void testConstruction() {
		adapter = new BatonChangedAdapter(mockLogger, mockBatonStateProvider);
		verify(mockBatonStateProvider).addBatonChangedObserver(adapter);
	}

	@Test
	public void testUpdate() {
		adapter = new BatonChangedAdapter(mockLogger, mockBatonStateProvider);
		ClientDetails mockClientDetails = mock(ClientDetails.class);
		when(mockBatonStateProvider.getBatonHolder()).thenReturn(mockClientDetails);
		when(mockClientDetails.toString()).thenReturn("somestring");
		
		adapter.update(null, mock(BatonChanged.class));
		verify(mockLogger).log("<<<Baton acquired by: 'somestring' >>>");
	
	}
}