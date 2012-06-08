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

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.InterfaceException;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gda.util.converters.CoupledConverterHolder;
import gda.util.converters.IQuantitiesConverter;
import gda.util.converters.IQuantityConverter;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class EpicsTca extends DeviceBase implements Device, Findable, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsTca.class);
	
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
	private String scalerCalChannelName = "";
	private Channel polarityChannel;
	private Channel roiScaEnableChannel;
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
	private Channel thresholdChannel;
	private EpicsController controller;
	private IQuantitiesConverter channelToEnergyConverter = null;
	private String converterName = "tca_roi_conversion";
	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

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
	public enum thresholdEnum {
		/** */
		Auto,
		/** */
		Manual
	}

	/**
	 * 
	 */
	public enum roiScaEnableEnum {
		/** */
		Disable,
		/** */
		Enable
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
	public EpicsTca() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}
	
	public void setTcaRecordName(String tcaRecordName) {
		this.tcaRecordName = tcaRecordName;
	}
	
	/**
	 * @param fullPVName
	 * @throws CAException
	 */
	private void configure(String fullPVName) throws CAException {
		tcaRecordName = fullPVName;
		scaler1HighChannel = channelManager.createChannel(getTcaRecordName() + "SCA1_HI", false);
		scaler1LowChannel = channelManager.createChannel(getTcaRecordName() + "SCA1_LOW", false);
		scaler2HighChannel = channelManager.createChannel(getTcaRecordName() + "SCA2_HI", false);
		scaler2LowChannel = channelManager.createChannel(getTcaRecordName() + "SCA2_LOW", false);
		scaler3HighChannel = channelManager.createChannel(getTcaRecordName() + "SCA3_HI", false);
		scaler3LowChannel = channelManager.createChannel(getTcaRecordName() + "SCA3_LOW", false);
		purEnableChannel = channelManager.createChannel(getTcaRecordName() + "PUR_ENABLE", false);
		scalerCalChannelName = getTcaRecordName() + "SCA_CAL";
		scalerCalChannel = channelManager.createChannel(scalerCalChannelName, false);
		roiScaEnableChannel = channelManager.createChannel(getTcaRecordName() + "ROI_SCA_ENABLE", false);
		polarityChannel = channelManager.createChannel(getTcaRecordName() + "POLARITY", false);
		scalerEnableChannel = channelManager.createChannel(getTcaRecordName() + "SCA_ENABLE", false);
		scaler1GateChannel = channelManager.createChannel(getTcaRecordName() + "SCA1_GATE", false);
		scaler2GateChannel = channelManager.createChannel(getTcaRecordName() + "SCA2_GATE", false);
		scaler3GateChannel = channelManager.createChannel(getTcaRecordName() + "SCA3_GATE", false);
		scaler1PurChannel = channelManager.createChannel(getTcaRecordName() + "SCA1_PUR", false);
		scaler2PurChannel = channelManager.createChannel(getTcaRecordName() + "SCA2_PUR", false);
		scaler3PurChannel = channelManager.createChannel(getTcaRecordName() + "SCA3_PUR", false);
		tcaSelectChannel = channelManager.createChannel(getTcaRecordName() + "TCA_SELECT", false);
		purAmpChannel = channelManager.createChannel(getTcaRecordName() + "PUR_AMP", false);
		statusChannel = channelManager.createChannel(getTcaRecordName() + "STATUS", false);
		thresholdChannel = channelManager.createChannel(getTcaRecordName() + "THRESHOLD", false);

		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
		channelManager.tryInitialize(100);
		configured = true;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			try {
				if (epicsTcaRecordName != null) {
					EpicsRecord epicsTcaRecord = (EpicsRecord) Finder.getInstance().find(epicsTcaRecordName);
					if (epicsTcaRecord != null) {
						configure(epicsTcaRecord.getFullRecordName());
					}
				} else if (getDeviceName() != null) {
					configure(getPV());
				} else if (tcaRecordName != null) {
					configure(tcaRecordName);
				}
			} catch (Exception e) {
				throw new FactoryException("Error initialising device " + getDeviceName(), e);
			}
		}
	}

	private String getTcaRecordName() {
		return tcaRecordName;
	}

	/**
	 * 
	 */
	public static final String channelToEnergyPrefix = "channelToEnergy:";
	/**
	 * 
	 */
	public static final String energyToChannelPrefix = "energyToChannel";

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
				return ((purEnableEnum) getpurEnable()).toString();
			else if (attributeName.equalsIgnoreCase("SCACAL"))
				return getScalerCal();
			else if (attributeName.equalsIgnoreCase("SCAENABLE"))
				return ((scalerEnableEnum) getScalerEnable()).toString();
			else if (attributeName.equalsIgnoreCase("POLARITY"))
				return ((polarityEnum) getPolarity()).toString();
			else if (attributeName.equalsIgnoreCase("SCA1GATE"))
				return ((scalerGateEnum) getScalerGate(1)).toString();
			else if (attributeName.equalsIgnoreCase("SCA2GATE"))
				return ((scalerGateEnum) getScalerGate(2)).toString();
			else if (attributeName.equalsIgnoreCase("SCA3GATE"))
				return ((scalerGateEnum) getScalerGate(3)).toString();
			else if (attributeName.equalsIgnoreCase("TCASELECT"))
				return ((tcaSelectEnum) getTcaSelect()).toString();
			else if (attributeName.equalsIgnoreCase("SCA1PUR"))
				return ((scalerPurEnum) getScalerPur(1)).toString();
			else if (attributeName.equalsIgnoreCase("SCA2PUR"))
				return ((scalerPurEnum) getScalerPur(2)).toString();
			else if (attributeName.equalsIgnoreCase("SCA3PUR"))
				return ((scalerPurEnum) getScalerPur(3)).toString();
			else if (attributeName.equalsIgnoreCase("PURAMP"))
				return ((purAmpEnum) getPurAmp()).toString();
			else if (attributeName.equalsIgnoreCase("STATUS"))
				return ((statusEnum) getStatus()).toString();
			else if (attributeName.equalsIgnoreCase("THRESHOLD"))
				return ((thresholdEnum) getThreshold()).toString();
			else if (attributeName.equalsIgnoreCase("ROISCAENABLE"))
				return ((roiScaEnableEnum) getRoiScaEnable()).toString();
			else if (attributeName.startsWith(channelToEnergyPrefix)) {
				String energy = null;
				if (channelToEnergyConverter == null && converterName != null) {
					channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
				}
				if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
					String channelString = attributeName.substring(channelToEnergyPrefix.length());
					Quantity channel = Quantity.valueOf(channelString);
					energy = ((IQuantityConverter) channelToEnergyConverter).toSource(channel).toString();
					return energy;
				}
				throw new DeviceException(
						"EpicsTCA : unable to find suitable converter to convert channel to energy. converterName  "
								+ converterName == null ? "not given" : converterName);
			} else if (attributeName.startsWith(energyToChannelPrefix)) {
				String channel = null;
				if (channelToEnergyConverter == null && converterName != null) {
					channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
				}
				if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
					String energyString = attributeName.substring(energyToChannelPrefix.length());
					Quantity energy = Quantity.valueOf(energyString);
					channel = ((IQuantityConverter) channelToEnergyConverter).toTarget(energy).toString();
					return channel;
				}
				throw new DeviceException(
						"EpicsTCA : unable to find suitable converter to convert energy to channel. converterName  "
								+ converterName == null ? "not given" : converterName);
			}
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

	private Object getThreshold() throws CAException, TimeoutException, InterruptedException {
		int threshold = -1;
		threshold = controller.cagetEnum(thresholdChannel);
		switch (threshold) {
		case 0:
			return thresholdEnum.Auto;
		case 1:
			return thresholdEnum.Manual;

		}
		return threshold;

	}

	private Object getRoiScaEnable() throws CAException, TimeoutException, InterruptedException {
		int roiScaEnable = -1;
		roiScaEnable = controller.cagetEnum(roiScaEnableChannel);
		switch (roiScaEnable) {
		case 0:
			return roiScaEnableEnum.Disable;
		case 1:
			return roiScaEnableEnum.Enable;

		}
		return roiScaEnable;

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

	private double connectionCheckedGetDouble(EpicsController controller, Channel channel, String name, double defValue)
			throws CAException, TimeoutException, InterruptedException {
		if (channel.getConnectionState() == Channel.CONNECTED) {
			return controller.cagetDouble(channel);
		}
		logger.error("Connection to " + name + " failed");
		return defValue;
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
		return connectionCheckedGetDouble(controller, scalerCalChannel, scalerCalChannelName, 0);
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
				setPurEnable(purEnableEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCACAL"))
				setScalerCAL(((Double) value).doubleValue());
			else if (attributeName.equalsIgnoreCase("SCAENABLE"))
				setScalerEnable(scalerEnableEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("POLARITY"))
				setPolarity(polarityEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA1GATE"))
				setScalerGate(1, scalerGateEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA2GATE"))
				setScalerGate(2, scalerGateEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA3GATE"))
				setScalerGate(3, scalerGateEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("TCASELECT"))
				setTcaSelect(tcaSelectEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA1PUR"))
				setScalerPur(1, scalerPurEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA2PUR"))
				setScalerPur(2, scalerPurEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("SCA3PUR"))
				setScalerPur(3, scalerPurEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("PURAMP"))
				setPurAmp(purAmpEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("STATUS"))
				setStatus(statusEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("THRESHOLD"))
				setThreshold(thresholdEnum.valueOf((String) value));
			else if (attributeName.equalsIgnoreCase("ROISCAENABLE"))
				setRoiScaEnable(roiScaEnableEnum.valueOf((String) value));
		} catch (Throwable th) {
			throw new DeviceException("failed to set attribute", th);
		}
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 */
	public void setScalerHigh(int scalerNo, double value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1HighChannel, value);
			break;
		case 2:
			controller.caput(scaler2HighChannel, value);
			break;
		case 3:
			controller.caput(scaler3HighChannel, value);
			break;
		}
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setScalerLow(int scalerNo, double value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1LowChannel, value);
			break;
		case 2:
			controller.caput(scaler2LowChannel, value);
			break;
		case 3:
			controller.caput(scaler3LowChannel, value);
			break;
		}

	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPurEnable(purEnableEnum value) throws CAException, InterruptedException {
		controller.caput(purEnableChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setThreshold(thresholdEnum value) throws CAException, InterruptedException {
		controller.caput(thresholdChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setRoiScaEnable(roiScaEnableEnum value) throws CAException, InterruptedException {
		controller.caput(roiScaEnableChannel, value.ordinal());
	}

	/**
	 * @param scalerNo
	 * @param value
	 * @throws CAException
	 */
	public void setScalerGate(int scalerNo, scalerGateEnum value) throws CAException, InterruptedException {
		switch (scalerNo) {
		case 1:
			controller.caput(scaler1GateChannel, value.ordinal());
			break;
		case 2:
			controller.caput(scaler2GateChannel, value.ordinal());
			break;
		case 3:
			controller.caput(scaler3GateChannel, value.ordinal());
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
			controller.caput(scaler1PurChannel, value.ordinal());
			break;
		case 2:
			controller.caput(scaler2PurChannel, value.ordinal());
			break;
		case 3:
			controller.caput(scaler3PurChannel, value.ordinal());
			break;
		}
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setScalerEnable(scalerEnableEnum value) throws CAException, InterruptedException {
		controller.caput(scalerEnableChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setTcaSelect(tcaSelectEnum value) throws CAException, InterruptedException {
		controller.caput(tcaSelectChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setScalerCAL(double value) throws CAException, InterruptedException {
		controller.caput(scalerCalChannel, value);
	}

	/**
	 * @param value
	 */
	public void setScalerROIEnable(@SuppressWarnings("unused") boolean value) {
		// TODO implement
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setStatus(statusEnum value) throws CAException, InterruptedException {
		controller.caput(statusChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPolarity(polarityEnum value) throws CAException, InterruptedException {
		controller.caput(polarityChannel, value.ordinal());
	}

	/**
	 * @param value
	 * @throws CAException
	 */
	public void setPurAmp(purAmpEnum value) throws CAException, InterruptedException {
		controller.caput(purAmpChannel, value.ordinal());
	}

	@Override
	public void initializationCompleted() {
		logger.info("Motor - " + getName() + " initialized.");
	}

	/**
	 * @return the epics record name
	 */
	public String getEpicsTcaRecordName() {
		return epicsTcaRecordName;
	}

	/**
	 * @param epicsTcaRecordName
	 */
	public void setEpicsTcaRecordName(String epicsTcaRecordName) {
		this.epicsTcaRecordName = epicsTcaRecordName;
	}

	/**
	 * @return converter name
	 */
	public String getCalibrationName() {
		return converterName;
	}

	/**
	 * @param calibrationName
	 */
	public void setCalibrationName(String calibrationName) {
		this.converterName = calibrationName;
	}

	private String deviceName;

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
	 * @return PV
	 * @throws InterfaceException
	 */
	public String getPV() throws InterfaceException {
		return GDAEpicsInterfaceReader.getPVFromSimplePVType(getDeviceName());
	}
}
