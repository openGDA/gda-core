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
import gda.scan.ScanInformation;

public class ADRectangularROIPlugin extends NullNXPlugin {


	private final NDROIPVs pvs;

	private final String pluginName;
	
	private String roiName;

	private ADRectangularROI roi;

	/**
	 * The name of the roi to push to Epics, and that {@link ADStatsROIPair} will use to prefix input names.
	 */
	public String getRoiName() {
		return roiName;
	}

	public void setRoiName(String roiName) {
		this.roiName = roiName;
	}
	
	public ADRectangularROI getRoi() {
		return roi;
	}

	/**
	 * @param roi null to disable
	 */
	public void setRoi(ADRectangularROI roi) {
		this.roi = roi;
	}
	
	public ADRectangularROIPlugin(NDROIPVs ndROIPVs, String name) {
		this.pvs = ndROIPVs;
		this.pluginName = name;
		this.setRoiName(name); // initial value only
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
			pvs.getNamePV().putCallback(getRoiName());
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(true);
			pvs.getXDimension().getEnablePVPair().putCallback(true);
			pvs.getYDimension().getEnablePVPair().putCallback(true);
			pvs.getZDimension().getEnablePVPair().putCallback(false);
			pvs.getXDimension().getMinPVPair().putCallback(getRoi().getXstart());
			pvs.getXDimension().getSizePVPair().putCallback(getRoi().getXsize());
			pvs.getYDimension().getMinPVPair().putCallback(getRoi().getYstart());
			pvs.getYDimension().getSizePVPair().putCallback(getRoi().getYsize());
		} else {
			pvs.getNamePV().putCallback("gda_inactive");
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(false);
			pvs.getXDimension().getEnablePVPair().putCallback(false);
			pvs.getYDimension().getEnablePVPair().putCallback(false);
			pvs.getZDimension().getEnablePVPair().putCallback(false);
		}
	}

}
