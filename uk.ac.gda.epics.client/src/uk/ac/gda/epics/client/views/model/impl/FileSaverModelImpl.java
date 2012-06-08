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

package uk.ac.gda.epics.client.views.model.impl;

import gda.device.detector.areadetector.v17.NDFile;
import gda.device.detector.areadetector.v17.NDPluginBase;
import gda.epics.interfaces.NDFileType;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.client.views.controllers.IFileSaverViewController;
import uk.ac.gda.epics.client.views.model.FileSaverModel;

/**
 *
 */
public class FileSaverModelImpl extends EPICSBaseModel<NDFileType> implements FileSaverModel {
	static final Logger logger = LoggerFactory.getLogger(FileSaverModelImpl.class);

	private Set<IFileSaverViewController> fileSaverViewController = new HashSet<IFileSaverViewController>();

	private Dim1SizeMonitorListener dim1SizeMonitorListener;
	private Dim0SizeMonitorListener dim0SizeMonitorListener;
	private TimeStampMonitorListener timestampMonitorListener;
	private CaptureStateMonitorListener captureStateMonitorListener;

	@Override
	public boolean registerFileSaverViewController(IFileSaverViewController adBaseViewController) {
		return fileSaverViewController.add(adBaseViewController);
	}

	@Override
	public boolean removeFileSaverViewController(IFileSaverViewController adBaseViewController) {
		return fileSaverViewController.remove(adBaseViewController);
	}

	public FileSaverModelImpl() {
		dim0SizeMonitorListener = new Dim0SizeMonitorListener();
		dim1SizeMonitorListener = new Dim1SizeMonitorListener();
		timestampMonitorListener = new TimeStampMonitorListener();
		captureStateMonitorListener = new CaptureStateMonitorListener();
	}

	private class CaptureStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (IFileSaverViewController controller : fileSaverViewController) {
					controller.updateFileSaverCaptureState(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	private class Dim0SizeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (IFileSaverViewController controller : fileSaverViewController) {
					controller.updateFileSaveX(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getDim0Size() throws Exception {
		try {
			if (getPluginBaseTypeConfig() != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(getPluginBaseTypeConfig().getArraySize0_RBV().getPv(),
						dim0SizeMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDPluginBase.ArraySize0_RBV, dim0SizeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class Dim1SizeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (IFileSaverViewController controller : fileSaverViewController) {
					controller.updateFileSaveY(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	@Override
	public int getDim1Size() throws Exception {
		try {
			if (getPluginBaseTypeConfig() != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(getPluginBaseTypeConfig().getArraySize1_RBV().getPv(),
						dim1SizeMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDPluginBase.ArraySize1_RBV, dim1SizeMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class TimeStampMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (IFileSaverViewController controller : fileSaverViewController) {
					controller.updateFileSaveTimeStamp(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	@Override
	public double getTimeStamp() throws Exception {
		try {
			if (getPluginBaseTypeConfig() != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(getPluginBaseTypeConfig().getTimeStamp_RBV().getPv(),
						timestampMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDPluginBase.TimeStamp_RBV, timestampMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	protected Class<NDFileType> getConfigClassType() {
		return NDFileType.class;
	}

	@Override
	protected void doCheckAfterPropertiesSet() throws Exception {
		if (deviceName != null && pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be declared");
		}
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	public short getCaptureState() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getCapture_RBV().getPv(),
						captureStateMonitorListener));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(NDFile.Capture_RBV, captureStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

}
