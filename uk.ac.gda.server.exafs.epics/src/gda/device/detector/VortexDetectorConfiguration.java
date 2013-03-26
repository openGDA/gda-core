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
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.vortex.VortexParameters;


/**
 * Called by Jython script vortexConfig
 */
public class VortexDetectorConfiguration extends DetectorConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(VortexDetectorConfiguration.class);
	
	private VortexParameters    vortexParameters;
	private ObservableComponent controller;

	private String additionalSavePath;

	public VortexDetectorConfiguration(final Object controller, 
			                           final String path,
			                           final Object vortexName) throws Exception{
		this.controller       = (ObservableComponent)controller;
		this.vortexParameters = (VortexParameters)getBean(path,vortexName);
	}

	public VortexDetectorConfiguration(final Object controller, 
			                           final String path,
			                           final Object vortexName, @SuppressWarnings("unused") final OutputParameters output) throws Exception{
		this.controller       = (ObservableComponent)controller;
		this.vortexParameters = (VortexParameters)getBean(path,vortexName);
	}
	
	public VortexDetectorConfiguration(final Object controller, 
			                           final String path,
 final Object vortexName,
			@SuppressWarnings("unused") final OutputParameters output, final String additionalSavePath) throws Exception{
		this.controller       = (ObservableComponent)controller;
		this.vortexParameters = (VortexParameters)getBean(path,vortexName);
		this.additionalSavePath = additionalSavePath;
	}
	
	@Override
	public void configure() throws FactoryException {
		doSetup();
	}
	

	private String doSetup() throws FactoryException {
		
		try {
			// Warning concrete class used here. This code must be called on the server.
			final Xmap xmapDetector = (Xmap)Finder.getInstance().find(vortexParameters.getDetectorName());

			// 1. Save parameters file.
			File templateFile = new File(xmapDetector.getConfigFileName());
			saveBeanToTemplate(vortexParameters, templateFile);
			logger.info("Wrote new Vortex Parameters to: "+xmapDetector.getConfigFileName());
			
			// 2. Set windows.
			xmapDetector.loadConfigurationFromFile();
			if (vortexParameters != null){
				xmapDetector.setSaveRawSpectrum(vortexParameters.isSaveRawSpectrum());
			}
			if(additionalSavePath != null)
				saveBeanToTemplate(vortexParameters, new File(templateFile.getParent() + File.separator +additionalSavePath));
		} catch (Exception ne) {
			logger.error("Cannot configure Vortex", ne);
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
		String message = " The vortex detector configuration updated.";
		controller.notifyIObservers("Message", new ScriptProgressEvent(message));
		return message;
	}
	
	@Override
	protected Class<? extends Object> getBeanClass() {
		return VortexParameters.class;
	}

	@Override
	protected URL getMappingURL() {
		return VortexParameters.mappingURL;
	}

	@Override
	protected URL getSchemaURL() {
		return VortexParameters.schemaURL;
	}


}
