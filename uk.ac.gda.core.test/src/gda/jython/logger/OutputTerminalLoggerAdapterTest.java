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

import static org.mockito.Mockito.verify;
import gda.jython.ITerminalOutputProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OutputTerminalLoggerAdapterTest {

	@Mock
	private ITerminalOutputProvider mockTerminalOutputProvider;

	@Mock
	private LineLogger mockLogger;

	private OutputTerminalAdapter adapter;

	@Test
	public void testConstruction() {
		adapter = new OutputTerminalAdapter(mockLogger, mockTerminalOutputProvider);
		verify(mockTerminalOutputProvider).addOutputTerminal(adapter);
	}

	@Test
	public void testPrint() throws Exception {
		adapter = new OutputTerminalAdapter(mockLogger, mockTerminalOutputProvider);
		adapter.write("abcd1234".getBytes("UTF8"));
		verify(mockLogger).log("abcd1234");
	}
}