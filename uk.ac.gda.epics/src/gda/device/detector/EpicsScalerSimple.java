/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.ReturnType;
import gda.factory.FactoryException;
import gov.aps.jca.CAStatus;

/**
 * <pre>
 * Class to allow an EpicsScaler to be used as a detector in GDA
 * CollectData - sends 1 to .CNT field
 * IsBusy - returns True whilst .CNT field remains 1
 * Data is read from .S<n> field.
 * The values of n are set in setChannelsToBeRead
 * The extraNames for the n values are set in setExtraNames.
 * Readout returns an Integer array of length equal to the number of channels to be read
 * </pre>
 */
public class EpicsScalerSimple extends DetectorBase {

	private static final Logger logger = LoggerFactory.getLogger(EpicsScalerSimple.class);

	private static final String RECORD = "";
	private static final String SCALER_FIELD = ".S";
	private static final String START = ".CNT";
	private static final String TP = ".TP";
	FindableEpicsDevice epicsDevice;
	protected ArrayList<Integer> channelsToBeRead = new ArrayList<Integer>();
	protected String[] fieldsToBeRead;
	private boolean waitingForPutToStart = false;

	/**
	 * @return Returns the channelsToBeRead.
	 */
	public ArrayList<Integer> getChannelsToBeRead() {
		return channelsToBeRead;
	}

	/**
	 * @param channelsToBeRead
	 *            The channelsToBeRead to set.
	 */
	public void setChannelsToBeRead(ArrayList<Integer> channelsToBeRead) {
		this.channelsToBeRead = channelsToBeRead;
		fieldsToBeRead = new String[channelsToBeRead.size()];
		int index = 0;
		for (Integer chan : channelsToBeRead) {
			fieldsToBeRead[index] = SCALER_FIELD + chan;
			index++;
		}
	}

	@Override
	public void collectData() throws DeviceException {
		try{
			if (epicsDevice != null) {
				//set to true before call to setValue to ensure the change in PutListener is last
				waitingForPutToStart=true;

				epicsDevice.setValue(RECORD, START, (short) 1, 5, (arg0) -> {
					if( arg0 == null ){
						//complete in dummy mode
					} else {
						if( arg0.getStatus() != CAStatus.NORMAL){
							logger.error("Error in collectData for {}", getName());
						}
					}
					waitingForPutToStart = false;
				});

				if(epicsDevice.getDummy()){
					Thread.sleep((long)(getCollectionTime()*1000));
					epicsDevice.setValue(RECORD, START, (short) 0);
				}
			}
		} catch (Exception e){
			waitingForPutToStart = false;
			throw new DeviceException("Error in collectData",e);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		if (epicsDevice != null) {
			return !waitingForPutToStart && (Short)epicsDevice.getValue(ReturnType.DBR_NATIVE, RECORD, START) == 0 ? Detector.IDLE
					: Detector.BUSY;
		}
		return Detector.IDLE;
	}

	/**
	 * method used for testing only
	 */
	public void _setFieldValue(int fieldIndex, Double val) throws DeviceException{
		if (epicsDevice != null) {
			epicsDevice.setValue(RECORD, fieldsToBeRead[fieldIndex], val);
		}
	}

	@Override
	public Object readout() throws DeviceException {
		Double[] channelReadings = new Double[fieldsToBeRead.length];
		for (int index = 0; index < fieldsToBeRead.length; index++) {

			if (epicsDevice == null) {
				channelReadings[index] = (double) ThreadLocalRandom.current().nextInt(0, 10 + 1);
			}
			else {
				channelReadings[index] = (Double) (epicsDevice.getValue(ReturnType.DBR_NATIVE, RECORD,
						fieldsToBeRead[index]));
			}
		}
		return channelReadings;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return getDetectorType();
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return getDetectorType();
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return this.getClass().getName();
	}

	/**
	 * @return Returns the epicsDevice.
	 */
	public FindableEpicsDevice getEpicsDevice() {
		return epicsDevice;
	}

	/**
	 * @param epicsDevice
	 *            The epicsDevice to set.
	 */
	public void setEpicsDevice(FindableEpicsDevice epicsDevice) {
		this.epicsDevice = epicsDevice;
	}

	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			super.configure();
			setInputNames(new String[0]);
			if (channelsToBeRead.size() != extraNames.length) {
				throw new FactoryException("EpicsScaler:" + getName()
						+ ". channelsToBeRead.size() != extraNames.length ");
			}
			if (epicsDevice == null) {
				if (LocalProperties.get(LocalProperties.GDA_MODE).equals("live")) {
					throw new FactoryException("EpicsScaler:" + getName() + ". epicsDevice not set");
				}

				logger.warn("{} has no epicsDevice configured. This is only valid for dummy mode and will return random values instead.", getName());
			}
			else if (epicsDevice.getDummy()) {
				setConfigured(true);
				//set configured so we can set default values in dummy mode
				try {
					epicsDevice.setValue(RECORD, START, (short) 0);
					for (int index = 0; index < fieldsToBeRead.length; index++) {
						epicsDevice.setValue(RECORD, fieldsToBeRead[index], new Double(index));
					}
					setCollectionTime(1.0);
				} catch (Exception ex) {
					throw new FactoryException("EpicsScaler:" + getName() + ". Setting setting up dummy epicsdevice",
							ex);
				}
			}
			setConfigured(true);
		}
	}
	private void checkInitialised(String nameOfCaller) throws DeviceException {
		if (!isConfigured()) {
			throw new DeviceException("EpicsScalerSimple " + getName() + ":" + nameOfCaller + " : - not yet configured");
		}
	}
	/**
	 * Sets the collection time for the scalers
	 *
	 * @param time
	 *            period to count
	 */
	@Override
	public void setCollectionTime(double time){
		try {
			checkInitialised("setCollectionTime");
			if(epicsDevice != null) {
				epicsDevice.setValue(RECORD, TP, time);
			}
		} catch (Throwable th) {
			throw new RuntimeException("EpicsSimpleScaler.setCollectionTime: failed to set collection time:" + getName(), th);
		}
	}

	/**
	 * Gets the collection time from the scalers
	 *
	 * @return double collectionTime
	 */
	@Override
	public double getCollectionTime() {
		try {
			checkInitialised("getCollectionTime");
			if (epicsDevice != null) {
				return (Double)epicsDevice.getValue(ReturnType.DBR_NATIVE, RECORD, TP);
			}
			else {
				return 0.;
			}
		} catch (Throwable th) {
			throw new RuntimeException("getCollectionTime: failed to get collection time:" + getName(), th);
		}
	}
 }
