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

package gda.analysis.datastructure.event;

/**
 * DataChangeObservable Interface
 */
public interface DataChangeObservable {
	/**
	 * Returns the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @return A boolean.
	 */
	public boolean getNotify();

	/**
	 * Sets the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @param notify
	 *            the new value of the flag.
	 */
	public void setNotify(boolean notify);

	/**
	 * Registers an object with this series, to receive notification whenever the series changes.
	 * <P>
	 * Objects being registered must implement the {@link DataChangeObserver} interface.
	 * 
	 * @param listener
	 *            the listener to register.
	 */
	public void addChangeListener(DataChangeObserver listener);

	/**
	 * Deregisters an object, so that it not longer receives notification whenever the series changes.
	 * 
	 * @param listener
	 *            the listener to deregister.
	 */
	public void removeChangeListener(DataChangeObserver listener);

	/**
	 * Sends a change event to all registered listeners.
	 * 
	 * @param event
	 *            contains information about the event that triggered the notification.
	 */
	public void notifyListeners(DataChangeEvent event);
}
