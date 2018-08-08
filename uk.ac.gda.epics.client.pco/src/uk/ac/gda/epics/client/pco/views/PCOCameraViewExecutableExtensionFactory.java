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

package uk.ac.gda.epics.client.pco.views;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.client.views.CameraPreviewView;
import uk.ac.gda.epics.client.views.CameraViewController;

/**
 *
 */
public class PCOCameraViewExecutableExtensionFactory implements FindableExecutableExtension {

	private final Logger logger = LoggerFactory.getLogger(PCOCameraViewExecutableExtensionFactory.class);
	private String viewPartName;
	private final String PCO_SUBSAMPLE_PLOT_VIEW_ID = "uk.ac.gda.beamline.client.pcosubsampleplot";
	private CameraViewController cameraViewController;

	private String name;

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("creating the PCO view");
		CameraPreviewView cameraPreviewView = new CameraPreviewView();
		cameraPreviewView.setCameraViewController(cameraViewController);
		cameraPreviewView.setViewPartName(viewPartName);
		cameraPreviewView.setSubsamplePlotViewName("PCO Subsample");
		cameraPreviewView.setSubsamplePlotViewId(PCO_SUBSAMPLE_PLOT_VIEW_ID);
		logger.info("PCO view created.");
		return cameraPreviewView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (cameraViewController == null) {
			throw new IllegalArgumentException("cameraViewController not set");
		}
		if (viewPartName == null) {
			throw new IllegalArgumentException("viewPartName not set");
		}
	}

	public void setCameraViewController(CameraViewController cameraViewController) {
		this.cameraViewController = cameraViewController;
	}

	public CameraViewController getCameraViewController() {
		return cameraViewController;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	public String getViewPartName() {
		return viewPartName;
	}
}
