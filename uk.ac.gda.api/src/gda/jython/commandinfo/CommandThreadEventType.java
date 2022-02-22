/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.jython.commandinfo;

public enum CommandThreadEventType {
	/** All command threads cleared */
	CLEAR,
	/** Notify observers to call for refresh */
	REFRESH,

	/** Command was successfully submitted, and will run later */
	SUBMITTED,
	/** Command could not be submitted, because the server was busy */
	BUSY,
	/** Command could not be submitted, due to an error */
	SUBMIT_ERROR,

	/** start a new command thread (supplied) */
	START,
	/** update an existing command thread (supplied) */
	UPDATE,
	/** terminate a command thread (supplied) */
	TERMINATE
}
