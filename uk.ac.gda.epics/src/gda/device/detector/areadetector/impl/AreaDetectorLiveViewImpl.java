/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.device.detector.areadetector.impl;

import gda.analysis.RCPPlotter;
import gda.device.DeviceBase;
import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorLiveView;
import gda.device.detector.areadetector.EPICSAreaDetectorImage;
import gda.device.detector.areadetector.EpicsAreaDetectorROIElement;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.eclipse.january.dataset.DoubleDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AreaDetectorLiveViewImpl extends DeviceBase implements AreaDetectorLiveView{

	// Setup the logging facilities
	transient private static final Logger logger = LoggerFactory.getLogger(AreaDetectorLiveViewImpl.class);

	private EPICSAreaDetectorImage image;
	private EpicsAreaDetectorROIElement imageROI;
	private int refreshTime;
	private String plotName;

	private boolean liveThreadRunning = false;

	private gda.device.detector.areadetector.impl.AreaDetectorLiveViewImpl.LiveThread liveThread;

	@Override
	public EPICSAreaDetectorImage getImage() {
		return image;
	}
	@Override
	public void setImage(EPICSAreaDetectorImage image) {
		this.image = image;
	}
	@Override
	public int getRefreshTime() {
		return refreshTime;
	}
	@Override
	public void setRefreshTime(int refreshTime) {
		this.refreshTime = refreshTime;
	}
	@Override
	public String getPlotName() {
		return plotName;
	}
	@Override
	public void setPlotName(String plotName) {
		this.plotName = plotName;
	}
	@Override
	public EpicsAreaDetectorROIElement getImageROI() {
		return imageROI;
	}
	@Override
	public void setImageROI(EpicsAreaDetectorROIElement imageROI) {
		this.imageROI = imageROI;
	}

	@Override
	public void configure() throws FactoryException {
		// start the thread
		try {
			//this.start();
		} catch (Exception e) {
			throw new FactoryException("Failed to start polling thread in configure", e);
		}
	}


	@Override
	public void start() throws CAException, TimeoutException, InterruptedException {

		// if the live thread is running then ignore this statement
		if (liveThreadRunning == false) {

			liveThreadRunning = true;

			// run up the thread to monitor the image
			liveThread = new LiveThread();
			AreaDetectorBin bin = imageROI.getBinning();
			liveThread.setup(bin.getBinX(), bin.getBinY(), getImage(), this);
			liveThread.start();

		}

	}

	@Override
	public void stop() throws CAException, InterruptedException, TimeoutException {

		liveThreadRunning = false;

		if(liveThread != null) {
			// wait for the thread to finish before returning
			liveThread.join();
		}

	}



	private class LiveThread extends Thread {

		int binX;
		int binY;
		EPICSAreaDetectorImage image = null;
		AreaDetectorLiveViewImpl parent = null;
		double lastTimeStamp = 0;

		public void setup(int binx, int biny, EPICSAreaDetectorImage image, AreaDetectorLiveViewImpl parent) {
			this.binX = binx;
			this.binY = biny;
			this.image = image;
			this.parent = parent;
		}

		@SuppressWarnings("static-access")
		@Override
		public void run() {

			while (parent.liveThreadRunning) {

				try {

					if(parent.getImage().getTimeStamp() > this.lastTimeStamp) {

						this.lastTimeStamp = parent.getImage().getTimeStamp();

						double scalefactor = binX*binY;
						// the division here is to normalise the data so it always appears to be plain 16bit
						DoubleDataset data = image.getImage().idivide(scalefactor);

						// set the max and min values so that the scale is correct
						data.set(0, 0, 0);
						data.set(65536, 0, 1);
						RCPPlotter.imagePlot(parent.getPlotName(), data);

					}

				} catch (Exception e) {
					logger.warn("Failure send PCO update to PCOPlot, with error",e);
					try {
						Thread.sleep(parent.getRefreshTime());
					} catch (InterruptedException e1) {
						logger.warn("Failure send PCO update to PCOPlot, with error",e1);
					}
				}

				// should sleep here for a bit to let the thread update.
				try {
					Thread.sleep(parent.getRefreshTime());
				} catch (InterruptedException e) {
					logger.warn("Failure to Sleep to PCOPlot, with error",e);
				}

			}

		}

	}




}
