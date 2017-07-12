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

package uk.ac.gda.server.ncd.scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.scannable.DummyUnitsScannable;
import gda.device.scannable.ScannablePositionChangeEvent;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.scan.Scan.ScanStatus;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class ScanResetDummyUnitScannable extends DummyUnitsScannable  implements IObserver {

	private double resetValue = Double.NaN;
	private boolean scanRunning = false;
	private static final Logger logger = LoggerFactory.getLogger(ScanResetDummyUnitScannable.class);
	
	public ScanResetDummyUnitScannable() {
		super();
	}
	
	@Override
	public void configure() {
		super.configure();
		try {
			moveTo(resetValue);
		} catch (DeviceException de) {
			//life goes on but log problem anyway
			logger.error("Could not set initial value for {}", getName(), de);
		}
		InterfaceProvider.getJSFObserver().addIObserver(this);
	}
	
	@Override
	public Double getPosition() throws DeviceException {
		double raw = (Double)super.getPosition();
		return raw;// == 0 ? Double.NaN : raw;
	}

	@Override
	public void moveTo(Object position) throws DeviceException {
		super.moveTo(position);
	}
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		if (externalPosition == null) {
			externalPosition = Double.NaN;
		}
		super.asynchronousMoveTo(externalPosition);
		notifyIObservers(this, new ScannablePositionChangeEvent(getPosition()));
	}
	
	public void setResetValue(double reset) {
		resetValue = reset;
	}

	public void reset() throws DeviceException {
		moveTo(resetValue);
	}
	
	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof ScanStatus) {
			ScanStatus ss = (ScanStatus) arg;
			boolean nowIdle = ss.isComplete();
			if (scanRunning && nowIdle) {
				try {
					moveTo(resetValue);
				} catch (DeviceException e) {
				}
			}
			scanRunning = !nowIdle;
		}
	}
}
