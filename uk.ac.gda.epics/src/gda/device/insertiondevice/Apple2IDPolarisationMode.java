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

package gda.device.insertiondevice;

/**
 * Define the polarisation modes as specified by Scientists.
 *
 * @see "http://confluence.diamond.ac.uk/display/I21/Insertion+device"
 */
public enum Apple2IDPolarisationMode {
	LH, // Linear Horizontal Mode
	LV, // Linear Vertical Mode
	CR, // Circular Right Mode
	CL, // Circular Left Mode
	LAP, // Linear Angular Positive
	LAN, // Linear Angular Negative
	UNKNOWN
}