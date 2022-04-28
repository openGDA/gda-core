/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig;

import gda.device.BaseEpicsDeviceController;

/**
 * As there is no simulator for the gas rig, this is hard to test in dummy mode. Therefore this base class
 * contains all the methods require for building live PVs, so that both the live and dummy controller classes
 * can inherit from it and the dummy controller can log the PVs which would be used in live mode, to aid
 * in debugging.
 *
 * @author Tom Richardson (too27251)
 */
public abstract class BaseGasRigController extends BaseEpicsDeviceController {

	private static final String MFC = "MFC-%02d:";
	private static final String GAS_NAME = MFC + "GAS:STR:RD";
	private static final String MASS_FLOW_SETPOINT = MFC + "SETPOINT:WR";
	private static final String MAX_MASS_FLOW = MASS_FLOW_SETPOINT + ".HOPR";

	private static final String SEQUENCE = "PLC-01:SEQ%d:";
	private static final String SEQUENCE_CONTROL = SEQUENCE + "CON";
	private static final String SEQUENCE_STATUS = SEQUENCE + "STA";
	private static final String SEQUENCE_PROGRESS = SEQUENCE + "PROGRESS";
	private static final String SEQUENCE_NUMERIC_PARAMETER = SEQUENCE + "PARAM%d:WR";
	private static final String SEQUENCE_ENUM_PARAMETER = SEQUENCE + "PARAM%d";

	private static final String VALVE_STATUS = "VALVE-%d:STA";
	private static final String VALVE_CONTROL = "VALVE-%d:CON";

	protected static final String SEQUENCE_START = "Start";
	protected static final String VALVE_CLOSE = "Close";
	protected static final String VALVE_OPEN = "Open";


	protected BaseGasRigController(String basePvName) {
		setBasePvName(basePvName);
	}

	protected String constructGasNamePvSuffix(int gasId) {
		return String.format(GAS_NAME, gasId);
	}

	protected String constructMaximumMassFlowPvSuffix(int gasId) {
		return String.format(MAX_MASS_FLOW, gasId);
	}

	protected String constructSequenceControlPvSuffix(int sequenceNumber) {
		return String.format(SEQUENCE_CONTROL, sequenceNumber);
	}

	protected String constructSequenceStatusPvSuffix(int sequenceNumber) {
		return String.format(SEQUENCE_STATUS, sequenceNumber);
	}

	protected String constructSequenceProgressPvSuffix(int sequenceNumber) {
		return String.format(SEQUENCE_PROGRESS, sequenceNumber);
	}

	protected String constructNumericSequenceParameterPv(int sequenceNumber, int parameterNumber) {
		return String.format(SEQUENCE_NUMERIC_PARAMETER, sequenceNumber, parameterNumber);
	}

	protected String constructEnumSequenceParameterPv(int sequenceNumber, int parameterNumber) {
		return String.format(SEQUENCE_ENUM_PARAMETER, sequenceNumber, parameterNumber);
	}

	protected String constructValveStatusPv(int valveNumber) {
		return String.format(VALVE_STATUS, valveNumber);
	}

	protected String constructValveControlPv(int valveNumber) {
		return String.format(VALVE_CONTROL, valveNumber);
	}

	protected String constructMassFlowSetPointPv(int gasId) {
		return String.format(MASS_FLOW_SETPOINT, gasId);
	}
}
