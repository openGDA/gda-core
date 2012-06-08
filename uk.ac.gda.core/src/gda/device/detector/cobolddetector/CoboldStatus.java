/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.cobolddetector;

import java.util.ArrayList;

/**
 * CoboldStatus Class
 */
public class CoboldStatus {
	/** DAQ State: Unknown */
	public static String unknown = "DAQ State: Unknown";
	/** DAQ State: Stopped */
	public static String ready = "DAQ State: Stopped";
	/** DAQ State: CollectingData */
	public static String collectingData = "DAQ State: CollectingData";
	/** DAQ State: CopyingGraphics */
	public static String copyingGraphics = "DAQ State: CopyingGraphics";

	private static boolean initialized = false;

	private static ArrayList<String> messages = new ArrayList<String>();

	/**
	 * Initialize the Cobold messages array
	 */
	public static void initialize() {
		CoboldStatus.messages.add(unknown);
		CoboldStatus.messages.add(ready);
		CoboldStatus.messages.add(collectingData);

		initialized = true;
	}

	/**
	 * Get the integer value of the Cobold status message
	 * 
	 * @param s
	 *            the status string
	 * @return value int value of the status
	 */
	public static int getValue(String s) {
		if (!initialized)
			initialize();

		int value = 0;
		if (messages.contains(s))
			value = messages.indexOf(s);

		return value;
	}
}
