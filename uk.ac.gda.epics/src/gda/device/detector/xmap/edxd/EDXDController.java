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

package gda.device.detector.xmap.edxd;

import gda.device.Detector;
import gda.device.DeviceException;
import gda.device.detector.DetectorBase;
import gda.device.epicsdevice.EpicsMonitorEvent;
import gda.device.epicsdevice.FindableEpicsDevice;
import gda.device.epicsdevice.IEpicsChannel;
import gda.device.epicsdevice.ReturnType;
import gda.factory.Configurable;
import gda.factory.FactoryException;
import gda.jython.InterfaceProvider;
import gda.observable.IObserver;
import gda.util.persistence.LocalDatabase.LocalDatabaseException;
import gda.util.persistence.LocalObjectShelf;
import gda.util.persistence.LocalObjectShelfManager;
import gda.util.persistence.ObjectShelfException;
import gov.aps.jca.dbr.DBR_Enum;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class describes the EDXD detector on I12, it is made up of 24 subdetectors
 */
public class EDXDController extends DetectorBase implements Configurable {
	private static final Logger logger = LoggerFactory.getLogger(EDXDController.class);
	protected static final int NUMBER_OF_ATTEMPTS = 5;
	protected int numberOfElements = 24;
	protected static final int TOTAL_NUMBER_OF_TRACE_DATASETS = 10;
	protected static final String EDXD_PLOT = "EDXD Plot";
	protected int version  = 2;
	protected FindableEpicsDevice xmap = null;
	protected boolean isBusy = false;
	protected String epicsDeviceName="edxd";
	protected List<EDXDElement> subDetectors = new ArrayList<EDXDElement>();
	protected DeviceException collectData_Exception;
	protected String GETPRESETVALUE = "GETPRESETVALUE";
	protected String GETPRESETTYPE = "GETPRESETTYPE";
	protected IEpicsChannel statusChannel;
	protected static String STATUS = "ACQUIRING";
	protected static String RESUMEMODE = "SETRESUME";
	protected static String SCAACTIVATE = "SCAACTIVATE";
	protected static String SETPRESETVALUE = "SETPRESETVALUE";
	protected static String SETPRESETTYPE = "SETPRESETTYPE";
	protected static String SCAELEMENTS = "SCAELEMENTS";


	// TODO these are accessed directly and shouldn't be
	public enum COLLECTION_MODES {MCA_SPECTRA, MCA_MAPPING, SCA_MAPPING , LIST_MAPPING}
	public enum PRESET_TYPES {NO_PRESET, REAL_TIME, LIVE_TIME , EVENTS, TRIGGERS}
	public enum PIXEL_ADVANCE_MODE { GATE, SYNC}
	public enum NEXUS_FILE_MODE {SINGLE, CAPTURE, STREAM}

	public EDXDController() {
	}

	public String getEpicsDeviceName() {
		return epicsDeviceName;
	}

	public void setEpicsDeviceName(String deviceName) {
		this.epicsDeviceName = deviceName;
	}

	@Override
	public void configure() throws FactoryException {
		//FIXME this should be pulled in from the spring configuration
		xmap = new FindableEpicsDevice();
		xmap.setDeviceName(epicsDeviceName);
		xmap.setName(epicsDeviceName);
		xmap.configure();
		statusChannel = xmap.createEpicsChannel(ReturnType.DBR_NATIVE, STATUS , "");
		statusChannel.addIObserver(new IObserver(){

			@Override
			public void update(Object source, Object arg) {
				logger.info("the status update from xmap is " + arg);
				if(arg instanceof EpicsMonitorEvent){
					EpicsMonitorEvent evt = (EpicsMonitorEvent) arg;
					isBusy = ((DBR_Enum)evt.epicsDbr).getEnumValue()[0] == 1;
				}
				else {
					isBusy = false;
				}
				try {
					notifyIObservers(this, getStatusObject());
					logger.debug("acquisition status updated to {}", getStatus());
				} catch (DeviceException e) {
					logger.error("ln351 : AcqStatusListener , error ", e);
				}

			}
		});
		// Add all the EDXD Elements to the detector
		for (int i = 0; i < numberOfElements; i++)
			subDetectors.add(new EDXDElement(xmap, i + 1));
	}

	@Override
	public void reconfigure() throws FactoryException {
		try {
			xmap.dispose();
		} catch (DeviceException e) {
			logger.warn(
					"DeviceException while trying to shutdown EpicsDevice during reconfigure of EDXDController. Will continue with reconfigure anyway",
					e);
		}
		xmap = null;
		configure();
	}

	protected Integer getStatusObject() {
		return isBusy ? Detector.BUSY : Detector.IDLE;
	}

	@Override
	public void collectData() throws DeviceException {
		collectData_Exception=null;
		// set the acquisition time
		xmap.setValue("SETPRESETVALUE","",collectionTime);
		// set to take the acquisition for the amount of time specified
		// 0 = Disabled
		// 1 = real time
		// 2 = live time
		// 3 = events
		xmap.setValue("SETPRESETTYPE","",1);
		(new Thread() {
			@Override
			public void run() {
				//isBusy = true;
				// now run the actual collection
				// this has been seen to fail, so a loop trying a couple of times would probably be good
				try {
					xmap.setValue(null,"ACQUIRE","",1,(2*collectionTime)+5);
				} catch (DeviceException e) {
					logger.error(e.getMessage(),e);
					collectData_Exception = e;
				}
				//isBusy = false;
				return;
			}
		}).start();

		// now give the thread enough time to start before returning
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// Just carry on, it shouldent be too much of a problem if this fails
		}
	}

	@Override
	public String getDescription() throws DeviceException {
		return "The EDXD Detector controller";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return " EDXD Detector";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Multi channel MCA";
	}

	@Override
	public int getStatus() throws DeviceException {
		return getStatusObject();
	}


	/**
	 * Get the acquisition/collection/preset  time in the hardware
	 * @return collection time
	 * @throws DeviceException
	 */
	public double getAcquisitionTime() throws DeviceException {
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,GETPRESETVALUE  ,"");
	}

	/**
	 * Get the acquisition mode in the hardware
	 * @return mode
	 * @throws DeviceException
	 */
	public int getPresetType() throws DeviceException {
		return (Integer) xmap.getValue(ReturnType.DBR_NATIVE,GETPRESETTYPE ,"");
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return false;
	}

	/**
	 * Check for any exception during data collection
	 * @throws DeviceException
	 */
	//should be changed to private access.
	public void verifyData() throws DeviceException {
		// If there was a problem when acquiring the data
		if (collectData_Exception != null)
			throw collectData_Exception;
	}

	/**
	 * Get the number of the mca elements attached to the controller
	 * @return number of mca elements
	 */
	public int getNumberOfElements() {
		return numberOfElements;
	}

	/**
	 * Set the number of mca elements attached to the controller
	 * @param numberOfElements
	 */
	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}

	/**
	 * Sets the number of bins that are used by the xmap.
	 * @param NumberOfBins a number up to 16k
	 * @return the number of bins which are actualy set.
	 * @throws DeviceException
	 */
	public int setBins(int NumberOfBins) throws DeviceException {
		xmap.setValue("SETNBINS","",NumberOfBins);
		return (Integer) xmap.getValue(ReturnType.DBR_NATIVE,"GETNBINS","");
	}

	/**
	 *
	 * @return The number of bins the xmap is curently set to
	 * @throws DeviceException
	 */
	public int getBins() throws DeviceException {
		return (Integer) xmap.getValue(ReturnType.DBR_NATIVE,"GETNBINS","");
	}

	/**
	 * Sets the dynamic range of the detector
	 * @param dynamicRange the dynamic range in KeV
	 * @return the actual value which has been set
	 * @throws DeviceException
	 */
	public double setDynamicRange(double dynamicRange) throws DeviceException {
		xmap.setValue("SETDYNRANGE","",dynamicRange);
		return (Double) xmap.getValue(ReturnType.DBR_NATIVE,"GETDYNRANGE0","");
	}

	/**
	 * Sets the energy per bin
	 * @param binWidth in eV
	 * @throws DeviceException
	 */
	public void setBinWidth(double binWidth) throws DeviceException {
		xmap.setValue("SETBINWIDTH","",binWidth);
	}

	/**
	 * Simple method which sets up the EDXD with some basic assumptions
	 * @param maxEnergy The maxim energy expected in KeV
	 * @param numberOfBins the number of bins that are wanted, i.e the resolution
	 * @throws DeviceException
	 */
	public void setup(double maxEnergy, int numberOfBins ) throws DeviceException {
		setDynamicRange(maxEnergy*2.0);// set the dynamic range to twice the max energy
		int bins = setBins(numberOfBins);
		setBinWidth((maxEnergy*1000.0)/bins);
	}

	// All the saving and loading settings
	/**
	 * Save all the xmap settings, with no description
	 * @param name
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws DeviceException
	 */
	public void saveCurrentSettings(String name) throws ObjectShelfException, LocalDatabaseException, DeviceException {
		saveCurrentSettings(name,"No Description");
	}

	/**
	 * Save all the xmap settings along with the given description
	 * @param name
	 * @param description
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws DeviceException
	 */
	public void saveCurrentSettings(String name, String description) throws ObjectShelfException, LocalDatabaseException, DeviceException {
		LocalObjectShelf los = LocalObjectShelfManager.open("EDXD"+name);
		los.addValue("desc", description);// Save the description
		los.addValue("nbins", getBins());// save the number of bins
		// populate the shelf from the subdetectors
		for( int i = 0; i < subDetectors.size(); i++)
			los.addValue(subDetectors.get(i).getName(), subDetectors.get(i).saveConfiguration());
		InterfaceProvider.getTerminalPrinter().print("File Saved Sucsessfully");
	}

	/**
	 * Loads a setting from the persistance
	 * @param name
	 * @return The description of the configuration loaded
	 * @throws DeviceException
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 */
	public String loadSettings(String name) throws DeviceException, ObjectShelfException, LocalDatabaseException {
		LocalObjectShelf los = LocalObjectShelfManager.open("EDXD"+name);
		setBins((Integer) los.get("nbins"));// load the number of bins
		// populate the shelf from the subdetectors
		for( int i = 0; i < subDetectors.size(); i++) {
			logger.info("Setting subdetector {} with loaded values",i);
			subDetectors.get(i).loadConfiguration((EDXDElementBean) los.get(subDetectors.get(i).getName()));
		}
		String desc = "No Description";
		try {
			desc = (String) los.get("desc");
		} catch (ObjectShelfException e) {
			// Do nothing id the desc is absent
		}
		return desc;
	}

	/**
	 * This method shows all the savefiles for the EDXD detector
	 * @return a string containing all the files
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 * @throws LocalDatabaseException
	 * @throws ObjectShelfException
	 */
	public String listSettings() throws ObjectShelfException, LocalDatabaseException{
		String result = "";
		for (String shelf : LocalObjectShelfManager.shelves()) {
			if(shelf.startsWith("EDXD")) {
				LocalObjectShelf los = LocalObjectShelfManager.open(shelf);
				String desc = "No Description";
				try {
					desc = (String) los.get("desc");
				} catch (ObjectShelfException e) {
					// Do nothing id the desc is absent
				}
				result += shelf.replace("EDXD", "")+"\t:\t"+desc+"\n";
			}
		}
		return result;
	}

	// Setters for the various settings accross the board
	/**
	 * Set the preampGain for all elements
	 * @param preampGain
	 * @throws DeviceException
	 */
	public void setPreampGain(double preampGain) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setPreampGain(preampGain);
	}

	/**
	 * Set the peakTime for all elements
	 * @param peakTime
	 * @throws DeviceException
	 */
	public void setPeakTime(double peakTime) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setPeakTime(peakTime);
	}

	/**
	 * Set the triggerThreshold for all elements
	 * @param triggerThreshold
	 * @throws DeviceException
	 */
	public void setTriggerThreshold(double triggerThreshold) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setTriggerThreshold(triggerThreshold);
	}

	/**
	 * Set the baseThreshold for all elements
	 * @param baseThreshold
	 * @throws DeviceException
	 */
	public void setBaseThreshold(double baseThreshold) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setBaseThreshold(baseThreshold);
	}

	/**
	 * Set the baseLength for all elements
	 * @param baseLength
	 * @throws DeviceException
	 */
	public void setBaseLength(int baseLength) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setBaseLength(baseLength);
	}

	/**
	 * Set the energyThreshold for all elements
	 * @param energyThreshold
	 * @throws DeviceException
	 */
	public void setEnergyThreshold(double energyThreshold) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setEnergyThreshold(energyThreshold);
	}

	/**
	 * Set the resetDelay for all elements
	 * @param resetDelay
	 * @throws DeviceException
	 */
	public void setResetDelay(double resetDelay) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setResetDelay(resetDelay);
	}

	/**
	 * Set the gapTime for all elements
	 * @param gapTime
	 * @throws DeviceException
	 */
	public void setGapTime(double gapTime) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setGapTime(gapTime);
	}

	/**
	 * Set the triggerPeakTime for all elements
	 * @param triggerPeakTime
	 * @throws DeviceException
	 */
	public void setTriggerPeakTime(double triggerPeakTime) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setTriggerPeakTime(triggerPeakTime);
	}

	/**
	 * Set the triggerGapTime for all elements
	 * @param triggerGapTime
	 * @throws DeviceException
	 */
	public void setTriggerGapTime(double triggerGapTime) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setTriggerGapTime(triggerGapTime);
	}

	/**
	 * Set the maxWidth for all elements
	 * @param maxWidth
	 * @throws DeviceException
	 */
	public void setMaxWidth(double maxWidth) throws DeviceException {
		for( int i = 0; i < subDetectors.size(); i++)
			subDetectors.get(i).setMaxWidth(maxWidth);
	}

	/**
	 * Get one of the elements specifialy by name
	 * @param index
	 * @return An the EDXDElement requested
	 */
	public EDXDElement getSubDetector(int index) {
		//check that sub detectors exist before attempting to access any.
		if(subDetectors.size()>0)
			return subDetectors.get(index);
		return null;
	}

	/**
	* Controller has two modes of operation.
	* clear on start or resume acquiring into the same spectrum
	* @param resume
	* @throws DeviceException
	*/
	public void setResume(boolean resume)throws DeviceException{
		int toset =0;
		if(resume)
			toset = 1;
		xmap.setValue(RESUMEMODE  ,"",toset);
	}

	/**
	 * read the data from the specified mca element
	 * @param mcaNumber
	 * @return data array
	 * @throws DeviceException
	 */
	public double[] getData(int mcaNumber) throws DeviceException {
		 return subDetectors.get(mcaNumber).readoutDoubles();
	}

	/**
	 * set the acquisition/collection/preset time in the controller
	 * @param collectionTime
	 * @throws DeviceException
	 */
	public void setAquisitionTime(double collectionTime)throws DeviceException {
		xmap.setValue(SETPRESETVALUE ,"",collectionTime);
	}

	/**
	 * set the acquisition mode defined in PRESET_MODES in the controller
	 * @param mode
	 * @throws DeviceException
	 */
	public void setPresetType(PRESET_TYPES mode) throws DeviceException{
		xmap.setValueNoWait(SETPRESETTYPE, "", mode.ordinal());
	}

	/**
	 * Start data acquisition in the controller. Uses the existing resume mode
	 * @throws DeviceException
	 */
	public void start() throws DeviceException {
		xmap.setValueNoWait("ACQUIRE","",1);
	}

	@Override
	public void stop() throws DeviceException {
		xmap.setValueNoWait("ACQUIRE","",0);
	}

	/**
	 * Activate the ROI mode in the controller
	 * @throws DeviceException
	 */
	public void activateROI() throws DeviceException{
		xmap.setValue(SCAACTIVATE, "", 1);
	}


	/** Disable  the ROI mode in the Controller
	 * @throws DeviceException
	 */
	public void deactivateROI() throws DeviceException{
		xmap.setValue(SCAACTIVATE, "", 0);
	}

	/**
	 * get the maximum number of ROI allowed per mca element
	 * @return number of rois
	 * @throws DeviceException
	 */
	public int getMaxAllowedROIs() throws DeviceException {
		return ((Double)xmap.getValue(ReturnType.DBR_NATIVE, SCAELEMENTS, "")).intValue();
	}

	@Override
	public Object readout() throws DeviceException {
		double[][] readout = new double[numberOfElements][];
		for(int i =0; i< numberOfElements; i++)
			readout[i] = subDetectors.get(i).readoutDoubles();
		return readout;
	}

	/**
	 * Read the counts from the sub detector
	 * @param mcaNumber
	 * @return data array
	 * @throws DeviceException
	 */
	public int getEvents(int mcaNumber) throws DeviceException {
		return subDetectors.get(mcaNumber).getEvents();
	}

	public double getICR(int mcaNumber) throws DeviceException{
		return subDetectors.get(mcaNumber).getInputCountRate();
	}

	public double getOCR(int mcaNumber) throws DeviceException{
		return subDetectors.get(mcaNumber).getOutputCountRate();
	}

}
