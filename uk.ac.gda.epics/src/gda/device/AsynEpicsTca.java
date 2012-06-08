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

package gda.device;

import gda.epics.connection.EpicsController;
import gda.epics.connection.STSHandler;
import gda.epics.util.JCAUtils;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.event.ConnectionEvent;
import gov.aps.jca.event.ConnectionListener;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import gov.aps.jca.event.PutEvent;
import gov.aps.jca.event.PutListener;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class AsynEpicsTca extends DeviceBase implements Device, Findable, MonitorListener, ConnectionListener,
		PutListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AsynEpicsTca.class);

	private EpicsRecord epicsTcaRecord;

	private String epicsTcaRecordName;

	private String tcaRecordName;

	private Channel scaler1HighChannel;

	private Channel scaler1LowChannel;

	private Channel scaler2HighChannel;

	private Channel scaler2LowChannel;

	private Channel scaler3HighChannel;

	private Channel scaler3LowChannel;

	private Channel purEnableChannel;

	private Channel scalerCalChannel;

	private Channel polarityChannel;

	private Channel scalerEnableChannel;

	private Channel scaler1GateChannel;

	private Channel scaler2GateChannel;

	private Channel scaler3GateChannel;

	private Channel tcaSelectChannel;

	private Channel scaler1PurChannel;

	private Channel scaler2PurChannel;

	private Channel scaler3PurChannel;

	private Channel purAmpChannel;

	private Channel statusChannel;

	private EpicsController controller;

	private HashSet<Channel> monitorInstalledSet;

	/**
	 * 
	 */
	public enum purEnableEnum {
		/** */
		No,
		/** */
		Yes
	}

	/**
	 * 
	 */
	public enum scalerEnableEnum {
		/** */
		No,
		/** */
		Yes
	}

	/**
	 * 
	 */
	public enum polarityEnum {
		/** */
		Normal,
		/** */
		Inverted
	}

	/**
	 * 
	 */
	public enum scalerGateEnum {
		/** */
		Disable,
		/** */
		Enable
	}

	/**
	 * 
	 */
	public enum scalerPurEnum {
		/** */
		No,
		/** */
		Yes
	}

	/**
	 * 
	 */
	public enum tcaSelectEnum {
		/** */
		No,
		/** */
		Yes
	}

	/**
	 * 
	 */
	public enum purAmpEnum {
		/** */
		No,
		/** */
		Yes
	}

	/**
	 * 
	 */
	public enum statusEnum {
		/** */
		Online,
		/** */
		SelfTestError,
		/** */
		ModuleReset,
		/** */
		CannotCommunicate
	}

	/**
	 * 
	 */
	public AsynEpicsTca() {
		controller = EpicsController.getInstance();
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if ((epicsTcaRecord = (EpicsRecord) Finder.getInstance().find(epicsTcaRecordName)) != null) {
				monitorInstalledSet = new HashSet<Channel>();

				tcaRecordName = epicsTcaRecord.getFullRecordName();
				logger.debug("the adc record name is" + tcaRecordName);

				try {
					scaler1HighChannel = controller.createChannel(getTcaRecordName() + "SCA1_HI", this);
					scaler1LowChannel = controller.createChannel(getTcaRecordName() + "SCA1_LOW", this);
					scaler2HighChannel = controller.createChannel(getTcaRecordName() + "SCA2_HI", this);
					scaler2LowChannel = controller.createChannel(getTcaRecordName() + "SCA2_LOW", this);
					scaler3HighChannel = controller.createChannel(getTcaRecordName() + "SCA3_HI", this);
					scaler3LowChannel = controller.createChannel(getTcaRecordName() + "SCA3_LOW", this);
					purEnableChannel = controller.createChannel(getTcaRecordName() + "PUR_ENABLE", this);
					scalerCalChannel = controller.createChannel(getTcaRecordName() + "SCA_CAL", this);
					polarityChannel = controller.createChannel(getTcaRecordName() + "POLARITY", this);
					scalerEnableChannel = controller.createChannel(getTcaRecordName() + "SCA_ENABLE", this);
					scaler1GateChannel = controller.createChannel(getTcaRecordName() + "SCA1_GATE", this);
					scaler2GateChannel = controller.createChannel(getTcaRecordName() + "SCA2_GATE", this);
					scaler3GateChannel = controller.createChannel(getTcaRecordName() + "SCA3_GATE", this);
					scaler1PurChannel = controller.createChannel(getTcaRecordName() + "SCA1_PUR", this);
					scaler2PurChannel = controller.createChannel(getTcaRecordName() + "SCA2_PUR", this);
					scaler3PurChannel = controller.createChannel(getTcaRecordName() + "SCA3_PUR", this);
					tcaSelectChannel = controller.createChannel(getTcaRecordName() + "TCA_SELECT", this);
					purAmpChannel = controller.createChannel(getTcaRecordName() + "PUR_AMP", this);
					statusChannel = controller.createChannel(getTcaRecordName() + "STATUS", this);
				} catch (Throwable th) {
					throw new FactoryException("faield to create channels", th);
				}

			}
			configured = true;
		}

	}

	private String getTcaRecordName() {
		return tcaRecordName;
	}

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {
		try {
			if (attributeName.equalsIgnoreCase("SCA1HI"))
				return getScalerHigh(1);
			else if (attributeName.equalsIgnoreCase("SCA2HI"))
				return getScalerHigh(2);
			else if (attributeName.equalsIgnoreCase("SCA3HI"))
				return getScalerHigh(3);
			else if (attributeName.equalsIgnoreCase("SCA1LOW"))
				return getScalerLow(1);
			else if (attributeName.equalsIgnoreCase("SCA2LOW"))
				return getScalerLow(2);
			else if (attributeName.equalsIgnoreCase("SCA3LOW"))
				return getScalerLow(3);
			else if (attributeName.equalsIgnoreCase("PURENABLE"))
				return getpurEnable();
			else if (attributeName.equalsIgnoreCase("SCACAL"))
				return getScalerCal();
			else if (attributeName.equalsIgnoreCase("SCAENABLE"))
				return getScalerEnable();
			else if (attributeName.equalsIgnoreCase("POLARITY"))
				return getPolarity();
			else if (attributeName.equalsIgnoreCase("SCA1GATE"))
				return getScalerGate(1);
			else if (attributeName.equalsIgnoreCase("SCA2GATE"))
				return getScalerGate(2);
			else if (attributeName.equalsIgnoreCase("SCA3GATE"))
				return getScalerGate(3);
			else if (attributeName.equalsIgnoreCase("TCASELECT"))
				return getTcaSelect();
			else if (attributeName.equalsIgnoreCase("SCA1PUR"))
				return getScalerPur(1);
			else if (attributeName.equalsIgnoreCase("SCA2PUR"))
				return getScalerPur(2);
			else if (attributeName.equalsIgnoreCase("SCA3PUR"))
				return getScalerPur(3);
			else if (attributeName.equalsIgnoreCase("PURAMP"))
				return getPurAmp();
			else if (attributeName.equalsIgnoreCase("STATUS"))
				return getStatus();
			return null;
		} catch (Throwable th) {
			throw new DeviceException("failed to get attribute", th);
		}
	}

	private Object getStatus() throws CAException, TimeoutException, InterruptedException {
		int status = -1;
		status = controller.cagetEnum(statusChannel);
		switch (status) {
		case 0:
			return statusEnum.Online;
		case 1:
			return statusEnum.SelfTestError;
		case 2:
			return statusEnum.ModuleReset;
		default:
			return statusEnum.CannotCommunicate;

		}

	}

	private Object getPurAmp() throws CAException, TimeoutException, InterruptedException {
		int purAmp = -1;
		if (purAmpChannel.getConnectionState() == Channel.CONNECTED)
			purAmp = controller.cagetEnum(purAmpChannel);
		// one of the possible values is Overlapped
		else
			logger.error("Connection to purAmp Channel failed");
		switch (purAmp) {
		case 0:
			return purAmpEnum.No;
		case 1:
			return purAmpEnum.Yes;
		}

		return purAmp;
	}

	private Object getScalerPur(int i) throws CAException, TimeoutException, InterruptedException {
		int scalerPur = -1;

		switch (i) {
		case 1:
			scalerPur = controller.cagetEnum(scaler1PurChannel);
			break;
		case 2:
			scalerPur = controller.cagetEnum(scaler2PurChannel);
			break;
		case 3:
			scalerPur = controller.cagetEnum(scaler3PurChannel);
			break;
		}

		switch (scalerPur) {
		case 0:
			return scalerPurEnum.No;
		case 1:
			return scalerPurEnum.Yes;
		}

		return scalerPur;
	}

	private Object getTcaSelect() throws CAException, TimeoutException, InterruptedException {
		int value = controller.cagetEnum(tcaSelectChannel);
		switch (value) {
		case 0:
			return tcaSelectEnum.No;
		case 1:
			return tcaSelectEnum.Yes;
		}
		return value;
	}

	private Object getScalerGate(int i) throws CAException, TimeoutException, InterruptedException {
		int scalerGate = -1;

		switch (i) {
		case 1:
			scalerGate = controller.cagetEnum(scaler1GateChannel);
			break;
		case 2:
			scalerGate = controller.cagetEnum(scaler2GateChannel);
			break;
		case 3:
			scalerGate = controller.cagetEnum(scaler3GateChannel);
			break;
		}

		switch (scalerGate) {
		case 0:
			return scalerGateEnum.Disable;
		case 1:
			return scalerGateEnum.Enable;
		}

		return scalerGate;
	}

	private Object getPolarity() throws CAException, TimeoutException, InterruptedException {
		int polarity = -1;
		polarity = controller.cagetEnum(polarityChannel);
		switch (polarity) {
		case 0:
			return polarityEnum.Normal;
		case 1:
			return polarityEnum.Inverted;
		}
		return polarity;
	}

	private Object getScalerEnable() throws CAException, TimeoutException, InterruptedException {
		int scaEnable = -1;
		scaEnable = controller.cagetEnum(scalerEnableChannel);
		switch (scaEnable) {
		case 0:
			return scalerEnableEnum.No;
		case 1:
			return scalerEnableEnum.Yes;
		}
		return scaEnable;
	}

	private Object getScalerCal() throws CAException, TimeoutException, InterruptedException {
		return controller.cagetDouble(scalerCalChannel);
	}

	private Object getpurEnable() throws CAException, TimeoutException, InterruptedException {
		int purEnable = -1;
		purEnable = controller.cagetEnum(purEnableChannel);
		switch (purEnable) {
		case 0:
			return purEnableEnum.No;
		case 1:
			return purEnableEnum.Yes;
		}
		return purEnable;
	}

	private Object getScalerLow(int i) throws CAException, TimeoutException, InterruptedException {
		double scalerLow = -1;

		switch (i) {
		case 1:
			scalerLow = controller.cagetDouble(scaler1LowChannel);
			break;
		case 2:
			scalerLow = controller.cagetDouble(scaler2LowChannel);
			break;
		case 3:
			scalerLow = controller.cagetDouble(scaler3LowChannel);
			break;
		}
		return scalerLow;

	}

	private Object getScalerHigh(int i) throws CAException, TimeoutException, InterruptedException {
		double scalerHigh = -1;

		switch (i) {
		case 1:
			scalerHigh = controller.cagetDouble(scaler1HighChannel);
			break;
		case 2:
			scalerHigh = controller.cagetDouble(scaler2HighChannel);
			break;
		case 3:
			scalerHigh = controller.cagetDouble(scaler3HighChannel);
			break;
		}
		return scalerHigh;
	}

	@Override
	public void setAttribute(String attributeName, Object value) throws DeviceException {
		try {
			if (attributeName.equalsIgnoreCase("SCA1HI"))
				setScalerHigh(1, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCA2HI"))
				setScalerHigh(2, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCA3HI"))
				setScalerHigh(3, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCA1LOW"))
				setScalerLow(1, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCA2LOW"))
				setScalerLow(2, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCA3LOW"))
				setScalerLow(3, ((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("PURENABLE"))
				setPurEnable((purEnableEnum) value);
			else if (attributeName.equalsIgnoreCase("SCACAL"))
				setScalerCAL(((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCAENABLE"))
				setScalerEnable((scalerEnableEnum) value);
			else if (attributeName.equalsIgnoreCase("POLARITY"))
				setPolarity((polarityEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA1GATE"))
				setScalerGate(1, (scalerGateEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA2GATE"))
				setScalerGate(2, (scalerGateEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA3GATE"))
				setScalerGate(3, (scalerGateEnum) value);
			else if (attributeName.equalsIgnoreCase("TCASELECT"))
				setTcaSelect((tcaSelectEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA1PUR"))
				setScalerPur(1, (scalerPurEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA2PUR"))
				setScalerPur(2, (scalerPurEnum) value);
			else if (attributeName.equalsIgnoreCase("SCA3PUR"))
				setScalerPur(3, (scalerPurEnum) value);
			else if (attributeName.equalsIgnoreCase("PURAMP"))
				setPurAmp((purAmpEnum) value);
			else if (attributeName.equalsIgnoreCase("STATUS"))
				setStatus((statusEnum) value);
		} catch (Throwable th) {
			throw new DeviceException("failed to set attribute", th);
		}
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setScalerHigh(int scalerNo, double value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1HighChannel, value, this);
			break;
		case 2:
			controller.caput(scaler2HighChannel, value, this);
			break;
		case 3:
			controller.caput(scaler3HighChannel, value, this);
			break;
		}
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 */
	public void setScalerLow(int scalerNo, double value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1LowChannel, value, this);
			break;
		case 2:
			controller.caput(scaler2LowChannel, value, this);
			break;
		case 3:
			controller.caput(scaler3LowChannel, value, this);
			break;
		}

	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPurEnable(purEnableEnum value) throws CAException, InterruptedException {
		controller.caput(purEnableChannel, value.ordinal(), this);
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 */
	public void setScalerGate(int scalerNo, scalerGateEnum value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1GateChannel, value.ordinal(), this);
			break;
		case 2:
			controller.caput(scaler2GateChannel, value.ordinal(), this);
			break;
		case 3:
			controller.caput(scaler3GateChannel, value.ordinal(), this);
			break;
		}
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 */
	public void setScalerPur(int scalerNo, scalerPurEnum value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1PurChannel, value.ordinal(), this);
			break;
		case 2:
			controller.caput(scaler2PurChannel, value.ordinal(), this);
			break;
		case 3:
			controller.caput(scaler3PurChannel, value.ordinal(), this);
			break;
		}
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setScalerEnable(scalerEnableEnum value) throws CAException, InterruptedException {
		controller.caput(scalerEnableChannel, value.ordinal(), this);
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setTcaSelect(tcaSelectEnum value) throws CAException, InterruptedException {
		controller.caput(tcaSelectChannel, value.ordinal(), this);
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setScalerCAL(double value) throws CAException, InterruptedException {
		controller.caput(scalerCalChannel, value, this);
	}

	/**
	 * @param value
	 */
	public void setScalerROIEnable(@SuppressWarnings("unused") boolean value) {
		// TODO implement, then remove the @SuppressWarnings("unused") tag
	}

	/**
	 * @param value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setStatus(statusEnum value) throws CAException, InterruptedException {
		controller.caput(statusChannel, value.ordinal(), this);
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPolarity(polarityEnum value) throws CAException, InterruptedException {
		controller.caput(polarityChannel, value.ordinal(), this);
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPurAmp(purAmpEnum value) throws CAException, InterruptedException {
		controller.caput(purAmpChannel, value.ordinal(), this);
	}

	@Override
	public void monitorChanged(MonitorEvent arg0) {
	}

	@Override
	public void connectionChanged(ConnectionEvent arg0) {
		onConnectionChanged(arg0);

	}

	/**
	 * Connection callback
	 * 
	 * @param ev
	 */
	private void onConnectionChanged(ConnectionEvent ev) {
		Channel ch = (Channel) ev.getSource();
		boolean installMonitor = false;

		if (ev.isConnected()) {
			synchronized (monitorInstalledSet) {
				installMonitor = !monitorInstalledSet.contains(ch);
			}
		}

		// start a monitor on the first connection
		if (installMonitor) {
			try {
				// Print some information
				logger.info(JCAUtils.timeStamp() + " Search successful for: " + ch.getName());
				// ch.printInfo();
				// Add a monitor listener on every successful connection
				// The following is commented out to solve scan pyException
				// problem
				// - need to track down the real cause.
				controller.setMonitor(ch, STSHandler.getSTSType(ch), Monitor.VALUE | Monitor.ALARM, this);

				synchronized (monitorInstalledSet) {
					monitorInstalledSet.add(ch);
				}

			} catch (Throwable ex) {
				logger.error("Add Monitor failed for Channel: " + ch.getName() + " : " + ex);
				return;
			}
		}

		// print connection state
		logger.info(JCAUtils.timeStamp() + " ");
		if (ch.getConnectionState() == Channel.CONNECTED) {
			logger.info(ch.getName() + " is connected");
		} else if (ch.getConnectionState() == Channel.DISCONNECTED) {
			logger.info(ch.getName() + " is disconnected");
		} else if (ch.getConnectionState() == Channel.CLOSED) {
			logger.info(ch.getName() + " is closed");
		}
	}

	@Override
	public void putCompleted(PutEvent pev) {
		Channel ch = (Channel) pev.getSource();
		if (pev.getStatus().isSuccessful()) {
			logger.debug(ch.getName() + " : Put completed successfully >>> " + pev.getStatus().getMessage());
		} else if (pev.getStatus().isError()) {
			logger.error(ch.getName() + " : Put Error >>> " + pev.getStatus().getMessage());
		}

		else if (pev.getStatus().isFatal()) {
			logger.error(ch.getName() + " : Fatal Error >>> " + pev.getStatus().getMessage());
		} else {
			logger.debug(ch.getName() + " : Put Warning >>> " + pev.getStatus().getMessage());
		}
	}

}
