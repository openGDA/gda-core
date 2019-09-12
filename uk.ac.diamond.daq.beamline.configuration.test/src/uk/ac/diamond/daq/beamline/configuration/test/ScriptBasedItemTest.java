package uk.ac.diamond.daq.beamline.configuration.test;

import static java.util.Collections.singletonMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.python.util.PythonInterpreter;

import gda.device.Scannable;
import gda.factory.Factory;
import gda.factory.Finder;
import gda.jython.Jython;
import uk.ac.diamond.daq.beamline.configuration.ScriptBasedItem;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class ScriptBasedItemTest {

	private static final String FILENAME = "func.py";
	private static final String FUNCTION = "myFunction";
	private static final String INPUT_PROPERTY = "potato";
	private static final String SQUARED_FUNCTION = "	return x**2";
	private static final String ERROR_IN_SCRIPT_MESSAGE = "Error executing function '" + FUNCTION + "'";

	private static PythonInterpreter python;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void simpleFunction() throws Exception {
		createScript(SQUARED_FUNCTION);
		double input = 5;
		double expectedOutput = Math.pow(input, 2);
		Scannable scannable = createMockScannable();

		ScriptBasedItem item = new ScriptBasedItem(new File(FILENAME), singletonMap(scannable, FUNCTION), INPUT_PROPERTY);
		Map<Scannable, Double> position = item.getPositions(getProperties(input));

		assertThat(position, is(equalTo(singletonMap(scannable, expectedOutput))));
	}

	@Test
	public void invalidFunctionNameThrows() throws Exception {
		createScript(SQUARED_FUNCTION);

		exception.expect(NullPointerException.class);
		exception.expectMessage("Could not find function 'covfefe'");

		new ScriptBasedItem(new File(FILENAME), singletonMap(createMockScannable(), "covfefe"), INPUT_PROPERTY).start(getProperties(1.0));
	}

	@Test
	public void moreAdvancedFunction() throws Exception {
		String script =
				  "\n   return fn2(x) ** 2"
				+ "\n"
				+ "def fn2(x):"
				+ "\n   return x - 1";
		createScript(script);

		Scannable scannable = createMockScannable();
		double input = 4.5;
		double result = Math.pow(input - 1, 2);

		Map<Scannable, Double> position = new ScriptBasedItem(new File(FILENAME), singletonMap(scannable, FUNCTION), INPUT_PROPERTY).getPositions(getProperties(input));

		assertThat(position, is(equalTo(singletonMap(scannable, result))));
	}

	@Test
	public void errorInScriptBubblesUp() throws Exception {
		createScript("	raise AssertionError('hi')"); // would be the same with invalid syntax
		Scannable scannable = createMockScannable();
		try {
			new ScriptBasedItem(new File(FILENAME), singletonMap(scannable, FUNCTION), INPUT_PROPERTY).start(getProperties(5.0));
			fail("Error from script was swallowed");
		} catch (WorkflowException expected) {
			assertThat(expected.getMessage(), containsString(ERROR_IN_SCRIPT_MESSAGE));
			// we do not move the scannable
			verifyZeroInteractions(scannable);
		}
	}

	@Test
	public void multipleScannablesWhenItAllWorks() throws Exception {
		String script = // header in createScript
				  "\n	return 42"
				+ "\n"
				+ "def fn2(x):"
				+ "\n	return -3";
		createScript(script);

		Scannable x = createMockScannable();
		Scannable y = createMockScannable();

		Map<Scannable, String> functionsMap = new HashMap<>();
		functionsMap.put(x, FUNCTION);
		functionsMap.put(y, "fn2");
		new ScriptBasedItem(new File(FILENAME), functionsMap, INPUT_PROPERTY).start(getProperties(0));

		verify(x).asynchronousMoveTo(42.0);
		verify(y).asynchronousMoveTo(-3.0);
	}

	@Test
	public void oneBadFunctionPreventsAllMovement() throws Exception {
		String script = // header in createScript
				  "\n	return 42"
				+ "\n"
				+ "def fn2(x):"
				+ "\n	return 'covfefe'";
		createScript(script);

		Scannable x = createMockScannable();
		Scannable y = createMockScannable();

		Map<Scannable, String> functionsMap = new HashMap<>();
		functionsMap.put(x, FUNCTION);
		functionsMap.put(y, "fn2");
		try {
			new ScriptBasedItem(new File(FILENAME), functionsMap, INPUT_PROPERTY).start(getProperties(0));
			fail("Should have thrown exception!");
		} catch (WorkflowException e) {
			verifyZeroInteractions(x, y);
		}
	}

	@Test
	public void noReturnStatement() throws Exception {
		createScript("	pass");

		exception.expect(WorkflowException.class);
		exception.expectMessage(ERROR_IN_SCRIPT_MESSAGE);

		new ScriptBasedItem(new File(FILENAME), singletonMap(createMockScannable(), FUNCTION), INPUT_PROPERTY).start(getProperties(5.0));
	}

	@Test
	public void fileNotFound() throws Exception {
		exception.expect(WorkflowException.class);
		exception.expectMessage("Error reading script");
		new ScriptBasedItem(new File("covfefe.py"), singletonMap(createMockScannable(), FUNCTION), INPUT_PROPERTY).start(getProperties(5.0));
	}

	private void createScript(String functionBody) throws Exception {
		File script = new File(FILENAME);

		if (script.exists()) {
			script.delete();
		}

		FileWriter writer = new FileWriter(script);

		writer.write("def " + FUNCTION + "(x):");
		writer.write(functionBody);
		writer.close();
	}

	/**
	 * Injects a Jython singleton mock into the Finder.
	 * Jython::exec and Jython::getFromJythonNamespace
	 * are delegated to a PythonInterpreter
	 */
	@BeforeClass
	public static void setup() {

		Jython jython = mock(Jython.class);
		python = new PythonInterpreter();

		doAnswer(invocation -> {
			python.exec(invocation.getArgumentAt(0, String.class));
			return null;
		}).when(jython).exec(anyString());

		doAnswer(invocation -> python.get(invocation.getArgumentAt(0, String.class)))
			.when(jython).getFromJythonNamespace(anyString(), anyString());

		Factory factory = mock(Factory.class);
		when(factory.getFindablesOfType(Jython.class)).thenReturn(singletonMap(Jython.SERVER_NAME, jython));
		Finder.getInstance().addFactory(factory);
	}

	@AfterClass
	public static void cleanUp() {
		// clean up Finder
		Finder.getInstance().removeAllFactories();

		// close python
		python.close();

		// delete file
		File script = new File(FILENAME);
		if (script.exists()) {
			script.delete();
		}
	}

	private Properties getProperties(double inputValue) {
		Properties properties = new Properties();
		properties.setProperty(INPUT_PROPERTY, Double.toString(inputValue));
		return properties;
	}

	private Scannable createMockScannable() {
		Scannable scannable = mock(Scannable.class);
		when(scannable.getName()).thenReturn("Mock Scannable");
		return scannable;
	}

}
