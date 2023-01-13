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
import uk.ac.gda.devices.excalibur.ArrayCounts;
import uk.ac.gda.devices.excalibur.ExcaliburConfigAdbase;
import uk.ac.gda.devices.excalibur.ReadoutNodeConnected;

/**
 *
 */
public class DummyExcaliburConfigAdbaseImpl extends ADBaseSimulator implements ExcaliburConfigAdbase, ADBase,
		ReadoutNodeConnected {

	private ArrayCounts arrayCounts;
	private int counterDepth;
	private int connectionStatus;

	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		return counterDepth;
	}

	@Override
	public void setCounterDepth(int counterDepth) throws CAException, InterruptedException, Exception {
		this.counterDepth = counterDepth;
	}

	@Override
	public ArrayCounts getArrayCounts() {
		return arrayCounts;
	}

	public void setArrayCounts(ArrayCounts arrayCounts) {
		this.arrayCounts = arrayCounts;
	}

	public static class ArrayCountsImpl  implements ArrayCounts {


		private int arrayCountfem6;
		private int arrayCountfem5;
		private int arrayCountfem4;
		private int arrayCountfem3;
		private int arrayCountfem2;
		private int arrayCountfem1;

		@Override
		public int getArrayCountFem1() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem1;
		}

		@Override
		public int getArrayCountFem2() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem2;
		}

		@Override
		public int getArrayCountFem3() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem3;
		}

		@Override
		public int getArrayCountFem4() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem4;
		}

		@Override
		public int getArrayCountFem5() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem5;
		}

		@Override
		public int getArrayCountFem6() throws TimeoutException, CAException, InterruptedException, Exception {
			return arrayCountfem6;
		}
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

	@Override
	public short[] getPixelMask() throws Exception {
		return null;
	}

	@Override
	public void setPixelMask(short[] pixelMask) throws Exception {
	}

	@Override
	public short[] getPixelTest() throws Exception {
		return null;
	}

	@Override
	public void setPixelTest(short[] pixelMask) throws Exception {
	}

	@Override
	public void setPixelGainMode(short[] pixelGainMode) throws Exception {
	}

	@Override
	public short[] getPixelGainMode() throws Exception {
		return null;
	}

	@Override
	public void setPixelThresholdA(short[] pixelThresholdA) throws Exception {
	}

	@Override
	public short[] getPixelThresholdA() throws Exception {
		return null;
	}

	@Override
	public short[] getPixelThresholdB() throws Exception {
		return null;
	}

	@Override
	public void setPixelThresholdB(short[] pixelThresholdB) throws Exception {
	}

	@Override
	public void setChipEnable(short[] enableBits) throws Exception {
	}

	@Override
	public short[] getChipEnable() throws Exception {
		return null;
	}

}
