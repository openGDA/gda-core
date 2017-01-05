/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.dxp.client.views.model;

import gda.epics.interfaces.NDPluginBaseType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.views.model.impl.EPICSBaseModel;
import uk.ac.gda.epics.dxp.client.views.StatusViewController;

/**
 *
 */
public class DXPModel extends EPICSBaseModel<Object> implements InitializingBean {
	static final Logger logger = LoggerFactory.getLogger(DXPModel.class);
	protected StatusViewController statusViewController;
	//private static List<String> detectorDataTypes;
	
	public static final String ElapsedReal="ElapsedReal";
	public static final String ElapsedLive="ElapsedLive";
	public static final String IDeadTime="IDeadTime";
	public static final String DeadTime="DeadTime";
	public static final String Acquiring="Acquiring";

	@Override
	protected Class<Object> getConfigClassType() {
		return Object.class;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	public StatusViewController getStatusViewController() {
		return statusViewController;
	}

	public void setStatusViewController(StatusViewController statusViewController) {
		this.statusViewController = statusViewController;
	}

	/**/
	private AcquireStateMonitorListener acquireStateMonitorListener;
	private RealTimeMonitorListener realTimeMonitorListener;
	private LiveTimeMonitorListener liveTimeMonitorListener;
	private InstantDeadTimeMonitorListener ideadtimeMonitorListener;
	private DeadTimeMonitorListener deadTimeMonitorListener;

	public DXPModel() {
		liveTimeMonitorListener = new LiveTimeMonitorListener();
		realTimeMonitorListener = new RealTimeMonitorListener();
		ideadtimeMonitorListener = new InstantDeadTimeMonitorListener();
		deadTimeMonitorListener = new DeadTimeMonitorListener();
		acquireStateMonitorListener = new AcquireStateMonitorListener();
	}

	private class AcquireStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isENUM()) {
				if (statusViewController != null) {
					statusViewController.updateAcquireState(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	private class RealTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				if (statusViewController != null) {
					statusViewController.updateRealTime(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class LiveTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				if (statusViewController != null) {
					statusViewController.updateLiveTime(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class InstantDeadTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				if (statusViewController != null) {
					statusViewController.updateInstantDeadTime(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class DeadTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr.isDOUBLE()) {
				statusViewController.updateDeadTime(((DBR_Double) dbr).getDoubleValue()[0]);
			}
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public short getAcquireState() throws Exception {
		try {
//			if (config != null) {
//				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAcquireState().getPv(),
//						acquireStateMonitorListener));
//			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(Acquiring, acquireStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	public int getInstantDeadTime() throws Exception {
		try {
//			if (config != null) {
//				return EPICS_CONTROLLER.cagetInt(createChannel(config.getInstantDeadTime().getPv(),
//						ideadtimeMonitorListener));
//			}
			return EPICS_CONTROLLER.cagetInt(getChannel(IDeadTime, ideadtimeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	*
	*/
	public double getDeadTime() throws Exception {
		try {
//			if (config != null) {
//				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getDeadTime().getPv(),
//						deadTimeMonitorListener));
//			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(DeadTime, deadTimeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	public double getRealTime() throws Exception {
		try {
//			if (config != null) {
//				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquireTime_RBV().getPv(),
//						realTimeMonitorListener));
//			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ElapsedReal, realTimeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	public double getLiveTime() throws Exception {
		try {
//			if (config != null) {
//				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getLiveTime().getPv(),
//						liveTimeMonitorListener));
//			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ElapsedLive, liveTimeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void doCheckAfterPropertiesSet() throws Exception {
		// initializeStatusViewValues();

	}

	public void initializeStatusViewValues() throws Exception {
		if (statusViewController != null) {

			try {
				double ideadtime = getInstantDeadTime();
				statusViewController.updateInstantDeadTime(ideadtime);
			} catch (TimeoutException tme) {
				logger.error("Cannot getArrayCounter_RBV - Either the PV is incorrect or the IOC is not in function");
			}
			try {
				double deadtime = getDeadTime();
				statusViewController.updateDeadTime(deadtime);
			} catch (TimeoutException tme) {
				logger.error("Cannot getTimeRemaining_RBV - Either the PV is incorrect or the IOC is not in function");
			}
			try {
				double realtime = getRealTime();
				statusViewController.updateRealTime(realtime);
			} catch (TimeoutException tme) {
				logger.error("Cannot getArrayCounter_RBV - Either the PV is incorrect or the IOC is not in function");
			}
			try {
				double livetime = getLiveTime();
				statusViewController.updateLiveTime(livetime);
			} catch (TimeoutException tme) {
				logger.error("Cannot getTimeRemaining_RBV - Either the PV is incorrect or the IOC is not in function");
			}

			try {
				short acquirestate = getAcquireState();
				statusViewController.updateAcquireState(acquirestate);
			} catch (TimeoutException tme) {
				logger.error("Cannot getDetectorState_RBV - Either the PV is incorrect or the IOC is not in function");
			}
		}
	}

	@Override
	protected NDPluginBaseType getPluginBaseTypeConfig() throws FactoryException {
		throw new FactoryException("No base plugin for ADBase");
	}

}
