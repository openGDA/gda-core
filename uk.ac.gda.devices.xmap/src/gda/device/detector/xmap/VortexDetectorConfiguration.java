/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
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

package gda.device.detector.xmap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.FluorescentDetectorConfigurationBase;
import gda.factory.FactoryException;

public class VortexDetectorConfiguration extends FluorescentDetectorConfigurationBase implements InitializingBean {

	private Logger logger = LoggerFactory.getLogger(VortexDetectorConfiguration.class);
	private Xmap xmap;
	private boolean saveRawSpectrum = false;

	public VortexDetectorConfiguration(Xmap xmap) {
		this.xmap = xmap;
	}

	@Override
	public void configure(String xmlFileName) throws FactoryException {
		try {
			xmap.setConfigFileName(xmlFileName);
			xmap.stop();
			logger.info("Wrote new Vortex Parameters to: " + xmap.getConfigFileName());
			xmap.loadConfigurationFromFile();
			xmap.setSaveRawSpectrum(saveRawSpectrum);
		} catch (Exception ne) {
			logger.error("Cannot configure Vortex", ne);
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xmap == null) {
			throw new IllegalArgumentException("Missing xspresssystem component");
		}
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	public void setSaveRawSpectrum(boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}

	public Xmap getXmap() {
		return xmap;
	}

	public void setXmap(Xmap xmap) {
		this.xmap = xmap;
	}

}
