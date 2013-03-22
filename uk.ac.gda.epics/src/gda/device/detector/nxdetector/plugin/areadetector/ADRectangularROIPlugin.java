/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.detector.nxdetector.plugin.areadetector;

import gda.device.detector.areadetector.v17.NDROIPVs;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.scan.ScanInformation;

public class ADRectangularROIPlugin extends NullNXPlugin {


	private final NDROIPVs pvs;

	private final String pluginName;
	
	private RectangularROI roi;
	
	private RectangularROIProvider roiProvider; // optional

	private Integer roiProviderIndex = null; // optional
	
	public final String INACTIVE_ROI_NAME = "gda_inactive";
	
	/**
	 * Get the roi provider
	 * @return the RectangularROIProvider or null if unset
	 */
	public RectangularROIProvider getRoiProvider() {
		return roiProvider;
	}

	/**
	 * Set the optional ROIProvider. Disables manual setting of ROI to avoid confusion.
	 * @param roiProvider null to remove
	 */
	public void setRoiProvider(RectangularROIProvider roiProvider) {
		this.roiProvider = roiProvider;
	}

	public Integer getRoiProviderIndex() {
		return roiProviderIndex;
	}

	public void setRoiProviderIndex(Integer roiProviderIndex) {
		this.roiProviderIndex = roiProviderIndex;
	}

	public RectangularROI getRoi() {
		if (roiProvider != null) {
			return roiProvider.getROI(getRoiProviderIndex());
		}
		return roi;
	}
	
	/**
	 * Set the roi to configure.
	 * @param roi null to disable
	 * @throws IllegalStateException if called with an ROI provider set
	 */
	public void setRoi(RectangularROI roi) throws IllegalStateException{
		if (roiProvider != null) {
			throw new IllegalStateException("An ROI cannot be set when an roiProvider is specified");
		}
		this.roi = roi;
	}
	
	public ADRectangularROIPlugin(NDROIPVs ndROIPVs, String name) {
		this.pvs = ndROIPVs;
		this.pluginName = name;
	}
	
	@Override
	public String getName() {
		return pluginName;
	}

	@Override
	public boolean willRequireCallbacks() {
		return getRoi() != null;
	}
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (getRoi() != null) {
			pvs.getNamePV().putCallback(getRoi().getName());
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(true);
			pvs.getXDimension().getEnablePVPair().putCallback(true);
			pvs.getYDimension().getEnablePVPair().putCallback(true);
			pvs.getZDimension().getEnablePVPair().putCallback(false);
			pvs.getXDimension().getMinPVPair().putCallback(getRoi().getXstart());
			pvs.getXDimension().getSizePVPair().putCallback(getRoi().getXsize());
			pvs.getYDimension().getMinPVPair().putCallback(getRoi().getYstart());
			pvs.getYDimension().getSizePVPair().putCallback(getRoi().getYsize());
		} else {
			pvs.getNamePV().putCallback(INACTIVE_ROI_NAME);
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(false);
			pvs.getXDimension().getEnablePVPair().putCallback(false);
			pvs.getYDimension().getEnablePVPair().putCallback(false);
			pvs.getZDimension().getEnablePVPair().putCallback(false);
		}
	}

}
