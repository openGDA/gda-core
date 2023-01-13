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
import gda.device.detector.areadetector.v17.impl.ADBaseSimulator;
import gov.aps.jca.CAException;
import gov.aps.jca.TimeoutException;
import uk.ac.gda.devices.excalibur.ExcaliburSummaryAdbase;
import uk.ac.gda.devices.excalibur.ReadoutNodeConnected;

/**
 *
 */
public class DummyExcaliburSummaryAdbaseImpl extends ADBaseSimulator implements ExcaliburSummaryAdbase, ADBase,
		ReadoutNodeConnected {


	private int gapFillConstant;
	private int frameDivisor;
	private int counterDepth;
	private int receiveCount1;
	private int receiveCount6;
	private int incorrectSequence;
	private int incompleteFrames;
	private int latestStripes;
	private int receiveCount5;
	private int receiveCount4;
	private int receiveCount3;
	private int receiveCount2;
	private int connectionStatus;

	@Override
	public int getGapFillConstant_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFillConstant;
	}

	@Override
	public int getGapFillConstant() throws TimeoutException, CAException, InterruptedException, Exception {
		return gapFillConstant;
	}

	@Override
	public void setGapFillConstant(int gapFillConstant) throws TimeoutException, CAException, InterruptedException,
			Exception {
		this.gapFillConstant = gapFillConstant;
	}

	@Override
	public int getFrameDivisor() throws TimeoutException, CAException, InterruptedException, Exception {
		return frameDivisor;
	}

	@Override
	public int getFrameDivisor_RBV() throws TimeoutException, CAException, InterruptedException, Exception {
		return frameDivisor;
	}

	@Override
	public void setFrameDivisor(int frameDivisor) throws TimeoutException, CAException, InterruptedException, Exception {
		this.frameDivisor = frameDivisor;
	}

	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		return counterDepth;
	}

	@Override
	public void setCounterDepth(int counterDepth) throws TimeoutException, CAException, InterruptedException, Exception {
		this.counterDepth = counterDepth;
	}

	@Override
	public int getReceiveCount1() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount1;
	}

	@Override
	public int getReceiveCount2() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount2;
	}

	@Override
	public int getReceiveCount3() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount3;
	}

	@Override
	public int getReceiveCount4() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount4;
	}

	@Override
	public int getReceiveCount5() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount5;
	}

	@Override
	public int getReceiveCount6() throws TimeoutException, CAException, InterruptedException, Exception {
		return receiveCount6;
	}

	@Override
	public int getLateStripes() throws TimeoutException, CAException, InterruptedException, Exception {
		return latestStripes;
	}

	@Override
	public int getIncompleteFrames() throws TimeoutException, CAException, InterruptedException, Exception {
		return incompleteFrames;
	}

	@Override
	public int getIncorrectSequence() throws TimeoutException, CAException, InterruptedException, Exception {
		return incorrectSequence;
	}

	@Override
	public void clearCounters() throws TimeoutException, CAException, InterruptedException, Exception {
		//
	}

	private int getConnectionStatus() {
		return connectionStatus;
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
