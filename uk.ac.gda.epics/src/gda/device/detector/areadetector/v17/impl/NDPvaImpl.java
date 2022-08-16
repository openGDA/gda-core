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

package gda.device.detector.areadetector.v17.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.epics.pvaClient.PvaClient;
import org.epics.pvaClient.PvaClientChannel;
import org.epics.pvaClient.PvaClientGet;
import org.epics.pvaClient.PvaClientGetData;
import org.epics.pvdata.factory.ConvertFactory;
import org.epics.pvdata.pv.Convert;
import org.epics.pvdata.pv.PVInt;
import org.epics.pvdata.pv.PVScalarArray;
import org.epics.pvdata.pv.PVString;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.PVStructureArray;
import org.epics.pvdata.pv.PVUnion;
import org.epics.pvdata.pv.ScalarType;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.StructureArrayData;
import org.springframework.beans.factory.InitializingBean;

import gda.device.detector.areadetector.v17.NDPva;
import gda.epics.LazyPVFactory;
import gda.epics.ReadOnlyPV;

public class NDPvaImpl extends NDBaseImpl implements NDPva, InitializingBean {

	private static final Convert convert = ConvertFactory.getConvert();

	private ReadOnlyPV<String> pvName;
	private String basePVName;
	private final PvaClient pva = PvaClient.get("pva");
	private int width;
	private int height;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (basePVName == null) {
			throw new IllegalArgumentException("'basePVName' needs to be declared");
		}
		if (getPluginBase() == null) {
			throw new IllegalArgumentException("'pluginBase' needs to be declared");
		}

		pvName = LazyPVFactory.newReadOnlyStringFromWaveformPV(basePVName + PVNAME_RBV);
	}

	/**
	 * This method goes through the process of connecting and destrying the channel every
	 * time it is called.
	 * <p>
	 * There is a <a href="https://github.com/epics-base/exampleJava/blob/master/exampleClient/src/org/epics/exampleJava/exampleClient/ExamplePvaClientGet.java">simpler example</a> however this does not appear to work reliably.
	 * If it did it would reduce a lot of this to: <br /> {@code PVStructure pvstructure = pva.channel(getPvName(), "pva", 5.0).get("record[queueSize=1]field()").getData().getPVStructure();}
	 */
	@Override
	public Object getImage() throws IOException {
		PvaClientChannel pvaChannel = pva.createChannel(getPvName(), "pva");
		pvaChannel.issueConnect();
		Status status = pvaChannel.waitConnect(2.0);
		if (!status.isOK()) {
			throw new IOException("connect failed");
		}
		PvaClientGet pvaGet = pvaChannel.createGet("field()");
		pvaGet.issueConnect();
		status = pvaGet.waitConnect();
		if (!status.isOK()) {
			throw new IOException("createGet failed");
		}
		PvaClientGetData pvaData = pvaGet.getData();
		PVStructure pvstructure = pvaData.getPVStructure();
		pvaChannel.destroy();
		PVScalarArray pvScalarArray = getPVScalarArray(pvstructure);
		int colorMode = getColorMode(pvstructure);
		int[] dims = extractDimensions(pvstructure);
		var dataSize = getDataSize(dims, colorMode);
		ScalarType scalarType = pvScalarArray.getScalarArray().getElementType();
		return extractArray(pvScalarArray, scalarType, dataSize);
	}

	private Object extractArray(PVScalarArray imageDataArray, ScalarType dataType, int dataSize) {
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

	@Override
	public String getPvName() throws IOException {
		return pvName.get();
	}

	private int getColorMode(PVStructure pvStructure) {
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
				// No ColorMode attribute found assume mono
				return 0;
			}
		}
		throw new IllegalStateException("PV Structure does not contain attributes");
	}

	private PVScalarArray getPVScalarArray(PVStructure pvStructure) {
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
		if (pvCodecStruct != null) {
			PVString pvCodec = pvCodecStruct.getSubField(PVString.class, "name");
			String codec = pvCodec.get();
			if (!codec.isEmpty()) {
				throw new IllegalStateException("Compressed NTNDArrays are not currently supported");
			}
		}

		return imageData;
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
		int[] dims = Arrays.stream(dimensionArrayData.data).map(struct -> struct.getSubField(PVInt.class, "size")).filter(Objects::nonNull).limit(numDims)
				.mapToInt(PVInt::get).toArray();
		if (dims.length != numDims) {
			throw new IllegalStateException("Invalid/incomplete dimension data");
		}

		return dims;
	}

	private int getDataSize(int[] dimensions, int colourMode) {

		int dim0 = dimensions[0];
		int dim1 = dimensions[1];
		int dim2;
		if (dimensions.length >= 3) {
			dim2 = dimensions[2];
		} else {
			dim2 = 0;
		}

		int rgbChannels;

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
		return width * height * rgbChannels;
	}

	public String getBasePVName() {
		return basePVName;
	}

	public void setBasePVName(String basePVName) {
		this.basePVName = basePVName;
	}

	@Override
	public Object getImageObject() throws IOException {
		return getImage();
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getWidth() {
		return width;
	}

}
