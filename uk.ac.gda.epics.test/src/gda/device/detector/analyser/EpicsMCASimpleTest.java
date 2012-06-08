/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.analyser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import gda.device.DeviceException;
import gda.device.epicsdevice.EpicsDevice;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.factory.FactoryException;

import java.util.HashMap;
import java.util.Vector;

import org.junit.Test;

/**
 * 
 */
public class EpicsMCASimpleTest {

	/**
	 * Test method for {@link gda.device.detector.analyser.EpicsMCASimple#configure()}.
	 * @throws FactoryException 
	 * @throws DeviceException 
	 */
	@Test
	public void testConfigure() throws FactoryException, DeviceException {
		HashMap<String, String> recordPVs = new HashMap<String, String>();
		recordPVs.put("RECORD", "BL0XI-DI-DET-01:aim_adc1");
		EpicsDevice mca = new EpicsDevice("mca_ed", recordPVs, true);
		mca.configure();
		FindableEpicsDevice epicsDevice = new FindableEpicsDevice("test", mca);
		EpicsMCASimple dev = new EpicsMCASimple();
		dev.setEpicsDevice(epicsDevice);
		dev.configure();
		@SuppressWarnings("unused")
		Object obj = dev.getCalibrationParameters();
		obj = dev.getData();
		obj = dev.getElapsedParameters();
		obj = dev.getRegionsOfInterest();
		obj = dev.getRegionsOfInterestCount();
		dev.dispose();
		obj = dev.getRegionsOfInterest();

		double dwellTime1 = dev.getDwellTime();
		dev.setDwellTime(1.0);
		// double dwellTime2 = dev.getDwellTime();
		// The dwell time appears to be changed automatically to 0
		// assertEquals(dwellTime2, 1.0);
		dev.setDwellTime(dwellTime1);

		long channels1 = dev.getNumberOfChannels();
		try {
			dev.setNumberOfChannels(channels1 / 2);
			long channels2 = dev.getNumberOfChannels();
			assertEquals(channels1 / 2, channels2);
		} finally {
			dev.setNumberOfChannels(channels1);
		}

		EpicsMCAPresets presets1 = (EpicsMCAPresets) dev.getPresets();
		try {
			EpicsMCAPresets presets2 = new EpicsMCAPresets((float) 1.0, (float) 2.0, 1, 2, 3, 4);
			dev.setPresets(presets2);
			EpicsMCAPresets presets3 = (EpicsMCAPresets) dev.getPresets();
			assertEquals(presets2, presets3);
		} finally {
			dev.setPresets(presets1);
		}

		EpicsMCARegionOfInterest[] regionsOfInterest1 = (EpicsMCARegionOfInterest[]) dev.getRegionsOfInterest();
		try {
			Vector<EpicsMCARegionOfInterest> _regionsOfInterest2 = new Vector<EpicsMCARegionOfInterest>();
			for (Integer i = 0; i < dev.getNumberOfRegions(); i++) {
				_regionsOfInterest2.add(new EpicsMCARegionOfInterest(i, i, i * 2, i / 2, i, i.toString()));
			}
			EpicsMCARegionOfInterest[] regionsOfInterest2 = _regionsOfInterest2
					.toArray(new EpicsMCARegionOfInterest[0]);
			dev.setRegionsOfInterest(regionsOfInterest2);
			EpicsMCARegionOfInterest[] regionsOfInterest3 = (EpicsMCARegionOfInterest[]) dev.getRegionsOfInterest();
			assertArrayEquals(regionsOfInterest2, regionsOfInterest3);
		} finally {
			dev.setRegionsOfInterest(regionsOfInterest1);
		}

		int[] data = (int[]) dev.getData();
		try {
			int len = data.length;
			int[] data1 = new int[len];
			for (int i = 0; i < len; i++) {
				data1[i] = i;
			}
			dev.setData(data1);
			int[] data2 = (int[]) dev.getData();
			assertArrayEquals(data1, data2);
		} finally {
			dev.setData(data);
		}

	}

}
