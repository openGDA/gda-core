/*-
 * Copyright © 2014 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

import gda.device.detector.FluorescentDetectorConfigurationBase;
import gda.factory.FactoryException;

/**
 * Utility class to configure an Xspress3 detector using the given XML file. It
 * notifies observers of progress e.g. an Xspress3 editor.
 *
 * @author rjw82
 *
 */
public class Xspress3DetectorConfiguration extends FluorescentDetectorConfigurationBase {

	private Logger logger = LoggerFactory.getLogger(Xspress3DetectorConfiguration.class);
	private Xspress3Detector xspress3;

	public Xspress3DetectorConfiguration(Xspress3Detector xspress) {
		this.xspress3 = xspress;
	}

	@Override
	public void configure(String xmlFileName) throws FactoryException {
		try {
			xspress3.setConfigFileName(xmlFileName);
			xspress3.stop();
			logger.info("Wrote new Xspress3 Parameters to: " + xspress3.getConfigFileName());
			xspress3.loadConfigurationFromFile();
		} catch (Exception ne) {
			logger.error("Cannot configure Xspress3", ne);
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
	}
}
