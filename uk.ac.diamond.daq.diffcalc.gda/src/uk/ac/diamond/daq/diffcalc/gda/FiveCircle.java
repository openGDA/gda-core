/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.diffcalc.gda;

import java.util.Map;

public class FiveCircle extends AngleTransform {

	private ReferenceGeometry firstAngle;
	private double firstAngleValue;

	@Override
	protected boolean isReferencePositionValid(Map<ReferenceGeometry, Double> position) {
		return (position.get(firstAngle).equals(firstAngleValue)) && (position.size() == 6);
	}


	@Override
	public Map<ReferenceGeometry, Double> getReferenceGeometry(Map<String, Double> beamlinePosition) {
		Map<ReferenceGeometry, Double> reference = super.getReferenceGeometry(beamlinePosition);

		reference.put(firstAngle, firstAngleValue);
		return reference;
	}

	@Override
	public Map<String, Double> getDefaultConstraints() {
		return Map.of(firstAngle.getName(), firstAngleValue);
	}

	public ReferenceGeometry getFirstAngle() {
		return firstAngle;
	}

	public void setFirstAngle(ReferenceGeometry firstAngle) {
		this.firstAngle = firstAngle;
	}

	public double getFirstAngleValue() {
		return firstAngleValue;
	}

	public void setFirstAngleValue(double firstAngleValue) {
		this.firstAngleValue = firstAngleValue;
	}

}
