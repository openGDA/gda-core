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

package gda.jython.translator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
import gda.util.TestUtils;
/**
 * Test that the GeneralTranslator produces the expected translation
 */
public class GeneralTranslatorTest {

	static String testScratchDirectoryName = null;
	final static String TestFileFolder = "testfiles/gda/jython/translator/GeneralTranslatorTest/";
	static GeneralTranslator translator;

	@BeforeClass
	public static void createGeneralTranslator() throws Exception {
		testScratchDirectoryName = TestUtils.generateDirectorynameFromClassname(GeneralTranslatorTest.class.getCanonicalName());
		TestUtils.makeScratchDirectory(testScratchDirectoryName);

		MockJythonServerFacade mockJythonServerFacade = new MockJythonServerFacade();
		InterfaceProvider.setCommandRunnerForTesting(mockJythonServerFacade);
		InterfaceProvider.setCurrentScanControllerForTesting(mockJythonServerFacade);
		InterfaceProvider.setTerminalPrinterForTesting(mockJythonServerFacade);
		InterfaceProvider.setScanStatusHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJythonNamespaceForTesting(mockJythonServerFacade);
		InterfaceProvider.setAuthorisationHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setScriptControllerForTesting(mockJythonServerFacade);
		InterfaceProvider.setPanicStopForTesting(mockJythonServerFacade);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJythonServerNotiferForTesting(mockJythonServerFacade);
		InterfaceProvider.setDefaultScannableProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setScanDataPointProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setBatonStateProviderForTesting(mockJythonServerFacade);
		InterfaceProvider.setJSFObserverForTesting(mockJythonServerFacade);

		translator = new GeneralTranslator();
		// set up some aliases, based on gda.jython.GDAJythonInterpreter (though this does not need to match that exactly)
		translator.translate("alias ls");
		translator.translate("vararg_alias pos");
		translator.translate("vararg_alias upos");
		translator.translate("vararg_alias inc");
		translator.translate("vararg_alias uinc");
		translator.translate("alias help");
		translator.translate("alias list_defaults");
		translator.translate("alias add_default");
		translator.translate("alias remove_default");
		translator.translate("vararg_alias level");
		translator.translate("alias pause");
		translator.translate("alias reset_namespace");
		translator.translate("alias run");
		translator.translate("vararg_alias scan");
		translator.translate("vararg_alias pscan");
		translator.translate("vararg_alias cscan");
		translator.translate("vararg_alias zacscan");
		translator.translate("vararg_alias testscan");
		translator.translate("vararg_alias gscan");
		translator.translate("vararg_alias tscan");
		translator.translate("vararg_alias timescan");

		mockJythonServerFacade.setEvaluateCommandResult("['scan']");
	}

	@Test
	/**
	 * translation of a file with mixed tab and space indentation
	 */
	public void translateMixed() throws IOException {
		translateTestRunner("checkBeamPos100427.txt");
	}

	@Test
	/**
	 * translation of line that contains brackets with spaces. from LookupTables.py
	 */
	public void translateReloadLookupTables() {
		Assert.assertEquals("return [ reloadLookupTables ]",translator.translate("return [ reloadLookupTables ]"));
	}
	@Test
	/**
	 * translation of a typical GDA script file
	 */
	public void translateAdvancedOptions() throws IOException {
		translateTestRunner("AdvancedOptions.txt");
	}

	@Test
	/**
	 * translation of a typical diffcalc file (note: the test translator does not include all the aliases)
	 */
	public void translateDiffcalc() throws IOException {
		translateTestRunner("diffcalc_session.txt");
	}

	@Test
	/**
	 * translation of a large file not directly related to GDA, with varied coding conventions
	 */
	public void translateNapi() throws IOException {
		translateTestRunner("napi.txt");
	}

	@Test
	public void translateStringLiterals() throws IOException {
		translateTestRunner("stringliteral.txt");
	}

	@Test
	public void translatePosCommand() throws IOException {
		translateTestRunner("poscommand.txt");
	}

	@Test
	public void translateSemiColonSplitCommands() throws IOException {
		translateTestRunner("semicolonSplitCommands.txt");
	}


	public void translateTestRunner(String testFileName) throws IOException {

		String translatedFilename = testScratchDirectoryName + testFileName + ".translated";
		String expectedFilename = TestFileFolder + testFileName + ".translated.expected";

		StringBuffer fileBuffer;
		String fileString = null;
		String line;

		FileReader fr = new FileReader(TestFileFolder + testFileName);
		BufferedReader dis = new BufferedReader(fr);
		fileBuffer = new StringBuffer();
		while ((line = dis.readLine()) != null) {
			fileBuffer.append(translator.translate(line) + "\n");
		}
		fr.close();
		fileString = fileBuffer.toString();

		PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(translatedFilename)));
		pw.print(fileString);
		pw.flush();
		pw.close();

		// Binary compare files
		assertArrayEquals(Files.readAllBytes(Paths.get(expectedFilename)),
				Files.readAllBytes(Paths.get(translatedFilename)));
	}

	@Test
	public void testSimpleScanCommand(){
		Assert.assertEquals("scan([a,1.,1.,1.])",translator.translate("scan a 1. 1. 1."));

	}

	@Test
	public void test_splitGroup(){
		Assert.assertArrayEquals(new String[]{"scan","a","1.","1","1.0","1.","'1'","'1 '","'1.'","' 1'"},
				GeneralTranslator.splitGroup("scan a 1. 1 1.0 1. '1' '1 ', '1.', ' 1'"));
	}

	@Test
	public void test_splitGroup2(){
		Assert.assertArrayEquals(new String[]{"pos","posname","2","\"string with quote's\"","5.0"},
				GeneralTranslator.splitGroup("pos posname 2 \"string with quote's\" 5.0"));
	}

	@Test
	public void test_splitGroup3(){
		Assert.assertArrayEquals(new String[]{"pos","posname","2","\"string with quote\'s\"","5.0"},
				GeneralTranslator.splitGroup("pos posname 2 \"string with quote\'s\" 5.0"));
	}

	@Test
	public void test_translate3(){
		Assert.assertEquals("pos([posname,2,\"string with quote's\",5.0])",
				translator.translate("pos posname 2 \"string with quote\'s\" 5.0"));
	}

	@Test
	public void test_translate5(){
		Assert.assertEquals("pos([posname,(1.0,1.0)])",
				translator.translate("pos posname ( 1.0 1.0)"));
	}

	@Test
	public void test_translate_GDA_4045(){
		Assert.assertEquals("scan([scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9),0,10,1,x,bsdiode])",
				translator.translate("scan scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9) 0 10 1 x bsdiode"));
		translator.translate("alias myfunkyalias");
		Assert.assertEquals("myfunkyalias(scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9),0,10,1,x,bsdiode)",
				translator.translate("myfunkyalias scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9) 0 10 1 x bsdiode"));
	}

	@Test
	@Ignore("2010/10/26 Test ignored since not passing GDA-3703")
	public void test_translateMultilineComment(){
		String original_command = "print \"\"\"\npos a 1.0\n\"\"\"";
		Assert.assertEquals(original_command,
				translator.translate(original_command));
		original_command = "print '''\npos a 1.0\n'''";
		Assert.assertEquals(original_command,
				translator.translate(original_command));
	}


	@Test
	public void testSimpleScanCommand2(){
		Assert.assertEquals("pos([posname,2,\"string with quote's\",5.0])",translator.translate("pos posname 2 \"string with quote's\" 5.0"));
	}

	@Test
	public void testStringWithMixOfQuotes1() {
		final String text = "\"he'llo [a b c]\"";
		assertEquals(text, translator.translate(text));
	}

	@Test
	public void testStringWithMixOfQuotes2() {
		final String text = "'he\"llo [a b c]'";
		assertEquals(text, translator.translate(text));
	}

	@Test
	public void testListContainingAdjacentLists() {
		final String originalText = "[ [1] [2] ]";
		final String expectedTranslation = "[ [1],[2] ]";
		assertEquals(expectedTranslation, translator.translate(originalText));
	}

	@Test
	public void testRunStatement() {
		final String originalText = "run \"CommentPD\"";
		final String expectedTranslation = "run(\"CommentPD\")";
		assertEquals(expectedTranslation, translator.translate(originalText));
	}

	@Test
	public void testLineWithComment() {
		final String originalText = "cam.configure() # TODO Remove if not required 27-Jul-2011/ais";
		final String expectedTranslation = "cam.configure() ";
		final String actualTranslation = translator.translate(originalText);
		assertTrue(expectedTranslation.equals(actualTranslation));
	}

}
