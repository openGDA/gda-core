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

package gda.observable;

import javax.swing.SwingUtilities;

/**
 * Class for calling an IObserver's update method in the EventDispatchThread. The recomended way of updating GUI
 * components to avoid thread problems.
 * 
 * @see gda.observable.UpdateDelayer
 */
public class LaterUpdater implements Runnable {
	private IObserver iObserver;

	private Object objectOne;

	private Object objectTwo;

	/**
	 * Constructor where objectOne is usually an IObservable passed on from a directly called update() and objectTwo the
	 * second argument of that call.
	 * 
	 * @param iObserver
	 *            the IObserver whose update will be called
	 * @param objectOne
	 *            will be passed as first argument to update method
	 * @param objectTwo
	 *            will be passed as second argument to update method
	 */
	public LaterUpdater(IObserver iObserver, Object objectOne, Object objectTwo) {
		this.iObserver = iObserver;
		this.objectOne = objectOne;
		this.objectTwo = objectTwo;

		// The SwingUtilities EventDispatchThread will call run() at some
		// suitable later time.
		SwingUtilities.invokeLater(this);
	}

	/**
	 * Implements the runnable interface. Calls the iObserver's update() method with the specified arguments.
	 */
	@Override
	public void run() {
		iObserver.update(objectOne, objectTwo);
	}
}
