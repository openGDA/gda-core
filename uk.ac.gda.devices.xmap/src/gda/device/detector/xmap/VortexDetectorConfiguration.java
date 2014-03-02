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

import gda.device.detector.FluorescentDetectorConfiguration;
import gda.device.detector.xspress.Xspress2DetectorConfiguration;
import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VortexDetectorConfiguration implements FluorescentDetectorConfiguration{
	
	private Logger logger = LoggerFactory.getLogger(Xspress2DetectorConfiguration.class);
	private Xmap xmap;
	private ObservableComponent observer;
	private String message = "Xspress configuration has not been applied yet";
	private boolean saveRawSpectrum = false;
	
	public VortexDetectorConfiguration(Xmap xmap, final ObservableComponent observer){
		this.observer = observer;
		this.xmap = xmap;
	}
	
	@Override
	public void configure(String xmlFileName) throws FactoryException {
		try {	
			xmap.setConfigFileName(xmlFileName);
			xmap.stop();
			logger.info("Wrote new Vortex Parameters to: "+xmap.getConfigFileName());
			xmap.loadConfigurationFromFile();
			xmap.setSaveRawSpectrum(saveRawSpectrum);
			message = " The Xspress detector configuration updated.";
			observer.notifyIObservers("Message", new ScriptProgressEvent(message));
		} catch (Exception ne) {
			logger.error("Cannot configure Vortex", ne);
			message = "Cannot configure Xspress " + ne.getMessage();
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
		String message = " The vortex detector configuration updated.";
		observer.notifyIObservers("Message", new ScriptProgressEvent(message));
	}
	
	@Override
	public String getMessage() {
		return message;
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	public void setsaveRawSpectrum(boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}

}
