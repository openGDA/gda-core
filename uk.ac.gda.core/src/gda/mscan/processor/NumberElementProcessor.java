/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import gda.mscan.ClauseContext;

/**
 * A Clause Element Processor for Numeric elements
 */
public class NumberElementProcessor extends ElementProcessorBase<Number> {

	public NumberElementProcessor(final Number source) {
		super(source);
	}

	/**
	 * Confirm that a {@link Number} is allowed as the next type to be processed in the MScan clause grammar
	 * and if so, add the {@link Number} used in construction to the context's currently active list, provided
	 * the maximum size has not been exceeded.{@link Number}.class is explicitly passed as the class to lookup in to
	 * {@link #isValidElement(ClauseContext, String, Class)} so that a single {@link Number} entry in the
	 * {@link ClauseContext} grammar can cover integers, doubles etc.
	 *
	 * @param context	The {@link ClauseContext} object being completed for the current MSCan clause
	 * @param index		The index of the clause element associated with the processor within the current clause
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * 					IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	@Override
	public void process(final ClauseContext context, final int index) {
		rejectIfFirstElement(index);
		if(isValidElement(context, this.getClass().getName(), Number.class)) {
			context.addParam(enclosed);
		}
	}

	@Override
	public boolean hasNumber() {
		return true;
	}

	/**
	 * Retrieve the value of the enclosed {@link Number}
	 *
	 * @return the value of the enclosed {@link Number}
	 */
	@Override
	public String getElementValue() {
		return String.valueOf(enclosed);
	}
}
