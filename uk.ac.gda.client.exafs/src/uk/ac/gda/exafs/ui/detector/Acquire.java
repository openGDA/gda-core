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

package uk.ac.gda.exafs.ui.detector;

import gda.device.Detector;
import gda.factory.Finder;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.rcp.views.plot.SashFormPlotComposite;

public class Acquire {
	private static final Logger logger = LoggerFactory.getLogger(Acquire.class);
	private boolean continuousAquire = false;
	private Thread continuousThread;
	private ReentrantLock lock;
	
	public Acquire() {
		lock = new ReentrantLock();
	}
	
	//isDisposed()
	//getAcquireWaitTime()
	//getDetectorCollectionTime()
	//getDetectorName()
	public void continuousAcquire(Display display, final boolean disposed, SashFormPlotComposite sashPlotFormComposite, final long aquireWaitTime, final double collectiontime, final String detectorName) {
		continuousAquire = !continuousAquire;
		if (continuousAquire && lock != null && lock.isLocked()) {
			final String msg = "There is currently an acquire running. You cannot run another one.";
			logger.info(msg);
			sashPlotFormComposite.appendStatus(msg, logger);
			return;
		}
		try {
			if(continuousAquire) {
				acquireStarted();
				continuousThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							lock.lock();
							while (continuousAquire) {
								if (!lock.isLocked())
									break;
								if (disposed)
									break;
								acquire(null, collectiontime);
								if (!lock.isLocked())
									break;
								if (disposed)
									break;
								Thread.sleep(aquireWaitTime);
							}
						} catch (InterruptedException e) {
							// Expected
						} catch (Throwable e) {
							logger.error("Continuous acquire problem with detector.", e);
						} finally {
							lock.unlock();
						}
					}
				}, "Detector Live Runner");
				continuousThread.start();
			} 
			else {
				// Run later otherwise button looks unresponsive.
				// Even though this is the display thread already.
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							lock.lock();
							acquireFinished();
							Detector detector = (Detector) Finder.getInstance().find(detectorName);
							logger.debug("Stopping detector");
							detector.stop();
						} catch (Exception e) {
							logger.error("Continuous problem configuring detector -  cannot stop detector.", e);
						} finally {
							lock.unlock();
						}
					}
				});
			}
		} catch (Exception e) {
			logger.error("Internal errror process continuous data from detector.", e);
			acquireFinished();
		}
	}
	
	public void acquire(IProgressMonitor monitor, double collectionTimeValue) throws Exception {
		
	}
	
	public void acquireStarted() {
		
	}

	public void acquireFinished() {
		
	}
	
}