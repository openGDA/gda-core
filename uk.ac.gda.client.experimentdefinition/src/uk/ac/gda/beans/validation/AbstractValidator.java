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

package uk.ac.gda.beans.validation;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import uk.ac.gda.client.experimentdefinition.IExperimentObject;
import uk.ac.gda.doe.DOEUtils;

/**
 * Base class for objects to check the values held by the IRichbeans/XML files are correct.
 * <p>
 * Needs to be subclassed to provide beamline specific implementations.
 * <p>
 * The concrete class in use should be referenced in a contribution to the uk.ac.gda.client.experimentdefinition
 * extension point.
 */
public abstract class AbstractValidator {

	/**
	 * Call to validate, throws various exceptions. The ValidationBean can contain many fields to validate when the
	 * beans being checked need to look at each others values.
	 * 
	 * @param bean
	 * @throws InvalidBeanException
	 */
	public abstract void validate(final IExperimentObject bean) throws InvalidBeanException;

	protected InvalidBeanMessage checkBounds(final String name, final Number value, final Number lower,
			final Number upper, final List<InvalidBeanMessage> errors, final String... messages) {

		// Nothing to check
		if (value == null)
			return null;

		if (lower != null && !Double.isNaN(lower.doubleValue())) {
			if (value.doubleValue() < lower.doubleValue()) {
				InvalidBeanMessage msg = new InvalidBeanMessage("'" + name + "' is smaller than the lower bound of '"
						+ lower + "'", messages);
				msg.setLabel(name);
				errors.add(msg);
				return msg;
			}
		}
		if (upper != null && !Double.isNaN(upper.doubleValue())) {
			if (value.doubleValue() > upper.doubleValue()) {
				InvalidBeanMessage msg = new InvalidBeanMessage("'" + name + "' is larger than the upper bound of '"
						+ upper + "'", messages);
				msg.setLabel(name);
				errors.add(msg);
				return msg;
			}
		}

		return null;
	}

	protected void checkRangeBounds(final String name, final String range, final double d, final double e,
			final List<InvalidBeanMessage> errors, final String message) {

		final List<? extends Number> vals = DOEUtils.expand(range);
		for (Number val : vals) {
			checkBounds(name, val, d, e, errors, message);
		}
	}

	protected InvalidBeanMessage checkNotNull(final String name, final String value,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (value == null || "".equals(value)) {
			InvalidBeanMessage msg = new InvalidBeanMessage("'" + name + "' has not been set", messages);
			msg.setLabel(name);
			errors.add(msg);
			return msg;
		}
		return null;

	}

	protected InvalidBeanMessage checkValue(final String name, final String value, final String[] values,
			final List<InvalidBeanMessage> errors, final String... messages) {
		if (value == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + name + " has no value and this is not allowed.",
					messages);
			msg.setLabel(name);
			errors.add(msg);
			return msg;
		}

		if (!Arrays.asList(values).contains(value)) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + name + " of '" + value
					+ "' is not allowed. The valid choices are:\n" + Arrays.asList(values).toString() + ".", messages);
			msg.setLabel(name);
			errors.add(msg);
			return msg;
		}

		return null;
	}

	protected InvalidBeanMessage checkFileExists(final String label, final String fileName, final String folderName,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (!(new File(folderName + fileName)).exists()) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " of '" + fileName + "' is not existing.",
					messages);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}

		return null;
	}

	protected InvalidBeanMessage checkRegExp(final String label, final String value, final String regExp,
			final List<InvalidBeanMessage> errors, final String... messages) {

		if (value == null) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " has no value and this is not allowed.",
					messages);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}
		if (!value.matches(regExp)) {
			InvalidBeanMessage msg = new InvalidBeanMessage("The " + label + " of '" + value + "' is not allowed.",
					messages);
			msg.setLabel(label);
			errors.add(msg);
			return msg;
		}

		return null;
	}


	protected void setFileName(List<InvalidBeanMessage> errors, String fileName) {
		if (errors == null)
			return;
		if (errors.isEmpty())
			return;
		if (fileName == null)
			return;

		for (InvalidBeanMessage invalidBeanMessage : errors) {
			invalidBeanMessage.setFileName(fileName);
		}
	}

}
