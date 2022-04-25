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

package gda.device.detector.hardwaretriggerable;

import java.text.MessageFormat;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.jython.ITerminalPrinter;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;

public abstract class DummyHardwareTriggerableDetectorBase extends HardwareTriggerableDetectorBase {
	private static final Logger logger = LoggerFactory.getLogger(DummyHardwareTriggerableDetectorBase.class);

	public boolean simulate = false;

	public CountDownLatch simulatedCollectionComplete;

	private int status = Detector.IDLE;
	private boolean integrating = false;

	@Override
	public void collectData() throws DeviceException {
		if (simulate) {
			status = Detector.BUSY;
			getTerminal().print(MessageFormat.format("{0}.arm() collecting {1} hardware triggered images ...\n", getName(),
					getNumberImagesToCollect()));
			simulatedCollectionComplete = new CountDownLatch(getNumberImagesToCollect());
			Async.execute(new MakeIdleWhenSimulatedCollectionComplete(getName()));
		} else {
			getTerminal().print(MessageFormat.format(
					"{0}.arm() called while configured to collect {1} hardware triggered images over {2}s ...\n",
					getName(), getHardwareTriggerProvider().getNumberTriggers(), getHardwareTriggerProvider()
							.getTotalTime()));
		}
	}

	@Override
	public int getStatus() {
		return status;
	}

	void setStatus(int status) {
		this.status = status;
	}

	class MakeIdleWhenSimulatedCollectionComplete implements Runnable {


		private final String name;
		public MakeIdleWhenSimulatedCollectionComplete(String name) {
			this.name = name;
		}
		@Override
		public void run() {
			try {
				simulatedCollectionComplete.await();
			} catch (InterruptedException e) {
				getTerminal().print(name + " interrupted with " + simulatedCollectionComplete.getCount() +" triggers remaining\n");
			} finally {
				getTerminal().print(name + " hardware triggered collection complete\n");
				status = Detector.IDLE;
			}
		}
	}

	@Override
	public void update(Object source, Object arg){
		try {
			simulatedTriggerRecieved();
		} catch (DeviceException e) {
			logger.error("Error in update for " + getName(), e);
		}
		simulatedCollectionComplete.countDown();
	}

	abstract void simulatedTriggerRecieved() throws DeviceException ;

	@Override
	public boolean integratesBetweenPoints() {
		return integrating;
	}

	public void setIntegrating(boolean b) {
		integrating = b;
	}

	protected ITerminalPrinter getTerminal() {
		return InterfaceProvider.getTerminalPrinter();
	}

}
