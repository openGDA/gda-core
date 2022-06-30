/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view.configuration;

import java.util.Objects;

import gda.factory.FindableBase;
import uk.ac.gda.client.properties.acquisition.AcquisitionTemplate;

/**
 * Holds configuration unique to tomography which does not fit into {@link AcquisitionTemplate}
 */
public class TomographyConfiguration extends FindableBase {

	/**
	 * Name of axis used for configuring in- and out-of-beam position
	 */
	private String lateralAxis;

	private double flatFieldDisplacement;

	public String getLateralAxis() {
		return lateralAxis;
	}

	public void setLateralAxis(String lateralAxis) {
		this.lateralAxis = lateralAxis;
	}

	public double getFlatFieldDisplacement() {
		return flatFieldDisplacement;
	}

	public void setFlatFieldDisplacement(double flatFieldDisplacement) {
		this.flatFieldDisplacement = flatFieldDisplacement;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(flatFieldDisplacement, lateralAxis);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TomographyConfiguration other = (TomographyConfiguration) obj;
		return Double.doubleToLongBits(flatFieldDisplacement) == Double.doubleToLongBits(other.flatFieldDisplacement)
				&& Objects.equals(lateralAxis, other.lateralAxis);
	}

}
