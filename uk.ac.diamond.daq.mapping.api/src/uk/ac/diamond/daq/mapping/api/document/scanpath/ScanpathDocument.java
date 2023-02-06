/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes a generic acquisition model. Classes extending this realise specific acquisition configuration.
 */
public class ScanpathDocument {

	/**
	 * An ordered list of trajectories in the scan. Inner-most trajectory first.
	 */
	private final LinkedList<Trajectory> trajectories;

	@JsonCreator
	public ScanpathDocument(@JsonProperty("trajectories") List<Trajectory> trajectories) {
		this.trajectories = new LinkedList<>(trajectories);
	}

	public List<Trajectory> getTrajectories() {
		return trajectories;
	}

}
