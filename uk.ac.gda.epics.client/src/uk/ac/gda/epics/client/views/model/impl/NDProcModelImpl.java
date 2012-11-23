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

import gda.device.detector.areadetector.v17.NDProcess;
import gda.epics.interfaces.NDProcessType;
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

import uk.ac.gda.epics.client.views.controllers.INDProcViewController;
import uk.ac.gda.epics.client.views.model.NdProcModel;

/**
 *
 */
public class NDProcModelImpl extends EPICSBaseModel<NDProcessType> implements NdProcModel {
	private final static Logger logger = LoggerFactory.getLogger(NDProcModelImpl.class);
	private ScaleMonitorListener scaleMonitorListener;
	private OffsetMonitorListener offsetMonitorListener;
	private EnableFlatFieldListener enableFlatFieldListener;
	private NumFilteredListener numFilteredListener;
	private Set<INDProcViewController> viewControllers = new HashSet<INDProcViewController>();

	@Override
	public boolean registerProcViewController(INDProcViewController viewController) {
		return viewControllers.add(viewController);
	}

	@Override
	public boolean removeProcViewController(INDProcViewController viewController) {
		return viewControllers.remove(viewController);
	}

	public NDProcModelImpl() {
		scaleMonitorListener = new ScaleMonitorListener();
		offsetMonitorListener = new OffsetMonitorListener();
		enableFlatFieldListener = new EnableFlatFieldListener();
		numFilteredListener = new NumFilteredListener();
	}

	@Override
	protected Class<NDProcessType> getConfigClassType() {
		return NDProcessType.class;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private class EnableFlatFieldListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (INDProcViewController controller : viewControllers) {
					controller.updateEnableFlatField(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	private class NumFilteredListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (INDProcViewController controller : viewControllers) {
					controller.updateNumFiltered(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class ScaleMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDProcViewController controller : viewControllers) {
					controller.updateProcScale(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	@Override
	public double getProcScale() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getScale_RBV().getPv(), scaleMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDProcess.Scale_RBV, scaleMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getNumFiltered() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER
						.cagetInt(createChannel(config.getNumFiltered_RBV().getPv(), numFilteredListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(NDProcess.NumFiltered_RBV, numFilteredListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private class OffsetMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				for (INDProcViewController controller : viewControllers) {
					controller.updateProcOffset(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	@Override
	public double getProcOffset() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER
						.cagetDouble(createChannel(config.getOffset_RBV().getPv(), offsetMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(NDProcess.Offset_RBV, offsetMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public short getEnableFlatField() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getEnableFlatField().getPv(),
						enableFlatFieldListener));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(NDProcess.EnableFlatField, enableFlatFieldListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	protected void doCheckAfterPropertiesSet() throws Exception {
		if (deviceName != null && pluginBase == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be declared");
		}

	}

	/**
	 * Sets the offset value in EPICS
	 * 
	 * @param offsetValue
	 * @throws Exception
	 */
	@Override
	public void setOffset(double offsetValue) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getOffset().getPv(), offsetMonitorListener), offsetValue);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDProcess.Offset, offsetMonitorListener), offsetValue);
			}
		} catch (Exception ex) {
			throw ex;
		}

	}

	@Override
	public void setScale(double scaleValue) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getScale().getPv(), scaleMonitorListener), scaleValue);
			} else {
				EPICS_CONTROLLER.caput(getChannel(NDProcess.Scale, scaleMonitorListener), scaleValue);
			}
		} catch (Exception ex) {
			throw ex;
		}
	}

}
