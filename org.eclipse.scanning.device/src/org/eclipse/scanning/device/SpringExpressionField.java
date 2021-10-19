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

package org.eclipse.scanning.device;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;

/**
 * A {@link MetadataNode} field that creates a {@link DataNode} the value of which is the result of a Spring Expression.
 * If the evaluation of the expression fails, it will return the error message instead.
 *
 * This class is designed to support {@Link Findable} objects as well as object defined in Jython Name Space.
 *
 * Example of usage in bean definition:
 * <pre>
 * {@code
<bean class="org.eclipse.scanning.device.SpringExpressionField">
	<property name="name" value="polarisation"/>
	<property name="expression" value="@idscannable.getPosition()[1]"/>
</bean>
 * }
 * </pre>
 * where '{@code @idscannable}' is a reference to a scannable instance defined in SPEL object format.
 *
 * @since 9.22
 * @author Fajin Yuan
 */
public class SpringExpressionField extends AbstractMetadataField {

	private static final Logger logger = LoggerFactory.getLogger(SpringExpressionField.class);
	private String expression;

	public SpringExpressionField() {
		// no-arg constructor for spring initialization
	}

	public SpringExpressionField(String fieldName, String expression) {
		super(fieldName);
		setExpression(expression);
	}

	public SpringExpressionField(String fieldName, String expression, String unit) {
		super(fieldName);
		setExpression(expression);
		setUnits(unit);
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	@Override
	protected DataNode createDataNode() throws NexusException {
		final Object value = getExpressionValue();
		return createDataNode(value);
	}

	private Object getExpressionValue() {
		final ExpressionParser parser = new SpelExpressionParser();
		final var context = new StandardEvaluationContext();
		context.setBeanResolver((ec, name) -> Finder.find(name) != null ? Finder.find(name) : InterfaceProvider.getJythonNamespace().getFromJythonNamespace(name));
		try {
			return parser.parseExpression(getExpression()).getValue(context);
		} catch (EvaluationException e) {
			logger.error("Evaluation of expression {} failed.", getExpression(), e);
			return e.getMessage();
		}
	}
}
