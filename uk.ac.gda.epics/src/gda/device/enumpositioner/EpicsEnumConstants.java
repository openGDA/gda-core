/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.enumpositioner;

public class EpicsEnumConstants {

	/**
	 * PV suffixes corresponding to the possible positions. These are appended to base PVs to create PVs like:<br>
	 * BL13I-EA-TURR-01:DEMAND.ZRST<br>
	 * BL13I-EA-TURR-01:CURRENTPOS.ZRST
	 */
	public static final String[] CHANNEL_NAMES = { "ZRST", "ONST", "TWST", "THST", "FRST", "FVST", "SXST", "SVST", "EIST", "NIST", "TEST", "ELST", "TVST",
			"TTST", "FTST", "FFST" };
}
