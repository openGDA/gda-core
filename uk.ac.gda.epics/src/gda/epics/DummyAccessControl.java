/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package gda.epics;

import gda.factory.FindableBase;
import gda.observable.IObservable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;

public class DummyAccessControl extends FindableBase implements IAccessControl, IObservable {

	private Status status = Status.ENABLED;
	private final ObservableComponent observableComponent = new ObservableComponent();

	@Override
	public Status getAccessControlState() {
		return status;
	}

	@Override
	public Status getStatus() {
		return this.status;
	}

	public void setStatus(Status status) {
		if (this.status != status) {
			notifyIObservers(this, status);
			this.status = status;
		}
	}

	/**
	 * Add an object to this objects's list of IObservers.
	 *
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void addIObserver(IObserver anIObserver) {
		observableComponent.addIObserver(anIObserver);

	}

	/**
	 * Delete an object from this objects's list of IObservers.
	 *
	 * @param anIObserver
	 *            object that implement IObserver and wishes to be notified by this object
	 */
	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observableComponent.deleteIObserver(anIObserver);

	}

	/**
	 * delete all IObservers from list of observing objects
	 */
	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();

	}

	/**
	 * Notify all observers on the list of the requested change.
	 *
	 * @param theObserved
	 *            the observed component
	 * @param theArgument
	 *            the data to be sent to the observer.
	 */
	private void notifyIObservers(Object theObserved, Object theArgument) {
		observableComponent.notifyIObservers(theObserved, theArgument);
	}

}
