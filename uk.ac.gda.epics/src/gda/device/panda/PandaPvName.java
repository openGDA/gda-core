/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

/** Set of logical name and and PV names for controlling Panda */
public enum PandaPvName {
	PCAP_ARM("PCAP:ARM"),
	PCAP_ACTIVE("PCAP:ACTIVE"),
	SEQ_BITA("SEQ1:BITA"),
	SEQ_ENABLE("SEQ1:ENABLE"),
	SEQ_TABLE("SEQ1:TABLE"),
	SEQ_LINE_REPEAT("SEQ1:LINE_REPEAT"),
	SEQ_TABLE_LINE("SEQ1:TABLE_LINE"),
	SEQ_STATE("SEQ1:STATE"),
	SEQ_ACTIVE("SEQ1:ACTIVE"),
	SEQ_PRESCALE_UNITS("SEQ1:PRESCALE:UNITS"),
	SEQ_PRESCALE("SEQ1:PRESCALE"),
	SEQ_REPEATS("SEQ1:REPEATS"),

	PULSE_ENABLE("PULSE1:ENABLE"),
	PULSE_TRIG("PULSE1:TRIG"),

	HDF_DIRECTORY("DATA:HDF_DIRECTORY"),
	HDF_FILE_NAME("DATA:HDF_FILE_NAME"),
	HDF_FULL_FILE_PATH("DATA:HDF_FULL_FILE_PATH"),
	HDF_FLUSH_PERIOD("DATA:FLUSH_PERIOD"),
	HDF_CAPTURE("DATA:CAPTURE"),
	HDF_NUM_CAPTURED("DATA:NUM_CAPTURED");

	private final String pvName;
	private PandaPvName(String pvName) {
		this.pvName = pvName;
	}
	public String getPvName() {
		return pvName;
	}
}
