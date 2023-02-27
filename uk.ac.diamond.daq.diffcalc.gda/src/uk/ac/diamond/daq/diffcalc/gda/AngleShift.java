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

import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AngleShift {

	private Map<String, Double> cutBeamlineAngles = emptyMap();

	public AngleShift(Map<String, Double> cutBeamlineAngles) {
		this.cutBeamlineAngles = cutBeamlineAngles;
	}

	public AngleShift() {}

	public Map<String, Double> cutAngles(Map<String, Double> diffractometerPosition) {
		Map<String, Double> cutPosition = new HashMap<>();

		for (Entry<String, Double> item: diffractometerPosition.entrySet()) {
			Double correctedAngle = cutAngle(item.getKey(), item.getValue());

			cutPosition.put(item.getKey(), correctedAngle);
		}
		return cutPosition;
	}

	public Double cutAngle(String angle, Double value) {
		Double boundary = cutBeamlineAngles.get(angle);
		if (boundary == null) {
			boundary = -180d;
		}

		if (value < boundary) {
			value += 360;
			cutAngle(angle, value);
		} else if (value >= boundary + 360) {
			value -= 360;
			cutAngle(angle, value);
		}

		return value;
	}

	public Map<String, Double> getCutBeamlineAngles() {
		return cutBeamlineAngles;
	}

	public void setCutBeamlineAngles(Map<String, Double> cutBeamlineAngles) {
		this.cutBeamlineAngles = cutBeamlineAngles;
	}
}
