/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.device.detector.countertimer;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import gda.device.DeviceException;
import gda.device.detector.DetectorWithReadout;
import gda.device.scannable.PositionCallableProvider;

public class CallableTfgScalerWithLogValues extends TfgScalerWithLogValues implements PositionCallableProvider<double[]>, DetectorWithReadout {

	AtomicBoolean readingOut = new AtomicBoolean(false);
	@Override
	public Callable<double[]> getPositionCallable() throws DeviceException {
		setReadingOut(true);
		Callable<double[]> callable = new Callable<double[]>(){
			@Override
			public double[] call() throws Exception {
			double[] reply = getSuperReadout();
			setReadingOut(false);
			return reply;
			}
		};		
			return callable;
	}

	@Override
	public double[] readout() throws DeviceException {
		Callable<double[]> positionCallable = getPositionCallable();

		try {
			double[] treeProvider = positionCallable.call();
			return treeProvider;
		} catch (DeviceException e) {
			throw e;
		} catch (Exception e) {
			throw new DeviceException("something wrong in the callable", e);
		}

	}
	
	protected double[] getSuperReadout() throws DeviceException {
		return super.readout();
	}

	private void setReadingOut(boolean readingOut) {
		synchronized (this.readingOut) {
			this.readingOut.set(readingOut);
			this.readingOut.notifyAll();
		}
	}
	@Override
	public void waitForReadoutCompletion() throws InterruptedException {
		synchronized (readingOut) {
			while (readingOut.get()) {
				try{
				readingOut.wait();
				}
				catch (InterruptedException e) {
					setReadingOut(false);
				}
			}
		}
	}

}
