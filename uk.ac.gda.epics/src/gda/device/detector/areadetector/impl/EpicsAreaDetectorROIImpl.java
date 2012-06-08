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

import gda.device.detector.areadetector.EpicsAreaDetectorROI;
import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gda.factory.FactoryException;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;

public class EpicsAreaDetectorROIImpl implements EpicsAreaDetectorROI{

	// Localizable variables
	private boolean local = true;


	// Variables to be set by Spring
	private String basePVName = null;


	// Values internal to the object for channel access
	private EpicsChannelManager ecm = new EpicsChannelManager();
	private EpicsController     ecl = EpicsController.getInstance();


	// Channels
	private Channel channelEnable;


	// Methods for the Localizable interface
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


	// methods for configurable interface and reset method
	@Override
	public void configure() throws FactoryException {
		try {
			channelEnable = ecl.createChannel(basePVName+"EnableCallbacks");

			// acknowledge that creation phase is completed
			ecm.creationPhaseCompleted();

			reset();

		} catch (Exception e) {
			throw new FactoryException("Failure to initialise AreaDetector",e);
		}

	}

	@Override
	public void reset() throws CAException, InterruptedException {
		setEnable(true);		
	}


	// Methods for manipulating the underlying channels
	@Override
	public void setEnable(boolean enable) throws CAException, InterruptedException {
		if(enable) {
			ecl.caput(channelEnable, "Yes");
		} else {
			ecl.caput(channelEnable, "No");
		}
	}	

}
