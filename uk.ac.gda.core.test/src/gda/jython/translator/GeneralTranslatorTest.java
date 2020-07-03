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

import static java.util.stream.Stream.iterate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Streams;

import gda.jython.InterfaceProvider;
import gda.jython.MockJythonServerFacade;
/**
 * Test that the GeneralTranslator produces the expected translation
 */
public class GeneralTranslatorTest {

	private final static String TEST_FILE_FOLDER = "testfiles/gda/jython/translator/GeneralTranslatorTest/";
	private GeneralTranslator translator = new GeneralTranslator();

	@BeforeClass
	public static void prepareInterfaceProvider() throws Exception {
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
		mockJythonServerFacade.setEvaluateCommandResult("['scan']");
	}

	@AfterClass
	public static void clearInterfaceProvider() {
		InterfaceProvider.setCommandRunnerForTesting(null);
		InterfaceProvider.setCurrentScanControllerForTesting(null);
		InterfaceProvider.setTerminalPrinterForTesting(null);
		InterfaceProvider.setScanStatusHolderForTesting(null);
		InterfaceProvider.setJythonNamespaceForTesting(null);
		InterfaceProvider.setAuthorisationHolderForTesting(null);
		InterfaceProvider.setScriptControllerForTesting(null);
		InterfaceProvider.setPanicStopForTesting(null);
		InterfaceProvider.setCurrentScanInformationHolderForTesting(null);
		InterfaceProvider.setJythonServerNotiferForTesting(null);
		InterfaceProvider.setDefaultScannableProviderForTesting(null);
		InterfaceProvider.setScanDataPointProviderForTesting(null);
		InterfaceProvider.setBatonStateProviderForTesting(null);
		InterfaceProvider.setJSFObserverForTesting(null);
	}

	private void setAliases(String... aliases) {
		Stream.of(aliases).forEach(translator::addAliasedCommand);
	}

	private void setVarargs(String... aliases) {
		Stream.of(aliases).forEach(translator::addAliasedVarargCommand);
	}

	@Test
	/**
	 * translation of a file with mixed tab and space indentation
	 */
	public void translateMixed() throws IOException {
		setVarargs("pos", "inc");
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
		setAliases("pause");
		setVarargs("scan");
		translateTestRunner("AdvancedOptions.txt");
	}

	@Test
	/**
	 * translation of a typical diffcalc file (note: the test translator does not include all the aliases)
	 */
	public void translateDiffcalc() throws IOException {
		setVarargs("pos");
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
		setVarargs("pos");
		translateTestRunner("poscommand.txt");
	}

	@Test
	public void translateSemiColonSplitCommands() throws IOException {
		setVarargs("pos");
		translateTestRunner("semicolonSplitCommands.txt");
	}


	public void translateTestRunner(String testFileName) throws IOException {
		String expectedFilename = TEST_FILE_FOLDER + testFileName + ".translated.expected";

		try (Stream<String> original = Files.lines(Paths.get(TEST_FILE_FOLDER + testFileName));
				Stream<String> expected = Files.lines(Paths.get(expectedFilename));) {

			Iterator<Integer> lineNo = iterate(1, i -> i+1).iterator();
			Streams.forEachPair(expected, original.map(translator::translate),
					(exp, act) -> assertEquals("Difference on line " + lineNo.next(), exp, act));
		}
	}

	@Test
	public void testSimpleScanCommand(){
		setVarargs("scan");
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
		setVarargs("pos");
		Assert.assertEquals("pos([posname,2,\"string with quote's\",5.0])",
				translator.translate("pos posname 2 \"string with quote\'s\" 5.0"));
	}

	@Test
	public void test_translate5(){
		setVarargs("pos");
		Assert.assertEquals("pos([posname,(1.0,1.0)])",
				translator.translate("pos posname ( 1.0 1.0)"));
	}

	@Test
	public void test_translate_GDA_4054(){
		setVarargs("scan");
		setAliases("myfunkyalias");
		Assert.assertEquals("scan([scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9),0,10,1,x,bsdiode])",
				translator.translate("scan scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9) 0 10 1 x bsdiode"));
		Assert.assertEquals("myfunkyalias(scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9),0,10,1,x,bsdiode)",
				translator.translate("myfunkyalias scannablejumpscannable.ScJuSc(\"step\",4,5,x,-.9) 0 10 1 x bsdiode"));
	}

	@Test
	@Ignore("2010/10/26 Test ignored since not passing GDA-3703")
	public void test_translateMultilineComment(){
		setAliases("pos");
		String original_command = "print \"\"\"\npos a 1.0\n\"\"\"";
		Assert.assertEquals(original_command,
				translator.translate(original_command));
		original_command = "print '''\npos a 1.0\n'''";
		Assert.assertEquals(original_command,
				translator.translate(original_command));
	}


	@Test
	public void testSimpleScanCommand2(){
		setVarargs("pos");
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
		translator.addAliasedCommand("run");
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
