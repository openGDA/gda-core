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

package uk.ac.gda.devices.detector.xspress3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.DeviceException;
import uk.ac.gda.api.remoting.ServiceInterface;
import uk.ac.gda.devices.detector.FluorescenceDetector;

@ServiceInterface(FluorescenceDetector.class)
public class Xspress3MiniDetector extends Xspress3Detector implements Xspress3Mini {

	private static final Logger logger = LoggerFactory.getLogger(Xspress3MiniDetector.class);

	private int waitForBusyTimeout = LocalProperties.getAsInt("gda.xsp3m.fluorescence.waitForBusyTimeout", 5);

	@Override
	public void collectData() throws DeviceException {
		logger.info("collecting data from Xspress3Mini Fluorescence Detector");
		Xspress3MiniController miniController = (Xspress3MiniController)controller;

		long collectionTimeout = (Math.round(miniController.getAcquireTime()) + waitForBusyTimeout) * 1000;

		controller.setTriggerMode(TRIGGER_MODE.Burst);
		controller.doErase();
		controller.doStart();
		((Xspress3MiniController)controller).waitForDetector(true, waitForBusyTimeout);
		((Xspress3MiniController)controller).waitForDetector(false, collectionTimeout);
	}

	@Override
	public void setCollectionTime(double collectionTime) throws DeviceException {
		((Xspress3MiniController)controller).setAcquireTime(collectionTime);
	}
}
