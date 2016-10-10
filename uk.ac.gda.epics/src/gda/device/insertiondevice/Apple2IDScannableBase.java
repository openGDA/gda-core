/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.insertiondevice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gda.observable.IObserver;

/**
 * Base class for a Scannable to allow control of an AppleII ID
 */
public abstract class Apple2IDScannableBase extends ScannableBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(Apple2IDScannableBase.class);

	protected IApple2ID controller;

	// interface InitializationListener

	@Override
	public void initializationCompleted() {
		logger.info("{} initialisation completed.", getName());
	}

	// ScannableBase overrides

	@Override
	public void configure() throws FactoryException {
		super.configure();
		controller.configure();
		controller.addIObserver(new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				notifyIObservers(source, arg);
			}
		});
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return controller.isBusy();
	}

	protected boolean motorPositionsEqual(final double a, final double b) {
		return controller.motorPositionsEqual(a, b);
	}

	protected double parseParamToDouble(final Object param) {
		if (param instanceof Number) {
			return (((Number) param).doubleValue());
		}
		return Double.parseDouble(param.toString());
	}

	// Configuration
	public void setController(IApple2ID controller) {
		this.controller = controller;
	}

	public IApple2ID getController() {
		return controller;
	}
}
