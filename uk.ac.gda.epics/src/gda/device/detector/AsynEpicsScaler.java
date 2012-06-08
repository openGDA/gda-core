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

import gda.device.Detector;
import gda.device.DeviceException;
import gda.epics.connection.EpicsController;
import gda.epics.connection.STSHandler;
import gda.epics.util.JCAUtils;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
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
 * Epics scaler class.
 */
public class AsynEpicsScaler extends gda.device.detector.DetectorBase implements Detector, MonitorListener, ConnectionListener,
		PutListener {
	
	private static final Logger logger = LoggerFactory.getLogger(AsynEpicsScaler.class);
	
	private Channel count = null;

	private Channel frequencyChannel = null;

	private Channel numberOfChannels = null;

	private Channel scalerState = null;

	private Channel timePreset = null;

	private String pvName;

	private Channel[] presetValues = null;

	private Channel[] scalerValues = null;

	private Channel[] scalerNames = null;

	private EpicsController controller;

	private String epicsRecordName;

	private EpicsRecord epicsRecord;

	private String recordName;

	private double frequency;


	private int scalerStatus;

	private HashSet<Channel> monitorInstalledSet;

	/**
	 * Constructor.
	 */
	public AsynEpicsScaler() {
		controller = EpicsController.getInstance();
	}

	/**
	 * initialisation - create all channels to PVs and setup monitoring.
	 * 
	 * @throws FactoryException
	 */
	@Override
	public void configure() throws FactoryException {
		// debug the Thread
		monitorInstalledSet = new HashSet<Channel>();
		epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsRecordName);
		recordName = epicsRecord.getFullRecordName();
		if (!configured) {
			try {
				// PV to start or stop counting
				count = controller.createChannel(recordName + ".CNT", this);
				// The frequency (in Hz) of the clock signal counted by scaler 1
				frequencyChannel = controller.createChannel(recordName + ".FREQ", this);
				// The number of channels actually supported by the underlying
				// hardware
				numberOfChannels = controller.createChannel(recordName + ".NCH", this);
				scalerState = controller.createChannel(recordName + ".SS", this);
				// This field is a proxy for the preset field, PR1, associated
				// with
				// scaler 1. Whenever TP changes, the record will set PR1 =
				// TP*FREQ
				timePreset = controller.createChannel(recordName + ".TP", this);

			} catch (Throwable th) {
				throw new FactoryException("faield to create channels", th);
			}

			if (presetValues != null) {
				// destroy all channels
			}
			if (scalerValues != null) {
				// destroy all channels
			}
			if (scalerNames != null) {
				// destroy all channels
			}

			// countState = controller.setMonitor(count, this);

			configured = true;

		}

	}

	/**
	 * @return epicsRecordName
	 */
	public String getEpicsRecordName() {
		return epicsRecordName;
	}

	/**
	 * @param epicsRecordName
	 */
	public void setEpicsRecordName(String epicsRecordName) {
		this.epicsRecordName = epicsRecordName;
	}

	/**
	 * @param pvName
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * @return pvName
	 */
	public String getPvName() {
		return pvName;
	}

	/**
	 * Gets the total number of channels actually supported by the scaler hardware
	 * 
	 * @return integer number of channels
	 * @throws DeviceException
	 */
	public int getTotalChans() throws DeviceException {
		try {
			return controller.cagetShort(numberOfChannels);
		} catch (Throwable th) {
			throw new DeviceException("failed to get total of channels", th);
		}
	}

	/**
	 * Loads the user set preset values to the scaler. If scaler n has been designated as a preset scaler , then when
	 * the scaler reaches the preset count , all scalers will be disabled, and the record will report counting has
	 * completed . When Preset changes to any positive value, the record will set gate of that corresponding scaler to
	 * 1.
	 * 
	 * @param value
	 *            double array of the preset values
	 * @throws DeviceException
	 */
	public void loadPresetChans(double[] value) throws DeviceException {
		try {
			int channels = getTotalChans();
			for (int i = 0; i < channels; i++)
				controller.caput(presetValues[i], value[i], this);
		} catch (Throwable th) {
			throw new DeviceException("failed to load preset channels", th);
		}
	}

	/**
	 * Starts the counting in the Scaler
	 * 
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		try {
			controller.caput(count, 1, this);
		} catch (Throwable th) {
			throw new DeviceException("failed to start", th);
		}
	}

	/**
	 * Stops the counting in the Scaler {@inheritDoc}
	 * 
	 * @see gda.device.scannable.ScannableBase#stop()
	 */
	@Override
	public void stop() throws DeviceException {
		try {
			controller.caput(count, 0, this);
		} catch (Throwable th) {
			throw new DeviceException("failed to stop", th);
		}
	}

	/**
	 * Reads the counts accumulated by all the scalers
	 * 
	 * @return a double array of the scaler counts
	 * @throws DeviceException
	 */
	public double[] readChans() throws DeviceException {
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
			throw new DeviceException("failed to read channels", th);
		}
	}

	/**
	 * Sets the name of the Channel
	 * 
	 * @param channel
	 *            integer channel number
	 * @param label
	 *            name to be associated with that channel
	 * @throws DeviceException
	 */
	public void setChannelLabel(int channel, String label) throws DeviceException {
		try {
			controller.caput(scalerNames[channel - 1], label, this);
		} catch (Throwable th) {
			throw new DeviceException("failed to set channel label", th);
		}

	}

	/**
	 * Gets the names of all the channels as a list
	 * 
	 * @return list of all the channel names
	 */
	@Override
	public String[] getExtraNames() {
		int channels = scalerNames.length;
		String[] channelNames = new String[channels];
		try {
			for (int i = 0; i < channels; i++) {
				channelNames[i] = "error while getting list";
			}
			for (int i = 0; i < channels; i++) {
				channelNames[i] = controller.cagetString(scalerNames[i]);
			}
		} catch (Throwable th) {
			logger.error("failed to get channel label list", th);
		}
		return channelNames;
	}

	@Override
	public void collectData() throws DeviceException {
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
			controller.caput(timePreset, time, this);
		} catch (Throwable th) {
			throw new RuntimeException("failed to set collection table", th);
		}

	}

	@Override
	public int getStatus() throws DeviceException {
		try {
			short countValue = controller.cagetEnum(count);
			if (countValue == 0) {
				return Detector.IDLE;
			}
			return Detector.BUSY;
		} catch (Throwable th) {
			throw new DeviceException("failed to get status", th);
		}
	}

	@Override
	public double[] readout() throws DeviceException {
		return readChans();
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
	public void monitorChanged(MonitorEvent ev) {
		if (((Channel) ev.getSource()).getName() == count.getName()) {
			DBR dbr = ev.getDBR();
			if (dbr.isENUM()) {
				scalerStatus = ((DBR_Enum) dbr).getEnumValue()[0];
				if (scalerStatus == 1) {
					logger.info("Scaler is in counting.");
				} else if (scalerStatus == 0) {
					logger.info("Scaler counting completed.");
				}
				notifyIObservers("ScalerStatus", scalerStatus);
			} else {
				logger.debug("Error: .CNT should return ENUM type value.");
			}
		} // END of Moto

	}

	@Override
	public void connectionChanged(ConnectionEvent ev) {
		if (ev.isConnected()) {
			if (ev.getSource() == numberOfChannels) {
				int channels = 0;
				try {
					channels = getTotalChans();
				} catch (DeviceException e) {
					logger.error("Warning >>> " + getName() + "  scaler number of channels update failed.");
				}

				// TODO remove all recreated monitors from monitorInstalledSet
				// TODO why even to recreate

				try {
					// Preset values for the associated scalers
					presetValues = new Channel[channels];
					// The counts accumulated by the scalers
					scalerValues = new Channel[channels];
					// Names the user has given to individual scaler
					// channels
					scalerNames = new Channel[channels];
					for (int i = 0; i < channels; i++) {
						presetValues[i] = controller.createChannel(recordName + ".PR" + (i + 1), this);
						scalerValues[i] = controller.createChannel(recordName + ".S" + (i + 1), this);
						scalerNames[i] = controller.createChannel(recordName + ".NM" + (i + 1), this);
					}
				} catch (Throwable th) {
					logger.error("Warning >>> " + getName() + "  failed to recreate chanels.");
				}
			}
			if (ev.getSource() == frequencyChannel) {
				try {
					controller.caput(frequencyChannel, this.frequency, this);
				} catch (Throwable th) {
					logger.error("Warning >>> " + getName() + " failed to set frequency.");
				}
			}
			if (ev.getSource() == scalerState) {
				try {
					scalerStatus = getStatus();
				} catch (DeviceException e) {
					logger.error("Warning >>> " + getName() + " scaler status update failed.");
				}
			}
		}

		onConnectionChanged(ev);
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
			logger.info(ch.getName() + " : Put completed successfully >>> " + pev.getStatus().getMessage());
		} else if (pev.getStatus().isError()) {
			logger.error(ch.getName() + " : Put Error >>> " + pev.getStatus().getMessage());
		}

		else if (pev.getStatus().isFatal()) {
			logger.error(ch.getName() + " : Fatal Error >>> " + pev.getStatus().getMessage());
		} else {
			logger.info(ch.getName() + " : Put Warning >>> " + pev.getStatus().getMessage());
		}
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
		return "EPICS";
	}

}
