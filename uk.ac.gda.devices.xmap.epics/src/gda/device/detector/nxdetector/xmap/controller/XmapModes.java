/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.xmap.controller;

public class XmapModes {

	public enum CollectionModeEnum {
		/* MCA spectra used in step scan: acquire only one spectrum */
		MCA_SPECTRA,
		/* MCA mapping used in raster/continuous scan: acquire multiple spectra */
		MCA_MAPPING, SCA_MAPPING, LIST_MAPPING
	}

	public enum PresetMode {
		/* Option used for hardware trigger */
		NO_PRESET, REAL_TIME, LIVE_TIME, EVENTS, TRIGGERS
	}

	public enum ListMode {
		EAndGate, EAndSync, EAndClock
	}

	public enum PixelAdvanceMode {
		Gate, Sync
	}

}
