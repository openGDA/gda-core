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

import gda.device.detector.areadetector.v17.NDPython;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class NDPythonImpl extends NDBaseImpl implements NDPython, InitializingBean {

	private final static EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private final Map<String, Channel> channelMap = new HashMap<String, Channel>();

	private Map<String, String> parameterMap = null;

	private String basePVName;

	private Channel getChannel(String pvSuffix) throws CAException, TimeoutException {
		String pvName = getBasePVName() + pvSuffix;
		Channel channel = channelMap.get(pvName);
		if (channel == null) {
			channel = EPICS_CONTROLLER.createChannel(pvName);
			channelMap.put(pvName, channel);
		}
		return channel;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
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
	public String getFilename_RBV() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(Filename_RBV));
	}

	@Override
	public String getFilename() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(Filename));
	}

	@Override
	public void setFilename(String filename) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(Filename), (filename + "\0").getBytes());

	}

	@Override
	public String getClassname_RBV() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(Classname_RBV));
	}

	@Override
	public String getClassname() throws Exception {
		return EPICS_CONTROLLER.cagetString(getChannel(Classname));
	}

	@Override
	public void setClassname(String classname) throws Exception {
		EPICS_CONTROLLER.caput(getChannel(Classname), (classname + "\0").getBytes());
	}

	@Override
	public void readFile() throws Exception {
		EPICS_CONTROLLER.caput(getChannel(ReadFile), 1);
	}

	@Override
	public double getRunTime() throws Exception {
		return EPICS_CONTROLLER.cagetDouble(getChannel(Time_RBV));
	}

	@Override
	public int getStatus() throws Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(PluginState_RBV));
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		if (parameterMap == null) {
			throw new IllegalArgumentException("'parameterMap' must be specified");
		}
	}

	@Override
	public void putParam(String parameter, Object value) throws Exception {
		Map<String, String> paramNames = getPythonParameters();
		if (!paramNames.containsKey(parameter)) {
			throw new IllegalArgumentException("Unrecognised parameter " + parameter);
		}
		Channel ch = getChannel(paramNames.get(parameter));
		if (value instanceof byte[]) EPICS_CONTROLLER.caput(ch, (byte[]) value);
		else if (value instanceof Byte) EPICS_CONTROLLER.caput(ch, (byte) value);
		else if (value instanceof short[]) EPICS_CONTROLLER.caput(ch, (short[]) value);
		else if (value instanceof Short) EPICS_CONTROLLER.caput(ch, (short) value);
		else if (value instanceof int[]) EPICS_CONTROLLER.caput(ch, (int[]) value);
		else if (value instanceof Integer) EPICS_CONTROLLER.caput(ch, (int) value);
		else if (value instanceof float[]) EPICS_CONTROLLER.caput(ch, (float[]) value);
		else if (value instanceof Float) EPICS_CONTROLLER.caput(ch, (float) value);
		else if (value instanceof double[]) EPICS_CONTROLLER.caput(ch, (double[]) value);
		else if (value instanceof Double) EPICS_CONTROLLER.caput(ch, (double) value);
		else if (value instanceof String[]) EPICS_CONTROLLER.caput(ch, (String[]) value);
		else if (value instanceof String) EPICS_CONTROLLER.caput(ch, (String) value);
		else {
			throw new IllegalArgumentException("Unhandled parameter type " + value.getClass() + " from " + value);
		}
	}

	@Override
	public String readParam(String paramter) throws Exception {
		return EPICS_CONTROLLER.caget(getChannel(getPythonParameters().get(paramter)));
	}

}
