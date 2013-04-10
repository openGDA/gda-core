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
	
	private RectangularROI<Integer> roi;
	
	private RectangularROIProvider<Integer> roiProvider; // optional

	private Integer roiProviderIndex = null; // optional
	
	public final String INACTIVE_ROI_NAME = "gda_inactive";
	
	/**
	 * Get the roi provider
	 * @return the RectangularROIProvider or null if unset
	 */
	public RectangularROIProvider<Integer> getRoiProvider() {
		return roiProvider;
	}

	/**
	 * Set the optional ROIProvider. Disables manual setting of ROI to avoid confusion.
	 * @param roiProvider null to remove
	 */
	public void setRoiProvider(RectangularROIProvider<Integer> roiProvider) {
		this.roiProvider = roiProvider;
	}

	public Integer getRoiProviderIndex() {
		return roiProviderIndex;
	}

	public void setRoiProviderIndex(Integer roiProviderIndex) {
		this.roiProviderIndex = roiProviderIndex;
	}

	public RectangularROI<Integer> getRoi() {
		if (roiProvider != null) {
			try {
				return roiProvider.getROI(getRoiProviderIndex());
			} catch (Exception e) {
				throw new IllegalStateException("Problem querying the configured roiProvider.", e);
			}
		}
		return roi;
	}
	
	/**
	 * Set the roi to configure.
	 * @param roi null to disable
	 * @throws IllegalStateException if called with an ROI provider set
	 */
	public void setRoi(RectangularROI<Integer> roi) throws IllegalStateException{
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
			pvs.getNamePV().putWait(getRoi().getName());
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putWait(true);
			pvs.getXDimension().getEnablePVPair().putWait(true);
			pvs.getYDimension().getEnablePVPair().putWait(true);
			pvs.getZDimension().getEnablePVPair().putWait(false);
			pvs.getXDimension().getMinPVPair().putWait(getRoi().getXstart());
			pvs.getXDimension().getSizePVPair().putWait(getRoi().getXsize());
			pvs.getYDimension().getMinPVPair().putWait(getRoi().getYstart());
			pvs.getYDimension().getSizePVPair().putWait(getRoi().getYsize());
		} else {
			pvs.getNamePV().putWait(INACTIVE_ROI_NAME);
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putWait(false);
			pvs.getXDimension().getEnablePVPair().putWait(false);
			pvs.getYDimension().getEnablePVPair().putWait(false);
			pvs.getZDimension().getEnablePVPair().putWait(false);
		}
	}

}
