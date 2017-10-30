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
	CLEAR,    // All command threads cleared
	REFRESH,  // Notify observers to call for refresh

	SUBMITTED,    // Command was successfully submitted, and will run later
	BUSY,         // Command could not be submitted, because the server was busy
	SUBMIT_ERROR, // Command could not be submitted, due to an error

	START,    // start a new command thread (supplied)
	UPDATE,   // update an existing command thread (supplied)
	TERMINATE // terminate a command thread (supplied)
}
