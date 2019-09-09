/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.ui;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uk.ac.gda.tomography.base.TomographyMode;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;
import uk.ac.gda.tomography.model.DevicePosition;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController.Positions;

public class StageConfiguration {
	private final TomographyParameterAcquisition acquisition;
	private final TomographyMode mode;
	private final Map<Positions, Set<DevicePosition<Double>>> motorsPosition;

	public StageConfiguration(TomographyParameterAcquisition acquisition, TomographyMode mode, Map<Positions, Set<DevicePosition<Double>>> motorsPosition) {
		super();
		this.acquisition = acquisition;
		this.mode = mode;
		this.motorsPosition = motorsPosition;
	}

	public TomographyParameterAcquisition getAcquisition() {
		return acquisition;
	}

	public TomographyMode getMode() {
		return mode;
	}

	public Map<Positions, Set<DevicePosition<Double>>> getMotorPositions() {
		return Collections.unmodifiableMap(motorsPosition);
	}
}
