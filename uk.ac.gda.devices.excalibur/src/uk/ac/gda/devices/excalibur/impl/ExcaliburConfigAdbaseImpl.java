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
import uk.ac.gda.devices.excalibur.ArrayCounts;
import uk.ac.gda.devices.excalibur.ExcaliburConfigAdbase;
import uk.ac.gda.devices.excalibur.ReadoutNodeConnected;

/**
 *
 */
public class ExcaliburConfigAdbaseImpl extends ADBaseImpl implements ExcaliburConfigAdbase, ADBase,
		ReadoutNodeConnected {

	private static final String CONNECTION_STATUS = "ConnectionStatus";
	private static final String COUNTER_DEPTH = "CounterDepth";
	private ArrayCounts arrayCounts;

	@Override
	public int getCounterDepth() throws TimeoutException, CAException, InterruptedException, Exception {
		return EPICS_CONTROLLER.cagetInt(getChannel(COUNTER_DEPTH));
	}

	@Override
	public void setCounterDepth(int counterDepth) throws CAException, InterruptedException, Exception {
		EPICS_CONTROLLER.caput(getChannel(COUNTER_DEPTH), counterDepth);
	}

	@Override
	public ArrayCounts getArrayCounts() {
		return arrayCounts;
	}

	public void setArrayCounts(ArrayCounts arrayCounts) {
		this.arrayCounts = arrayCounts;
	}

	public static class ArrayCountsImpl extends BasePvProvidingImpl implements ArrayCounts {

		private static final String _6_FEM_ARRAY_COUNTER_RBV = "6:FEM:ArrayCounter_RBV";
		private static final String _5_FEM_ARRAY_COUNTER_RBV = "5:FEM:ArrayCounter_RBV";
		private static final String _4_FEM_ARRAY_COUNTER_RBV = "4:FEM:ArrayCounter_RBV";
		private static final String _3_FEM_ARRAY_COUNTER_RBV = "3:FEM:ArrayCounter_RBV";
		private static final String _2_FEM_ARRAY_COUNTER_RBV = "2:FEM:ArrayCounter_RBV";
		private static final String _1_FEM_ARRAY_COUNTER_RBV = "1:FEM:ArrayCounter_RBV";

		@Override
		public int getArrayCountFem1() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_1_FEM_ARRAY_COUNTER_RBV));
		}

		@Override
		public int getArrayCountFem2() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_2_FEM_ARRAY_COUNTER_RBV));
		}

		@Override
		public int getArrayCountFem3() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_3_FEM_ARRAY_COUNTER_RBV));
		}

		@Override
		public int getArrayCountFem4() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_4_FEM_ARRAY_COUNTER_RBV));
		}

		@Override
		public int getArrayCountFem5() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_5_FEM_ARRAY_COUNTER_RBV));
		}

		@Override
		public int getArrayCountFem6() throws TimeoutException, CAException, InterruptedException, Exception {
			return EPICS_CONTROLLER.cagetInt(getChannel(_6_FEM_ARRAY_COUNTER_RBV));
		}
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
