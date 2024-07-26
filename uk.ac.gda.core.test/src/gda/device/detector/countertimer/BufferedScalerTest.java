/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package gda.device.detector.countertimer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.ContinuousParameters;
import gda.device.DeviceException;
import gda.device.detector.DAServer;

public class BufferedScalerTest {
	private BufferedScaler bufferedScaler;
	private DAServerForTest daServer;

	private static final String triggerStart = "tfg setup-trig start ttl%d";
	private static final String setupGroup = "tfg setup-groups %scycles %s";
	private static final String timingGroup = "%d %.4g %.4g 0 0 0 %d";
	private static final String endGroup = "-1 0 0 0 0 0 0";

	@BeforeEach
	public void setup() {
		daServer = new DAServerForTest();
		bufferedScaler = new BufferedScaler();
		bufferedScaler.setDaserver(daServer);
		bufferedScaler.setTtlSocket(0);
		bufferedScaler.setNumCycles(1);
	}

	/** DAServer implementation that stores the command strings passed to sendCommand */
	private class DAServerForTest extends DAServer {
		private List<String> commandStrings = new ArrayList<>();

		@Override
		public Object sendCommand(String command) {
			commandStrings.add(command);
			return null;
		}

		public void clearCommands() {
			commandStrings.clear();
		}

		public List<String> getCommands() {
			return commandStrings;
		}
	}

	@Test
	public void testInternalTrigger() throws DeviceException {
		var p = createParameters(1000, 0.1);
		bufferedScaler.setContinuousParameters(p);
		bufferedScaler.setUseInternalTriggeredFrames(true);

		daServer.clearCommands();
		bufferedScaler.setContinuousMode(true);

		List<String> commandString = daServer.getCommands();
		assertEquals("Incorrect number of command lines sent", 2, commandString.size());
		testTimingGroup(commandString.get(0), false, false);
		testTfgStart(commandString.get(1), false);
	}

	@Test
	public void testExternalTriggerStartAndFrames() throws DeviceException {
		var p = createParameters(1000, 0.1);
		bufferedScaler.setContinuousParameters(p);

		daServer.clearCommands();
		bufferedScaler.setContinuousMode(true);

		List<String> commandString = daServer.getCommands();
		assertEquals("Incorrect number of command lines sent", 3, commandString.size());

		testTriggerStart(commandString.get(0));
		testTimingGroup(commandString.get(1), true, true);
		testTfgStart(commandString.get(2), true);
	}

	private ContinuousParameters createParameters(int numFrames, double timePerFrame) {
		ContinuousParameters p = new ContinuousParameters();
		p.setTotalTime(timePerFrame*numFrames);
		p.setNumberDataPoints(numFrames);
		return p;
	}

	/**
	 * Extract numbers from a string
	 * @param str
	 * @return array of doubles
	 */
	private double[] extractNumbers(String str) {
		String[] splitStr = str.split("[\\s+]");
		return Stream.of(splitStr).mapToDouble(Double::parseDouble).toArray();
	}

	/**
	 * Split string by newline character
	 * @param s
	 * @return list of strings
	 */
	private List<String> splitLines(String s) {
		return Stream.of(s.split("\\n")).toList();
	}

	private String createTimingGroup(int numFrames, double frameTime, boolean externalTrigger) {
		if (externalTrigger) {
			return String.format(timingGroup, numFrames, bufferedScaler.getFrameDeadTime(), 1e-8, bufferedScaler.getTtlSocket()+8);
		} else {
			return String.format(timingGroup, numFrames, bufferedScaler.getFrameDeadTime(), frameTime, 0);
		}
	}

	private void checkTimingGroup(String expected, String actual) {
		assertArrayEquals("Timing group is not correct", extractNumbers(expected), extractNumbers(actual), 1e-6);
	}

	/**
	 * Check structure of timing group string is correct :
	 * <li> group is set for internal/external triggering correctly, with correct number of cycles
	 * <li> custom initial group command appears first in the timing group setup
	 * <li> Correct number of frames, framelength etc have been set
	 * <li> end group line has been added
	 *
	 * @param timingGroupString
	 * @param externalTriggerStart
	 * @param externalTriggerFrames
	 */
	private void testTimingGroup(String timingGroupString, boolean externalTriggerStart, boolean externalTriggerFrames) {

		List<String> splitLine = splitLines(timingGroupString);
		assertEquals(3, splitLine.size());
		String ext_start = externalTriggerStart ? "ext-start " : "";
		assertEquals("Timing group setup is not correct", String.format(setupGroup, ext_start, bufferedScaler.getNumCycles()), splitLine.get(0));

		checkTimingGroup(createTimingGroup(1000, 0.1, externalTriggerFrames), splitLine.get(1));
		assertEquals(endGroup, splitLine.get(2));
	}

	/**
	 * Test string containing start command is correct, accounting for internal/external trigger mode
	 * @param command
	 * @param externalTrigger
	 */
	private void testTfgStart(String command, boolean externalTrigger) {
		assertEquals(externalTrigger ? "tfg arm" : "tfg start", command);
	}

	/**
	 * Check 'external trigger start' command is correct
	 * @param command
	 */
	private void testTriggerStart(String command) {
		assertEquals(String.format(triggerStart, bufferedScaler.getTtlSocket()), command);
	}
}
