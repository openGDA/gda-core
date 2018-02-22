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

package uk.ac.gda.beans.exafs.b18;

import java.io.Serializable;

/**
 * Position of a scannable that should be moved at the start of each scan, e.g. in SampleEnvironmentIterator.
 * Contains :
 *  <li> name of scannable to be moved
 *  <li> description of scannable (for user identification in GUI only)
 *  <li> demand position for scannable
 *  <li> boolean flag for whether scannable is to be moved or not
 *  See also {@link SampleParameterMotorPositionsComposite}.
 */
public class SampleParameterMotorPosition implements Serializable {

	/** Name of scannable to be moved */
	private String scannableName;

	/** Helpful description (optional, for user identification only) */
	private String description;

	/** Whether scannable is to be moved at start of scan */
	private boolean doMove;

	/** name of 'getter' function for doMove field */
	public static final String DO_MOVE_GETTER_NAME = "getDoMove";

	/** name of 'getter' function for demandPosition field */
	public static final String DEMAND_POSITION_GETTER_NAME = "getDemandPosition";

	/** Position scannable is to be moved to */
	private double demandPosition;

	public SampleParameterMotorPosition() {
		scannableName = "";
		description = "";
		doMove = false;
		demandPosition = 0;
	}

	public SampleParameterMotorPosition(String scannableName, double demandPosition, boolean moveToPosition) {
		this.scannableName = scannableName;
		description = scannableName;
		this.doMove = moveToPosition;
		this.demandPosition = demandPosition;
	}

	/** Name of scannable to be moved */
	public String getScannableName() {
		return scannableName;
	}
	public void setScannableName(String scannableName) {
		this.scannableName = scannableName;
	}

	/** Description of scannable (for user identification in sample parameters GUI only) */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** Whether scannble should be moved to its demand position at the start of the scan */
	public boolean getDoMove() {
		return doMove;
	}
	public void setDoMove(boolean moveToPosition) {
		this.doMove = moveToPosition;
	}

	/** Position scannable should be moved to at the start of the scan */
	public double getDemandPosition() {
		return demandPosition;
	}
	public void setDemandPosition(double position) {
		this.demandPosition = position;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(demandPosition);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (doMove ? 1231 : 1237);
		result = prime * result + ((scannableName == null) ? 0 : scannableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SampleParameterMotorPosition other = (SampleParameterMotorPosition) obj;
		if (Double.doubleToLongBits(demandPosition) != Double.doubleToLongBits(other.demandPosition))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (doMove != other.doMove)
			return false;
		if (scannableName == null) {
			if (other.scannableName != null)
				return false;
		} else if (!scannableName.equals(other.scannableName))
			return false;
		return true;
	}

}
