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

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.TIMEHandler;
import gda.epics.connection.EpicsController.MonitorType;
import gda.epics.util.JCAUtils;
import gda.factory.FactoryException;
import gda.jython.JythonServerFacade;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.BYTE;
import gov.aps.jca.dbr.DBR;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The scripting interface between Jython and EPICS. CAClient provides client-side implementation of caget(), caput(),
 * and camonitor() for accessing EPICS PVs directly from Jython scripts or Jython Terminal.
 */
public class ChannalAccessCommands extends EpicsBase implements Epics, MonitorListener, ConnectionListener {

	private static final Logger logger = LoggerFactory.getLogger(ChannalAccessCommands.class);


	private Channel theChannel = null;

	private Channel[] chs = null;

	private boolean configured = false;

	EpicsController controller = EpicsController.getInstance();
	EpicsChannelManager channelmanager = new EpicsChannelManager();

	// ////////// Constructor and methods for Jython Terminal Command-line // /////////
	/**
	 * Constructor
	 */
	public ChannalAccessCommands() {
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
	 * start the default CA monitor Listener to this PV, the default behaviour is the monitor prints PV name and value to
	 * Jython Terminal.
	 * 
	 * @return Monitor
	 * @throws CAException
	 */
	public Monitor camonitor(String pv) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		return controller.setMonitor(channel, this);
	}
	public void caput(String pv, int value, boolean wait) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caputWait(channel, value);
		} else {
			controller.caput(channel, value);
		}
	}
	public void caput(String pv, int value, boolean wait, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caput(channel, value, timeoutinsecond);
		} else {
			controller.caput(channel, value);
		}
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
	public void caput(String pv, String value, boolean wait) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caputWait(channel, value);
		} else {
			controller.caput(channel, value);
		}
	}
	public void caput(String pv, String value, boolean wait, double timeoutinsecond) throws CAException, TimeoutException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait) {
			controller.caput(channel, value, timeoutinsecond);
		} else {
			controller.caput(channel, value);
		}
	}
	public void caput(String pv, String value, boolean wait, boolean charString) throws CAException, InterruptedException, TimeoutException{
		if (charString) {
			int[] waveform = JCAUtils.getIntArrayFromWaveform(value);
			caput(pv, waveform, wait);
		} else {
			caput(pv, value, wait);
		}
	}
	public void caput(String pv, int[] value, boolean wait) throws CAException, InterruptedException, TimeoutException {
		Channel channel = channelmanager.createChannel(pv);
		if (wait){
			controller.caputWait(channel, value);
		} else {
			controller.caput(channel, value);
		}
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
	 * Sets the value of the PV.
	 * 
	 * @param pv
	 *            the PV name
	 * @param value
	 *            the value
	 * @throws CAException
	 */
	private void caput(String pv, int[] value) throws CAException, InterruptedException {
		Channel channel = channelmanager.createChannel(pv);
		controller.caput(channel, value);
	}


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