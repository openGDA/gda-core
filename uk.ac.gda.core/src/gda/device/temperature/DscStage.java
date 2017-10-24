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

package gda.device.temperature;

import gda.device.DeviceException;
import gda.device.TemperatureRamp;
import gda.util.PollerEvent;

import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Linkam DSC device is used as a temperature controlled sample stage here. It connects to a LinkamCI which controls
 * it. DSC stands for Differential Scanning Calorimetry originally.
 */

public class DscStage implements LinkamStage {

	private static final Logger logger = LoggerFactory.getLogger(DscStage.class);

	private String debugName = "DscStage";

	private LinkamCI linkamCI = null;

	private AsynchronousReaderWriter arw = null;

	private double samplingTime = 0.9;

	private double xValue = -1.0;

	private double xValueDifference = 0.0;

	private DataFileWriter dataFileWriter = null;

	private final String SAMPLINGTIMECOMMAND = "\u00e7";

	/**
	 * @param linkamCI
	 * @param arw
	 * @param samplingTime
	 */
	public DscStage(LinkamCI linkamCI, AsynchronousReaderWriter arw, double samplingTime) {
		this.linkamCI = linkamCI;
		this.arw = arw;
		this.samplingTime = samplingTime;

		dataFileWriter = linkamCI.dataFileWriter;
	}

	/**
	 * Extracts DCS value from four byte string.
	 *
	 * @param string
	 *            the four byte string
	 * @return the DSC value
	 */
	private int extractDscValue(String string) {
		// FIXME migrate at least a summary to Javadoc
		// Positive values have range 0 to 7FFF representing 0 to 32764
		// Values 32765, 32766 and 32767 have special meaning.
		// Negative values from -32767 to -1 are represented as 8001
		// (32769) to FFFF (65535).
		// NB value 32768 is not mentioned in the instructions and so will,
		// of course, never appear.
		int value = java.lang.Integer.parseInt(string, 16);
		if (value > 32768) {
			value = -((65535 - value) + 1);
		}
		return value;
	}

	/**
	 * Set the sampling time
	 *
	 * @param samplingTime
	 *            the time in seconds???
	 */
	public void setSamplingTime(double samplingTime) {
		this.samplingTime = samplingTime;
		sendSamplingTime();
	}

	@Override
	public void sendRamp(TemperatureRamp ramp) {
		int coolingSpeed = ramp.getCoolingSpeed();

		if (coolingSpeed == 0) {
			linkamCI.setAttribute("LNPumpSpeed", new Integer(coolingSpeed));
			linkamCI.setAttribute("LNPumpAuto", Boolean.TRUE);
		} else {
			linkamCI.setAttribute("LNPumpAuto", Boolean.FALSE);
			linkamCI.setAttribute("LNPumpSpeed", new Integer(coolingSpeed));
		}
	}

	/**
	 * Sends a new DSC sampling time
	 */
	private void sendSamplingTime() {
		logger.debug("Linkam sendDscSamplingTime() called: " + samplingTime);

		int intToSend = (int) (samplingTime / 0.05);

		// FIXME
		// for the current kludged version we always send 0.9s
		intToSend = 18;
		String commandData = String.valueOf(intToSend);

		while (commandData.length() < 4) {
			commandData = " " + commandData;
		}
		arw.handleCommand(SAMPLINGTIMECOMMAND + commandData);
	}

	private void getDscData() {
		String reply = null;
		String dataString = null;
		double temperature = -273;
		int dscValue = 0;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		// Empty DSC buffer is specified by returning 32767, need to
		// continue reading till we get that.
		while (true) {
			try {
				reply = arw.sendCommandAndGetReply("D");

				// FIXME summary in Javadoc ?
				// 10 * temperature is in bytes 0 to 3 as a hexadecimal number.
				// NB note the anti-intuitive specification of
				// String.substring() - this is correct to get bytes 0 to 3.
				temperature = LinkamCI.extractTemperature(reply.substring(0, 4));

				// The DSC data is in bytes 4 to 7 as a hexadecimal number.
				// NB note the anti-intuitive specification of
				// String.substring() - this is correct to get bytes 4 to 7.
				dscValue = extractDscValue(reply.substring(4, 8));

				if (dscValue == 32767)
					break;

				dataString = n.format(xValue) + " " + temperature + " " + dscValue;
				logger.debug("DSC dataString is " + dataString);
				linkamCI.sendNotify(dataString);

				dataString = n.format(xValue - xValueDifference) + " " + temperature + " " + dscValue;

				if (dataFileWriter != null) {
					dataFileWriter.write(dataString);
				}

				if (xValue != -1.0)
					xValue += samplingTime;

			} catch (DeviceException de) {
				logger.error("Error getting DSC data", de);
			}
		}
	}

	// Start of methods implementing interface LinkamStage

	/**
	 * Called by controlling LinkamCI from within its own pollDone.
	 *
	 * @param pe
	 *            the PollerEvent which caused the LinkamCI pollDone to be called
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		getDscData();
	}

	/**
	 * Sends sampling time only at present
	 */
	@Override
	public void sendStartupCommands() {
		sendSamplingTime();
	}

	@Override
	public void startExperiment() {
		xValueDifference = xValue;
		dataFileWriter.open();
	}

	@Override
	public void startRamping() {
		getDscData();
		xValue = 0.0;
	}

	@Override
	public void stop() {
		linkamCI.setAttribute("LNPumpSpeed", new Integer(0));
		linkamCI.setAttribute("LNPumpAuto", Boolean.TRUE);
		dataFileWriter.close();
		xValue = -1.0;
		xValueDifference = 0.0;
	}

	@Override
	public String getDataFileName() {
		return dataFileWriter.getDataFileName();
	}

	// End of methods implementing LinkamStage

}
