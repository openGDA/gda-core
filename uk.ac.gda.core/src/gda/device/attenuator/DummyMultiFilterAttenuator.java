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

package gda.device.attenuator;

import gda.device.DeviceException;
import gda.factory.FactoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyMultiFilterAttenuator extends MultiFilterAttenuator {

	private static final Logger logger = LoggerFactory.getLogger(DummyMultiFilterAttenuator.class);
	
	@Override
	public void configure() throws FactoryException {
		super.configure();
		
		// Use the highest transmission as the initial value
		this.actualTransmission = transmissions.get(transmissions.size() - 1);
	}
	
	@Override
	protected void setFilterStates(Transmission transmission) {
		logger.info("Setting transmission to {}", transmission);
	}
	
	@Override
	public ClosestMatchTransmission getClosestMatchTransmission(double transmission, double energyInKeV) throws DeviceException {
		ClosestMatchTransmission cmt = new ClosestMatchTransmission();
		cmt.energy = energyInKeV;
		cmt.closestAchievableTransmission = getClosestMatchTransmission(transmission);
		return cmt;
	}

}
