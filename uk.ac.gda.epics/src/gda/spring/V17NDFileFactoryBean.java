/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package gda.spring;

import uk.ac.gda.util.FilePathConverter;
import gda.device.detector.areadetector.IPVProvider;
import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.impl.NDFileImpl;
/**
 * FactoryBean to make the creation of an bean that implements NDFile easier
 */
public class V17NDFileFactoryBean extends V17PluginFactoryBeanBase<NDFile>{

	private boolean resetToInitialValues;
	private FilePathConverter filePathConverter;

	public boolean isResetToInitialValues() {
		return resetToInitialValues;
	}

	public void setResetToInitialValues(boolean resetToInitialValues) {
		this.resetToInitialValues = resetToInitialValues;
	}

	public FilePathConverter getFilePathConverter() {
		return filePathConverter;
	}

	public void setFilePathConverter(FilePathConverter filePathConverter) {
		this.filePathConverter = filePathConverter;
	}

	@Override
	protected NDFile createObject(NDPluginBase pluginBase, IPVProvider pvProvider) throws Exception {
		NDFileImpl plugin = new NDFileImpl();
		plugin.setPluginBase(pluginBase);
		plugin.setPvProvider(pvProvider);
		plugin.setResetToInitialValues(resetToInitialValues);
		plugin.setFilePathConverter(filePathConverter);
		plugin.afterPropertiesSet();
		return plugin;
	}

}
