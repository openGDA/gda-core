/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.hrpd.sample;

public class SampleDefinitionException extends RuntimeException {

	private static final String INVALID_MESSAGE_FORMAT = "Error in sample definition (sample: %s, field: %s) - %s";
	private static final String MISSING_MESSAGE_FORMAT = "Value is required for field %s (sample: %s)";

	private final String field;
	private final String sample;

	public SampleDefinitionException(String message, String sample, String field) {
		super(message);
		this.field = field;
		this.sample = sample;
	}

	// For lookups that reference by rows (excel)
	public static SampleDefinitionException parseError(int sample, String field, Object message) {
		return parseError(Integer.toString(sample), field, message);
	}

	public static SampleDefinitionException parseError(String sample, String field, Object message) {
		return new SampleDefinitionException(String.format(INVALID_MESSAGE_FORMAT, sample, field, message), sample, field);
	}

	public static SampleDefinitionException missing(int sample, String field) {
		return missing(Integer.toString(sample), field);
	}

	public static SampleDefinitionException missing(String sample, String field) {
		return new SampleDefinitionException(String.format(MISSING_MESSAGE_FORMAT, field, sample), sample, field);
	}

	public String getField() {
		return field;
	}

	public String getSample() {
		return sample;
	}
}
