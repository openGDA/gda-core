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

import java.text.MessageFormat;

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
	static public volatile boolean paused = false;

	/**
	 * allows scripts to be stopped at a convenient point
	 */
	static public volatile boolean interrupted = false;

	/**
	 * @param paused - allows UI/users to pause/resume scripts
	 */
	public static void setPaused(boolean paused){
		logger.info("paused flag set from " + ScriptBase.paused + " to " + paused + " by thread :'" + Thread.currentThread().getName() + "'");
		ScriptBase.paused = paused;
	}
	/**
	 * @param interrupted - allows scripts to be stopped at a convenient point
	 */
	public static void setInterrupted(boolean interrupted){
		String msg = MessageFormat.format("interrupted flag set from {0} to {1} by thread :''{2}''", 
				ScriptBase.interrupted, interrupted,Thread.currentThread().getName());
		logger.info(msg);
		logger.debug(msg + " from:\n" + generateStackTrace());
		ScriptBase.interrupted = interrupted;
	}
	
	private static String generateStackTrace() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		String trace = "";
		for (int i = 2; i < stackTrace.length; i++) {
			trace =trace + "    " + stackTrace[i].toString() + "\n";
		}
		return "    " + trace.trim();
	}
	/**
	 * @return allows scripts to be stopped at a convenient point
	 */
	public static boolean isInterrupted(){
		return interrupted;
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
		try {
			if (paused) {
				JythonServerFacade.getInstance().setScriptStatus(Jython.PAUSED);
				while (paused) {
					Thread.sleep(250);
					if (interrupted) {
						interrupted = false;
						logger.info("Interrupted flag was set true while paused in checkForPauses; clearing flag and throwing an InterruptedException into thread: {}", Thread.currentThread().getName());
						JythonServerFacade.getInstance().setScriptStatus(Jython.IDLE);
						throw new InterruptedException("Interupted flag was found to be true while paused.");
					}
				}
				JythonServerFacade.getInstance().setScriptStatus(Jython.RUNNING);
			}
			if (interrupted) {
				interrupted = false;
				JythonServerFacade.getInstance().setScriptStatus(Jython.IDLE);
				logger.info("Interrupted flag was set while checkingForPauses; clearing flag and throwing an InterruptedException into thread: {}", Thread.currentThread().getName());
				throw new InterruptedException("ScriptBase.interrupted flag was found to be true");
			}
		}
		// Rethrow the exception if script stop is attempted.
		// This would then be caught in the main part of the script,
		// stopping it.
		catch (InterruptedException ex) {
			throw ex;
		}
	}

}
