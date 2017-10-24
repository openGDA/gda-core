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
import gda.device.Serial;
import gda.factory.Finder;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jscience.physics.quantities.Frequency;
import org.jscience.physics.quantities.Length;
import org.jscience.physics.quantities.Quantity;
import org.jscience.physics.units.SI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to control the Photoelastic Modulator PEM-90
 */

public class PEM90 extends DeviceBase implements Modulator {

	private static final Logger logger = LoggerFactory.getLogger(PEM90.class);

	private static final double MAXWAVELENGTH = 19999.9;

	private static final double MINWAVELENGTH = 00000.0;

	private static final double MAXRETARDATION = 1.0;

	private static final double MINRETARDATION = 0.0;

	private static final int ECHO = 0;

	private static final int NOECHO = 1;

	private final int ECHOREAD = 3;

	private final int NOECHOREAD = 2;

	private static final int NORMALOPER = 0;

	private static final int INHIBITEDOPER = 1;

	private static final int WORDLENGTH = 6;

	private boolean echo = true;

	private String serialDeviceName;

	private Serial serial;

	// RS232 communications protocol defaults;
	private final static int READ_TIMEOUT = 200;

	private String parity = Serial.PARITY_NONE;

	private int baudRate = Serial.BAUDRATE_9600;

	private int stopBits = Serial.STOPBITS_1;

	private int byteSize = Serial.BYTESIZE_8;

	/**
	 * Constructor.
	 */
	public PEM90() {
	}

	@Override
	public void configure() {
		logger.debug("Finding: " + serialDeviceName);
		if ((serial = (Serial) Finder.getInstance().find(serialDeviceName)) == null) {
			logger.error("Serial Device " + serialDeviceName + " not found");
		} else {
			try {
				serial.setBaudRate(baudRate);
				serial.setStopBits(stopBits);
				serial.setByteSize(byteSize);
				serial.setParity(parity);
				serial.setReadTimeout(READ_TIMEOUT);
				serial.flush();
				setEcho(false);

			} catch (DeviceException de) {
				logger.debug("exception occured in creating the RS232");
			}
		}
	}

	/**
	 * @param serialDeviceName
	 */
	public void setSerialDeviceName(String serialDeviceName) {
		this.serialDeviceName = serialDeviceName;
	}

	/**
	 * @return serial device name
	 */
	public String getSerialDeviceName() {
		return serialDeviceName;
	}

	/**
	 * gets the current wavelength from the serial
	 *
	 * @return current Wavelength in nanometers
	 */
	@Override
	public Length getWaveLength() {
		double wavelength = 0.0;
		try {
			String wavestr = ioPEM90Query("W");

			// modulator returns o/p of the form (eg)009876
			// for the value 987.6 ,the following
			// if statement is to introduce
			// a decimal in the value returned

			if (wavestr.length() == WORDLENGTH) {
				wavestr = wavestr.substring(0, WORDLENGTH - 1) + "." + wavestr.substring(WORDLENGTH - 1, WORDLENGTH);
			}
			wavelength = Double.valueOf(wavestr).doubleValue();
			logger.debug("The wavelength read is " + wavelength);
		} catch (DeviceException de) {
			logger.debug("Error reading the wavelength" + de);
		} catch (NumberFormatException e) {
			logger.debug("Error reading wavLength Please try later");
		} catch (Exception e) {
			logger.debug("Error reading wavLength" + e);
		}
		return Quantity.valueOf(wavelength, SI.NANO(SI.METER));
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
		String correctWave = "";
		String adjustWave2 = "";

		if (wavelen < MINWAVELENGTH || wavelen > MAXWAVELENGTH)
			throw new DeviceException("Invalid  value for WaveLength");

		// modulator i/p for a value (eg)987.6 should be 009876
		// the following code does the conversion
		NumberFormat format = NumberFormat.getInstance();
		DecimalFormat df = new DecimalFormat();
		df.applyPattern("000000");
		format.setMaximumFractionDigits(1);
		String adjustWave1 = format.format(wavelen);
		int len = adjustWave1.length();
		int ind = adjustWave1.indexOf(".");
		if (ind == -1) {
			correctWave = adjustWave1;
			adjustWave2 = "0";
		} else {
			correctWave = adjustWave1.substring(0, ind);
			adjustWave2 = adjustWave1.substring(ind + 1, len);
			logger.debug("The" + correctWave + "is" + adjustWave2);
		}

		correctWave = correctWave + adjustWave2;
		correctWave = df.format(Double.parseDouble(correctWave));
		correctWave.trim();

		ioPEM90("W:" + correctWave);
		String check = ioPEM90Query("W");
		if (!check.equals(correctWave))
			throw new DeviceException("Error in setting the wavelength");
		notifyIObservers(this, check);

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

		int retardation = 0;
		try {
			retardation = Integer.parseInt(ioPEM90Query("R"));
			logger.debug("the retardation is " + retardation);
		} catch (DeviceException de) {
			logger.debug("Error reading Retardation " + de);
		} catch (NumberFormatException e) {
			logger.debug("Error reading Retardation" + " Please try later");
		} catch (Exception e) {
			logger.debug("Error reading Retardation" + e);
		}
		return retardation;
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
		logger.debug("The correct retardation value is" + correctRetard);

		ioPEM90("R:" + correctRetard);
		String check = ioPEM90Query("R");
		if (!check.equals(correctRetard)) {
			throw new DeviceException("Error in setting the Retardation");
		}
		notifyIObservers(this, check);
	}

	/**
	 * resets the instrument to factory default
	 */
	@Override
	public void reset() {
		try {
			ioPEM90("Z");
		} catch (DeviceException de) {
			logger.debug("Error resetting", de);
		}
	}

	/**
	 * Controls the ON/OFF of echo
	 *
	 * @param echocommand
	 *            boolean to switch echo
	 */
	@Override
	public void setEcho(boolean echocommand) {
		echo = echocommand;
		int echoNum;
		echoNum = (echocommand) ? ECHO : NOECHO;

		try {
			ioPEM90("E:" + echoNum);
		} catch (DeviceException de) {
			logger.debug("Error setting echo to {}", echocommand, de);
		}
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
		String freqread = "";
		double frequency = 0.0;

		// frequency can be read as F or 2F

		freqread = (numberOfTimes == 2) ? "2F" : "F";

		try {
			// setEcho(false);
			String freqstr = ioPEM90Query(freqread);

			// conversion of numbers from (eg) 050776 to (eg)50.776
			if (freqstr.length() == WORDLENGTH) {
				freqstr = freqstr.substring(0, WORDLENGTH - 3) + "." + freqstr.substring(WORDLENGTH - 3, WORDLENGTH);
			}

			frequency = Double.valueOf(freqstr).doubleValue();
			logger.debug("The frequency read is " + frequency);
		} catch (DeviceException de) {
			logger.debug("Error reading frequency" + de);
		} catch (NumberFormatException e) {
			logger.debug("Error reading Frequency Please try later");
		} catch (Exception e) {
			logger.debug("Error reading frequency" + e);
		}
		return Quantity.valueOf(frequency, SI.KILO(SI.HERTZ));
	}

	/**
	 * sets the retardation to INHIBIT/NORMAL mode
	 *
	 * @param inhibitRetardation
	 *            boolean
	 */
	@Override
	public void setInhibit(boolean inhibitRetardation) {
		int inhibit;

		inhibit = (inhibitRetardation) ? INHIBITEDOPER : NORMALOPER;

		try {
			ioPEM90("I:" + inhibit);
		} catch (DeviceException de) {
			logger.debug("Error setting inhibit to {}", inhibitRetardation, de);
		}
	}

	@Override
	public Object getAttribute(String attributeName) {
		// temporary arrangement so that the pem position
		// with units can be displayed on the GUI.
		if ("status".equalsIgnoreCase(attributeName)) {
			double d = getWaveLength().to(SI.NANO(SI.METER)).getAmount();
			return String.valueOf(d) + " nm";
		}
		return null;
	}

	private void ioPEM90(String s) throws DeviceException {

		String message = s + '\r';
		logger.debug("Sent to PEM90" + s);
		write(message);
		// requires a minimum sleep of 500 ms for correct operation
		synchronized (this) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException ie) {
				logger.debug("thread interrupted" + ie);
			}
		}
		serial.flush();
	}

	private void write(String message) throws DeviceException {
		for (int i = 0; i < message.length(); i++) {
			serial.writeChar(message.charAt(i));
		}
	}

	private String ioPEM90Query(String s) throws DeviceException {
		String msg = s + '\r';
		logger.debug("Sent to PEM90" + s);

		write(msg);
		char ch;
		String result = "";

		int noOfRead = (echo) ? ECHOREAD : NOECHOREAD;

		for (int i = 0; i < noOfRead; i++) {
			StringBuffer reply = new StringBuffer("");
			while (true) {
				ch = serial.readChar();
				logger.debug("the char read is " + (int) ch);
				if (ch >= '0' && ch <= '9')
					reply.append(ch);
				if (ch == '\r' || ch == '\0')
					break;
			}

			result = reply.toString();
			logger.debug("The value read at read no " + (i + 1) + "is " + reply);
		}
		serial.flush();
		logger.debug("the final value read is" + result);
		return (result);
	}
}
