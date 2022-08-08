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

package gda.rcp.ncd.calibration;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import uk.ac.gda.server.ncd.calibration.CalibrationEdge;

public class CalibrationSet {
	private CalibrationEdge edge;
	private Path scanFile;
	private List<ObservedFeature> features = emptyList();

	public CalibrationSet(CalibrationEdge edge) {
		this.edge = edge;
	}

	public Optional<Path> getDataFile() {
		return ofNullable(scanFile);
	}

	public List<ObservedFeature> getFeatures() {
		return features;
	}

	public String getName() {
		return edge.getName();
	}

	public void setDataFile(String scanPath) {
		scanFile = Paths.get(scanPath);
		features = edge.getFeatures().stream()
				.map(ObservedFeature::new)
				.collect(toList());
	}

	public CalibrationEdge getEdge() {
		return edge;
	}
}
