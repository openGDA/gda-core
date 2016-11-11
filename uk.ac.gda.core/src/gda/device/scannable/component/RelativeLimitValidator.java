/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.scannable.component;

import gda.device.DeviceException;
import gda.device.scannable.PositionConvertorFunctions;

import java.text.MessageFormat;

/**
 * Asserts that 'minDiff <= a-b <= maxDiff' for two elements of a position.
 */
public class RelativeLimitValidator implements PositionValidator {

	private Double minimumDifference;

	private Double maximumDifference;

	private Integer aIndex;

	private String aName;

	private Integer bIndex;

	private String bName;

	public void setMinimumDifference(Double minimumDifference) {
		this.minimumDifference = minimumDifference;
	}

	public Double getMinimumDifference() {
		return minimumDifference;
	}

	public void setMaximumDifference(Double maximumDifference) {
		this.maximumDifference = maximumDifference;
	}

	public Double getMaximumDifference() {
		return maximumDifference;
	}

	public void setaName(String aName) {
		this.aName = aName;
	}

	public String getaName() {
		return aName;
	}

	public void setbName(String bName) {
		this.bName = bName;
	}

	public String getbName() {
		return bName;
	}

	public void setaIndex(int aIndex) {
		this.aIndex = aIndex;
	}

	public int getaIndex() {
		return aIndex;
	}

	public void setbIndex(int bIndex) {
		this.bIndex = bIndex;
	}

	public int getbIndex() {
		return bIndex;
	}

	@Override
	public String toString() {
		if ((getMinimumDifference() == null) && (getMaximumDifference() == null)) {
			return "";
		}
		String msg = "";
		if (getMinimumDifference() != null) {
			msg += getMinimumDifference() + " <= ";
		}
		msg += MessageFormat.format("{0} - {1}", getaName(), getbName());

		if (getMaximumDifference() != null) {
			msg += " <= " + getMaximumDifference();
		}

		return msg;
	}

	@Override
	public String checkInternalPosition(Object[] internalPosition) throws DeviceException {
		if (internalPosition == null) {
			return null;
		}
		Double[] internalPositionArray = PositionConvertorFunctions.toDoubleArray(internalPosition);
		double aVal = internalPositionArray[getaIndex()];
		double bVal = internalPositionArray[getbIndex()];
		double difference = aVal - bVal;
		if ((getMinimumDifference() != null) && (difference < getMinimumDifference())) {
			return "Lower relative limit violation of '" + toString() + "'" + MessageFormat.format(", where {0} = {1} and {2} = {3}", getaName(), aVal, getbName(), bVal);
		}
		if ((getMaximumDifference() != null) && (difference > getMaximumDifference())) {
			return "Upper relative limit violation of '" + toString() + "'" + MessageFormat.format(", where {0} = {1} and {2} = {3}", getaName(), aVal, getbName(), bVal);
		}
		return null;
	}

}
