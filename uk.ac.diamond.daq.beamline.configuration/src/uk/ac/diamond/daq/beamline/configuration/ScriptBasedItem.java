package uk.ac.diamond.daq.beamline.configuration;

import static java.util.stream.Collectors.joining;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.python.core.PyException;
import org.python.core.PyFloat;
import org.python.core.PyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.factory.Finder;
import gda.jython.Jython;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

/**
 * {@link WorkflowItem} implementation which computes target positions
 * for its configured {@link Scannable scannables} by evaluating
 * Jython functions in a script.
 */
public class ScriptBasedItem extends WorkflowItemBase {

	private static final Logger logger = LoggerFactory.getLogger(ScriptBasedItem.class);

	private final String inputProperty;
	private final File script;
	private final Map<Scannable, String> functionsPerScannable;

	public ScriptBasedItem(File script, Map<Scannable, String> functionsPerScannable, String inputProperty) {
		this.script = script;
		this.functionsPerScannable = functionsPerScannable;
		this.inputProperty = inputProperty;
	}

	@Override
	public Set<Scannable> getScannables() {
		return functionsPerScannable.keySet();
	}

	@Override
	public Map<Scannable, Double> getPositions(Properties properties) throws WorkflowException {
		PyObject argument = getFunctionArgument(properties);
		String source = readFile();
		return evaluateUserFunctions(source, argument);
	}

	private PyObject getFunctionArgument(Properties properties) {
		double input = Double.parseDouble(properties.getProperty(inputProperty));
		return new PyFloat(input);
	}

	private String readFile() throws WorkflowException {
		try (BufferedReader scriptReader = new BufferedReader(new FileReader(script))) {
			return scriptReader.lines().collect(joining("\n"));
		} catch (IOException e) {
			throw new WorkflowException("Error reading script", e);
		}
	}

	private Map<Scannable, Double> evaluateUserFunctions(String source, PyObject argument) throws WorkflowException {
		Map<Scannable, Double> targetPositions = new HashMap<>();

		for (Map.Entry<Scannable, String> scannableEntry : functionsPerScannable.entrySet()) {
			Scannable scannable = scannableEntry.getKey();
			String functionName = scannableEntry.getValue();
			PyObject function = getFunction(source, functionName);

			double result = evaluateFunction(functionName, function, argument);

			targetPositions.put(scannable, result);
		}
		return targetPositions;
	}

	private PyObject getFunction(String code, String functionName) {
		Jython jython = getJythonInterpreter();
		jython.exec(code);
		return Objects.requireNonNull((PyObject) jython.getFromJythonNamespace(functionName, jython.getName()),
				"Could not find function '" + functionName + "'");
	}

	private Jython getJythonInterpreter() {
		return Finder.getInstance().findSingleton(Jython.class);
	}

	/**
	 * The function will be given a single double as argument, and expects a double as return
	 *
	 * Any exception is rethrown as a WorkflowException
	 */
	private double evaluateFunction(String functionName, PyObject function, PyObject argument) throws WorkflowException {
		logger.debug("Evaluating {}({})...", functionName, argument.__str__());
		try {
			double result = function.__call__(argument).asDouble();
			logger.debug("... evaluation result = {}", result);
			return result;
		} catch (PyException e) {
			throw new WorkflowException("Error executing function '" + functionName + "'", e);
		}
	}

}
