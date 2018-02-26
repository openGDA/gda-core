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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supplies a namespace for volatile variables which scripts may want to refer to while running.
 */
public abstract class ScriptBase {

	private static final Logger logger = LoggerFactory.getLogger(ScriptBase.class);

	/**
	 * allows UI/users to pause/resume scripts
	 */
	private static volatile boolean paused = false;

	/**
	 * @param paused - allows UI/users to pause/resume scripts
	 */
	public static void setPaused(boolean paused){
		logger.info("paused flag set from {} to {} by thread: '{}'", ScriptBase.paused, paused, Thread.currentThread().getName());
		ScriptBase.paused = paused;
	}

	/**
	 * @return - allows UI/users to pause/resume scripts
	 */
	public static boolean isPaused() {
		return paused;
	}

	/**
	 * This should be called in frequent places during long scripts where the script's author wishes to allow the
	 * ability to pause\resume the script
	 *
	 * @throws InterruptedException
	 */
	public static void checkForPauses() throws InterruptedException {
		// TODO: GDA-5776 Script status is only changed if this method is called.
		checkForInterruption();
		if (paused ) {
			JythonServerFacade.getInstance().setScriptStatus(JythonStatus.PAUSED);
			while (paused) {
				Thread.sleep(250);
				checkForInterruption();
			}
			JythonServerFacade.getInstance().setScriptStatus(JythonStatus.RUNNING);
		}

		checkForInterruption();
	}

	public static void checkForInterruption() throws InterruptedException {
		if (Thread.interrupted()) { // clears as read
			logger.info("Raising InterruptedException as thread was interrupted:", Thread.currentThread()
					.getName());
			logger.warn("GDA-5776 - *not* calling setScriptStatus(JythonStatus.IDLE");
			throw new InterruptedException();
		}
	}
}
