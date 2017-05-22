/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.bpm;

import gda.device.BPM;
import gda.device.DeviceException;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.ObjectServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class TestQBPM.
 */
public class TestQBPM {
	private static final Logger logger = LoggerFactory.getLogger(TestQBPM.class);

	/**
	 * The main method.
	 *
	 * @param args
	 *            the args
	 * @throws FactoryException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws FactoryException, InterruptedException {
		double x, y;
		ObjectServer.createLocalImpl("/home/shk/gda/config/xml/bpm.xml");
		BPM bpm = (BPM) Finder.getInstance().find("bpm");
		for (int i = 0; i < 1000; i++) {
			try {
				x = bpm.getX();
				y = bpm.getY();
				logger.debug("BPM X = " + x + " Y = " + y);
			} catch (DeviceException e) {
				e.printStackTrace();
			}
			Thread.sleep(1000);
		}
		System.exit(0);
	}
}
