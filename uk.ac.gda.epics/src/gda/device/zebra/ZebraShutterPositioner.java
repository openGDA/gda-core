/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.zebra;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.enumpositioner.ValveBase;
import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.impl.ZebraImpl;
import gda.factory.FactoryException;
import gda.observable.Observable;
import gda.observable.Observer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An {@link EnumPositioner} that operates a Zebra soft input.
 */
public class ZebraShutterPositioner extends ValveBase {

	private static final Logger logger = LoggerFactory.getLogger(ZebraShutterPositioner.class);

	private ZebraImpl zebra;

	private int inputNumber;

	public void setZebra(ZebraImpl zebra) {
		this.zebra = zebra;
	}

	public void setInputNumber(int inputNumber) {
		this.inputNumber = inputNumber;
	}

	@Override
	public void configure() throws FactoryException {

		setPositions(new String[] {OPEN, CLOSE});

		try {
			zebra.getSoftInputObservable().addObserver(new Observer<SoftInputChangedEvent>() {
				@Override
				public void update(Observable<SoftInputChangedEvent> source, SoftInputChangedEvent arg) {
					if (arg.getInputNumber() == inputNumber) {
						notifyIObservers(this, softInputStateToPosition(arg.isSet()));
					}
				}
			});
		} catch (Exception e) {
			logger.error("Unable to monitor Zebra soft inputs", e);
		}
	}

	@Override
	public Object rawGetPosition() throws DeviceException {
		return softInputStateToPosition(get());
	}

	private static String softInputStateToPosition(boolean state) {
		return state ? OPEN : CLOSE;
	}

	@Override
	public void rawAsynchronousMoveTo(Object position) throws DeviceException {

		final String newPos = position.toString();

		if (!newPos.equals(OPEN) && !newPos.equals(CLOSE)) {
			throw new DeviceException("Invalid position: " + position);
		}

		set(newPos.equals(OPEN));
	}

	private boolean get() throws DeviceException {
		try {
			return zebra.isSoftInputSet(inputNumber);
		} catch (IOException e) {
			throw new DeviceException("Could not get position", e);
		}
	}

	private void set(boolean set) throws DeviceException {
		try {
			zebra.setSoftInput(inputNumber, set);
		} catch (IOException e) {
			throw new DeviceException("Could not set position", e);
		}
	}

}
