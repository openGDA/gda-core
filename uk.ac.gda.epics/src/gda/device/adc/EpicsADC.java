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

package gda.device.adc;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.Adc;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.InterfaceException;
import gda.epics.interfaces.SimpleScalerType;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EpicsADC Class.
 */
public class EpicsADC extends DeviceBase implements Adc, Findable, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsADC.class);
	
	/**
	 * Gains
	 */
	public double[] Gain = { 256, 512, 1024, 2048, 4096, 8192, 16384 };

	private Channel adcOffsetChannel;

	private Channel adcLldChannel;

	private Channel adcGainChannel;

	private Channel uldChannel;

	private Channel zeroChannel;

	private Channel amodChannel;

	private Channel cmodChannel;

	private Channel pmodChannel;

	private Channel gmodChannel;

	private Channel tmodChannel;

	private Channel scanModeChannel;

	private String scanModePv;

	private String epicsAdcRecordName;

	private EpicsRecord epicsAdcRecord;

	private String adcRecordName;

	private String deviceName;

	private EpicsController controller;

	// private int connectedState = Channel.CONNECTED.getValue();
	/**
	 * 
	 */
	public static String GAIN = "gain";

	/**
	 * 
	 */
	public static String OFFSET = "offset";

	/**
	 * 
	 */
	public static String LLD = "lld";

	/**
	 * 
	 */
	public static String ULD = "uld";

	/**
	 * 
	 */
	public static String ZERO = "zero";

	/**
	 * 
	 */
	public static String AMOD = "amod";

	/**
	 * 
	 */
	public static String CMOD = "cmod";

	/**
	 * 
	 */
	public static String PMOD = "pmod";

	/**
	 * 
	 */
	public static String GMOD = "gmod";

	/**
	 * 
	 */
	public static String TMOD = "tmod";

	/**
	 * 
	 */
	public static String GAIN_VALUES = "gain_values";

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	/**
	 * gmod enum
	 */
	public enum gmodEnum implements Serializable {
		/**
		 * 
		 */
		Coincidence, /**
		 * 
		 */
		AntiCoincidence
	}

	/**
	 * cmod enum
	 */
	public enum cmodEnum {
		/**
		 * 
		 */
		Early, /**
		 * 
		 */
		Late
	}

	/**
	 * pmod enum
	 */
	public enum pmodEnum {
		/**
		 * 
		 */
		Auto, /**
		 * 
		 */
		Delayed
	}

	/**
	 * amod enum
	 */
	public enum amodEnum implements Serializable {
		/**
		 * 
		 */
		PHA, /**
		 * 
		 */
		SVA
	}

	/**
	 * tmod enum
	 */
	public enum tmodEnum {
		/**
		 * 
		 */
		Overlapped, /**
		 * 
		 */
		NonOverlapped
	}

	/**
	 * Constructor.
	 */
	public EpicsADC() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}
	
	public void setAdcRecordName(String adcRecordName) {
		this.adcRecordName = adcRecordName;
	}
	
	private String getAdcRecordName() {
		return adcRecordName;
	}

	/**
	 * @return The Epics record name for the ADC.
	 */
	public String getEpicsAdcRecordName() {
		return epicsAdcRecordName;
	}

	/**
	 * @param epicsAdcRecordName
	 */
	public void setEpicsAdcRecordName(String epicsAdcRecordName) {
		this.epicsAdcRecordName = epicsAdcRecordName;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			// EPICS interface verion 2 for phase I beamlines + I22
			if (getEpicsAdcRecordName() != null) {
				if ((epicsAdcRecord = (EpicsRecord) Finder.getInstance().find(epicsAdcRecordName)) != null) {

					adcRecordName = epicsAdcRecord.getFullRecordName();
					createChannelAccess(adcRecordName);
				} else {
					logger.error("Epics Record " + epicsAdcRecordName + " not found");
					throw new FactoryException("Epics Record " + epicsAdcRecordName + " not found");
				}
			}
			// EPICS interface version 3 for phase II beamlines (excluding I22).
			else if (getDeviceName() != null) {
				SimpleScalerType scalerConfig;
				try {
					scalerConfig = Configurator.getConfiguration(getDeviceName(),
							gda.epics.interfaces.SimpleScalerType.class);
					adcRecordName = scalerConfig.getRECORD().getPv();
					createChannelAccess(adcRecordName);
				} catch (ConfigurationNotFoundException e) {
					/* Try to read from unchecked xml */
					try {
						createChannelAccess(getPV());
					} catch (Exception ex) {
						logger.error("Can NOT find EPICS configuration for scaler " + getDeviceName() + "."
								+ e.getMessage(), ex);
					}
				}

			}
			
			else if (adcRecordName != null) {
				createChannelAccess(adcRecordName);
			}
			
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS interface configuration for the motor " + getName());
				throw new FactoryException("Missing EPICS interface configuration for the motor " + getName());
			}
			configured = true;
		}

	}

	private void createChannelAccess(String recordName) throws FactoryException {
		try {
			adcRecordName = recordName;
			adcGainChannel = channelManager.createChannel(getAdcRecordName() + "GAIN", false);
			adcOffsetChannel = channelManager.createChannel(getAdcRecordName() + "OFFSET", false);
			adcLldChannel = channelManager.createChannel(getAdcRecordName() + "LLD", false);
			uldChannel = channelManager.createChannel(getAdcRecordName() + "ULD", false);
			zeroChannel = channelManager.createChannel(getAdcRecordName() + "ZERO", false);
			amodChannel = channelManager.createChannel(getAdcRecordName() + "AMOD", false);
			cmodChannel = channelManager.createChannel(getAdcRecordName() + "CMOD", false);
			pmodChannel = channelManager.createChannel(getAdcRecordName() + "PMOD", false);
			gmodChannel = channelManager.createChannel(getAdcRecordName() + "GMOD", false);
			tmodChannel = channelManager.createChannel(getAdcRecordName() + "TMOD", false);

			scanModePv = getAdcRecordName();
			// logger.info("the scan mode pv is " + scanModePv + " "+ getAdcRecordName());
			scanModeChannel = channelManager.createChannel(scanModePv, false);
			// logger.info("scan moe channel is " + scanModeChannel);
			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			channelManager.tryInitialize(100);
		}

		catch (Throwable th) {
			throw new FactoryException("Scaler - " + getName() + " faield to create control channels", th);
		}

	}

	@Override
	public double getVoltage(int channel) throws DeviceException {
		return 0;
	}

	@Override
	public double[] getVoltages() throws DeviceException {
		return null;
	}

	@Override
	public void setRange(int channel, int range) throws DeviceException {
	}

	@Override
	public int getRange(int channel) throws DeviceException {
		return 0;
	}

	@Override
	public void setUniPolar(int channel, boolean polarity) throws DeviceException {
	}

	@Override
	public int[] getRanges() throws DeviceException {
		return null;
	}

	@Override
	public boolean isUniPolarSettable() throws DeviceException {
		return false;
	}

	@Override
	public void setSampleCount(int count) throws DeviceException {
	}

	/**
	 * @return Gain
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public double getGain() throws CAException, TimeoutException, InterruptedException {
		int gain = 0;

		if (adcGainChannel.getConnectionState() == Channel.CONNECTED)
			gain = controller.cagetEnum(adcGainChannel);
		else
			logger.error("Connection to Adc Gain Channel failed");
		return Gain[gain];

	}

	/**
	 * @param gain
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setGain(double gain) throws CAException, InterruptedException {
		int i = 0;
		for (; i < Gain.length; i++)
			if (Gain[i] == gain)
				break;
		controller.caput(adcGainChannel, i);
	}

	/**
	 * @return Low Level Discriminator
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public double getLld() throws CAException, TimeoutException, InterruptedException {
		double lld = 0.0;
		if (adcLldChannel.getConnectionState() == Channel.CONNECTED)
			lld = controller.cagetDouble(adcLldChannel);
		else
			logger.error("Connection to Adc low level discriminator Channel failed");
		return lld;

	}

	/**
	 * @param lld
	 *            Low Level Discriminator
	 * @throws CAException
	 */
	public void setLld(double lld) throws CAException, InterruptedException {
		controller.caput(adcLldChannel, lld);
	}

	/**
	 * @return Offset
	 * @throws CAException
	 * @throws TimeoutException
	 */
	public double getOffset() throws CAException, TimeoutException, InterruptedException {
		double offset = 0.0;
		if (adcOffsetChannel.getConnectionState() == Channel.CONNECTED)
			offset = controller.cagetDouble(adcOffsetChannel);
		else
			logger.error("Connection to adc Offset Channel failed");

		return offset;
	}

	/**
	 * @param offset
	 * @throws CAException
	 */
	public void setOffset(double offset) throws CAException, InterruptedException {
		controller.caput(adcOffsetChannel, offset);
	}

	@Override
	public Object getAttribute(String attribute) throws DeviceException {
		try {
			if (attribute.equalsIgnoreCase(GAIN_VALUES))
				return Gain;
			else if (attribute.equalsIgnoreCase(GAIN))
				return getGain();
			else if (attribute.equalsIgnoreCase(OFFSET))
				return getOffset();
			else if (attribute.equalsIgnoreCase(LLD))
				return getLld();
			else if (attribute.equalsIgnoreCase(ULD))
				return getUld();
			else if (attribute.equalsIgnoreCase(ZERO))
				return getZero();
			else if (attribute.equalsIgnoreCase(AMOD))
				return ((amodEnum) getAmod()).toString();
			else if (attribute.equalsIgnoreCase(CMOD))
				return ((cmodEnum) getCmod()).toString();
			else if (attribute.equalsIgnoreCase(PMOD))
				return ((pmodEnum) getPmod()).toString();
			else if (attribute.equalsIgnoreCase(GMOD))
				return ((gmodEnum) getGmod()).toString();
			else if (attribute.equalsIgnoreCase(TMOD))
				return ((tmodEnum) getTmod()).toString();
			else
				return 0;
		} catch (Throwable th) {
			throw new DeviceException("failed to get attribute", th);
		}
	}

	private Object getTmod() throws CAException, TimeoutException, InterruptedException {
		int tmod = -1;
		if (tmodChannel.getConnectionState() == Channel.CONNECTED)
			tmod = controller.cagetEnum(tmodChannel);
		// one of the possible values is Overlapped
		else
			logger.error("Connection to adc TMOD Channel failed");
		switch (tmod) {
		case 0:
			return tmodEnum.Overlapped;
		case 1:
			return tmodEnum.NonOverlapped;
		}

		return tmod;
	}

	private Object getGmod() throws CAException, TimeoutException, InterruptedException {
		int gmod = 0;
		if (gmodChannel.getConnectionState() == Channel.CONNECTED)
			gmod = controller.cagetEnum(gmodChannel);
		// one of the possible values is Coincidence
		else
			logger.error("Connection to adc GMOD Channel failed");

		switch (gmod) {
		case 0:
			return gmodEnum.Coincidence;
		case 1:
			return gmodEnum.AntiCoincidence;
		}

		return gmod;
	}

	private Object getPmod() throws CAException, TimeoutException, InterruptedException {
		int pmod = 0;
		if (pmodChannel.getConnectionState() == Channel.CONNECTED)
			pmod = controller.cagetEnum(pmodChannel);
		// one of the possible values is Auto
		else
			logger.error("Connection to adc PMOD Channel failed");

		switch (pmod) {
		case 0:
			return pmodEnum.Auto;
		case 1:
			return pmodEnum.Delayed;
		}

		return pmod;
	}

	private Object getCmod() throws CAException, TimeoutException, InterruptedException {
		int cmod = 0;
		if (cmodChannel.getConnectionState() == Channel.CONNECTED)
			cmod = controller.cagetEnum(cmodChannel);
		// one of the possible values is Early
		else
			logger.error("Connection to adc CMOD Channel failed");

		switch (cmod) {
		case 0:
			return cmodEnum.Early;
		case 1:
			return cmodEnum.Late;
		}

		return cmod;
	}

	private Object getAmod() throws CAException, TimeoutException, InterruptedException {
		int amod = 0;
		if (amodChannel.getConnectionState() == Channel.CONNECTED)
			amod = controller.cagetEnum(amodChannel);
		// one of the possible values is PHA
		else
			logger.error("Connection to adc AMOD Channel failed");

		switch (amod) {
		case 0:
			return amodEnum.PHA;
		case 1:
			return amodEnum.SVA;
		}

		return amod;
	}

	private Object getZero() throws CAException, TimeoutException, InterruptedException {
		double zero = 0.0;
		if (zeroChannel.getConnectionState() == Channel.CONNECTED)
			zero = controller.cagetDouble(zeroChannel);
		else
			logger.error("Connection to adc ZERO Channel failed");

		return zero;
	}

	private Object getUld() throws CAException, TimeoutException, InterruptedException {
		double uld = 0.0;
		if (uldChannel.getConnectionState() == Channel.CONNECTED)
			uld = controller.cagetDouble(uldChannel);
		else
			logger.error("Connection to adc ULD Channel failed");

		return uld;
	}

	@Override
	public void setAttribute(String attribute, Object value) throws DeviceException {
		try {
			if (attribute.equalsIgnoreCase(GAIN))
				setGain(((Double) value).doubleValue());
			else if (attribute.equalsIgnoreCase(OFFSET))
				setOffset(((Double) value).doubleValue());
			else if (attribute.equalsIgnoreCase(LLD))
				setLld(((Double) value).doubleValue());
			else if (attribute.equalsIgnoreCase(ULD))
				setUld(((Double) value).doubleValue());
			else if (attribute.equalsIgnoreCase(ZERO))
				setZero(((Double) value).doubleValue());
			else if (attribute.equalsIgnoreCase(AMOD))
				setAmod(amodEnum.valueOf((String) value));
			else if (attribute.equalsIgnoreCase(CMOD))
				setCmod(cmodEnum.valueOf((String) value));
			else if (attribute.equalsIgnoreCase(PMOD))
				setPmod(pmodEnum.valueOf((String) value));
			else if (attribute.equalsIgnoreCase(GMOD))
				setGmod(gmodEnum.valueOf((String) value));
			else if (attribute.equalsIgnoreCase(TMOD))
				setTmod(tmodEnum.valueOf((String) value));
		} catch (Throwable th) {
			throw new DeviceException("failed to set attribute", th);
		}
	}

	public void setTmod(tmodEnum tmod) throws CAException, InterruptedException {
		controller.caput(tmodChannel, tmod.ordinal());

	}

	public void setGmod(gmodEnum gmod) throws CAException, InterruptedException {
		controller.caput(gmodChannel, gmod.ordinal());

	}

	public void setPmod(pmodEnum pmod) throws CAException, InterruptedException {
		controller.caput(pmodChannel, pmod.ordinal());
	}

	public void setCmod(cmodEnum cmod) throws CAException, InterruptedException {
		controller.caput(cmodChannel, cmod.ordinal());
	}

	public void setAmod(amodEnum amod) throws CAException, InterruptedException {
		controller.caput(amodChannel, amod.ordinal());
	}

	public void setZero(double zero) throws CAException, InterruptedException {
		controller.caput(zeroChannel, zero);
	}

	public void setUld(double uld) throws CAException, InterruptedException {
		controller.caput(uldChannel, uld);
	}

	@Override
	public void initializationCompleted() {
		logger.info("Adc - " + getName() + " initialized.");
	}

	/**
	 * @param mode
	 * @throws DeviceException
	 */
	public void setScanMode(int mode) throws DeviceException {
		try {
			if (scanModeChannel.getConnectionState() == Channel.CONNECTED) {
				controller.caput(scanModeChannel, mode);
			} else {
				logger.error("Connection to ScanMode (" + scanModePv + ") channel failed");
			}
		} catch (Throwable th) {
			throw new DeviceException("Failed to set scan mode (" + scanModePv + ")", th);
		}
	}

	/**
	 * @param mode
	 * @throws DeviceException
	 */
	public void setScanMode(String mode) throws DeviceException {
		try {
			if (scanModeChannel.getConnectionState() == Channel.CONNECTED) {
				controller.caput(scanModeChannel, mode);
			} else {
				logger.error("Connection to ScanMode (" + scanModePv + ") channel failed");
			}
		} catch (Throwable th) {
			throw new DeviceException("Failed to set scan mode (" + scanModePv + ")", th);
		}
	}

	/**
	 * @return The ScanMode as a String
	 * @throws DeviceException
	 */
	public String getScanMode() throws DeviceException {
		String mode = "unknown";

		try {
			if (scanModeChannel.getConnectionState() == Channel.CONNECTED) {
				mode = controller.cagetString(scanModeChannel);
			} else {
				logger.error("Connection to ScanMode (" + scanModePv + ") channel failed.");
			}
		} catch (Throwable th) {
			throw new DeviceException("Failed to get ScanMode (" + scanModePv + ")", th);
		}

		return mode;
	}

	/**
	 * @return The ScanMode as an Integer
	 * @throws DeviceException
	 */
	public int getScanModeInteger() throws DeviceException {
		int mode = -1;

		try {
			if (scanModeChannel.getConnectionState() == Channel.CONNECTED) {
				mode = controller.cagetInt(scanModeChannel);
			} else {
				logger.error("Connection to ScanMode (" + scanModePv + ") channel failed.");
			}
		} catch (Throwable th) {
			throw new DeviceException("Failed to get ScanMode (" + scanModePv + ")", th);
		}

		return mode;
	}

	/**
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * @return String PV
	 * @throws InterfaceException
	 */
	public String getPV() throws InterfaceException {
		return GDAEpicsInterfaceReader.getPVFromSimplePVType(getDeviceName());
	}
}
