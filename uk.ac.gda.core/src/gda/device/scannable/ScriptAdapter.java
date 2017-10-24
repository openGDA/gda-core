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

package gda.device.scannable;

import gda.device.Scannable;
import gda.jython.JythonServerFacade;

/**
 * Encapsulates a Jython script in the Scannable interface. This allows a script to be called at each node of a scan as
 * the script is run by the move methods. The script runs in its own thread, outside of the normal thread used for
 * scripts, so it cannot be stopped by using any of the GUI buttons. Therefor when running, the thread may only be
 * interacted with via direct commands from the Jython terminal.
 * <p>
 * The script must be located in folder known by the Jython interpreter.
 */
public class ScriptAdapter extends ScannableBase implements Scannable {

	/**
	 * This is the Jython documentation. Use it in the GDA Jython via the help command.
	 */
	public static String __doc__ = "This is Scannable which wraps a script. When it is moved to any position, the script is run.";

	String fileName = "";

	JythonServerFacade scriptingmediator = JythonServerFacade.getInstance();

	/**
	 *
	 */
	public ScriptAdapter() {
		level = 99;
	}

	/**
	 * ScriptAdapter
	 *
	 * @param fileName
	 *            This must be the name of a Jython script placed in a folder in the Jython path.
	 */
	public ScriptAdapter(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * ScriptAdapter
	 *
	 * @param name
	 *            The name of this object.
	 * @param fileName
	 *            This must be the name of a Jython script placed in a folder in the Jython path.
	 */
	public ScriptAdapter(String name, String fileName) {
		setName(name);
		this.fileName = fileName;
	}

	/**
	 * Set the name of the script this object encapsulates.
	 *
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Get the full path of the script.
	 *
	 * @return String
	 */
	public String getFileName() {
		return fileName;
	}

	@Override
	public Object getPosition() {
		// meaningless in this context
		return null;
	}

	@Override
	public void asynchronousMoveTo(Object position) {
		scriptingmediator.runScript(fileName);
	}

	@Override
	public void moveTo(Object increment) {
		scriptingmediator.runScript(fileName);
		try {
			while (runningThread()) {
				Thread.sleep(1000);
			}
		} catch (InterruptedException ex) {
		}
	}

	@Override
	public void stop() {
		scriptingmediator.beamlineHalt();
	}

	@Override
	public boolean isBusy() {
		return false;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int getLevel() {
		return level;
	}

	private boolean runningThread() {
		String isAlive = scriptingmediator.evaluateCommand("scriptThread.isAlive()");
		if (isAlive != null) {
			return isAlive.compareTo("1") == 0;
		}
		return false;
	}

	@Override
	public String[] getInputNames() {
		String[] temp = new String[1];
		temp[0] = fileName;
		return temp;
	}
}
