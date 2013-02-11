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

package org.opengda.detector.electronanalyser.client.regioneditor;

import gda.device.scannable.ScannableMotor;
import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.model.regiondefinition.util.RegionDefinitionResourceUtil;
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
	private int framerate;
	private double energyresolution;
	private int cameraXSize;
	private int cameraYSize;
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
		if (!(getCameraEnergyResolution()==0)) regionView.setCameraEnergyResolution(energyresolution);
		if (!(getCameraFrameRate()==0)) regionView.setCameraFrameRate(framerate);
		if (!(getCameraXSize()==0)) regionView.setCameraXSize(cameraXSize);
		if (!(getCameraYSize()==0)) regionView.setCameraYSize(cameraYSize);
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
	public void setCameraFrameRate(int rate) {
		if (rate < 1) {
			throw new IllegalArgumentException(
					"Camera frame rate must be great than and equal to 1.");
		}
		this.framerate = rate;
	}

	public int getCameraFrameRate() {
		return this.framerate;
	}

	public void setCameraEnergyResolution(double resolution) {
		this.energyresolution = resolution;
	}

	public double getCameraEnergyResolution() {
		return this.energyresolution;
	}
	public int getCameraXSize() {
		return cameraXSize;
	}

	public void setCameraXSize(int detecterXSize) {
		this.cameraXSize = detecterXSize;
	}

	public int getCameraYSize() {
		return cameraYSize;
	}

	public void setCameraYSize(int detecterYSize) {
		this.cameraYSize = detecterYSize;
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

}