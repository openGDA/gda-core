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

package gda.device.detector.addetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ADPco extends ADDetector {
	
	private static Logger logger = LoggerFactory.getLogger(ADPco.class);

	public ADPco() {
		setLocal(true);
	}
	
	public void initialiseFileWriterPluginImageSizeByTakingExposure() throws Exception {
		logger.info("Epics kludge: Exposing a single image to initialise image size in file writing plugin");
		
		getFileWriter().setEnable(false);
		getFileWriter().enableCallback(true);
		getCollectionStrategy().prepareForCollection(.01, 1);
		collectData();
		waitWhileBusy();
		endCollection();
		getFileWriter().enableCallback(false);
		getFileWriter().setEnable(true);
		logger.info("Epics kludge complete");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		getFileWriter().setEnable(true);
	}
	
	

}
