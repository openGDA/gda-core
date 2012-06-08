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

package gda.device.detector.nexusprocessor;

import gda.factory.Findable;

import java.util.Collection;

public abstract class DataSetProcessorBase extends Object implements DataSetProcessor, Findable {

	private String name; 
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	boolean enable=true;
	
	@Override
	public boolean isEnabled() {
		return enable;
	}

	@Override
	public void setEnable(boolean enable) {
		this.enable = enable;
	}

	protected abstract Collection<String> _getExtraNames();
	protected abstract Collection<String> _getOutputFormat();

	@Override
	public Collection<String> getExtraNames() {
		return enable ? _getExtraNames() : null;
	}

	@Override
	public Collection<String> getOutputFormat() {
		return enable ?  _getOutputFormat() : null;
	}

	
}
