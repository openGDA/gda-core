/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import static uk.ac.diamond.daq.api.messaging.messages.DestinationConstants.GDA_MESSAGES_SCAN_METADATA_TOPIC;

import java.util.Map;

import uk.ac.diamond.daq.api.messaging.Destination;
import uk.ac.diamond.daq.api.messaging.Message;

@Destination(GDA_MESSAGES_SCAN_METADATA_TOPIC)
public class ScanMetadataMessage implements Message {

	private final ScanStatus scanStatus;
	private final String filePath;
	private final Map<String, Object> scanMetadata;

	public ScanMetadataMessage(ScanStatus scanStatus,
			String filePath,
			Map<String, Object> scanMetadata) {
		this.scanStatus = scanStatus;
		this.filePath = filePath;
		this.scanMetadata = scanMetadata;
	}

	public ScanStatus getScanStatus() {
		return scanStatus;
	}

	public String getFilePath() {
		return filePath;
	}

	public Map<String, Object> getScanMetadata() {
		return scanMetadata;
	}

	@Override
	public String toString() {
		return "ScanMetadataMessage [scanStatus=" + scanStatus + ", filePath=" + filePath
				+ ", scanMetadata=" + scanMetadata + "]";
	}

}
