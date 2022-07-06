/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.richbeans.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.richbeans.api.event.ValueEvent;
import org.eclipse.richbeans.api.event.ValueListener;
import org.eclipse.richbeans.api.widget.IExpressionManager;
import org.eclipse.richbeans.api.widget.IExpressionWidget;
import org.eclipse.richbeans.api.widget.IFieldProvider;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.nfunk.jep.ASTVarNode;
import org.nfunk.jep.JEP;
import org.nfunk.jep.Node;
import org.nfunk.jep.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.util.list.SortNatural;

/**
 * Allows the expression only to be a function of field values of fields in the bean passed in.
 */
public class BeanExpressionManager implements IExpressionManager, ValueListener {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(BeanExpressionManager.class);

	private double value;
	private boolean expressionValid;
	private String expression;
	private IFieldProvider provider;
	private IExpressionWidget widget;
	private Collection<String> fields;
	private JEP jepParser;
	private ExpressionContentProposalProvider proposalProvider;
	private List<IFieldWidget> precedents = new ArrayList<IFieldWidget>(7);

	public BeanExpressionManager(final IExpressionWidget widget, IFieldProvider prov) {

		this.widget = widget;
		this.provider = prov;

		// The parser which will check expressions.
		this.jepParser = new JEP();
		jepParser.addStandardFunctions();
		jepParser.addStandardConstants();
		jepParser.setAllowUndeclared(false);
		jepParser.setImplicitMul(true);
	}

	@Override
	public double getExpressionValue() {
		return value;
	}

	@Override
	public String getExpression() {
		return expression;
	}

	@Override
	public void setExpression(String expression) {

		this.expression = expression;

		try {
			if (expression == null) {
				value = Double.NaN;
				expressionValid = false;
			}
			this.value = calculateValue();
			this.expressionValid = !Double.isNaN(value) && !Double.isInfinite(value);

			updateListeners();

		} catch (Exception ne) {
			clearListeners();
			value = Double.NaN;
			expressionValid = false;
		}
	}

	/**
	 * Removes old listeners and adds new ones.
	 *
	 * @throws ParseException
	 */
	private void updateListeners() throws Exception {

		clearListeners();
		if (!expressionValid)
			return;

		// NOTE newer versions of JEP have TreeAnalyzer for this...
		final Node node = jepParser.parse(expression);
		parseNode(node, precedents);

		for (IFieldWidget wid : precedents) {
			wid.addValueListener(this);
		}
	}

	private void clearListeners() {
		for (IFieldWidget widget : precedents) {
			try {
				widget.removeValueListener(this);
			} catch (Throwable ignored) {
				// Intentional, keep trying to clear the listeners.
			}
		}
		precedents.clear();
	}

	@Override
	public void valueChangePerformed(ValueEvent e) {
		if (e.getFieldName() == null)
			return;
		jepParser.addVariable(e.getFieldName(), e.getDoubleValue());
		this.value = jepParser.getValue();
		widget.setExpressionValue(value);
	}

	/**
	 * Recursive node parsing
	 *
	 * @param node
	 * @param precedents
	 * @throws Exception
	 */
	private void parseNode(Node node, List<IFieldWidget> precedents) throws Exception {

		if (node instanceof ASTVarNode) {
			IFieldWidget widget = getWidget((ASTVarNode) node);
			if (widget != null)
				precedents.add(widget);
			return;
		}

		for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
			final Node c = node.jjtGetChild(i);
			parseNode(c, precedents);
		}
	}

	private IFieldWidget getWidget(ASTVarNode node) {
		try {
			return provider.getField(node.getName());
		} catch (Exception ne) {
			return null;
		}
	}

	private double calculateValue() {
		for (String field : fields) {
			Object value;
			try {
				value = provider.getFieldValue(field);
			} catch (Exception e) {
				continue;
			}
			if (!(value instanceof Number))
				continue;
			jepParser.addVariable(field, ((Number) value).doubleValue());
		}
		jepParser.parseExpression(getExpression());
		return jepParser.getValue();
	}

	@Override
	public Collection<String> getAllowedSymbols() throws Exception {
		return fields;
	}

	/**
	 * Assumes that the fields are already sorted.
	 */
	@Override
	public void setAllowedSymbols(final Collection<String> fs) throws Exception {
		// Remove duplicates
		final Set<String> fieldSet = new HashSet<String>(fs);

		// Remove self
		if (widget.getFieldName() != null)
			fieldSet.remove(widget.getFieldName());

		// Sort list
		final List<String> fieldList = new ArrayList<String>(fieldSet.size());
		fieldList.addAll(fieldSet);
		Collections.sort(fieldList, new SortNatural<String>(false));
		fields = fieldList;

		// Create content helper
		final String[] fieldsArray = fields.toArray(new String[fields.size()]);
		if ((widget.getControl()) instanceof StyledText  && widget.isExpressionAllowed()) {
			if (proposalProvider == null) {
				proposalProvider = new ExpressionContentProposalProvider(fieldsArray, widget);
				ContentProposalAdapter adapter = new ContentProposalAdapter((Control) widget.getControl(),
						new StyledTextContentAdapter(), proposalProvider, null, null);
				adapter.setLabelProvider(new ExpressionLabelProvider(fields));
				adapter.setPropagateKeys(true);
				adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);

			} else {
				proposalProvider.setProposals(fieldsArray);
			}
		}

	}

	@Override
	public boolean isExpressionValid() {
		return expressionValid;
	}

	@Override
	public String getValueListenerName() {
		return widget.getFieldName() + " " + getClass().getName();
	}
}