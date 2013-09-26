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

package gda.device.scannable;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Untested on hardware! This is simply coded against the information in the Controls group wiki for the moment...
 * <p>
 * Operates the Epics Trajectory Template in ContinuousScans.
 * <p>
 * Initially coded to only operate one motor, but the Epics Trajectory Template can have up to 8 axes.
 * <p>
 * Initially only performs a simple constant velocity scan from one point to another.
 */
public class EpicsTrajectoryScannable extends ScannableMotionUnitsBase implements ContinuouslyScannable,
		InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsTrajectoryScannable.class);

	private class TrajPutListener implements PutListener {
		@Override
		public void putCompleted(PutEvent arg0) {
			trajMoveComplete = true;
			String trajStatus = "unknown";
			try {
				trajStatus = controller.cagetString(trajStatusChannel);
			} catch (Exception e) {
				logger.warn("exception fetching trajactory completion status: " + e.getMessage());
			}
			logger.info("Trajectory move completed by " + getName() + " with status " + trajStatus);
		}
	}

	public static String numElementsPV = "NELM";
	public static String trajTimePV = "TIME";
	public static String startTrajPV = "EXECUTE";
	public static String trajStatusPV = "ESTATUS";
	public static String trajAbortPV = "ABORT";
	public static String trajPositionsAxis1PV = "M1TRAJ";

	private String templateName = "";
	private ContinuousParameters parameters = null;

	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private Channel numElementsChannel;
//	private Channel trajTimeChannel;
	private Channel startTrajChannel;
	private Channel trajStatusChannel;
	private Channel trajAbortChannel;
	private Channel trajPositionsAxis1Channel;

	private TrajPutListener trajListener;
	private volatile boolean trajMoveComplete = true;

	public EpicsTrajectoryScannable() {
	}

	@Override
	public void configure() throws FactoryException {
		if (templateName == null || templateName.isEmpty()) {
			throw new FactoryException(getName() + " cannot configure as Epics template not defined!");
		}

		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		try {
			numElementsChannel = channelManager.createChannel(templateName + numElementsPV);
//			trajTimeChannel = channelManager.createChannel(templateName + trajTimePV);
			startTrajChannel = channelManager.createChannel(templateName + startTrajPV);
			trajStatusChannel = channelManager.createChannel(templateName + trajStatusPV);
			trajAbortChannel = channelManager.createChannel(templateName + trajAbortPV);
			trajPositionsAxis1Channel = channelManager.createChannel(templateName + trajPositionsAxis1PV);
		} catch (CAException e) {
			throw new FactoryException(e.getMessage(), e);
		}
	}

	@Override
	public ContinuousParameters getContinuousParameters() {
		return parameters;
	}

	@Override
	public void prepareForContinuousMove() throws DeviceException {
		if (parameters != null) {
			try {
				controller.caput(numElementsChannel, parameters.getNumberDataPoints());
				// TODO - this needs to be more flexible for non-linear scans. Vasanthi thinks this is wrong for these
				// scans anyway...
				controller.caput(trajPositionsAxis1Channel, new double[] { parameters.getStartPosition(),
						parameters.getEndPosition() });
			} catch (Exception e) {
				throw new DeviceException(getName() + " exception in prepareForContinuousMove",e);
			}
		}
	}

	@Override
	public void performContinuousMove() throws DeviceException {
		try {
			trajMoveComplete = false;
			controller.caput(startTrajChannel, 1, trajListener);
			logger.info("Trajectory move started by " + getName());
		} catch (Exception e) {
			throw new DeviceException(getName() + " exception in performContinuousMove",e);
		}
	}
	
	@Override
	public void continuousMoveComplete() throws DeviceException {
		// nothing this class needs to do, I think.
	}

	@Override
	public boolean isBusy() throws DeviceException {
		return trajMoveComplete ? super.isBusy() : true;
	}

	@Override
	public void stop() throws DeviceException {
		if (!trajMoveComplete) {
			try {
				controller.caput(trajAbortChannel, 1);
			} catch (Exception e) {
				throw new DeviceException(getName() + " exception in stop",e);
			}
		}
		super.stop();
	}

	@Override
	public void setContinuousParameters(ContinuousParameters parameters) {
		this.parameters = parameters;
	}

	@Override
	public void initializationCompleted() {
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateName() {
		return templateName;
	}

	@Override
	public double calculateEnergy(int frameIndex) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfDataPoints() {
		return parameters.getNumberDataPoints();
	}
}
