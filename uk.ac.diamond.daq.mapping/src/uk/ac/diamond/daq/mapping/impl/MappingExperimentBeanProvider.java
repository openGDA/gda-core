/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.impl;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.osgi.OsgiService;

/**
 * An object that wraps a mapping bean. This object is configured in spring.
 * An instance of this class should be injected rather than the mapping bean directly so
 * that you get always get a reference to the current mapping bean in the mapping view
 * even after another bean has been loaded from the filesystem.
 */
@OsgiService(IMappingExperimentBeanProvider.class)
public class MappingExperimentBeanProvider implements IMappingExperimentBeanProvider {

	private IMappingExperimentBean mappingExperimentBean;
	private boolean setByView = false;

	@Override
	public IMappingExperimentBean getMappingExperimentBean() {
		return mappingExperimentBean;
	}

	@Override
	public void setMappingExperimentBean(IMappingExperimentBean bean) {
		this.mappingExperimentBean = bean;
	}

	@Override
	public boolean isSetByView() {
		return setByView;
	}

	@Override
	public void setSetByView(boolean setByView) {
		this.setByView = setByView;
	}
}
