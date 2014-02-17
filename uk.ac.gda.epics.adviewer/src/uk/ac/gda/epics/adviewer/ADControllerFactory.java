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

import gda.rcp.util.OSGIServiceRegister;

import java.util.Dictionary;

import uk.ac.gda.epics.adviewer.views.ADUtils;
import uk.ac.gda.util.dictionary.MapBasedDictionary;


public class ADControllerFactory {
	
	//TODO Make the following match convention
	private static final String MPG_PROC_PLUGIN_SUFFIX = "PRO1:";
	private static final String ROI_PLUGIN_SUFFIX = "ROI1";
	private static final String MPG_PLUGIN_SUFFIX = "MPG1:";
	private static final String ARRAY_PLUGIN_SUFFIX = "ARR:";
	private static final String STAT_PLUGIN_SUFFIX = "STAT:";//"STAT1:";
	private static final String ADBASE_SUFFIX = "CAM:";
	private static ADControllerFactory instance;
	
	
	public static ADControllerFactory getInstance(){
		if( instance == null){
			instance = new ADControllerFactory();
		}
		return instance;
	}
	public void registerADController(String serviceName) throws Exception{
		
		String detectorName = ADUtils.getDetectorNameFromPVServiceName(serviceName);
		DynamicADControllerImpl impl = new DynamicADControllerImpl(serviceName, detectorName, ADUtils.getPVFromPVServiceName(serviceName), ADBASE_SUFFIX, STAT_PLUGIN_SUFFIX, ARRAY_PLUGIN_SUFFIX,
				MPG_PLUGIN_SUFFIX, ROI_PLUGIN_SUFFIX, MPG_PROC_PLUGIN_SUFFIX);
		
		OSGIServiceRegister modelReg = new OSGIServiceRegister();
		modelReg.setClass(ADController.class);
		modelReg.setService(impl);
		@SuppressWarnings("unchecked")
		Dictionary<String, String> properties = new MapBasedDictionary();
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
				throw new Exception("Unable to access ADController for service '" + serviceName + "'");
		}
		return config;
	}	
}
