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

package uk.ac.diamond.daq.diffcalc.gda;

import java.util.Map;

public class FourCircle extends AngleTransform {

	private ReferenceGeometry firstAngle;
	private ReferenceGeometry secondAngle;

	private double firstAngleValue;
	private double secondAngleValue;

	@Override
	protected boolean isReferencePositionValid(Map<ReferenceGeometry, Double> position) {
		return position.size() == 6 &&
			   position.get(firstAngle).equals(firstAngleValue) &&
			   position.get(secondAngle).equals(secondAngleValue);
	}

	@Override
	public Map<ReferenceGeometry, Double> getReferenceGeometry(Map<String, Double> beamlinePosition) {
		Map<ReferenceGeometry, Double> reference = super.getReferenceGeometry(beamlinePosition);

		reference.put(firstAngle, firstAngleValue);
		reference.put(secondAngle, secondAngleValue);

		return reference;
	}

	@Override
	public Map<String, Double> getDefaultConstraints() {
		return Map.of(
				firstAngle.getName(), firstAngleValue,
				secondAngle.getName(), secondAngleValue
				);
	}

	public ReferenceGeometry getFirstAngle() {
		return firstAngle;
	}

	public void setFirstAngle(ReferenceGeometry firstAngle) {
		this.firstAngle = firstAngle;
	}

	public ReferenceGeometry getSecondAngle() {
		return secondAngle;
	}

	public void setSecondAngle(ReferenceGeometry secondAngle) {
		this.secondAngle = secondAngle;
	}

	public double getFirstAngleValue() {
		return firstAngleValue;
	}

	public void setFirstAngleValue(double firstAngleValue) {
		this.firstAngleValue = firstAngleValue;
	}

	public double getSecondAngleValue() {
		return secondAngleValue;
	}

	public void setSecondAngleValue(double secondAngleValue) {
		this.secondAngleValue = secondAngleValue;
	}

}
