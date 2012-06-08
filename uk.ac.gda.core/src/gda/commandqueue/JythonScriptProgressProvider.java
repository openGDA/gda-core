/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

import gda.jython.ScriptBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

public class JythonScriptProgressProvider implements IObservable {

	private static JythonScriptProgressProvider instance;

	public static JythonScriptProgressProvider getInstance() {
		if (instance == null) {
			instance = new JythonScriptProgressProvider();
		}
		return instance;
	}

	/*
	 * Ensure there is only 1 instance by making constructor private. Users must call getInstance
	 */
	private JythonScriptProgressProvider() {
	}

	/**
	 * Updates observers, pauses if required and throws exception if stopped
	 * 
	 * @param percentDone
	 * @param msg
	 * @throws InterruptedException
	 */
	static public void sendProgress(float percentDone, String msg) throws InterruptedException {
		// pause if required.
		sendProgress(percentDone, msg, true);
	}

	/**
	 * Updates observers, pauses if required and throws exception if stopped
	 * 
	 * @param percentDone
	 * @param msg
	 * @throws InterruptedException
	 */
	static public void sendProgress(int percentDone, String msg) throws InterruptedException {
		// pause if required.
		sendProgress(percentDone, msg, true);
	}

	/**
	 * Updates observers, does not check for pauses if requested
	 * 
	 * @param percentDone
	 * @param msg
	 * @param checkForPause
	 *            - if true waits whilst in paused state.
	 * @throws InterruptedException
	 *             - if script is interrupted
	 */
	static public void sendProgress(int percentDone, String msg, boolean checkForPause) throws InterruptedException {
		if (checkForPause) {
			ScriptBase.checkForPauses();
		}
		getInstance().updateProgress(percentDone, msg);
	}

	/**
	 * Updates observers, does not check for pauses if requested
	 * 
	 * @param percentDone
	 * @param msg
	 * @param checkForPause
	 *            - if true waits whilst in paused state.
	 * @throws InterruptedException
	 *             - if script is interrupted
	 */
	static public void sendProgress(float percentDone, String msg, boolean checkForPause) throws InterruptedException {
		if (checkForPause) {
			ScriptBase.checkForPauses();
		}
		getInstance().updateProgress(percentDone, msg);
	}

	ObservableComponent obsComp = new ObservableComponent();

	public void updateProgress(float percentDone, String msg) {
		obsComp.notifyIObservers(this, new SimpleCommandProgress(percentDone, msg));
	}

	/**
	 * @param anIObserver
	 * @see gda.observable.ObservableComponent#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		obsComp.addIObserver(anIObserver);
	}

	/**
	 * @param anIObserver
	 * @see gda.observable.ObservableComponent#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		obsComp.deleteIObserver(anIObserver);
	}

	/**
	 * @see gda.observable.ObservableComponent#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		obsComp.deleteIObservers();
	}
}