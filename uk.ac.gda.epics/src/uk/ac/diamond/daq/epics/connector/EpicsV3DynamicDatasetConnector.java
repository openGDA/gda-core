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

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import org.eclipse.january.DatasetException;
import org.eclipse.january.dataset.DataEvent;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataListener;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IDatasetChangeChecker;
import org.eclipse.january.dataset.IDatasetConnector;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cosylab.epics.caj.CAJChannel;
import com.google.common.util.concurrent.RateLimiter;

import gda.epics.connection.EpicsChannelManager;
import gda.epics.connection.EpicsController;
import gov.aps.jca.CAException;
import gov.aps.jca.Channel;
import gov.aps.jca.Monitor;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Enum;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

/**
 * Connects to a remote Epics V3 Array, and uses the data to populate a dataset which dynamically changes whenever the Epics data changes.
 * <p>
 * This class was significantly rewritten as part of <a href="https://jira.diamond.ac.uk/browse/DAQ-1255">DAQ-1255</a>
 *
 * @author Matt Taylor
 * @author James Mudd
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

	private final RateLimiter frameRateLimiter = RateLimiter.create(20);

	private final EpicsChannelManager channelManager = new EpicsChannelManager();

	private Channel dataChannel = null;
	private Channel dim0Ch = null;
	private Channel dim1Ch = null;
	private Channel dim2Ch = null;
	private Channel numDimCh = null;
	private Channel colourModeCh = null;

	private Monitor dataChannelMonitor = null;
	private final MonitorListener dataChannelMonitorListener = this::dataUpdate;
	private final MonitorListener arrayParametersMonitorListener = this::arrayParametersUpdate;

	private final Set<IDataListener> listeners = new CopyOnWriteArraySet<>();

	private int dim0;
	private int dim1;
	private int dim2;
	private int height = 0;
	private int width = 0;
	private int rgbChannels = 0;
	private int dataSize; // = height * width * rgbChannels
	private int numDimensions = 0;
	private String colourMode = "";
	private String dataTypeStr = "";

	private final String arrayBasePv;
	private final String dataChannelPV;
	private final String dim0PV;
	private final String dim1PV;
	private final String dim2PV;
	private final String numDimensionsPV;
	private final String colourModePV;
	private final String dataTypePV;

	private IDataset dataset;
	private String[] colourModes;

	private Monitor dim0ChMonitor;
	private Monitor dim1ChMonitor;
	private Monitor dim2ChMonitor;
	private Monitor colourModeChMonitor;
	private Monitor numDimChMonitor;

	/**
	 * Constructor, takes the name of the base plugin name
	 *
	 * @param arrayBasePv
	 *            The name of the 'parent' PV endpoint
	 */
	public EpicsV3DynamicDatasetConnector(String arrayBasePv) {
		this.arrayBasePv = arrayBasePv;
		this.dataChannelPV = arrayBasePv + ARRAY_DATA_SUFFIX;
		this.dim0PV = arrayBasePv + ARRAY_SIZE_0_SUFFIX;
		this.dim1PV = arrayBasePv + ARRYA_SIZE_1_SUFFIX;
		this.dim2PV = arrayBasePv + ARRAY_SIZE_2_SUFFIX;
		this.numDimensionsPV = arrayBasePv + NUMBER_OF_DIMENSIONS_SUFFIX;
		this.colourModePV = arrayBasePv + COLOUR_MODE_SUFFIX;
		this.dataTypePV = arrayBasePv + DATA_TYPE_SUFFIX;
	}

	@Override
	public String getPath() {
		// Not applicable
		return null;
	}

	@Override
	public void setPath(String path) {
		throw new UnsupportedOperationException("Cannot setPath on a EPICS stream");
	}

	@Override
	public int[] getMaxShape() {
		// Return null
		return null;
	}

	@Override
	public void setMaxShape(int... maxShape) {
		throw new UnsupportedOperationException("Cannot setMaxShape on a EPICS stream");
	}

	@Override
	public void startUpdateChecker(int milliseconds, IDatasetChangeChecker checker) {
		throw new UnsupportedOperationException("EPICS stream does not support update checker");
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
		final DataEvent dataEvent = new DataEvent(getDatasetName(), getDataShape());
		listeners.forEach(listener -> listener.dataChangePerformed(dataEvent));
	}

	@Override
	public String getDatasetName() {
		return arrayBasePv;
	}

	@Override
	public void setDatasetName(String datasetName) {
		throw new UnsupportedOperationException("Cannot setDatasetName on a EPICS stream");
	}

	@Override
	public void setWritingExpected(boolean expectWrite) {
		throw new UnsupportedOperationException("Cant write to an EPICS stream");
	}

	@Override
	public boolean isWritingExpected() {
		return false;
	}

	@Override
	public String connect() throws DatasetException {
		return connect(1, TimeUnit.SECONDS);
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		logger.debug("Connecting to EPICS Array at '{}'", dataChannelPV);
		try {
			dataChannel = channelManager.createChannel(dataChannelPV);
			dim0Ch = channelManager.createChannel(dim0PV);
			dim1Ch = channelManager.createChannel(dim1PV);
			dim2Ch = channelManager.createChannel(dim2PV);
			numDimCh = channelManager.createChannel(numDimensionsPV);
			colourModeCh = channelManager.createChannel(colourModePV);
			Channel dataTypeCh = channelManager.createChannel(dataTypePV);
			// Wait for the channels to be ready
			channelManager.tryInitialize(unit.toMillis(time));

			dim0 = EPICS_CONTROLLER.cagetInt(dim0Ch);
			dim1 = EPICS_CONTROLLER.cagetInt(dim1Ch);
			dim2 = EPICS_CONTROLLER.cagetInt(dim2Ch);
			numDimensions = EPICS_CONTROLLER.cagetInt(numDimCh);
			colourMode = EPICS_CONTROLLER.cagetString(colourModeCh);
			dataTypeStr = EPICS_CONTROLLER.cagetString(dataTypeCh);

			colourModes = EPICS_CONTROLLER.cagetLabels(colourModeCh);

			calculateAndUpdateDataSize();

			// Initialise the dataset to be zeros. This is needed so when the trace is added to the plotting system its actually drawn.
			dataset = DatasetFactory.zeros(getDataShape());

			dataChannelMonitor = EPICS_CONTROLLER.setMonitor(dataChannel, dataChannelMonitorListener, dataSize);
			dim0ChMonitor = EPICS_CONTROLLER.setMonitor(dim0Ch, arrayParametersMonitorListener);
			dim1ChMonitor = EPICS_CONTROLLER.setMonitor(dim1Ch, arrayParametersMonitorListener);
			dim2ChMonitor = EPICS_CONTROLLER.setMonitor(dim2Ch, arrayParametersMonitorListener);
			colourModeChMonitor = EPICS_CONTROLLER.setMonitor(colourModeCh, arrayParametersMonitorListener);
			numDimChMonitor = EPICS_CONTROLLER.setMonitor(numDimCh, arrayParametersMonitorListener);
		} catch (Exception e) {
			logger.error("Error connecting to EPICS stream", e);
			throw new DatasetException(e);
		}
		logger.info("Connected to EPICS Array at '{}'", dataChannelPV);
		return null; // No thread is used to "run" the connection
	}

	@Override
	public void disconnect() throws DatasetException {
		logger.debug("Disconnecting...");
		// Remove listeners to stop sending updates
		listeners.clear();

		if (null != dataChannelMonitor) {
			dataChannelMonitor.removeMonitorListener(dataChannelMonitorListener);
			dataChannelMonitor = null;
		}
		if (null != dim0ChMonitor) {
			dim0ChMonitor.removeMonitorListener(arrayParametersMonitorListener);
			dim0ChMonitor = null;
		}
		if (null != dim1ChMonitor) {
			dim1ChMonitor.removeMonitorListener(arrayParametersMonitorListener);
			dim1ChMonitor = null;
		}
		if (null != dim2ChMonitor) {
			dim2ChMonitor.removeMonitorListener(arrayParametersMonitorListener);
			dim2ChMonitor = null;
		}
		if (null != colourModeChMonitor) {
			colourModeChMonitor.removeMonitorListener(arrayParametersMonitorListener);
			colourModeChMonitor = null;
		}
		if (null != numDimChMonitor) {
			numDimChMonitor.removeMonitorListener(arrayParametersMonitorListener);
			numDimChMonitor = null;
		}

		// Destroy all the channels
		channelManager.destroy();
		logger.info("Disconnected EPICS array stream '{}'", dataChannelPV);
	}

	@Override
	public ILazyDataset getDataset() {
		return dataset;
	}

	@Override
	public boolean resize(int... newShape) {
		throw new UnsupportedOperationException("Cant resize an EPICS stream");
	}

	@Override
	public boolean refreshShape() {
		return false; // There is no file so false
	}

	private void calculateAndUpdateDataSize() {
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

		dataSize = height * width * rgbChannels;

		logger.debug("New values: height={} width={} channels={} numDimensions={} or colourMode={}", height, width, rgbChannels, numDimensions, colourMode);
	}

	private int[] getDataShape() {
		return new int[] { height, width };
	}

	/**
	 * This is the method called by EPICS when the monitor on the {@link #ARRAY_DATA_SUFFIX} is triggered. It converts the event into a dataset and fires
	 * listeners
	 *
	 * @param event The EPICS monitor update with the new data
	 */
	private void dataUpdate(MonitorEvent event) {
		// Only notify of data update at certain FPS
		if (frameRateLimiter.tryAcquire()) {
			// Extract the updated data
			final Object data = event.getDBR().getValue();

			// Build the new dataset
			if (isRgb()) {
				dataset = DatasetFactory.createFromObject(RGBDataset.class, data, getDataShape());
			} else {
				dataset = DatasetFactory.createFromObject(data, getDataShape());
			}
			if (isUnsigned()) {
				dataset = DatasetUtils.makeUnsigned(dataset);
			}

			// Update listeners
			fireDataListeners();
		} else {
			logger.trace("Frame dropped");
		}
	}

	private boolean isRgb() {
		return !MONO_COLOUR_MODE.equalsIgnoreCase(colourMode);
	}

	private boolean isUnsigned() {
		return UINT8_DATA_TYPE.equalsIgnoreCase(dataTypeStr);
	}

	/**
	 * This is called by EPICS when one of the monitors of array parameters is triggered. It logs it and updated the data monitor
	 *
	 * @param event THe EPICS monitor event when an parameter is changed
	 */
	private void arrayParametersUpdate(MonitorEvent event) {

		final Object source = event.getSource();
		final DBR dbr = event.getDBR();

		if (source instanceof CAJChannel) {
			CAJChannel chan = (CAJChannel) source;
			String channelName = chan.getName();

			if (channelName.equalsIgnoreCase(dim0PV)) {
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
				DBR_Enum dbrs = (DBR_Enum) dbr;
				short value = dbrs.getEnumValue()[0];
				if (colourMode != colourModes[value]) {
					colourMode = colourModes[value];
					logger.debug("New colourModePV value '{} 'from {}", colourModes[value], colourMode);
					updateDataChannelMonitor();
				}
			} else {
				logger.debug("New value from {}, ignoring: {}", channelName, dbr);
			}
		}
	}

	/**
	 * This removes the existing monitor (if any), then recalculates the data size (this is important as we want to attach a monitor of the right number of
	 * elements) then re-adds the listener with the new data size.
	 */
	private void updateDataChannelMonitor() {
		dataChannelMonitor.removeMonitorListener(dataChannelMonitorListener);
		calculateAndUpdateDataSize();
		try {
			dataChannelMonitor = EPICS_CONTROLLER.setMonitor(dataChannel, dataChannelMonitorListener, dataSize);
		} catch (CAException | InterruptedException e) {
			logger.error("Error attaching data channel monitor listener", e);
		}
	}

}
