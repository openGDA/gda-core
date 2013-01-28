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

package org.opengda.detector.electroanalyser.client.regioneditor;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electroanalyser.api.RegionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory method that invokes the View object
 */
public class RegionViewFactoryFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory
			.getLogger(RegionViewFactoryFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinition regionDefinition;

	public String getViewPartName() {
		return viewPartName;
	}

	public void setRegionDefinition(RegionDefinition regionDefinition) {
		this.regionDefinition = regionDefinition;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("Creating tomoalignment view");
		RegionView regionView = new RegionView();
		regionView.setViewPartName(viewPartName);
		regionView.setRegionDefinition(regionDefinition);
		return regionView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}
}