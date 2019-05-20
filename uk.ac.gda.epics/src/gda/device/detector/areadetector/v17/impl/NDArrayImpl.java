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

package gda.device.detector.areadetector.v17.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDArray;
import gda.device.detector.areadetector.v17.NDPluginBase.DataType;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class NDArrayImpl extends NDBaseImpl implements NDArray, InitializingBean {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	/**
	 * Map that stores the channel against the PV name
	 */
	private Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private String basePVName;

	static final Logger logger = LoggerFactory.getLogger(NDArrayImpl.class);

	@Override
	public byte[] getByteArrayData() throws Exception {
		try {
			Channel ch = getChannel(ARRAY_DATA);
			return EPICS_CONTROLLER.cagetByteArray(ch);
		} catch (Exception ex) {
			logger.warn("problem with getByteArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public float[] getFloatArrayData() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetFloatArray(getChannel(ARRAY_DATA));
		} catch (Exception ex) {
			logger.warn("problem in getFloatArrayData()", ex);
			throw ex;
		}
	}

	/**
	 * @param arrayData
	 *            The arrayData to set
	 * @throws Exception
	 */
	public void setArrayData(int[] arrayData) throws Exception {
		try {
			EPICS_CONTROLLER.caput(getChannel(ARRAY_DATA), arrayData);
		} catch (Exception ex) {
			logger.warn("problem with getArrayData()", ex);
			throw ex;
		}

	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'deviceName','basePVName' or 'pvProvider' needs to be declared");
		}
		if (getPluginBase() == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be declared");
		}

	}

	/**
	 * @return Returns the basePVName. getBasePVName
	 */
	public String getBasePVName() {
		return basePVName;
	}

	/**
	 * @param basePVName
	 *            The basePVName to set.
	 */
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	/**
	 * This method allows to toggle between the method in which the PV is acquired.
	 *
	 * @param pvElementName
	 * @param args
	 * @return {@link Channel} to talk to the relevant PV.
	 * @throws Exception
	 */
	private Channel getChannel(String pvElementName, String... args) throws Exception {
		try {
			String pvPostFix = null;
			if (args.length > 0) {
				// PV element name is different from the pvPostFix
				pvPostFix = args[0];
			} else {
				pvPostFix = pvElementName;
			}

			return createChannel(basePVName + pvPostFix);
		} catch (Exception exception) {
			logger.warn("Problem getting channel", exception);
			throw exception;
		}
	}

	public Channel createChannel(String fullPvName) throws CAException, TimeoutException {
		Channel channel = channelMap.get(fullPvName);
		if (channel == null) {
			try {
				channel = EPICS_CONTROLLER.createChannel(fullPvName);
			} catch (CAException cae) {
				logger.warn("Problem creating channel", cae);
				throw cae;
			} catch (TimeoutException te) {
				logger.warn("Problem creating channel", te);
				throw te;

			}
			channelMap.put(fullPvName, channel);
		}
		return channel;
	}

	@Override
	public void reset() throws Exception {
		getPluginBase().reset();
	}

	@Override
	public byte[] getByteArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetByteArray(getChannel(ARRAY_DATA), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with getByteArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public short[] getShortArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetShortArray(getChannel(ARRAY_DATA), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with cagetShortArray()", ex);
			throw ex;
		}
	}

	@Override
	public int[] getIntArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetIntArray(getChannel(ARRAY_DATA), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with cagetIntArray()", ex);
			throw ex;
		}
	}

	@Override
	public float[] getFloatArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetFloatArray(getChannel(ARRAY_DATA), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with cagetFloatArray()", ex);
			throw ex;
		}
	}

	@Override
	public Object getImageData(int expectedNumPixels) throws Exception {
		Channel ch = getChannel(ARRAY_DATA);
		return EPICS_CONTROLLER.getDBR(ch, ch.getFieldType(),expectedNumPixels).getValue();
	}

	@Override
	public double[] getDoubleArrayData(int numberOfElements) throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ARRAY_DATA), numberOfElements);
		} catch (Exception ex) {
			logger.warn("problem with cagetDoubleArray()", ex);
			throw ex;
		}
	}

	@Override
	public double[] getDoubleArrayData() throws Exception {
		try {
			return EPICS_CONTROLLER.cagetDoubleArray(getChannel(ARRAY_DATA));
		} catch (Exception ex) {
			logger.warn("problem in getFloatArrayData()", ex);
			throw ex;
		}
	}

	@Override
	public DataType getDataType() throws Exception {
		String label = null;
		try {
			label = EPICS_CONTROLLER.cagetString(getChannel(DATA_TYPE_RBV));
		} catch (Exception e) {
			logger.error("Failed to get data type from EPICS", e);
			throw e;
		}

		try {
			return DataType.valueOf(label.toUpperCase());
		} catch (IllegalArgumentException e) {
			logger.error("Data type returned from EPICS was not matched.", e);
			throw e;
		}
	}

}
