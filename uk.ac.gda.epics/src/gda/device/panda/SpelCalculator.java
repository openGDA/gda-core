/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Class that uses Spring {@link ExpressionParser} to transform a series of numerical inputs to one or more outputs via.
 * a series of mathematical expressions.
 * <li>{@link #computeOutput(List)} transforms input to output via one or more equations
 *
 * <li>{@link #setDataVariableNames(List)} sets the variable name to use for each of the input values passed to
 * {@link #computeOutput(List)}. (default is A, B, C, D ... Z if no list is given).
 *
 * <li>{@link #setOutputExpressions(Map)} sets the map of expressions used to transform the input values (key = output
 * name, value = expression). Express
 * <br> <br>
 * Examples:
 * <br>
 * With dataVariableList = ["A", "B", "C"], expressionMap = {"value1":"#A", "Average" : "(#A+#B+#C)/3"}, computeOutput([5.0,6.0,7.0]) produces [5.0, 6.0]
 * <br><br>
 * Create SpelCalculator in Jython to compute log values for ionchambers :
 * <pre>
 * {@code ionchamber_vals = SpelCalculator()
 * ionchamber_vals.addOutputExpression("I0", "#A")
 * ionchamber_vals.addOutputExpression("It", "#B")
 * ionchamber_vals.addOutputExpression("Iref", "#C")
 * ionchamber_vals.addOutputExpression("ln(I0It)", "#log(#A/#B)")
 * ionchamber_vals.addOutputExpression("ln(Itref)", "#log(#B/#C)")
 * }
 * </pre>
 */

public class SpelCalculator {

	private static final Logger logger = LoggerFactory.getLogger(SpelCalculator.class);

	private Map<String, String> outputExpressions = Collections.emptyMap(); // key= output name, value= expression
	private Map<String, Expression> springExpressions = Collections.emptyMap(); // key = string expression, value = Expression object
	private List<String> dataVariableNames = Collections.emptyList(); // variable name to use for each of the data input values
	private List<String> defaultVariableNames = Collections.emptyList();

	private ExpressionParser parser;
	private StandardEvaluationContext context;
	private List<String> outputFormat = Collections.emptyList();
	private String defaultFormat = "%5.5g";

	public SpelCalculator() {
		// Generate default data variable names - uppercase A..Z
		defaultVariableNames = IntStream.range('A', 'Z'+1).mapToObj(Character::toString).toList();

		// Set the expression compiler mode to improve expression performance (factor of >2)
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
				this.getClass().getClassLoader());

		parser = new SpelExpressionParser(config);

		initialiseContext();
	}

	/**
	 * Initialise new {@link StandardEvaluationContext} and register the extra sin, cos, tan, exp, log functions.
	 */
	private void initialiseContext() {
		context = new StandardEvaluationContext();
		// register some mathematical functions that can be used in the expressions
		try {
			for(var func : ContextFunction.values()) {
				context.registerFunction(func.getName(), func.getMethod());
			}
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalArgumentException("Problem adding math functions to evaluation context", e);
		}
	}

	/**
	 * Additional functions added to ExpressionParser
	 */
	private enum ContextFunction {
		SIN("sin", Math.class),
		COS("cos", Math.class),
		TAN("tan", Math.class),
		EXP("exp", Math.class),
		LOG("log", SpelCalculator.class), // natural log
		LOG10("log10", SpelCalculator.class);

		private final String name;
		private final Class<?> clazz;

		private ContextFunction(String name, Class<?> clazz) {
			this.name = name;
			this.clazz = clazz;
		}
		public String getName() {
			return name;
		}

		public Method getMethod() throws NoSuchMethodException, SecurityException {
			return clazz.getMethod(name, double.class);
		}
	}

	/**
	 * Base 10 logarithm (infinite and NaN values replaced by zero)
	 * (this needs to be public static for using as function in Spring EvaluationContext)
	 * @param value
	 * @return log(value)
	 */
	public static double log10(double value) {
		Double logVal = Math.log10(value);
		return logVal.isInfinite() || logVal.isNaN() ? 0.0 : logVal;
	}

	/**
	 * Natural logarithm (infinite and NaN values replaced by zero)
	 * (this needs to be public static for using as function in Spring EvaluationContext)
	 * @param value
	 * @return log(value)
	 */
	public static double log(double value) {
		Double logVal = Math.log(value);
		return logVal.isInfinite() || logVal.isNaN() ? 0.0 : logVal;
	}

	public List<Double> computeOutput(double inputData) {
		return computeOutput(List.of(inputData));
	}

	public List<Double> computeOutput(Double[] inputData) {
		return computeOutput(Arrays.asList(inputData));
	}

	/**
	 * Transform a list of input values using the stored expressions.
	 * Each element in inputData list is assigned to a variable in the evaluation
	 * context using the inputDataNames (if set) or defaultInputNames (A...Z).
	 * e.g. for defaultInputNames, variable values are set to :
	 *
	 *  A = inputData[0], B = inputData[1], C = inputData[2] etc.
	 *
	 *  Each expression stored in {@link #setOutputExpressions} is then evaluated
	 *  using the updated evaluation context.
	 *
	 * @param inputData
	 * @return Evaluated expressions using variables set from inputData
	 */
	public List<Double> computeOutput(List<Double> inputData) {
		List<String> dataNames = dataVariableNames;
		if (dataVariableNames.isEmpty()) {
			logger.info("Using default variable names (A..Z)");
			dataNames = defaultVariableNames;
		}

		if (dataNames.size() < inputData.size()) {
			throw new IllegalArgumentException("Number of input data names is less than size of input data!");
		}

		// Assign context variable values using input data values and data names :
		// (i.e. data variable called dataName[0] = inputData[0] etc)
		for(int i=0; i<inputData.size(); i++) {
			context.setVariable(dataNames.get(i), inputData.get(i));
		}

		// Old old-fashioned loop rather than stream so we can catch any exceptions and
		// rethrow with useful information to help identify the problematic expression
		List<Double> vals = new ArrayList<>();
		for(var stringExpr : outputExpressions.entrySet()) {
			try {
				vals.add(springExpressions.get(stringExpr.getKey()).getValue(context, Double.class));
			} catch(ExpressionException e) {
				String message = String.format("Problem evaluating expression with label '%s' (%s)", stringExpr.getKey(), stringExpr.getValue());
				throw new IllegalArgumentException(message, e);
			}
		}
		return vals;
	}

	/**
	 * Set a variable in the evaluation context to the specified value.
	 *
	 * @param name
	 * @param value
	 */
	public void setVariable(String name, Double value) {
		context.setVariable(name, value);
	}

	/**
	 * Return value of value from evaluation context
	 * @param name
	 * @return value or null if variable was not found
	 */
	public Object lookupVariable(String name) {
		return context.lookupVariable(name);
	}

	public Map<String, String> getOutputExpressions() {
		return outputExpressions;
	}

	public List<String> getOutputNames() {
		return outputExpressions.keySet().stream().toList();
	}

	public List<String> getOutputFormat() {
		if (!outputFormat.isEmpty()) {
			return outputFormat;
		}
		return Collections.nCopies(outputExpressions.size(), defaultFormat);
	}

	public void clearOutputExpressions() {
		outputExpressions = Collections.emptyMap();
	}

	/**
	 * Add an output expression to be evaluated in {@link #computeOutput(List)}.
	 *
	 * @param label user friendly label for the expression
	 * @param expression expression to add.
	 */
	public void addOutputExpression(String label, String expression) {
		if (outputExpressions == null || outputExpressions.isEmpty()) {
			outputExpressions = new LinkedHashMap<>();
		}
		this.outputExpressions.put(label, expression);
		parseSpringExpressions();
	}

	/**
	 * Set the output expressions to be evaluated in {@link #computeOutput(List)}.
	 *
	 * @param outputExpressions key = label, value = output expression
	 */
	public void setOutputExpressions(Map<String, String> outputExpressions) {
		this.outputExpressions = new LinkedHashMap<>(outputExpressions);
		parseSpringExpressions();
	}

	/**
	 * Update map of Spring {@link Expression} objects from current set of String outputExpressions
	 *
	 */
	private void parseSpringExpressions() {
		springExpressions = outputExpressions.entrySet()
			.stream()
			.collect(
				Collectors.toMap(Entry::getKey, ent-> parser.parseExpression(ent.getValue()))
			);
	}

	public List<String> getDataVariableNames() {
		return dataVariableNames;
	}

	public void setDataVariableNames(List<String> inputDataNames) {
		this.dataVariableNames = inputDataNames;
	}
}