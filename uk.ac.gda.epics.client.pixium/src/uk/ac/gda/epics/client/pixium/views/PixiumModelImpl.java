/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package uk.ac.gda.epics.client.pixium.views;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.pixium.IPixiumNXDetector;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.epics.client.views.model.impl.EPICSBaseModel;

public class PixiumModelImpl extends EPICSBaseModel implements InitializingBean, PixiumModel {
	static final Logger logger = LoggerFactory.getLogger(PixiumModelImpl.class);
	private Set<IPixiumViewController> pixiumViewControllers = new HashSet<IPixiumViewController>();
	private PUModeMonitorListener puModeMonitorListener;
	private CalibrationRequiredStateMonitorListener requiredStateMonitorListener;
	private CalibrationRunningStateMonitorListener runningStateMonitorListener;

	public PixiumModelImpl() {
		puModeMonitorListener=new PUModeMonitorListener();
		requiredStateMonitorListener=new CalibrationRequiredStateMonitorListener();
		runningStateMonitorListener=new CalibrationRunningStateMonitorListener();
	}

	@Override
	public void setPUMode(int mode) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(IPixiumNXDetector.PU_MODE, null), mode);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getPUMode() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(IPixiumNXDetector.PU_MODE_RBV,puModeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public short getCalibrationRequiredState() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(IPixiumNXDetector.CALIBRATION_REQUIRED_RBV, requiredStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void calibrate() throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(IPixiumNXDetector.CALIBRATE, null), 1);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public short getCalibrateState() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(IPixiumNXDetector.CALIBRATE_RBV, runningStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void stop() throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(IPixiumNXDetector.CALIBRATE, null), 0);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public boolean registerPixiumViewController(IPixiumViewController controller) {
		return pixiumViewControllers.add(controller);
	}

	@Override
	public boolean removePixiumViewController(IPixiumViewController controller) {
		return pixiumViewControllers.remove(controller);
	}

	private class PUModeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (IPixiumViewController controller : pixiumViewControllers) {
					controller.updatePUMode(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}
	private class CalibrationRequiredStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (IPixiumViewController controller : pixiumViewControllers) {
					controller.updateCalibrationRequiredState(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}
	private class CalibrationRunningStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (IPixiumViewController controller : pixiumViewControllers) {
					controller.updateCalibrationRunningState(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

}
