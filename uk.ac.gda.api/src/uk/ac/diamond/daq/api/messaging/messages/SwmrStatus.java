/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.api.messaging.messages;

/**
 * Status of HDF5 Single Write Multiple Read mode of scan file
 */
public enum SwmrStatus {

	/**
	 * Not HDF5 file, or not opened with SWMR enabled
	 */
	DISABLED,

	/**
	 * File has SWMR enabled but it is not yet active
	 */
	ENABLED,

	/**
	 * File has SWMR enabled and it has be activated
	 */
	ACTIVE;
}
