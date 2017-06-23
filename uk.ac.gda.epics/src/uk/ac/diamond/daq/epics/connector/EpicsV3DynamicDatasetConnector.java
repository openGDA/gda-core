/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.epics.connector;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;

import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Connects to a remote Epics V3 Array, and uses the data to populate a dataset which dynamically changes whenever the Epics data changes.
 *
 * @author Matt Taylor
 */
public class EpicsV3DynamicDatasetConnector implements IDatasetConnector {

	private static final Logger logger = LoggerFactory.getLogger(EpicsV3DynamicDatasetConnector.class);

	private static final EpicsController EPICS_CONTROLLER = EpicsController.getInstance();

	private static final String ARRAY_DATA_SUFFIX = ":ArrayData";
	private static final String ARRAY_SIZE_0_SUFFIX = ":ArraySize0_RBV";
	private static final String ARRYA_SIZE_1_SUFFIX = ":ArraySize1_RBV";
	private static final String ARRAY_SIZE_2_SUFFIX = ":ArraySize2_RBV";
	private static final String NUMBER_OF_DIMENSIONS_SUFFIX = ":NDimensions_RBV";
	private static final String COLOUR_MODE_SUFFIX = ":ColorMode_RBV";
	private static final String DATA_TYPE_SUFFIX = ":DataType_RBV";
	private static final String MONO_COLOUR_MODE = "Mono";
	private static final String UINT8_DATA_TYPE = "UInt8";
	private static final long FRAME_LIMIT_TIME_DIFFERENCE= 1000 / 20; // 20 FPS

	private Channel dataChannel = null;
	private Channel dim0Ch = null;
	private Channel dim1Ch = null;
	private Channel dim2Ch = null;
	private Channel numDimCh = null;
	private Channel colourModeCh = null;

	private Monitor dataChannelMonitor = null;
	private EpicsMonitorListener dataChannelMonitorListener = null;

	private final LinkedList<IDataListener> listeners = new LinkedList<>();

	private int dim0;
	private int dim1;
	private int dim2;
	private int height = 0;
	private int width = 0;
	private int rgbChannels = 0;
	private int numDimensions = 0;
	private String colourMode = "";
	private String dataTypeStr = "";

	private final String dataChannelPV;
	private final String dim0PV;
	private final String dim1PV;
	private final String dim2PV;
	private final String numDimensionsPV;
	private final String colourModePV;
	private final String dataTypePV;

	private DBRType dataType = null;
	private long lastSystemTime = System.currentTimeMillis();
	private ILazyDataset dataset;

	/**
	 * Constructor, takes the name of the base plugin name
	 *
	 * @param arrayPluginName
	 *            The name of the 'parent' PV endpoint
	 */
	public EpicsV3DynamicDatasetConnector(String arrayPluginName) {
		this.dataChannelPV = arrayPluginName + ARRAY_DATA_SUFFIX;
		this.dim0PV = arrayPluginName + ARRAY_SIZE_0_SUFFIX;
		this.dim1PV = arrayPluginName + ARRYA_SIZE_1_SUFFIX;
		this.dim2PV = arrayPluginName + ARRAY_SIZE_2_SUFFIX;
		this.numDimensionsPV = arrayPluginName + NUMBER_OF_DIMENSIONS_SUFFIX;
		this.colourModePV = arrayPluginName + COLOUR_MODE_SUFFIX;
		this.dataTypePV = arrayPluginName + DATA_TYPE_SUFFIX;
	}

	@Override
	public String getPath() {
		// Not applicable
		return null;
	}

	@Override
	public void setPath(String path) {
		// Not applicable
	}

	@Override
	public int[] getMaxShape() {
		// TODO applicable?
		return null;
	}

	@Override
	public void setMaxShape(int... maxShape) {
		// TODO applicable?
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		// TODO applicable?
	}

	@Override
	public void addDataListener(IDataListener l) {
		listeners.add(l);
	}

	@Override
	public void removeDataListener(IDataListener l) {
		listeners.remove(l);
	}

	@Override
	public void fireDataListeners() {
		// TODO Auto-generated method stub
	}

	@Override
	public String getDatasetName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDatasetName(String datasetName) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isWritingExpected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String connect() throws DatasetException {
		return connect(500, TimeUnit.MILLISECONDS);
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		try {
			dataChannel = EPICS_CONTROLLER.createChannel(dataChannelPV);
			dim0Ch = EPICS_CONTROLLER.createChannel(dim0PV);
			dim1Ch = EPICS_CONTROLLER.createChannel(dim1PV);
			dim2Ch = EPICS_CONTROLLER.createChannel(dim2PV);
			numDimCh = EPICS_CONTROLLER.createChannel(numDimensionsPV);
			colourModeCh = EPICS_CONTROLLER.createChannel(colourModePV);
			Channel dataTypeCh = EPICS_CONTROLLER.createChannel(dataTypePV);

			dataType = dataChannel.getFieldType();
			dim0 = EPICS_CONTROLLER.cagetInt(dim0Ch);
			dim1 = EPICS_CONTROLLER.cagetInt(dim1Ch);
			dim2 = EPICS_CONTROLLER.cagetInt(dim2Ch);
			numDimensions = EPICS_CONTROLLER.cagetInt(numDimCh);
			colourMode = EPICS_CONTROLLER.cagetString(colourModeCh);
			dataTypeStr = EPICS_CONTROLLER.cagetString(dataTypeCh);

			int dataSize = calculateAndUpdateDataSize();
			// Without specifying data size in cagets, they will always try to get max data size, which could be >> actual data, causing timeouts.

			DBR dbr = dataChannel.get(dataType, dataSize);

			if (dataType.equals(DBRType.BYTE)) {
				EPICS_CONTROLLER.cagetByteArray(dataChannel, dataSize); // Without doing this, the dbr isn't populated with the actual data
				handleByte(dbr);
			} else if (dataType.equals(DBRType.SHORT)) {
				EPICS_CONTROLLER.cagetShortArray(dataChannel, dataSize); // Without doing this, the dbr isn't populated with the actual data
				handleShort(dbr);
			} else if (dataType.equals(DBRType.INT)) {
				EPICS_CONTROLLER.cagetIntArray(dataChannel, dataSize); // Without doing this, the dbr isn't populated with the actual data
				handleInt(dbr);
			} else if (dataType.equals(DBRType.FLOAT)) {
				EPICS_CONTROLLER.cagetFloatArray(dataChannel, dataSize); // Without doing this, the dbr isn't populated with the actual data
				handleFloat(dbr);
			} else if (dataType.equals(DBRType.DOUBLE)) {
				EPICS_CONTROLLER.cagetDoubleArray(dataChannel, dataSize); // Without doing this, the dbr isn't populated with the actual data
				handleDouble(dbr);
			} else {
				logger.error("Unknown DBRType - " + dataType);
			}

			dataChannelMonitorListener = new EpicsMonitorListener();
			dataChannelMonitor = EPICS_CONTROLLER.setMonitor(dataChannel, dataChannelMonitorListener, dataSize);
			EPICS_CONTROLLER.setMonitor(dim0Ch, new EpicsMonitorListener());
			EPICS_CONTROLLER.setMonitor(dim1Ch, new EpicsMonitorListener());
			EPICS_CONTROLLER.setMonitor(dim2Ch, new EpicsMonitorListener());
			EPICS_CONTROLLER.setMonitor(colourModeCh, new EpicsMonitorListener());
			EPICS_CONTROLLER.setMonitor(numDimCh, new EpicsMonitorListener());
		} catch (Exception e) {
			logger.error("Error connecting to EPICS stream", e);
			throw new DatasetException(e);
		}
		return null;
	}

	@Override
	public void disconnect() throws DatasetException {
		if (null != dataChannel) {
			EPICS_CONTROLLER.destroy(dataChannel);
		}
		if (null != dim0Ch) {
			EPICS_CONTROLLER.destroy(dim0Ch);
		}
		if (null != dim1Ch) {
			EPICS_CONTROLLER.destroy(dim1Ch);
		}
		if (null != dim2Ch) {
			EPICS_CONTROLLER.destroy(dim2Ch);
		}
		if (null != colourModeCh) {
			EPICS_CONTROLLER.destroy(colourModeCh);
		}
		if (null != numDimCh) {
			EPICS_CONTROLLER.destroy(numDimCh);
		}
	}

	@Override
	public ILazyDataset getDataset() {
		return dataset;
	}

	@Override
	public boolean resize(int... newShape) {
		// TODO ?
		return false;
	}

	@Override
	public boolean refreshShape() {
		// TODO ?
		return false;
	}

	private int calculateAndUpdateDataSize() {
		String currentWidthPV;
		String currentHeightPV;
		String rgbChannelsPV;

		if (colourMode.equals(MONO_COLOUR_MODE)) {

			width = dim0;
			height = dim1;
			rgbChannels = 1;

			currentWidthPV = dim0PV;
			currentHeightPV = dim1PV;
			rgbChannelsPV = dim2PV;

		} else {
			if (colourMode.equals("RGB2")) {

				width = dim0;
				height = dim2;
				rgbChannels = dim1;

				currentWidthPV = dim0PV;
				currentHeightPV = dim2PV;
				rgbChannelsPV = dim1PV;

			} else if (colourMode.equals("RGB3")) {

				width = dim0;
				height = dim1;
				rgbChannels = dim2;

				currentWidthPV = dim0PV;
				currentHeightPV = dim1PV;
				rgbChannelsPV = dim2PV;

			} else {

				width = dim1;
				height = dim2;
				rgbChannels = dim0;

				currentWidthPV = dim1PV;
				currentHeightPV = dim2PV;
				rgbChannelsPV = dim0PV;
			}
		}
		// Ensure that we never return a data size less than 1.

		// EPICS takes a data size of 0 as maximum possible size, which causes it to ask for more data than is available.

		if (width <= 1) {
			logger.warn("Image width {} from {}, assuming a width of at least 1. Check that plugin is receiving images.", width, currentWidthPV);
			width = 1;
		}
		if (height <= 1) {
			logger.warn("Image height {} from {}, assuming a height of at least 1. Check that plugin is receiving images.", height, currentHeightPV);
			height = 1;
		}
		if (rgbChannels < 1) {
			logger.warn("Image channels {} from {}, assuming a at least 1 channel. Check that plugin is receiving images.", rgbChannels, rgbChannelsPV);
			rgbChannels = 1;
		}
		return height * width * rgbChannels;
	}

	/**
	 * Handles a byte DBR, updating the dataset with the data from the DBR
	 *
	 * @param dbr
	 */
	private void handleByte(DBR dbr) {

		DBR_Byte dbrb = (DBR_Byte) dbr;
		byte[] rawData = dbrb.getByteValue();
		short[] latestData = new short[rawData.length];

		if (dataTypeStr.equalsIgnoreCase(UINT8_DATA_TYPE)) {
			for (int i = 0; i < rawData.length; i++) {
				latestData[i] = (short) (rawData[i] & 0xFF);
			}
		} else {
			for (int i = 0; i < rawData.length; i++) {
				latestData[i] = rawData[i];
			}
		}

		int dataSize = calculateAndUpdateDataSize();

		if (latestData.length != dataSize) {
			if (dataSize > latestData.length) {
				logger.warn("Warning: Image size is larger than data array size");
			}
			latestData = Arrays.copyOf(latestData, dataSize);
		}
		if (numDimensions == 2) {
			dataset = DatasetFactory.createFromObject(latestData, new int[] { height, width });
		} else {
			dataset = DatasetFactory.createFromObject(RGBDataset.class, latestData, new int[] { height, width });
		}
	}

	/**
	 * Handles a short DBR, updating the dataset with the data from the DBR
	 *
	 * @param dbr
	 */
	private void handleShort(DBR dbr) {

		DBR_Short dbrb = (DBR_Short) dbr;
		short[] latestData = Arrays.copyOf(dbrb.getShortValue(), dbrb.getShortValue().length);

		if (latestData != null) {
			int dataSize = calculateAndUpdateDataSize();

			if (latestData.length != dataSize) {
				if (dataSize > latestData.length) {
					logger.warn("Warning: Image size is larger than data array size");
				}
				latestData = Arrays.copyOf(latestData, dataSize);
			}
			if (numDimensions == 2) {
				dataset = DatasetFactory.createFromObject(latestData, new int[] { height, width });
			} else {
				dataset = DatasetFactory.createFromObject(RGBDataset.class, latestData, new int[] { height, width });
			}
		}
	}

	/**
	 * Handles an int DBR, updating the dataset with the data from the DBR
	 *
	 * @param dbr
	 */
	private void handleInt(DBR dbr) {

		DBR_Int dbrb = (DBR_Int) dbr;
		int[] latestData = Arrays.copyOf(dbrb.getIntValue(), dbrb.getIntValue().length);

		if (latestData != null) {
			int dataSize = calculateAndUpdateDataSize();

			if (latestData.length != dataSize) {
				if (dataSize > latestData.length) {
					logger.warn("Warning: Image size is larger than data array size");
				}
				latestData = Arrays.copyOf(latestData, dataSize);
			}
			if (numDimensions == 2) {
				dataset = DatasetFactory.createFromObject(latestData, new int[] { height, width });
			} else {
				dataset = DatasetFactory.createFromObject(RGBDataset.class, latestData, new int[] { height, width });
			}
		}
	}

	/**
	 * Handles a float DBR, updating the dataset with the data from the DBR
	 *
	 * @param dbr
	 */
	private void handleFloat(DBR dbr) {

		DBR_Float dbrb = (DBR_Float) dbr;
		float[] latestData = Arrays.copyOf(dbrb.getFloatValue(), dbrb.getFloatValue().length);

		if (latestData != null) {
			int dataSize = calculateAndUpdateDataSize();

			if (latestData.length != dataSize) {
				if (dataSize > latestData.length) {
					logger.warn("Warning: Image size is larger than data array size");
				}
				latestData = Arrays.copyOf(latestData, dataSize);
			}
			if (numDimensions == 2) {
				dataset = DatasetFactory.createFromObject(latestData, new int[] { height, width });
			} else {
				dataset = DatasetFactory.createFromObject(RGBDataset.class, latestData, new int[] { height, width });
			}
		}
	}

	/**
	 * Handles a double DBR, updating the dataset with the data from the DBR
	 *
	 * @param dbr
	 */
	private void handleDouble(DBR dbr) {

		DBR_Double dbrb = (DBR_Double) dbr;
		double[] latestData = Arrays.copyOf(dbrb.getDoubleValue(), dbrb.getDoubleValue().length);

		if (latestData != null) {
			int dataSize = calculateAndUpdateDataSize();

			if (latestData.length != dataSize) {
				if (dataSize > latestData.length) {
					logger.warn("Warning: Image size is larger than data array size");
				}
				latestData = Arrays.copyOf(latestData, dataSize);
			}
			if (numDimensions == 2) {
				dataset = DatasetFactory.createFromObject(latestData, new int[] { height, width });
			} else {
				dataset = DatasetFactory.createFromObject(RGBDataset.class, latestData, new int[] { height, width });
			}
		}
	}

	/**
	 * Private class used to perform actions based on events sent from the Epics PVs
	 */
	private class EpicsMonitorListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			try {
				Object source = arg0.getSource();

				if (source instanceof CAJChannel) {
					CAJChannel chan = (CAJChannel) source;
					String channelName = chan.getName();
					DBR dbr = arg0.getDBR();

					if (channelName.equalsIgnoreCase(dataChannelPV)) {
						if (dataType.equals(DBRType.BYTE)) {
							handleByte(dbr);
						} else if (dataType.equals(DBRType.SHORT)) {
							handleShort(dbr);
						} else if (dataType.equals(DBRType.INT)) {
							handleInt(dbr);
						} else if (dataType.equals(DBRType.FLOAT)) {
							handleFloat(dbr);
						} else if (dataType.equals(DBRType.DOUBLE)) {
							handleDouble(dbr);
						} else {
							logger.error("Unknown DBRType - " + dataType);
						}

						// Only notify of data update at certain FPS
						long timeNow = System.currentTimeMillis();
						if (timeNow - lastSystemTime > FRAME_LIMIT_TIME_DIFFERENCE) {
							for (IDataListener listener : listeners) {
								int[] shape = new int[] { height, width };
								DataEvent evt = new DataEvent("", shape);
								listener.dataChangePerformed(evt);
							}
							lastSystemTime = timeNow;
						}
					} else if (channelName.equalsIgnoreCase(dim0PV)) {
						DBR_Int dbri = (DBR_Int) dbr;
						int value = dbri.getIntValue()[0];
						if (dim0 != value) {
							dim0 = value;
							logger.debug("New dim0PV value {} for {}", value, dim0PV);
							updateDataChannelMonitor();
						}
					} else if (channelName.equalsIgnoreCase(dim1PV)) {
						DBR_Int dbri = (DBR_Int) dbr;
						int value = dbri.getIntValue()[0];
						if (dim1 != value) {
							dim1 = value;
							logger.debug("New dim1PV value {} for {}", value, dim1PV);
							updateDataChannelMonitor();
						}
					} else if (channelName.equalsIgnoreCase(dim2PV)) {
						DBR_Int dbri = (DBR_Int) dbr;
						int value = dbri.getIntValue()[0];
						if (dim2 != value) {
							dim2 = value;
							logger.debug("New dim2PV value {} for {}", value, dim2PV);
							updateDataChannelMonitor();
						}
					} else if (channelName.equalsIgnoreCase(numDimensionsPV)) {
						DBR_Int dbri = (DBR_Int) dbr;
						int value = dbri.getIntValue()[0];
						if (numDimensions != value) {
							numDimensions = value;
							logger.debug("New numDimensionsPV value {} from {}", value, numDimensionsPV);
							updateDataChannelMonitor();
						}
					} else if (channelName.equalsIgnoreCase(colourModePV)) {
						DBR_String dbrs = (DBR_String) dbr;
						String value = dbrs.getStringValue()[0];
						if (colourMode != value) {
							colourMode = value;
							logger.debug("New colourModePV value '{} 'from {}", value, colourModePV);
							updateDataChannelMonitor();
						}
					} else {
						logger.debug("New value from {}, ignoring: {}", channelName, dbr);
					}
				}
			} catch (Exception e) {
				logger.error("Error handling update from EPICS", e);
			}
		}

		private void updateDataChannelMonitor() throws CAException, InterruptedException {
			int[] were = { height, width, rgbChannels };
			dataChannelMonitor.removeMonitorListener(dataChannelMonitorListener);
			int dataSize = calculateAndUpdateDataSize();
			logger.debug("New value for height {} width {} channels {} numDimensions {} or colourMode {} " + "(height, width & channels were {})", height,
					width, rgbChannels, numDimensions, colourMode, were);
			dataChannelMonitor = EPICS_CONTROLLER.setMonitor(dataChannel, dataChannelMonitorListener, dataSize);
		}
	}
}
