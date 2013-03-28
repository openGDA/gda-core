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

package org.opengda.detector.electronanalyser.client.viewfactories;

import gda.device.scannable.ScannableMotor;
import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.views.RegionView;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory class that creates the RegionView object to be contributed to eclipse's {@link org.eclipse.ui.views} extension point.
 */
public class RegionViewFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory
			.getLogger(RegionViewFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private ScannableMotor dcmenergy;
	private ScannableMotor pgmenergy;

	public String getViewPartName() {
		return viewPartName;
	}

	public void setRegionDefinitionResourceUtil(RegionDefinitionResourceUtil regionDefinitionResourceUtil) {
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

	@Override
	public Object create() throws CoreException {
		logger.info("Creating region editor view");
		RegionView regionView = new RegionView();
		regionView.setViewPartName(viewPartName);
		regionView.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		if (getCamera()!=null) regionView.setCamera(camera);
		if (dcmenergy!=null) regionView.setDcmEnergy(dcmenergy);
		if (pgmenergy!=null) regionView.setPgmEnergy(pgmenergy);
		
		return regionView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}


	public void setDcmEnergy(ScannableMotor energy) {
		this.dcmenergy=energy;
	}
	public ScannableMotor getDcmEnergy() {
		return this.dcmenergy;
	}
	public void setPgmEnergy(ScannableMotor energy) {
		this.pgmenergy=energy;
	}
	public ScannableMotor getPgmEnergy() {
		return this.pgmenergy;
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

}