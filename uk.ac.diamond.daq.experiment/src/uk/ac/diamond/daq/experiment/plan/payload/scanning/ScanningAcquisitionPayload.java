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

package uk.ac.diamond.daq.experiment.plan.payload.scanning;

import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;

public class ScanningAcquisitionPayload implements Payload {

	private final ScanningAcquisition scan;
	private QueueResolution queueResolution;


	public ScanningAcquisitionPayload(ScanningAcquisition scan, QueueResolution queueResolution) {
		this.scan = scan;
		this.queueResolution = queueResolution;
	}


	public ScanningAcquisition getScan() {
		return scan;
	}


	public QueueResolution getQueueResolution() {
		return queueResolution;
	}


	public void setQueueResolution(QueueResolution queueResolution) {
		this.queueResolution = queueResolution;
	}

}
