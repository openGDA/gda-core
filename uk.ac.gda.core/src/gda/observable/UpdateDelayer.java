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

/**
 * Stands between an IObserver and and IObservable and ensures that the IObservers's update method is only called in the
 * EventDispatchThread.
 *
 * @see gda.observable.LaterUpdater
 */
public class UpdateDelayer implements IObserver {
	IObserver iObserver;

	/**
	 * Constructor. An IObserver which wants to IObserve an IObservable but be sure that its update method is only
	 * called in the Swing EventDispatch Thread should create an UpdateDelayer instead of adding itself as an IObserver.
	 * Replace: iObservable.addIObserver(this); with: ud = new UpdateDelayer(this, iObservable);
	 *
	 * @param iObserver
	 *            the observer
	 * @param iObservable
	 *            the observable
	 */
	public UpdateDelayer(IObserver iObserver, IObservable iObservable) {
		this.iObserver = iObserver;

		// the UpdateDisplayer IObserves the specified IObservable directly
		iObservable.addIObserver(this); //FIXME: potential race condition
	}

	/**
	 * Implements IObserver interface - called by the IObservable.
	 *
	 * @param observable
	 *            the IObservable
	 * @param argument
	 *            the argument passed to the notifyIObservers
	 */
	@SuppressWarnings("unused")
	@Override
	public void update(Object observable, Object argument) {
		// LaterUpdater will call iObserver's update later (in the
		// EventDispatchThread) with arguments observable and argument.
		new LaterUpdater(iObserver, observable, argument);
	}
}