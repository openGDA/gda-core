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

package gda.jython;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gda.device.Motor;
import gda.device.MotorException;
import gda.factory.Factory;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.scan.ScanBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class JythonServerTest {
	private JythonServer jythonServer;
	private ScriptPaths pathsList;
	private String[] pathsArray;

	@Before
	public void setUp() {
		jythonServer = new JythonServer();
		pathsArray = new String[] {"Nonesuch", "Nonexistent"};
		List<ScriptProject> projects = new ArrayList<ScriptProject>();
		for (String path : pathsArray) {
			projects.add(new ScriptProject(path, "Project: " + path, ScriptProjectType.CORE));
		}
		pathsList = new ScriptPaths(projects);
		pathsList.setStartupScript("/some/folder/localStation.py");
		jythonServer.setJythonScriptPaths(pathsList);
		
		InterfaceProvider.setTerminalPrinterForTesting(new ITerminalPrinter() {
			@Override
			public void print(String text) {
				System.out.println("ITerminalPrinter: " + text);
			}
		});
	}
	
	@After
	public void tearDown() {
		jythonServer = null;
		pathsList = null;
		pathsArray = null;
		ScriptBase.setInterrupted(false);
		ScanBase.setInterrupted(false);
	}
	
	@Test
	public void testConfiguringServerPassesPathsToInterpreter() {
		try {
			jythonServer.configure();
		} catch (FactoryException e) {
			//expected due to ignoring most of .configure()'s prereqs.
		}
		GDAJythonInterpreter interpreter = jythonServer.getJythonInterpreter();
		Assert.assertNotNull(interpreter);
		Assert.assertEquals(pathsList, interpreter.getJythonScriptPaths());
	}
	
	@Test
	public void testLocateStringUsesScriptPathsObject() {
		ScriptProject realProject = new ScriptProject("test/gda/jython/testfiles", "Real Project", ScriptProjectType.CONFIG);
		ScriptPaths realPath = new ScriptPaths(Collections.singletonList(realProject));
		jythonServer.setJythonScriptPaths(realPath);
		Assert.assertNotNull(jythonServer.locateScript("exists"));
	}
	
	@Test
	public void testDefaultScriptFolderIsTheFirstEntryInTheList() {
		Assert.assertEquals("Nonesuch", jythonServer.getDefaultScriptProjectFolder());
	}
	
	@Test
	public void testPanicStop_StopsMotors() throws MotorException, InterruptedException {
		Factory factory = mock(Factory.class);
		Motor mockMotor1 = mock(Motor.class);
		Motor mockMotor2 = mock(Motor.class);
		Motor mockMotor3 = mock(Motor.class);
		when(factory.getFindables()).thenReturn(Arrays.asList((Findable)mockMotor1, (Findable)mockMotor2, (Findable)mockMotor3));
		Finder.getInstance().addFactory(factory );
		
		jythonServer.panicStop("Unused JSFIdentifier");
		Thread.sleep(1000);
		verify(mockMotor1).stop();
		verify(mockMotor2).stop();
		verify(mockMotor3).stop();
	}
	
	@Test
	public void testPanicStop_StopsMotorsDespiteADelayAndExceptionFromOne() throws MotorException, InterruptedException {
		Factory factory = mock(Factory.class);
		Motor mockMotor1 = mock(Motor.class);
		Motor mockMotor2 = mock(Motor.class);
		Motor mockMotor3 = mock(Motor.class);
		
		when(mockMotor1.getName()).thenReturn("disconnected_motor");
		class BeSlowAndThenFailAnswer implements Answer<Void> {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				Thread.sleep(2000);
				throw new Exception("bang");
			}
		}
		
		doAnswer(new BeSlowAndThenFailAnswer()).when(mockMotor1).stop();
		
		when(factory.getFindables()).thenReturn(Arrays.asList((Findable)mockMotor1, (Findable)mockMotor2, (Findable)mockMotor3));
		Finder.getInstance().addFactory(factory );
		
		jythonServer.panicStop("Unused JSFIdentifier");
		Thread.sleep(3000);
		verify(mockMotor1).stop();
		verify(mockMotor2).stop();
		verify(mockMotor3).stop();
	}
}
