/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.exafs.scan;

import java.io.Serializable;

import uk.ac.gda.beans.exafs.IDetectorParameters;
import uk.ac.gda.beans.exafs.IScanParameters;

/**
 * Message bean to be sent to the LoggingScriptController which records the progress of XAS scans at the start of every
 * scan.
 * <p>
 * This information may then the sent to the parts of the UI which depend on information about the scan which is
 * currently running.
 */
public class ScanStartedMessage implements Serializable {

	private IScanParameters startedScan;
	private IDetectorParameters detectorParams;

	public ScanStartedMessage(IScanParameters startedScan, IDetectorParameters detectorParams) {
		super();
		this.startedScan = startedScan;
		this.detectorParams = detectorParams;
	}

	public IScanParameters getStartedScan() {
		return startedScan;
	}

	public IDetectorParameters getDetectorParams() {
		return detectorParams;
	}

	@Override
	public String toString() {
		return "Scan started using beans: " + startedScan.toString() + " " + detectorParams.toString();
	}
}
