/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.pressurecell.controller;

/**
 * Enumuration of all the actions that can be requested of a pressure valve enum PV
 * <br>
 * Actions are used by both armable and non-armable valves, with the arm/disarm
 * variants being ignored if not valid.
 */
public enum ValveControl {
	OPEN, CLOSE, RESET, ARM, DISARM;
}
