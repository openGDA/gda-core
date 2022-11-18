/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.gda.beans.validation;

/**
 * Severity of a warning.
 * HIGH: This scan should not be run
 * MEDIUM: This scan can be run at the user's discretion
 * LOW: This scan is safe to run, but there is something the user should be made aware of, e.g. motors will move
 */
public enum WarningType {
	LOW,
	MEDIUM,
	HIGH
}
