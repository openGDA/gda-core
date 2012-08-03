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

	public static final String TOMO_CONFIG_EDITING_DOMAIN = "uk.ac.gda.tomography.config.editingDomain";

	public static final String MOVE_MODULE_COMMAND = "tomographyScani13.moveToModule(%d)";

	public static final String MOVE_TOMO_ALIGNMENT_MOTORS_COMMAND = "tomographyScani13.moveTomoAlignmentMotors(%s)";

	public static final String TOMOGRAPHY_SCAN_COMMAND = "tomographyScani13.tomoScani12(%1$.3g, %2$.3g, %3$d, %4$d, %5$s, %6$d, %7$.3g, %8$.3g, %9$.3g,%10$s)";
	
	public static final String TOMOGRAPHY_IS_RUNNING_CONFIG_CMD = "tomographyScani13.tomographyConfigurationManager.getRunningConfig()";
}
