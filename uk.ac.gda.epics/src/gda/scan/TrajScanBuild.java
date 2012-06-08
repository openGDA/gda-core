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

package gda.scan;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides support for EPICS EpicsTrajectoryScanController. A trajectory scan allows fine control of
 * movement using an XPS motor controller. It allows a given number of pulses to be output from the controller, spaced
 * evenly over a set time frame. These pulses are used to control MCA data acquisition module. Operations in trajectory
 * scan involves four steps:
 * <ol>
 * <li>Setup or configure</li>
 * <li>Build</li>
 * <li>Execute</li>
 * <li>Read</li>
 * </ol>
 * Detector data collection will be handled by MCA object.
 */
public class TrajScanBuild implements InitializationListener {
	private static final Logger logger = LoggerFactory.getLogger(TrajScanBuild.class);

	/**
	 * EPICS Put call back handler
	 */
	private BuildCallbackListener bcbl;
	protected boolean buildDone = false;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private Channel build;

	/**
	 * default constructor
	 */
	public TrajScanBuild() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		bcbl = new BuildCallbackListener();
	}

	/**
	 * starts the build process in EPICS.
	 * 
	 * @param pv
	 */
	public void build(String pv) {
		try {
			build = channelManager.createChannel(pv, false);
		} catch (CAException e1) {
			logger.error("Trajectory scan {} build process failed: ",e1);
		}
		buildDone = false;
		JythonServerFacade.getInstance().print("Trajectory Build is called.");
		controller.execute(new Runnable() {
			@Override
			public void run() {
				try {
					controller.caput(build, 1, bcbl);
					JythonServerFacade.getInstance().print("Trajectory Build after caput.");
					while (!buildDone) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// do nothing
						}
					}
					JythonServerFacade.getInstance().print("Done.");
				} catch (Exception e) {
					logger.error("Trajectory scan {} build process failed: ",e);
				}
			}
		});
	}

	/**
	 * The build call back handler
	 */
	public class BuildCallbackListener implements PutListener {
		@Override
		public synchronized void putCompleted(PutEvent ev) {
			JythonServerFacade.getInstance().print("Build call back now." + ev.getStatus());
			// terminate build calling thread
			buildDone = true;
		}
	}


	@Override
	public void initializationCompleted() {
	}

}
