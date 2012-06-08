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
import gda.device.detector.analyser.EpicsMCAPresets;
import gda.device.detector.analyser.EpicsMCASimple;
import gda.factory.Finder;

import java.util.ArrayList;

import org.apache.commons.lang.ArrayUtils;

/**
 * CounterTimer that uses a number of EpicsMCAs as channels. The data from each MCA is summed to give a single value per
 * channel.
 */
public class EpicsMCACounterTimer extends gda.device.detector.DetectorBase implements Detector {

	protected ArrayList<String> epicsMcaNameList = new ArrayList<String>();

	private ArrayList<EpicsMCASimple> epicsMcaList = new ArrayList<EpicsMCASimple>();

	@Override
	public void configure() {
		//if an epicsMcaList exists (set directly in Spring), then do not
		//use the name list. If only nameList, find the EpicsMCASimple objects.
		if ((epicsMcaList==null) && (epicsMcaNameList.size()>0)) {
			for (int i = 0; i < epicsMcaNameList.size(); i++) {
				epicsMcaList.add((EpicsMCASimple) Finder.getInstance().find(epicsMcaNameList.get(i)));
			}
		}
	}

	public void countAsync(double time) throws DeviceException {
		for (EpicsMCASimple e : epicsMcaList) {
			e.clearWaitForCompletion();
			EpicsMCAPresets p = (EpicsMCAPresets) e.getPresets();
			p.setPresetRealTime((float) 0.0); // we want live time NOT real
			// time to cater for mca dead
			// time of 2 microseconds/event
			p.setPresetLiveTime((float) time); // from comment in
			// setCollectionTime in
			// gda.device.Detector time is
			// in seconds.
			e.setPresets(p);
			// Set the dwell time to zero to avoid any delays.
			e.setDwellTime(0.0);
			e.startAcquisition();
		}
	}

	@Override
	public void collectData() throws DeviceException {
		countAsync(collectionTime);
	}

	@Override
	public int getStatus() throws DeviceException {
		// This might be right
		int status = Detector.IDLE;

		// Loop over all MCAs in the list.
		for (EpicsMCASimple e : epicsMcaList) {
			if (e.getStatus() == Detector.BUSY) {

				// If any of the elements are busy then return a BUSY
				// immediately.
				status = Detector.BUSY;
			}
			// JythonServerFacade.getInstance().print(
			// "Status of (" + e.getEpicsMcaRecordName() + ") = " + status);
		}
		// If non were found to be busy then they all must be idle!
		return status;
	}

	/**
	 * @return sum of all data
	 * @throws DeviceException
	 */
	private double[] sumOfAllData() throws DeviceException {
		double[] values = new double[epicsMcaList.size()];
		int j = 0;
		for (EpicsMCASimple e : epicsMcaList) {
			int[] data = (int[]) e.readout();

			for (int i = 0; i < data.length; i++) {
				values[j] += data[i];
			}
			j++;
		}
		return values;
	}

	private double[] regionsOfInterestAsFlatArray() throws DeviceException{
		java.util.Vector<double[][]> allData = regionsOfInterestCounts();
		int arraySize = 0;
		for( double[][] regionofInterest : allData){
			int outerLen = regionofInterest.length;
			for( int i =0; i< outerLen; i++){
				arraySize += regionofInterest[i].length;
			}
		}
		double[] data = new double[arraySize];
		int data_index=0;
		for( double[][] regionofInterest : allData){
			int outerLen = regionofInterest.length;
			for( int i =0; i< outerLen; i++){
				int innerLen = regionofInterest[i].length;
				for( int j=0; j< innerLen; j++ ){
					data[data_index]= regionofInterest[i][j];
					data_index++;
				}
			}
		}
		if( data_index != arraySize )
			throw new DeviceException("Error converting regionsOfInterest");
		return data;
		
	}
	/*
	 * return java.util.Vector<double[][]>. Each member of the vector represents a single mca data[n][0] is the total
	 * number of counts in the n'th ROI data[n][1] is the net number of counts in the n'th ROI, i.e. the total counts
	 * less the total background in that ROI.
	 */
	private java.util.Vector<double[][]> regionsOfInterestCounts() throws DeviceException {
		java.util.Vector<double[][]> allData = new java.util.Vector<double[][]>();
		for (EpicsMCASimple e : epicsMcaList) {
			allData.add(e.getRegionsOfInterestCount());
		}
		return allData;
	}

	@Override
	public Object readout() throws DeviceException {
		return (readoutMode == READOUT_MODE.SUM_OF_ALL_DATA) ? sumOfAllData() : 
			(readoutMode == READOUT_MODE.REGIONS_OF_INTEREST_AS_FLAT_ARRAY) ? regionsOfInterestAsFlatArray() :
				regionsOfInterestCounts();
	}

	/**
	 * @param channel
	 * @param epicsMcaName
	 */
	public void setEpicsMcaName(int channel, String epicsMcaName) {
		if (channel >= 0 && channel < epicsMcaNameList.size()){
			this.extraNames = (String[]) ArrayUtils.add(this.extraNames, epicsMcaName);
		}
	}

	protected enum READOUT_MODE {
		/**
		 * Sum all data mode
		 */
		SUM_OF_ALL_DATA,
		/**
		 * Sum rois mode
		 */
		REGIONS_OF_INTEREST,
		/**
		 * Array of rois mode
		 */
		REGIONS_OF_INTEREST_AS_FLAT_ARRAY
	}

	private READOUT_MODE readoutMode = READOUT_MODE.REGIONS_OF_INTEREST;
	
	

	private static final String sumOfAllDatastring = "SUM_OF_ALL_DATA",
			regionsOfInterestString = "REGIONS_OF_INTEREST",
			regionsOfInterestAsFlatArrayString = "REGIONS_OF_INTEREST_AS_FLAT_ARRAY";

	// 0 - return a vector of int. Each member being the summed counts over all
	// channels read from each mca

	/**
	 * @param readoutMode
	 */
	public void setReadOutMode(String readoutMode) {
		if(readoutMode.equals(sumOfAllDatastring))
			this.readoutMode = READOUT_MODE.SUM_OF_ALL_DATA;
		else if(readoutMode.equals(regionsOfInterestAsFlatArrayString))
			this.readoutMode = READOUT_MODE.REGIONS_OF_INTEREST_AS_FLAT_ARRAY;
		else
			this.readoutMode = READOUT_MODE.REGIONS_OF_INTEREST;
	}

	/**
	 * @return readoutMode
	 */
	public String getReadOutMode() {
		if(readoutMode == READOUT_MODE.SUM_OF_ALL_DATA)
			return sumOfAllDatastring;
		else if(readoutMode == READOUT_MODE.REGIONS_OF_INTEREST_AS_FLAT_ARRAY)
			return regionsOfInterestAsFlatArrayString;
	
		return regionsOfInterestString;
	}

	/**
	 * Gets an arraylist of all the names of the EpicsMCAs that are used with the countertimer.
	 * 
	 * @return ArrayList of all the EpicsMCA names.
	 */
	public ArrayList<String> getEpicsMcaNameList() {
		return epicsMcaNameList;
	}

	/**
	 * Sets an arraylist of EpicsMCAs.
	 * 
	 * @param epicsMcaList the list of EpicsMCASimples
	 */
	public void setEpicsMCASimpleList(ArrayList<EpicsMCASimple> epicsMcaList) {
		this.epicsMcaList = epicsMcaList;
	}
	/**
	 * Gets the EpicsMCA name for a given channel.
	 * 
	 * @param channel
	 * @return The EpicsMCA name
	 */
	public String getEpicsMcaName(int channel) {
		String epicsMcaName = null;
		if (channel >= 0 && channel < epicsMcaNameList.size())
			epicsMcaName = epicsMcaNameList.get(channel);

		return epicsMcaName;
	}

	/**
	 * @param epicsMcaName
	 */
	public void addEpicsMcaName(String epicsMcaName) {
		epicsMcaNameList.add(epicsMcaName);
	}
	
	/**
	 * Sets the EPICS MCA names.
	 * 
	 * @param epicsMcaNames the names
	 */
	public void setEpicsMcaNames(ArrayList<String> epicsMcaNames) {
		this.epicsMcaNameList = epicsMcaNames;
	}
	/**
	 * Sets the EPICS MCA names.
	 * 
	 * @param epicsMcaNames the names
	 */
	public void setEpicsMcaNameList(ArrayList<String> epicsMcaNames) {
		this.epicsMcaNameList = epicsMcaNames;
	}


	@Override
	public boolean createsOwnFiles() throws DeviceException {
		// readout() doesn't return a filename.
		return false;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "EPICS Mca based Counter Timer";
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
