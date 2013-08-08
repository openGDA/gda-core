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

import gda.device.detector.DetectorConfiguration;
import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;
import java.io.File;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.gda.beans.xspress.XspressParameters;

public class Xspress2DetectorConfiguration extends DetectorConfiguration {

	private Logger logger = LoggerFactory.getLogger(Xspress2DetectorConfiguration.class);
	private Xspress2System xspress2System;
	private XspressParameters xspressParameters;
	private ObservableComponent observer;
	private boolean onlyShowFF;
	private boolean showDTRawValues;
	private boolean saveRawSpectrum;
	
	public Xspress2DetectorConfiguration(Xspress2System xspress2System, final ObservableComponent observer, final String path, final Object beanName,
			boolean onlyShowFF, boolean showDTRawValues, boolean saveRawSpectrum) throws Exception {
		this.observer = observer;
		this.xspressParameters = (XspressParameters) getBean(path, beanName);
		this.onlyShowFF = onlyShowFF;
		this.showDTRawValues = showDTRawValues;
		this.saveRawSpectrum = saveRawSpectrum;
		this.xspress2System = xspress2System;
	}
	
	@Override
	public void configure() throws FactoryException {
		String message = null;
		try {
			// save bean
			File templateFile = new File(xspress2System.getConfigFileName());
			saveBeanToTemplate(xspressParameters,templateFile);
			logger.info("Wrote new Xspress Parameters to: " + xspress2System.getConfigFileName());

			// tell detector to configure
			xspress2System.configure();
			
			// set the ascii output options
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
