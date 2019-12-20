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

package gda.mscan;

import gda.mscan.processor.IClauseElementProcessor;

public class ValidationUtils {

	/**
	 * Throws an [@link IllegalArgumentException} using the supplied message prefixed with a standard header if the
	 * rejection condition is true;.
	 *
	 * @param rejectionCondition	The boolean to be evaluated to determin if an exception should be thrown (if true)
	 * @param failureMessage		The message to be appended to the standard header
	 *
	 * @throws IllegalArgumentException	if the rejection condition is true
	 */
	public static void throwIf(final boolean rejectionCondition, final String failureMessage) {
		if (rejectionCondition) {
			throwOnInvalidCommand(failureMessage);
		}
	}

	public static void throwOnInvalidCommand(final String message) {
		throw new IllegalArgumentException(String.format("Invalid MScan command - %s", message));
	}

	/**
	 * Provides uniform Null checking for all types
	 *
	 * @param obj		The object instance to be checked
	 * @param typeName	The type name to be used in the exception message
	 *
	 * @throws IllegalArgumentException if the supplied object is null
	 */
	public static void nullCheck(final Object obj, final String typeName) {
		if (obj == null) {
			throwOnInvalidCommand(String.format("The supplied %s was null", typeName));
		}
	}

	/**
	 * Provides an in line null check throwing if this fails
	 *
	 * @param toReturn	{@link IClauseElementProcessor} to be evaluated and returned if it is not null
	 * @return			The incoming parameter if it is not null
	 * throws IllegaArgumentException if the incoming parameter is null
	 */
	public static IClauseElementProcessor withNullProcessorCheck(final IClauseElementProcessor toReturn) {
		nullCheck(toReturn,"processor");
		return toReturn;
	}
}
