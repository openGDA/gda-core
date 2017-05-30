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

import gda.configuration.properties.LocalProperties;

/**
 * A Component that may be used by Observable objects to maintain a set of IObservers.
 * <p>
 * It is thread safe due to the use of a {@link CopyOnWriteArraySet} being used to hold the {@link IObserver}'s.
 */
public class ObservableComponent implements IObservable, IIsBeingObserved {
	private static final Logger logger = LoggerFactory.getLogger(ObservableComponent.class);
	private static final String TIME_TO_UPDATE_WARNING_PROPERTY = "gda.observable.notificationWarningTime";
	private static final int TIME_TO_UPDATE_WARNING_DEFAULT_MILLIS = 100; //ms

	/**
	 * This is the time in ms, which if exceeded while updating an observer of an event will cause a warning to be logged. It can be set by the property
	 * {@value #TIME_TO_UPDATE_WARNING_PROPERTY}. If not set it defaults to {@value #TIME_TO_UPDATE_WARNING_DEFAULT_MILLIS} ms.
	 */
	private static final long TIME_TO_UPDATE_WARNING_MILLIS = LocalProperties.getAsInt(TIME_TO_UPDATE_WARNING_PROPERTY, TIME_TO_UPDATE_WARNING_DEFAULT_MILLIS);

	/**
	 * This is the set of observer that will be notified of events. {@link CopyOnWriteArraySet} is chosen here because although it's slow to write to the set
	 * (i.e. when adding or removing observer) it's very fast to iterate though the set. Typically modifying observers in done infrequently compared to the
	 * number of events sent, this offers good performance.
	 */
	private final Set<IObserver> myIObservers = new CopyOnWriteArraySet<>();

	/**
	 * Add an observer to the list of observers providing that the list does not already contain an instance of the observer on it. {@inheritDoc}
	 *
	 * @param anIObserver
	 *            the observer
	 * @see gda.observable.IObservable#addIObserver(gda.observable.IObserver)
	 * @throws IllegalArgumentException
	 *             If anIObserver is null
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		if (anIObserver == null) {
			throw new IllegalArgumentException("Can't add a null observer");
		}
		myIObservers.add(anIObserver);
	}

	/**
	 * Delete the instance of this observer from the list observers. {@inheritDoc}
	 *
	 * @param anIObserver
	 *            the observer
	 * @see gda.observable.IObservable#deleteIObserver(gda.observable.IObserver)
	 * @throws IllegalArgumentException
	 *             If anIObserver is null
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		if (anIObserver == null) {
			throw new IllegalArgumentException("Can't delete a null observer");
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

				if (logger.isDebugEnabled()) {
					/*
					 * Try to log something useful about the args of update which caused the exception to be thrown, but beware: ServerSideEventDispatcher.update
					 * details how calling toString (including via {} in logger) on Scannables can cause deadlocks...so just use class names to describe problem.
					 */
					// TODO Probably this workaround to avoid calling toString in no longer required. Scannable.toFormattedString was added.
					// Additionally this class now no longer contains explicit synchronized methods due to the use of CopyOnWriteArraySet
					final String anIObserverDescription = anIObserver == null ? "null" : anIObserver.getClass().getName();
					final String theObservedDescription = theObserved == null ? "null" : theObserved.getClass().getName();

					logger.debug("triggered by {}.update({}, {})", anIObserverDescription, theObservedDescription, changeCode, ex);
				}

			}
		}
	}

	/**
	 * This is the method which actually updates {@link IObserver}s with new events.
	 *
	 * @param anIObserver
	 *            The {@link IObserver} to update i.e. the sink of events
	 * @param theObserved
	 *            The {@link IObservable} being observed i.e. the source of events
	 * @param changeCode
	 *            The event to be updated about
	 */
	protected void sendEventToObserver(final IObserver anIObserver, final Object theObserved, final Object changeCode) {
		// Store the start time
		final long startMills = System.currentTimeMillis();

		// Actually do the update
		anIObserver.update(theObserved, changeCode);

		// Now do some performance analysis and possibly logging
		final long endMillis = System.currentTimeMillis();
		final long timeToUpdate = endMillis - startMills;
		if (timeToUpdate > TIME_TO_UPDATE_WARNING_MILLIS) { // Took "too long" to update
			logger.warn("Notifying observer '{}' of the event '{}' from '{}' took {} ms", anIObserver, changeCode, theObserved, timeToUpdate);
		} else if (logger.isTraceEnabled()) { // if trace is enabled and not already logged, log the performance of the event
			logger.trace("Notifying observer '{}' of the event '{}' from '{}' took {} ms", anIObserver, changeCode, theObserved, timeToUpdate);
		}
	}

	@Override
	public boolean IsBeingObserved() {
		return !myIObservers.isEmpty();
	}

}
