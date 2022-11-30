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

package gda.device.detector.areadetector.v17.impl;

import gda.device.detector.areadetector.v17.GetPluginBaseAvailable;
import gda.device.detector.areadetector.v17.NDPluginBase;

public abstract class NDBaseImpl implements GetPluginBaseAvailable {

	private NDPluginBase pluginBase;

	@Override
	final public NDPluginBase getPluginBase() {
		return pluginBase;
	}

	/**
	 * @param pluginBase
	 *            The pluginBase to set.
	 */
	final public void setPluginBase(NDPluginBase pluginBase) {
		this.pluginBase = pluginBase;
	}

	@Override
	final public String getPortName_RBV() throws Exception {
		return pluginBase.getPortName_RBV();
	}

}
