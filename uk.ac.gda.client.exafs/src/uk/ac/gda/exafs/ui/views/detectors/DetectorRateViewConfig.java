/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.detectors;

import java.util.List;

import org.eclipse.ui.part.ViewPart;

import gda.device.detector.DetectorMonitorDataProviderInterface;
import gda.rcp.views.FindableViewFactoryBase;
import gda.rcp.views.ViewFactoryFinder;

/**
 * Configuration class for detector rates view. This is also a factory that can be used to add view
 * using {@link ViewFactoryFinder}.
 */
public class DetectorRateViewConfig extends FindableViewFactoryBase {
	private String viewDescription = "Detector rate view"; // user friendly description of the view
	private List<String> detectorNames; // detectors to be used in this detector rate view
	private double collectionTime = 1.0; // collection time for each frame of data
	private DetectorMonitorDataProviderInterface dataProvider; // ref. to server side object used to collect the detector rates

	public DetectorMonitorDataProviderInterface getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(DetectorMonitorDataProviderInterface dataProvider) {
		this.dataProvider = dataProvider;
	}

	public String getViewDescription() {
		return viewDescription;
	}

	public void setViewDescription(String viewName) {
		this.viewDescription = viewName;
	}

	public List<String> getDetectorNames() {
		return detectorNames;
	}

	public void setDetectorNames(List<String> detectorNames) {
		this.detectorNames = detectorNames;
	}

	public double getCollectionTime() {
		return collectionTime;
	}

	public void setCollectionTime(double collectionTime) {
		this.collectionTime = collectionTime;
	}

	@Override
	public ViewPart createView() {
		DetectorRateView detRateView = new DetectorRateView();
		detRateView.setViewConfig(this);
		return detRateView;
	}
}
