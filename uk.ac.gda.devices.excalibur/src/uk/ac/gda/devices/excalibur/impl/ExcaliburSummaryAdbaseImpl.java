/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.devices.excalibur.impl;

import gda.device.detector.areadetector.v17.ADBase;
import gda.device.detector.areadetector.v17.impl.ADBaseImpl;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.devices.excalibur.ExcaliburSummaryAdbase;
import uk.ac.gda.devices.excalibur.ReadoutNodeConnected;

/**
 *
 */
public class ExcaliburSummaryAdbaseImpl extends ADBaseImpl implements ExcaliburSummaryAdbase, ADBase,
		ReadoutNodeConnected {
	private static final Logger logger = LoggerFactory.getLogger(ExcaliburSummaryAdbaseImpl.class);

	private static final String CONNECTION_STATUS = "ConnectionStatus";
	private static final String CLEAR = "Clear";
	private static final String INCORRECT_SEQUENCE = "IncorrectSequence";
	private static final String INCOMPLETE_FRAMES = "IncompleteFrames";
	private static final String LATE_STRIPES = "LateStripes";
	private static final String RECEIVE_COUNT6 = "ReceiveCount6";
	private static final String RECEIVE_COUNT5 = "ReceiveCount5";
	private static final String RECEIVE_COUNT3 = "ReceiveCount3";
	private static final String RECEIVE_COUNT4 = "ReceiveCount4";
	private static final String RECEIVE_COUNT2 = "ReceiveCount2";
	private static final String RECEIVE_COUNT1 = "ReceiveCount1";
	private static final String COUNTER_DEPTH = "CounterDepth";
	private static final String FRAME_DIVISOR_RBV = "FrameDivisor_RBV";
	private static final String FRAME_DIVISOR = "FrameDivisor";
	private static final String GAP_FILL_CONSTANT = "GapFillConstant";
	private static final String GAP_FILL_CONSTANT_RBV = "GapFillConstant_RBV";

	@Override
	public int getGapFillConstant_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GAP_FILL_CONSTANT_RBV));
	}

	@Override
	public int getGapFillConstant() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(GAP_FILL_CONSTANT));
	}

	@Override
	public void setGapFillConstant(int gapFillConstant) throws TimeoutException, CAException, InterruptedException,
			Exception {
		EPICS_CONTROLLER.caput(getChannel(GAP_FILL_CONSTANT), gapFillConstant);
	}

	@Override
	public int getFrameDivisor() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_DIVISOR));
	}

	@Override
	public int getFrameDivisor_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(FRAME_DIVISOR_RBV));
	}

	@Override
	public void setFrameDivisor(int frameDivisor) throws TimeoutException, CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(FRAME_DIVISOR), frameDivisor);
	}

	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		logger.error("getCounterDepth commented out");
		return 0; //TODO EPICS_CONTROLLER.cagetInt(getChannel(COUNTER_DEPTH));
	}

	@Override
	public void setCounterDepth(int counterDepth) throws TimeoutException, CAException, InterruptedException, Exception {
		logger.error("setCounterDepth commented out");
		//TODO EPICS_CONTROLLER.caput(getChannel(COUNTER_DEPTH), counterDepth);
	}

	@Override
	public int getReceiveCount1() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT1));
	}

	@Override
	public int getReceiveCount2() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT2));
	}

	@Override
	public int getReceiveCount3() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT3));
	}

	@Override
	public int getReceiveCount4() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT4));
	}

	@Override
	public int getReceiveCount5() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT5));
	}

	@Override
	public int getReceiveCount6() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(RECEIVE_COUNT6));
	}

	@Override
	public int getLateStripes() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(LATE_STRIPES));
	}

	@Override
	public int getIncompleteFrames() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(INCOMPLETE_FRAMES));
	}

	@Override
	public int getIncorrectSequence() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(INCORRECT_SEQUENCE));
	}

	@Override
	public void clearCounters() throws TimeoutException, CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(CLEAR), 1);
	}

	private int getConnectionStatus() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(CONNECTION_STATUS));
	}

	@Override
	public boolean isReadoutNode1Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 1) == 1) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReadoutNode2Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 2) == 2) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReadoutNode3Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 4) == 4) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReadoutNode4Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 8) == 8) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReadoutNode5Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 16) == 16) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isReadoutNode6Connected() throws TimeoutException, CAException, InterruptedException, Exception {
		int connectionStatus = getConnectionStatus();
		if ((connectionStatus & 32) == 32) {
			return true;
		}
		return false;
	}

}
