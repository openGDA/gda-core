/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.detector.vortex;

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Timer;
import gda.device.XmapDetector;
import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;
import uk.ac.gda.exafs.ui.detector.Acquire;

public class VortexAcquire extends Acquire {
	private int[][][] data3d;
	private static final Logger logger = LoggerFactory.getLogger(Acquire.class);
	private boolean continuousAquire = false;
	private Thread continuousThread;
	private ReentrantLock lock;
	private XmapDetector xmapDetector;
	private Timer tfg;
	private SashFormPlotComposite sashPlotFormComposite;
	
	public VortexAcquire(SashFormPlotComposite sashPlotFormComposite, XmapDetector xmapDetector, Timer tfg){
		this.sashPlotFormComposite = sashPlotFormComposite;
		this.xmapDetector = xmapDetector;
		this.tfg = tfg;
		lock = new ReentrantLock();
	}
	
	protected void acquire(double collectionTime) throws Exception {
		xmapDetector.clearAndStart();
		tfg.countAsync(collectionTime);
		xmapDetector.stop();
		xmapDetector.waitWhileBusy();
		int[][] data = xmapDetector.getData();
		data3d = convert2DTo3DArray(data);
	}
	
	public int[][][] getData3d() {
		return data3d;
	}

	protected int[][][] convert2DTo3DArray(int[][] data) {
		int[][][] ret = new int[data.length][1][];
		for (int i = 0; i < data.length; i++)
			ret[i][0] = data[i];
		return ret;
	}
	
	public void continuousAcquire(final long aquireWaitTime, final double collectiontime) {
		if (lock != null && lock.isLocked()) {
			final String msg = "There is currently an acquire running. You cannot run another one.";
			logger.info(msg);
			sashPlotFormComposite.appendStatus(msg, logger);
			return;
		}
		try {
			continuousThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					lock.lock();
					while (continuousAquire) {
						if (!lock.isLocked())
							break;
						acquire(collectiontime);
						if (!lock.isLocked())
							break;
						Thread.sleep(aquireWaitTime);
					}
				} catch (InterruptedException e) {
					logger.error("Continuous acquire problem with detector.", e);
				} catch (Throwable e) {
					logger.error("Continuous acquire problem with detector.", e);
				} finally {
					lock.unlock();
				}
			}
		}, "Detector Live Runner");
		continuousThread.start();
		} 
		catch (Exception e) {
			logger.error("Internal errror process continuous data from detector.", e);
		}
	}

}