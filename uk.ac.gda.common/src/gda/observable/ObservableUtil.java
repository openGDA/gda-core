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

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Component that may be used by Observable objects to maintain its list of Observers
 */
public class ObservableUtil<E> implements Observable<E>, IIsBeingObserved {

	private Vector<Observer<E>> myObservers = new Vector<Observer<E>>();

	private Map<Observer<E>, Predicate<E>> observersPredicates = new HashMap<Observer<E>, Predicate<E>>();

	private static final Logger logger = LoggerFactory.getLogger(ObservableUtil.class);

	/**
	 * Add an observer to the list of observers providing that the list does not already contain an instance of the
	 * observer on it. If a predicate was associated with this observer then remove it. {@inheritDoc}
	 *
	 * @param observer
	 *            the observer
	 * @see gda.observable.Observable#addObserver(gda.observable.Observer)
	 */
	@Override
	public void addObserver(Observer<E> observer) {
		if (observer == null)
			return;
		synchronized(myObservers){
			if (!myObservers.contains(observer)){
				myObservers.addElement(observer);
			}
			observersPredicates.remove(observer);
		}
	}

	/**
	 * Add an observer to the list of observers providing that the list does not already contain an instance of the
	 * observer on it. Either way, associate the specified predicate with the observer. {@inheritDoc}
	 *
	 * @param observer
	 *            the observer
	 * @see gda.observable.Observable#addObserver(gda.observable.Observer)
	 */
	@Override
	public void addObserver(Observer<E> observer, Predicate<E> predicate) {
		if (observer == null)
			return;
		synchronized(myObservers){
			if (!myObservers.contains(observer)){
				myObservers.addElement(observer);
			}
			observersPredicates.put(observer, predicate);
		}
	}

	/**
	 * Delete the instance of this observer from the list observers. {@inheritDoc}
	 *
	 * @param observer
	 *            the observer
	 * @see gda.observable.Observable#removeObserver(gda.observable.Observer)
	 */
	@Override
	public void removeObserver(Observer<E> observer) {
		if (observer == null)
			return;
		synchronized(myObservers){
			myObservers.removeElement(observer);
			observersPredicates.remove(observer);
		}
	}

	/**
	 * Delete all observers from the list of observers.
	 */
	public void deleteIObservers() {
		synchronized(myObservers){
			myObservers.removeAllElements();
			observersPredicates.clear();
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
	@SuppressWarnings("unchecked")
	public void notifyIObservers(Observable<E> theObserved, E changeCode) {

		Observer<E>[] observers;

		synchronized(myObservers){
			observers = (Observer<E>[]) myObservers.toArray(new Observer<?>[0]);
		}

		for (Observer<E> observer : observers) {
			try {
				Predicate<E> predicate = observersPredicates.get(observer);
				if (predicate != null) {
					// if predicate is configured and it is *not* true then don't send the update.
					if (!predicate.apply(changeCode)) {
						continue;
					}
				}
				observer.update(theObserved, changeCode);
			} catch (Exception ex) {
				logger.error("notifyIObservers of " + theObserved.toString(), ex);
			}
		}
	}

	@Override
	public boolean IsBeingObserved() {
		return !myObservers.isEmpty();
	}

}
