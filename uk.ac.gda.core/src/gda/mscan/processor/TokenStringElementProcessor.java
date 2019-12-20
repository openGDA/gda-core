/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.mscan.processor;

import java.util.List;

import gda.mscan.ClausesContext;

/**
 * A Clause Element Processor for elements which are a {@link String} of space separated tokens.
 */
public class TokenStringElementProcessor extends ElementProcessorBase<String> {

	public TokenStringElementProcessor(final String source) {
		super(source);
	}

	@Override
	public void process(ClausesContext context, final List<IClauseElementProcessor> clauseProcessors, int index) {
		rejectIfFirstElement(index);
	}

	@Override
	public String getElementValue() {
		return enclosed;
	}

	@Override
	public boolean hasTokenString() {
		return true;
	}



}
