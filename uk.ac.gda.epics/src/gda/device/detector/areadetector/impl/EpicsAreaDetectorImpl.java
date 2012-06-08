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

package gda.device.detector.areadetector.impl;

import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.EpicsAreaDetector;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class EpicsAreaDetectorImpl implements EpicsAreaDetector {

	// Localizable Variables
	private Boolean local = true;


	// values to be set by Spring
	private String basePVName = null;
	private Integer initialMinX = null;
	private Integer initialMinY = null;
	private Integer initialSizeX = null;
	private Integer initialSizeY = null;
	private Integer initialBinX = null;
	private Integer initialBinY = null;
	private String initialDataType = null;


	// Values internal to the object for Channel Access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();


	// Channels
	private Channel channelPortName_RBV;
	private Channel channelDataType;
	private Channel channelMinX;
	private Channel channelMinY;
	private Channel channelMinX_RBV;
	private Channel channelMinY_RBV;
	private Channel channelSizeX;
	private Channel channelSizeY;
	private Channel channelSizeX_RBV;
	private Channel channelSizeY_RBV;
	private Channel channelAcquireTime;
	private Channel channelAcquireTime_RBV;
	private Channel channelAcquirePeriod;
	private Channel channelAcquirePeriod_RBV;
	private Channel channelAcquire;
	private Channel channelBinX;
	private Channel channelBinY;
	private Channel channelBinX_RBV;
	private Channel channelBinY_RBV;
	private Channel channelDetectorState_RBV;
	private Channel channelImageMode;
	private Channel channelArrayCounter;
	private Channel channelArrayCounter_RBV;
	private Channel channelNumExposures;
	private Channel channelNumExposures_RBV;
	private Channel channelNumImages;
	private Channel channelNumImages_RBV;
	private Channel channelTriggerMode;


	// Methods for Localizable interface
	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	// Getters and setters for Spring
	@Override
	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public Integer getInitialMinX() {
		return initialMinX;
	}

	@Override
	public void setInitialMinX(Integer initialMinX) {
		this.initialMinX = initialMinX;
	}

	@Override
	public Integer getInitialMinY() {
		return initialMinY;
	}

	@Override
	public void setInitialMinY(Integer initialMinY) {
		this.initialMinY = initialMinY;
	}

	@Override
	public Integer getInitialSizeX() {
		return initialSizeX;
	}

	@Override
	public void setInitialSizeX(Integer initialSizeX) {
		this.initialSizeX = initialSizeX;
	}

	@Override
	public Integer getInitialSizeY() {
		return initialSizeY;
	}

	@Override
	public void setInitialSizeY(Integer initialSizeY) {
		this.initialSizeY = initialSizeY;
	}

	@Override
	public Integer getInitialBinX() {
		return initialBinX;
	}

	@Override
	public void setInitialBinX(Integer initialBinX) {
		this.initialBinX = initialBinX;
	}

	@Override
	public Integer getInitialBinY() {
		return initialBinY;
	}

	@Override
	public void setInitialBinY(Integer initialBinY) {
		this.initialBinY = initialBinY;
	}

	@Override
	public String getInitialDataType() {
		return initialDataType;
	}

	@Override
	public void setInitialDataType(String initialDataType) {
		this.initialDataType = initialDataType;
	}


	// Methods for the configurable interface, and reset method
	@Override
	public void configure() throws FactoryException {
		try {
			// Set up all the CA channels
			channelPortName_RBV = ecl.createChannel(basePVName + "PortName_RBV");
			channelDataType = ecl.createChannel(basePVName + "DataType");
			channelMinX = ecl.createChannel(basePVName + "MinX");
			channelMinY = ecl.createChannel(basePVName + "MinY");
			channelSizeX = ecl.createChannel(basePVName + "SizeX");
			channelSizeY = ecl.createChannel(basePVName + "SizeY");
			channelMinX_RBV = ecl.createChannel(basePVName + "MinX_RBV");
			channelMinY_RBV = ecl.createChannel(basePVName + "MinY_RBV");
			channelSizeX_RBV = ecl.createChannel(basePVName + "SizeX_RBV");
			channelSizeY_RBV = ecl.createChannel(basePVName + "SizeY_RBV");
			channelAcquireTime = ecl.createChannel(basePVName + "AcquireTime");
			channelAcquireTime_RBV = ecl.createChannel(basePVName + "AcquireTime_RBV");
			channelAcquirePeriod = ecl.createChannel(basePVName + "AcquirePeriod");
			channelAcquirePeriod_RBV = ecl.createChannel(basePVName + "AcquirePeriod_RBV");
			channelAcquire = ecl.createChannel(basePVName + "Acquire");
			channelBinX = ecl.createChannel(basePVName + "BinX");
			channelBinY = ecl.createChannel(basePVName + "BinY");
			channelBinX_RBV = ecl.createChannel(basePVName + "BinX_RBV");
			channelBinY_RBV = ecl.createChannel(basePVName + "BinY_RBV");
			channelDetectorState_RBV = ecl.createChannel(basePVName + "DetectorState_RBV");
			channelImageMode = ecl.createChannel(basePVName + "ImageMode");
			channelArrayCounter = ecl.createChannel(basePVName + "ArrayCounter");
			channelArrayCounter_RBV = ecl.createChannel(basePVName + "ArrayCounter_RBV");
			channelNumExposures = ecl.createChannel(basePVName + "NumExposures");
			channelNumExposures_RBV = ecl.createChannel(basePVName + "NumExposures_RBV");
			channelNumImages = ecl.createChannel(basePVName + "NumImages");
			channelNumImages_RBV = ecl.createChannel(basePVName + "NumImages_RBV");
			channelTriggerMode = ecl.createChannel(basePVName + "TriggerMode");

			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();

			// Set the initial Parameters
			reset();

		} catch (Exception e) {
			throw new FactoryException("Failure to initialise AreaDetector", e);
		}
	}

	@Override
	public void reset() throws CAException, InterruptedException {
		if (initialDataType != null)
			setDataType(initialDataType);
		if ((initialMinX != null) && (initialMinY != null) && (initialSizeX != null) && (initialSizeY != null)) {
			setROI(initialMinX, initialMinY, initialSizeX, initialSizeY);
		}
		if ((initialBinX != null) && (initialBinY != null)) {
			setBinning(initialBinX, initialBinY);
		}
	}


	// Methods for manipulating the underlying channels
	@Override
	public void setROI(int minx, int miny, int sizex, int sizey) throws CAException, InterruptedException {
		ecl.caput(channelMinX, minx);
		ecl.caput(channelMinY, miny);
		ecl.caput(channelSizeX, sizex);
		ecl.caput(channelSizeY, sizey);
	}

	@Override
	public AreaDetectorROI getROI() throws TimeoutException, CAException, InterruptedException {
		return new AreaDetectorROIImpl(ecl.cagetInt(channelMinX_RBV), ecl.cagetInt(channelMinY_RBV), ecl
				.cagetInt(channelSizeX_RBV), ecl.cagetInt(channelSizeY_RBV));
	}

	@Override
	public void setBinning(int binx, int biny) throws CAException, InterruptedException {
		ecl.caput(channelBinX, binx);
		ecl.caput(channelBinY, biny);
	}

	@Override
	public AreaDetectorBin getBinning() throws TimeoutException, CAException, InterruptedException {
		return new AreaDetectorBinImpl(ecl.cagetInt(channelBinX_RBV), ecl.cagetInt(channelBinY_RBV));
	}

	@Override
	public void setExpTime(double expTime) throws CAException, InterruptedException {
		ecl.caput(channelAcquireTime, expTime);
	}

	@Override
	public double getExpTime() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetDouble(channelAcquireTime_RBV);
	}
	
	@Override
	public void setAcquirePeriod(double acquirePeriod) throws CAException, InterruptedException {
		ecl.caput(channelAcquirePeriod, acquirePeriod);
	}

	@Override
	public double getAquirePeriod() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetDouble(channelAcquirePeriod_RBV);
	}	

	@Override
	public String getPortName() throws TimeoutException, CAException, InterruptedException {
		return ecl.caget(channelPortName_RBV);
	}

	@Override
	public void acquire() throws CAException, InterruptedException {
		ecl.caput(channelAcquire, 1);
	}
	
	@Override
	public void stop() throws CAException, InterruptedException {
		ecl.caput(channelAcquire, 0);
	}

	@Override
	public String getState() throws TimeoutException, CAException, InterruptedException {
		return ecl.caget(channelDetectorState_RBV);
	}

	@Override
	public void setImageMode(int imageMode) throws CAException, InterruptedException {
		ecl.caput(channelImageMode, imageMode);
	}

	@Override
	public void setArrayCounter(int imageNumber) throws CAException, InterruptedException {
		ecl.caput(channelArrayCounter, imageNumber);
	}

	@Override
	public int getArrayCounter() throws NumberFormatException, TimeoutException, CAException, InterruptedException {
		return Integer.parseInt(ecl.caget(channelArrayCounter_RBV));
	}
	
	@Override
	public void setNumExposures(int NumberExposures) throws CAException, InterruptedException {
		ecl.caput(channelNumExposures, NumberExposures);
	}

	@Override
	public int getNumExposures() throws NumberFormatException, TimeoutException, CAException, InterruptedException {
		return Integer.parseInt(ecl.caget(channelNumExposures_RBV));
	}
	
	@Override
	public void setNumImages(int NumberImages) throws CAException, InterruptedException {
		ecl.caput(channelNumImages, NumberImages);
	}

	@Override
	public int getNumImages() throws NumberFormatException, TimeoutException, CAException, InterruptedException {
		return Integer.parseInt(ecl.caget(channelNumImages_RBV));
	}

	@Override
	public void setDataType(String dataType) throws CAException, InterruptedException {
		ecl.caput(channelDataType, dataType);
	}

	@Override
	public int getAquireState() throws NumberFormatException, TimeoutException, CAException, InterruptedException {
		return Integer.parseInt(ecl.caget(channelAcquire));
		
	}

	@Override
	public void setTriggerMode(int triggerMode) throws CAException, InterruptedException {
		ecl.caput(channelTriggerMode, triggerMode);
	}
}
