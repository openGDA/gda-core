/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

package gda.device.panda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import gda.device.DeviceException;
import gda.factory.Configurable;

public interface PandaController extends Configurable {

	/**
	 * Class to represent a single row of values in the sequence table
	 */
	public static class SequenceTableRow {
		private int repeats = 1;
		private String triggerType;
		private int position;
		private int time1;
		private List<Integer> outputs1; // 'output1' values on a, b, c, d, e, f ports (0 or 1)
		private int time2;
		private List<Integer> outputs2; // 'output2' values on a, b, c, d, e, f ports (0 or 1)
		public static final int NUM_OUTPUTS = 6;

		public SequenceTableRow() {
			triggerType = TriggerType.IMMEDIATE.getValue();
			time1 = 1000;
			outputs1 = Collections.nCopies(NUM_OUTPUTS, 0);
			outputs2 = Collections.nCopies(NUM_OUTPUTS, 0);
		}

		public SequenceTableRow(SequenceTableRow other) {
			repeats = other.repeats;
			triggerType = other.triggerType;
			position = other.position;
			time1 = other.time1;
			outputs1 = new ArrayList<>(other.outputs1);
			time2 = other.time2;
			outputs2 = new ArrayList<>(other.outputs2);
		}

		public int getRepeats() {
			return repeats;
		}

		public void setRepeats(int repeats) {
			this.repeats = repeats;
		}

		public String getTriggerType() {
			return triggerType;
		}

		public void setTriggerType(String triggerType) {
			this.triggerType = triggerType;
		}

		public int getPosition() {
			return position;
		}

		public void setPosition(int position) {
			this.position = position;
		}

		public int getTime1() {
			return time1;
		}

		public void setTime1(int time1) {
			this.time1 = time1;
		}

		public List<Integer> getOutputs1() {
			return outputs1;
		}

		private int numberToBit(int num) {
			if (num > 0) {
				return 1;
			}
			return 0;
		}
		public void setOutputs1(int index,int value) {
			this.outputs1.set(index, numberToBit(value));
		}

		public void setOutputs1(List<Integer> outputs1) {
			this.outputs1 = outputs1.stream().map(this::numberToBit).toList();
		}

		public int getTime2() {
			return time2;
		}

		public void setTime2(int time2) {
			this.time2 = time2;
		}

		public List<Integer> getOutputs2() {
			return outputs2;
		}

		public void setOutputs2(List<Integer> outputs2) {
			this.outputs2 = outputs2.stream().map(this::numberToBit).toList();
		}
	}

	/** Name of each column in the sequence table, in the same order that they appear in the web GUI */
	enum SequenceTableColumns {
		REPEATS,
		TRIGGER,
		POSITION,
		TIME1,
		OUTA1,
		OUTB1,
		OUTC1,
		OUTD1,
		OUTE1,
		OUTF1,
		TIME2,
		OUTA2,
		OUTB2,
		OUTC2,
		OUTD2,
		OUTE2,
		OUTF2;

		public static List<SequenceTableColumns> getOutput1Columns() {
			return List.of(OUTA1, OUTB1, OUTC1, OUTD1, OUTE1, OUTF1);
		}
		public static List<SequenceTableColumns> getOutput2Columns() {
			return List.of(OUTA2, OUTB2, OUTC2, OUTD2, OUTE2, OUTF2);
		}
		public String value() {
			return this.toString().toLowerCase();
		}
	}

	/** Units allowed for table time prescale value */
	enum SequenceTableTimeUnits {
		MINUTES(1.0/60),
		SECONDS(1.0),
		MILLISEC(1e3),
		MICROSEC(1e6);

		private final double convertSecondsToUnits; // conversion factor from time in seconds to time in minutes, seconds, etc.
		private SequenceTableTimeUnits(double convertSecondsToUnits) {
			this.convertSecondsToUnits = convertSecondsToUnits;
		}
		/** Convert time in seconds to the sequence table time units
		 * (i.e. seconds to milliseconds, microseconds, etc).
		 */
		public double convertFromSeconds(double timeInSeconds) {
			return convertSecondsToUnits*timeInSeconds;
		}
	}

	/**
	 * Enumeration of possible Trigger types allowed in sequence table
	 */
	enum TriggerType{
		IMMEDIATE("Immediate"),
		BITA_0("BITA=0"), BITA_1("BITA=1"),
		BITB_0("BITB=0"), BITB_1("BITB=1"),
		BITC_0("BITC=0"), BITC_1("BITC=1"),
		POSA_GT_POSITION("POSA>=POSITION"),
		POSA_LEQ_POSITION("POSA<=POSITION"),
		POSB_GEQ_POSITION("POSB>=POSITION"),
		POSB_LEQ_POSITION("POSB<=POSITION"),
		POSC_GEQ_POSITION("POSC>=POSITION"),
		POSC_LEQ_POSITION("POSC<=POSITION");

		private final String stringValue;
		private TriggerType(String stringValue) {
			this.stringValue = stringValue;
		}

		public String getValue() {
			return stringValue;
		}
	}

	/** State the sequence table can be in */
	enum SeqTableState {
		UNREADY, WAIT_ENABLE, WAIT_TRIGGER, PHASE1, PHASE2;
	}

	/**
	 * Set the filename and directory in the Hdf writer
	 * @param directory
	 * @param filename
	 * @throws DeviceException
	 */
	void setupHdfWriter(String directory, String filename) throws DeviceException;

	/** Set the capture state on the Hdf writer and wait for completion */

	/**
	 * Set the capture state on the Hdf writer and wait for completion
	 * @param state 1 to arm, 0 to disarm
	 * @throws DeviceException
	 * @return True if capture state was set successfully within timeout, False otherwise
	 */
	boolean setHdfCapture(int state) throws DeviceException;

	/**
	 * @return Current capture state of the hdf writer (0 for disarmed, 1 for armed).
	 * @throws DeviceException
	 */
	int getHdfCapture() throws DeviceException;

	/** Get the full path the hdf file */
	String getHdfFileFullPath() throws DeviceException;;

	/** Return number of points captured by the Hdf writer */
	int getHdfNumCaptured() throws DeviceException;

	/** Wait until >= expectedFrame points have been captured by the Hdf writer.
	 *
	 * @param expectedFrame
	 * @return True if frame number was reached within timeout, False otherwise
	 * @throws DeviceException
	 */
	boolean waitForHdfNumCaptured(int expectedFrame) throws DeviceException;

	/**
	 * Setup the sequence table using the list of tableRows
	 * @param tableRows
	 * @throws DeviceException
	 */
	void setSeqTableRows(List<SequenceTableRow> tableRows) throws DeviceException;

	/**
	 * Set the sequence table time units and prescale values
	 * @param unit
	 * @param value
	 * @throws DeviceException
	 */
	void setSeqTablePrescale(SequenceTableTimeUnits unit, double value) throws DeviceException;

	/**
	 * Set Sequence BitA value
	 * @param value
	 * @throws DeviceException
	 */
	void setSeqBitA(String value) throws DeviceException;

	/**
	 * @return Number of the sequence table line currently being processed
	 * @throws DeviceException
	 */
	int getSeqTableLine() throws DeviceException;

	/**
	 * @return the repetition number of the sequence table line being run
	 * @throws DeviceException
	 */
	int getSeqTableLineRepeat() throws DeviceException; // the repetition number of the sequence table line being run

	/**
	 * @return state of the sequence table - one states in {@link SeqTableState}
	 * @throws DeviceException
	 */
	SeqTableState getSeqTableState() throws DeviceException;

	/**
	 * Set the capture state on Position capture block and wait for completion
	 * @param state Use 1 to arm, 0 to disarm
	 * @throws DeviceException
	 * @return True if capture state was set successfully within timeout, False otherwise
	 */
	boolean setPCapArm(int state) throws DeviceException;

	/**
	 * @return capture state on Position capture block (0 = disarmed, 1 = armed)
	 * @throws DeviceException
	 */
	int getPCapArm() throws DeviceException;
}
