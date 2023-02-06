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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import uk.ac.gda.api.acquisition.TrajectoryShape;

/**
 * Describes an N-dimensional motion in a scan
 */
public class Trajectory {

	/** The axes involved in the trajectory */
	private final List<ScannableTrackDocument> axes;

	private final TrajectoryShape shape;

	public Trajectory(ScannableTrackDocument axis) {
		this(List.of(axis), deduceSingleAxisShape(axis));
	}

	@JsonCreator
	public Trajectory(@JsonProperty("axes") List<ScannableTrackDocument> axes, @JsonProperty("shape") TrajectoryShape shape) {
		this.axes = axes;
		this.shape = shape;
	}

	private static TrajectoryShape deduceSingleAxisShape(ScannableTrackDocument axis) {
		if (axis.getScannable() == null) return TrajectoryShape.STATIC_POINT;
		return TrajectoryShape.ONE_DIMENSION_LINE;
	}

	public List<ScannableTrackDocument> getAxes() {
		return axes;
	}

	public TrajectoryShape getShape() {
		return shape;
	}

}
