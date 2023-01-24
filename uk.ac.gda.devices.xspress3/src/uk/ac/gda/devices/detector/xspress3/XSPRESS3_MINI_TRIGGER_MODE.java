/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

public enum XSPRESS3_MINI_TRIGGER_MODE {

	Software,
	Hardware,
	Burst,
	TTL_Veto_Only,
	IDC,
	Software_Start_Stop {
		@Override
		public String toString() {
			return "Software_Start/Stop";
		}
	},
	TTL_Both,
	LVDS_Veto_Only,
	LVDS_Both
}
