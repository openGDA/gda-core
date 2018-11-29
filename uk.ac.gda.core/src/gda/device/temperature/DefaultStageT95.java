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

import java.text.NumberFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.TemperatureRamp;
import gda.util.PollerEvent;

/**
 * Default Linkam Stage. (i.e. one with no commands of its own) FIXME what is a stage ?
 */
public class DefaultStageT95 implements LinkamStage {
	private static final Logger logger = LoggerFactory.getLogger(DefaultStageT95.class);
	private long startTime = 0;
	private double timeSinceStart = -1000.0;
	private double xValueDifference = 0.0;
	private LinkamT95 linkamT95 = null;
	private DataFileWriter dataFileWriter = null;
	private static int index = 1;

	/**
	 * @param linkamT95
	 */
	public DefaultStageT95(LinkamT95 linkamT95) {
		logger.debug("Creating DefaultStageT95");

		this.linkamT95 = linkamT95;
		dataFileWriter = linkamT95.dataFileWriter;
	}

	@Override
	public void sendStartupCommands() {
		// Deliberately does nothing
	}

	@Override
	public void sendRamp(TemperatureRamp ramp) {
		// Deliberately does nothing
	}

	@Override
	public void startRamping() {
		Date d = new Date();
		startTime = d.getTime();
		timeSinceStart = 0.0;
	}

	@Override
	public void startExperiment() {
		xValueDifference = timeSinceStart;
		if (dataFileWriter != null)
			dataFileWriter.open();
	}

	@Override
	public void stop() {
		if (dataFileWriter != null)
			dataFileWriter.close();
		timeSinceStart = -1000.0;
		xValueDifference = 0.0;
	}

	/**
	 * Executes when poll timer fires
	 *
	 * @param pe
	 *            the polling event
	 */
	@Override
	public void pollDone(PollerEvent pe) {
		String dataString;

		NumberFormat n = NumberFormat.getInstance();
		n.setMaximumFractionDigits(2);
		n.setGroupingUsed(false);

		logger.debug("DefaultStageT95 pollDone called");
		logger.debug("DefaultStageT95 pollDone timeSinceStart is {}", timeSinceStart);
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		double currentTemp = linkamT95.getTemperature();
		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + currentTemp;
		linkamT95.sendNotify(dataString);

		if (dataFileWriter != null) {
			dataString = "" + n.format((timeSinceStart - xValueDifference) / 1000.0) + " " + currentTemp;
			dataFileWriter.write(dataString);
		}

		double data[] = new double[2];
		data[0] = currentTemp;
		data[1] = (timeSinceStart - xValueDifference) / 1000.0;
		if (data[1] >= 0.0) {
			logger.debug("current temp {}, time since start {}", data[0], data[1]);
			linkamT95.getBufferedData().add(data);
			logger.debug("index {}, array size {}", index, linkamT95.getBufferedData().size());
			index++;
		}
	}

	@Override
	public String getDataFileName() {
		return (dataFileWriter != null) ? dataFileWriter.getDataFileName() : null;
	}
}
