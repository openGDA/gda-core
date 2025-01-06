/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package org.opengda.detector.electronanalyser.client.viewfactories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.views.SequenceViewCreator;
import org.opengda.detector.electronanalyser.lenstable.IRegionValidator;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;

public class SequenceViewCreatorFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(SequenceViewCreatorFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private IRegionValidator regionValidator;
	private boolean useSequenceCache = false;

	@Override
	public Object create() throws CoreException {
		logger.info("Creating {} view", getViewPartName());
		SequenceViewCreator sequenceViewCreator = createView();
		sequenceViewCreator.setViewPartName(viewPartName);
		sequenceViewCreator.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		if (camera != null) sequenceViewCreator.setCamera(camera);
		if (regionValidator!=null) sequenceViewCreator.setRegionValidator(regionValidator);
		sequenceViewCreator.setUseCache(isUseSequenceCache());
		return sequenceViewCreator;
	}

	protected SequenceViewCreator createView() {
		return new SequenceViewCreator();
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	public boolean isUseSequenceCache() {
		return useSequenceCache;
	}

	public void setUseSequenceCache(boolean useSequenceCache) {
		this.useSequenceCache = useSequenceCache;
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setRegionDefinitionResourceUtil(
			RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
		this.regionDefinitionResourceUtil = regionDefinitionResourceUtil;
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

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public IRegionValidator getRegionValidator() {
		return regionValidator;
	}

	public void setRegionValidator(IRegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}
}