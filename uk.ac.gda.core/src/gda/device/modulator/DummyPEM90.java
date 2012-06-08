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

package gda.device.modulator;

import gda.device.DeviceBase;
import gda.device.DeviceException;
import gda.device.Modulator;
import gda.factory.Findable;

import java.text.DecimalFormat;

import org.jscience.physics.quantities.Frequency;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;

/**
 * Class to control the Photoelastic Modulator PEM-90
 */

public class DummyPEM90 extends DeviceBase implements Modulator, Findable {
	private static final double MAXWAVELENGTH = 19999.9;

	private static final double MINWAVELENGTH = 00000.0;

	private static final double MAXRETARDATION = 1.0;

	private static final double MINRETARDATION = 0.0;

	// private static final int ECHO = 0;
	// private static final int NOECHO = 1;
	// private final int ECHOREAD = 3;
	// private final int NOECHOREAD = 2;
	// private static final int NORMALOPER = 0;
	// private static final int INHIBITEDOPER = 1;
	// private static final int WORDLENGTH = 6;
	// private boolean echo = true;
	// private int noOfRead = 3;
	private double currentWaveLength = 0.0;

	private double currentFrequency = 0.0;

	private int currentRetardation = 0;

	/**
	 * Constructor.
	 */
	public DummyPEM90() {
	}
	
	@Override
	public void configure(){
		// no configuration required
	}

	/**
	 * gets the current wavelength from the device
	 * 
	 * @return current Wavelength in nanometers
	 */
	@Override
	public Length getWaveLength() {
		return Quantity.valueOf(currentWaveLength, SI.NANO(SI.METER));
	}

	/**
	 * Sets the wavelenth between(00000.0 - 19999.9)
	 * 
	 * @param wavelen
	 *            the wavelength to be set
	 * @throws DeviceException
	 */
	@Override
	public void setWaveLength(double wavelen) throws DeviceException {
		if (wavelen < MINWAVELENGTH || wavelen > MAXWAVELENGTH)
			throw new DeviceException("Error in setting the wavelength");

		currentWaveLength = wavelen;
		notifyIObservers(this, "");

	}

	/**
	 * gets the current retardation set in the device
	 * 
	 * @return retardation
	 */
	@Override
	public int getRetardation() {
		// unit of retardation wave units
		// no idea on its representation

		return currentRetardation;
	}

	/**
	 * sets the retardation between (0000 to 1000)
	 * 
	 * @param retard
	 *            amount of retardation
	 * @throws DeviceException
	 */

	@Override
	public void setRetardation(double retard) throws DeviceException {
		if (retard < MINRETARDATION || retard > MAXRETARDATION) {
			throw new DeviceException("Invalid Value for Retardation");
		}

		// conversion of numbers from (eg)0.98 to (eg) 0098
		DecimalFormat retDf = new DecimalFormat();
		retDf.applyPattern("0000");
		String correctRetard = retDf.format((retard * 1000));
		currentRetardation = Integer.parseInt(correctRetard);
		notifyIObservers(this, "");

	}

	/**
	 * resets the instrument to factory default
	 */
	@Override
	public void reset() {

	}

	/**
	 * Controls the ON/OFF of echo
	 * 
	 * @param echocommand
	 *            boolean to switch echo
	 */
	@Override
	public void setEcho(boolean echocommand) {

	}

	/**
	 * reads the reference frequency
	 * 
	 * @param numberOfTimes
	 *            can be one /two
	 * @return Frequency in KiloHertz
	 */
	@Override
	public Frequency readFrequency(int numberOfTimes) {
		return Quantity.valueOf(currentFrequency, SI.KILO(SI.HERTZ));
	}

	/**
	 * sets the retardation to INHIBIT/NORMAL mode
	 * 
	 * @param inhibitRetardation
	 *            boolean
	 */
	@Override
	public void setInhibit(boolean inhibitRetardation) {

	}

	@Override
	public Object getAttribute(String attributeName) {
		if ("status".equalsIgnoreCase(attributeName)) {
			double d = getWaveLength().to(SI.NANO(SI.METER)).getAmount();
			return String.valueOf(d) + " nm";
		}
		return null;
	}
}
