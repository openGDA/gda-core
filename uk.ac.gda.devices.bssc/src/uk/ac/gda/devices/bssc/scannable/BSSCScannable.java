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

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.device.scannable.corba.impl.ScannableAdapter;
import gda.device.scannable.corba.impl.ScannableImpl;
import gda.factory.FactoryException;
import gda.factory.corba.util.CorbaAdapterClass;
import gda.factory.corba.util.CorbaImplClass;

import org.embl.BaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.bssc.BioSAXSSampleChanger;

@CorbaAdapterClass(ScannableAdapter.class)
@CorbaImplClass(ScannableImpl.class)
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
		super.configure();
		try {
			setInputNames(new String[] {});
			setExtraNames(new String[] { "detergentlevel", "waterlevel", "wastelevel" });
			setOutputFormat(new String[] { "%2.0f", "%2.0f", "%2.0f" });
			bssc.getTemperatureSampleStorage(); // WARNING - this generates an exception, so the logic expects the
												// following code not to be run in all cases
			setExtraNames(new String[] { "seutemp", "storagetemp", "detergentlevel", "waterlevel", "wastelevel" });
			setOutputFormat(new String[] { "%3.1f", "%3.1f", "%2.0f", "%2.0f", "%2.0f" });
		} catch (Exception ignored) {
			// normal behaviour in simulation
		}
		if (poller == null || !poller.isAlive()) {
			poller = new Thread(new Poller(), getName() + " polling thread");
			poller.start();
		}
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
			if (getExtraNames().length == 3) {
				cachedPosition = new double[] { bssc.getDetergentLevel(), bssc.getWaterLevel(), bssc.getWasteLevel() };
			} else {
				cachedPosition = new double[] { bssc.getTemperatureSEU(), bssc.getTemperatureSampleStorage(),
						bssc.getDetergentLevel(), bssc.getWaterLevel(), bssc.getWasteLevel() };
			}
			whackPoller();
			return cachedPosition;
		} catch (BaseException e) {
			if (LocalProperties.get("gda.instrument").equals("ncd")) {
				//running in sim mode => no sample changer
				if (getExtraNames().length == 3) {
					return new double[] {0,0,0};
				}
				return new double[] {0,0,0,0,0};
			}
			throw new DeviceException("error getting values", e);
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
