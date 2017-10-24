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
import gda.device.SerialReaderWriter;
import gda.device.TemperatureRamp;
import gda.util.PollerEvent;

import java.text.NumberFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Linkam DSC device is used as a temperature controlled sample stage here. It connects to a LinkamT95 which controls
 * it. DSC stands for Differential Scanning Calorimetry.
 */

public class DscStageT95 implements LinkamStage {

	private static final Logger logger = LoggerFactory.getLogger(DscStageT95.class);
	private String debugName = "DscStage";
	private LinkamT95 linkamT95 = null;
	private SerialReaderWriter rw = null;
	private static final double samplingTime = 0.29736;
	private double xValue = -1.0;
	private double xValueDifference = 0.0;
	private DataFileWriter dataFileWriter = null;

	/**
	 * @param linkamT95
	 * @param rw
	 */
	public DscStageT95(LinkamT95 linkamT95, SerialReaderWriter rw) {
		this.linkamT95 = linkamT95;
		this.rw = rw;
		dataFileWriter = linkamT95.dataFileWriter;
	}

	/**
	 * Extracts DCS value from four byte string.
	 * Positive values have range 0 to 7FFF representing 0 to 32764
	 * Values 32765, 32766 and 32767 have special meaning.
	 * Negative values from -32767 to -1 are represented as 8001
	 * (32769) to FFFF (65535).
	 * NB value 32768 is not mentioned in the instructions and so will,
	 * of course, never appear.
	 *
	 * @param string
	 *            the four byte string
	 * @return the DSC value
	 */
	private int extractDscValue(String string) {
		int value = java.lang.Integer.parseInt(string, 16);
		if (value > 32768) {
			value = -((65535 - value) + 1);
		}
		return value;
	}

	@Override
	public void sendRamp(TemperatureRamp ramp) {
		int coolingSpeed = ramp.getCoolingSpeed();

		if (coolingSpeed == 0) {
			linkamT95.setAttribute("LNPumpSpeed", new Integer(coolingSpeed));
			linkamT95.setAttribute("LNPumpAuto", Boolean.TRUE);
		} else {
			linkamT95.setAttribute("LNPumpAuto", Boolean.FALSE);
			linkamT95.setAttribute("LNPumpSpeed", new Integer(coolingSpeed));
		}
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
				reply = rw.sendCommandAndGetReply("D");

				// 10 * temperature is in bytes 0 to 3 as a hexadecimal number.
				// NB note the anti-intuitive specification of
				// String.substring() - this is correct to get bytes 0 to 3.
				temperature = LinkamT95.extractTemperature(reply.substring(0, 4));

				// The DSC data is in bytes 4 to 7 as a hexadecimal number.
				// NB note the anti-intuitive specification of
				// String.substring() - this is correct to get bytes 4 to 7.
				dscValue = extractDscValue(reply.substring(4, 8));

				if (dscValue == 32767)
					break;

				dataString = n.format(xValue) + " " + temperature + " " + dscValue;
				logger.debug("DSC dataString is " + dataString);
				linkamT95.sendNotify(dataString);

				dataString = n.format(xValue - xValueDifference) + " " + temperature + " " + dscValue;

				if (dataFileWriter != null) {
					dataFileWriter.write(dataString);
				}

				if (xValue != -1.0)
					xValue += samplingTime;

				double data[] = new double[3];
				data[0] = temperature;
				data[1] = (xValue - xValueDifference);
				data[2] = dscValue;
				linkamT95.getBufferedData().add(data);

			} catch (DeviceException de) {
				logger.error("Error getting DSC Data", de);
			}
		}
	}

	// Start of methods implementing interface LinkamStage

	/**
	 * Called by controlling LinkamT95 from within its own pollDone.
	 *
	 * @param pe
	 *            the PollerEvent which caused the LinkamT95 pollDone to be called
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		getDscData();
	}

	/**
	 * do nothing at present
	 */
	@Override
	public void sendStartupCommands() {
	}

	@Override
	public void startExperiment() {
		xValueDifference = xValue;
		if (dataFileWriter != null)
			dataFileWriter.open();
	}

	@Override
	public void startRamping() {
		xValue = 0.0;
		getDscData();
	}

	@Override
	public void stop() {
		linkamT95.setAttribute("LNPumpSpeed", new Integer(0));
		linkamT95.setAttribute("LNPumpAuto", Boolean.TRUE);
		if (dataFileWriter != null)
			dataFileWriter.close();
		xValue = -1.0;
		xValueDifference = 0.0;
	}

	@Override
	public String getDataFileName() {
		return (dataFileWriter != null) ? dataFileWriter.getDataFileName() : null;
	}

	// End of methods implementing LinkamStage

}
