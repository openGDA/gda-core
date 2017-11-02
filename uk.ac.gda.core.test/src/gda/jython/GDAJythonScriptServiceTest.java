/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import static org.eclipse.scanning.api.script.ScriptLanguage.GROOVY;
import static org.eclipse.scanning.api.script.ScriptLanguage.SPEC_PASTICHE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.scanning.api.script.ScriptExecutionException;
import org.eclipse.scanning.api.script.ScriptLanguage;
import org.eclipse.scanning.api.script.ScriptRequest;
import org.eclipse.scanning.api.script.ScriptResponse;
import org.eclipse.scanning.api.script.UnsupportedLanguageException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class GDAJythonScriptServiceTest {

	private IScriptService scriptService = new GDAJythonScriptService();

	private ICommandRunner mockCommandRunner;

	@Before
	public void setUp() {
		mockCommandRunner = Mockito.mock(ICommandRunner.class);
		InterfaceProvider.setCommandRunnerForTesting(mockCommandRunner);
	}

	@Test
	public void testSupported() {
		final ScriptLanguage[] expected = new ScriptLanguage[] { SPEC_PASTICHE };
		assertArrayEquals(expected, scriptService.supported());
	}

	@Test
	public void testExecute() throws Exception {
		File testFile = File.createTempFile("test", ".py", null);
		testFile.deleteOnExit();
		String testFilePath = testFile.getAbsolutePath();

		ScriptRequest scriptRequest = new ScriptRequest(testFilePath, SPEC_PASTICHE);

		ScriptResponse<?> response = scriptService.execute(scriptRequest);
		assertNotNull(response);

		verify(mockCommandRunner).runScript(testFile);
	}

	@Test
	public void testExecuteLocateScript() throws Exception {
		File testFile = File.createTempFile("test", ".py", null);
		testFile.deleteOnExit();
		String testFilePath = testFile.getAbsolutePath();
		when(mockCommandRunner.locateScript("test.py")).thenReturn(testFilePath);

		ScriptRequest scriptRequest = new ScriptRequest("test.py", SPEC_PASTICHE);

		ScriptResponse<?> response = scriptService.execute(scriptRequest);
		assertNotNull(response);

		verify(mockCommandRunner).runScript(testFile);
	}

	@Test(expected=ScriptExecutionException.class)
	public void testExecuteNoSuchFile() throws Exception {
		ICommandRunner mockCommandRunner = Mockito.mock(ICommandRunner.class);
		InterfaceProvider.setCommandRunnerForTesting(mockCommandRunner);

		ScriptRequest scriptRequest = new ScriptRequest("/tmp/noSuchFile.py", SPEC_PASTICHE);

		scriptService.execute(scriptRequest);
	}

	@Test(expected=UnsupportedLanguageException.class)
	public void testExecuteUnsupportedScriptLanguage() throws Exception {
		ScriptRequest scriptRequest = new ScriptRequest("/tmp/noSuchFile.py", GROOVY);
		scriptService.execute(scriptRequest);
	}

}
