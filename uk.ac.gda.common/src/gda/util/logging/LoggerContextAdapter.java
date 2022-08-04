/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package gda.util.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;

/**
 * Provides default implementations for the methods described by the {@link LoggerContextListener} interface.
 * <p>
 * Classes that wish to deal with logger context events can extend this class and override only the methods that they are interested in.
 */
public class LoggerContextAdapter implements LoggerContextListener {

	@Override
	public boolean isResetResistant() {
		return false;
	}

	@Override
	public void onStart(LoggerContext context) {
		// do nothing
	}

	@Override
	public void onReset(LoggerContext context) {
		// do nothing
	}

	@Override
	public void onStop(LoggerContext context) {
		// do nothing
	}

	@Override
	public void onLevelChange(Logger logger, Level level) {
		// do nothing
	}

}
