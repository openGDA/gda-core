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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.opengda.detector.electronanalyser.client.Camera;
import org.opengda.detector.electronanalyser.client.views.SequenceViewLive;
import org.opengda.detector.electronanalyser.lenstable.RegionValidator;
import org.opengda.detector.electronanalyser.utils.RegionDefinitionResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.views.FindableExecutableExtension;
import uk.ac.gda.devices.vgscienta.IVGScientaAnalyserRMI;

/**
 * Factory method that invokes the View object
 */
public class SequenceViewFactory implements FindableExecutableExtension {

	private final Logger logger = LoggerFactory.getLogger(SequenceViewFactory.class);
	private String viewPartName;
	private String name;
	private RegionDefinitionResourceUtil regionDefinitionResourceUtil;
	private Camera camera;
	private String location;
	private String user;
	private IVGScientaAnalyserRMI analyser;
	private String analyserStatePV;
	private String analyserTotalTimeRemainingPV;
	private RegionValidator regionValidator;
	private boolean disableSequenceEditingDuringAnalyserScan = true;
	private boolean useSequenceCache = false;

	public boolean isUseSequenceCache() {
		return useSequenceCache;
	}

	public void setUseSequenceCache(boolean useSequenceCache) {
		this.useSequenceCache = useSequenceCache;
	}

	public void setDisableSequenceEditingDuringAnalyserScan(boolean value) {
		disableSequenceEditingDuringAnalyserScan = value;
	}

	public boolean getDisableSequenceEditingDuringAnalyserScan() {
		return disableSequenceEditingDuringAnalyserScan;
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

	@Override
	public Object create() throws CoreException {
		logger.info("Creating sequence editor view");
		SequenceViewLive sequenceView = new SequenceViewLive();
		sequenceView.setViewPartName(viewPartName);
		sequenceView.setRegionDefinitionResourceUtil(regionDefinitionResourceUtil);
		sequenceView.setUseCache(isUseSequenceCache());
		sequenceView.setDisableSequenceEditingDuringAnalyserScan(getDisableSequenceEditingDuringAnalyserScan());
		if (camera != null) {
			sequenceView.setCamera(camera);
		}
		if (analyser!=null) {
			sequenceView.setAnalyser(analyser);
		}
		if (getAnalyserStatePV() != null) {
			sequenceView.setDetectorStatePV(getAnalyserStatePV());
		}
		if (getAnalyserTotalTimeRemainingPV()!=null) {
			sequenceView.setAnalyserTotalTimeRemianingPV(analyserTotalTimeRemainingPV);
		}

		if (regionValidator!=null) {
			sequenceView.setRegionValidator(regionValidator);
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

	public IVGScientaAnalyserRMI getAnalyser() {
		return analyser;
	}

	public void setAnalyser(IVGScientaAnalyserRMI analyser) {
		this.analyser = analyser;
	}

	public String getAnalyserTotalTimeRemainingPV() {
		return analyserTotalTimeRemainingPV;
	}

	public void setAnalyserTotalTimeRemainingPV(
			String analyserTotalTimeRemainingPV) {
		this.analyserTotalTimeRemainingPV = analyserTotalTimeRemainingPV;
	}

	public String getAnalyserStatePV() {
		return analyserStatePV;
	}

	public void setAnalyserStatePV(String analyserStatePV) {
		this.analyserStatePV = analyserStatePV;
	}

	public RegionValidator getRegionValidator() {
		return regionValidator;
	}

	public void setRegionValidator(RegionValidator regionValidator) {
		this.regionValidator = regionValidator;
	}
}