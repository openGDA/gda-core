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

package uk.ac.diamond.daq.mapping.ui.stage;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.gda.client.properties.stage.position.Position;

/**
 * Represents both the acquisition and the hutch configuration
 *
 * @author Maurizio Nagni
 */
public class StageConfiguration {
	private ScanningAcquisition acquisition;
	private StageDescription stageDescription;
	private Map<Position, Set<DevicePosition<Double>>> motorsPositions;

	public StageConfiguration(ScanningAcquisition acquisition, StageDescription mode, Map<Position, Set<DevicePosition<Double>>> motorsPositions) {
		super();
		this.acquisition = acquisition;
		this.stageDescription = mode;
		this.motorsPositions = motorsPositions;
	}

	public StageConfiguration(StageDescription mode, Map<Position, Set<DevicePosition<Double>>> motorsPositions) {
		super();
		this.stageDescription = mode;
		this.motorsPositions = motorsPositions;
	}

	public StageConfiguration() {
		super();
	}

	public ScanningAcquisition getAcquisition() {
		return acquisition;
	}

	public StageDescription getStageDescription() {
		return stageDescription;
	}

	public Map<Position, Set<DevicePosition<Double>>> getMotorsPositions() {
		return Collections.unmodifiableMap(motorsPositions);
	}
}
