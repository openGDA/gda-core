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

import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;

import gda.device.detector.areadetector.EPICSAreaDetectorImage;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class EPICSAreaDetectorImageImpl implements EPICSAreaDetectorImage {

	// LocalizableLocalizable
	private boolean local = true;

	// Varlues to be set by spring
	private String basePVName = null;
	private String initialArrayPort = null;
	private String initialArrayAddress = null;

	// Values internal to the object for channel access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();

	// Channels
	private Channel channelEnable;
	private Channel channelArrayPort;
	private Channel channelArrayAddress;
	private Channel channelArrayPort_RBV;
	private Channel channelArrayAddress_RBV;
	private Channel channelArrayData;
	private Channel channelArrayWidth_RBV;
	private Channel channelArrayHeight_RBV;
	private Channel channelTimeStamp_RBV;

	// Methods for the Localizable interface
	@Override
	public boolean isLocal() {
		return local;
	}

	@Override
	public void setLocal(boolean local) {
		this.local = local;
	}

	// getters and setters for spring
	@Override
	public String getBasePVName() {
		return basePVName;
	}

	@Override
	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public String getInitialArrayPort() {
		return initialArrayPort;
	}

	@Override
	public void setInitialArrayPort(String initialArrayPort) {
		this.initialArrayPort = initialArrayPort;
	}

	@Override
	public String getInitialArrayAddress() {
		return initialArrayAddress;
	}

	@Override
	public void setInitialArrayAddress(String initialArrayAddress) {
		this.initialArrayAddress = initialArrayAddress;
	}

	// Methods for the Configurable interface and the reset method
	@Override
	public void configure() throws FactoryException {
		try {
			channelEnable = ecl.createChannel(basePVName + "EnableCallbacks");
			channelArrayPort = ecl.createChannel(basePVName + "NDArrayPort");
			channelArrayAddress = ecl.createChannel(basePVName + "NDArrayAddress");
			channelArrayPort_RBV = ecl.createChannel(basePVName + "NDArrayPort_RBV");
			channelArrayAddress_RBV = ecl.createChannel(basePVName + "NDArrayAddress_RBV");
			channelArrayData = ecl.createChannel(basePVName + "ArrayData", 5.0);
			channelArrayWidth_RBV = ecl.createChannel(basePVName + "ArraySize0_RBV");
			channelArrayHeight_RBV = ecl.createChannel(basePVName + "ArraySize1_RBV");
			channelTimeStamp_RBV = ecl.createChannel(basePVName + "TimeStamp_RBV");
			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();

			// Then populate the channels if necessary
			reset();

		} catch (Exception e) {
			throw new FactoryException("Failure to initialise AreaDetector", e);
		}

	}

	@Override
	public void reset() throws CAException, InterruptedException {
		if (initialArrayAddress != null)
			setArrayAddress(initialArrayAddress);
		if (initialArrayPort != null)
			setArrayPort(initialArrayPort);
	}

	// Methods for manipulating the underlying channels
	@Override
	public void setEnable(boolean enable) throws CAException, InterruptedException {
		if (enable) {
			ecl.caput(channelEnable, "Yes");
		} else {
			ecl.caput(channelEnable, "No");
		}
	}

	@Override
	public void setArrayPort(String arrayPort) throws CAException, InterruptedException {
		ecl.caput(channelArrayPort, arrayPort);
	}

	@Override
	public String getArrayPort() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetString(channelArrayPort_RBV);
	}

	@Override
	public void setArrayAddress(String arrayAddress) throws CAException, InterruptedException {
		ecl.caput(channelArrayAddress, arrayAddress);
	}

	@Override
	public String getArrayAddress() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetString(channelArrayAddress_RBV);
	}

	@Override
	public double getTimeStamp() throws TimeoutException, CAException, InterruptedException {
		return ecl.cagetDouble(channelTimeStamp_RBV);
	}

	/**
	 * Gets the most recent image which has been put to the image plug-in in the Epics environment
	 *
	 * @return a 2D dataset containing the image data
	 * @throws TimeoutException
	 *             if the data cannot be retrieved in time
	 * @throws CAException
	 *             if there are any other CA errors
	 */
	@Override
	public DoubleDataset getImage() throws TimeoutException, CAException, InterruptedException {
		int width = ecl.cagetInt(channelArrayWidth_RBV);
		int height = ecl.cagetInt(channelArrayHeight_RBV);
		double[] data = ecl.cagetDoubleArray(channelArrayData, width * height);
		DoubleDataset dataSet = DatasetFactory.createFromObject(DoubleDataset.class, data, height, width);
		dataSet.setName("PCOImage");
		return dataSet;
	}

}
