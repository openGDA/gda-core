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

import java.io.IOException;

import gda.device.DeviceException;
import gda.device.EnumPositioner;
import gda.device.enumpositioner.ValveBase;
import gda.device.zebra.controller.SoftInputChangedEvent;
import gda.device.zebra.controller.impl.ZebraImpl;
import gda.factory.FactoryException;
import gda.observable.IObserver;

/**
 * An {@link EnumPositioner} that operates a Zebra soft input.
 */
public class ZebraShutterPositioner extends ValveBase {

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
		
		zebra.addIObserver(new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof SoftInputChangedEvent) {
					final SoftInputChangedEvent sice = (SoftInputChangedEvent) arg;
					if (sice.getInputNumber() == inputNumber) {
						notifyIObservers(this, softInputStateToPosition(sice.isSet()));
					}
				}
			}
		});
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
