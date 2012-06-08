/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.device.filterarray;

import gda.device.DeviceException;
import gda.device.FilterArray;

/**
 * Simulated implementation of the FilterArray interface.
 */
public class DummyFilterArray extends FilterArrayBase implements FilterArray {
	private double currentAbsorption = 0.5;

	private double currentEnergy = 1.0;
	
	@Override
	public void configure(){
		// no configuration required
	}

	@Override
	public double getAbsorption() throws DeviceException {
		return currentAbsorption;
	}

	@Override
	public void setAbsorption(double absorption) throws DeviceException {
		if (absorption < 1.0 && absorption > 0.0) {
			currentAbsorption = absorption;
		}
	}

	@Override
	public double getTransmission() throws DeviceException {
		return 1.0 - currentAbsorption;
	}

	@Override
	public void setTransmission(double transmission) throws DeviceException {
		if (transmission < 1.0 && transmission > 0.0) {
			currentAbsorption = 1.0 - transmission;
		}
	}

	@Override
	public double getCalculationEnergy() throws DeviceException {
		return currentEnergy;
	}

	@Override
	public void setCalculationEnergy(double energy) throws DeviceException {
		currentEnergy = energy;
	}

	@Override
	public double getCalculationWavelength() throws DeviceException {
		return 12.39845 / currentEnergy;
	}

	@Override
	public void setCalculationWavelength(double wavelength) throws DeviceException {
		currentEnergy = 12.39845 / wavelength;
	}

}
