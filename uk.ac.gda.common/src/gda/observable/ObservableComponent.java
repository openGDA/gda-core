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

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Component that may be used by Observable objects to maintain its list of IObservers
 * DO NOT synchronize the methods of this class as doing so in the past led to deadlocks when used with DOFS and AbosulteMove objects
 */
public class ObservableComponent implements IObservable, IIsBeingObserved {
	private Vector<IObserver> myIObservers = new Vector<IObserver>();
	private static final Logger logger = LoggerFactory.getLogger(ObservableComponent.class);

	/**
	 * Add an observer to the list of observers providing that the list does not already contain an instance of the
	 * observer on it. {@inheritDoc}
	 * 
	 * @param anIObserver
	 *            the observer
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		if( anIObserver  == null)
			return;
		synchronized(myIObservers){
			if (!myIObservers.contains(anIObserver))
				myIObservers.addElement(anIObserver);
		}
	}

	/**
	 * Delete the instance of this observer from the list observers. {@inheritDoc}
	 * 
	 * @param anIObserver
	 *            the observer
	 * @see gda.observable.IObservable#deleteIObserver(gda.observable.IObserver)
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		if( anIObserver  == null)
			return;
		synchronized(myIObservers){
			myIObservers.removeElement(anIObserver);
		}
	}

	/**
	 * Delete all observers from the list of observers. {@inheritDoc}
	 * 
	 * @see gda.observable.IObservable#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		synchronized(myIObservers){
			myIObservers.removeAllElements();
		}
	}

	/**
	 * Notify all observers on the list of the requested change.
	 * 
	 * @param theObserved
	 *            the observed component
	 * @param changeCode
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object changeCode) {
		IObserver[] observers;
		synchronized(myIObservers){
			observers = myIObservers.toArray(new IObserver[0]);
		}
		
		for (IObserver anIObserver : observers) {
			try {
				anIObserver.update(theObserved, changeCode);
			} catch (Exception ex) {
				logger.error("notifyIObservers of " + theObserved.toString(), ex);
			}
		}
	}

	@Override
	public boolean IsBeingObserved() {
		return !myIObservers.isEmpty();
	}

}
