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
import org.opengda.detector.electronanalyser.client.views.RegionCreatorView;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

public class RegionCreatorFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(RegionCreatorFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private IVGScientaAnalyserRMI analyser;


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
		logger.info("Creating region creator view");
		RegionCreatorView regionCreatorView = new RegionCreatorView();
		regionCreatorView.setViewPartName(viewPartName);
		regionCreatorView.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		if (getCamera()!=null) regionCreatorView.setCamera(camera);
		if (getAnalyser()!=null) regionCreatorView.setAnalyser(getAnalyser());
		return regionCreatorView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

}
