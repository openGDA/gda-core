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

import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.nexus.NexusException;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;

public class EpicsPandaController extends ConfigurableBase implements PandaController  {

	private static final Logger logger = LoggerFactory.getLogger(EpicsPandaController.class);

	/** Collection of PVs objects */
	private Map<PandaPvName, PVAccess> pvMap = new EnumMap<>(PandaPvName.class);
	private Map<String, PVAccess> extraPvMap = new HashMap<>();

	private String basePvName = "";

	private double monitorTimeoutSecs = 10;

	private HdfDataProvider hdfDataProvider = new HdfDataProvider();
	private SocketDataProvider socketDataProvider = new SocketDataProvider();
	private FrameNumberDataProvider frameNumberDataProvider = new FrameNumberDataProvider();
	private PandaDataProvider dataProvider = socketDataProvider;

	private List<String> dataNames = Collections.emptyList();

	public enum DataSourceType {None, HdfFile, DataSocket}


	public void setDataSource(DataSourceType dataSource) {
		dataProvider = switch(dataSource) {
			case DataSourceType.None -> frameNumberDataProvider;
			case DataSourceType.HdfFile -> hdfDataProvider;
			case DataSourceType.DataSocket -> socketDataProvider;
		};
	}

	public void setDataSource(String dataSourceString) {
		setDataSource(DataSourceType.valueOf(dataSourceString));
	}

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
	public void reconfigure() throws FactoryException {
		setConfigured(false);
		disconnect();
		configure();
	}

	private void disconnect() throws FactoryException {
		for(var pv : pvMap.values()) {
			pv.disconnect();
		}
		try {
			socketDataProvider.disconnectDataSocket();
		} catch (IOException e) {
			throw new FactoryException("Problem disconnecting Panda datasocket", e);
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
		return getPv(PandaPvName.SEQ_TABLE_LINE).getValue(Double.class).intValue();
	}

	@Override
	public int getSeqTableLineRepeat() throws DeviceException {
		return getPv(PandaPvName.SEQ_LINE_REPEAT).getValue(Double.class).intValue();
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

	@Override
	public double[] readData(int frameIndex) throws DeviceException {
		return dataProvider.readData(frameIndex);
	}

	@Override
	public double[][] readData(int startFrame, int finalFrame) throws DeviceException {
		return dataProvider.readData(startFrame, finalFrame);
	}

	private static interface PandaDataProvider {
		double[] readData(int frameIndex) throws DeviceException;
		double[][] readData(int startFrame, int finalFrame) throws DeviceException;
		String[] getExtraNames();
		String[] getOutputFormat();
		void setDataNames(List<String> dataNames);
	}

	private class FrameNumberDataProvider implements PandaDataProvider {

		@Override
		public double[] readData(int frameIndex) throws DeviceException {
			return new double[] {1};
		}

		@Override
		public double[][] readData(int startFrame, int finalFrame) throws DeviceException {
			return IntStream.range(startFrame, finalFrame+1)
					.mapToObj(val -> new double[] {val})
					.toList()
					.toArray(new double[][] {});
		}

		@Override
		public String[] getExtraNames() {
			return new String[] {"frame"};
		}

		@Override
		public String[] getOutputFormat() {
			return new String[] {"%.0f"};
		}

		@Override
		public void setDataNames(List<String> dataNames) {
			// dataNames not required.
		}
	}

	private static class SocketDataProvider implements PandaDataProvider {
		private DataSocket dataSocket;
		private String socketIpAddress = "bl20j-ts-panda-02";
		private int socketPort = 8889;
		private int socketRetries = 10;
		private String dataFormatString = "%5.5g";
		private List<String> dataNames;

		private void prepareSocket() throws IOException {
			if (dataSocket == null) {
				dataSocket = new DataSocket(socketIpAddress, socketPort);
			}
			dataSocket.clearData();
		}

		public void disconnectDataSocket() throws IOException {
			if (dataSocket==null) {
				return;
			}
			dataSocket.disconnect();
			dataSocket = null;
		}

		@Override
		public double[] readData(int frameIndex) throws DeviceException {
			try {
				if (frameIndex == 0) {
					prepareSocket();
				}
				logger.debug("Reading frame {}", frameIndex);
				dataSocket.waitForNumFrames(frameIndex, socketRetries);
				return dataSocket.getFrame(frameIndex, dataNames);
			} catch (IOException | InterruptedException e) {
				throw new DeviceException(e);
			}
		}

		@Override
		public double[][] readData(int startFrame, int finalFrame) throws DeviceException {
			try {
				logger.debug("Reading frames : {} ...  {}", startFrame, finalFrame);
				if (startFrame == 0) {
					prepareSocket();
				}
				dataSocket.updateValueData();
				dataSocket.waitForNumFrames(finalFrame, socketRetries);
				List<double[]> socketData = dataSocket.getFrames(startFrame, finalFrame, dataNames);
				return socketData.toArray(new double[][] {});
			} catch (IOException | InterruptedException e) {
				throw new DeviceException(e);
			}
		}

		@Override
		public String[] getExtraNames() {
			return dataNames.toArray(new String[] {});
		}

		@Override
		public String[] getOutputFormat() {
			return Collections.nCopies(dataNames.size(), dataFormatString).toArray(new String[] {});
		}

		@Override
		public void setDataNames(List<String> dataNames) {
			this.dataNames = dataNames;
		}
	}

	private class HdfDataProvider implements PandaDataProvider {

		private PandaHdfReader hdfReader = new PandaHdfReader();
		private List<String> dataNames;

		/**
		 * Connect the hdfReader to current Hdf file
		 * @throws DeviceException
		 */
		private void connectHdfFile() throws DeviceException {
			String filename=getHdfFileFullPath();
			logger.info("Setting up Swmr file reader for {}", filename);

			try {
				hdfReader.close();
			} catch (ScanFileHolderException e) {
				throw new DeviceException("Problem closeing hdf file", e);
			}

			hdfReader.setFilename(filename);
			hdfReader.setDataNames(dataNames);
			hdfReader.connect();
		}

		@Override
		public double[] readData(int frameIndex) throws DeviceException {
			// num captured frames need to be frameIndex + 1
			if (!waitForHdfNumCaptured(frameIndex+1)) {
				logger.warn("Problem waiting for Hdf writer flush frames : expected {} frames, but received {}", frameIndex, getHdfNumCaptured());
			}

			if (frameIndex==0) {
				connectHdfFile();
			}

			try {
				return hdfReader.readData(frameIndex);
			} catch (NexusException e) {
				throw new DeviceException(e);
			}
		}

		@Override
		public double[][] readData(int startFrame, int finalFrame) throws DeviceException{
			try {
				if (startFrame == 0) {
					connectHdfFile();
				}

				hdfReader.waitForFrames(finalFrame);
				List<double[]> data = hdfReader.readData(startFrame, finalFrame);
				int nFrames = data.get(0).length;
				double[][] frameData = new double[nFrames][data.size()];

				for(int i=0; i<data.size(); i++) {
					var itemData = data.get(i);
					for(int frame=0; frame<itemData.length; frame++) {
						frameData[frame][i] = itemData[frame];
					}
				}
				return frameData;
			} catch (NexusException ne) {
				throw new DeviceException("Problem reading out frames " + startFrame + " to " + finalFrame);
			}
		}

		@Override
		public
		String[] getExtraNames() {
			return hdfReader.getOutputNames();
		}
		@Override
		public String[] getOutputFormat() {
			return hdfReader.getOutputFormat();
		}

		@Override
		public void setDataNames(List<String> dataNames) {
			this.dataNames = dataNames;
		}
	}

	/**
	 * Set the dataNames to match all available ones present in the data socket stream
	 * e.g. do this after running a scan, so that subsequent scans readout all the data.
	 */
	public void updateDataNamesFromStream() {
		this.dataNames = socketDataProvider.dataSocket.fieldNames();
	}

	/**
	 * Set the names of the data to be read from Hdf file/data socket during a scan
	 * @param dataNames
	 */
	public void setDataNames(List<String> dataNames) {
		this.dataNames = dataNames;
		Stream.of(dataProvider, hdfDataProvider).forEach(p -> p.setDataNames(dataNames));
	}

	public List<String> getDataNames() {
		return dataNames;
	}

	@Override
	public String[] getOutputNames() {
		return dataProvider.getExtraNames();
	}

	@Override
	public String[] getOutputFormat() {
		return dataProvider.getOutputFormat();
	}

	public void setSocketIpAddress(String address) throws IOException {
		socketDataProvider.socketIpAddress = address;

		// disconnect, so new connection is made next time try to read data
		socketDataProvider.disconnectDataSocket();
	}

}
