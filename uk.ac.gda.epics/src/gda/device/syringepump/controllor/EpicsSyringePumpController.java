/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package gda.device.syringepump.controllor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.syringepump.SyringePumpController;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

public class EpicsSyringePumpController extends DeviceBase implements SyringePumpController {
	private static final Logger logger = LoggerFactory.getLogger(EpicsSyringePumpController.class);

	private static final String ENABLED = "DISABLE";
	private static final int ENABLED_VALUE = 0;

	private static final String STATUS = "STATUS";
	private static final int IDLE_VALUE = 0;

	private static final String INFUSE = "IRUN";
	private static final String WITHDRAW = "WRUN";
	private static final String STOP = "STOP";

	private static final String FORCE = "FORCE";
	private static final String FORCE_RBV = "FORCE:RBV";
	private static final String TTIME = "TTIME";
	private static final String TTIME_RBV = "TTIME:RBV";
	private static final String DIAMETER = "DIAMETER";
	private static final String DIAMETER_RBV = "DIAMETER:RBV";

	private static final String IRATE = "IRATE";
	private static final String IRATE_RBV = "IRATE:RBV";
	private static final String WRATE = "WRATE";
	private static final String WRATE_RBV = "WRATE:RBV";

	private static final String INFUSED_VOLUME = "IVOLUME:RBV";
	private static final String CLEAR_INFUSED = "CLEARINFUSED";
	private static final String WITHDRAWN_VOLUME = "WVOLUME:RBV";
	private static final String CLEAR_WITHDRAWN = "CLEARWITHDRAWN";

	private static final String TVOLUME = "TVOLUME";

	String pvPrefix;
	private String name;
	private EpicsController controller;
	private EpicsChannelManager channelManager;
	private double volume;
	private double capacity;


	private PutListener infuseListener = new PutListener() {

		@Override
		public void putCompleted(PutEvent ev) {

		}
	};

	private Channel enable;
	private Channel status;
	private Channel force;
	private Channel force_rbv;
	private Channel targetTime;
	private Channel targetTime_rbv;
	private Channel diameter;
	private Channel diameter_rbv;
	private Channel infuseRate;
	private Channel infuseRate_rbv;
	private Channel withdrawRate;
	private Channel withdrawRate_rbv;
	private Channel targetVolume;
	private Channel withdrawnVolume;
	private Channel infusedVolume;
	private Channel stop;
	private Channel wrun;
	private Channel irun;
	private Channel clearInfused;
	private Channel clearWithdrawn;

	private boolean configured;

	private void createChannelAccess() throws CAException, InterruptedException {
		channelManager = new EpicsChannelManager();

		enable = channelManager.createChannel(pvPrefix + ENABLED);
		status = channelManager.createChannel(pvPrefix + STATUS);

		force = channelManager.createChannel(pvPrefix + FORCE);
		force_rbv = channelManager.createChannel(pvPrefix + FORCE_RBV);

		targetTime = channelManager.createChannel(pvPrefix + TTIME);
		targetTime_rbv = channelManager.createChannel(pvPrefix + TTIME_RBV);

		diameter = channelManager.createChannel(pvPrefix + DIAMETER);
		diameter_rbv = channelManager.createChannel(pvPrefix + DIAMETER_RBV);

		infuseRate = channelManager.createChannel(pvPrefix + IRATE);
		infuseRate_rbv = channelManager.createChannel(pvPrefix + IRATE_RBV);

		withdrawRate = channelManager.createChannel(pvPrefix + WRATE);
		withdrawRate_rbv = channelManager.createChannel(pvPrefix + WRATE_RBV);

		targetVolume = channelManager.createChannel(pvPrefix + TVOLUME);

		infusedVolume = channelManager.createChannel(pvPrefix + INFUSED_VOLUME);
		clearInfused = channelManager.createChannel(pvPrefix + CLEAR_INFUSED);

		withdrawnVolume = channelManager.createChannel(pvPrefix + WITHDRAWN_VOLUME);
		clearWithdrawn = channelManager.createChannel(pvPrefix + CLEAR_WITHDRAWN);

		irun = channelManager.createChannel(pvPrefix + INFUSE);
		wrun = channelManager.createChannel(pvPrefix + WITHDRAW);
		stop = channelManager.createChannel(pvPrefix + STOP);

		controller.setMonitor(withdrawnVolume, new MonitorListener() {
			@Override
			public void monitorChanged(MonitorEvent ev) {
				try {
					notifyIObservers(this, getVolume());
				} catch (DeviceException e) {
					logger.warn("{} - Could not update volume after monitor event", getName());
				}
			}
		});
		controller.setMonitor(infusedVolume, new MonitorListener() {
			@Override
			public void monitorChanged(MonitorEvent ev) {
				try {
					notifyIObservers(this, getVolume());
				} catch (DeviceException e) {
					logger.warn("{} - Could not update volume after monitor event", getName());
				}
			}
		});

		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
	}

	@Override
	public boolean isBusy() throws DeviceException {
		try {
			return controller.cagetInt(status) != IDLE_VALUE;
		} catch (TimeoutException | CAException | InterruptedException e) {
			throw new DeviceException(getName() + " Could not get status from EpicsSyringePumpController", e);
		}
	}

	@Override
	public boolean isEnabled() {
		try {
			return controller.cagetInt(enable) != ENABLED_VALUE;
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("{} - Could not get enabled status from EpicsSyringePumpController", getName(), e);
			return false;
		}
	}

	private void checkState() {
		if (!isEnabled()) {
			throw new IllegalStateException(getName() + " - is not enabled");
		} else if (!configured) {
			throw new IllegalStateException(getName() + " - is not configured");
		} else if (volume < 0 || volume > capacity) {
			throw new IllegalStateException(getName() + " - Volume must be between 0 and capacity of syringe");
		}
	}

	@Override
	public void stop() throws DeviceException {
		try {
			logger.debug("Stopping syringe pump {}", getName());
			controller.caput(stop, 1, infuseListener);
		} catch (CAException | InterruptedException e) {
			throw new DeviceException(getName() + " - Error calling stop on EpicsSyringePump", e);
		}
	}

	private void irun() throws DeviceException {
		try {
			checkState();
			controller.caput(irun, 1, infuseListener);
		} catch (CAException | InterruptedException e) {
			throw new DeviceException(getName() + " - Could not run infuse", e);
		}
	}

	private void wrun() throws DeviceException {
		try {
			checkState();
			controller.caput(wrun, 1, infuseListener);
		} catch (CAException | InterruptedException e) {
			throw new DeviceException(getName() + " - Could not run withdraw", e);
		}
	}

	private void clear() throws DeviceException {
		try {
			checkState();
			controller.caput(clearInfused, 1);
			controller.caput(clearWithdrawn, 1);
		} catch (CAException | InterruptedException e) {
			throw new DeviceException(getName() + " - Could not clear infuse/withdrawn volumes", e);
		}
	}

	@Override
	public void setForce(double percent) throws DeviceException {
		setDouble(force, "force", percent);
	}

	@Override
	public double getForce() throws DeviceException {
		return getDouble(force_rbv, "force");
	}

	@Override
	public void setTargetTime(double seconds) throws DeviceException {
		setDouble(targetTime, "target time", seconds);
	}

	@Override
	public double getTargetTime() throws DeviceException {
		return getDouble(targetTime_rbv, "target time");
	}

	@Override
	public void setDiameter(double millimeters) throws DeviceException {
		setDouble(diameter, "diameter", millimeters);
	}

	@Override
	public double getDiameter() throws DeviceException {
		return getDouble(diameter_rbv, "diameter");
	}

	@Override
	public void setInfuseRate(double mlps) throws DeviceException {
		setDouble(infuseRate, "infuse rate", mlps);
	}

	@Override
	public double getInfuseRate() throws DeviceException {
		return getDouble(infuseRate_rbv, "infuse rate");
	}

	@Override
	public void setWithdrawRate(double mlps) throws DeviceException {
		setDouble(withdrawRate, "withdraw rate", mlps);
	}

	@Override
	public double getWithdrawRate() throws DeviceException {
		return getDouble(withdrawRate_rbv, "withdraw rate");
	}

	private double getWithdrawnVolume() throws DeviceException {
		return getDouble(withdrawnVolume, "withdrawn volume");
	}

	private double getInfusedVolume() throws DeviceException {
		return getDouble(infusedVolume, "infused volume");
	}

	private void setTargetVolume(double ml) throws DeviceException {
		setDouble(targetVolume, "target volume", ml);
	}

	@Override
	public void infuse(double ml) throws DeviceException {
		if (isBusy()) {
			throw new DeviceException(getName() + " - Syringe already moving");
		}
		clear();
		double endVolume = volume - ml;
		if (endVolume > capacity) {
			throw new DeviceException(getName() + " - Cannot withdraw to more than the capacity of syringe");
		} else if (endVolume < 0) {
			throw new DeviceException(getName() + " - Cannot infuse more than the current volume of syringe");
		}
		if (ml == 0) {
			return;
		} else if (ml < 0) {
			setTargetVolume(-ml);
			wrun();
		} else {
			setTargetVolume(ml);
			irun();
		}
	}

	@Override
	public void configure() throws FactoryException {
		if (configured) {
			return;
		}
		if (getName() == null) {
			throw new FactoryException("Name cannot be null for SyringePumpController");
		}
		if (controller == null) {
			controller = EpicsController.getInstance();
		}
		try {
			createChannelAccess();
		} catch (CAException | InterruptedException e) {
			throw new FactoryException(getName() + " - Could not create channel access", e);
		}
		configured = true;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	private double getDouble(Channel channel, String fieldname) throws DeviceException {
		try {
			checkState();
			return controller.cagetDouble(channel);
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not get " + fieldname + " from EpicsSyringePumpController");
		}
	}

	private void setDouble(Channel channel, String fieldname, double value) throws DeviceException {
		try {
			checkState();
			logger.trace("{} - Setting {} to {}", getName(), fieldname, value);
			controller.caput(channel, value);
		} catch (Exception e) {
			throw new DeviceException(getName() + " - Could not set " + fieldname + " from EpicsSyringePumpController");
		}
	}

	@Override
	public double getCapacity() {
		return capacity;
	}

	public void setCapacity(double ml) {
		capacity = ml;
	}

	@Override
	public double getVolume() throws DeviceException {
		return volume + getWithdrawnVolume() - getInfusedVolume();
	}

	@Override
	public void setVolume(double ml) throws DeviceException {
		try {
			if (isBusy()) {
				throw new IllegalStateException(getName() + " - Cannot set volume while pump is busy");
			}
			volume = ml;
		} catch (DeviceException e) {
			logger.error("{} - Could not get busy status while changing volume", getName(), e);
			throw e;
		}
	}

}
