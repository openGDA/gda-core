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

public class ADROIPlugin extends NullNXPlugin {

	public class RectangularROI {

		final private int xstart;
		final private int xsize;
		final private int ystart;
		final private int ysize;

		public RectangularROI(int xstart, int xsize, int ystart, int ysize) {
			this.xstart = xstart;
			this.xsize = xsize;
			this.ystart = ystart;
			this.ysize = ysize;
		}

		public int getXstart() {
			return xstart;
		}

		public int getXsize() {
			return xsize;
		}

		public int getYstart() {
			return ystart;
		}

		public int getYsize() {
			return ysize;
		}
	}

	private final NDROIPVs pvs;

	private final String name;

	private boolean enabled = false;
	
	private RectangularROI roi;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public RectangularROI getRoi() {
		return roi;
	}

	public void setRoi(RectangularROI roi) {
		this.roi = roi;
	}
	
	public ADROIPlugin(NDROIPVs ndROIPVs, String name) {
		this.pvs = ndROIPVs;
		this.name = name;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean willRequireCallbacks() {
		return isEnabled();
	}
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		pvs.getNamePV().putCallback(getName());
		pvs.getPluginBasePVs().getEnableCallbacksPVPair().putCallback(isEnabled());
		pvs.getXDimension().getEnablePV().putCallback(isEnabled());
		pvs.getYDimension().getEnablePV().putCallback(isEnabled());
		pvs.getZDimension().getEnablePV().putCallback(false);
		if (getRoi() != null) {
			pvs.getXDimension().getStartPVPair().putCallback(getRoi().getXstart());
			pvs.getXDimension().getSizePVPair().putCallback(getRoi().getXsize());
			pvs.getYDimension().getStartPVPair().putCallback(getRoi().getYstart());
			pvs.getYDimension().getSizePVPair().putCallback(getRoi().getYsize());
		}
	}

}
