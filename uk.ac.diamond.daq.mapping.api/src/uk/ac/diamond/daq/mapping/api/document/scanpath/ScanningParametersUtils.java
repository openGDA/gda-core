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

import java.util.ArrayList;
import java.util.List;

import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument.Axis;
import uk.ac.gda.api.acquisition.TrajectoryShape;

public class ScanningParametersUtils {

	private ScanningParametersUtils() {
		// static utils
	}

	public static ScannableTrackDocument getAxis(ScanpathDocument scan, Axis axis) {
		return scan.getTrajectories().stream()
				.map(Trajectory::getAxes).flatMap(List::stream)
				.filter(doc -> doc.getAxis().equals(axis))
				.findFirst().orElseThrow(() -> new IllegalArgumentException("Scan does not define axis " + axis.toString()));
	}

	/**
	 * Replaces in place the given {@code updatedAxes} in the given {@code document}
	 */
	public static void updateAxes(ScanpathDocument scan, List<ScannableTrackDocument> updatedAxes) {
		updatedAxes.forEach(axis -> updateAxis(scan, axis));
	}

	public static void updateAxis(ScanpathDocument scan, ScannableTrackDocument axis) {
		// 1: Find trajectory containing given axis
		var trajectory = scan.getTrajectories().stream()
			.filter(traj -> traj.getAxes().stream().anyMatch(doc -> doc.getAxis().equals(axis.getAxis())))
			.findFirst().orElseThrow();

		// 2: Replace said trajectory with updated one
		var updatedTrajectoryAxis = new ArrayList<ScannableTrackDocument>();
		for (var trajectoryAxis : trajectory.getAxes()) {
			if (trajectoryAxis.getAxis().equals(axis.getAxis())) {
				updatedTrajectoryAxis.add(axis);
			} else {
				updatedTrajectoryAxis.add(trajectoryAxis);
			}
		}
		var updatedTrajectory = new Trajectory(updatedTrajectoryAxis, trajectory.getShape());

		var trajectoryIndex = scan.getTrajectories().indexOf(trajectory);
		scan.getTrajectories().remove(trajectory);
		scan.getTrajectories().add(trajectoryIndex, updatedTrajectory);
	}

	public static void updateTrajectoryShape(ScanpathDocument scan, Trajectory trajectory, TrajectoryShape shape) {
		var updated = new Trajectory(trajectory.getAxes(), shape);
		var index = scan.getTrajectories().indexOf(trajectory);
		scan.getTrajectories().remove(trajectory);
		scan.getTrajectories().add(index, updated);
	}

	public static List<ScannableTrackDocument> getAxesDocuments(ScanpathDocument scan) {
		return scan.getTrajectories().stream().map(Trajectory::getAxes).flatMap(List::stream).toList();
	}

}
