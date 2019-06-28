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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.epics.client.views.model.impl.EPICSBaseModel;
import uk.ac.gda.epics.dxp.client.views.StatusViewController;

public class DXPModel extends EPICSBaseModel implements InitializingBean, IDXPModel {
	private static final Logger logger = LoggerFactory.getLogger(DXPModel.class);

	private StatusViewController statusViewController;

	private static final String ELAPSED_REAL_TIME = "ElapsedReal";
	private static final String ELAPSED_LIVE_TIME = "ElapsedLive";
	private static final String INSTANT_DEAD_TIME = "IDeadTime";
	private static final String DEAD_TIME = "DeadTime";
	private static final String ACQUIRING = "Acquiring";

	private final MonitorListener acquireStateMonitorListener;
	private final MonitorListener realTimeMonitorListener;
	private final MonitorListener liveTimeMonitorListener;
	private final MonitorListener instantDeadTimeMonitorListener;
	private final MonitorListener deadTimeMonitorListener;

	public DXPModel() {
		liveTimeMonitorListener = this::liveTimeMonitorChanged;
		realTimeMonitorListener = this::realTimeMonitorChanged;
		instantDeadTimeMonitorListener = this::instantDeadTimeMonitorChanged;
		deadTimeMonitorListener = this::deadTimeMonitorChanged;
		acquireStateMonitorListener = this::acquireStatemonitorChanged;
	}

	private void acquireStatemonitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isENUM() && statusViewController != null) {
			statusViewController.updateAcquireState(((DBR_Enum) dbr).getEnumValue()[0]);
		}
	}

	private void realTimeMonitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isDOUBLE() && statusViewController != null) {
			statusViewController.updateRealTime(((DBR_Double) dbr).getDoubleValue()[0]);
		}
	}

	private void liveTimeMonitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isDOUBLE() && statusViewController != null) {
			statusViewController.updateLiveTime(((DBR_Double) dbr).getDoubleValue()[0]);
		}
	}

	private void instantDeadTimeMonitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isDOUBLE() && statusViewController != null) {
			statusViewController.updateInstantDeadTime(((DBR_Double) dbr).getDoubleValue()[0]);
		}
	}

	private void deadTimeMonitorChanged(MonitorEvent arg0) {
		final DBR dbr = arg0.getDBR();
		if (dbr.isDOUBLE()) {
			statusViewController.updateDeadTime(((DBR_Double) dbr).getDoubleValue()[0]);
		}
	}

	@Override
	public short getAcquireState() throws Exception {
		return EPICS_CONTROLLER.cagetEnum(getChannel(ACQUIRING, acquireStateMonitorListener));
	}

	@Override
	public int getInstantDeadTime() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(INSTANT_DEAD_TIME, instantDeadTimeMonitorListener));
	}

	@Override
	public double getDeadTime() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(DEAD_TIME, deadTimeMonitorListener));
	}

	@Override
	public double getRealTime() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ELAPSED_REAL_TIME, realTimeMonitorListener));
	}

	@Override
	public double getLiveTime() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(ELAPSED_LIVE_TIME, liveTimeMonitorListener));
	}

	@Override
	public void doCheckAfterPropertiesSet() throws Exception {
		// nothing to do
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
}
