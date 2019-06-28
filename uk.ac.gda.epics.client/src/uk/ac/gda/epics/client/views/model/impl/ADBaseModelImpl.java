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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.ADBase;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import uk.ac.gda.epics.client.views.controllers.IAdBaseViewController;
import uk.ac.gda.epics.client.views.model.AdBaseModel;

/**
 *
 */
public class ADBaseModelImpl extends EPICSBaseModel implements InitializingBean, AdBaseModel {
	static final Logger logger = LoggerFactory.getLogger(ADBaseModelImpl.class);
	private static List<String> detectorDataTypes;

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
	private NumExposurePerImageMonitorListener numExposuresPerImageMonitorListener;
	private NumImagesMonitorListener numImagesMonitorListener;
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
		numExposuresPerImageMonitorListener = new NumExposurePerImageMonitorListener();
		numImagesMonitorListener = new NumImagesMonitorListener();
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
			if (dbr != null && dbr.isENUM()) {
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
			if (dbr != null && dbr.isDOUBLE()) {
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
			if (dbr != null && dbr.isDOUBLE()) {
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
			if (dbr != null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateArrayCounter(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}
	private class NumExposurePerImageMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateNumExposures(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class NumImagesMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isINT()) {
				for (IAdBaseViewController controller : adBaseViewControllers) {
					controller.updateNumImages(((DBR_Int) dbr).getIntValue()[0]);
				}
			}
		}
	}

	private class ArrayRateMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent arg0) {
			DBR dbr = arg0.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
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
			if (dbr != null && dbr.isDOUBLE()) {
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
			if (dbr != null && dbr.isINT()) {
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
			if (dbr != null && dbr.isINT()) {
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
			if (dbr != null && dbr.isSHORT()) {
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
			return EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.DetectorState_RBV, detectorStateMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getArrayCounter_RBV() throws Exception {
		try {
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
	public double getAcqExposureRBV() throws Exception {
		try {
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
			EPICS_CONTROLLER.caput(getChannel(ADBase.AcquireTime, null), exposureTime);
		} catch (Exception ex) {
			throw ex;
		}

	}

	@Override
	public double getAcqPeriodRBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDouble(getChannel(ADBase.AcquirePeriod_RBV, acqPeriodMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}
	@Override
	public void setAcqPeriod(double periodTime) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ADBase.AcquirePeriod, null), periodTime);
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public short getAcquireState() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.Acquire, acquireStateListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public String getDatatype() throws Exception {
		try {
			return getDataTypeList().get(EPICS_CONTROLLER.cagetEnum(getChannel(ADBase.DataType, dataTypeListener)));
		} catch (Exception ex) {
			throw ex;
		}
	}

	private List<String> getDataTypeList() throws Exception {
		if (detectorDataTypes == null) {
			detectorDataTypes = Arrays.asList(EPICS_CONTROLLER.cagetLabels(getChannel(ADBase.DataType, dataTypeListener)));
		}
		return detectorDataTypes;
	}

	@Override
	public String getPortName() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetString(getChannel(ADBase.PortName_RBV, null));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getNumberOfExposuresPerImage_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ADBase.NumExposures_RBV,
					numExposuresPerImageMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

	@Override
	public int getNumberOfImages_RBV() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetInt(getChannel(ADBase.NumImages_RBV,
					numImagesMonitorListener));
		} catch (Exception ex) {
			throw ex;
		}
	}

}
