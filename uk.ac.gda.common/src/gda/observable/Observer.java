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
 * Interface for an object that is observing an {@link Observable}
 *
 */
@FunctionalInterface
public interface Observer<E> {
	/**
	 * Called whenever an observed object is changed.
	 *
	 * @param source
	 *            the object being observed
	 * @param arg
	 *            the object specific to the observed object and which representing the change being notified (eg data,
	 *            state etc.)
	 */
	void update(Observable<E> source, E arg);
}
