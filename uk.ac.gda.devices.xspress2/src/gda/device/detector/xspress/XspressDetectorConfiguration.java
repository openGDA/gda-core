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

import gda.device.detector.FluorescentDetectorConfigurationBase;
import gda.factory.FactoryException;
import gda.jython.scriptcontroller.event.ScriptProgressEvent;
import gda.observable.ObservableComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

public class XspressDetectorConfiguration extends FluorescentDetectorConfigurationBase implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(XspressDetectorConfiguration.class);
	private XspressSystem xspressSystem;
	private ObservableComponent observer;
	private String message = "Xspress configuration has not been applied yet";
	private boolean onlyShowFF = false;
	private boolean showDTRawValues = false;
	private boolean saveRawSpectrum = false;
	private String name;

	public XspressDetectorConfiguration() {
	}

	public XspressDetectorConfiguration(XspressSystem xspressSystem, final ObservableComponent observer) {
		this.observer = observer;
		this.xspressSystem = xspressSystem;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (xspressSystem == null) {
			throw new IllegalArgumentException("Missing xspresssystem component");
		}
		if (observer == null) {
			throw new IllegalArgumentException("Missing observer component");
		}
	}

	public XspressSystem getXspressSystem() {
		return xspressSystem;
	}

	public void setXspressSystem(XspressSystem xspressSystem) {
		this.xspressSystem = xspressSystem;
	}

	public ObservableComponent getObserver() {
		return observer;
	}

	public void setObserver(ObservableComponent observer) {
		this.observer = observer;
	}

	public void configure(String xmlFileName) throws FactoryException {
		try {
			xspressSystem.setConfigFileName(xmlFileName);
			xspressSystem.configure();
			xspressSystem.setOnlyDisplayFF(onlyShowFF);
			xspressSystem.setAddDTScalerValuesToAscii(showDTRawValues);
			xspressSystem.setSaveRawSpectrum(saveRawSpectrum);
			message = " The Xspress detector configuration updated.";
			observer.notifyIObservers("Message", new ScriptProgressEvent(message));
		} catch (Exception ne) {
			logger.error("Cannot configure Xspress", ne);
			message = "Cannot configure Xspress " + ne.getMessage();
			throw new FactoryException("Error during configuration:" + ne.getMessage());
		}
	}

	public void configure(String xmlFileName, boolean onlyShowFF, boolean showDTRawValues, boolean saveRawSpectrum) throws FactoryException {
		this.onlyShowFF = onlyShowFF;
		this.showDTRawValues = showDTRawValues;
		this.saveRawSpectrum = saveRawSpectrum;
		configure(xmlFileName);

	}

	public String getMessage() {
		return message;
	}

	public boolean isOnlyShowFF() {
		return onlyShowFF;
	}

	public void setOnlyShowFF(boolean onlyShowFF) {
		this.onlyShowFF = onlyShowFF;
	}

	public boolean isShowDTRawValues() {
		return showDTRawValues;
	}

	public void setShowDTRawValues(boolean showDTRawValues) {
		this.showDTRawValues = showDTRawValues;
	}

	public boolean isSaveRawSpectrum() {
		return saveRawSpectrum;
	}

	public void setSaveRawSpectrum(boolean saveRawSpectrum) {
		this.saveRawSpectrum = saveRawSpectrum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void configure () {
		placeInJythonNamespace(name, this);
	}
}
