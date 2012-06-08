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

/**
 * Definition of static Cobold Commands available for scripting interface
 */
public class CoboldCommands {
	/** get Cobold status */
	// can't get this to work, using DaqState.bat instead
	// public final static String GET_STATUS = "-DaqState";
	/** get Cobold mode */
	public final static String GET_MODE = "-DaqMode";
	/** get if Cobold command is running */
	public final static String GET_COMMAND_RUNNING = "-CommandRunning";
	/** execute a Cobold Command File */
	public final static String EXECUTE = "execute";
	/** stop data collection */
	public final static String STOP = "stop";
	/** update displayed graphics */
	public static final String UPDATE = "update";
	/** save currently displayed data to temporary file */
	public static final String SAVE_ASCII = "exportAscii";
	/** save raw data to temporary file */
	public static final String SAVE_RAW = "exportAscii";
	/** view spectrum */
	public static final CharSequence VIEW = "view";
	/** save as */
	public static final String SAVE_AS = "Save As";
	/** save */
	public static final String SAVE = "Save";
	/** show spectra */
	public static final String SHOW_SPECTRA = "show spectra";
	/** restart */
	public static final String RESTART = "restart";
	/** show parameters */
	public static final String SHOW_PARAMS = "show parameters";
	/** execute command file */
	public static final String EXECUTE_CCF = "execute";
}
