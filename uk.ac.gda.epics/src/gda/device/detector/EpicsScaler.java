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

package gda.device.detector;

import java.lang.reflect.Array;

import org.python.core.PySequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.TimeoutException;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Epics scaler class.
 */
public class EpicsScaler extends DetectorBase implements InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsScaler.class);

	private boolean initialised = false;

	private static final long serialVersionUID = 1L;

	private Channel count = null;

	private Channel countMode=null;

	private Channel numberOfChannels = null;

	private Channel timePreset = null;

	private String pvName;

	private Channel[] presetValues = null;

	private Channel[] scalerValues = null;

	private Channel[] scalerNames = null;

	private EpicsController controller;

	private EpicsChannelManager channelManager;

	private double frequency;

	private volatile int scalerStatus;

	private volatile int totalChannels = -1;

	/*
	 * cache of channel names read from EPICS. If the list of channels is to be changed at run time then this will
	 * cache will need to be refreshed
	 */
	private String[] channelNameValues;

	private boolean controlCountMode;

	public static enum CountMode{
		oneShot,
		autoCount
	}
	/**
	 * Constructor.
	 */
	public EpicsScaler() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		setStatus(Detector.IDLE);
		setInputNames(new String[]{});
	}

	/**
	 * {@inheritDoc} initialisation - create all channels to PVs and setup monitoring.
	 *
	 */
	@Override
	public void configure() throws FactoryException {
		if (!isConfigured()) {
			if (pvName == null) {
				throw new IllegalStateException("No pvName configured for EpicsScaler '" + getName() + "'");
			}

			createChannelAccess();
			setConfigured(true);
		}
	}

	private void createChannelAccess() throws FactoryException {
		try {
			// PV to start or stop counting
			count = channelManager.createChannel(pvName + ".CNT", new CNTMonitorListener(), false);
			if (isControlCountMode()) {
				countMode = channelManager.createChannel(pvName + ":AutoCount");
			}
			// The number of channels actually supported by the underlying hardware
			numberOfChannels = channelManager.createChannel(pvName + ".NCH", false);
			// This field is a proxy for the preset field, PR1, associated with
			// scaler 1. Whenever TP changes, the record will set PR1 = TP*FREQ
			timePreset = channelManager.createChannel(pvName + ".TP", false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
			channelManager.tryInitialize(100);
		} catch (Throwable th) {
			throw new FactoryException("Scaler - " + getName() + " faield to create control channels", th);
		}
	}

	public CountMode getCountMode() throws DeviceException {
		if (!isControlCountMode()) throw new IllegalStateException("Count Mode control is not supported!");
		try {
			int countmode = controller.cagetInt(countMode);
			if (countmode==CountMode.oneShot.ordinal()) {
				return CountMode.oneShot;
			} else if (countmode==CountMode.autoCount.ordinal()) {
				return CountMode.autoCount;
			} else {
				logger.error("{}: count mode {} is not supported.", getName(), countmode);
				throw new DeviceException(getName() + ": Count Mode from scaler is not supported!");
			}
		} catch (TimeoutException | CAException | InterruptedException e) {
			logger.error("Failed to get Count Mode from scaler", e);
			throw new DeviceException("Failed to get Count Mode from scaler", e);
		}
	}

	public void setCountMode(CountMode value) throws DeviceException {
		if (!isControlCountMode()) throw new IllegalStateException("Count Mode control is not supported!");
		try {
			controller.caput(countMode, value.ordinal());
		} catch (CAException | InterruptedException e) {
			logger.error("Failed to set Count Mode", e);
			throw new DeviceException("Failed to set Count Mode to "+value.name(), e);
		}
	}

	/**
	 * Gets the total number of channels actually supported by the scaler hardware
	 *
	 * @return integer number of channels
	 * @throws DeviceException
	 */
	public int getTotalChans() throws DeviceException {
		if (totalChannels < 0) {
			try {
				totalChannels = controller.cagetShort(numberOfChannels);
			} catch (Throwable th) {
				throw new DeviceException("failed to get total number of channels", th);
			}
		}
		return totalChannels;
	}

	/**
	 * Loads the user set preset values to the scaler channels. If scaler channel n has been designated as a preset
	 * scaler channel , then when the scaler channel reaches the preset count , all scaler channels will be disabled,
	 * and the record will report counting has completed . When Preset changes to any positive value, the record will
	 * set gate of that corresponding scaler channel to 1.
	 *
	 * @param value
	 *            double array of the preset values
	 * @throws DeviceException
	 */
	public void loadPresetChans(double[] value) throws DeviceException {
		CheckInitialised("loadPresetChans");
		try {
			int channels = getTotalChans();
			for (int i = 0; i < channels; i++)
				controller.caput(presetValues[i], value[i]);
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.loadPresetChans: failed to load preset channels", th);
		}
	}

	/**
	 * Starts the counting in the Scaler
	 *
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		CheckInitialised("start");
		try {
			controller.caput(count, 1);
			// We need to set the status to BUSY else a check that it is BUSY
			// straight after this call may show that it is not
			// Such an issue has been observed.
			setStatus(Detector.BUSY);
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.start: failed to start", th);
		}
	}

	/**
	 * Stops the counting in the Scaler
	 *
	 * @throws DeviceException
	 */
	@Override
	public void stop() throws DeviceException {
		CheckInitialised("stop");
		try {
			controller.caput(count, 0);
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.stop: failed to stop", th);
		}
	}

	/**
	 * Reads the counts accumulated by all the scalers
	 *
	 * @return a double array of the scaler counts
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
		CheckInitialised("readChans");
		try {
			int channels = getTotalChans();
			double[] channelValues = new double[channels];
			for (int i = 0; i < channels; i++) {
				// date type returned is long. may need to be revisited at some
				// point
				channelValues[i] = controller.cagetInt(scalerValues[i]);
			}
			return channelValues;
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.readChans: failed to read channels", th);
		}
	}

	/**
	 * Gets the user set name of the Channel
	 *
	 * @param channel
	 *            integer channel number - zero based
	 * @return the Name of the channel associated with a channel number
	 * @throws DeviceException
	 */
	public String getChannelLabel(int channel) throws DeviceException {
		CheckInitialised("getChannelLabel");
		try {
			return controller.cagetString(scalerNames[channel]);
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.setChannelLabel: failed to get channel label - channel = "
					+ Integer.toString(channel), th);

		}
	}

	/**
	 * Sets the name of the Channel
	 *
	 * @param channel
	 *            integer channel number - zero based
	 * @param label
	 *            name to be associated with that channel
	 * @throws DeviceException
	 */
	public void setChannelLabel(int channel, String label) throws DeviceException {
		CheckInitialised("setChannelLabel");
		try {
			controller.caput(scalerNames[channel], label);
		} catch (Throwable th) {
			throw new DeviceException("EpicsScaler.setChannelLabel: failed to set channel label - channel = "
					+ Integer.toString(channel) + " label = " + label, th);
		}

	}

	/**
	 * Gets the names of all the channels as a list
	 *
	 * @return list of all the channel names
	 */
	@Override
	public String[] getExtraNames() {
		if (!isConfigured()){
			return new String[]{};
		}
		try {
			CheckInitialised("getChannelLabelList");
		} catch (DeviceException e) {
			logger.error("failed to get channel label list", e);
			return new String[]{};
		}
		if( channelNameValues == null){
			int channels = scalerNames.length;
			String[] channelNames = new String[channels];
			try {
				for (int i = 0; i < channels; i++) {
					channelNames[i] = controller.cagetString(scalerNames[i]);
				}
				//only copy to cache if read successfully
				channelNameValues = channelNames;
			} catch (Throwable th) {
				logger.error("failed to get channel label list", th);
			}
			return channelNames;
		}
		return channelNameValues;
	}

	/*
	 * public void setAttribute(String attributeName, Object value) throws DeviceException { } public Object
	 * getAttribute(String attributeName) throws DeviceException { return null; }
	 */

	@Override
	public void collectData() throws DeviceException {
		CheckInitialised("collectData");
		start();
	}

	/**
	 * Sets the collection time for the scalers
	 *
	 * @param time
	 *            period to count
	 */
	@Override
	public void setCollectionTime(double time) {
		try {
			CheckInitialised("setCollectionTime");
			controller.caput(timePreset, time);
		} catch (Throwable th) {
			throw new RuntimeException("EpicsScaler.setCollectionTime: failed to set collection time", th);
		}

	}

	/**
	 * Gets the collection time from the scalers
	 *
	 * @return double collectionTime
	 */
	@Override
	public double getCollectionTime() {
		double value;
		try {
			value = controller.cagetDouble(timePreset);
		} catch (Throwable th) {
			throw new RuntimeException("EpicsScaler.setCollectionTime: failed to set collection time", th);
		}
		return value;
	}

	private void setStatus(int scalerStatus) {
		this.scalerStatus = scalerStatus;
	}

	@Override
	public int getStatus() {
		return scalerStatus;
	}

	@Override
	public double[] readout() throws DeviceException {
		return readChans();
	}

	/**
	 * @param channel
	 * @return data
	 * @throws DeviceException
	 */
	public int readout(int channel) throws DeviceException {
		int value;
		try {
			value = controller.cagetInt(scalerValues[channel]);
		} catch (Throwable th) {
			throw new DeviceException("failed to get detector value on channel " + channel, th);
		}
		return value;

	}

	/**
	 * Gets the clock frequency of the Scaler
	 *
	 * @return double frequency
	 */
	public double getFrequency() {
		return frequency;
	}

	/**
	 * Sets the clock frequency of the scaler
	 *
	 * @param frequency
	 */
	public void setFrequency(double frequency) {
		this.frequency = frequency;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = { getTotalChans() };
		return dims;
	}

	private class CNTMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent ev) {
			DBR dbr = ev.getDBR();
			if (dbr.isENUM()) {
				setStatus(((DBR_Enum) dbr).getEnumValue()[0] == 1 ? Detector.BUSY : Detector.IDLE);
				notifyIObservers("ScalerStatus", getStatus());
			} else {
				logger.error("EpicsScaler.monitorChanged: Error - .CNT should return ENUM type value.");
			}
		} // END of Moto

	}

	/**
	 * adds a monitor to the specified channel
	 *
	 * @param channel
	 * @param l
	 * @throws DeviceException
	 */
	public void addMonitor(int channel, MonitorListener l) throws DeviceException {
		try {
			scalerValues[channel - 1].addMonitor(Monitor.VALUE, l);
		} catch (Throwable e) {
			throw new DeviceException("Failed to add monitor to channel " + channel, e);
		}
	}

	private void CheckInitialised(String nameOfCaller) throws DeviceException {
		if (!initialised) {
			throw new DeviceException("EpicsScaler." + nameOfCaller + " : - not yet initialised");
		}
	}

	@Override
	public void initializationCompleted() {
		int channels = 0;
		try {
			channels = getTotalChans();
		} catch (DeviceException e) {
			logger.error("Scaler - " + getName() + "  scaler number of channels update failed.");
		}

		try {
			// Preset values for the associated scalers
			presetValues = new Channel[channels];
			// The counts accumulated by the scalers
			scalerValues = new Channel[channels];
			// Names the user has given to individual scaler channels
			scalerNames = new Channel[channels];
			for (int i = 0; i < channels; i++) {
				presetValues[i] = channelManager.createChannel(pvName + ".PR" + (i + 1), false);
				scalerValues[i] = channelManager.createChannel(pvName + ".S" + (i + 1), false);
				scalerNames[i] = channelManager.createChannel(pvName + ".NM" + (i + 1), false);
			}
		} catch (Throwable th) {
			logger.error("Scaler - " + getName() + "  failed to create individual channels.", th);
		}
		initialised = true;
		logger.info("Scaler - " + getName() + " is initialised.");
	}

	/**
	 * Does the same job as the other formatPosition method except rather than using a supplied format string, use the
	 * index of the array of formats this object holds. This is to be used when an object has multiple elements which
	 * descibe its position and those element require different formatting.
	 *
	 * @param format
	 *            the index in the array of formats to use
	 * @param number
	 *            the number to format
	 * @return a formatted string
	 */
	public String formatPosition(int format, double number) {
		if (format < outputFormat.length) {
			return String.format(outputFormat[format], number);
		}
		return String.format(outputFormat[0], number);
	}

	@Override
	public String toString() {
		try {
			String myString = "";
			Object position = this.getPosition();

			// print out simple version if only one inputName and
			// getPosition and getReportingUnits do not return arrays.
			if (!(position.getClass().isArray() || position instanceof PySequence)) {
				myString += this.getName() + " : " + this.getPosition();
			} else {
				myString += getName() + " : ";
				if (position instanceof PySequence) {
					for (int i = 0; i < ((PySequence) position).__len__(); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += this.formatPosition(i, Double.parseDouble(((PySequence) position).__finditem__(i)
								.toString()));
					}
				} else {
					for (int i = 0; i < Array.getLength(position); i++) {
						if (i > 0) {
							myString += " ";
						}
						myString += this.formatPosition(i, Double.parseDouble(Array.get(position, i).toString()));
					}
				}
			}
			return myString;
		} catch (Exception e) {
			logger.warn("{}: exception while getting value", getName(), e);
			return valueUnavailableString();
		}
	}

	/**
	 * @param pvName
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * @return pv name
	 */
	public String getPvName() {
		return pvName;
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS scaler";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "unknown";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return "EPICS";
	}

	public boolean isControlCountMode() {
		return controlCountMode;
	}

	public void setControlCountMode(boolean controlCountMode) {
		this.controlCountMode = controlCountMode;
	}

}
