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

package gda.device.dataLogger;

import gda.device.DataLogger;
import gda.device.DeviceException;
import gda.factory.Finder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.amplifier.TestObjectAssistant;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class HPDataLoggerTest {
	private static final Logger logger = LoggerFactory.getLogger(HPDataLoggerTest.class);

	/**
	 * Test Main Method.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		DataLogger dataLogger;
		TestObjectAssistant toa = new TestObjectAssistant();
		toa.createLocalObjects();
		dataLogger = (DataLogger) (Finder.getInstance().find("MiDataLogger"));
		if (dataLogger == null)
			logger.error("Could not the find the dataLogger");
		else {
			try {
				/*
				 * dataLogger.intialise("COM1"); dataLogger.setRS232Params( HPDataLogger.HPC_BAUD_9600,
				 * HPDataLogger.HPC_PAR_EVEN, HPDataLogger.HPC_CHAR_7, StopBits._ONE5_STOPBITS);
				 * dataLogger.checkForError();
				 */
				logger.debug("the datat logger is" + dataLogger);
				logger.debug("The number of channels are" + dataLogger.getNoOfChannels());
				/*
				 * String readVal[] = dataLogger.readValues(); Message.out("the string length is " + readVal.length);
				 * for (int i = 0; i < readVal.length; i++) { Message.out("the read string is " + i + " " + readVal[i]); }
				 */
				dataLogger.disconnect();
			} catch (DeviceException e) {
				logger.error("caught device Exception" + e.getMessage());
			}
		}
	}
}
