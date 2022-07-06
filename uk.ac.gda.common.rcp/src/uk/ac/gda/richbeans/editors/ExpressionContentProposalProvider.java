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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.richbeans.api.widget.IExpressionWidget;
import org.eclipse.swt.widgets.Control;

public class ExpressionContentProposalProvider implements IContentProposalProvider {

	private List<String>      fields;
	private IExpressionWidget expressionContainer;

	public ExpressionContentProposalProvider(String[] proposals, IExpressionWidget expressionContainer) {
		super();
		this.expressionContainer = expressionContainer;
		setProposals(proposals);
	}

	@Override
	public IContentProposal[] getProposals(String contents, int position) {

		if (!(((Control) expressionContainer.getControl()).isFocusControl())) {
			return new IContentProposal[]{};
		}
		if (!expressionContainer.isExpressionParseRequired(contents)) {
			return new IContentProposal[]{};
		}

		final List<String> items = new ArrayList<String>();

		final String lastTerm = ExpressionUtils.getLastTerm(contents.substring(0,position));
		try {
			Double.parseDouble(lastTerm);
			return new IContentProposal[]{};
		} catch (Exception ignored) {

		}

		if ("".equals(lastTerm)) {
			items.addAll(fields);
			items.addAll(ExpressionUtils.getConstants());
			items.addAll(ExpressionUtils.getFunctionsWithOpeningBrackets());

		} else {
			filter(lastTerm, fields, items);
			filter(lastTerm, ExpressionUtils.getFunctions(), items, "(");
			filter(lastTerm, ExpressionUtils.getConstants(), items);
		}

		final List<IContentProposal> list = new ArrayList<IContentProposal>(items.size());
		for (String var : items) list.add(makeContentProposal(var));

		return list.toArray(new IContentProposal[list.size()]);
	}

	private void filter(final String lastTerm, final List<String> list, final List<String> items, String... appends) {
		for (String var : list) {
			if (var.length() >= lastTerm.length() && var.substring(0, lastTerm.length()).equalsIgnoreCase(lastTerm)) {
				items.add(var+(appends!=null&&appends.length>0?appends[0]:""));
			}
		}
	}

	/**
	 * Assumes that the list passed in is the GDA variables which have values.
	 *
	 * Adds contants to the list.
	 */
	public void setProposals(String[] ev) {
		this.fields = Collections.unmodifiableList(Arrays.asList(ev));
	}

	/*
	 * Make an IContentProposal for showing the specified String.
	 */
	private IContentProposal makeContentProposal(final String proposal) {
		return new IContentProposal() {
			@Override
			public String getContent() {
				return proposal;
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public String getLabel() {
				return proposal;
			}

			@Override
			public int getCursorPosition() {
				return proposal.length();
			}
		};
	}
}