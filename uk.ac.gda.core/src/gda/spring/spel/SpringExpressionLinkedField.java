/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.spring.spel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.SymbolicNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.scanning.device.LinkedField;
import org.eclipse.scanning.device.MetadataNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * A {@link MetadataNode} that add a link to a {@link DataNode} at a path calculated from a given Spring Expression.
 * If the evaluation of the expression fails, it will return the error message instead.
 *
 * The expression is designed to support {@Link Findable} objects as well as object defined in Jython Name Space in GDA.
 * It also support a list of SpEL-functions defined in {@link SpELUtils} class. SpEL functions when used must be defined in a {@link Map}
 * with the entry key to be the name of the method, the entry value to be a list of fully qualified class names.
 *
 * Example of usage in bean definition:
 * <pre>
 * {@code
	<bean class="gda.spring.spel.SpringExpressionLinkedField" init-method="init">
		<property name="name" value="energy"/>
		<property name="expression" value="(100.0 - @iddgap.getPosition()) > 2.0 ? '/entry/instrument/id/idd/energy' : '/entry/instrument/id/idu/energy'"/>
	</bean>
 * }
 * </pre>
 * where '{@code @iddgap}' is a reference to a scannable instance defined in SpEL object format.
 *
 * @since 9.37
 * @author Fajin Yuan
 */
public class SpringExpressionLinkedField extends LinkedField {

	private static final Logger logger = LoggerFactory.getLogger(SpringExpressionLinkedField.class);
	private String  expression;
	private ExpressionParser parser;
	private StandardEvaluationContext context;
	private Map<String, List<Class<?>>> functionNamesParameterTypes = new HashMap<>();

	public SpringExpressionLinkedField() {
		// no-arg constructor for spring initialization
	}

	public SpringExpressionLinkedField(String fieldName, String expression) {
		super(fieldName, null);
		setExpression(expression);
	}

	public SpringExpressionLinkedField(String fieldName, String externalFilePath, String expression) {
		super(fieldName, externalFilePath, null);
		setExpression(expression);
	}

	public void init() throws NoSuchMethodException, SecurityException {
		parser = new SpelExpressionParser();
		context = new StandardEvaluationContext();
		context.setBeanResolver((ec, name) -> Finder.find(name) != null ? Finder.find(name) : InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name));
		if (!functionNamesParameterTypes.isEmpty()) {
			// register function in SpELUtils class to be used
			for (Map.Entry<String, List<Class<?>>> e : functionNamesParameterTypes.entrySet()) {
				Class<?>[] parameterTypes = new Class[e.getValue().size()];
				Class<?>[] array = e.getValue().toArray(parameterTypes);
				context.registerFunction(e.getKey(), SpELUtils.class.getDeclaredMethod(e.getKey(), array));
			}
		}
	}

	@Override
	public SymbolicNode doCreateNode() throws NexusException {
		super.setLinkPath(getExpressionValue());
		return super.doCreateNode();
	}

	private String getExpressionValue() {
		try {
			return parser.parseExpression(getExpression()).getValue(context, String.class);
		} catch (EvaluationException e) {
			logger.error("Evaluation of expression {} failed.", getExpression(), e);
			return e.getMessage();
		}
	}

	public Map<String, List<Class<?>>> getFunctionNamesParameterTypes() {
		return functionNamesParameterTypes;
	}

	public void setFunctionNamesParameterTypes(Map<String, List<Class<?>>> functionNamesParameterTypes) {
		this.functionNamesParameterTypes = functionNamesParameterTypes;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}
}
