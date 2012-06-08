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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.detector.areadetector.AreaDetectorBin;
import gda.device.detector.areadetector.AreaDetectorROI;
import gda.device.detector.areadetector.EpicsAreaDetectorROIElement;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class EpicsAreaDetectorROIElementImpl implements EpicsAreaDetectorROIElement {
	// Setup the logging facilities
	static final Logger logger = LoggerFactory.getLogger(EpicsAreaDetectorROIElementImpl.class);

	// localizable variables
	private Boolean local = true;

	// Variables to be set by Spring
	private String basePVName = null;
	private Integer initialMinX = null;
	private Integer initialMinY = null;
	private Integer initialSizeX = null;
	private Integer initialSizeY = null;
	private Integer initialBinX = null;
	private Integer initialBinY = null;
	private String initialDataType = null;
	private Boolean initialUseROI = null;

	// Values internal to the object for channel access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();

	// Channels
	private Channel channelUse;
	private Channel channelMinX;
	private Channel channelMinY;
	private Channel channelSizeX;
	private Channel channelSizeY;
	private Channel channelMinX_RBV;
	private Channel channelMinY_RBV;
	private Channel channelSizeX_RBV;
	private Channel channelSizeY_RBV;
	private Channel channelDataType;
	private Channel channelBinX;
	private Channel channelBinY;
	private Channel channelBinX_RBV;
	private Channel channelBinY_RBV;

	// Methods for the localizable interface
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

	@Override
	public Boolean getInitialUseROI() {
		return initialUseROI;
	}

	@Override
	public void setInitialUseROI(Boolean initialUseROI) {
		this.initialUseROI = initialUseROI;
	}

	// Methods for the Configurable interface and teh reset method
	@Override
	public void configure() throws FactoryException {

		try {

			configureChannelUse();
			configureChannelMixX();
			configureChannelMinY();
			configureChannelSizeX();
			configureChannelSizeY();
			configureChannelMinX_RBV();
			configureChannelMinY_RBV();
			configureChannelSizeX_RBV();
			configureChannelSizeY_RBV();
			configureChannelBinX();
			configureChannelBinY();
			configureChannelBinX_RBV();
			configureChannelBinY_RBV();
			configureChannelDataType();

			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();

			// Set the initial Parameters
			reset();

		} catch (Exception e) {
			throw new FactoryException("Failure to initialise AreaDetector", e);
		}

	}

	/**
	 * @throws CAException
	 * @throws TimeoutException
	 */
	protected void configureChannelDataType() throws CAException, TimeoutException {
		channelDataType = ecl.createChannel(basePVName + "DataType");
	}

	protected void configureChannelBinY_RBV() throws CAException, TimeoutException {
		channelBinY_RBV = ecl.createChannel(basePVName + "BinY_RBV");
	}

	protected void configureChannelBinX_RBV() throws CAException, TimeoutException {
		channelBinX_RBV = ecl.createChannel(basePVName + "BinX_RBV");
	}

	protected void configureChannelBinY() throws CAException, TimeoutException {
		channelBinY = ecl.createChannel(basePVName + "BinY");
	}

	protected void configureChannelBinX() throws CAException, TimeoutException {
		channelBinX = ecl.createChannel(basePVName + "BinX");
	}

	protected void configureChannelSizeY_RBV() throws CAException, TimeoutException {
		channelSizeY_RBV = ecl.createChannel(basePVName + "SizeY_RBV");
	}

	protected void configureChannelSizeX_RBV() throws CAException, TimeoutException {
		channelSizeX_RBV = ecl.createChannel(basePVName + "SizeX_RBV");
	}

	protected void configureChannelMinY_RBV() throws CAException, TimeoutException {
		channelMinY_RBV = ecl.createChannel(basePVName + "MinY_RBV");
	}

	protected void configureChannelMinX_RBV() throws CAException, TimeoutException {
		channelMinX_RBV = ecl.createChannel(basePVName + "MinX_RBV");
	}

	protected void configureChannelSizeY() throws CAException, TimeoutException {
		channelSizeY = ecl.createChannel(basePVName + "SizeY");
	}

	protected void configureChannelSizeX() throws CAException, TimeoutException {
		channelSizeX = ecl.createChannel(basePVName + "SizeX");
	}

	protected void configureChannelMinY() throws CAException, TimeoutException {
		channelMinY = ecl.createChannel(basePVName + "MinY");
	}

	protected void configureChannelMixX() throws CAException, TimeoutException {
		channelMinX = ecl.createChannel(basePVName + "MinX");
	}

	protected void configureChannelUse() throws CAException, TimeoutException {
		channelUse = ecl.createChannel(basePVName + "Use");
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
		if (initialUseROI != null)
			setUse(initialUseROI);

	}

	// Methods for manipulating the underlying channles
	@Override
	public void setUse(boolean enable) throws CAException, InterruptedException {
		if (enable) {
			ecl.caput(channelUse, "Yes");
		} else {
			ecl.caput(channelUse, "No");
		}
	}

	@Override
	public void setROI(int minx, int miny, int sizex, int sizey) throws CAException, InterruptedException {
		ecl.caput(channelMinX, minx);
		ecl.caput(channelMinY, miny);
		ecl.caput(channelSizeX, sizex);
		ecl.caput(channelSizeY, sizey);
	}

	@Override
	public void setROI(AreaDetectorROI roi) throws CAException, InterruptedException {
		setROI(roi.getMinX(), roi.getMinY(), roi.getSizeX(), roi.getSizeY());
	}

	@Override
	public AreaDetectorROI getROI() throws TimeoutException, CAException, InterruptedException {
		return new AreaDetectorROIImpl(ecl.cagetInt(channelMinX_RBV), ecl.cagetInt(channelMinY_RBV),
				ecl.cagetInt(channelSizeX_RBV), ecl.cagetInt(channelSizeY_RBV));
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
	public void setDataType(String dataType) throws CAException, InterruptedException {
		ecl.caput(channelDataType, dataType);
	}

}
