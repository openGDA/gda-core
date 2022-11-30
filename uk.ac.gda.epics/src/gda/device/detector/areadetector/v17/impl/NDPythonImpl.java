/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.AreaDetectorException;
import gda.device.detector.areadetector.v17.NDPython;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBRType;

public class NDPythonImpl extends NDBaseImpl implements NDPython, InitializingBean {

	private final Map<String, Channel> channelMap = new HashMap<>();

	private Map<String, String> parameterMap = null;

	private String basePVName;

	private Channel getChannel(String pvSuffix) throws CAException, TimeoutException {
		final String pvName = getBasePVName() + pvSuffix;
		Channel channel = channelMap.get(pvName);
		if (channel == null) {
			channel = EpicsController.getInstance().createChannel(pvName);
			channelMap.put(pvName, channel);
		}
		return channel;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName.endsWith(":") ? basePVName : basePVName + ":";
	}

	@Override
	public Map<String, String> getPythonParameters() {
		return parameterMap;
	}

	@Override
	public void setPythonParameters(Map<String, String> pythonParameters) {
		this.parameterMap = pythonParameters;
	}

	@Override
	public String getFilename_RBV() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetString(getChannel(Filename_RBV));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython file name RBV", e);
		}
	}

	@Override
	public String getFilename() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetString(getChannel(Filename));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython file name", e);
		}
	}

	@Override
	public void setFilename(String filename) throws AreaDetectorException {
		try {
			EpicsController.getInstance().caput(getChannel(Filename), (filename + "\0").getBytes());
		} catch (Exception e) {
			throw new AreaDetectorException("Error setting ADPython file name", e);
		}
	}

	@Override
	public String getClassname_RBV() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetString(getChannel(Classname_RBV));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython class name RBV", e);
		}
	}

	@Override
	public String getClassname() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetString(getChannel(Classname));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython class name", e);
		}
	}

	@Override
	public void setClassname(String classname) throws AreaDetectorException {
		try {
			EpicsController.getInstance().caput(getChannel(Classname), (classname + "\0").getBytes());
		} catch (Exception e) {
			throw new AreaDetectorException("Error setting ADPython class name", e);
		}
	}

	@Override
	public void readFile() throws AreaDetectorException {
		try {
			EpicsController.getInstance().caput(getChannel(ReadFile), 1);
		} catch (Exception e) {
			throw new AreaDetectorException("Error reading ADPython file", e);
		}
	}

	@Override
	public double getRunTime() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetDouble(getChannel(Time_RBV));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython run time", e);
		}
	}

	@Override
	public int getStatus() throws AreaDetectorException {
		try {
			return EpicsController.getInstance().cagetInt(getChannel(PluginState_RBV));
		} catch (Exception e) {
			throw new AreaDetectorException("Error getting ADPython status", e);
		}
	}

	@Override
	public void afterPropertiesSet() throws AreaDetectorException {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		if (parameterMap == null) {
			throw new IllegalArgumentException("'parameterMap' must be specified");
		}
	}

	@Override
	public void putParam(String parameter, Object value) throws AreaDetectorException {
		final Map<String, String> paramNames = getPythonParameters();
		if (!paramNames.containsKey(parameter)) {
			throw new IllegalArgumentException("Unrecognised parameter " + parameter);
		}
		try {
			final Channel ch = getChannel(paramNames.get(parameter));
			final EpicsController controller = EpicsController.getInstance();
			if (value instanceof byte[]) controller.caput(ch, (byte[]) value);
			else if (value instanceof Byte) controller.caput(ch, (byte) value);
			else if (value instanceof short[]) controller.caput(ch, (short[]) value);
			else if (value instanceof Short) controller.caput(ch, (short) value);
			else if (value instanceof int[]) controller.caput(ch, (int[]) value);
			else if (value instanceof Integer) controller.caput(ch, (int) value);
			else if (value instanceof float[]) controller.caput(ch, (float[]) value);
			else if (value instanceof Float) controller.caput(ch, (float) value);
			else if (value instanceof double[]) controller.caput(ch, (double[]) value);
			else if (value instanceof Double) controller.caput(ch, (double) value);
			else if (value instanceof String[]) controller.caput(ch, (String[]) value);
			else if (value instanceof String) controller.caput(ch, (String) value);
			else {
				throw new IllegalArgumentException("Unhandled parameter type " + value.getClass() + " from " + value);
			}
		} catch (Exception e) {
			throw new AreaDetectorException("Error putting parameter " + parameter, e);
		}
	}

	@Override
	public String readParam(String parameter) throws AreaDetectorException {
		try {
			return EpicsController.getInstance().caget(getChannel(getPythonParameters().get(parameter)));
		} catch (Exception e) {
			throw new AreaDetectorException("Error reading parameter " + parameter, e);
		}
	}

	@Override
	public Object readParam(String parameter, DBRType type) throws AreaDetectorException {
		final Map<String, String> paramNames = getPythonParameters();
		if (!paramNames.containsKey(parameter)) {
			throw new IllegalArgumentException("Unrecognised parameter " + parameter);
		}
		try {
			final Channel ch = getChannel(paramNames.get(parameter));
			final EpicsController controller = EpicsController.getInstance();
			if (type == DBRType.BYTE) {
				return controller.cagetByteArray(ch);
			} else if (type == DBRType.DOUBLE) {
				return controller.cagetDoubleArray(ch);
			} else if (type == DBRType.FLOAT) {
				return controller.cagetFloatArray(ch);
			} else if (type == DBRType.INT) {
				return controller.cagetIntArray(ch);
			} else if (type == DBRType.SHORT) {
				return controller.cagetShortArray(ch);
			} else if (type == DBRType.STRING) {
				return controller.cagetStringArray(ch);
			}
		} catch (Exception e) {
			throw new AreaDetectorException("Error reading parameter " + parameter, e);
		}
		throw new IllegalArgumentException(String.format("Unhandled parameter type %s for parameter %s", type, parameter));
	}
}
