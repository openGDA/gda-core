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

package gda.device.detector.analyser;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.SimpleMcaType;
import gda.epics.xml.EpicsRecord;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.factory.Findable;
import gda.factory.Finder;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to communicate with an epics MCA record. The MCA record controls and acquires data from a multi-channel
 * analyser (MCA).
 * 
 * @see <a href="http://cars9.uchicago.edu/software/epics/mcaRecord.html">MCARecord</a>
 * @since 7.3
 */
public class EpicsMcaController extends DeviceBase implements Configurable, Findable, InitializationListener {

	private static final Logger logger = LoggerFactory.getLogger(EpicsMcaController.class);

	/**
	 * acquisition flag
	 */
	private boolean acquisitionDone = true;

	/**
	 * reading flag
	 */
	private boolean readingDone = true;

	/**
	 * triggers for channel advance
	 */
	public enum ChannelAdvanceSource {
		/**
		 * using internal clock
		 */
		INTERNAL,
		/**
		 * using external trigger
		 */
		EXTERNAL
	}

	/**
	 * MCA modes
	 */
	public enum Mode {
		/**
		 * Pulse-Height Analysis mode. Each number presented to the hardware is interpreted as the number of channel
		 * whose contents are to be incremented by one.
		 */
		PHA,

		/**
		 * Multichannel scaler mode. The MCA maintains a "current channel" number, which increases either with time or
		 * in response to a channel-advance signal, and increments the current channel's contents by one (or by the
		 * number presented to the MCA input, depending on the hardware) on each event.
		 */
		MCS,

		/**
		 * List mode. The MCA simply records each event in the data array.
		 */
		List
	}

	// control fields
	/**
	 * erase and start acquire, 1 start, 0 rest
	 */
	private Channel eraseStartAcqChannel;

	/**
	 * start acquire, 1 start, 0 rest
	 */
	private Channel startAcqChannel;

	/**
	 * stop acquire, 1 stop, 0 rest
	 */
	private Channel stopAcqChannel;

	/**
	 * acquisition status, 1 acquiring, 0 done
	 */
	private Channel statusChannel;

	/**
	 * process record,
	 */
	private Channel procChannel;

	/**
	 * reading status
	 */
	@SuppressWarnings("unused")
	private Channel rdnsChannel;

	/**
	 * read array, 1 to start read
	 */
	private Channel readChannel;

	/**
	 * reading array status, 1 read, 0 done
	 */
	@SuppressWarnings("unused")
	private Channel rdngChannel;

	/**
	 * hardware to erase data array, 1 to erase
	 */
	private Channel eraseChannel;

	/**
	 * channel-advance source, in MCS mode, 0 internal, 1 external
	 */
	private Channel chasChannel;

	/**
	 * Dwell time per channel in MCS mode and CHAS=Internal, float
	 */
	private Channel dwellTimeChannel;

	/**
	 * number of channels to use, long
	 */
	private Channel numberChannelsToUseChannel;

	/**
	 * Time sequence number for memory region to use
	 */
	private Channel sequenceChannel;

	/**
	 * Channel advance pre-scale factor in MCS and external mode, default=1
	 */
	private Channel psclCh;

	/**
	 * preset real time - seconds to acquire data, 0.0 to ignore
	 */
	private Channel presetRealTimeChannel;

	/**
	 * preset live time, seconds when hardware ready to accept data, 0.0 to ignore
	 */
	@SuppressWarnings("unused")
	private Channel presetLiveTimeChannel;

	/**
	 * preset counts, halt acquisition when sum of the numbers of counts in channels PCTL to PCTH reaches this value
	 */
	private Channel presetCountsChannel;

	/**
	 * low channel for preset counts
	 */
	private Channel presetCountLowChannel;

	/**
	 * high channel for preset counts
	 */
	private Channel presetCountHighChannel;

	/**
	 * preset number of sweeps, acquisition halt on reaching this value
	 */
	private Channel presetSweepChannel;

	/**
	 * Mode 0 - PHA, 1 - MCS, 2 - LIST
	 */
	private Channel modeChannel;

	// Status Fields
	/**
	 * elapsed real time reported by hardware
	 */
	private Channel elapsedRealTimeChannel;

	/**
	 * elapsed live time reported by hardware
	 */
	private Channel elapsedLiveTimeChannel;

	/**
	 * average dead time of the detector, which is the percent of time the detector was not able to collect data during
	 * the current elapsed time.
	 */
	private Channel avgDeadTimeChannel;

	/**
	 * instantaneous dead time of the detector, which is the percent of time the detector was not able to collect data
	 * since the previous read status.
	 */
	private Channel instDeadTimeChannel;

	/**
	 * actual counts in preset region
	 */
	private Channel actChannel;

	/**
	 * the time that acquisition was last started as reported by IOC
	 */
	private Channel startTimeChannel;

	// Calibration fields -place for client software to store these values.
	// The relationship between calibrated units (cal) and channel number (chan)
	// is defined as cal=offset+slope*chan+quadratic*chan*chan
	/**
	 * Calibration offset, calibrated value of the first channel in the spectrum
	 */
	private Channel calibrationOffsetChannel;

	/**
	 * Calibration units name
	 */
	private Channel engineeringUnitsChannel;

	/**
	 * Calibration slope
	 */
	private Channel calibrationSlopeChannel;

	/**
	 * calibration quadratic term
	 */
	private Channel calibrationQuadraticChannel;

	/**
	 * the two theta angle of the detector in energy-dispersive diffraction experiments
	 */
	private Channel twoThetaChannel;

	// Value fields
	/**
	 * Maximum number of channels, long
	 */
	private Channel maxNumberChannelsChannel;

	/**
	 * current value, - the data array of the MCA
	 */
	private Channel dataChannel;

	/**
	 * Number of channels read as reported by device-support routine in EPICS.
	 */
	private Channel numberOfChannelsReadChannel;

	// Region-of-interest fields
	/**
	 * low channels for each region
	 */
	private Channel[] roiLowChannels;

	/**
	 * high channel for each region
	 */
	private Channel[] roiHighChannels;

	/**
	 * number of data channels on either side of region used to calculate background
	 */
	private Channel[] roiBackgroundChannels;

	/**
	 * is preset for a region, non-zero means preset on
	 */
	private Channel[] roiPresetChannels;

	/**
	 * total number of counts in the region.
	 */
	private Channel[] roiCountChannels;

	/**
	 * net number of counts in the region.
	 */
	private Channel[] roiNetCountChannels;

	/**
	 * the preset count value at which acquisition halt.
	 */
	private Channel[] roiPresetCountChannels;

	/**
	 * name of each region.
	 */
	private Channel[] roiNameChannels;

	/**
	 * maximum number of ROI
	 */
	public int NUMBER_OF_REGIONS = 32;

	/**
	 * maximum number of channels that the device supports, will be initialised at start.
	 */
	private int MAXIMUM_NUMBER_CHANNELS;

	// cache values in GDA objects
	@SuppressWarnings("unused")
	private double[] roiLowValues;

	@SuppressWarnings("unused")
	private double[] roiHighValues;

	@SuppressWarnings("unused")
	private int[] roiBackgroundValues;

	private double[] roiCountValues;

	private double[] roiNetCountValues;

	@SuppressWarnings("unused")
	private double[] roiPresetCountValues;

	@SuppressWarnings("unused")
	private String[] roiNameValues;

	private double elapsedRealTimeValue;

	private double elapsedLiveTimeValue;

	private double instDeadTimeValue;

	private double deadTimeValue;

	/**
	 * EPICS controller for CA methods
	 */
	private EpicsController controller;

	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	/**
	 * phase I interface GDA-EPICS link name
	 */
	private String epicsMcaRecordName;

	/**
	 * phase I interface GDA-EPICS link object
	 */
	private EpicsRecord epicsMcaRecord;

	/**
	 * EPICS record name for a MCA - the long name e.g.BL11I-....
	 */
	private String mcaRecordName;

	/**
	 * phase II interface GDA-EPICS link parameter
	 */
	private String deviceName;

	/**
	 * Region-of-interest listener
	 */
	private RoiCountsMonitorListener roicountlist;

	/**
	 * Region-of-interest listener
	 */
	private RoiNetCountsMonitorListener roinetlist;

	/**
	 * Elapsed real time Listener
	 */
	private RealTimeMonitorListener rtimelist;

	/**
	 * Elapsed live time listener
	 */
	private LiveTimeMonitorListener ltimelist;

	/**
	 * Elapsed live time listener
	 */
	private DeadTimeMonitorListener dtimelist;

	/**
	 * Elapsed live time listener
	 */
	private InstDeadTimeMonitorListener itimelist;

	/**
	 * Acquisition status listener
	 */
	private ACQGMonitorListener acqglist;

	/**
	 * Reading array status listener
	 */
	private RDNGMonitorListener readingArrayList;

	/**
	 * current value array listener
	 */
	private VALMonitorListener datalist;

	/**
	 * Constructor, initialise this object.
	 */
	public EpicsMcaController() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		roicountlist = new RoiCountsMonitorListener();
		roinetlist = new RoiNetCountsMonitorListener();
		rtimelist = new RealTimeMonitorListener();
		ltimelist = new LiveTimeMonitorListener();
		dtimelist = new DeadTimeMonitorListener();
		itimelist = new InstDeadTimeMonitorListener();
		acqglist = new ACQGMonitorListener();
		readingArrayList = new RDNGMonitorListener();
		datalist = new VALMonitorListener();

		roiLowChannels = new Channel[NUMBER_OF_REGIONS];
		roiLowValues = new double[NUMBER_OF_REGIONS];
		roiHighChannels = new Channel[NUMBER_OF_REGIONS];
		roiHighValues = new double[NUMBER_OF_REGIONS];
		roiBackgroundChannels = new Channel[NUMBER_OF_REGIONS];
		roiBackgroundValues = new int[NUMBER_OF_REGIONS];
		roiPresetChannels = new Channel[NUMBER_OF_REGIONS];

		roiCountChannels = new Channel[NUMBER_OF_REGIONS];
		roiCountValues = new double[NUMBER_OF_REGIONS];
		roiNetCountChannels = new Channel[NUMBER_OF_REGIONS];
		roiNetCountValues = new double[NUMBER_OF_REGIONS];
		roiPresetCountChannels = new Channel[NUMBER_OF_REGIONS];
		roiPresetCountValues = new double[NUMBER_OF_REGIONS];
		roiNameChannels = new Channel[NUMBER_OF_REGIONS];
		roiNameValues = new String[NUMBER_OF_REGIONS];
	}

	/**
	 * Constructor taking a PV name, you must run .configure() to fully initialise the object
	 * 
	 * @param name
	 *            String, the PV name
	 */
	public EpicsMcaController(String name) {
		this();
		setName(name);
	}

	/**
	 * constructor allowing setting of object name and MCA record name, you must run .configure() to fully initialise
	 * the object
	 * 
	 * @param name -
	 *            the object name
	 * @param recordName -
	 *            EPICS MCA record name
	 */
	public EpicsMcaController(String name, String recordName) {
		this();
		setName(name);
		this.mcaRecordName = recordName;
	}

	/**
	 * object initialisation - create all EPICS channels required
	 * 
	 * @see gda.device.DeviceBase#configure()
	 */
	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			if (getEpicsMcaRecordName() != null) {
				// phase I beamlines interface using GDA's epicsRecordName
				if ((epicsMcaRecord = (EpicsRecord) Finder.getInstance().find(epicsMcaRecordName)) != null) {
					mcaRecordName = epicsMcaRecord.getFullRecordName();
					createChannelAccess(mcaRecordName);
					channelManager.tryInitialize(100);
				} else {
					logger.error("Epics Record " + epicsMcaRecordName + " not found");
					throw new FactoryException("Epics Record " + epicsMcaRecordName + " not found");
				}
			} else if (getDeviceName() != null) {
				// phase II beamlines interface using GDA's deviceName.
				SimpleMcaType mcaConfig;
				try {
					mcaConfig = Configurator.getConfiguration(getDeviceName(), gda.epics.interfaces.SimpleMcaType.class);
					mcaRecordName = mcaConfig.getRECORD().getPv();
					createChannelAccess(mcaRecordName);
					channelManager.tryInitialize(100);
				} catch (ConfigurationNotFoundException e) {
					logger.error("Can NOT find EPICS configuration for motor " + getDeviceName(), e);
					throw new FactoryException("Epics MCA " + getDeviceName() + " not found");
				}
			} else if (getMcaRecordName() != null) {
				// allow directly set EPICS's record name for MCA
				createChannelAccess(mcaRecordName);
				channelManager.tryInitialize(100);
			}
			// Nothing specified in Server XML file
			else {
				logger.error("Missing EPICS configuration for MCA {}", getName());
				throw new FactoryException("Missing EPICS configuration for MCA " + getName());
			}
			configured = true;
		}
	}

	/**
	 * actually create most channels provides MCA records
	 * 
	 * @param mcaRecordName
	 * @throws FactoryException
	 */
	private void createChannelAccess(String mcaRecordName) throws FactoryException {
		try {
			// control fields
			eraseStartAcqChannel = channelManager.createChannel(mcaRecordName + ".ERST", false);
			startAcqChannel = channelManager.createChannel(mcaRecordName + ".STRT", false);
			stopAcqChannel = channelManager.createChannel(mcaRecordName + ".STOP", false);
			statusChannel = channelManager.createChannel(mcaRecordName + ".ACQG", acqglist, false);
			procChannel = channelManager.createChannel(mcaRecordName + ".PROC", false);
			rdnsChannel = channelManager.createChannel(mcaRecordName + ".RDNS", false);
			readChannel = channelManager.createChannel(mcaRecordName + ".READ", false);
			rdngChannel = channelManager.createChannel(mcaRecordName + ".RDNG", readingArrayList, false);
			dataChannel = channelManager.createChannel(mcaRecordName + ".VAL", datalist, false);

			eraseChannel = channelManager.createChannel(mcaRecordName + ".ERAS", false);
			chasChannel = channelManager.createChannel(mcaRecordName + ".CHAS", false);
			numberChannelsToUseChannel = channelManager.createChannel(mcaRecordName + ".NUSE", false);
			sequenceChannel = channelManager.createChannel(mcaRecordName + ".SEQ", false);
			dwellTimeChannel = channelManager.createChannel(mcaRecordName + ".DWEL");
			psclCh = channelManager.createChannel(mcaRecordName + ".PSCL", false);
			presetLiveTimeChannel = channelManager.createChannel(mcaRecordName + ".PLTM", false);
			presetRealTimeChannel = channelManager.createChannel(mcaRecordName + ".PRTM", false);
			presetCountsChannel = channelManager.createChannel(mcaRecordName + ".PCT", false);
			presetCountLowChannel = channelManager.createChannel(mcaRecordName + ".PCTL", false);
			presetCountHighChannel = channelManager.createChannel(mcaRecordName + ".PCTH", false);
			presetSweepChannel = channelManager.createChannel(mcaRecordName + ".PSWP", false);
			modeChannel = channelManager.createChannel(mcaRecordName + ".MODE", false);
			// calibration fields
			engineeringUnitsChannel = channelManager.createChannel(mcaRecordName + ".EGU", false);
			calibrationOffsetChannel = channelManager.createChannel(mcaRecordName + ".CALO", false);
			calibrationSlopeChannel = channelManager.createChannel(mcaRecordName + ".CALS", false);
			calibrationQuadraticChannel = channelManager.createChannel(mcaRecordName + ".CALQ", false);
			twoThetaChannel = channelManager.createChannel(mcaRecordName + ".TTH", false);
			// status field
			elapsedRealTimeChannel = channelManager.createChannel(mcaRecordName + ".ERTM", rtimelist, false);
			elapsedLiveTimeChannel = channelManager.createChannel(mcaRecordName + ".ELTM", ltimelist, false);
			avgDeadTimeChannel = channelManager.createChannel(mcaRecordName + ".DTIM", dtimelist, false);
			instDeadTimeChannel = channelManager.createChannel(mcaRecordName + ".IDTIM", itimelist, false);
			actChannel = channelManager.createChannel(mcaRecordName + ".ACT", false);
			startTimeChannel = channelManager.createChannel(mcaRecordName + ".STIM", false);

			// region of interest
			for (int i = 0; i < roiLowChannels.length; i++) {
				roiLowChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "LO", false);
				roiHighChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "HI", false);
				roiBackgroundChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "BG", false);
				roiPresetChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "IP", false);
				roiCountChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i), roicountlist, false);
				roiNetCountChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "N", roinetlist,
						false);
				roiPresetCountChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "P", false);
				roiNameChannels[i] = channelManager.createChannel(mcaRecordName + ".R" + (i) + "NM", false);
			}
			// value fileds

			numberOfChannelsReadChannel = channelManager.createChannel(mcaRecordName + ".NORD", false);
			maxNumberChannelsChannel = channelManager.createChannel(mcaRecordName + ".NMAX", false);

			// acknowledge that creation phase is completed
			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	// **************** Control fields ********************************
	/**
	 * starts data acquisition
	 * 
	 * @throws DeviceException
	 */
	public void startAcquisition() throws DeviceException {
		try {
			controller.caput(startAcqChannel, 1);
			acquisitionDone = false;
			readingDone = false;
		} catch (Throwable th) {
			throw new DeviceException("failed to start acquisition", th);
		}
	}

	/**
	 * stops data acquisition
	 * 
	 * @throws DeviceException
	 */
	public void stopAcquisition() throws DeviceException {
		try {
			controller.caput(stopAcqChannel, 1);
		} catch (Throwable th) {
			throw new DeviceException("failed to stop acquisition", th);
		}

	}

	/**
	 * Activates the MCA using the Erase & Start acquire.
	 * 
	 * @throws DeviceException
	 */
	public void eraseStartAcquisition() throws DeviceException {
		try {
			controller.caput(eraseStartAcqChannel, 1);
			acquisitionDone = false;
			readingDone = false;
		} catch (Throwable th) {
			throw new DeviceException("failed to start acquisition", th);
		}
	}

	/**
	 * gets acquire status from MCA (poll from hardware)
	 * 
	 * @return 0 done, 1 Acquire
	 * @throws DeviceException
	 */
	public int getAcquireStatus() throws DeviceException {
		try {
			return controller.cagetInt(statusChannel);
		} catch (Throwable th) {
			throw new DeviceException("failed to get acquire status", th);
		}
	}

	/**
	 * get current MCA status
	 * 
	 * @return MCA status
	 * @throws DeviceException
	 */
	public MCAStatus getStatus() throws DeviceException {
		try {

			MCAStatus status = MCAStatus.UNKNOWN;
			// we need to fire the PROC to ensure the RDGN field is updated
			controller.caput(procChannel, "1");
			status = (acquisitionDone && readingDone) ? MCAStatus.READY : MCAStatus.BUSY;
			return status;
		} catch (Throwable th) {
			logger.error("getStatus failed on {}.", getName());
			throw new DeviceException("EpicsMCA.getStatus: failed to get status", th);
		}
	}

	/**
	 * initiates record process
	 * 
	 * @throws DeviceException
	 */
	public void processRecord() throws DeviceException {
		try {
			controller.caput(procChannel, "1");
		} catch (Throwable th) {
			logger.error("Process Record failed on {}.", getName());
			throw new DeviceException("EpicsMCA.processRecord: failed to initiate record process", th);
		}
	}

	/**
	 * initiates reading of the MCA's data.
	 * 
	 * @throws DeviceException
	 */
	public void read() throws DeviceException {
		try {
			controller.caput(readChannel, 1);
		} catch (Throwable th) {
			logger.error("Read failed on {}.", getName());
			throw new DeviceException("EpicsMCA.read: failed to initiate reading of data", th);
		}
	}

	/**
	 * erases the data array of the MCA
	 * 
	 * @throws DeviceException
	 */
	public void clear() throws DeviceException {
		try {
			controller.caput(eraseChannel, 1);
		} catch (Throwable th) {
			logger.error("Failed to erase data array on {}.", getName());
			throw new DeviceException("EpicsMCA.clear: fail to clear", th);
		}
	}

	/**
	 * gets the channel advance source in MCS mode
	 * 
	 * @return 0 - Internal clock, 1 - external signal
	 * @throws DeviceException
	 */
	public short getChannalAdvanceSource() throws DeviceException {
		try {
			return controller.cagetEnum(chasChannel);
		} catch (Throwable th) {
			logger.error("Failed to get Channel Advanc Source {}.", getName());
			throw new DeviceException("EpicsMCA.clear: fail to get Channel Advanc Source", th);
		}
	}

	/**
	 * sets channel advance source in MCS mode
	 * 
	 * @param s
	 *            0 - Internal clock or 1 - External signal.
	 * @throws DeviceException
	 */
	public void setChannelAdvanceSource(int s) throws DeviceException {
		try {
			if (s == 0 || s == 1) {
				controller.caput(chasChannel, s);
			} else {
				logger.error("Invalid input data {}", s);
				throw new IllegalArgumentException("Input must be 0 or 1");
			}
		} catch (Throwable th) {
			logger.error("Error change Channel Advance Source {}.", getName());
			throw new DeviceException("EpicsMCA.setChannelAdvanceSource: failed", th);
		}
	}

	/**
	 * gets the number of channels to use for spectrum acquisition
	 * 
	 * @return the number of channels to use
	 * @throws DeviceException
	 */
	public long getNumberOfChannels() throws DeviceException {
		try {
			return controller.cagetInt(numberChannelsToUseChannel);
		} catch (Throwable th) {
			logger.error("failed to get number of channels {}.", getName());
			throw new DeviceException("failed get number of channels", th);
		}
	}

	/**
	 * sets the number of channels (array elements) to use for spectrum acquisition
	 * 
	 * @param channels
	 * @throws DeviceException
	 */
	public void setNumberOfChannels(long channels) throws DeviceException {
		if (channels > MAXIMUM_NUMBER_CHANNELS) {
			throw new IllegalArgumentException("Invalid number of channels," + " Maximum channels allowed is  "
					+ MAXIMUM_NUMBER_CHANNELS);
		}
		try {
			controller.caput(numberChannelsToUseChannel, channels);
		} catch (Throwable th) {
			logger.error("failed to set number of channels {}.", getName());
			throw new DeviceException("failed to set number of channels", th);
		}

	}

	/**
	 * sets the time sequence number which tell the hardware which memory region to use for data acquisition, readout
	 * and erasing. The main use of this field is for time-resolved spectroscopy, since it permits rapidly changing the
	 * location of data acquisition, without having to read and erase the acquisition memory between successive spectra.
	 * 
	 * @param sequence
	 * @throws DeviceException
	 */
	public void setSequence(long sequence) throws DeviceException {
		try {
			controller.caput(sequenceChannel, sequence);
		} catch (Throwable th) {
			logger.error("failed to set sequence for {}.", getName());
			throw new DeviceException("failed to set sequence", th);
		}
	}

	/**
	 * gets the time sequence number which is the memory region hardware used for data acquisition
	 * 
	 * @return the time sequence number
	 * @throws DeviceException
	 */
	public long getSequence() throws DeviceException {
		try {
			long seq = controller.cagetInt(sequenceChannel);

			return seq;
		} catch (Throwable th) {
			logger.error("failed to get sequence from {}.", getName());
			throw new DeviceException("EpicsMCA.getSequence:failed to get sequence", th);
		}
	}

	/**
	 * Gets the Dwell Time (DWEL) per channel, used only in MCS mode and CHAS is internal.
	 * 
	 * @return Dwell Time
	 * @throws DeviceException
	 */
	public double getDwellTime() throws DeviceException {
		try {
			double dwellTime = controller.cagetDouble(dwellTimeChannel);

			return dwellTime;
		} catch (Throwable th) {
			logger.error("failed to get dwell time from {}.", getName());
			throw new DeviceException("failed get dwell time", th);
		}
	}

	/**
	 * Sets the dwell time (DWEL) to tell the hardware how many seconds to spend in each channel when the hardware is in
	 * MCS mode and the channel advance source is "Internal"
	 * 
	 * @param time
	 * @throws DeviceException
	 */
	public void setDwellTime(double time) throws DeviceException {
		try {
			controller.caput(dwellTimeChannel, time);

		} catch (Throwable th) {
			logger.error("failed to set dwell time for {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set dwellTime (DWEL)", th);
		}
	}

	/**
	 * gets the Channel Advance Pre-Scale Factor when in MCS mode and source is "External". Default is 1.
	 * 
	 * @return the channel advance pre-scale factor
	 * @throws DeviceException
	 */
	public int getChannelAdvanceFactor() throws DeviceException {
		try {
			return controller.cagetInt(psclCh);
		} catch (Throwable th) {
			logger.error("failed to get channel advance pre-sacle factor from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get channel advance pre-sacle factor", th);
		}
	}

	/**
	 * sets the Channel Advance pre-Scale factor when in MCS mode and source is "External". The hardware will advance to
	 * the next channel after receiving PSCL external pulses. Default is 1.
	 * 
	 * @param n
	 * @throws DeviceException
	 */
	public void setChannelAdvanceFactor(int n) throws DeviceException {
		try {
			controller.caput(psclCh, n);
		} catch (Throwable th) {
			logger.error("failed to set channel advance pre-sacle factor for {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set channel advance pre-sacle factor", th);
		}
	}

	/**
	 * gets the preset real time from hardware for how many seconds to acquire data according to a free running clock
	 * (real time).
	 * 
	 * @return preset real time
	 * @throws DeviceException
	 */
	public float getPresetRealTime() throws DeviceException {
		try {
			return controller.cagetFloat(presetRealTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset realtime from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get preset real time", th);
		}
	}

	/**
	 * sets the preset real time to the hardware for how many seconds to acquire data according to a free running clock
	 * (real time). sets 0.0 instructs the hardware to ignore it.
	 * 
	 * @param time
	 * @throws DeviceException
	 */
	public void setPresetRealTime(double time) throws DeviceException {
		try {
			controller.caput(presetRealTimeChannel, time);
		} catch (Throwable th) {
			logger.error("failed to set preset realtime to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset real time", th);
		}
	}

	/**
	 * gets the preset live time from hardware for how many seconds to acquire data according to a clock which counts
	 * only when hardware is ready to accept data (live time).
	 * 
	 * @return preset live time
	 * @throws DeviceException
	 */
	public float getPresetLiveTime() throws DeviceException {
		try {
			return controller.cagetFloat(presetRealTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset realtime from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get preset real time", th);
		}
	}

	/**
	 * sets the preset live time to the hardware for how many seconds to acquire data according to a clock which counts
	 * only when hardware is ready to accept data (live time). Sets it to 0.0 instructs the hardware to ignore it.
	 * 
	 * @param time
	 * @throws DeviceException
	 */
	public void setPresetLiveTime(double time) throws DeviceException {
		try {
			controller.caput(presetRealTimeChannel, time);
		} catch (Throwable th) {
			logger.error("failed to set preset realtime to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset real time", th);
		}
	}

	/**
	 * gets the preset counts from hardware
	 * 
	 * @return preset counts
	 * @throws DeviceException
	 */
	public int getPresetCounts() throws DeviceException {
		try {
			return controller.cagetInt(presetCountsChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset counts from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get preset counts", th);
		}
	}

	/**
	 * sets the preset counts which tells the hardware that data acquisition is to be halted when the sum of the numbers
	 * of counts acquired in channels PCTL through PCTH inclusive reaches this value. Sets it to 0.0 instructs the
	 * hardware to ignore it.
	 * 
	 * @param counts
	 * @throws DeviceException
	 */
	public void setPresetCounts(int counts) throws DeviceException {
		try {
			controller.caput(presetCountsChannel, counts);
		} catch (Throwable th) {
			logger.error("failed to set preset counts to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset counts", th);
		}
	}

	/**
	 * gets the preset count low channel from hardware
	 * 
	 * @return preset count low channel
	 * @throws DeviceException
	 */
	public int getPresetCountLow() throws DeviceException {
		try {
			return controller.cagetInt(presetCountLowChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset count low channel from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get preset count low channel", th);
		}
	}

	/**
	 * sets the preset count low channel
	 * 
	 * @param channel
	 * @throws DeviceException
	 */
	public void setPresetCountLow(int channel) throws DeviceException {
		try {
			controller.caput(presetCountLowChannel, channel);
		} catch (Throwable th) {
			logger.error("failed to set preset count low channel to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset count low channel", th);
		}
	}

	/**
	 * gets the preset count high channel from hardware
	 * 
	 * @return preset count high channel
	 * @throws DeviceException
	 */
	public int getPresetCountHigh() throws DeviceException {
		try {
			return controller.cagetInt(presetCountHighChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset count high channel from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get preset count high channel", th);
		}
	}

	/**
	 * sets the preset count high channel
	 * 
	 * @param channel
	 * @throws DeviceException
	 */
	public void setPresetCountHigh(int channel) throws DeviceException {
		try {
			controller.caput(presetCountHighChannel, channel);
		} catch (Throwable th) {
			logger.error("failed to set preset count high channel to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset count high channel", th);
		}
	}

	/**
	 * gets the preset number of sweeps from hardware
	 * 
	 * @return number of sweeps
	 * @throws DeviceException
	 */
	public int getPresetSweep() throws DeviceException {
		try {
			return controller.cagetInt(presetSweepChannel);
		} catch (Throwable th) {
			logger.error("failed to get preset number of sweeps from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get number of sweeps", th);
		}
	}

	/**
	 * sets the preset number of sweeps which tell the hardware that data acquisition in MCS mode is to continue until
	 * the preset number of sweeps have completed.
	 * 
	 * @param sweeps
	 * @throws DeviceException
	 */
	public void setPresetSweep(int sweeps) throws DeviceException {
		try {
			controller.caput(presetSweepChannel, sweeps);
		} catch (Throwable th) {
			logger.error("failed to set preset number of sweeps to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set preset number of sweeps", th);
		}
	}

	/**
	 * sets the the operation mode of the MCA
	 * 
	 * @param mode
	 * @throws DeviceException
	 */
	public void setMode(Mode mode) throws DeviceException {
		try {
			controller.caput(modeChannel, mode.toString());
		} catch (Throwable th) {
			logger.error("failed to set mode to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set mode", th);
		}
	}

	/**
	 * sets the the operation mode of the MCA
	 * 
	 * @return mode
	 * @throws DeviceException
	 */
	public Mode getMode() throws DeviceException {
		Mode mode;
		try {

			short v = controller.cagetEnum(modeChannel);
			if (v == 0) {
				mode = Mode.PHA;
			} else if (v == 1) {
				mode = Mode.MCS;
			} else if (v == 2) {
				mode = Mode.List;
			} else {
				throw new IllegalStateException("MCA is in an UNKNOWN state.");
			}
			return mode;
		} catch (Throwable th) {
			logger.error("failed to get mode to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get mode", th);
		}
	}

	// ************** Calibration fields ******************************
	/**
	 * gets calibration units name from hardware
	 * 
	 * @return calibration units name
	 * @throws DeviceException
	 */
	public String getCalibrationUnitsName() throws DeviceException {
		try {
			return controller.caget(engineeringUnitsChannel);
		} catch (Throwable th) {
			logger.error("failed to get calibration units name from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get calibration units name", th);
		}
	}

	/**
	 * sets the calibration units name
	 * 
	 * @param unitName
	 * @throws DeviceException
	 */
	public void setCalibrationUnitsName(String unitName) throws DeviceException {
		try {
			controller.caput(engineeringUnitsChannel, unitName);
		} catch (Throwable th) {
			logger.error("failed to set calibration units name to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set calibration units name", th);
		}
	}

	/**
	 * gets calibration offset from hardware
	 * 
	 * @return calibration offset
	 * @throws DeviceException
	 */
	public double getCalibrationOffset() throws DeviceException {
		try {
			return controller.cagetDouble(calibrationOffsetChannel);
		} catch (Throwable th) {
			logger.error("failed to get calibration offset from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get calibration offset", th);
		}
	}

	/**
	 * sets the calibration offset
	 * 
	 * @param value
	 * @throws DeviceException
	 */
	public void setCalibrationOffset(double value) throws DeviceException {
		try {
			controller.caput(calibrationOffsetChannel, value);
		} catch (Throwable th) {
			logger.error("failed to set calibration offset to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set calibration offset", th);
		}
	}

	/**
	 * gets calibration slope from hardware
	 * 
	 * @return calibration slope
	 * @throws DeviceException
	 */
	public double getCalibrationSlope() throws DeviceException {
		try {
			return controller.cagetDouble(calibrationSlopeChannel);
		} catch (Throwable th) {
			logger.error("failed to get calibration slope from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get calibration slope", th);
		}
	}

	/**
	 * sets the calibration slope
	 * 
	 * @param value
	 * @throws DeviceException
	 */
	public void setCalibrationSlope(double value) throws DeviceException {
		try {
			controller.caput(calibrationSlopeChannel, value);
		} catch (Throwable th) {
			logger.error("failed to set calibration slope to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set calibration slope", th);
		}
	}

	/**
	 * gets calibration Quadratic from hardware
	 * 
	 * @return calibration quadratic
	 * @throws DeviceException
	 */
	public double getCalibrationQuadratic() throws DeviceException {
		try {
			return controller.cagetDouble(calibrationQuadraticChannel);
		} catch (Throwable th) {
			logger.error("failed to get calibration Quadratic from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get calibration Quadratic", th);
		}
	}

	/**
	 * sets the calibration Quadratic
	 * 
	 * @param value
	 * @throws DeviceException
	 */
	public void setCalibrationQuadratic(double value) throws DeviceException {
		try {
			controller.caput(calibrationQuadraticChannel, value);
		} catch (Throwable th) {
			logger.error("failed to set calibration Quadratic to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set calibration Quadratic", th);
		}
	}

	/**
	 * /** gets calibration Two Theta i.e the two theta angle of the detector useful in energy-dispersive diffraction
	 * experiments
	 * 
	 * @return two theta angle of the detector
	 * @throws DeviceException
	 */
	public double getTwoTheta() throws DeviceException {
		try {
			return controller.cagetDouble(twoThetaChannel);
		} catch (Throwable th) {
			logger.error("failed to get calibration Two Theta from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get calibration Two Theta", th);
		}
	}

	/**
	 * sets the calibration Two Theta, i.e the two theta angle of the detector useful in energy-dispersive diffraction
	 * experiments
	 * 
	 * @param value
	 * @throws DeviceException
	 */
	public void setTwoTheta(double value) throws DeviceException {
		try {
			controller.caput(twoThetaChannel, value);
		} catch (Throwable th) {
			logger.error("failed to set calibration Two Theta to {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to set calibration Two Theta", th);
		}
	}

	// **************** Status Fields ***********************************
	/**
	 * gets the elapsed real time from hardware
	 * 
	 * @return elapsed real time
	 * @throws DeviceException
	 */
	public double getElapsedRealTime() throws DeviceException {
		try {
			return controller.cagetDouble(elapsedRealTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get elapsed real time from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get elapsed real time", th);
		}
	}

	/**
	 * gets the elapsed live time from hardware
	 * 
	 * @return elapsed live time
	 * @throws DeviceException
	 */
	public double getElapsedLiveTime() throws DeviceException {
		try {
			return controller.cagetDouble(elapsedLiveTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get elapsed live time from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get elapsed live time", th);
		}
	}

	/**
	 * gets the number of actual counts in preset region
	 * 
	 * @return number of actual counts
	 * @throws DeviceException
	 */
	public double getActualCountInPresetRegion() throws DeviceException {
		try {
			return controller.cagetDouble(actChannel);
		} catch (Throwable th) {
			logger.error("failed to get number of actual counts from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get number of actual counts", th);
		}
	}

	/**
	 * gets the average dead time of the detector
	 * 
	 * @return average dead time of the detector
	 * @throws DeviceException
	 */
	public double getAverageDeadTime() throws DeviceException {
		try {
			return controller.cagetDouble(avgDeadTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get average dead time of the detector from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get average dead time of the detector", th);
		}
	}

	/**
	 * gets the instantaneous dead time of the detector
	 * 
	 * @return instantaneous dead time of the detector
	 * @throws DeviceException
	 */
	public double getInstantaneousDeadTime() throws DeviceException {
		try {
			return controller.cagetDouble(instDeadTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get instantaneous dead time of the detector from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get instantaneous dead time of the detector", th);
		}
	}

	/**
	 * gets the absolute acquisition start time from IOC
	 * 
	 * @return absolute acquisition start time
	 * @throws DeviceException
	 */
	public String getAcquisitionStartTime() throws DeviceException {
		try {
			return controller.cagetString(startTimeChannel);
		} catch (Throwable th) {
			logger.error("failed to get absolute acquisition start time from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to absolute acquisition start time", th);
		}
	}

	/**
	 * gets the low channel for the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's low channel
	 * @throws DeviceException
	 */
	public int getRegionLowChannel(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetInt(roiLowChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the low channel of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the low channel of region " + regionIndex, th);
		}
	}

	/**
	 * sets the low channel for the specified region.
	 * 
	 * @param regionIndex
	 * @param lowChannel
	 * @throws DeviceException
	 */
	public void setRegionLowChannel(int regionIndex, int lowChannel) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			controller.caput(roiLowChannels[regionIndex], lowChannel);
		} catch (Throwable th) {
			logger.error("failed to set the low channel for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the low channel for region " + regionIndex, th);
		}
	}

	/**
	 * gets the high channel for the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's high channel
	 * @throws DeviceException
	 */
	public int getRegionHighChannel(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetInt(roiHighChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the high channel of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the high channel of region " + regionIndex, th);
		}
	}

	/**
	 * sets the high channel for the specified region.
	 * 
	 * @param regionIndex
	 * @param highChannel
	 * @throws DeviceException
	 */
	public void setRegionHighChannel(int regionIndex, int highChannel) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			controller.caput(roiHighChannels[regionIndex], highChannel);
		} catch (Throwable th) {
			logger.error("failed to set the high channel for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the high channel for region " + regionIndex, th);
		}
	}

	/**
	 * gets the Background for the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's high channel
	 * @throws DeviceException
	 */
	public int getRegionBackground(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetInt(roiBackgroundChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the Background of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the Background of region " + regionIndex, th);
		}
	}

	/**
	 * sets the Background for the specified region.
	 * 
	 * @param regionIndex
	 * @param highChannel
	 * @throws DeviceException
	 */
	public void setRegionBackground(int regionIndex, int highChannel) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			controller.caput(roiBackgroundChannels[regionIndex], highChannel);
		} catch (Throwable th) {
			logger.error("failed to set the Background for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the Background for region " + regionIndex, th);
		}
	}

	/**
	 * gets the is-preset for the specified region.
	 * 
	 * @param regionIndex
	 * @return true or false
	 * @throws DeviceException
	 */
	public boolean isRegionPreset(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetEnum(roiPresetChannels[regionIndex]) != 0;
		} catch (Throwable th) {
			logger.error("failed to get the is-preset of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the is-preset of region " + regionIndex, th);
		}
	}

	/**
	 * sets the is-preset for the specified region.
	 * 
	 * @param regionIndex
	 * @param b
	 *            true or false
	 * @throws DeviceException
	 */
	public void setRegionPreset(int regionIndex, boolean b) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			if (b)
				controller.caput(roiPresetChannels[regionIndex], 1);
			else
				controller.caput(roiPresetChannels[regionIndex], 0);
		} catch (Throwable th) {
			logger.error("failed to set the is-preset for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the is-preset for region " + regionIndex, th);
		}
	}

	/**
	 * gets the total number of counts in the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's counts
	 * @throws DeviceException
	 */
	public double getRegionCounts(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetDouble(roiCountChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the count of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the count of region " + regionIndex, th);
		}
	}

	/**
	 * gets the net number of counts in the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's counts
	 * @throws DeviceException
	 */
	public double getRegionNetCounts(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetDouble(roiNetCountChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the net count of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the net count of region " + regionIndex, th);
		}
	}

	/**
	 * gets the preset count of the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's preset count
	 * @throws DeviceException
	 */
	public double getRegionPresetCount(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetDouble(roiPresetCountChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the preset count of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the preset count of region " + regionIndex, th);
		}
	}

	/**
	 * sets the preset count of the specified region.
	 * 
	 * @param regionIndex
	 * @param count
	 * @throws DeviceException
	 */
	public void setRegionPresetCount(int regionIndex, double count) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			controller.caput(roiPresetCountChannels[regionIndex], count);
		} catch (Throwable th) {
			logger.error("failed to set the preset count for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the preset count for region " + regionIndex, th);
		}
	}

	/**
	 * gets the preset count of the specified region.
	 * 
	 * @param regionIndex
	 * @return the region's name
	 * @throws DeviceException
	 */
	public String getRegionName(int regionIndex) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			return controller.cagetString(roiNameChannels[regionIndex]);
		} catch (Throwable th) {
			logger.error("failed to get the name of region {} from {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to the name of region " + regionIndex, th);
		}
	}

	/**
	 * sets the name of the specified region.
	 * 
	 * @param regionIndex
	 * @param name
	 * @throws DeviceException
	 */
	public void setRegionName(int regionIndex, String name) throws DeviceException {
		if (regionIndex < 0 || regionIndex > (NUMBER_OF_REGIONS - 1)) {
			throw new IllegalArgumentException("Region Index must be from 0 to 31 inclusive.");
		}
		try {
			controller.caput(roiNameChannels[regionIndex], name);
		} catch (Throwable th) {
			logger.error("failed to set the name for region {} to {}.", regionIndex, getName());
			throw new DeviceException("EpicsMCA: Failed to set the name for region " + regionIndex, th);
		}
	}

	// ******************** Value fields **********************************
	/**
	 * @return the data array
	 * @throws DeviceException
	 */
	public int[] getData() throws DeviceException {
		try {
			// asyn device support, SIS driver and device support only supports
			// long data. Check with controls group - should FTVL field be used
			// to find the data type of the data array
			int[] dataArray = controller.cagetIntArray(dataChannel);

			return dataArray;
		} catch (Throwable th) {
			logger.error("failed to get data from {}.", getName());
			throw new DeviceException("EpicsMCA: Failed to get data.", th);
		}
	}

	/**
	 * sets data array to the MCA
	 * 
	 * @param data
	 * @throws DeviceException
	 */
	public void setData(int[] data) throws DeviceException {
		try {
			controller.caput(dataChannel, data);
		} catch (Throwable th) {
			logger.error("failed to set data to {}.", getName());
			throw new DeviceException("EpicsMCA.setData: failed to set data", th);
		}
	}

	/**
	 * gets the maximum number of channels (array elements) allocated at iocInit.
	 * 
	 * @return Maximum number of channels
	 * @throws DeviceException
	 */
	public int getMaxNumberOfChannels() throws DeviceException {
		try {
			return controller.cagetInt(maxNumberChannelsChannel);
		} catch (Throwable th) {
			logger.error("failed to get maximum number of channels from {}.", getName());
			throw new DeviceException("EpicsMCA.setData: failed to get maximum number of channels", th);
		}
	}

	/**
	 * gets the number of channels read.
	 * 
	 * @return number of channels read
	 * @throws DeviceException
	 */
	public int getNumberOfChannelRead() throws DeviceException {
		try {
			return controller.cagetInt(numberOfChannelsReadChannel);
		} catch (Throwable th) {
			logger.error("failed to get number of channels read from {}.", getName());
			throw new DeviceException("EpicsMCA.setData: failed to get number of channels read.", th);
		}
	}

	/**
	 * Sets number of region of interest
	 * 
	 * @param numberOfRegions
	 */
	public void setNumberOfRegions(int numberOfRegions){
		this.NUMBER_OF_REGIONS = numberOfRegions;
	}

	/**
	 * gets number of region of interest
	 * 
	 * @return number of regions
	 */
	public int getNumberOfRegions() {
		return NUMBER_OF_REGIONS;
	}

	/**
	 * Monitoring current acquire status of the hardware
	 */
	private class ACQGMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isENUM()) {
				acquisitionDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
				if (acquisitionDone) {
					try {
						// now ask for a read and set ReadingDone false
						controller.caput(readChannel, 1);
						readingDone = false;
					} catch (Exception e) {
						logger.error("error calling caput for readChannel ", e);
					}
				}
			}
		}
	}

	/**
	 * monitor reading array status
	 */
	public class RDNGMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isENUM()) {
				readingDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
				notifyIObservers(EpicsMcaController.this, (acquisitionDone & readingDone) ? MCAStatus.READY
						: MCAStatus.BUSY);
			}
		}
	}

	/**
	 * monitor value array
	 */
	public class VALMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isINT()) {
				int[] data = ((DBR_Int) dbr).getIntValue();
				notifyIObservers(EpicsMcaController.this, data);
			}
		}
	}

	/**
	 * monitors elapses real time
	 */
	public class RealTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				elapsedRealTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr != null && dbr.isFLOAT()) {
				elapsedRealTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
			}
			notifyIObservers(EpicsMcaController.this, elapsedRealTimeValue);
		}
	}

	/**
	 * monitors real live time
	 */
	public class LiveTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				elapsedLiveTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr != null && dbr.isFLOAT()) {
				elapsedLiveTimeValue = ((DBR_Float) dbr).getFloatValue()[0];

			}
			notifyIObservers(EpicsMcaController.this, elapsedLiveTimeValue);
		}
	}

	/**
	 * monitors average dead time
	 */
	public class DeadTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				deadTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr != null && dbr.isFLOAT()) {
				deadTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
			}
			notifyIObservers(EpicsMcaController.this, deadTimeValue);
		}
	}

	/**
	 * monitors instantaneous dead time
	 */
	public class InstDeadTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				instDeadTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
			} else if (dbr != null && dbr.isFLOAT()) {
				instDeadTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
			}
			notifyIObservers(EpicsMcaController.this, instDeadTimeValue);
		}
	}

	/**
	 * monitor the total number of counts of the region of interest
	 */
	public class RoiCountsMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			Channel ch = (Channel) mev.getSource();
			for (int i = 0; i < NUMBER_OF_REGIONS; i++) {
				if (ch == roiCountChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isDOUBLE()) {
						roiCountValues[i] = ((DBR_Double) dbr).getDoubleValue()[0];
						double roc[] = new double[2];
						roc[0] = i;
						roc[1] = roiCountValues[i];

						notifyIObservers(EpicsMcaController.this, roc);
					}
				}
			}
		}
	}

	/**
	 * monitor the net number of counts of the region of interest
	 */
	public class RoiNetCountsMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			Channel ch = (Channel) mev.getSource();
			for (int i = 0; i < NUMBER_OF_REGIONS; i++) {
				if (ch == roiNetCountChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isDOUBLE()) {
						roiNetCountValues[i] = ((DBR_Double) dbr).getDoubleValue()[0];
						double roc[] = new double[2];
						roc[0] = i;
						roc[1] = roiNetCountValues[i];

						notifyIObservers(EpicsMcaController.this, roc);
					}
				}
			}
		}
	}

	@Override
	public void initializationCompleted() {
		try {
			MAXIMUM_NUMBER_CHANNELS = this.getMaxNumberOfChannels();
		} catch (DeviceException e) {
			logger.error("EpicsMCA failed to initialise elapsed and roi values", e);
		}
	}

	/**
	 * Gets the MCA record name - shared name between GDA and EPICS for phase I beamlines.
	 * 
	 * @return the Epics MCA record name.
	 */
	public String getEpicsMcaRecordName() {
		return epicsMcaRecordName;
	}

	/**
	 * Sets the MCA record name - shared name between GDA and EPICS for phase I beamlines.
	 * 
	 * @param epicsMcaRecordName
	 */
	public void setEpicsMcaRecordName(String epicsMcaRecordName) {
		this.epicsMcaRecordName = epicsMcaRecordName;
	}

	/**
	 * gets the device name shared by GDA and EPICS
	 * 
	 * @return device name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the device name which is shared by GDA and EPICS
	 * 
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	/**
	 * gets MCA record name - the actual EPICS Record name after remove field name
	 * 
	 * @return MCA record name
	 */
	public String getMcaRecordName() {
		return mcaRecordName;
	}

	/**
	 * sets MCA record name - the actual EPICS Record name after remove field name
	 * 
	 * @param mcaRecordName
	 */
	public void setMcaRecordName(String mcaRecordName) {
		this.mcaRecordName = mcaRecordName;
	}
}
