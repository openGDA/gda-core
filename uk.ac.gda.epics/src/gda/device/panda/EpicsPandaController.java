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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.ConfigurableBase;

public class EpicsPandaController extends ConfigurableBase implements PandaController  {
	private static final Logger logger = LoggerFactory.getLogger(EpicsPandaController.class);

	/** Set of logical name and and PV names for controlling Panda */
	private enum PandaPvName {
		PCAP_ARM("PCAP:ARM"),
		PCAP_ACTIVE("PCAP:ACTIVE"),
		SEQ_BITA("SEQ1:BITA"),
		SEQ_ENABLE("SEQ1:ENABLE"),
		SEQ_TABLE("SEQ1:TABLE"),
		SEQ_LINE_REPEAT("SEQ1:LINE_REPEAT"),
		SEQ_TABLE_LINE("SEQ1:TABLE_LINE"),
		SEQ_STATE("SEQ1:STATE"),
		SEQ_ACTIVE("SEQ1:ACTIVE"),
		SEQ_PRESCALE_UNITS("SEQ1:PRESCALE:UNITS"),
		SEQ_PRESCALE("SEQ1:PRESCALE"),

		PULSE_ENABLE("PULSE1:ENABLE"),
		PULSE_TRIG("PULSE1:TRIG"),

		HDF_DIRECTORY("DATA:HDF_DIRECTORY"),
		HDF_FILE_NAME("DATA:HDF_FILE_NAME"),
		HDF_FULL_FILE_PATH("DATA:HDF_FULL_FILE_PATH"),
		HDF_FLUSH_PERIOD("DATA:FLUSH_PERIOD"),
		HDF_CAPTURE("DATA:CAPTURE"),
		HDF_NUM_CAPTURED("DATA:NUM_CAPTURED");

		private final String pvName;
		private PandaPvName(String pvName) {
			this.pvName = pvName;
		}
		public String getPvName() {
			return pvName;
		}
	}

	/** Collection of PVs objects */
	private Map<PandaPvName, PVAccess> pvMap = new EnumMap<>(PandaPvName.class);
	private Map<String, PVAccess> extraPvMap = new HashMap<>();

	private String basePvName = "";

	private double monitorTimeoutSecs = 10;

	/**
	 * Create all the PVAccess object for controlling Panda
	 */
	@Override
	public void configure() {
		// create all the PVs
		logger.info("Creating PVs...");
		for(var v : PandaPvName.values()) {
			logger.debug("Creating PV for {} : {}",v, basePvName+v.getPvName());
			pvMap.put(v, createPv(v.getPvName()));
		}
		setConfigured(true);
	}

	private PVAccess createPv(String pvName) {
		return new PVAccess(basePvName+pvName);
	}

	@Override
	public void reconfigure() {
		setConfigured(false);
		disconnect();
		configure();
	}

	private void disconnect() {
		for(var pv : pvMap.values()) {
			pv.disconnect();
		}
	}

	/**
	 *
	 * @param pvName
	 * @return PVAccess object for the PandaPvName enum value
	 * @throws DeviceException if PV is not found
	 */
	private PVAccess getPv(PandaPvName pvName) throws DeviceException {
		if (pvMap.containsKey(pvName)) {
			return pvMap.get(pvName);
		}
		throw new DeviceException("Not PV found for "+pvName.toString()+" in "+EpicsPandaController.class.getSimpleName()+". Have the PVs been created?");
	}

	@Override
	public void putPvValue(String pvName, Object value) throws DeviceException {
		PVAccess pv = extraPvMap.computeIfAbsent(pvName, this::createPv);
		pv.putValue(value);
	}

	private <T> List<T> collectVals(List<SequenceTableRow> rows, Function<SequenceTableRow, T> valueFunction) {
		return rows.stream().map(valueFunction::apply).toList();
	}

	@Override
	public void setSeqTableRows(List<SequenceTableRow> tableRows) throws DeviceException {
		// Get the PVStructure object for the sequence table
		PVAccess seqTable = getPv(PandaPvName.SEQ_TABLE);
		PVField pvPut = seqTable.getPvPutValueField();
		if (!(pvPut instanceof PVStructure)) {
			throw new DeviceException("Cannot set sequence table - PVField is not PVStructure");
		}

		PVStructure pvStruct = (PVStructure) pvPut;
		// fill the structure with new values from tableRows :
		seqTable.putArray(pvStruct, SequenceTableColumns.REPEATS.value(), collectVals(tableRows, SequenceTableRow::getRepeats));
		seqTable.putArray(pvStruct, SequenceTableColumns.POSITION.value(), collectVals(tableRows, SequenceTableRow::getPosition));
		seqTable.putArray(pvStruct, SequenceTableColumns.TIME1.value(), collectVals(tableRows, SequenceTableRow::getTime1));
		seqTable.putArray(pvStruct, SequenceTableColumns.TIME2.value(), collectVals(tableRows, SequenceTableRow::getTime2));

		List<SequenceTableColumns> output1Columns = SequenceTableColumns.getOutput1Columns();
		for(int i=0; i<output1Columns.size(); i++) {
			int columnNumber = i; // needs to be local so it can used in Function parameter
			seqTable.putArray(pvStruct, output1Columns.get(columnNumber).value(), collectVals(tableRows, row->row.getOutputs1().get(columnNumber)));
		}

		List<SequenceTableColumns> output2Columns = SequenceTableColumns.getOutput2Columns();
		for(int i=0; i<output2Columns.size(); i++) {
			int columnNumber = i; // needs to be local so it can used in Function parameter
			seqTable.putArray(pvStruct, output2Columns.get(columnNumber).value(), collectVals(tableRows, row->row.getOutputs2().get(columnNumber)));
		}
		seqTable.putArray(pvStruct, SequenceTableColumns.TRIGGER.value(), collectVals(tableRows,  SequenceTableRow::getTriggerType));

		// Push to epics
		seqTable.putComplete();
	}

	@Override
	public void setSeqTablePrescale(SequenceTableTimeUnits unit, double value) throws DeviceException {
		getPv(PandaPvName.SEQ_PRESCALE_UNITS).putValue(unit.ordinal());
		getPv(PandaPvName.SEQ_PRESCALE).putValue(value);
	}

	@Override
	public void setSeqBitA(String value) throws DeviceException {
		getPv(PandaPvName.SEQ_BITA).putValue(value);
	}

	@Override
	public int getSeqTableLine() throws DeviceException {
		return getPv(PandaPvName.SEQ_TABLE_LINE).getValue(Integer.class);
	}

	@Override
	public int getSeqTableLineRepeat() throws DeviceException {
		return getPv(PandaPvName.SEQ_LINE_REPEAT).getValue(Integer.class);
	}

	@Override
	public SeqTableState getSeqTableState() throws DeviceException {
		String strValue = getPv(PandaPvName.SEQ_STATE).getValue(String.class);
		try {
			return SeqTableState.valueOf(strValue);
		} catch(IllegalArgumentException e) {
			throw new DeviceException("Sequence table state '"+strValue+"' is not one of the recognised states in "+SeqTableState.class.getCanonicalName());
		}
	}

	@Override
	public boolean setPCapArm(int state) throws DeviceException {
		getPv(PandaPvName.PCAP_ARM).putValue(state);
		return getPv(PandaPvName.PCAP_ACTIVE).monitorForCondition(val -> Integer.valueOf(val.toString()) == state, monitorTimeoutSecs);
	}

	@Override
	public int getPCapArm() throws DeviceException {
		return getPv(PandaPvName.PCAP_ARM).getValue(Integer.class);
	}

	@Override
	public void setPulseTrig(String value) throws DeviceException {
		getPv(PandaPvName.PULSE_TRIG).putValue(value);
	}

	@Override
	public void setPulseEnable(String value) throws DeviceException {
		getPv(PandaPvName.PULSE_ENABLE).putValue(value);
	}

	@Override
	public void setupHdfWriter(String directory, String filename) throws DeviceException {
		getPv(PandaPvName.HDF_DIRECTORY).putValue(directory);
		getPv(PandaPvName.HDF_FILE_NAME).putValue(filename);
	}

	@Override
	public boolean setHdfCapture(int state) throws DeviceException {
		getPv(PandaPvName.HDF_CAPTURE).putValue(state);
		return getPv(PandaPvName.HDF_CAPTURE).monitorForCondition(val -> Integer.valueOf(val.toString()) == state, monitorTimeoutSecs);
	}

	@Override
	public int getHdfCapture() throws DeviceException {
		return getPv(PandaPvName.HDF_CAPTURE).getValue(Integer.class);
	}

	@Override
	public String getHdfFileFullPath() throws DeviceException {
		return getPv(PandaPvName.HDF_FULL_FILE_PATH).getValue(String.class);
	}

	@Override
	public int getHdfNumCaptured() throws DeviceException {
		return getPv(PandaPvName.HDF_NUM_CAPTURED).getValue(Integer.class);
	}

	@Override
	public boolean waitForHdfNumCaptured(int expectedFrame) throws DeviceException {
		return getPv(PandaPvName.HDF_NUM_CAPTURED).monitorForCondition(val -> Integer.valueOf(val.toString()) >= expectedFrame, monitorTimeoutSecs);
	}

	public String getBasePvName() {
		return basePvName;
	}

	public void setBasePvName(String basePvName) {
		this.basePvName = basePvName;
	}

	public double getMonitorTimeoutSecs() {
		return monitorTimeoutSecs;
	}

	public void setMonitorTimeoutSecs(double monitorTimeoutSecs) {
		this.monitorTimeoutSecs = monitorTimeoutSecs;
	}
}
