/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * Class which holds a snapshot of the state of the server. Used by classes to inform each other of the server state
 * changing.
 */
public class JythonServerStatus implements Serializable {

	/**
	 * Status of the currently running script
	 */
	public final JythonStatus scriptStatus;

	/**
	 * Status of the currently running scan (only one can run at once).
	 */
	public final JythonStatus scanStatus;

	/** Name of the currently running script - null if no script is running */
	public final String scriptName;

	public JythonServerStatus(JythonStatus scriptStatus, JythonStatus scanStatus) {
		this(scriptStatus, scanStatus, null);
	}

	public JythonServerStatus(JythonStatus scriptStatus, JythonStatus scanStatus, String scriptName) {
		if (scriptStatus == JythonStatus.IDLE && scriptName != null) {
			throw new IllegalArgumentException("Script name must be null if no script is running");
		}
		this.scriptStatus = scriptStatus;
		this.scanStatus = scanStatus;
		this.scriptName = scriptName;
	}

	public boolean isScriptOrScanPaused(){
		return scriptStatus==JythonStatus.PAUSED || scanStatus==JythonStatus.PAUSED;
	}

	public boolean areScriptAndScanIdle(){
		return scriptStatus==JythonStatus.IDLE  && scanStatus==JythonStatus.IDLE;
	}

	public Optional<String> getScriptName() {
		return Optional.ofNullable(scriptName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + scanStatus.ordinal();
		result = prime * result + scriptStatus.ordinal();
		result = prime * result + scriptName.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JythonServerStatus other = (JythonServerStatus) obj;
		if (scanStatus != other.scanStatus)
			return false;
		if (scriptStatus != other.scriptStatus)
			return false;
		return Objects.equals(scriptName, other.scriptName);
	}

	@Override
	public String toString() {
		return String.format("Command Server status: script=%s, scan=%s", scriptStatus, scanStatus);
	}
}
