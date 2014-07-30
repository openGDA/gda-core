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

import gda.device.detector.FluorescentDetectorConfiguration;
import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Xspress3DetectorConfiguration implements FluorescentDetectorConfiguration{
	
	private Logger logger = LoggerFactory.getLogger(Xspress3DetectorConfiguration.class);
	private Xspress3Detector xspress3;
	private ObservableComponent observer;
	private String message = "Xspress configuration has not been applied yet";
	
	public Xspress3DetectorConfiguration(Xspress3Detector xmap, final ObservableComponent observer){
		this.observer = observer;
		this.xspress3 = xmap;
	}
	
	@Override
	public void configure(String xmlFileName) throws FactoryException {
		try {	
			xspress3.setConfigFileName(xmlFileName);
			xspress3.stop();
			logger.info("Wrote new Xspress3 Parameters to: "+xspress3.getConfigFileName());
			xspress3.loadConfigurationFromFile();
			message = " The Xspress detector configuration updated.";
			observer.notifyIObservers("Message", new ScriptProgressEvent(message));
		} catch (Exception ne) {
			logger.error("Cannot configure Xspress3", ne);
			message = "Cannot configure Xspress3 " + ne.getMessage();
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
		String message = " The xspress3 detector configuration was updated.";
		observer.notifyIObservers("Message", new ScriptProgressEvent(message));
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
