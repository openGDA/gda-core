/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.detector.xspress3;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.nexus.extractor.NexusGroupData;
import gda.data.nexus.tree.NexusTreeProvider;
import gda.device.DeviceException;
import gda.device.detector.NXDetectorData;
import gda.device.detector.NexusDetector;
import gda.factory.FactoryException;

public class DummyXspress3MiniDetector extends DummyXspress3Detector implements NexusDetector{

	private static final Logger logger = LoggerFactory.getLogger(DummyXspress3MiniDetector.class);

	@Override
	public String[] getExtraNames() {
		return extraNames;
	}

	@Override
	public void configure() throws FactoryException {
		String[] extraNames = new String[getExtraNames().length+1];
		setOutputFormat(Arrays.stream(extraNames).map(i->"%.3f").toArray(String[]::new));
		super.configure();
	}

	@Override
	public NexusTreeProvider readout() throws DeviceException {
		NXDetectorData nxData = new NXDetectorData(this);
		double[] data = new double[getExtraNames().length];
		for (int i=0; i<data.length;i++) {
			data[i]=100.0*Math.random();
			nxData.addData(getName(), extraNames[i], new NexusGroupData(data[i]));
			nxData.setPlottableValue(extraNames[i], data[i]);
		}
		return nxData;
	}
}
