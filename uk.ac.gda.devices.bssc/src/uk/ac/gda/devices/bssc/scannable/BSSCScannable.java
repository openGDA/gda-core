/*-
 * Copyright © 2011-2013 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.bssc.scannable;

import org.embl.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.devices.bssc.BioSAXSSampleChanger;

@ServiceInterface(Scannable.class)
public class BSSCScannable extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(BSSCScannable.class);

	BioSAXSSampleChanger bssc;
	Thread poller;
	final BSSCScannable us = this;
	public int waittime = 20000;
	Object cachedPosition = null;

	class Poller implements Runnable {

		Object lastUpdateSent = null;

		@Override
		public void run() {
			while (true) {
				try {
					synchronized (us) {
						us.wait(waittime);
						Thread.sleep(100);
					}
				} catch (InterruptedException e) {
					// Reset interrupt status and terminate loop
					Thread.currentThread().interrupt();
					return;
				}
				if (cachedPosition != null && cachedPosition != lastUpdateSent) {
					notifyIObservers(us, cachedPosition);
					lastUpdateSent = cachedPosition;
				} else {
					try {
						notifyIObservers(us, us.getPosition());
						lastUpdateSent = cachedPosition;
					} catch (DeviceException e) {
						logger.error("error reading postion for updates", e);
					}
				}
			}
		}
	}

	public void whackPoller() {
		synchronized (us) {
			us.notify();
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (isConfigured()) {
			return;
		}
		super.configure();
		setInputNames(new String[] {});
		setExtraNames(new String[] { "seutemp", "storagetemp", "detergentlevel", "waterlevel", "wastelevel" });
		setOutputFormat(new String[] { "%3.1f", "%3.1f", "%2.0f", "%2.0f", "%2.0f" });
		if (poller == null || !poller.isAlive()) {
			poller = new Thread(new Poller(), getName() + " polling thread");
			poller.start();
		}
		setConfigured(true);
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return !bssc.isReady();
		} catch (BaseException e) {
			throw new DeviceException("error getting state", e);
		}
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			cachedPosition = new double[] { bssc.getTemperatureSEU(), bssc.getTemperatureSampleStorage(),
					bssc.getDetergentLevel(), bssc.getWaterLevel(), bssc.getWasteLevel() };
			whackPoller();
			return cachedPosition;
		} catch (BaseException e) {
			throw new DeviceException("Error getting sample changer values", e);
		}
	}

	public BioSAXSSampleChanger getBssc() {
		return bssc;
	}

	public void setBssc(BioSAXSSampleChanger bssc) {
		this.bssc = bssc;
	}

	public void load() throws DeviceException {
		try {
			this.bssc.loadPlates();
		} catch (BaseException e) {
			throw new DeviceException("error loading plates", e);
		}
	}

	public void scanAndPark() throws DeviceException {
		try {
			this.bssc.scanAndPark();
		} catch (BaseException e) {
			throw new DeviceException("error scanning and parking plates", e);
		}
	}
}