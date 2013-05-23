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

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.views.SequenceView;
import org.opengda.detector.electronanalyser.server.IVGScientaAnalyser;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory method that invokes the View object
 */
public class SequenceViewFactory implements FindableExecutableExtension {

	private final Logger logger = LoggerFactory
			.getLogger(SequenceViewFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private String location;
	private String user;
	private IVGScientaAnalyser analyser;
	private String detectorStatePV;

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

	@Override
	public Object create() throws CoreException {
		logger.info("Creating sequence editor view");
		SequenceView sequenceView = new SequenceView();
		sequenceView.setViewPartName(viewPartName);
		sequenceView
				.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		if (camera != null) {
			sequenceView.setCamera(camera);
		}
		if (location != null) {
			sequenceView.setLocation(location);
		}
		if (user != null) {
			sequenceView.setUser(user);
		}
		if (analyser!=null) {
			sequenceView.setAnalyser(analyser);
		}
		if (getDetectorStatePV() != null) {
			sequenceView.setDetectorStatePV(getDetectorStatePV());
		}
		return sequenceView;
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

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public IVGScientaAnalyser getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyser analyser) {
		this.analyser = analyser;
	}

	public String getDetectorStatePV() {
		return detectorStatePV;
	}

	public void setDetectorStatePV(String detectorStatePV) {
		this.detectorStatePV = detectorStatePV;
	}
}