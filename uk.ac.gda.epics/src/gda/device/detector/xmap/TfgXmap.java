/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.detector.xmap;

import gda.device.CounterTimer;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.XmapDetector;
import gda.device.detector.countertimer.TFGCounterTimer;
import gda.epics.CAClient;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.CAException;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a Tfg and XMap combination. Since the Tfg will generally also be part of a TfgScaler combination there is
 * a slave mode. In this mode methods which set things on the Tfg do nothing.
 */
public class TfgXmap extends TFGCounterTimer implements CounterTimer, Detector {
	private static final Logger logger = LoggerFactory.getLogger(TfgXmap.class);
	private XmapDetector xmap = null;
	private String xmapName;
	private int minimumReadoutDelay = 0;
	private String collectionModePV;

	public TfgXmap() {
		this.setExtraNames(new String[] { "FF" });
		this.setOutputFormat(new String[] { "%9.2f" });
	}

	@Override
	public void configure() throws FactoryException {
		// TODO remove finder
		if (xmap == null)
			if ((xmap = (XmapDetector) Finder.getInstance().find(xmapName)) == null)
				logger.error("XmapDetector " + xmapName + " not found");
		super.configure();
	}

	/**
	 * Set the Xspress system name
	 * 
	 * @param xspressSystemName
	 *            the Xspress system name
	 */
	public void setXmapName(String xspressSystemName) {
		this.xmapName = xspressSystemName;
	}

	/**
	 * Get the Xspress system name
	 * 
	 * @return the Xspress system name.
	 */
	public String getXmapName() {
		return xmapName;
	}

	/**
	 * @return Returns the xmap.
	 */
	public XmapDetector getXmap() {
		return xmap;
	}

	/**
	 * @param xmap
	 *            The xmap to set.
	 */
	public void setXmap(XmapDetector xmap) {
		this.xmap = xmap;
	}


	@Override
	public double[] readout() throws DeviceException {
		if(minimumReadoutDelay!=0){
			long delay = minimumReadoutDelay;
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		final double ff = xmap.readoutScalerData();
		return new double[] { ff };
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		return xmap.getDataDimensions();
	}

	@Override
	public void collectData() throws DeviceException {
		if (xmap != null)
			xmap.clearAndStart();
		if (!slave && timer != null)
			timer.countAsync(collectionTime);
	}

	private void clearXmap() throws DeviceException {
		if (slave && xmap != null)
			xmap.clear();
	}

	@Override
	public void atPointStart() throws DeviceException {
		clearXmap();
	}

	@Override
	public double[] readChannel(int startFrame, int frameCount, int channel) throws DeviceException {
		return null;
	}

	@Override
	public double[] readFrame(int startChannel, int channelCount, int frame) throws DeviceException {
		return null;
	}
	
	@Override
	public void atScanStart() throws DeviceException {
		CAClient caclient = new CAClient();
		try {
			caclient.caput(collectionModePV, 0);
		} catch (CAException e) {
			logger.error("Could not set collection mode to spectra", e);
		} catch (InterruptedException e) {
			logger.error("Could not set collection mode to spectra", e);
		}
	}

	//TODO I can't see that this is ever called??
	@Override
	public void start() throws DeviceException {
		xmap.start();
		if (!slave & timer != null) {
			timer.start();
		}
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	/**
	 * Override DetectorBase to work within scans
	 */
	@Override
	public Object getPosition() throws DeviceException {
		Object ob = this.readout();
		double[] rois = (double[]) ob;
		if (slave)
			return rois;
		return ArrayUtils.addAll(new double[] { collectionTime }, rois);
	}

	/**
	 * Override ScannableBase to work within scans
	 */
	@Override
	public String[] getInputNames() {
		if (slave)
			return new String[] {};
		return new String[] { "time" };
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Vortex based CounterTimer";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CounterTimer";
	}

	public int getMinimumReadoutDelay() {
		return minimumReadoutDelay;
	}

	public void setMinimumReadoutDelay(int minimumReadoutDelay) {
		this.minimumReadoutDelay = minimumReadoutDelay;
	}
	
	public String getCollectionModePV() {
		return collectionModePV;
	}

	public void setCollectionModePV(String collectionModePV) {
		this.collectionModePV = collectionModePV;
	}
}