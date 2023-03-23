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

import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AngleTransform {
	protected Map<ReferenceGeometry, String> referenceAnglesToBeamlineMotors;
	protected Map<ReferenceGeometry, Map<AngleScaling, Double>> referenceScaling = new EnumMap<>(ReferenceGeometry.class);

	protected abstract boolean isReferencePositionValid(Map<ReferenceGeometry, Double> position);
	protected boolean isBeamlinePositionValid(Map<String, Double> position) {
		List<String> requiredMotors = new ArrayList<>(referenceAnglesToBeamlineMotors.values());
		List<String> motors = new ArrayList<>(position.keySet());

		return motors.size() == requiredMotors.size() && motors.containsAll(requiredMotors);

	}

	protected Double calculateReferenceAngle(Map<String, Double> beamlinePosition, ReferenceGeometry referenceAngle,
			String beamlineAngle) {
		double angleValue = beamlinePosition.get(beamlineAngle);

		Map<AngleScaling, Double> scaling = referenceScaling.get(referenceAngle);
		if (scaling != null) {
			angleValue = scaling.get(AngleScaling.MULT) * angleValue + scaling.get(AngleScaling.ADD);
		}
		return angleValue;
	}

	protected Double calculateBeamlineAngle(Map<ReferenceGeometry, Double> referencePosition, ReferenceGeometry referenceAngle) {
		Double angleValue = referencePosition.get(referenceAngle);

		Map<AngleScaling, Double> scaling = referenceScaling.get(referenceAngle);
		if (scaling != null) {
			angleValue = (angleValue - scaling.get(AngleScaling.ADD)) / scaling.get(AngleScaling.MULT);
		}
		return angleValue;
	}

	public Map<ReferenceGeometry, Double> getReferenceGeometry(Map<String, Double> beamlinePosition) {

		if (!isBeamlinePositionValid(beamlinePosition)) {
			throw new IllegalArgumentException("Beamline position non-valid, check motor names and number of motors.");
		}

		return referenceAnglesToBeamlineMotors.entrySet()
				.stream()
				.collect(toMap(
						Entry::getKey,
						item -> calculateReferenceAngle(beamlinePosition, item.getKey(),item.getValue()),
						(x, y) -> x,
						() -> new EnumMap<>(ReferenceGeometry.class)));
	}

	public Map<String, Double> getBeamlineGeometry(Map<ReferenceGeometry, Double> referencePosition) {
		if (!isReferencePositionValid(referencePosition)) {
			throw new IllegalArgumentException("Reference position non-valid, check fixed angle values.");
		}

		return referenceAnglesToBeamlineMotors
				.entrySet()
				.stream()
				.collect(toMap(
						Entry::getValue,
						item -> calculateBeamlineAngle(referencePosition, item.getKey())));
	}
	public abstract Map<String, Double> getDefaultConstraints();
	public Map<ReferenceGeometry, String> getReferenceAnglesToBeamlineMotors() {
		return referenceAnglesToBeamlineMotors;
	}
	public void setReferenceAnglesToBeamlineMotors(Map<ReferenceGeometry, String> referenceAnglesToBeamlineMotors) {
		this.referenceAnglesToBeamlineMotors = referenceAnglesToBeamlineMotors;
	}
	public Map<ReferenceGeometry, Map<AngleScaling, Double>> getReferenceScaling() {
		return referenceScaling;
	}
	public void setReferenceScaling(Map<ReferenceGeometry, Map<AngleScaling, Double>> referenceScaling) {
		this.referenceScaling = referenceScaling;
	}
}
