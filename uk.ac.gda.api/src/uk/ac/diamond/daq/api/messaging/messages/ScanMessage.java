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

package uk.ac.diamond.daq.api.messaging.messages;

import static uk.ac.diamond.daq.api.messaging.messages.DestinationConstants.GDA_MESSAGES_SCAN_TOPIC;

import java.util.Arrays;
import java.util.List;

import uk.ac.diamond.daq.api.messaging.Destination;
import uk.ac.diamond.daq.api.messaging.Message;

/**
 * Message sent to update listeners of scans starting, progressing and finishing.
 * <p>
 * See https://confluence.diamond.ac.uk/x/4FdRBQ for some design discussion.
 *
 * @author James Mudd
 * @since GDA 9.12
 */
@Destination(GDA_MESSAGES_SCAN_TOPIC)
public class ScanMessage implements Message {

	public enum ScanStatus {
		STARTED,
		UPDATED,
		ENDED;
	}

	private final ScanStatus status;
	private final String filePath;
	private final boolean swmrActive;
	private final int scanNumber;
	private final int[] scanDimensions;
	private final List<String> scannables;
	private final List<String> detectors;
	private final double percentageComplete;

	public ScanMessage(ScanStatus status,
			String filePath,
			boolean swmrActive,
			int scanNumber,
			int[] scanDimensions,
			List<String> scannables,
			List<String> detectors,
			double percentageComplete) {

		this.status = status;
		this.filePath = filePath;
		this.swmrActive = swmrActive;
		this.scanNumber = scanNumber;
		this.scanDimensions = scanDimensions;
		this.scannables = scannables;
		this.detectors = detectors;
		this.percentageComplete = percentageComplete;
	}

	public ScanStatus getStatus() {
		return status;
	}

	public String getFilePath() {
		return filePath;
	}

	public boolean isSwmrActive() {
		return swmrActive;
	}

	public int getScanNumber() {
		return scanNumber;
	}

	public int[] getScanDimensions() {
		return scanDimensions;
	}

	public List<String> getScannables() {
		return scannables;
	}

	public List<String> getDetectors() {
		return detectors;
	}

	public double getPercentageComplete() {
		return percentageComplete;
	}

	@Override
	public String toString() {
		return "ScanMessage [status=" + status + ", filePath=" + filePath + ", swmrActive=" + swmrActive
				+ ", scanNumber=" + scanNumber + ", scanDimensions=" + Arrays.toString(scanDimensions) + ", scannables="
				+ scannables + ", detectors=" + detectors + ", percentageComplete=" + percentageComplete + "]";
	}

}
