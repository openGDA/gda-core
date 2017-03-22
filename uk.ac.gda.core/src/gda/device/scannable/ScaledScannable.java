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

package gda.device.scannable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;
import gda.observable.IObserver;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
public class ScaledScannable extends ScannableBase {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ScaledScannable.class);
	
	private Scannable scannable;
	
	public void setScannable(Scannable scannable) {
		this.scannable = scannable;
	}
	
	private double scalingFactor;
	
	public void setScalingFactor(double scalingFactor) {
		this.scalingFactor = scalingFactor;
	}
	
	@Override
	public void configure() throws FactoryException {
		scannable.addIObserver(new IObserver() {
			@Override
			public void update(Object source, Object arg) {
				if (arg instanceof ScannablePositionChangeEvent) {
					final ScannablePositionChangeEvent actualSpce = (ScannablePositionChangeEvent) arg;
					final double actualPos = (Double) actualSpce.newPosition;
					final double convertedPos = actualPos * scalingFactor;
					ScannablePositionChangeEvent convertedSpce = new ScannablePositionChangeEvent(convertedPos);
					notifyIObservers(this, convertedSpce);
				}
			}
		});
	}
	
	@Override
	public boolean isBusy() throws DeviceException {
		return scannable.isBusy();
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		final double actualPos = ScannableUtils.getCurrentPositionArray(scannable)[0];
		final double convertedPos = actualPos * scalingFactor;
		return convertedPos;
	}
	
	@Override
	public void asynchronousMoveTo(Object externalPosition) throws DeviceException {
		final Double[] convertedPos = ScannableUtils.objectToArray(externalPosition);
		final double actualPos = convertedPos[0] / scalingFactor;
		scannable.asynchronousMoveTo(actualPos);
	}
	
}
