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

import gda.device.TemperatureRamp;
import gda.util.PollerEvent;

import java.text.NumberFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default Linkam Stage. (i.e. one with no commands of its own) FIXME what is a stage ?
 */
public class DefaultStage implements LinkamStage {
	
	private static final Logger logger = LoggerFactory.getLogger(DefaultStage.class);
	
	private long startTime = 0;

	private double timeSinceStart = -1000.0;

	private double xValueDifference = 0.0;

	private LinkamCI linkamCI = null;

	private DataFileWriter dataFileWriter = null;

	/**
	 * @param linkamCI
	 */
	public DefaultStage(LinkamCI linkamCI) {
		logger.debug("Creating DefaultStage");

		this.linkamCI = linkamCI;
		dataFileWriter = linkamCI.dataFileWriter;
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
		dataFileWriter.open();
	}

	@Override
	public void stop() {
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

		logger.debug("DefaultStage pollDone called");
		logger.debug("DefaultStage pollDone timeSinceStart is " + timeSinceStart);
		if (timeSinceStart >= 0.0) {
			Date d = new Date();
			timeSinceStart = d.getTime() - startTime;
		}

		dataString = "" + n.format(timeSinceStart / 1000.0) + " " + linkamCI.getTemperature();
		linkamCI.sendNotify(dataString);

		if (dataFileWriter != null) {
			dataString = "" + n.format((timeSinceStart - xValueDifference) / 1000.0) + " " + linkamCI.getTemperature();
			dataFileWriter.write(dataString);
		}
	}

	@Override
	public String getDataFileName() {
		return dataFileWriter.getDataFileName();
	}
}
