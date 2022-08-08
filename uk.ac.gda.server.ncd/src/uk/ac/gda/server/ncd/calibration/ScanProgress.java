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

package uk.ac.gda.server.ncd.calibration;

import java.io.Serializable;
import java.util.UUID;

public class ScanProgress implements Serializable {
	private final UUID edgeId;
	private final CalibrationUpdate.Update type;
	private final String scanFile;

	public ScanProgress(CalibrationUpdate.Update type, UUID scanId, String scanFile) {
		this.type = type;
		this.edgeId = scanId;
		this.scanFile = scanFile;
	}

	public String getScanFile() {
		return scanFile;
	}

	public CalibrationUpdate.Update getType() {
		return type;
	}

	public UUID getEdgeId() {
		return edgeId;
	}
}
