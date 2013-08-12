/*-
 * Copyright © 2013 Diamond Light Source Ltd., Science and Technology
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

package gda.device.detector.xspress;

import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.xspress.XspressParameters;
import uk.ac.gda.util.beans.xml.XMLHelpers;

public class Xspress2DetectorConfiguration{

	private Logger logger = LoggerFactory.getLogger(Xspress2DetectorConfiguration.class);
	private Xspress2System xspress2System;
	private ObservableComponent observer;
	
	public Xspress2DetectorConfiguration(Xspress2System xspress2System, final ObservableComponent observer) {
		this.observer = observer;
		this.xspress2System = xspress2System;
	}
	
	public XspressParameters createBeanFromXML(String xmlPath){
		try {
			return (XspressParameters) XMLHelpers.createFromXML(XspressParameters.mappingURL, XspressParameters.class, XspressParameters.schemaURL, new File(xmlPath));
		} catch (Exception e) {
			logger.error("Could not create XspressParameters bean from file "+xmlPath, e);
		}
		return null;
	}
	
	public void createXMLfromBean(XspressParameters xspressBean){
		
		File file = new File(xspress2System.getConfigFileName());
		try {
			XMLHelpers.writeToXML(XspressParameters.mappingURL, xspressBean, file);
			logger.info("Wrote new Xspress Parameters to: " + xspress2System.getConfigFileName());
		} catch (Exception e) {
			logger.error("Could not save XspressParameters bean to "+file, e);
		}
	}
	
	public void configure(String xmlFileName, boolean onlyShowFF, boolean showDTRawValues, boolean saveRawSpectrum) throws FactoryException {
		String message = null;
		try {
			xspress2System.setConfigFileName(xmlFileName);
			xspress2System.configure();
			xspress2System.setOnlyDisplayFF(onlyShowFF);
			xspress2System.setAddDTScalerValuesToAscii(showDTRawValues);
			xspress2System.setSaveRawSpectrum(saveRawSpectrum);
			message = " The Xspress detector configuration updated.";
			observer.notifyIObservers("Message", new ScriptProgressEvent(message));
		} catch (Exception ne) {
			logger.error("Cannot configure Xspress", ne);
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
	}
}