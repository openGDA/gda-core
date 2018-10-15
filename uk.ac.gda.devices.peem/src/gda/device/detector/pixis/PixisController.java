/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package gda.device.detector.pixis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.detector.areadetector.AreaDetectorController;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.TimeoutException;

public class PixisController extends AreaDetectorController {
	// Setup the logging facilities
	transient private static final Logger logger = LoggerFactory.getLogger(PixisController.class);

	
	private String basePVName = null;
	
	// Values internal to the object for Channel Access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController ecl = EpicsController.getInstance();

	// Channels
	private Channel channelTemp;
	private Channel channelTemp_RBV;

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}
	
	// Methods for configurable interface and the reset method
	@Override
	public void configure() throws FactoryException{
		try {
			channelTemp = ecl.createChannel(basePVName + "SetTemperature");
			channelTemp_RBV = ecl.createChannel(basePVName + "MeasuredTemperature_RBV");
			
			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();
			
			super.configure();
			
		} catch (Exception e) {
			throw new FactoryException("Failed to configure the PIXIS camera", e);
		}
	}
	
	@Override
	public String getDescription() throws DeviceException {
		//TODO Parameterise this
		return "Princeton Pixis Camera" ;
	}

	@Override
	public String getDetectorID() throws DeviceException {
		//TODO Parameterise this
		return "Princeton Pixis Camera" ;
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "CCD";
	}


	// PIXIS specific methods
	public void setTemp(double newTemp) throws CAException, InterruptedException{
		ecl.caput(channelTemp, newTemp);
	}

	public double getTemp() throws CAException, TimeoutException, InterruptedException{
		return ecl.cagetDouble(channelTemp_RBV);
	}
	
}
