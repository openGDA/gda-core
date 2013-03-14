/*-
 * Copyright © 2012 Diamond Light Source Ltd., Science and Technology
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

import gda.device.detector.DetectorConfiguration;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import java.io.File;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.beans.exafs.OutputParameters;
import uk.ac.gda.beans.xspress.XspressParameters;

/**
 * Called by Jython script vortexConfig
 */
public class Xspress2DetectorConfiguration extends DetectorConfiguration {

	private static final Logger logger = LoggerFactory.getLogger(Xspress2DetectorConfiguration.class);

	private XspressParameters xspressParameters;
	private ObservableComponent controller;
//	private OutputParameters outputParameters;

	private String additionalSavePath;

	public Xspress2DetectorConfiguration(final ObservableComponent controller, final String path, final Object beanName,
			@SuppressWarnings("unused") final OutputParameters outputParams) throws Exception {

		this.controller = controller;
		this.xspressParameters = (XspressParameters) getBean(path, beanName);
//		this.outputParameters = outputParams;
		additionalSavePath =new File(path).getName();
	}
	public Xspress2DetectorConfiguration(final ObservableComponent controller, final String path, final Object beanName,
			@SuppressWarnings("unused") final OutputParameters outputParams, final String addtionalSavePath) throws Exception {

		this.controller = controller;
		this.xspressParameters = (XspressParameters) getBean(path, beanName);
//		this.outputParameters = outputParams;
		this.additionalSavePath = addtionalSavePath;
	}

	@Override
	public void configure() throws FactoryException {
		doSetup();
	}

	/**
	 * @return status
	 * @throws FactoryException
	 */
	private String doSetup() throws FactoryException {

		String message = null;
		try {
			// Warning concrete class used here. This code must be called on the server.
			final Xspress2System xspress2 = (Xspress2System) Finder.getInstance().find(
					xspressParameters.getDetectorName());

			// 1. Save bean
			File templateFile = new File(xspress2.getConfigFileName());
			saveBeanToTemplate(xspressParameters,templateFile);
			logger.info("Wrote new Xspress Parameters to: " + xspress2.getConfigFileName());

			// 2. Tell detector to configure
			xspress2.configure();
			// 3. set the ascii output options
			if (xspressParameters != null){
				xspress2.setOnlyDisplayFF(xspressParameters.isXspressOnlyShowFF());
				xspress2.setAddDTScalerValuesToAscii(xspressParameters.isXspressShowDTRawValues());
				xspress2.setSaveRawSpectrum(xspressParameters.isSaveRawSpectrum());
			}

//			if(additionalSavePath != null && !additionalSavePath.equals(templateFile.getName())){
//				saveBeanToTemplate(xspressParameters, new File(templateFile.getParent() + File.separator+additionalSavePath));
//			}
			message = " The Xspress detector configuration updated.";
			controller.notifyIObservers("Message", new ScriptProgressEvent(message));
		} catch (Exception ne) {
			logger.error("Cannot configure Xspress", ne);
			message = " !Error with Xspress detector configuration!";
			throw new FactoryException("Cannot configure Xspress", ne);
		}
		return message;
	}

	@Override
	protected Class<? extends Object> getBeanClass() {
		return XspressParameters.class;
	}

	@Override
	protected URL getMappingURL() {
		return XspressParameters.mappingURL;
	}

	@Override
	protected URL getSchemaURL() {
		return XspressParameters.schemaURL;
	}

}
