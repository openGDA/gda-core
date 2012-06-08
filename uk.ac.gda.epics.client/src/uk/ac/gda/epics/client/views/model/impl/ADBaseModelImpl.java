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

import gda.device.detector.areadetector.v17.ADBase;
import gda.epics.interfaces.ADBaseType;
import gda.epics.interfaces.NDPluginBaseType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;

/**
 *
 */
public class ADBaseModelImpl extends EPICSBaseModel<ADBaseType> implements InitializingBean, AdBaseModel {
	static final Logger logger = LoggerFactory.getLogger(ADBaseModelImpl.class);
	private static List<String> detectorDataTypes;

	@Override
	protected Class<ADBaseType> getConfigClassType() {
		return ADBaseType.class;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	private Set<IAdBaseViewController> adBaseViewControllers = new HashSet<IAdBaseViewController>();

	@Override
	public boolean registerAdBaseViewController(IAdBaseViewController adBaseViewController) {
		return adBaseViewControllers.add(adBaseViewController);
	}

	
	@Override
	public boolean removeAdBaseViewController(IAdBaseViewController adBaseViewController) {
		return adBaseViewControllers.remove(adBaseViewController);
	}

	/**/
	private DetectorStateMonitorListener detectorStateMonitorListener;
	private AcqExposureMonitorListener acqExposureMonitorListener;
	private AcqPeriodMonitorListener acqPeriodMonitorListener;
	private ArrayCounterMonitorListener arrayCounterMonitorListener;
	private TimeRemainingMonitorListener timeRemainingMonitorListener;
	private ArrayRateMonitorListener arrayRateMonitorListener;
	private NumImagesCounterMonitorListener numImagesCounterMonitorListener;
	private NumExposuresCounterMonitorListener numExposuresCounterMonitorListener;
	private AcquireStateMonitorListener acquireStateListener;
	private DataTypeMonitorListener dataTypeListener;

	public ADBaseModelImpl() {
		acqPeriodMonitorListener = new AcqPeriodMonitorListener();
		acqExposureMonitorListener = new AcqExposureMonitorListener();
		arrayCounterMonitorListener = new ArrayCounterMonitorListener();
		timeRemainingMonitorListener = new TimeRemainingMonitorListener();
		numExposuresCounterMonitorListener = new NumExposuresCounterMonitorListener();
		numImagesCounterMonitorListener = new NumImagesCounterMonitorListener();
		arrayRateMonitorListener = new ArrayRateMonitorListener();
		detectorStateMonitorListener = new DetectorStateMonitorListener();
		acquireStateListener = new AcquireStateMonitorListener();
		dataTypeListener = new DataTypeMonitorListener();
	}

	private class AcquireStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isENUM()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateAcquireState(((DBR_Enum) dbr).getEnumValue()[0]);
				}
			}
		}
	}

	private class DataTypeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isENUM()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					short enumVal = ((DBR_Enum) dbr).getEnumValue()[0];
					try {
						controller.updateDetectorDataType(getDataTypeList().get(enumVal));
					} catch (Exception e) {
						logger.error("Problem getting datatype list", e);
					}
				}
			}
		}
	}

	private class AcqExposureMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isDOUBLE()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateAcqExposure(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class AcqPeriodMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isDOUBLE()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateAcqPeriod(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class ArrayCounterMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateArrayCounter(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class ArrayRateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isDOUBLE()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateArrayRate(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class TimeRemainingMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isDOUBLE()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateTimeRemaining(((DBR_Double) dbr).getDoubleValue()[0]);
				}
			}
		}
	}

	private class NumExposuresCounterMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateNumberOfExposuresCounter(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class NumImagesCounterMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateNumberOfImagesCounter(((DBR_Int) dbr).getIntValue()[0]);
				}
			}

		}
	}

	private class DetectorStateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr!=null && dbr.isSHORT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateDetectorState(((DBR_Short) dbr).getShortValue()[0]);
				}
			}
		}
	}

	/**
	 * @throws InterruptedException
	 * @throws CAException
	 * @throws TimeoutException
	 */
	@Override
	public short getDetectorState_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getDetectorState_RBV().getPv(),
						detectorStateMonitorListener));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.DetectorState_RBV, detectorStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getArrayCounter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getArrayCounter_RBV().getPv(),
						arrayCounterMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ADBase.ArrayCounter_RBV, arrayCounterMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getTimeRemaining_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getTimeRemaining_RBV().getPv(),
						timeRemainingMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ADBase.TimeRemaining_RBV, timeRemainingMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public double getArrayRate_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getArrayRate_RBV().getPv(),
						arrayRateMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ADBase.ArrayRate_RBV, arrayRateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumExposuresCounter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumExposuresCounter_RBV().getPv(),
						numExposuresCounterMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ADBase.NumExposuresCounter_RBV,
					numExposuresCounterMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	*
	*/
	@Override
	public int getNumImagesCounter_RBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetInt(createChannel(config.getNumImagesCounter_RBV().getPv(),
						numImagesCounterMonitorListener));
			}
			return EPICS_CONTROLLER.cagetInt(getChannel(ADBase.NumImagesCounter_RBV, numImagesCounterMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public void doCheckAfterPropertiesSet() throws Exception {
		// initializeStatusViewValues();

	}

	public void initializeStatusViewValues() throws Exception {
		for (IAdBaseViewController controller : adBaseViewControllers) {
			try {
				int arrayCounter_RBV = getArrayCounter_RBV();
				controller.updateArrayCounter(arrayCounter_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getArrayCounter_RBV - Either the PV is incorrect or the IOC is not in function");
			}
			try {
				double timeRemaining_RBV = getTimeRemaining_RBV();
				controller.updateTimeRemaining(timeRemaining_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getTimeRemaining_RBV - Either the PV is incorrect or the IOC is not in function");
			}

			try {
				int numExposuresCounter_RBV = getNumExposuresCounter_RBV();
				controller.updateNumberOfExposuresCounter(numExposuresCounter_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getNumExposuresCounter_RBV - Either the PV is incorrect or the IOC is not in function");
			}

			try {
				int numImagesCounter_RBV = getNumImagesCounter_RBV();
				controller.updateNumberOfImagesCounter(numImagesCounter_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getNumImagesCounter_RBV - Either the PV is incorrect or the IOC is not in function");
			}

			try {
				double arrayRate_RBV = getArrayRate_RBV();
				controller.updateArrayRate(arrayRate_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getArrayRate_RBV - Either the PV is incorrect or the IOC is not in function");
			}

			try {
				short detectorState_RBV = getDetectorState_RBV();
				controller.updateDetectorState(detectorState_RBV);
			} catch (TimeoutException tme) {
				logger.error("Cannot getDetectorState_RBV - Either the PV is incorrect or the IOC is not in function");
			}
		}
	}

	@Override
	protected NDPluginBaseType getPluginBaseTypeConfig() throws FactoryException {
		throw new FactoryException("No base plugin for ADBase");
	}

	@Override
	public double getAcqExposureRBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquireTime_RBV().getPv(),
						acqExposureMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ADBase.AcquireTime_RBV, acqExposureMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	/**
	 * Sets the exposure time
	 * 
	 * @param exposureTime
	 * @throws Exception
	 */
	@Override
	public void setAcqExposure(double exposureTime) throws Exception {
		try {
			if (config != null) {
				EPICS_CONTROLLER.caput(createChannel(config.getAcquireTime_RBV().getPv(), acqExposureMonitorListener),
						exposureTime);
			}
			EPICS_CONTROLLER.caput(getChannel(ADBase.AcquireTime_RBV, acqExposureMonitorListener), exposureTime);
		} catch (Exception ex) {
			throw ex;
		}

	}

	@Override
	public double getAcqPeriodRBV() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetDouble(createChannel(config.getAcquirePeriod_RBV().getPv(),
						acqPeriodMonitorListener));
			}
			return EPICS_CONTROLLER.cagetDouble(getChannel(ADBase.AcquirePeriod_RBV, acqPeriodMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public short getAcquireState() throws Exception {
		try {
			if (config != null) {
				return EPICS_CONTROLLER.cagetEnum(createChannel(config.getAcquire().getPv(), acquireStateListener));
			}
			return EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.Acquire, acquireStateListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public String getDatatype() throws Exception {
		try {
			if (config != null) {
				return getDataTypeList().get(
						EPICS_CONTROLLER.cagetEnum(createChannel(config.getDataType().getPv(), dataTypeListener)));
			}
			return getDataTypeList().get(EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.DataType, dataTypeListener)));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private List<String> getDataTypeList() throws Exception {
		if (detectorDataTypes == null) {
			String[] labels = null;
			if (config != null) {
				labels = EPICS_CONTROLLER.cagetLabels(createChannel(config.getDataType().getPv(), dataTypeListener));
			} else {
				labels = EPICS_CONTROLLER.cagetLabels(getChannel(ADBase.DataType, dataTypeListener));
			}
			detectorDataTypes = Arrays.asList(labels);
		}
		return detectorDataTypes;
	}
	
}
