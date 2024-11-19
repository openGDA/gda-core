/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.gda.core.sampletransfer;

/**
 * Represents the various commands that a client
 * can send to a server to control a sequence.
 */
public enum SequenceCommand {
	/**
	 * Initiates the sequence.
	 */
	START,
	/**
	 * The sequence was paused and needs to continue.
	 */
	RESUME,
	/**
	 * Request current step that failed.
	 */
	RETRY,
	/**
	 * Request to terminate the current running sequence.
	 */
	STOP
}
