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
			pvs.getNamePV().putWait(getRoiName());
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putWait(true);
			pvs.getXDimension().getEnablePVPair().putWait(true);
			pvs.getYDimension().getEnablePVPair().putWait(true);
			pvs.getZDimension().getEnablePVPair().putWait(false);
			pvs.getXDimension().getMinPVPair().putWait(getRoi().getXstart());
			pvs.getXDimension().getSizePVPair().putWait(getRoi().getXsize());
			pvs.getYDimension().getMinPVPair().putWait(getRoi().getYstart());
			pvs.getYDimension().getSizePVPair().putWait(getRoi().getYsize());
		} else {
			pvs.getNamePV().putWait("gda_inactive");
			pvs.getPluginBasePVs().getEnableCallbacksPVPair().putWait(false);
			pvs.getXDimension().getEnablePVPair().putWait(false);
			pvs.getYDimension().getEnablePVPair().putWait(false);
			pvs.getZDimension().getEnablePVPair().putWait(false);
		}
	}

}
