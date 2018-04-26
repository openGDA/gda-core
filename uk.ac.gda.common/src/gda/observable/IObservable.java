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
 * A general purpose interface for an object to notify observing objects. Observing objects must implement the {@link IObserver} interface and must be added to
 * the {@link IObservable} object's list of IObserver by invoking the {@link #addIObserver(IObserver)} method.
 *
 * @see gda.observable.IObserver
 */
public interface IObservable {
	/**
	 * Add an object to this objects list of IObservers.
	 *
	 * @param observer
	 *            object that wishes to be notified by this object
	 */
	void addIObserver(IObserver observer);

	/**
	 * Delete an object from this objects list of IObservers.
	 *
	 * @param observer
	 *            object that no longer wishes to be notified by this object
	 */
	void deleteIObserver(IObserver observer);

	/**
	 * Delete all IObservers from this object's list of observing objects
	 */
	void deleteIObservers();
}
