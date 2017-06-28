/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.jython.commands;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import gda.MockFactory;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Findable;
import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;

public class GeneralCommandsTest {

	private MockJythonServerFacade jsfNoGroup;
	private MockJythonServerFacade jsfWithGroup;

	@Before
	public void createNamespaceWithGroup() throws Exception {

		ScannableMotion lev4 = MockFactory.createMockScannableMotion("lev4", 4);
		ScannableMotion lev5a = MockFactory.createMockScannableMotion("lev5a", 5);
		Scannable lev5b = MockFactory.createMockScannable("lev5b", 5);
		ScannableMotion lev6 = MockFactory.createMockScannableMotion("lev6", 6);


		jsfNoGroup = new MockJythonServerFacade();
		jsfNoGroup.placeInJythonNamespace("lev4", lev4);
		jsfNoGroup.placeInJythonNamespace("lev5a", lev5a);
		jsfNoGroup.placeInJythonNamespace("lev5b", lev5b);
		jsfNoGroup.placeInJythonNamespace("lev6", lev6);

		ScannableGroup group = new ScannableGroup();
		group.setName("group");
		group.addGroupMember(lev4);
		group.addGroupMember(lev5a);
		group.addGroupMember(lev5b);
		group.addGroupMember(lev6);

		jsfWithGroup = new MockJythonServerFacade();
		jsfWithGroup.placeInJythonNamespace("group", group);

	}

	@Test
	public void lsNoGroup() throws DeviceException{
		InterfaceProvider.setJythonNamespaceForTesting(jsfNoGroup);
		InterfaceProvider.setTerminalPrinterForTesting(jsfNoGroup);
		GeneralCommands.ls(Findable.class);

		String output = jsfNoGroup.getTerminalOutput();
		String expected = "\nlev4 : 0.0\nlev5a : 0.0\nlev5b : 0.0\nlev6 : 0.0\n\n";

		assertEquals(expected, output);
	}

	@Test
	public void lsWithGroup() throws Exception {
		InterfaceProvider.setJythonNamespaceForTesting(jsfWithGroup);
		InterfaceProvider.setTerminalPrinterForTesting(jsfWithGroup);
		GeneralCommands.ls(Findable.class);

		String output = jsfWithGroup.getTerminalOutput();
		String expected = "\ngroup ::\n  lev4  : 0.0\n  lev5a : 0.0\n  lev5b : 0.0\n  lev6  : 0.0\n\n";

		assertEquals(expected, output);
	}

	@Test
	public void posAllNoGroup() throws DeviceException{
		InterfaceProvider.setJythonNamespaceForTesting(jsfNoGroup);
		InterfaceProvider.setTerminalPrinterForTesting(jsfNoGroup);
		ScannableCommands.pos();

		String output = jsfNoGroup.getTerminalOutput();
		String expected = "lev4  : 0.0\nlev5a : 0.0\nlev5b : 0.0\nlev6  : 0.0\n";

		assertEquals(expected, output);
	}

	@Test
	public void posAllWithGroup() throws Exception {
		InterfaceProvider.setJythonNamespaceForTesting(jsfWithGroup);
		InterfaceProvider.setTerminalPrinterForTesting(jsfWithGroup);
		ScannableCommands.pos();

		String output = jsfWithGroup.getTerminalOutput();
		String expected = "group ::\n  lev4  : 0.0\n  lev5a : 0.0\n  lev5b : 0.0\n  lev6  : 0.0\n";

		assertEquals(expected, output);
	}

}
