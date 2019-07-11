/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
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
import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientMonitor;
import org.epics.pvaClient.PvaClientMonitorData;
import org.epics.pvaClient.PvaClientMonitorRequester;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.ArrayData;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.StructureArrayData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

import uk.ac.diamond.daq.concurrent.Async;

/**
 * Connects to an Epics V4 PVA (PV Access) plugin to populate a dataset which dynamically updates as array data and attributes change. This is similar in
 * structure to {@link EpicsV3DynamicDatasetConnector} however there are significant differences.
 * <p>
 * Updates are received by creating a monitor of the whole structure obtained at {@code pvaPv}. An attempt was made to create separate monitors for the data and
 * the dimension info however the dimension part of the structure would update even when only the data changed. Therefore both parts are updated when there is a
 * data event.
 * <p>
 * The polling is performed in a separate thread. There is an option to create a {@link PvaClientMonitor} with a {@link PvaClientMonitorRequester} which would
 * call a method automatically with new events however this often lead to RuntimeExceptions which would kill the PVA library threads when a fast running stream
 * was terminated. This would require restarting the application therefore the polling based approach using a reference to the {@code PvaClientMonitor} was
 * chosen.
 *
 * @author Joe Shannon
 * @since GDA 9.16
 */
public class EpicsV4DynamicDatasetConnector implements IDatasetConnector {

	private static final Logger logger = LoggerFactory.getLogger(EpicsV4DynamicDatasetConnector.class);

	/**
	 * PV Request. Empty 'field' gets the whole structure.
	 */
	private static final String MONITOR_REQUEST = "record[queueSize=1]field()";

	private static final PvaClient pva = PvaClient.get("pva");
	private static final Convert convert = ConvertFactory.getConvert();

	private int height = 0;
	private int width = 0;
	private int rgbChannels = 0;
	private IDataset dataset;
	private PvaClientChannel pvaClientChannel;
	private PvaClientMonitor pvaClientMonitor;
	private ScalarType dataType;
	private String pvName;
	private int colourMode;
	private int dataSize;
	private final RateLimiter frameRateLimiter = RateLimiter.create(20);
	private final Set<IDataListener> listeners = new CopyOnWriteArraySet<>();

	private volatile boolean monitorActive = false;

	public EpicsV4DynamicDatasetConnector(String pvName) {
		this.pvName = pvName;
	}

	@Override
	public String getPath() {
		// N/A
		return null;
	}

	@Override
	public void setPath(String path) {
		throw new UnsupportedOperationException("Cannot setPath on a EPICS stream");

	}

	@Override
	public ILazyDataset getDataset() {
		return dataset;
	}

	@Override
	public boolean resize(int... newShape) {
		throw new UnsupportedOperationException("Cannot resize an EPICS stream");
	}

	@Override
	public int[] getMaxShape() {
		return getDataShape();
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
	public boolean refreshShape() {
		return false; // There is no file so false
	}

	@Override
	public void addDataListener(IDataListener listener) {
		listeners.add(listener);

	}

	@Override
	public void removeDataListener(IDataListener listener) {
		listeners.remove(listener);

	}

	@Override
	public void fireDataListeners() {
		final DataEvent dataEvent = new DataEvent(getDatasetName(), getDataShape());
		listeners.forEach(listener -> listener.dataChangePerformed(dataEvent));
	}

	@Override
	public String getDatasetName() {
		return pvName;
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
		return connect(5, TimeUnit.SECONDS);
	}

	@Override
	public String connect(long time, TimeUnit unit) throws DatasetException {
		try {
			// Initialise the dataset. This is needed so when the trace is added to the plotting system it's actually drawn
			// SCI-8921 Must be of non 0 x 0 shape
			dataset = DatasetFactory.zeros(10, 10);

			if (pvaClientChannel != null) {
				throw new DatasetException("Channel: " + pvName + " already connected");
			}
			logger.debug("Trying to connect to : {} ", pvName);
			pvaClientChannel = pva.createChannel(pvName, "pva");
			pvaClientChannel.connect();

			// Create and connect to pvaClientMonitor
			pvaClientMonitor = pvaClientChannel.monitor(MONITOR_REQUEST);
			monitorActive = true;
			logger.info("Connected to {} and started client monitor", pvName);

			// Start poll loop
			Async.execute(this::pvaMonitorEventLoop);

		} catch (Exception ex) {
			logger.error("Could not connect to : {} {}", pvName, ex.getMessage());
			throw new DatasetException(ex);
		}
		return null;
	}

	/**
	 * Disconnect from the PVA stream. This requires synchronising to ensure that it does not stop the monitor whilst the polling loop is processing a data
	 * event. The calls are run in a separate thread because parts of the plotting/view control mechanism also lock on this instance.
	 */
	@Override
	public void disconnect() throws DatasetException {
		monitorActive = false;
		Async.execute(() -> {
			synchronized (this) {
				logger.info("Disconnecting from PVA");
				listeners.clear();

				if (pvaClientMonitor != null) {
					pvaClientMonitor.stop();
					pvaClientMonitor.destroy();
					pvaClientMonitor = null;
				}

				if (pvaClientChannel != null) {
					pvaClientChannel.destroy();
					pvaClientChannel = null;
				}
			}
		});
	}

	private int[] getDataShape() {
		return new int[] { height, width };
	}

	private void updateArrayAttributes(int[] dimensions, int colourMode) {

		this.colourMode = colourMode;
		int dim0 = dimensions[0];
		int dim1 = dimensions[1];
		int dim2;
		if (dimensions.length >= 3) {
			dim2 = dimensions[2];
		} else {
			dim2 = 0;
		}

		// ColourMode definitions at http://epics-pvdata.sourceforge.net/alpha/normativeTypes/normativeTypes.html
		if (colourMode == 0) {
			width = dim0;
			height = dim1;
			rgbChannels = 1;
		} else {
			if (colourMode == 3) {
				width = dim0;
				height = dim2;
				rgbChannels = dim1;
			} else if (colourMode == 4) {
				width = dim0;
				height = dim1;
				rgbChannels = dim2;
			} else { // colourMode is 2
				width = dim1;
				height = dim2;
				rgbChannels = dim0;
			}
		}
		dataSize = width * height * rgbChannels;
	}

	/**
	 * Extract an object out of the array and convert to dataset
	 */
	private void updateData(PVScalarArray imageDataArray) {
		dataType = imageDataArray.getScalarArray().getElementType();
		Object data = extractArray(imageDataArray);

		if (colourMode != 0) {
			dataset = DatasetFactory.createFromObject(RGBDataset.class, data, getDataShape());
		} else {
			dataset = DatasetFactory.createFromObject(data, getDataShape());
		}
		if (dataType.isUInteger()) {
			dataset = DatasetUtils.makeUnsigned(dataset);
		}
		fireDataListeners();
	}

	/**
	 * This isn't particularly nice however there doesn't seem to be a generic way to get array data based on type. Each {@link Convert} method requires a
	 * specific variable to exist and be passed in.
	 * <p>
	 * An alternative would be to use sub classes of {@link ArrayData} and {@link PVScalarArray} however this gives the same problem as there is no generic get
	 * method.
	 *
	 * @param imageDataArray
	 * @return
	 */
	private Object extractArray(PVScalarArray imageDataArray) {
		switch (dataType) {
		case pvByte:
		case pvUByte:
			byte[] byteArray = new byte[dataSize];
			convert.toByteArray(imageDataArray, 0, dataSize, byteArray, 0);
			return byteArray;
		case pvDouble:
			double[] doubleArray = new double[dataSize];
			convert.toDoubleArray(imageDataArray, 0, dataSize, doubleArray, 0);
			return doubleArray;
		case pvFloat:
			float[] floatArray = new float[dataSize];
			convert.toFloatArray(imageDataArray, 0, dataSize, floatArray, 0);
			return floatArray;
		case pvInt:
		case pvUInt:
			int[] intArray = new int[dataSize];
			convert.toIntArray(imageDataArray, 0, dataSize, intArray, 0);
			return intArray;
		case pvLong:
		case pvULong:
			long[] longArray = new long[dataSize];
			convert.toLongArray(imageDataArray, 0, dataSize, longArray, 0);
			return longArray;
		case pvShort:
		case pvUShort:
			short[] shortArray = new short[dataSize];
			convert.toShortArray(imageDataArray, 0, dataSize, shortArray, 0);
			return shortArray;
		case pvString:
		case pvBoolean: // This is the only valid conversion specified for boolean in ConvertFactory
			String[] stringArray = new String[dataSize];
			convert.toStringArray(imageDataArray, 0, dataSize, stringArray, 0);
			return stringArray;
		default:
			throw new IllegalArgumentException("Unknown ScalarType: " + dataType);
		}
	}

	/**
	 * This is a polling loop designed to be run in a separate thread. A form of double-checked locking is used to ensure that methods are not called on
	 * {@code pvaClientMonitor} unless we are sure it has not been stopped/destroyed. {@link PvaClientMonitor#waitEvent(double)} is used rather than poll to
	 * reduce number of calls when there are no new updates.
	 * <p>
	 * This drives updates in the live stream via {@link #updateData(PVStructure)} and {@link #updateArrayAttributes(PVStructure)} methods.
	 */
	private void pvaMonitorEventLoop() {
		while (monitorActive) {
			synchronized (this) {
				if (!monitorActive) {
					return;
				}
				if (pvaClientMonitor.waitEvent(1)) {
					PvaClientMonitorData monitorData = pvaClientMonitor.getData();
					if (frameRateLimiter.tryAcquire()) {
						try {
							LivestreamDataFromPvaStructure data = new LivestreamDataFromPvaStructure(monitorData.getPVStructure());
							updateArrayAttributes(data.getDimensionSizes(), data.getColorMode());
							updateData(data.getImageArray());
						} catch (IllegalStateException e) {
							logger.error("Invalid PVA Structure for livestream ({}) - {}", pvName, e.getMessage());
						}
					} else {
						logger.trace("Frame dropped");
					}
					pvaClientMonitor.releaseEvent();
				}
			}
		}
	}

	/**
	 * A class to extract and validate the data that is required for producing the livestream dataset from the {@link PVStructure}.
	 */
	private static class LivestreamDataFromPvaStructure {
		private final PVScalarArray imageArray;
		private final int colourMode;
		private final int[] dimensions;

		public PVScalarArray getImageArray() {
			return imageArray;
		}

		public int getColorMode() {
			return colourMode;
		}

		public int[] getDimensionSizes() {
			return dimensions;
		}

		public LivestreamDataFromPvaStructure(final PVStructure pvStructure) {
			imageArray = extractImageArray(pvStructure);
			colourMode = extractColourMode(pvStructure);
			dimensions = extractDimensions(pvStructure);
		}

		private int[] extractDimensions(PVStructure pvStructure) {
			PVStructureArray dimensionArrayStructure = pvStructure.getSubField(PVStructureArray.class, "dimension");
			if (dimensionArrayStructure == null) {
				throw new IllegalStateException("No dimension field found in PV Structure");
			}

			int numDims = dimensionArrayStructure.getLength();
			if (numDims < 2) { // Require at least 2 dimensions
				throw new IllegalStateException("Invalid dimensions of PV Structure");
			}

			StructureArrayData dimensionArrayData = new StructureArrayData();
			// This copies into dimensionArrayData it is then accessible via it's data field
			dimensionArrayStructure.get(0, numDims, dimensionArrayData);

			// The limit in this stream is required because
			// it's possible for dimensionArrayStructure#value to be longer than
			// dimensionArrayStructure#length (containing stale dimension data)
			// - can be observed if colour mode changed from Mono -> RGB -> Mono
			int[] dims = Arrays.stream(dimensionArrayData.data)
					.map(struct -> struct.getSubField(PVInt.class, "size"))
					.filter(Objects::nonNull)
					.limit(numDims)
					.mapToInt(PVInt::get)
					.toArray();
			if (dims.length != numDims) {
				throw new IllegalStateException("Invalid/incomplete dimension data");
			}

			return dims;
		}

		private int extractColourMode(PVStructure pvStructure) {
			PVStructureArray attributeStructures = pvStructure.getSubField(PVStructureArray.class, "attribute");
			if (attributeStructures != null) {
				int nattr = attributeStructures.getLength();
				StructureArrayData attrdata = new StructureArrayData();
				attributeStructures.get(0, nattr, attrdata);
				Map<String, PVInt> attribMap = new HashMap<>();
				for (PVStructure structure : attrdata.data) {
					// Only want to extract colour mode which is an integer
					attribMap.put(structure.getSubField(PVString.class, "name").get(), structure.getSubField(PVUnion.class, "value").get(PVInt.class));
				}
				if (attribMap.containsKey("ColorMode")) {
					return attribMap.get("ColorMode").get();
				} else {
					throw new IllegalStateException("No ColorMode attribute found");
				}
			}
			throw new IllegalStateException("PV Structure does not contain attributes");

		}

		private PVScalarArray extractImageArray(PVStructure pvStructure) {
			// Make sure there is actually image data
			PVUnion pvUnionValue = pvStructure.getSubField(PVUnion.class, "value");
			if (pvUnionValue == null) {
				throw new IllegalStateException("Image data field not found/valid");
			}
			PVScalarArray imageData = pvUnionValue.get(PVScalarArray.class);
			if (imageData == null) {
				throw new IllegalStateException("Image data is not a scalar array");
			}

			if (imageData.getLength() == 0) {
				throw new IllegalStateException("Array contains no elements");
			}

			// Check if there is a codec
			PVStructure pvCodecStruct = pvStructure.getSubField(PVStructure.class, "codec");
			PVString pvCodec = pvCodecStruct.getSubField(PVString.class, "name");
			String codec = pvCodec.get();
			if (!codec.isEmpty()) {
				throw new IllegalStateException("Compressed NTNDArrays are not currently supported");
			}

			return imageData;
		}
	}
}
