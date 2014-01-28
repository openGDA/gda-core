/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.exafs.i20;

public class CryostatProperties {
	public static final String[] LOOP_OPTION = new String[] { "Loop over sample, then temperature", "Loop over temperature, then sample" };
	public static final String[] CONTROL_MODE = new String[] { "None", "Manual PID", "Zone control", "Open Loop", "Auto-tune PID" };
	public static final String[] HEATER_RANGE = new String[] { "4mW", "40mW", "400mW", "4W", "40W" };
}
