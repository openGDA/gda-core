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

package gda.device.xspress;

import gda.util.Gaussian;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation - Code and
 * Comments
 */
public class DummyExafsServer extends ExafsServer {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyExafsServer.class);
	
	private int numberOfDetectors;

	private long timeOut = 10000;

	private double[][] windows;

	private long[][] dummyData;

	@Override
	protected void openSocket() {
		// Deliberately does nothing
	}

	@Override
	public ExafsServerReply sendCommand(String command) {
		ExafsServerReply reply = null;

		logger.debug("DummyExafsServer (" + getHost() + "," + getPort() + ") received command: " + command);
		StringTokenizer strtok = new StringTokenizer(command);
		int commandNumber = -1;

		try {
			commandNumber = Integer.valueOf(strtok.nextToken()).intValue();
		} catch (NumberFormatException nfe) {
			logger.debug("DummyExafsServer (" + getHost() + "," + getPort() + ") cannot parse command: " + command);
		}

		if (commandNumber == 228) {
			int numberOfBoards = Integer.valueOf(strtok.nextToken()).intValue();
			numberOfDetectors = Integer.valueOf(strtok.nextToken()).intValue();
			windows = new double[numberOfDetectors][2];
			dummyData = new long[numberOfDetectors][];
			for (int i = 0; i < numberOfDetectors; i++) {
				dummyData[i] = createDummyData(i);
			}
			reply = new ExafsServerReply("dummy exafsserver 228 0 " + numberOfBoards + " xspress boards initialized");
		} else if (commandNumber == 227) {
			int which = Integer.valueOf(strtok.nextToken());
			int start = Integer.valueOf(strtok.nextToken());
			int end = Integer.valueOf(strtok.nextToken());
			windows[which][0] = start;
			windows[which][1] = end;

			reply = new ExafsServerReply("dummy exafsserver 227 0 window set");
		}

		return reply;
	}

	@Override
	public ArrayList<ExafsServerReply> sendCommand(String command, String lookFor) {
		ArrayList<ExafsServerReply> replyList = null;
		logger.debug("DummyExafsServer (" + getHost() + "," + getPort() + ") received command: " + command);

		StringTokenizer strtok = new StringTokenizer(command);
		int commandNumber = Integer.valueOf(strtok.nextToken()).intValue();

		// MCData
		if (commandNumber == 226) {
			int detector = Integer.valueOf(strtok.nextToken()).intValue();
			int startChannel = Integer.valueOf(strtok.nextToken()).intValue();
			int endChannel = Integer.valueOf(strtok.nextToken()).intValue();
			int countTime = Integer.valueOf(strtok.nextToken()).intValue();
			int multiple = Integer.valueOf(strtok.nextToken()).intValue();

			logger.debug("detector " + detector + " start " + startChannel + " end " + endChannel + " time "
					+ countTime + " multiple " + multiple);
			replyList = new ArrayList<ExafsServerReply>();

			replyList
					.add(new ExafsServerReply("dummy exafsserver 226 0 " + startChannel + " " + endChannel + " 10 10"));
			replyList.add(new ExafsServerReply("dummy exafsserver 220 0 pretend mcdata about to start"));
			try {
				Thread.sleep(countTime * 1000);
			} catch (InterruptedException e) {
				// Deliberately do nothing
			}

			String string = "";
			for (int counter = startChannel; counter <= endChannel; counter += multiple) {
				string = "";
				for (int j = 0; j < multiple && j + counter <= endChannel; j++) {
					string = string + dummyData[detector][counter + j] + " ";
				}
				replyList.add(new ExafsServerReply("dummy exafsserver 226 0 " + string));
			}

			replyList.add(new ExafsServerReply("dummy exafsserver 226 0 ending"));
			replyList.add(new ExafsServerReply("dummy exafsserver 220 0 mcdata complete"));
		} else if (commandNumber == 246 || commandNumber == 247) {
			replyList = new ArrayList<ExafsServerReply>();
			int numberOfValues = numberOfDetectors * 4;
			int numberOfLines = numberOfValues / 12;

			for (int i = 0; i < numberOfLines; i++)
				replyList
						.add(new ExafsServerReply("dummy exafsserver " + commandNumber + " 0 "
								+ plausibleDetectorOutput() + " " + plausibleDetectorOutput() + " "
								+ plausibleDetectorOutput()));
			int leftOver = numberOfValues % 12;
			if (leftOver != 0)
				replyList.add(new ExafsServerReply("dummy exafsserver " + commandNumber + " 0 "
						+ plausibleDetectorOutput()));
			replyList.add(new ExafsServerReply("dummy exafsserver " + commandNumber + " 0 read done"));
		}
		return replyList;
	}

	private String plausibleDetectorOutput() {
		String rtrn = "";
		int total = (int) (Math.random() * 1000.0);
		rtrn = rtrn + total / 2 + " " + total + " " + total / 4 + " " + (int) (Math.random() * 100.0);
		return rtrn;

	}

	@Override
	public long getTimeOut() {
		return timeOut;
	}

	@Override
	public void setTimeOut(long timeOut) {
		this.timeOut = timeOut;
	}

	/*
	 * Creates some dummy data @return an array of dummy values
	 */
	private long[] createDummyData(int detector) {
		long[] data = new long[4096];
		Gaussian gaussianOne;
		Gaussian gaussianTwo;
		double noiseLevel = 0.2 + 0.01 * detector;

		gaussianOne = new Gaussian(1600.0 + 100.0 * detector, 500.0, 1000.0);
		gaussianTwo = new Gaussian(1000.0 + 10.0 * detector, 200.0, 500.0);
		for (int i = 0; i < 4096; i++) {
			data[i] = (long) (gaussianOne.yAtX(i) * (1.0 - Math.random() * noiseLevel) + gaussianTwo.yAtX(i)
					* (1.0 - Math.random() * noiseLevel));
		}

		return data;
	}

}