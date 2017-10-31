/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.epics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.connection.TIMEHandler;
import gda.epics.util.JCAUtils;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_CTRL_Enum;
import gov.aps.jca.dbr.DOUBLE;
import gov.aps.jca.dbr.ENUM;
import gov.aps.jca.dbr.FLOAT;
import gov.aps.jca.dbr.INT;
import gov.aps.jca.dbr.LABELS;
import gov.aps.jca.dbr.PRECISION;
import gov.aps.jca.dbr.SHORT;
import gov.aps.jca.dbr.STRING;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutListener;

/**
 * The scripting interface between Jython and EPICS. CAClient provides client-side implementation of caget(), caput(),
 * and camonitor() for accessing EPICS PVs directly from Jython scripts or Jython Terminal.
 */
public class CAClient extends EpicsBase implements Epics, MonitorListener, ConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(CAClient.class);

	private Channel theChannel = null;

	private Channel[] chs = null;

	private boolean configured = false;

	EpicsController controller = EpicsController.getInstance();
	EpicsChannelManager channelmanager = new EpicsChannelManager();

	/**
	 * Converts a DBR object from a MonitorEvent to a string
	 *
	 * @param dbr
	 * @return string
	 */
	public static String value2String(DBR dbr) {
		String value = null;

		if (dbr.isBYTE())
			value = String.valueOf(((BYTE) dbr).getByteValue()[0]);
		else if (dbr.isDOUBLE())
			value = String.valueOf(((DOUBLE) dbr).getDoubleValue()[0]);
		else if (dbr.isENUM())
			value = String.valueOf(((ENUM) dbr).getEnumValue()[0]);
		else if (dbr.isFLOAT())
			value = String.valueOf(((FLOAT) dbr).getFloatValue()[0]);
		else if (dbr.isINT())
			value = String.valueOf(((INT) dbr).getIntValue()[0]);
		else if (dbr.isLABELS())
			value = String.valueOf(((LABELS) dbr).getLabels()[0]);
		else if (dbr.isPRECSION())
			value = String.valueOf(((PRECISION) dbr).getPrecision());
		else if (dbr.isSHORT())
			value = String.valueOf(((SHORT) dbr).getShortValue()[0]);
		else if (dbr.isSTRING())
			value = String.valueOf(((STRING) dbr).getStringValue()[0]);
		return value;
	}

	// ////////// Constructor and methods for Jython Terminal Command-line // /////////
	/**
	 * Constructor
	 */
	public CAClient() {
		super();
	}

	/**
	 * Gets the EpicsController of this client
	 *
	 * @return the Epics controller
	 */

	public EpicsController getController() {

		return this.controller;
	}

	/**
	 * Gets the channel of this client
	 *
	 * @return the Epics channel
	 */

	public Channel getChannel() {

		return this.theChannel;
	}

	/**
	 * Gets the channels of this client
	 *
	 * @return the Epics channels
	 */

	public Channel[] getChannels() {

		return this.chs;
	}

	/**
	 * Gets the value of the specified PV.
	 *
	 * @param pv
	 *            the EPICS PV name
	 * @return the value of the PV in String format
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String caget(String pv) throws CAException, TimeoutException, InterruptedException {
		Channel channel = null;
		String value;
		channel = channelmanager.createChannel(pv);
		value = controller.caget(channel);
		return value;
	}

	/**
	 * @param pv
	 *            the EPICS PV name
	 * @return Element Count
	 * @throws CAException
	 */
	public int getElementCount(String pv) throws CAException {
		Channel channel = null;
		int value;
		channel = channelmanager.createChannel(pv);
		value = channel.getElementCount();
		return value;
	}

	/**
	 * @param pv
	 * @return String[]
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public String[] cagetWithTimeStamp(String pv) throws CAException, TimeoutException, InterruptedException {
		Channel ch = null;
		String[] value = new String[3];
		DBR dbr = null;
		ch = channelmanager.createChannel(pv);
		dbr = controller.caget(ch, MonitorType.TIME);
		value[0] = ch.getName();
		value[1] = timeStamp2String(dbr);
		value[2] = value2String(dbr);

		return value;
	}
	private static String DBRtoString(DBR dbr) {
		String valStr = null;
		Object _value= dbr.getValue();
		if (_value instanceof double[]) {
			valStr = "";
			for (double val : (double[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof float[]) {
			valStr = "";
			for (float val : (float[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof short[]) {
			valStr = "";
			for (short val : (short[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof int[]) {
			valStr = "";
			for (int val : (int[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof byte[]) {
			valStr = "";
			for (byte val : (byte[]) _value) {
				valStr += val + ":";
			}
		}
		if (_value instanceof String[]) {
			valStr = "";
			for (String val : (String[]) _value) {
				valStr += val + ":";
			}
		}
		if (valStr != null) {
			if (valStr.endsWith(":")) {
				valStr = valStr.substring(0, valStr.length() - 1);
			}
		}
		if (valStr == null)
			valStr = _value.toString();
		return valStr;
	}

	/*
	 * Simple method to get a value as a String. Inefficient but convenient for command line use
	 */
	static public String get(String pv) throws CAException, TimeoutException, InterruptedException{
		Channel channel=null;
		EpicsController epicsController = EpicsController.getInstance();
		try{
			channel = epicsController.createChannel(pv);
			DBR dbr = epicsController.getCTRL(channel);
			String valStr = null;
			if (dbr instanceof DBR_CTRL_Enum) {
				valStr = ((DBR_CTRL_Enum) dbr).getLabels()[((DBR_CTRL_Enum) dbr).getEnumValue()[0]];
			} else {
				if (dbr.getValue() instanceof byte[]) {
					// convert to ASCII string
					byte[] data = (byte[])dbr.getValue();
					int length = 0;
					for (; length < dbr.getCount(); length++) {
						if (data[length] == 0)
							break;
					}
					valStr = new String(data, 0, length);
				} else {
					valStr = DBRtoString(dbr);
				}
			}
			return valStr;
		} finally{
			if( channel != null)
				epicsController.destroy(channel);
		}
	}

	/*
	 * Simple method to get a value as a String. Inefficient but convenient for command line use
	 */
	static public DBR getDBR(String pv) throws CAException, TimeoutException, InterruptedException{
		Channel channel=null;
		EpicsController epicsController = EpicsController.getInstance();
		try{
			channel = epicsController.createChannel(pv);
			return epicsController.getCTRL(channel);
		} finally{
			if( channel != null)
				epicsController.destroy(channel);
		}
	}


	/*
	 * Simple method to put a value as a String. Inefficient but convenient for command line use
	 */
	static public void put(String pv, String value) throws CAException, InterruptedException, FactoryException{
		CAClient caClient = new CAClient(pv);
		try{
			caClient.configure();
			caClient.caput(value);
		} finally{
			caClient.clearup();
		}
	}
	/*
	 * Simple method to put a value as a String to a ByteArray PV. Inefficient but convenient for command line use
	 */
	static public void putStringAsWaveform(String pv, String value) throws CAException, InterruptedException, FactoryException{
		CAClient caClient = new CAClient(pv);
		try{
			caClient.configure();
			caClient.caput((value+'\0').getBytes());
		} finally{
			caClient.clearup();
		}
	}

	/*
	 * Simple method to put a value as a Double. Inefficient but convenient for command line use
	 */
	static public void put(String pv, Double value) throws CAException, InterruptedException, FactoryException{
		CAClient caClient = new CAClient(pv);
		try{
			caClient.configure();
			caClient.caput(value);
		} finally{
			caClient.clearup();
		}
	}
	/*
	 * Simple method to put a value as an Integer. Inefficient but convenient for command line use
	 */
	static public void put(String pv, Integer value) throws CAException, InterruptedException, FactoryException{
		CAClient caClient = new CAClient(pv);
		try{
			caClient.configure();
			caClient.caput(value);
		} finally{
			caClient.clearup();
		}
	}

	/**
	 * Gets values of a list of PVs from EPICS servers.
	 *
	 * @param pvs
	 *            the list of PVs
	 * @return the values of these PVs
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String[] caget(String... pvs) throws CAException, TimeoutException, InterruptedException {
		String[] values = new String[pvs.length];
		for (int i = 0; i < pvs.length; i++)
			values[i] = caget(pvs[i]);
		return values;
	}

	/**
	 * @param pv
	 * @return String[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String[] cagetArray(String pv) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		int elementCount = channel.getElementCount();
		String[] values = new String[elementCount];
		values = controller.caget(channel, elementCount);
		return values;
	}


	/**
	 * adding a specified CA monitor listener to the PV specified, users need to specify a proper monitor handler that implement
	 * MonitorChanged(MonitorEvent ev) method.
	 *
	 * This only works when a channel is already created in GDA, i.e. in channel cache.
	 * @deprecated This is not a realisable way to add a {@link MonitorListener} to a channel as it does not wait for channel connection callback before added monitor listener.
	 * Use {@link #addMonitorListener(String, MonitorListener)} instead.
	 *
	 * Note {@link Monitor} is not the same as {@link MonitorListener}. {@link Monitor} is just a client side object storing monitored info and give access to instances of {@link MonitorListener} attached to a channel.
	 *
	 * @param ml
	 * @return Monitor
	 * @throws CAException
	 */
	@Deprecated
	public Monitor camonitor(String pv, MonitorListener ml) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		return controller.setMonitor(channel, ml);
	}

	/**
	 * start the default CA monitor Listener to this PV, the default behaviour is the monitor prints PV name and value to
	 * Jython Terminal.
	 *
	 * This only works when a channel is already created in GDA, i.e. in channel cache.
	 * @deprecated This is not a realisable way to add a {@link MonitorListener} to a channel as it does not wait for channel connection callback before added monitor listener.
	 * Use {@link #addMonitorListener(String)} instead.
	 *
	 * @return Monitor
	 * @throws CAException
	 */
	@Deprecated
	public Monitor camonitor(String pv) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		return controller.setMonitor(channel, this);
	}
	/**
	 * adding a specified CA monitor listener to the PV specified, users need to specify a proper monitor handler that implement
	 * MonitorChanged(MonitorEvent ev) method. MonitorListener is added on channel connection callback.
	 *
	 * @param ml
	 * @throws CAException
	 */
	public void addMonitorListener(String pv, MonitorListener ml) throws CAException {
		channelmanager.createChannel(pv, ml);
	}

	/**
	 * Adding this object as the default monitor Listener to the specified PV. The default behaviour is the monitor prints PV name and value to
	 * Jython Terminal. This MonitorListener is added on channel connection callback.
	 *
	 * @throws CAException
	 */
	public void addMonitorListener(String pv) throws CAException {
		channelmanager.createChannel(pv, this);
	}
	/**
	 * Sets the value of the specified PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @throws CAException
	 */
	public void caput(String pv, int value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}

	/**
	 * Sets the value of the specified PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @throws CAException
	 */
	public void caput(String pv, short value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}

	/**
	 * Sets the value of the specified PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @throws CAException
	 */
	public void caput(String pv, float value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}

	/**
	 * Sets the value of the specified PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @throws CAException
	 */
	public void caput(String pv, double value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}

	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, String value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);

	}

	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void caput(String pv, byte[] value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);

	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, int[] value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, short[] value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, float[] value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, double[] value, PutListener lstnr ) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}

	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, byte[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);

	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, int[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, short[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, float[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}
	/**
	 * Sets the value of the PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	public void caput(String pv, double[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}
	/**
	 * Sets a list of values to the specified list of PVs, respectively.
	 *
	 * @param pvs
	 *            this list of PVs
	 * @param values
	 *            the list of values
	 * @throws CAException
	 */
	public void caput(String[] pvs, double[] values) throws CAException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i]);
	}

	/**
	 * Sets a list of values to the specified list of PVs, respectively.
	 *
	 * @param pvs
	 *            this list of PVs
	 * @param values
	 *            the list of values
	 * @throws CAException
	 */
	public void caput(String[] pvs, byte[] values) throws CAException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i]);
	}

	/**
	 * Sets a list of values to the specified list of PVs, respectively.
	 *
	 * @param pvs
	 *            this list of PVs
	 * @param values
	 *            the list of values
	 * @throws CAException
	 */
	public void caput(String[] pvs, String[] values) throws CAException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i]);
	}

	/**
	 * Sets the value of the specified PV.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public void caput(String pv, double value, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String pv, int value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String pv, float value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String pv, short value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}
	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String pv, double value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}

	/**
	 * Sets the value of the PV and wait for callback.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(String pv, String value, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, timeoutinsecond);

	}

	public void caput(String pv, double value, boolean wait) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caputWait(channel, value);
		} else {
			controller.caput(channel, value);
		}
	}
	public void caput(String pv, double value, boolean wait, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caput(channel, value, timeoutinsecond);
		} else {
			controller.caput(channel, value);
		}
	}
	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value of this PV
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String pv, String value, PutListener lstnr) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value, lstnr);
	}

	/**
	 * Sets a list of values to the specified list of PVs, respectively and wait for callback.
	 *
	 * @param pvs
	 *            this list of PVs
	 * @param values
	 *            the list of values
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(String[] pvs, double[] values, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pvs
	 *            the PV name array
	 * @param values
	 *            the value array of these corresponding PVs
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String[] pvs, double[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i], lstnr);
	}

	/**
	 * Sets a list of values to the specified list of PVs, respectively and wait for callback.
	 *
	 * @param pvs
	 *            this list of PVs
	 * @param values
	 *            the list of values
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(String[] pvs, String[] values, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value of the specified PV with put call back listener, this listener must implemenets
	 * putCompleted(PutEvent pe) method to handle the call-back.
	 *
	 * @param pvs
	 *            the PV name array
	 * @param values
	 *            the value array of these corresponding PVs
	 * @param lstnr
	 *            the put listener
	 * @throws CAException
	 */
	public void caput(String[] pvs, String[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < pvs.length; i++)
			caput(pvs[i], values[i], lstnr);
	}
	/**
	 * caput a string value as waveform to the corresponding PVs in EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caputStringAsWaveform(String pv, String value) throws CAException, InterruptedException {
		int[] waveform = JCAUtils.getIntArrayFromWaveform(value);
		caput(pv, waveform);
	}

	/**
	 * Asynchronously sets a string as waveform to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 *            the input string value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caputStringAsWaveform(String pv, String value, PutListener lstnr) throws CAException, InterruptedException {
		int[] waveform = JCAUtils.getIntArrayFromWaveform(value);
		caput(pv, waveform, lstnr);
	}

	// ///////// Constructor and Methods for jython scripting as scannable objects ///////////

	/**
	 * Constructor that initialises a PV name.
	 *
	 * @param pv
	 */
	public CAClient(String pv) {
		this.pvName = pv;
	}

	/**
	 * Constructor that initialises a list of PV names.
	 *
	 * @param pvs
	 */
	public CAClient(String... pvs) {
		for (int i = 0; i < pvs.length; i++) {
			this.pvNames.add(pvs[i]);
		}
	}

	/**
	 * configure the channel access for EPICS PV or PVs. This method must be called before caget() or caput().
	 *
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		try {
			super.configure();
			if (!configured) {
				if (pvName != null) {
//					theChannel = controller.createChannel(pvName,this);
					theChannel = channelmanager.createChannel(pvName);
				} else if (pvNames != null) {
					chs = new Channel[pvNames.size()];
					for (int i = 0; i < pvNames.size(); i++) {
//						chs[i] = controller.createChannel(pvNames.get(i),this);
						chs[i] = channelmanager.createChannel(pvNames.get(i),this);
					}
				} else {
					logger.info("No PV strings are given.");
				}
				configured = true;
			}
		} catch (Throwable th) {
			throw new FactoryException("failed to configure", th);
		}

	}

	/**
	 * adding a specified CA monitor to this Channel, users need to specify a proper monitor handler that implement
	 * MonitorChanged(MonitorEvent ev) method.
	 *
	 * @param ml
	 * @return Monitor
	 * @throws CAException
	 */
	public Monitor camonitor(MonitorListener ml) throws CAException, InterruptedException {
		return controller.setMonitor(theChannel, ml);
	}
	public Monitor camonitor(MonitorListener ml, int count) throws CAException, InterruptedException {
		return controller.setMonitor(theChannel, ml, count);
	}

	/**
	 * start the default CA monitor to this Channel, the default behaviour is the monitor prints PV name and value to
	 * Jython Terminal.
	 *
	 * @return Monitor
	 * @throws CAException
	 */
	public Monitor camonitor() throws CAException, InterruptedException {
		return controller.setMonitor(theChannel, this);
	}

	/**
	 * Remove a monitor from this channel
	 *
	 * @param m
	 */
	public void removeMonitor(Monitor m) {
		controller.clearMonitor(m);
	}

	/**
	 * Gets the value from EPICS Server.
	 *
	 * @return the value
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String caget() throws CAException, TimeoutException, InterruptedException {
		return controller.caget(theChannel);
	}

	/**
	 * @param rtype
	 * @return DBR
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public DBR caget(MonitorType rtype) throws CAException, TimeoutException, InterruptedException {
		return controller.caget(theChannel, rtype);
	}

	/**
	 * @return String[]
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public String[] cagetWithTimeStamp() throws CAException, TimeoutException, InterruptedException {
		DBR dbr = caget(MonitorType.TIME);
		String[] value = new String[3];
		value[0] = theChannel.getName();
		value[1] = timeStamp2String(dbr);
		value[3] = value2String(dbr);

		return value;
	}

	/**
	 * @return String[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String[] cagetArray() throws CAException, TimeoutException, InterruptedException {
		int elementCount = theChannel.getElementCount();
		String[] values = new String[elementCount];
		values = controller.caget(theChannel, elementCount);
		return values;
	}

	/**
	 * @return double[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public double[] cagetArrayDouble() throws CAException, TimeoutException, InterruptedException {
		double[] values = controller.cagetDoubleArray(theChannel);
		return values;
	}

	/**
	 * @param numberOfElements
	 * @return int[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int[] cagetArrayUnsigned(int numberOfElements) throws CAException, TimeoutException, InterruptedException {
		byte[] values = controller.cagetByteArray(theChannel, numberOfElements);
		int[] uvalues = new int[values.length];
		for(int i=0; i<values.length; i++){
			uvalues[i] = values[i]&0xff;
		}
		return uvalues;
	}

	/**
	 * @return int[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int[] cagetArrayUnsigned() throws CAException, TimeoutException, InterruptedException {
		byte[] values = controller.cagetByteArray(theChannel);
		int[] uvalues = new int[values.length];
		for(int i=0; i<values.length; i++){
			uvalues[i] = values[i]&0xff;
		}
		return uvalues;
	}



	/**
	 * @param numberOfElements
	 * @return byte[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public byte[] cagetArrayByte(int numberOfElements) throws CAException, TimeoutException, InterruptedException {
		byte[] values = controller.cagetByteArray(theChannel, numberOfElements);
		return values;
	}

	/**
	 * @return byte[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public byte[] cagetArrayByte() throws CAException, TimeoutException, InterruptedException {
		byte[] values = controller.cagetByteArray(theChannel);
		return values;
	}

	/**
	 * @param numberOfElements
	 * @return int[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int[] cagetArrayInt(int numberOfElements) throws CAException, TimeoutException, InterruptedException {
		int[] values = controller.cagetIntArray(theChannel, numberOfElements);
		return values;
	}

	/**
	 * @return int[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int[] cagetArrayInt() throws CAException, TimeoutException, InterruptedException {
		int[] values = controller.cagetIntArray(theChannel);
		return values;
	}

	/**
	 * @param numberOfElements
	 * @return double[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public double[] cagetArrayDouble(int numberOfElements) throws CAException, TimeoutException, InterruptedException {
		double[] values = controller.cagetDoubleArray(theChannel, numberOfElements);
		return values;
	}

	/**
	 * @param numberOfElements
	 * @return double[]
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public float[] cagetArrayFloat(int numberOfElements) throws CAException, TimeoutException, InterruptedException {
		float[] values = controller.cagetFloatArray(theChannel, numberOfElements);
		return values;
	}
	/**
	 * @return double
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public double cagetMax() throws CAException, TimeoutException, InterruptedException {
		return max(cagetArrayDouble());
	}

	/**
	 * @return int
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public int cagetPeakPosition() throws CAException, TimeoutException, InterruptedException {
		return peakPosition(cagetArrayDouble());
	}

	/**
	 * Gets the values from EPICS servers.
	 *
	 * @return the value array
	 * @throws CAException
	 * @throws TimeoutException
	 * @throws InterruptedException
	 */
	public String[] cagetAllChannels() throws CAException, TimeoutException, InterruptedException {
		String[] values = new String[chs.length];
		for (int i = 0; i < chs.length; i++)
			values[i] = controller.caget(chs[i]);
		return values;
	}

	/**
	 * Sets the value to the PV on EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(double value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}

	/**
	 * Sets the value to the PV on EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(int value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}
	public void caputWait(int value) throws CAException, TimeoutException, InterruptedException {
		controller.caputWait(theChannel, value);
	}

	/**
	 * Sets the value to the PV on EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(short value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}

	/**
	 * Sets the value to the PV on EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(float value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}
	public void caputWait(double value) throws CAException, TimeoutException, InterruptedException {
		controller.caputWait(theChannel, value);
	}
	/**
	 * Sets the value to the PV on EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(byte value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}

	/**
	 * Sets the value to the PV in EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caput(String value) throws CAException, InterruptedException {
		controller.caput(theChannel, value);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(int[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(short[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(float[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(byte[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(double[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param values
	 * @throws CAException
	 */
	public void caput(String[] values) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i]);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the value to the PV on EPICS server and wait for callback.
	 *
	 * @param value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, double value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(double value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the value to the PV on EPICS server and wait for callback.
	 *
	 * @param value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, int value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(int value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the value to the PV on EPICS server and wait for callback.
	 *
	 * @param value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, short value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(short value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the value to the PV on EPICS server and wait for callback.
	 *
	 * @param value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, byte value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(byte value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the value to the PV on EPICS server and wait for callback.
	 *
	 * @param value
	 * @param timeoutinsecond
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, float value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(float value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the value to the PV in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param value
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, String value) throws CAException, TimeoutException, InterruptedException {
		controller.caput(theChannel, value, timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(String value, PutListener lstnr) throws CAException, InterruptedException {
		controller.caput(theChannel, value, lstnr);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, double[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(double[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], lstnr);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, int[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(int[] values, PutListener lstnr) throws CAException, InterruptedException {
		if (pvName != null) {
			// single channel waveform input
			controller.caput(theChannel, values, lstnr);
		} else if (pvNames != null) {
			for (int i = 0; i < chs.length; i++)
				controller.caput(chs[i], values[i], lstnr);
		} else {
			logger.info("This client does not have any PV strings as destinations.");
		}
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, short[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(short[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], lstnr);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, float[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(float[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], lstnr);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server and wait for callback.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, byte[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 */
	public void caput(byte[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], lstnr);
	}

	/**
	 * Sets the values to the corresponding PVs in EPICS server.
	 *
	 * @param timeoutinsecond
	 * @param values
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public void caput(double timeoutinsecond, String[] values) throws CAException, TimeoutException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], timeoutinsecond);
	}

	/**
	 * Asynchronously sets the value to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param values
	 *            array
	 * @param lstnr
	 * @throws CAException
	 * @throws InterruptedException
	 */
	public void caput(String[] values, PutListener lstnr) throws CAException, InterruptedException {
		for (int i = 0; i < chs.length; i++)
			controller.caput(chs[i], values[i], lstnr);
	}
	/**
	 * caput a string value as waveform to the corresponding PVs in EPICS server.
	 *
	 * @param value
	 * @throws CAException
	 */
	public void caputStringAsWaveform(String value) throws CAException, InterruptedException {
		int[] waveform = JCAUtils.getIntArrayFromWaveform(value);
		caput(waveform);
	}
	/**
	 * Asynchronously sets a string as waveform to the PV on EPICS server with a put callback listener, which must implements the
	 * putCompleted(PutEvent pe) method to handle the callback.
	 *
	 * @param value
	 *            the input string value
	 * @param lstnr
	 * @throws CAException
	 */
	public void caputStringAsWaveform(String value, PutListener lstnr) throws CAException, InterruptedException {
		int[] waveform = JCAUtils.getIntArrayFromWaveform(value);
		caput(waveform, lstnr);
	}
	/**
	 * Clear the CA channels and reclaim the resources.
	 */
	public void clearup() {
		if (configured) {
			if (theChannel != null) {
				controller.destroy(theChannel);
				theChannel = null;
			} else if (chs != null) {
				for (int i = 0; i < chs.length; i++)
					controller.destroy(chs[i]);
				chs = null;
			}
			configured = false;
		}
	}

	/**
	 * @return Element Count
	 */
	public int getElementCount() {
		return theChannel.getElementCount();
	}

	/**
	 * Gets the configuration state of the object.
	 *
	 * @return true if configured, false if not.
	 */
	public boolean isConfigured() {
		return configured;
	}

	/**
	 * Set the configuration state of the object.
	 *
	 * @param configstate
	 */
	public void setConfigured(boolean configstate) {
		this.configured = configstate;
	}

	private String timeStamp2String(DBR dbr) {
		String timeStamp = null;

		if (dbr.isTIME()) {
			timeStamp = TIMEHandler.getTimeStamp(dbr).toMONDDYYYY();
		}
		return timeStamp;
	}

	private double max(double[] t) {
		double maximum = t[0]; // start with the first value
		for (int i = 1; i < t.length; i++) {
			if (t[i] > maximum) {
				maximum = t[i]; // new maximum
			}
		}
		return maximum;
	}

	private int peakPosition(double[] t) {
		double maximum = t[0]; // start with the first value
		int position = 0;
		for (int i = 1; i < t.length; i++) {
			if (t[i] > maximum) {
				maximum = t[i]; // new maximum
				position = i; // position of the peak or maximum value
			}
		}
		return position;
	}// end method peakPosition

	/**
	 * This is the default Monitor handler that simply print the PV name and value on the JythonTerminal. {@inheritDoc}
	 *
	 * @see gov.aps.jca.event.MonitorListener#monitorChanged(gov.aps.jca.event.MonitorEvent)
	 */
	@Override
	public void monitorChanged(MonitorEvent arg0) {
		String pvName = ((Channel) arg0.getSource()).getName();
		String value = String.valueOf(arg0.getDBR().getValue());
		JythonServerFacade.getInstance().print(pvName + "\t " + value);

	}

	@Override
	public void connectionChanged(ConnectionEvent arg0) {
		arg0.getSource();
	}
}
