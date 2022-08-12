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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import gda.MockFactory;
import gda.TestHelpers;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.scannable.scannablegroup.ScannableGroup;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import gda.jython.JythonServer;
import gda.jython.MockJythonServerFacade;

public class GeneralCommandsTest {

	@Mock
	private JythonServer mockCommandServer;
	private MockJythonServerFacade printingFacade = new MockJythonServerFacade();

	private Map<String, Findable> findablesMap;
	private Map<String, Scannable> scannablesMap;

	@BeforeEach
	public void createNamespaceWithGroup() {
		Finder.removeAllFactories();
		InterfaceProvider.setTerminalPrinterForTesting(printingFacade);
		mockCommandServer = Mockito.mock(JythonServer.class);
		findablesMap = new HashMap<>();
		scannablesMap = new HashMap<>();


		Factory withServerSingleton = TestHelpers.createTestFactory();
		withServerSingleton.addFindable(mockCommandServer);
		Finder.addFactory(withServerSingleton);

		Mockito.when(mockCommandServer.getAllObjectsOfType(Findable.class)).thenReturn(findablesMap);
		Mockito.when(mockCommandServer.getAllObjectsOfType(Scannable.class)).thenReturn(scannablesMap);
	}

	private void addToNamespace(boolean addGroup) throws DeviceException, FactoryException {
		ScannableMotion lev4 = MockFactory.createMockScannableMotion("lev4", 4);
		ScannableMotion lev5a = MockFactory.createMockScannableMotion("lev5a", 5);
		Scannable lev5b = MockFactory.createMockScannable("lev5b", 5);
		ScannableMotion lev6 = MockFactory.createMockScannableMotion("lev6", 6);

		ScannableGroup group = new ScannableGroup();
		group.setName("group");
		group.addGroupMember(lev4);
		group.addGroupMember(lev5a);
		group.addGroupMember(lev5b);
		group.addGroupMember(lev6);

		for (Scannable find : Arrays.asList(lev4, lev5a, lev5b, lev6)) {
			findablesMap.put(find.getName(), find);
			scannablesMap.put(find.getName(), find);
		}
		if (addGroup) {
			findablesMap.put(group.getName(), group);
			scannablesMap.put(group.getName(), group);
		}
	}

	@AfterAll
	public static void cleanUpFactories() {
		Finder.removeAllFactories();
		InterfaceProvider.setTerminalPrinterForTesting(null);
	}

	@Test
	public void lsNoGroup() throws DeviceException, FactoryException {
		addToNamespace(false);
		GeneralCommands.ls(Findable.class);

		String output = printingFacade.getTerminalOutput();
		String expected = "\nlev4 : 0.0\nlev5a : 0.0\nlev5b : 0.0\nlev6 : 0.0\n\n";

		assertEquals(expected, output);
	}

	@Test
	public void lsWithGroup() throws DeviceException, FactoryException {
		addToNamespace(true);
		GeneralCommands.ls(Findable.class);

		String output = printingFacade.getTerminalOutput();
		String expected = "\ngroup ::\n  lev4  : 0.0\n  lev5a : 0.0\n  lev5b : 0.0\n  lev6  : 0.0\n\n";

		assertEquals(expected, output);
	}

	@Test
	public void posAllNoGroup() throws DeviceException, FactoryException {
		addToNamespace(false);
		ScannableCommands.pos();

		String output = printingFacade.getTerminalOutput();
		String expected = "lev4  : 0.0\nlev5a : 0.0\nlev5b : 0.0\nlev6  : 0.0\n";

		assertEquals(expected, output);
	}

	@Test
	public void posAllWithGroup() throws DeviceException, FactoryException {
		addToNamespace(true);
		ScannableCommands.pos();

		String output = printingFacade.getTerminalOutput();
		String expected = "group ::\n  lev4  : 0.0\n  lev5a : 0.0\n  lev5b : 0.0\n  lev6  : 0.0\n";

		assertEquals(expected, output);
	}

}
