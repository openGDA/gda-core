/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view;

/**
 * <pre>
 * Set of commands in the tomoAlignment.py module of the i12 beamline configuration.
 * <code>gda-dls-beamlines-i12.git/i12/scripts/tomo/tomoAlignment.py</code> 
 * This relies on the fact that the tomoalignment module is imported in the localstation.py 
 * module and is accessible.
 * </pre>
 */
public interface TomoAlignmentCommands {

	public static final String MOVE_MODULE_COMMAND = "tomoAlignment.moveToModule(%d)";

	public static final String GET_MODULE_COMMAND = "tomoAlignment.getModule()";

	public static final String TOMOGRAPHY_IS_RUNNING_CONFIG_CMD = "tomoAlignment.tomographyConfigurationManager.getRunningConfig()";

	public static final String TOMOGRAPHY_STOP_SCAN = "tomoAlignment.tomographyConfigurationManager.stopScan()";

	public static final String CHANGE_SUBDIR = "tomoAlignment.changeSubDir('%s')";

	public static final String GET_SUBDIR = "tomoAlignment.getSubdir()";

	public static final String MOVE_VERTICAL = "tomoAlignment.moveVerticalBy(%s, %s, %f)";

	public static final String GET_VERTICAL = "tomoAlignment.getVerticalMotorPositions()";

	public static final String AUTO_FOCUS = "tomoAlignment.autoFocus(%f)";

	public static final String MOVE_T3_M1_Z_COMMAND = "tomoAlignment.moveT3M1ZTo(%d, %f)";

}
