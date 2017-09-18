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

package gda.commandqueue;

import java.io.File;
import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.jython.IJythonServerStatusObserver;
import gda.jython.IJythonServerStatusProvider;
import gda.jython.InterfaceProvider;
import gda.jython.Jython;
import gda.jython.JythonServerStatus;
import gda.observable.IObserver;

/**
 * JythonScriptFileRunnerCommand is an implementation of Command whose run method runs the script file set by the
 * setScriptFile method in the CommandRunner
 */
public class JythonScriptFileRunnerCommand extends CommandBase implements Serializable {

	private static Logger logger = LoggerFactory.getLogger(JythonScriptFileRunnerCommand.class);

	protected String scriptFile;
	protected String settingsFile; // file to be returned in get details if null return scriptFile
	protected boolean hasAlreadyBeenRun = false;

	boolean abortedRequested = false;
	Object abortRequestedLock = new Object();

	/**
	 * do not return until script and scan state are both IDLE
	 *
	 * @throws Exception
	 */
	@Override
	public void run() throws Exception {

		if (hasAlreadyBeenRun) {
			throw new Exception("Command has already been run");
		}

		hasAlreadyBeenRun = true;

		IObserver progressProviderObserver = null;
		IJythonServerStatusObserver jythonServerStatusObserver = null;
		IJythonServerStatusProvider jythonServerStatusProvider = null;
		JythonScriptProgressProvider jProgressProvider = null;

		try {

			// listen to messages from the script and pass to the observer of this command
			progressProviderObserver = new IObserver() {
				@Override
				public void update(Object source, Object arg) {
					obsComp.notifyIObservers(JythonScriptFileRunnerCommand.this, arg); // should be CommandProgress
				}
			};
			jProgressProvider = JythonScriptProgressProvider.getInstance();
			jProgressProvider.addIObserver(progressProviderObserver);

			/**
			 * Listen to the server status to know when it is paused/restarted and set the state which will notify the
			 * observers of this command
			 */
			jythonServerStatusObserver = new IJythonServerStatusObserver() {
				@Override
				public void update(Object source, Object arg) {
					if (arg instanceof JythonServerStatus) {

						JythonServerStatus jsState = (JythonServerStatus) arg;
						int scriptState = jsState.scriptStatus;
						int scanState = jsState.scanStatus;
						boolean scriptOrScanPaused = scriptState == Jython.PAUSED || scanState == Jython.PAUSED;

						if (getState().equals(Command.STATE.NOT_STARTED) && scriptState == Jython.RUNNING) {
							setState(Command.STATE.RUNNING);
						}

						if (getState().equals(Command.STATE.RUNNING) && scriptOrScanPaused) {
							setState(Command.STATE.PAUSED);
						}

						if (getState().equals(Command.STATE.PAUSED) && !scriptOrScanPaused) {
							setState(Command.STATE.RUNNING);
						}
					}

				}
			};
			jythonServerStatusProvider = InterfaceProvider.getJythonServerStatusProvider();
			jythonServerStatusProvider.addJythonServerStatusObserver(jythonServerStatusObserver);

			InterfaceProvider.getCurrentScanController().resumeCurrentScan();

			InterfaceProvider.getScriptController().resumeCurrentScript();

			InterfaceProvider.getCommandRunner().runScript(new File(scriptFile), "");

			// wait 1 second for the script to start
			Thread.sleep(1000);
			if (getState().equals(Command.STATE.NOT_STARTED)) {
				throw new Exception("Script failed to start after 1 second:" + scriptFile + ". See server log for possible reason");
			}

			// Wait until state of the server indicates that the script has completed
			JythonServerStatus jsState = jythonServerStatusProvider.getJythonServerStatus();
			while (!jsState.areScriptAndScanIdle()) {
				Thread.sleep(1000);
				jsState = jythonServerStatusProvider.getJythonServerStatus();
			}

			if (abortedRequested) {
				setState(Command.STATE.ABORTED);
			} else {
				setState(Command.STATE.COMPLETED);
			}
		}

		catch (Exception e) {
			if (jProgressProvider != null) {
				jProgressProvider.updateProgress(100, "Error :" + e.getMessage());
			}
			setState(Command.STATE.ERROR);
			throw e;
		}

		finally {
			// clearup

			if (jythonServerStatusProvider != null && jythonServerStatusObserver != null) {
				jythonServerStatusProvider.deleteJythonServerStatusObserver(jythonServerStatusObserver);
			}

			if (jProgressProvider != null && progressProviderObserver != null) {
				jProgressProvider.deleteIObserver(progressProviderObserver);
			}
		}
	}

	@Override
	public String toString() {
		return "JythonCommand [scriptFile=" + scriptFile + ", description=" + getDescription() + ":" + state + "]";
	}

	@Override
	public void abort() {
		synchronized (abortRequestedLock) {
			if (!abortedRequested) {
				logger.info("Aborting command :" + getDescription());
				InterfaceProvider.getCommandAborter().abortCommands();
				abortedRequested = true;
			} else {
				logger.info("Abort ignored in command :" + getDescription());
			}
		}
	}

	@Override
	public void pause() {
		InterfaceProvider.getCurrentScanController().pauseCurrentScan();
		InterfaceProvider.getScriptController().pauseCurrentScript();
		setState(Command.STATE.PAUSED);
	}

	@Override
	public void resume() throws Exception {
		if (InterfaceProvider.getScanStatusHolder().getScanStatus() == Jython.PAUSED) {
			InterfaceProvider.getCurrentScanController().resumeCurrentScan();
			setState(Command.STATE.RUNNING);
		}
		if (InterfaceProvider.getScriptController().getScriptStatus() == Jython.PAUSED) {
			InterfaceProvider.getScriptController().resumeCurrentScript();
			setState(Command.STATE.RUNNING);
		}
	}

	/**
	 * @return absolute path to script file
	 */
	public String getScriptFile() {
		return scriptFile;
	}

	/**
	 * @param scriptFile absolute path to script file
	 */
	public void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
		setDescription(scriptFile);
	}

	@Override
	public CommandSummary getCommandSummary() {
		return new SimpleCommandSummary(getDescription());
	}

	@Override
	public CommandDetails getDetails() throws Exception {
		String settings = StringUtils.hasLength(settingsFile) ? settingsFile : scriptFile;
		return new SimpleCommandDetailsPath(getDescription(), settings);
	}

	@Override
	public void setDetails(String details) throws Exception {
		throw new UnsupportedOperationException();
	}

	public void setSettingsPath(String settingsPath) {
		this.settingsFile = settingsPath;
	}
}
