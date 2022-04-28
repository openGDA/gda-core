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

package uk.ac.gda.client.livecontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;

/**
 * Subclass of {@link CommandControl} to run a single command in the Jython interpreter
 * <br><br>
 * Example bean definition:
 * <pre>
 * {@code
 * <bean id="removeLens" class="uk.ac.gda.client.livecontrol.JythonCommandControl">
 *   <property name="jythonCommand" value="sequences.remove_lens()" />
 *   <property name="buttonText" value="Remove Lens" />
 * </bean>
 * }
 * </pre>
 */
public class JythonCommandControl extends CommandControl {
	private static final Logger logger = LoggerFactory.getLogger(JythonCommandControl.class);

	@Override
	protected void runCommand(String command) {
		logger.debug("Running Jython command: {}", command);
		InterfaceProvider.getCommandRunner().runCommand(command);
	}

	/**
	 * For backwards compatibility only, does the same as {@link #getCommand()}
	 *
	 * @return jythonCommand String
	 * @deprecated
	 */
	@Deprecated(since = "gda-9.26", forRemoval = true)
	public String getJythonCommand() {
		logger.warn("getJythonCommand() will be removed in the GDA 9.28 release, switch to using getCommand() instead");
		return getCommand();
	}

	/**
	 * For backwards compatibility only, does the same as {@link #setCommand(String)}
	 *
	 * @param jythonCommand
	 * @deprecated
	 */
	@Deprecated(since = "gda-9.26", forRemoval = true)
	public void setJythonCommand(String jythonCommand) {
		logger.warn("setJythonCommand() will be removed in the GDA 9.28 release, switch to using setCommand() instead");
		setCommand(jythonCommand);
	}
}
