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

import gda.device.Analyser;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.MCAStatus;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaceSpec.GDAEpicsInterfaceReader;
import gda.epics.interfaceSpec.InterfaceException;
import gda.epics.xml.EpicsRecord;
import gda.factory.FactoryException;
import gda.factory.Finder;
import gda.util.converters.CoupledConverterHolder;
import gda.util.converters.IQuantitiesConverter;
import gda.util.converters.IQuantityConverter;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.Vector;

import org.jscience.physics.quantities.Quantity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to communicate with an epics MCA record. The MCA record controls and acquires data from a multichannel analyser
 * (MCA).
 */
public class EpicsMCA extends AnalyserBase implements Analyser, Detector, InitializationListener {
	
	private static final Logger logger = LoggerFactory.getLogger(EpicsMCA.class);

	private static final long serialVersionUID = 1L;

	private IQuantitiesConverter channelToEnergyConverter = null;

	private String converterName = "mca_roi_conversion";

	private boolean acquisitionDone = true, readingDone = true;

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
		LIST
	}

	private EpicsController controller;

	private Channel eraseStartAcqChannel;

	private Channel startAcqChannel;

	private Channel stopAcqChannel;

	private Channel[] roiLowChannels;

	private double[] roiLowValues;

	private Channel[] roiHighChannels;

	private double[] roiHighValues;

	private Channel[] roiBackgroundChannels;

	private int[] roiBackgroundValues;

	private Channel[] roiPresetChannels;

	private Channel[] roiCountChannels;

	private double[] roiCountValues;

	private Channel[] roiNetCountChannels;

	private double[] roiNetCountValues;

	private Channel[] roiPresetCountChannels;

	private double[] roiPresetCountValues;

	private Channel[] roiNameChannels;

	private String[] roiNameValues;

	// /////////////private int connectedState = Channel.CONNECTED.getValue();
	private Channel eraseChannel;

	private Channel statusChannel, procChannel, readChannel;

	private Channel calibrationOffsetChannel;

	private Channel engineeringUnitsChannel;

	private Channel calibrationSlopeChannel;

	private Channel calibrationQuadraticChannel;

	private Channel twoThetaChannel;

	private Channel dataChannel;

	private Channel sequenceChannel;

	private Channel elapsedRealTimeChannel;

	private double elapsedRealTimeValue;

	private Channel elapsedLiveTimeChannel;

	private double elapsedLiveTimeValue;

	private Channel presetRealTimeChannel;

	private Channel presetLiveTimeChannel;

	private Channel presetCountsChannel;

	private Channel presetCountLowChannel;

	private Channel presetCountHighChannel;

	private Channel presetSweepChannel;

	// private Monitor acqgStatusMonitor;
	
	private String pvName;
	
	private String epicsMcaRecordName;

//	private EpicsRecord epicsMcaRecord;

	private String mcaRecordName;

	// private Channel modeChannel;

	// private Monitor dataMonitor;

	// private Channel readStatusChannel;

	private int numberOfRegions = 32;

	private Channel numberChannelsToUseChannel;

	private Channel maxNumberChannelsToUseChannel;

	private Channel dwellTimeChannel;

	// private HashSet<Channel> monitorInstalledSet;

	// private double timeout = EpicsGlobals.getTIMEOUT();
	/**
	 * EPICS Channel Manager
	 */
	private EpicsChannelManager channelManager;

	private Object timeLock;

	/**
	 * Constructor.
	 */
	public EpicsMCA() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
		timeLock = new Object();
	}
	
	/**
	 * Sets the PV name used by this object.
	 * 
	 * @param pvName the PV name
	 */
	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	/**
	 * Constructor taking a PV name
	 * 
	 * @param name
	 *            String, the PV name
	 */
	public EpicsMCA(String name) {
		this();
		setName(name);
	}

	private void configure(String fullPVName) throws CAException {
		mcaRecordName = fullPVName;
		eraseStartAcqChannel = channelManager.createChannel(getMcaRecordName() + ".ERST", false);
		startAcqChannel = channelManager.createChannel(getMcaRecordName() + ".STRT", false);
		stopAcqChannel = channelManager.createChannel(getMcaRecordName() + ".STOP", false);
		statusChannel = channelManager.createChannel(getMcaRecordName() + ".ACQG", new ACQGMonitorListener(), false);
		channelManager.createChannel(getMcaRecordName() + ".RDNG", new RDNGMonitorListener(), false);
		procChannel = channelManager.createChannel(getMcaRecordName() + ".PROC", new RDNGMonitorListener(), false);
		dataChannel = channelManager.createChannel(getMcaRecordName() + ".VAL", new VALMonitorListener(), false);
		readChannel = channelManager.createChannel(getMcaRecordName() + ".READ", false);
		RoiMonitorListener roilist = new RoiMonitorListener();
		roiLowChannels = new Channel[numberOfRegions];
		roiLowValues = new double[numberOfRegions];
		roiHighChannels = new Channel[numberOfRegions];
		roiHighValues = new double[numberOfRegions];
		roiBackgroundChannels = new Channel[numberOfRegions];
		roiBackgroundValues = new int[numberOfRegions];
		roiPresetChannels = new Channel[numberOfRegions];

		roiCountChannels = new Channel[numberOfRegions];
		roiCountValues = new double[numberOfRegions];
		roiNetCountChannels = new Channel[numberOfRegions];
		roiNetCountValues = new double[numberOfRegions];
		roiPresetCountChannels = new Channel[numberOfRegions];
		roiPresetCountValues = new double[numberOfRegions];
		roiNameChannels = new Channel[numberOfRegions];
		roiNameValues = new String[numberOfRegions];
		for (int i = 0; i < roiLowChannels.length; i++) {
			roiLowChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "LO", roilist, false);
			roiHighChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "HI", roilist, false);
			roiBackgroundChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "BG", roilist,
					false);
			roiPresetChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "IP", false);
			roiCountChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i), roilist, false);
			roiNetCountChannels[i] = channelManager
					.createChannel(getMcaRecordName() + ".R" + (i) + "N", roilist, false);
			roiPresetCountChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "P", roilist,
					false);
			roiNameChannels[i] = channelManager.createChannel(getMcaRecordName() + ".R" + (i) + "NM", roilist, false);

		}

		eraseChannel = channelManager.createChannel(getMcaRecordName() + ".ERAS", false);
		engineeringUnitsChannel = channelManager.createChannel(getMcaRecordName() + ".EGU", false);
		calibrationOffsetChannel = channelManager.createChannel(getMcaRecordName() + ".CALO", false);
		calibrationSlopeChannel = channelManager.createChannel(getMcaRecordName() + ".CALS", false);
		calibrationQuadraticChannel = channelManager.createChannel(getMcaRecordName() + ".CALQ", false);
		twoThetaChannel = channelManager.createChannel(getMcaRecordName() + ".TTH", false);
		sequenceChannel = channelManager.createChannel(getMcaRecordName() + ".SEQ", false);

		RealTimeMonitorListener rtimelist = new RealTimeMonitorListener();
		elapsedRealTimeChannel = channelManager.createChannel(getMcaRecordName() + ".ERTM", rtimelist, false);

		LiveTimeMonitorListener ltimelist = new LiveTimeMonitorListener();
		elapsedLiveTimeChannel = channelManager.createChannel(getMcaRecordName() + ".ELTM", ltimelist, false);
		// readStatusChannel = channelManager.createChannel(
		// getMcaRecordName() + ".PROC", false);
		presetLiveTimeChannel = channelManager.createChannel(getMcaRecordName() + ".PLTM", false);
		presetRealTimeChannel = channelManager.createChannel(getMcaRecordName() + ".PRTM", false);
		presetCountsChannel = channelManager.createChannel(getMcaRecordName() + ".PCT", false);
		presetCountLowChannel = channelManager.createChannel(getMcaRecordName() + ".PCTL", false);
		presetCountHighChannel = channelManager.createChannel(getMcaRecordName() + ".PCTH", false);
		presetSweepChannel = channelManager.createChannel(getMcaRecordName() + ".PSWP", false);
		numberChannelsToUseChannel = channelManager.createChannel(getMcaRecordName() + ".NUSE", false);
		maxNumberChannelsToUseChannel = channelManager.createChannel(getMcaRecordName() + ".NMAX", false);
		dwellTimeChannel = channelManager.createChannel(getMcaRecordName() + ".DWEL");
		// acknowledge that creation phase is completed
		channelManager.creationPhaseCompleted();
		channelManager.tryInitialize(100);
		configured = true;
	}

	@Override
	public void configure() throws FactoryException {
		if (!configured) {
			try {
				if (pvName == null) {
					if (epicsMcaRecordName != null) {
						EpicsRecord epicsRecord = (EpicsRecord) Finder.getInstance().find(epicsMcaRecordName);
						if (epicsRecord != null) {
							pvName = epicsRecord.getFullRecordName();
						}
					} else if (getDeviceName() != null) {
						pvName = getPV();
					}
				}
				configure(pvName);
			} catch (Exception e) {
				throw new FactoryException("Error initialising device " + getDeviceName(), e);
			}
		}
	}

	@Override
	public void addRegionOfInterest(int regionIndex, double regionLow, double regionHigh, int regionBackground,
			double regionPreset, String regionName) throws DeviceException {
		try {
			controller.caput(roiLowChannels[regionIndex], regionLow);
			controller.caput(roiHighChannels[regionIndex], regionHigh);
			controller.caput(roiBackgroundChannels[regionIndex], regionBackground);
			if (regionPreset <= 0) {
				controller.caput(roiPresetChannels[regionIndex], 0);
			} else {
				controller.caput(roiPresetChannels[regionIndex], 1);
				controller.caput(roiPresetCountChannels[regionIndex], regionPreset);

			}
			if (regionName != null) {
				controller.caput(roiNameChannels[regionIndex], regionName);
			}

		} catch (Throwable th) {
			throw new DeviceException("failed to add region of interest", th);
		}
	}

	@Override
	public void clear() throws DeviceException {
		try {
			controller.caput(eraseChannel, 1);
		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.clear: exception seen", th);
		}
	}

	/**
	 * Clears the mca, but does not return until the clear has been done.
	 * 
	 * @throws DeviceException
	 */
	public void clearWaitForCompletion() throws DeviceException {
		try {
			// you cannot call caPutWait unless the PROC field is being fired in
			// a different thread or independently e.g. by EPICS.
			controller.caput(eraseChannel, 1);

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.clearWaitForCompletion: exception seen - " + th.getMessage(), th);
		}
	}

	@Override
	public void deleteRegionOfInterest(int regionIndex) throws DeviceException {
		try {
			controller.caput(roiLowChannels[regionIndex], -1);
			controller.caput(roiHighChannels[regionIndex], -1);
			controller.caput(roiBackgroundChannels[regionIndex], -1);
			controller.caput(roiPresetChannels[regionIndex], 0);
			controller.caput(roiPresetCountChannels[regionIndex], 0);
			controller.caput(roiNameChannels[regionIndex], "");

		} catch (Throwable th) {
			throw new DeviceException("failed to delete region of interest", th);
		}

	}

	@Override
	public Object getCalibrationParameters() throws DeviceException {
		try {

			String egu = null;
			if (engineeringUnitsChannel.getConnectionState() == Channel.CONNECTED) {
				egu = controller.cagetString(engineeringUnitsChannel);
			} else {
				logger.error("Connection to EGU Channel failed");
			}

			float calo = 0;
			if (calibrationOffsetChannel.getConnectionState() == Channel.CONNECTED) {
				calo = controller.cagetFloat(calibrationOffsetChannel);
			} else {
				logger.error("Connection to CALO Channel failed");
			}

			float cals = 1;
			if (calibrationSlopeChannel.getConnectionState() == Channel.CONNECTED) {
				cals = controller.cagetFloat(calibrationSlopeChannel);
			} else {
				logger.error("Connection to CALS Channel failed");
			}

			float calq = 1;
			if (calibrationQuadraticChannel.getConnectionState() == Channel.CONNECTED) {
				calq = controller.cagetFloat(calibrationQuadraticChannel);
			} else {
				logger.error("Connection to CALQ Channel failed");
			}

			float tth = 0;
			if (twoThetaChannel.getConnectionState() == Channel.CONNECTED) {
				tth = controller.cagetFloat(twoThetaChannel);
			} else {
				logger.error("Connection to TTH Channel failed");
			}

			return new EpicsMCACalibration(egu, calo, cals, calq, tth);
		} catch (Throwable th) {
			throw new DeviceException("failed to get calibration parameters", th);
		}
	}

	@Override
	public Object getData() throws DeviceException {
		try {

			// asyn device support, SIS driver and device support only supports
			// long
			// data
			// Check with controls group - should FTVL field be used to find the
			// datat
			// type of the data array
			int[] dataArray = null;
			// double[] dataArray = null;
			if (dataChannel.getConnectionState() == Channel.CONNECTED) {
				dataArray = controller.cagetIntArray(dataChannel);
				// System.out.println("After getting the data in EPics MCA");
			} else {
				logger.error("Connection to data Channel failed");
			}

			return dataArray;
		} catch (Throwable th) {
			throw new DeviceException("failed to get data", th);
		}
	}

	@Override
	public Object getElapsedParameters() throws DeviceException {
		try {

			float[] elapsed = new float[2];
			if (elapsedRealTimeChannel.getConnectionState() == Channel.CONNECTED
					&& elapsedLiveTimeChannel.getConnectionState() == Channel.CONNECTED) {
				elapsed[0] = controller.cagetFloat(elapsedRealTimeChannel);
				elapsed[1] = controller.cagetFloat(elapsedLiveTimeChannel);

				synchronized (timeLock) {
					elapsedRealTimeValue = elapsed[0];
					elapsedLiveTimeValue = elapsed[1];
				}

				// System.out.println("the elapsed parameters are " +
				// elapsed[0]
				// + " "
				// //// + elapsed[1]);

			} else {
				logger.error("Connection to elapsed Channel failed");
			}

			return elapsed;
		} catch (Throwable th) {
			throw new DeviceException("failed to get elapsed parameters", th);
		}
	}

	private Object getCachedElapsedParameters() {
		float[] elapsed = new float[2];
		synchronized (timeLock) {
			// TODO fix this
			elapsed[0] = (float) elapsedRealTimeValue;
			elapsed[1] = (float) elapsedLiveTimeValue;
		}
		return elapsed;

	}

	/**
	 * Gets the Dwell Time (DWEL).
	 * 
	 * @return Dwell Time
	 * @throws DeviceException
	 */
	public double getDwellTime() throws DeviceException {
		try {

			double dwellTime = Double.NaN;
			if (dwellTimeChannel.getConnectionState() == Channel.CONNECTED) {
				dwellTime = controller.cagetDouble(dwellTimeChannel);
			} else {
				logger.error("EpicsMCA: Connection to dwell time (DWEL) channel failed");
			}
			return dwellTime;
		} catch (Throwable th) {
			throw new DeviceException("failed get number of channels", th);
		}
	}

	@Override
	public int getNumberOfRegions() throws DeviceException {
		return numberOfRegions;
	}

	@Override
	public Object getPresets() throws DeviceException {
		try {

			float prtm = 0;
			if (presetRealTimeChannel.getConnectionState() == Channel.CONNECTED) {
				prtm = controller.cagetFloat(presetRealTimeChannel);
			} else {
				logger.error("Connection to PRTM Channel failed");
			}

			float pltm = 0;
			if (presetLiveTimeChannel.getConnectionState() == Channel.CONNECTED) {
				pltm = controller.cagetFloat(presetLiveTimeChannel);
			} else {
				logger.error("Connection to CALO Channel failed");
			}

			int pct = 1;
			if (presetCountsChannel.getConnectionState() == Channel.CONNECTED) {
				pct = controller.cagetInt(presetCountsChannel);
			} else {
				logger.error("Connection to CALS Channel failed");
			}

			int pctl = 1;
			if (presetCountLowChannel.getConnectionState() == Channel.CONNECTED) {
				pctl = controller.cagetInt(presetCountLowChannel);
			} else {
				logger.error("Connection to CALQ Channel failed");
			}

			int pcth = 0;
			if (presetCountHighChannel.getConnectionState() == Channel.CONNECTED) {
				pcth = controller.cagetInt(presetCountHighChannel);
			} else {
				logger.error("Connection to TTH Channel failed");
			}

			int pswp = 0;
			if (presetSweepChannel.getConnectionState() == Channel.CONNECTED) {
				pswp = controller.cagetInt(presetSweepChannel);
			} else {
				logger.error("Connection to TTH Channel failed");
			}

			return new EpicsMCAPresets(prtm, pltm, pct, pctl, pcth, pswp);
		} catch (Throwable th) {
			throw new DeviceException("failed to get presets", th);
		}

	}

	@Override
	public Object getRegionsOfInterest() throws DeviceException {
		return getRegionsOfInterest(numberOfRegions);
	}

	@Override
	public double[][] getRegionsOfInterestCount() throws DeviceException {
		try {

			double[][] regionsCount = new double[numberOfRegions][2];
			for (int i = 0; i < regionsCount.length; i++) {
				if (roiCountChannels[i].getConnectionState() == Channel.CONNECTED
						&& roiNetCountChannels[i].getConnectionState() == Channel.CONNECTED) {
					regionsCount[i][0] = controller.cagetDouble(roiCountChannels[i]);
					roiCountValues[i] = regionsCount[i][0];
					regionsCount[i][1] = controller.cagetDouble(roiNetCountChannels[i]);
					roiNetCountValues[i] = regionsCount[i][1];
				} else {
					logger.error("Connection to Region Background channel failed");
				}
			}
			return regionsCount;
		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.getRegionsOfInterestCount:failed to get region of interest count", th);
		}
	}

	@Override
	public long getSequence() throws DeviceException {
		try {
			long seq = Long.MAX_VALUE;

			if (sequenceChannel.getConnectionState() == Channel.CONNECTED)
				seq = controller.cagetInt(sequenceChannel);
			else
				logger.error("Connection to data Channel failed");
			return seq;
		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.getSequence:failed to get sequence", th);
		}
	}

	@Override
	public int getStatus() throws DeviceException {
		try {

			int status = Detector.STANDBY;// Should probably rename MCAStatus
			// to Analyser status

			if (statusChannel.getConnectionState() == Channel.CONNECTED) {
				// we need to fire the PROC to ensure the RDGN field is updated
				controller.caput(procChannel, "1");
				status = (acquisitionDone && readingDone) ? Detector.IDLE : Detector.BUSY;
			} else
				logger.error("EpicsMCA.getStatus: statusChannel is not connected.");
			return status;
		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.getStatus: failed to get status", th);
		}
	}

	@Override
	public void setCalibration(Object calibrate) throws DeviceException {
		try {
			EpicsMCACalibration calib = (EpicsMCACalibration) calibrate;

			controller.caput(engineeringUnitsChannel, calib.getEngineeringUnits());
			controller.caput(calibrationOffsetChannel, calib.getCalibrationOffset());
			controller.caput(calibrationSlopeChannel, calib.getCalibrationSlope());
			controller.caput(calibrationQuadraticChannel, calib.getCalibrationQuadratic());
			controller.caput(twoThetaChannel, calib.getTwoThetaAngle());

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.setCalibration: failed to set calibration", th);
		}
	}

	@Override
	public void setData(Object data) throws DeviceException {
		try {
			int valArray[] = (int[]) data;
			controller.caput(dataChannel, valArray);

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA.setData: failed to set data", th);
		}
	}

	/**
	 * Sets the dwell time (DWEL)
	 * 
	 * @param time
	 * @throws DeviceException
	 */
	public void setDwellTime(double time) throws DeviceException {
		try {
			controller.caput(dwellTimeChannel, time);

		} catch (Throwable th) {
			throw new DeviceException("EpicsMCA: Failed to set dwellTime (DWEL)", th);
		}
	}

	@Override
	public void setNumberOfRegions(int numberOfRegions) throws DeviceException {
		this.numberOfRegions = numberOfRegions;
	}

	@Override
	public void setPresets(Object data) throws DeviceException {
		try {
			EpicsMCAPresets preset = (EpicsMCAPresets) data;
			controller.caput(presetRealTimeChannel, preset.getPresetRealTime());
			controller.caput(presetLiveTimeChannel, preset.getPresetLiveTime());
			controller.caput(presetCountsChannel, preset.getPresetCounts());
			controller.caput(presetCountLowChannel, preset.getPresetCountlow());
			controller.caput(presetCountHighChannel, preset.getPresetCountHigh());
			controller.caput(presetSweepChannel, preset.getPresetSweeps());

		} catch (Throwable th) {
			throw new DeviceException("failed to set presets", th);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see gda.device.Analyser#setRegionsOfInterest(java.lang.Object)
	 * the intput parameter highLow object should actually be an array of EpicsMCARegionsOfInterest objects
	 */
	@Override
	public void setRegionsOfInterest(Object highLow) throws DeviceException {
		try {
			EpicsMCARegionOfInterest[] rois = (EpicsMCARegionOfInterest[]) highLow;
			for (int i = 0; i < rois.length; i++) {
				int regionIndex = rois[i].getRegionIndex();
				controller.caput(roiLowChannels[regionIndex], rois[i].getRegionLow());
				controller.caput(roiHighChannels[regionIndex], rois[i].getRegionHigh());
				controller.caput(roiBackgroundChannels[regionIndex], rois[i].getRegionBackground());
				double regionPreset = rois[i].getRegionPreset();
				if (regionPreset <= 0)
					controller.caput(roiPresetChannels[regionIndex], 0);
				else {
					controller.caput(roiPresetChannels[regionIndex], 1);
				}
				controller.caput(roiPresetCountChannels[regionIndex], regionPreset);
				controller.caput(roiNameChannels[regionIndex], rois[i].getRegionName());

			}

		} catch (Throwable th) {
			throw new DeviceException("failed to set region of interest", th);
		}
	}

	@Override
	public void setSequence(long sequence) throws DeviceException {
		try {
			controller.caput(sequenceChannel, sequence);
		} catch (Throwable th) {
			throw new DeviceException("failed to set sequence", th);
		}
	}

	/**
	 * Activates the MCA using the Erase & Start button.
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

	@Override
	public void startAcquisition() throws DeviceException {
		try {
			controller.caput(startAcqChannel, 1);
			acquisitionDone = false;
			readingDone = false;
		} catch (Throwable th) {
			throw new DeviceException("failed to start acquisition", th);
		}
	}

	@Override
	public void stopAcquisition() throws DeviceException {
		try {
			controller.caput(stopAcqChannel, 1);

		} catch (Throwable th) {
			throw new DeviceException("failed to stop acquisition", th);
		}

	}

	private class ACQGMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isENUM()) {
				acquisitionDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
				if (acquisitionDone) {
					try {
						// now ask for a read and set ReadingDone false
						controller.caput(readChannel, "1");
						readingDone = false;
					} catch (Exception e) {
						logger.error("monitorChanged - error calling caput for readChannel",e);
					}
				}
			}

		}
	}

	private class RDNGMonitorListener implements MonitorListener {
		// TODO to be cleaned up
		@Override
		public void monitorChanged(MonitorEvent mev) {
			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isENUM()) {
				readingDone = ((DBR_Enum) dbr).getEnumValue()[0] == 0;
				notifyIObservers(EpicsMCA.this, (acquisitionDone & readingDone) ? MCAStatus.READY : MCAStatus.BUSY);
			}
		}
	}

	private class VALMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent mev) {

			// System.out.println("indisde data channel monitor update");
			DBR dbr1 = mev.getDBR();
			if (dbr1 != null && dbr1.isINT()) {
				// System.out.println("indisde data channel monitor update "
				// +dbr1);
				int[] data = ((DBR_Int) dbr1).getIntValue();
				// System.out.println("the length of the data is " +
				// data.length);
				notifyIObservers(EpicsMCA.this, data);

			}

		}
	}

	private class RealTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				synchronized (timeLock) {
					elapsedRealTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
				}
				notifyIObservers(EpicsMCA.this, getCachedElapsedParameters());
			} else if (dbr != null && dbr.isFLOAT()) {
				synchronized (timeLock) {
					elapsedRealTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
				}
				notifyIObservers(EpicsMCA.this, getCachedElapsedParameters());
			}
		}
	}

	private class LiveTimeMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {

			DBR dbr = mev.getDBR();
			if (dbr != null && dbr.isDOUBLE()) {
				synchronized (timeLock) {
					elapsedLiveTimeValue = ((DBR_Double) dbr).getDoubleValue()[0];
				}
				notifyIObservers(EpicsMCA.this, getCachedElapsedParameters());
			} else if (dbr != null && dbr.isFLOAT()) {
				synchronized (timeLock) {
					elapsedLiveTimeValue = ((DBR_Float) dbr).getFloatValue()[0];
				}
				notifyIObservers(EpicsMCA.this, getCachedElapsedParameters());
			}
		}
	}

	// TODO separate
	private class RoiMonitorListener implements MonitorListener {
		@Override
		public void monitorChanged(MonitorEvent mev) {
			Channel ch = (Channel) mev.getSource();
			// System.out.println("inside roi monitor");
			for (int i = 0; i < numberOfRegions; i++) {
				if (ch == roiLowChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isINT()) {
						roiLowValues[i] = ((DBR_Int) dbr).getIntValue()[0];
						notifyIObservers(EpicsMCA.this, getCachedNthRegionsOfInterest(i));
						break;
					}

				} else if (ch == roiHighChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isINT()) {
						roiHighValues[i] = ((DBR_Int) dbr).getIntValue()[0];
						notifyIObservers(EpicsMCA.this, getCachedNthRegionsOfInterest(i));
						break;
					}

				} else if (ch == roiBackgroundChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isINT()) {
						roiBackgroundValues[i] = ((DBR_Int) dbr).getIntValue()[0];
						notifyIObservers(EpicsMCA.this, getCachedNthRegionsOfInterest(i));
						break;
					}

				} else if (ch == roiPresetCountChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isDOUBLE()) {
						roiPresetCountValues[i] = ((DBR_Double) dbr).getDoubleValue()[0];
						notifyIObservers(EpicsMCA.this, getCachedNthRegionsOfInterest(i));
						break;
					}

				}

				else if (ch == roiCountChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isDOUBLE()) {
						roiCountValues[i] = ((DBR_Double) dbr).getDoubleValue()[0];
						double ro[] = getCachedNthRegionOfInterestCount(i);
						double roc[] = new double[ro.length + 1];
						roc[0] = i;
						for (int j = 0; j < ro.length; j++) {
							roc[j + 1] = ro[j];
						}

						notifyIObservers(EpicsMCA.this, roc);
						break;

					}

				} else if (ch == roiNetCountChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isDOUBLE()) {
						roiNetCountValues[i] = ((DBR_Double) dbr).getDoubleValue()[0];
						double ro[] = getCachedNthRegionOfInterestCount(i);
						double roc[] = new double[ro.length + 1];
						roc[0] = i;
						for (int j = 0; j < ro.length; j++) {
							roc[j + 1] = ro[j];
						}

						notifyIObservers(EpicsMCA.this, roc);
						break;
					}

				} else if (ch == roiNameChannels[i]) {
					DBR dbr = mev.getDBR();
					if (dbr != null && dbr.isSTRING()) {
						roiNameValues[i] = ((DBR_String) dbr).getStringValue()[0];
						notifyIObservers(EpicsMCA.this, getCachedNthRegionsOfInterest(i));
						break;
					}

				}

			}

		}
	}

	@Override
	public void initializationCompleted() {
		try {
			this.getElapsedParameters();
			this.getRegionsOfInterest();
			this.getRegionsOfInterestCount();
		} catch (DeviceException e) {
			logger.error("EpicsMCA failed to initialise elapsed and roi values");
			e.printStackTrace();
		}
	}

	private String getMcaRecordName() {
		return mcaRecordName;
	}

	/**
	 * Sets the MCA Record's Name.
	 * 
	 * @param name
	 */
	public void setMcaRecordName(String name) {
		this.mcaRecordName = name;
	}

	private Object getRegionsOfInterest(int noOfRegions) throws DeviceException {

		try {

			Vector<EpicsMCARegionOfInterest> roiVector = new Vector<EpicsMCARegionOfInterest>();
			for (int regionIndex = 0; regionIndex < noOfRegions; regionIndex++) {
				EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();

				rois.setRegionIndex(regionIndex);

				if (roiLowChannels[regionIndex].getConnectionState() == Channel.CONNECTED) {
					int low = controller.cagetInt(roiLowChannels[regionIndex]);
					if (low >= 0) {
						rois.setRegionLow(low);
						roiLowValues[regionIndex] = low;
					} else
						continue;
				} else {
					logger.error("Connection to Region Low channel failed");
				}
				if (roiHighChannels[regionIndex].getConnectionState() == Channel.CONNECTED) {
					int high = controller.cagetInt(roiHighChannels[regionIndex]);
					if (high >= rois.getRegionLow()) {
						rois.setRegionHigh(high);
						roiHighValues[regionIndex] = high;
					} else
						continue;
				} else {
					logger.error("Connection to Region High channel failed");
				}
				if (roiBackgroundChannels[regionIndex].getConnectionState() == Channel.CONNECTED) {
					roiBackgroundValues[regionIndex] = controller.cagetShort(roiBackgroundChannels[regionIndex]);
					rois.setRegionBackground(roiBackgroundValues[regionIndex]);
				} else {
					logger.error("Connection to Region Background channel failed");
				}
				if (roiPresetCountChannels[regionIndex].getConnectionState() == Channel.CONNECTED) {
					roiPresetCountValues[regionIndex] = controller.cagetDouble(roiPresetCountChannels[regionIndex]);
					rois.setRegionPreset(roiPresetCountValues[regionIndex]);
				} else {
					logger.error("Connection to Region preset channel failed");
				}

				if (roiNameChannels[regionIndex].getConnectionState() == Channel.CONNECTED) {
					roiNameValues[regionIndex] = controller.cagetString(roiNameChannels[regionIndex]);
					rois.setRegionName(roiNameValues[regionIndex]);
				} else {
					logger.error("Connection to Region Background channel failed");
				}
				roiVector.add(rois);
			}
			if (roiVector.size() != 0) {
				EpicsMCARegionOfInterest[] selectedrois = new EpicsMCARegionOfInterest[roiVector.size()];
				for (int j = 0; j < selectedrois.length; j++) {
					selectedrois[j] = roiVector.get(j);
				}
				return selectedrois;
			}
			return null;
		} catch (Throwable th) {
			throw new DeviceException("failed get regions of interest", th);
		}

	}

	private Object getCachedNthRegionsOfInterest(int regionIndex) {
		EpicsMCARegionOfInterest rois = new EpicsMCARegionOfInterest();

		rois.setRegionIndex(regionIndex);
		rois.setRegionLow(roiLowValues[regionIndex]);
		rois.setRegionHigh(roiHighValues[regionIndex]);
		rois.setRegionBackground(roiBackgroundValues[regionIndex]);
		rois.setRegionPreset(roiPresetCountValues[regionIndex]);
		rois.setRegionName(roiNameValues[regionIndex]);
		return rois;
	}

	private double[] getCachedNthRegionOfInterestCount(int regionIndex) {
		double[] regionsCount = new double[2];
		regionsCount[0] = roiCountValues[regionIndex];
		regionsCount[1] = roiNetCountValues[regionIndex];
		return regionsCount;
	}

	@Override
	public long getNumberOfChannels() throws DeviceException {
		try {

			long noChannels = Long.MAX_VALUE;
			if (numberChannelsToUseChannel.getConnectionState() == Channel.CONNECTED) {
				noChannels = controller.cagetInt(numberChannelsToUseChannel);
			} else {
				logger.error("Connection to number channels to use channel failed");
			}

			return noChannels;
		} catch (Throwable th) {
			throw new DeviceException("failed get number of channels", th);
		}
	}

	@Override
	public void setNumberOfChannels(long channels) throws DeviceException {
		try {
			if (numberChannelsToUseChannel.getConnectionState() == Channel.CONNECTED
					&& maxNumberChannelsToUseChannel.getConnectionState() == Channel.CONNECTED) {
				long max = controller.cagetInt(maxNumberChannelsToUseChannel);
				if (channels > max) {
					throw new DeviceException("Invalid number of channels," + " Maximum channels allowed is  " + max);
				}
				controller.caput(numberChannelsToUseChannel, channels);
			} else {
				logger.error("Connection to number channels to use channel failed");
			}

		} catch (Throwable th) {
			throw new DeviceException("failed to set number of channels", th);
		}

	}

	@Override
	public void collectData() throws DeviceException {
		clear();

		// check that a preset time has been set
		EpicsMCAPresets presets = (EpicsMCAPresets) getPresets();

		// if it has, then begin collection
		if (presets.getPresetLiveTime() > 0.0 && this.collectionTime > 0.0) {
			startAcquisition();
		}
		// otherwise, stop collection and tell user, so any scans do not
		// continue
		// forever
		else {
			stopAcquisition(); // throws an exception?
		}

	}

	@Override
	public Object readout() throws DeviceException {
		return getData();
	}

	/**
	 * Gets the MCA record name.
	 * 
	 * @return the Epics MCA record name.
	 */
	public String getEpicsMcaRecordName() {
		return epicsMcaRecordName;
	}

	/**
	 * Sets the MCA record name.
	 * 
	 * @param epicsMcaRecordName
	 */
	public void setEpicsMcaRecordName(String epicsMcaRecordName) {
		this.epicsMcaRecordName = epicsMcaRecordName;
	}

	/**
	 * 
	 */
	public static final String channelToEnergyPrefix = "channelToEnergy:";
	/**
	 * 
	 */
	public static final String numberOfChannelsAttr = "NumberOfChannels";
	/**
	 * 
	 */
	public static final String energyToChannelPrefix = "energyToChannel";

	@Override
	public Object getAttribute(String attributeName) throws DeviceException {

		if (attributeName.startsWith(channelToEnergyPrefix)) {
			String energy = null;
			if (channelToEnergyConverter == null && converterName != null) {
				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
			}
			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
				String channelString = attributeName.substring(channelToEnergyPrefix.length());
				Quantity channel = Quantity.valueOf(channelString);
				try {
					energy = ((IQuantityConverter) channelToEnergyConverter).toSource(channel).toString();
					return energy;
				} catch (Exception e) {
					throw new DeviceException("EpicsMCA.getAttribute exception", e);
				}
			}
			throw new DeviceException(
					"EpicsMCA : unable to find suitable converter to convert channel to energy. converterName  "
							+ converterName == null ? "not given" : converterName);
		} else if (attributeName.startsWith(energyToChannelPrefix)) {
			// String channel = null;
			if (channelToEnergyConverter == null && converterName != null) {
				channelToEnergyConverter = CoupledConverterHolder.FindQuantitiesConverter(converterName);
			}
			if (channelToEnergyConverter != null && channelToEnergyConverter instanceof IQuantityConverter) {
				String energyString = attributeName.substring(energyToChannelPrefix.length());
				Quantity energy = Quantity.valueOf(energyString);
				try {
					long ichannel = (long) ((IQuantityConverter) channelToEnergyConverter).toTarget(energy).getAmount();
					return Long.toString(Math.max(Math.min(ichannel, getNumberOfChannels() - 1), 0));
				} catch (Exception e) {
					throw new DeviceException("EpicsMCA.getAttribute exception", e);
				}
			}
			throw new DeviceException(
					"EpicsTCA : unable to find suitable converter to convert energy to channel. converterName  "
							+ converterName == null ? "not given" : converterName);
		} else if (attributeName.equals(numberOfChannelsAttr)) {
			return 1024;// getNumberOfChannels();
		} else {
			return super.getAttribute(attributeName);
		}
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

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS Mca";
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
