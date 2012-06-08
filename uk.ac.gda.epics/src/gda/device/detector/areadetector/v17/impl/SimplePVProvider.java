/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

import gda.device.detector.areadetector.IPVProvider;
import gda.epics.PVProvider;

import org.springframework.beans.factory.InitializingBean;

public class SimplePVProvider implements IPVProvider, PVProvider, InitializingBean {

	String prefix;
	
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPV(String key) throws Exception {
		return prefix+key;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (prefix == null) {
			throw new IllegalArgumentException("prefix is null");
		}
	}

	@Override
	public String getPV() throws Exception {
		return prefix;
	}	
}
