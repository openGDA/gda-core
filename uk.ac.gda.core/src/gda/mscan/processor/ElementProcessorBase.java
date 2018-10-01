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

import java.util.List;

import gda.device.Scannable;
import gda.mscan.ClauseContext;

/**
 * A base class for implementers of {@link IClauseElementProcessor} containing common methods used in validation.
 */
public abstract class ElementProcessorBase<T> implements IClauseElementProcessor{

	protected final T enclosed;

	protected ElementProcessorBase (final T source) {
		enclosed = source;
	}

	/**
	 * Retrieves the list of types that are allowed to follow the type of the previous clause element by the
	 * {@link ClauseContext} grammar so that we can check if the current element type is allowed
	 *
	 * @param context	The {@link ClauseContext} being filled in as the current clause is parsed
	 *
	 * @return			A list of types that are allowed to follow elements of the previous element's type
	 *
	 * @throws			IllegalStateException if the previous element of the context is null (this should never occur)
	 * 					IllegalArgumentException if there is no list of successors corresponding to the type of the
	 * 					previous element i.e. it is not a valid element type
	 */
	protected List<Class<?>> lookupSuccessorsForPrevious(final ClauseContext context) {
		Class<?> previous = context.getPreviousType();
		if (previous == null) {
			throw new IllegalStateException(String.format(
					"Error: the ClauseContext Object contains a null previousType, this should not be possible"));
		}
		List<Class<?>> successors = context.grammar().get(previous);
		if (successors == null) {
			throw new IllegalArgumentException(String.format(
					"Invalid MScan command - The clause grammar does not feature the type %d", previous));
		}
		return successors;
	}

	/**
	 * Checks that the type of the enclosed field is present as an allowed successor to the previous clause element
	 *
	 * @param context			The {@link ClauseContext} related to the clause currently being parsed
	 * @param processorName		The name of the processor subclass for Exception message purposes
	 *
	 * @return					true if the type associated with this processor is a valid successor element
	 *
	 * @throws					UnsupportedOperationException if this is not the case
	 */
	protected boolean isValidElement(final ClauseContext context, final String processorName) {
		return isValidElement(context, processorName, enclosed.getClass());
	}

	/**
	 * Checks that the supplied type is present as an allowed successor to the previous clause element. This allows a
	 * common superclass to be used for types like Integer, Double etc.
	 *
	 * @param context			The {@link ClauseContext} related to the clause currently being parsed
	 * @param processorName		The name of the processor subclass for Exception message purposes
	 * @param lookupUsing		The type to be checked against the {@link ClauseContext} grammar
	 *
	 * @return					true if the type associated with this processor is a valid successor element
	 *
	 * @throws					UnsupportedOperationException if this is not the case
	 */
	protected boolean isValidElement(final ClauseContext context,
									 final String processorName, final Class<?> lookupUsing) {
		List<Class<?>> successors = lookupSuccessorsForPrevious(context);
		if (successors.contains(lookupUsing)) {
			return true;
		}
		throw new UnsupportedOperationException(String.format(
			"Invalid MScan command - Out of sequence call to %s; previousType was %s, allowable successors types are %s",
				processorName, context.getPreviousType(), successors));
	}

	/**
	 * Rejects elements of the current associated type if they are the first element in the clause, which can only be
	 * a {@link Scannable}
	 *
	 * @param index				The position of the current element in the clause
	 *
	 * @throws					UnsupportedOperationException if the element type is not allowed at postiion 0
	 */
	protected void rejectIfFirstElement(final int index) {
		if (index == 0) {
			throw new UnsupportedOperationException(String.format(
					"%s cannot be the first element of a clause", enclosed.getClass()));
		}
	}

	/**
	 * Gets the element used to create the processor instance
	 *
	 * @return	the source element object used to create the processor instance
	 */
	@Override
	public T getElement() {
		return enclosed;
	}

}
