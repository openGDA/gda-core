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

package gda.analysis.datastructure;

import gda.analysis.datastructure.event.DataChangeEvent;
import gda.analysis.datastructure.event.DataChangeObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Managed Data Object Class.
 */
public class ManagedDataObject {
	/** Storage for registered change listeners. */
	private List<DataChangeObserver> listeners;

	/** A flag that controls whether or not changes are notified. */
	private boolean notify = true;

	/** */
	private String name;

	/**
	 * A String detailing the type of the managed object e.g. Folder, 1DData, Function etc...
	 */
	private String type;

	/**
	 * Constructor.
	 */
	public ManagedDataObject() {
	}

	/**
	 * Returns the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @return A boolean.
	 */
	public boolean getNotify() {
		return this.notify;
	}

	/**
	 * Sets the flag that controls whether or not change events are sent to registered listeners.
	 * 
	 * @param notify
	 *            the new value of the flag.
	 */
	public void setNotify(boolean notify) {
		if (this.notify != notify) {
			this.notify = notify;
			fireDataChanged();
		}
	}

	/**
	 * Registers an object with this series, to receive notification whenever the series changes.
	 * <P>
	 * Objects being registered must implement the {@link DataChangeObserver} interface.
	 * 
	 * @param listener
	 *            the listener to register.
	 */
	public void addChangeListener(DataChangeObserver listener) {
		if (listeners == null)
			listeners = new ArrayList<DataChangeObserver>();
		this.listeners.add(listener);
	}

	/**
	 * Deregisters an object, so that it not longer receives notification whenever the series changes.
	 * 
	 * @param listener
	 *            the listener to deregister.
	 */
	public void removeChangeListener(DataChangeObserver listener) {
		this.listeners.remove(listener);
	}

	/**
	 * General method for signalling to registered listeners that the series has been changed.
	 */
	public void fireDataChanged() {
		if (this.notify) {
			notifyListeners(new DataChangeEvent(this));
		}
	}

	/**
	 * Sends a change event to all registered listeners.
	 * 
	 * @param event
	 *            contains information about the event that triggered the notification.
	 */
	protected void notifyListeners(DataChangeEvent event) {
		if (listeners != null) {
			for (Iterator<DataChangeObserver> itr = listeners.iterator(); itr.hasNext();) {
				DataChangeObserver ist = itr.next();
				ist.dataChanged(event);
			}
		}
	}

	/**
	 * Returns the name.
	 * 
	 * @return name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the type.
	 * 
	 * @return type as String.
	 */
	public String getType() {
		return type;
	}
}
