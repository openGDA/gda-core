/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.adviewer;

import java.util.Dictionary;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import gda.util.osgi.OSGiServiceRegister;
import uk.ac.gda.epics.adviewer.views.ADUtils;

public class ADControllerFactory {
	private static final Logger logger = LoggerFactory.getLogger(ADControllerFactory.class);

	private static ADControllerFactory instance = new ADControllerFactory();

	public static ADControllerFactory getInstance(){
		// this class has no state, so there's no advantage to lazy initialization
		return instance;
	}

	public void registerADController(String serviceName) throws Exception{

		String detectorName = ADUtils.getDetectorNameFromPVServiceName(serviceName);
		String suffixType = ADUtils.getSuffixTypeFromPVServiceName(serviceName);
		//from pv prefix get plugin suffixes
		ADPVSuffixes adPVSuffixes=null;
		if( StringUtils.hasText(suffixType)){
			adPVSuffixes = (ADPVSuffixes)Activator.getNamedService(ADPVSuffixes.class, suffixType);
		}
		if (adPVSuffixes == null) {
			logger.info("ADControllerFactory.registerADController() No PV suffix service ''{}'' found for detector ''{}'', assuming DLSADPVSuffixes for ''{}''",
					suffixType, detectorName, serviceName);
			adPVSuffixes = new DLSADPVSuffixes();
		}
		DynamicADControllerImpl impl = new DynamicADControllerImpl(serviceName, detectorName, ADUtils.getPVFromPVServiceName(serviceName), adPVSuffixes);

		OSGiServiceRegister modelReg = new OSGiServiceRegister();
		modelReg.setClass(ADController.class);
		modelReg.setService(impl);
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(Activator.SERVICE_NAME, serviceName);
		modelReg.setProperties(properties);
		modelReg.afterPropertiesSet();
	}

	public ADController getADController(String serviceName) throws Exception {
		ADController config = (ADController)Activator.getNamedService(ADController.class, serviceName);
		if( config == null){
			ADControllerFactory.getInstance().registerADController(serviceName);
			config = (ADController)Activator.getNamedService(ADController.class, serviceName);
			if (config == null)
				throw new RuntimeException("Unable to access ADController for service '" + serviceName + "'");
		}
		return config;
	}
}
