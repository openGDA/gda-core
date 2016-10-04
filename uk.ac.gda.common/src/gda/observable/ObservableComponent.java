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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Component that may be used by Observable objects to maintain its list of IObservers DO NOT synchronize the methods of this class as doing so in the past
 * led to deadlocks when used with DOFS and AbosulteMove objects
 */
public class ObservableComponent implements IObservable, IIsBeingObserved {
	private final Set<IObserver> myIObservers = new CopyOnWriteArraySet<>();
	private static final Logger logger = LoggerFactory.getLogger(ObservableComponent.class);

	/**
	 * Add an observer to the list of observers providing that the list does not already contain an instance of the observer on it. {@inheritDoc}
	 *
	 * @param anIObserver
	 *            the observer
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		if (anIObserver == null) {
			return;
		}
		myIObservers.add(anIObserver);
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
		if (anIObserver == null) {
			return;
		}
		myIObservers.remove(anIObserver);
	}

	/**
	 * Delete all observers from the list of observers. {@inheritDoc}
	 *
	 * @see gda.observable.IObservable#deleteIObservers()
	 */
	@Override
	public void deleteIObservers() {
		myIObservers.clear();
	}

	/**
	 * Notify all observers on the list of the requested change, swallowing any exceptions to ensure all observers are updated.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param changeCode
	 *            the data to be sent to the observer.
	 */
	public void notifyIObservers(Object theObserved, Object changeCode) {
		for (IObserver anIObserver : myIObservers) {
			try {
				sendEventToObserver(anIObserver, theObserved, changeCode);

			} catch (Exception ex) {
				// Don't log the full stack trace to keep the log tidier, full trace is in debug if enabled
				logger.error("swallowing exception: {}", ex.toString());

				/*
				 * Try to log something useful about the args of update which caused the exception to be thrown, but beware: ServerSideEventDispatcher.update
				 * details how calling toString (including via {} in logger) on Scannables can cause deadlocks...so just use class names to describe problem.
				 */
				final String anIObserverDescription = anIObserver == null ? "null" : anIObserver.getClass().getName();
				final String theObservedDescription = theObserved == null ? "null" : theObserved.getClass().getName();

				if (logger.isDebugEnabled()) {
					logger.debug("triggered by {}.update({}, {})", anIObserverDescription, theObservedDescription, changeCode, ex);
				}

			}
		}
	}

	protected void sendEventToObserver(IObserver anIObserver, Object theObserved, Object changeCode) {
		anIObserver.update(theObserved, changeCode);
	}

	@Override
	public boolean IsBeingObserved() {
		return !myIObservers.isEmpty();
	}

}
