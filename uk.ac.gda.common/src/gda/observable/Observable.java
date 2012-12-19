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

package gda.observable;

/**
 * Interface for an object that supports type observation
 *
 */
public interface Observable<E> extends IIsBeingObserved{
	/**
	 * Add an object to this objects's list of IObservers.
	 * 
	 * @param observer
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	void addIObserver(Observer<E> observer) throws Exception;

	/**
	 * Delete an object from this objects's list of IObservers.
	 * 
	 * @param observer
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	void deleteIObserver(Observer<E> observer);

	/**
	 * delete all IObservers from list of observing objects
	 */
	void deleteIObservers();
}
