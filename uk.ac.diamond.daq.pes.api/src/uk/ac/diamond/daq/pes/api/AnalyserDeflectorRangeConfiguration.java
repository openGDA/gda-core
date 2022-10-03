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

package uk.ac.diamond.daq.pes.api;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

public class AnalyserDeflectorRangeConfiguration implements Serializable {

	private Map<String, DeflectorRange> deflectorRanges;

	public AnalyserDeflectorRangeConfiguration(Map<String, DeflectorRange> deflectorRanges) {
		this.deflectorRanges = deflectorRanges;
	}

	public DeflectorRange getDeflectorRangeForLensMode(String lensMode) {
		if (deflectorRanges.containsKey(lensMode)) {
			return deflectorRanges.get(lensMode);
		} else {
			throw new IllegalArgumentException("No deflector range has been configured for lens mode" + lensMode);
		}
	}

	public double getDeflectorXMaximumForLensMode(String lensMode) {
		return getDeflectorRangeForLensMode(lensMode).getDeflectorXMaximum();
	}

	public double getDeflectorXMinimumForLensMode(String lensMode) {
		return getDeflectorRangeForLensMode(lensMode).getDeflectorXMinimum();
	}

	public Set<String> getAllLensModes() {
		return deflectorRanges.keySet();
	}
}
