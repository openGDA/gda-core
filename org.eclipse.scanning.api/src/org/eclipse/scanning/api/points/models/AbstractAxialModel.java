/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.eclipse.scanning.api.points.models;

abstract class AbstractAxialModel extends AbstractPointsModel implements IBoundsToFit {

	private boolean boundsToFit;

	protected AbstractAxialModel(){
		defaultBoundsToFit();
	}

	@Override
	public boolean isBoundsToFit() {
		return boundsToFit;
	}

	@Override
	public void setBoundsToFit(boolean boundsToFit) {
		pcs.firePropertyChange(PROPERTY_DEFAULT_BOUNDS_FIT, this.boundsToFit, boundsToFit);
		this.boundsToFit = boundsToFit;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (boundsToFit ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj))
			return false;
		AbstractAxialModel other = (AbstractAxialModel) obj;
		if (boundsToFit != other.boundsToFit)
			return false;
		return true;
	}

}
