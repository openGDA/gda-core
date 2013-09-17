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

import uk.ac.gda.devices.bssc.BioSAXSSampleChanger;
import gda.device.DeviceException;
import gda.device.scannable.ScannableBase;
import gda.factory.FactoryException;

public class BSSCScannable extends ScannableBase {
	private static final Logger logger = LoggerFactory.getLogger(BSSCScannable.class);

	BioSAXSSampleChanger bssc;
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		try {
			setInputNames(new String[] {});
			setExtraNames(new String [] {"detergentlevel", "waterlevel", "wastelevel"});
			setOutputFormat(new String [] {"%2.0f", "%2.0f", "%2.0f"});
			bssc.getTemperatureSampleStorage();
			setExtraNames(new String [] { "seutemp", "storagetemp", "detergentlevel", "waterlevel", "wastelevel"});
			setOutputFormat(new String [] {"%3.1f", "%3.1f", "%2.0f", "%2.0f", "%2.0f"});
		} catch (Exception ignored) {
			
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
			if (getExtraNames().length == 3)
					return new double[] {bssc.getDetergentLevel(), bssc.getWaterLevel(), bssc.getWasteLevel()};
			return new double[] {bssc.getTemperatureSEU(), bssc.getTemperatureSampleStorage(), bssc.getDetergentLevel(), bssc.getWaterLevel(), bssc.getWasteLevel()};
		} catch (BaseException e) {
			throw new DeviceException("error getting values", e);
		}
	}

	public BioSAXSSampleChanger getBssc() {
		return bssc;
	}

	public void setBssc(BioSAXSSampleChanger bssc) {
		this.bssc = bssc;
	}
}