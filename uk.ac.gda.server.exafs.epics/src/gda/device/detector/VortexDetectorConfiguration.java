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

package gda.device.detector;

import gda.device.detector.xmap.Xmap;
import gda.device.detector.xspress.Xspress2DetectorConfiguration;
import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.vortex.VortexParameters;
import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class VortexDetectorConfiguration{
	
	private Logger logger = LoggerFactory.getLogger(Xspress2DetectorConfiguration.class);
	private Xmap xmap;
	private ObservableComponent observer;
	private String message = "Xspress configuration has not been applied yet";
	
	public VortexDetectorConfiguration(Xmap xmap, final ObservableComponent observer){
		this.xmap = xmap;
		this.observer = observer;
	}
	
	public VortexParameters createBeanFromXML(String xmlPath) throws Exception{
		return (VortexParameters) XMLHelpers.createFromXML(VortexParameters.mappingURL, VortexParameters.class, VortexParameters.schemaURL, new File(xmlPath));
	}
	
	public void createXMLfromBean(VortexParameters vortexBean) throws Exception{
		File file = new File(xmap.getConfigFileName());
		XMLHelpers.writeToXML(XspressParameters.mappingURL, vortexBean, file);
	}
	
	public void configure(String xmlFileName, boolean isVortexSaveRawSpectrum) throws FactoryException {
		try {	
			xmap.setConfigFileName(xmlFileName);
			xmap.stop();
			logger.info("Wrote new Vortex Parameters to: "+xmap.getConfigFileName());
			xmap.loadConfigurationFromFile();
			xmap.setSaveRawSpectrum(isVortexSaveRawSpectrum);
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
	
	public String getMessage() {
		return message;
	}
}
