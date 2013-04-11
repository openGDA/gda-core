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
import gda.device.detector.areadetector.v17.impl.NDROIPVsImpl;
import gda.device.detector.nxdetector.plugin.NullNXPlugin;
import gda.device.detector.nxdetector.roi.RectangularROI;
import gda.device.detector.nxdetector.roi.RectangularROIProvider;
import gda.scan.ScanInformation;

import java.io.IOException;

public class ADRectangularROIPlugin extends NullNXPlugin implements NDPlugin{

	public static ADRectangularROIPlugin createFromBasePVName(String pluginName, String basePVName, RectangularROIProvider<Integer> roiProvider) {
		NDROIPVs ndROIPVs = NDROIPVsImpl.createFromBasePVName(basePVName);
		return new ADRectangularROIPlugin(ndROIPVs, pluginName, roiProvider);
	}

	private final NDROIPVs pvs;

	private final String pluginName;
	
	private final RectangularROIProvider<Integer> roiProvider; // optional
	
	public final String INACTIVE_ROI_NAME = "gda_inactive";

	private String ndInputArrayPort;

	public ADRectangularROIPlugin(NDROIPVs ndROIPVs, String pluginName, RectangularROIProvider<Integer> roiProvider) {
		this.pvs = ndROIPVs;
		this.roiProvider = roiProvider;
		this.pluginName = pluginName;
	}
	
	public RectangularROI<Integer> getRoi() throws IllegalArgumentException, IndexOutOfBoundsException, Exception {
		return roiProvider.getRoi();
	}
	
	@Override
	public String getName() {
		return pluginName;
	}

	@Override
	public boolean willRequireCallbacks() {
		try {
			return getRoi() != null;
		} catch (Exception e) {
			throw new IllegalStateException("Could not get ROI", e);
		}
	}
	
	@Override
	public void prepareForCollection(int numberImagesPerCollection, ScanInformation scanInfo) throws Exception {
		if (getRoi() != null) {
			if (getInputNDArrayPort() != null) {
				pvs.getPluginBasePVs().getNDArrayPortPVPair().putWait(getInputNDArrayPort());
			}
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

	/**
	 * {@inheritDoc}
	 * 
	 * @return ndArrayPort name that will be configured during prepareforCollection if an roi were configured.
	 */
	@Override
	public String getInputNDArrayPort() {
		return ndInputArrayPort; 
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @param nDArrayPort ndArrayPort name that will be configured during prepareforCollection if an roi were configured.
	 */
	@Override
	public void setInputNDArrayPort(String nDArrayPort) {
		this.ndInputArrayPort = nDArrayPort;
	}

	/**
	 * Get the permanent Epics port name.
	 * @return portName
	 * @throws IOException 
	 */
	@Override
	public String getPortName() throws IOException {
		return pvs.getPluginBasePVs().getPortNamePV().get();
	}


}
