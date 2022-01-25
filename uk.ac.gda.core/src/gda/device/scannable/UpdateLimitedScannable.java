/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package gda.device.scannable;

import java.util.Date;
import java.util.function.Supplier;

import gda.device.Scannable;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.gda.api.remoting.ServiceInterface;

/**
 * A {@link PassthroughScannableDecorator} that will rate limit the updates received from the scannable delegate and
 * only forward them to observers at the set rate.
 *
 * If rates are being received from the delegate slower than the specified rate limit then the rate will match that of
 * the delegate.
 *
 * Will only add an observable to the delegate if it is itself being observed.
 */
@ServiceInterface(Scannable.class)
public class UpdateLimitedScannable extends PassthroughScannableDecorator implements IObserver {
	private long lastUpdate = 0;
	private double msBetweenUpdates = 0;
	private ObservableComponent observableComponent = new ObservableComponent();
	private Supplier<Date> dateFactory;

	public UpdateLimitedScannable(Scannable delegate, Supplier<Date> dateFactory) {
		super(delegate);
		this.dateFactory = dateFactory;
	}

	public UpdateLimitedScannable(Scannable delegate) {
		this(delegate, Date::new);
	}

	/**
	 * Sets the time between updates in milliseconds.
	 *
	 * @param msBetweenUpdates
	 *            time between updates in milliseconds
	 */
	public void setMsBetweenUpdates(double msBetweenUpdates) {
		this.msBetweenUpdates = msBetweenUpdates;
	}

	@Override
	public void addIObserver(IObserver observer) {
		if (!observableComponent.isBeingObserved()) {
			delegate.addIObserver(this);
		}
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
		if (!observableComponent.isBeingObserved()) {
			delegate.deleteIObserver(this);
		}
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
		delegate.deleteIObserver(this);
	}

	@Override
	public void update(Object source, Object arg) {
		long currentTime = dateFactory.get().getTime();
		if ((currentTime - lastUpdate) > msBetweenUpdates) {
			lastUpdate = currentTime;
			observableComponent.notifyIObservers(this, arg);
		}
	}
}
