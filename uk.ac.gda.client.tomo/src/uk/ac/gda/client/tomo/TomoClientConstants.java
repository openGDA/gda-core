/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo;

public interface TomoClientConstants {

	public static final String TOMO_MULTIPLE_VERTICAL_MOTORS_CONTEXT = "uk.ac.gda.tomography.multiple.vertical.motors.context";

	public static final String TOMO_CONFIG_EDITING_DOMAIN = "uk.ac.gda.tomography.config.editingDomain";

	public static final String MOVE_MODULE_COMMAND = "tomoAlignment.moveToModule(%d)";

	public static final String TOMOGRAPHY_IS_RUNNING_CONFIG_CMD = "tomoAlignment.tomographyConfigurationManager.getRunningConfig()";

	public static final String TOMOGRAPHY_STOP_SCAN = "tomoAlignment.tomographyConfigurationManager.stopScan()";

	public static final String CHANGE_SUBDIR = "tomoAlignment.changeSubDir('%s')";

	public static final String GET_SUBDIR = "tomoAlignment.getSubdir()";

	public static final String MOVE_VERTICAL = "tomoAlignment.moveVerticalBy(%s, %s, %f)";

	public static final String GET_VERTICAL = "tomoAlignment.getVerticalMotorPositions()";

	public static double MAX_INTENSITY = 65535;
}
