/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import java.util.Optional;

/**
 * Interface used by some classes to holder the script status
 * Provided to ensure loose coupling between callers and implementation
 */
public interface IScriptController {
	/**
	 * @return status see values in Jython e.g. JYTHON.IDLE
	 */
	public JythonStatus getScriptStatus();

	/**
	 * Returns the name of the currently running script, if present
	 * @return name of current script
	 */
	public Optional<String> getScriptName();

	/**
	 *
	 * @param status see values in Jython e.g. JYTHON.IDLE
	 */
	public void setScriptStatus(JythonStatus status);

	/**
	 * @see Jython#pauseCurrentScript
	 */
	public void pauseCurrentScript();

	/**
	 * @see Jython#resumeCurrentScript
	 */
	public void resumeCurrentScript();
}
