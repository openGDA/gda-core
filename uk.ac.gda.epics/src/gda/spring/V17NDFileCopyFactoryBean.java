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

package gda.spring;

import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.device.detector.areadetector.v17.impl.NDFileCopy;

public class V17NDFileCopyFactoryBean extends V17NDFileFactoryBean {
	private String copyPluginPrefix="BL13I-EA-DET-01:COPY";

	public String getCopyPluginPrefix() {
		return copyPluginPrefix;
	}

	public void setCopyPluginPrefix(String copyPluginPrefix) {
		this.copyPluginPrefix = copyPluginPrefix;
	}
	@Override
	protected NDFile createObject(NDPluginBase pluginBase, String basePv) throws Exception {
		NDFileCopy plugin = new NDFileCopy();
		plugin.setCopyPluginPrefix(copyPluginPrefix);
		plugin.setPluginBase(pluginBase);
		plugin.setBasePVName(basePv);
		plugin.setResetToInitialValues(resetToInitialValues);
		plugin.setFilePathConverter(filePathConverter);
		plugin.afterPropertiesSet();
		return plugin;

	}
}
