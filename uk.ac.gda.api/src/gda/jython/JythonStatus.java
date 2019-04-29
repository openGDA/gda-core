/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.jython;

/**
 * JythonStatus represents the three possible states of a Jython script, scan or queue.
 */
public enum JythonStatus {

	/**
	 * Script, scan or queue not in use
	 */
	IDLE,

	/**
	 * Script, scan or queue in progress but paused
	 */
	PAUSED,

	/**
	 * Script, scan or queue in progress and not paused
	 */
	RUNNING;

}