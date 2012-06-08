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

package gda.device.detector.pilatus;

import gda.configuration.epics.ConfigurationNotFoundException;
import gda.configuration.epics.Configurator;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.epics.connection.InitializationListener;
import gda.epics.interfaces.PilatusType;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
/**
 *
 */
public class EpicsPilatus extends DetectorBase implements
		InitializationListener, Pilatus {
	private static final Logger logger = LoggerFactory
			.getLogger(EpicsPilatus.class);
	
	private String modeRecordName;
	private String gainRecordName;
	private String thresholdRecordName;
	private String startRecordName;
	private String abortRecordName;
	private String exp_tRecordName;
	private String nimagesRecordName;
	private String exp_pRecordName;
//	private String nexpimagesRecordName;
	private String delayRecordName;
	private String pathRecordName;
	private String filenameRecordName;
	private String fileformatRecordName;
	private String headerRecordName;
	private String filenumberRecordName;
/*	private String autoincrementRecordName;
	private String timeoutRecordName;
*/	private String generalBoxPv;
	private String generalBoxSendPv;
/*	private String bpnameRecordName;
	private String ffnameRecordName;
*/
	private Channel mode;
	private Channel gain;
	private Channel threshold;
	private Channel start;
	private Channel abort;
	private Channel exp_t;
	private Channel nimages;
	private Channel exp_p;
//	private Channel nexpimages;
	private Channel delay;
	private Channel path;
	private Channel filename;
	private Channel fileformat;
	private Channel headerChannel;
	private Channel filenumber;
/*	private Channel autoincrement;
	private Channel timeout;
*/	private Channel generalBoxChannel;
	private Channel generalBoxSendChannel;
/*	private Channel bpname;
	private Channel ffname;
*/
	//give default values so that the initial check before they are set don't fail
	private String filenameString="";
	private String filepathString="";
	private String fileformatString="%s%s%04d.tiff";
	private int filenumberInt = 1;
	private double cached_exptime;
	private double cached_expperiod;

	private Vector<String> modeNames= new Vector<String>();
	private Vector<String> gainNames = new Vector<String>();
	private String deviceName;
	private String pvName;

	private EpicsChannelManager channelManager;
	private EpicsController controller;

	public EpicsPilatus() {
		controller = EpicsController.getInstance();
		channelManager = new EpicsChannelManager(this);
	}

	private void createChannelAccess() throws FactoryException {
		try {
			mode = channelManager.createChannel(modeRecordName, false);
			gain = channelManager.createChannel(gainRecordName, false);
			threshold = channelManager
					.createChannel(thresholdRecordName, false);
			start = channelManager.createChannel(startRecordName, false);
			abort = channelManager.createChannel(abortRecordName, false);
			exp_t = channelManager.createChannel(exp_tRecordName, false);
			nimages = channelManager.createChannel(nimagesRecordName, false);
			exp_p = channelManager.createChannel(exp_pRecordName, false);
/*			nexpimages = channelManager.createChannel(nexpimagesRecordName,
					false);
*/			delay = channelManager.createChannel(delayRecordName, false);
			path = channelManager.createChannel(pathRecordName, false);
			filename = channelManager.createChannel(filenameRecordName, false);
			fileformat = channelManager.createChannel(fileformatRecordName,
					false);
			headerChannel = channelManager.createChannel(headerRecordName, false);
			filenumber = channelManager.createChannel(filenumberRecordName,
					false);
/*			autoincrement = channelManager.createChannel(
					autoincrementRecordName, false);
			timeout = channelManager.createChannel(timeoutRecordName, false);
*/			generalBoxChannel = channelManager.createChannel(generalBoxPv, false);
			generalBoxSendChannel = channelManager.createChannel(generalBoxSendPv, false);
//			bpname = channelManager.createChannel(bpnameRecordName, false);
//			ffname = channelManager.createChannel(ffnameRecordName, false);

			channelManager.creationPhaseCompleted();
		} catch (Throwable th) {
			// TODO take care of destruction
			throw new FactoryException("failed to connect to all channels", th);
		}
	}

	@Override
	public void configure() throws FactoryException {
		this.setInputNames(new String[] {"ExposureTime"});
		this.setExtraNames(new String[] {"Filepath"});
		this.setOutputFormat(new String[] {"%f", "%s"});
		cached_exptime = 0.;
		cached_expperiod = 0;
		if (!configured) {
				if (getDeviceName() != null) {
					PilatusType pvConfig;
					try {
						pvConfig = Configurator.getConfiguration(getDeviceName(),
								PilatusType.class);
						setRecordNames(pvConfig);
					} catch (ConfigurationNotFoundException e) {
						logger.error("Can NOT find EPICS configuration for motor "
								+ getDeviceName(), e);
					}
				} else if (getPvName() != null) {
					setRecordNamesPvName();
				}

			// Nothing specified in Server XML file
			else {
				logger
						.error("Missing EPICS interface configuration for the Pilatus detector "
								+ getName());
				throw new FactoryException(
						"Missing EPICS interface configuration for the Pilatus detector "
								+ getName());
			}
			createChannelAccess();
			channelManager.tryInitialize(100);

			configured = true;
		}
	}

	@Override
	public void reconfigure() throws FactoryException{
		configured = false;
		channelManager = null;
		configure();
	}

	@Override
	public void collectData() throws DeviceException {
		acquireCommand();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public int getStatus() throws DeviceException {
		if (isBusyCommand()) {
			return BUSY;
		}
		return IDLE;
	}

	@Override
	public Object readout() throws DeviceException {
		updateFilenumber();
		String formatted = String.format(fileformatString, filepathString, filenameString, filenumberInt);
		return formatted;
	}

	/**
	 * Dectris recommend using setting both collection time and collection period. Previous experiments seem to have used just collection time
	 */
	@Override
	public void setCollectionTime(double t) {
		try {
			setCollectionTimeCommand(t);
		} catch (DeviceException e) {
			logger.error("Exception while setting collection time", e);
		}
		cached_exptime = t;
	}
	
	/**
	 * Dectris recommend using setting both collection time and collection period. Previous experiments seem to have used just collection time
	 */
	@Override
	public void setCollectionPeriod(double t) {
		try {
			setCollectionPeriodCommand(t);
		} catch (DeviceException e) {
			logger.error("Exception while setting collection time", e);
		}
		cached_expperiod = t;
	}

	/**
	 *Returns the cached collection time because we cannot get this information
	 * directly from the detector
	 */
	@Override
	public double getCollectionTime() {
		return cached_exptime;
	}
	
	/**
	 *Returns the cached collection period because we cannot get this information directly from the detector
	 */
	@Override
	public double getCollectionPeriod() {
		return cached_expperiod;
	}
	
	@Override
	public void stop() throws DeviceException {
		stopCommand();
	}
	
	@Override
	public Object getPosition() throws DeviceException {
		updateFilenumber();
		readout();
		Object[] toReturn = new Object[]{cached_exptime, filenumberInt};
		return toReturn;
	}
	
	@Override
	public void asynchronousMoveTo(Object time) throws DeviceException {
		setCollectionTime((Double) time);
		collectData();
	}

	@Override
	public void initializationCompleted() {
		String[] mode = null;
		try {
			mode = getModeLabels();
			for (int i = 0; i < mode.length; i++) {
				if (mode[i] != null || mode[i] != "") {
					modeNames.add(mode[i]);
				}
			}
		} catch (DeviceException e) {
			logger.error("Problem getting mode labels", e);
		}
		String[] gain = null;
		try {
			gain = getGainLabels();
			for (int i = 0; i < gain.length; i++) {
				if (gain[i] != null || gain[i] != "") {
					gainNames.add(gain[i]);
				}
			}
		} catch (DeviceException e) {
			logger.error("Problem getting gain labels", e);
		}
	}

	//noninterface commands
	//TODO borrowed from EpicsPositioner... perhaps we should use a couple of these for mode and gain
	/**
	 * Set acquisition mode of detector
	 * @param modeName
	 * @throws DeviceException
	 */
	@Override
	public void setMode(String modeName) throws DeviceException {
		// find in the modeNames array the index of the string
		if (modeNames.contains(modeName)) {
			int target = modeNames.indexOf(modeName);
			try {
				controller.caputWait(mode, target);
			} catch (Throwable th) {
				throw new DeviceException("Failed to set mode to " + modeName, th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Mode called: " + modeName + " not found.");
	}

	//TODO borrowed from EpicsPositioner... perhaps we should use a couple of these for mode and gain
	@Override
	public void setGain(String gainName) throws DeviceException {
		// find in the gainNames array the index of the string
		if (gainNames.contains(gainName)) {
			int target = gainNames.indexOf(gainName);
			try {
				controller.caputWait(gain, target);
			} catch (Throwable th) {
				throw new DeviceException("Failed to set gain to " + gainName, th);
			}
			return;
		}

		// if get here then wrong position name supplied
		throw new DeviceException("Gain called: " + gainName + " not found.");
	}
	
	/**
	 * Get acquisition mode of detector
	 * @return acquireMode
	 * @throws DeviceException
	 */
	@Override
	public String getMode() throws DeviceException {
		try {
			short modeIndex = controller.cagetEnum(mode);
			return modeNames.get(modeIndex);
		} catch (Exception e) {
			throw new DeviceException("Exception while getting mode ", e);
		}
	}
	
	@Override
	public String getGain() throws DeviceException {
		try {
			short gainIndex = controller.cagetEnum(gain);
			return gainNames.get(gainIndex);
		} catch (Exception e) {
			throw new DeviceException("Exception while getting gain ",e);
		}
	}
	
	@Override
	public String[] getModeLabels() throws DeviceException {
		String[] modeLabels = new String[5]; // TODO hard-coded value
		try {
			modeLabels = controller.cagetLabels(mode);
		} catch (Exception e) {
			throw new DeviceException("Exception while getting mode labels ",e);
		}
		return modeLabels;
	}
	
	@Override
	public String[] getGainLabels() throws DeviceException {
		String[] gainLabels = new String[4]; //TODO hard-coded value
		try {
			gainLabels = controller.cagetLabels(gain);
		} catch (Exception e) {
			throw new DeviceException("Exception while getting gain labels ", e);
		}
		return gainLabels;
	}
	//commands that talk directly to the PVs
	
	@Override
	public void setFilename(String filename) throws DeviceException {
		try {
			controller.caputWait(this.filename, filename);
		} catch (Throwable th) {
			logger.error("Error when trying to set filename", th);
			throw new DeviceException("setFilename exception", th);
		}
		this.filenameString = filename;
	}

	@Override
	public void setFileformat(String fileformat) throws DeviceException {
		try {
			controller.caputWait(this.fileformat, fileformat);
		} catch (Throwable th) {
			logger.error("Error when trying to set file format", th);
			throw new DeviceException("setFileformat exception", th);
		}
		this.fileformatString = fileformat;
	}
	
	@Override
	public void setFileHeader(String fileHeader) throws DeviceException {
		try {
			controller.caputWait(this.headerChannel, fileHeader);
		} catch (Throwable th) {
			logger.error("Error when trying to set file header", th);
			throw new DeviceException("setFileHeader exception", th);
		}
	}

	@Override
	public void setFilepath(String filepath) throws DeviceException {
		try {
			controller.caputWait(path, filepath);
		} catch (Throwable th) {
			logger.error("Error when trying to set file path", th);
			throw new DeviceException("setFilepath exception", th);
		}
		this.filepathString = filepath;
	}

	/**
	 * Sets the number of the next file to be recorded
	 * @param n
	 * @throws DeviceException
	 */
	@Override
	public void setFilenumber(int n) throws DeviceException {
		try {
			controller.caputWait(filenumber, n);
		} catch (Throwable th) {
			logger.error("Error while setting filenumber", th);
			throw new DeviceException("setFilenumber exception", th);
		}
	}
	
	/**
	 * Returns the current file number stored by EPICS
	 * @return fileNumber
	 * @throws DeviceException
	 */
	@Override
	public int getFilenumber() throws DeviceException {
		try {
			return controller.cagetInt(filenumber);
		} catch (Throwable th) {
			logger.error("Error when getting filenumber", th);
			throw new DeviceException("getFilenumber exception", th);
		}
	}
	/**
	 * Returns the number of the last file recorded
	 * @throws DeviceException
	 */
	@Override
	public void updateFilenumber() throws DeviceException {
		try {
			filenumberInt = controller.cagetInt(filenumber) - 1;
		} catch (Throwable th) {
			logger.error("Error when getting filenumber", th);
			throw new DeviceException("updateFilenumber exception", th);
		}
	}

	@Override
	public void setNumberImages(int n) throws DeviceException {
		try {
			controller.caputWait(nimages, n);
		} catch (Throwable th) {
			logger.error("Error when trying to set the number of images", th);
			throw new DeviceException("setNumberImages exception", th);
		}
	}
	
	/**
	 * This sets the collection time - the time the detector is actually exposing. Most useful for single images. Use collection period for multiple images
	 * @param t
	 * @throws DeviceException
	 */
	public void setCollectionTimeCommand (double t) throws DeviceException {
		try {
			controller.caputWait(exp_t, t);
		} catch (Throwable th) {
			logger.error("Error when trying to set the collection time", th);
			throw new DeviceException("setCollectionTimeCommand exception", th);
		}
	}
	
	/**
	 * Used for setting collection period. For multiple images, user would most likely define the period that they want the collection to work at.
	 * The time should then be calculated based on the period.
	 * @param t
	 * @throws DeviceException
	 */
	public void setCollectionPeriodCommand (double t) throws DeviceException {
		try {
			controller.caputWait(exp_p, t);
		} catch (Throwable th) {
			logger.error("Error when trying to set the collection period", th);
			throw new DeviceException("setCollectionPeriodCommand exception", th);
		}
	}
	
	public boolean isBusyCommand () throws DeviceException {
		try {
			return !(controller.cagetInt(start)==0);
		} catch (Throwable th) {
			logger.error("Error when trying to get acquiring status", th);
			throw new DeviceException("isBusyCommand exception", th);
		}
	}
	
	public void stopCommand () throws DeviceException {
		try {
			controller.caputWait(abort, 1);
		} catch (Throwable th) {
			logger.error("Error when trying to set abort PV", th);
			throw new DeviceException("stopCommand exception", th);
		}
	}
	/**
	 * The acquire command must not be a caputWait because it returns only after the acquisition is complete
	 */
	public void acquireCommand () throws DeviceException {
		if (!isBusy()) {
			try {
				controller.caput(start, 1);
			} catch (Throwable th) {
				logger.error("Error when trying to start acquisition", th);
				throw new DeviceException("acquireCommand exception", th);
			}
		}
	}
	
	@Override
	public double getThresholdEnergy() throws DeviceException {
		double returnValue;
		try {
			logger.warn("This method returns before the underlying hardware has been updated");
			returnValue = controller.cagetDouble(threshold);
		} catch (Throwable th) {
			logger.error("Error when trying to get threshold energy", th);
			throw new DeviceException("getThresholdEnergy exception", th);
		}
		return returnValue;
	}
	
	@Override
	public void setThresholdEnergy(double energyValue) throws DeviceException {
		try {
			controller.caputWait(threshold, energyValue);
			logger.info("Wait of 90 sec (6m) or 40 sec (2m) recommended to allow detector to finish command");
		} catch (Throwable th) {
			logger.error("Error when trying to set threshold energy", th);
			throw new DeviceException("setThresholdEnergy exception", th);
		}
	}

	@Override
	public void setDelayTime(double delaySec) throws DeviceException {
		try {
			controller.caputWait(delay, delaySec);
		} catch (Throwable th) {
			logger.error("Error when trying to set delay", th);
			throw new DeviceException("setDelayTime exception", th);
		}
	}
	
	@Override
	public double getDelayTime() throws DeviceException {
		try {
			return controller.cagetDouble(delay);
		} catch (Throwable th) {
			logger.error("Error when trying to get delay", th);
			throw new DeviceException("getDelayTime exception", th);
		}
	}
	/**
	 * gets the name of the device.
	 * 
	 * @return String name
	 */
	public String getDeviceName() {
		return deviceName;
	}

	/**
	 * sets the name of the device.
	 * 
	 * @param deviceName
	 */
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	private void setRecordNames(PilatusType config) {
		modeRecordName = config.getMODE().getPv();
		gainRecordName = config.getGAIN().getPv();
		thresholdRecordName = config.getTHRESHOLD().getPv();
		startRecordName = config.getSTART().getPv();
		abortRecordName = config.getABORT().getPv();
		exp_tRecordName = config.getEXP_T().getPv();
		nimagesRecordName = config.getNIMAGES().getPv();
		exp_pRecordName = config.getEXP_P().getPv();
//		nexpimagesRecordName = config.getNEXPIMAGES().getPv();
		delayRecordName = config.getDELAY().getPv();
		pathRecordName = config.getPATH().getPv();
		filenameRecordName = config.getFILENAME().getPv();
		fileformatRecordName = config.getFILEFORMAT().getPv();
		filenumberRecordName = config.getFILENUMBER().getPv();
/*		autoincrementRecordName = config.getAUTOINCREMENT().getPv();
		timeoutRecordName = config.getTIMEOUT().getPv();
*///		bpnameRecordName = config.getBPNAME().getPv();
//		ffnameRecordName = config.getFFNAME().getPv();
//		headerRecordName = config.getHEADER().getPv();
	}

	private void setRecordNamesPvName() {
		modeRecordName = getPvName() + ":AcquireMode";
		gainRecordName = getPvName() + ":Gain";
		thresholdRecordName = getPvName() + ":ThresholdEnergy";
		startRecordName = getPvName() + ":Acquire";
		abortRecordName = getPvName() + ":Abort";
		exp_tRecordName = getPvName() + ":ExposureTime";
		nimagesRecordName = getPvName() + ":NImages";
		exp_pRecordName = getPvName() + ":ExposurePeriod";
//		nexpimagesRecordName = getPvName() + ":NExposures";
		delayRecordName = getPvName() + ":DelayTime";
		pathRecordName = getPvName() + ":FilePath";
		filenameRecordName = getPvName() + ":Filename";
		fileformatRecordName = getPvName() + ":FileFormat";
		headerRecordName = getPvName() + ":Header";
		filenumberRecordName = getPvName() + ":FileNumber";
/*		autoincrementRecordName = getPvName() + ":AutoIncrement";
		timeoutRecordName = getPvName() + ":ReadTiffTimeout";
*/		generalBoxPv = getPvName() + ":GEN";
		generalBoxSendPv = getPvName() + ":GEN:SEND";
	}
	@Override
	public String getDescription() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void setGeneralText(String toSend) {
		try {
			controller.caputAsWaveform(generalBoxChannel, toSend);
		} catch (CAException e) {
			logger.error("EpicsGeneralBox: CAException ", e);
		} catch (InterruptedException e) {
			logger.error("EpicsGeneralBox: InterruptedException", e);
		}
	}

	@Override
	public void sendGeneralText() {
		try {
			controller.caputWait(generalBoxSendChannel, 1);
		} catch (TimeoutException e) {
			logger.error("EpicsGeneralBox: TimeoutException", e);
		} catch (CAException e) {
			logger.error("EpicsGeneralBox: CAException", e);
		} catch (InterruptedException e) {
			logger.error("EpicsGeneralBox: InterruptedException", e);
		}
	}
	
	@Override
	public boolean isLocal() {
		return true;
	}
	
}
