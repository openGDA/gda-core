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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

public class FourCircle implements AngleTransform {

	private ReferenceGeometry firstAngle;

	private ReferenceGeometry secondAngle;

	private double firstAngleValue;
	private double secondAngleValue;

	private Map<ReferenceGeometry, String> referenceAnglesToBeamlineMotors;

	private Map<ReferenceGeometry, Map<AngleScaling, Double>> referenceScaling;

	private boolean isReferencePositionValid(Map<ReferenceGeometry, Double> position) {
		boolean firstAnglesIdentical = position.get(firstAngle).equals(firstAngleValue);
		boolean secondAnglesIdentical = position.get(secondAngle).equals(secondAngleValue);

		return firstAnglesIdentical && secondAnglesIdentical;

	}

	private boolean isBeamlinePositionValid(Map<String, Double> position) {
		List<String> requiredMotors = new ArrayList<>(referenceAnglesToBeamlineMotors.values());
		List<String> motors = new ArrayList<>(position.keySet());

		List<Boolean> requiredMotorsInGivenPosition = requiredMotors.stream().map(motors::contains).toList();

		return (requiredMotorsInGivenPosition.contains(false) || (requiredMotors.size() != motors.size()))? false: true;

	}

	@Override
	public Map<ReferenceGeometry, Double> getReferenceGeometry(Map<String, Double> beamlinePosition) {
		Boolean isValid = isBeamlinePositionValid(beamlinePosition);
		if (isValid.equals(false)) {
			throw new IllegalArgumentException("Beamline position non-valid, check motor names and number of motors.");
		}

		var reference = new EnumMap<ReferenceGeometry, Double>(ReferenceGeometry.class);

		reference.put(firstAngle, firstAngleValue);
		reference.put(secondAngle, secondAngleValue);

		for (Entry<ReferenceGeometry, String> item: referenceAnglesToBeamlineMotors.entrySet()) {
			ReferenceGeometry referenceAngle = item.getKey();
			String beamlineAngle = item.getValue();

			Double angleValue = beamlinePosition.get(beamlineAngle);

			if (!Objects.isNull(referenceScaling)) {
				Map<AngleScaling, Double> scaling = referenceScaling.get(referenceAngle);
				if (!Objects.isNull(scaling)) {
					angleValue = scaling.get(AngleScaling.MULT) * angleValue + scaling.get(AngleScaling.ADD);
				}
			}
			reference.put(referenceAngle, angleValue);

		}
		return reference;
	}

	@Override
	public Map<String, Double> getBeamlineGeometry(Map<ReferenceGeometry, Double> referencePosition) {
		Boolean isValid = isReferencePositionValid(referencePosition);

		if (isValid.equals(false)) {
			throw new IllegalArgumentException("Reference position non-valid, check fixed angle values.");
		}
		var beamline = new HashMap<String, Double>();

		for (Entry<ReferenceGeometry, String> item: referenceAnglesToBeamlineMotors.entrySet()) {
			ReferenceGeometry referenceAngle = item.getKey();
			String beamlineAngle = item.getValue();

			Double angleValue = referencePosition.get(referenceAngle);

			if (!Objects.isNull(referenceScaling)) {
				Map<AngleScaling, Double> scaling = referenceScaling.get(referenceAngle);

				if (!Objects.isNull(scaling)) {
					angleValue = (angleValue - scaling.get(AngleScaling.ADD)) / scaling.get(AngleScaling.MULT);
				}
			}
			beamline.put(beamlineAngle, angleValue);
		}

		return beamline;
	}

	@Override
	public Map<String, Double> defaultConstraints() {
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

	public Map<ReferenceGeometry, String> getReferenceAnglesToBeamlineMotors() {
		return referenceAnglesToBeamlineMotors;
	}

	public void setReferenceAnglesToBeamlineMotors(Map<ReferenceGeometry, String> referenceToBeamlineMapping) {
		this.referenceAnglesToBeamlineMotors = referenceToBeamlineMapping;
	}

	public Map<ReferenceGeometry, Map<AngleScaling, Double>> getReferenceScaling() {
		return referenceScaling;
	}

	public void setReferenceScaling(
			Map<ReferenceGeometry, Map<AngleScaling, Double>> referenceScaling) {
		this.referenceScaling = referenceScaling;
	}

}
