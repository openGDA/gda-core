/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.scannable.DummyScannable;
import uk.ac.gda.api.remoting.ServiceInterface;

/** B07-1 Choose between CCMC and PGM to return photon energy to SPECS analyser.
 *  CCM positions is a gda.device.enumpositioner.EpicsPositionerCallback, while pgm_energy is
 *  gda.device.scannable.TweakableScannableMotor.
 *
 *  As agreed with the B07-1 beamline staff:
 *  1. CCM position name must be "Xtal_" followed by an actual photon energy except for "Out of Beam" position.
 *  2. When CCM is in "out of beam" -> get energy from PGM, otherwise parse CCM position name.
 * */
@ServiceInterface(Scannable.class)
public class EndstationPhotonEnergyProvider extends DummyScannable{
	private static final Logger logger = LoggerFactory.getLogger(EndstationPhotonEnergyProvider.class);
	private Scannable pgmEnergy;
	private Scannable ccmPositions;
	private double energy;
	private double ccmLowerLimit;
	private double ccmUpperLimit;
	private String ccmPositionPrefix;

	public String getCcmPositionPrefix() {
		return ccmPositionPrefix;
	}
	public void setCcmPositionPrefix(String ccmPositionPrefix) {
		this.ccmPositionPrefix = ccmPositionPrefix;
	}
	public double getCcmLowerLimit() {
		return ccmLowerLimit;
	}
	public void setCcmLowerLimit(double ccmLowerLimit) {
		this.ccmLowerLimit = ccmLowerLimit;
	}
	public double getCcmUpperLimit() {
		return ccmUpperLimit;
	}
	public void setCcmUpperLimit(double ccmUpperLimit) {
		this.ccmUpperLimit = ccmUpperLimit;
	}

	public Scannable getPgmEnergy() {
		return pgmEnergy;
	}
	public void setPgmEnergy(Scannable pgmEnergy) {
		this.pgmEnergy = pgmEnergy;
	}
	public Scannable getCcmcPositions() {
		return ccmPositions;
	}
	public void setCcmcPositions(Scannable ccmcPositions) {
		this.ccmPositions = ccmcPositions;
	}

	@Override
	public Object getPosition() throws DeviceException {
		try {
			String readback = (String) ccmPositions.getPosition();
			logger.info("Got actual CCM position {}",readback);

			/* if position does not start with prefix (including Out of Beam) return PGM energy */
			if (!(readback.startsWith(ccmPositionPrefix))){
				logger.debug("Failed to get photon energy from CCM - name does not start with Xtal_ : taking energy from PGM");
				return (double) pgmEnergy.getPosition();
			}

			/* otherwise first split based on prefix, extract double and check if it is in a range */
			try {
				energy = Double.parseDouble(readback.split(ccmPositionPrefix)[1].replaceAll("[^0-9?!\\.]",""));
				logger.debug("Extracted photon energy from CCM position: {}", energy);
				if ((energy>ccmUpperLimit) || (energy<ccmLowerLimit)) {
					logger.error("Extracted photon energy from CCMC position: {} is not in the allowed range (1500,3000)", energy);
					logger.debug("Failed to get photon energy from CCM - taking energy from PGM");
					energy = (double) pgmEnergy.getPosition();
				}
			} catch (Exception e) {
				logger.debug("Failed to parse photon energy from CCM callback - taking energy from PGM");
				energy = (double) pgmEnergy.getPosition();
			}
		} catch (DeviceException e) {
			logger.error("Failed to get photon energy from both CCM and PGM");
		}
		return energy;
	}
}
