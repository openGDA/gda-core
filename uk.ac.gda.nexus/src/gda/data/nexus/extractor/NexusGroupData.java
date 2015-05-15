/*-
 * Copyright © 2009-2013 Diamond Light Source Ltd.
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

package gda.data.nexus.extractor;

import gda.data.nexus.NexusUtils;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyWriteableDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.dawnsci.hdf5.nexus.NexusFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data class that is returned by first class Nexus aware detectors
 */
public class NexusGroupData implements Serializable {
	transient private static final Logger logger = LoggerFactory.getLogger(NexusGroupData.class);		
	private Serializable data;
	
	/**
	 * dimensions of data
	 */
	public int[] dimensions;

	/**
	 * This array may be used to indicate a preferred choice of chunking to the datawriter.
	 * The data-writer might well ignore that though (for now all will). 
	 */
	public int[] chunkDimensions = null;

	/**
	 * type of data for output e.g. Dataset.STRING
	 */
	private int dtype;

	/**
	 * Setting this can advise a data-writer to use the specified compression algorithm
	 * for a choice see:
	 * 
	 * {@link NexusFile}.COMPRESSION_*
	 */
	public Integer compressionType = null;

	/**
	 * Flag to indicate that when writing this value to a file the entry is to linked to the NXEntry/NXDetector section as a variable of the scan
	 */
	public boolean isDetectorEntryData = false;

	private boolean isUnsigned = false;

	private int textLength = -1;

	/**
	 * Maximum length of any text string encoded as bytes
	 */
	public static final int MAX_TEXT_LENGTH = 255;

	private static final Charset UTF8 = Charset.forName("UTF-8");

	NexusGroupData() {
	}

	/**
	 * @param dimensions
	 * @param dtype dataset type specified for output
	 * @param data
	 */
	NexusGroupData(int[] dimensions, int dtype, Serializable data) {
		this.dimensions = dimensions;
		this.dtype = dtype;
		this.data = data;
	}

	/**
	 * @param dimensions
	 * @param clazz
	 */
	NexusGroupData(int[] dimensions, Class<?> clazz) {
		this.dimensions = dimensions;
		dtype = AbstractDataset.getDTypeFromClass(clazz);
		this.data = null;
	}

	/**
	 * @param dimensions
	 * @param data
	 */
	public NexusGroupData(int[] dimensions, Serializable data) {
		this.dimensions = dimensions;
		this.data = data;
		if (data.getClass().isArray()) {
			if (data instanceof boolean[] || data instanceof Boolean[]) {
				dtype = Dataset.INT8;
			} else if (data instanceof byte[] || data instanceof Byte[]) {
				dtype = Dataset.INT8;
			} else if (data instanceof short[] || data instanceof Short[]) {
				dtype = Dataset.INT16;
			} else if (data instanceof int[] || data instanceof Integer[]) {
				dtype = Dataset.INT32;
			} else if (data instanceof long[] || data instanceof Long[]) {
				dtype = Dataset.INT64;
			} else if (data instanceof float[] || data instanceof Float[]) {
				dtype = Dataset.FLOAT32;
			} else if (data instanceof double[] || data instanceof Double[]) {
				dtype = Dataset.FLOAT64;
			} else if (data instanceof String[]) {
				dtype = Dataset.STRING;
				makeBytes((String[]) data, null);
			} else {
				dtype = -1;
				throw new IllegalArgumentException("Unknown class of serializable array");
			}
		} else {
			dtype = -1;
			throw new IllegalArgumentException("Serializable must be an array");
		}
	}

	/**
	 * @param dataset
	 * @return group data
	 */
	public static NexusGroupData createFromDataset(IDataset dataset) {
		Dataset ad = DatasetUtils.convertToDataset(dataset);
		return new NexusGroupData(ad.getShapeRef(), ad.getBuffer());
	}

	/**
	 * Set maximum length of any string when encoded as bytes
	 * @param length
	 */
	public void setMaxStringLength(int length) {
		this.textLength = length;
	}

	/**
	 * TODO replace with ncsa.hdf.object.Dataset#stringToByte
	 * Makes fixed size byte array
	 * @param text
	 * @param maxLength maximum encoded length in bytes of each string
	 */
	private void makeBytes(String[] text, Integer maxLength) {
		int n = text.length;
		byte[][] lines = new byte[n][];
		int max = -1;
		for (int i = 0; i < n; i++) {
			String t = text[i];
			if (t == null)
				continue;
			lines[i] = t.getBytes(UTF8);
			max = Math.max(max, lines[i].length);
		}
		if (maxLength == null || maxLength < 0) {
			textLength = max;
		} else {
			textLength = maxLength;
		}
		byte[] bdata = new byte[textLength * n];
		int k = 0;
		for (int i = 0; i < n; i++) {
			byte[] t = lines[i];
			if (t == null)
				continue;

			int l = t.length > textLength ? textLength : t.length;
			System.arraycopy(t, 0, bdata, k, l);
			k += textLength;
		}
		data = bdata;
		dimensions = new int[] { n };
	}

	private String[] makeStrings(byte[] bdata) {
		if (textLength <= 0) { // single string case
			String text;
			text = new String(bdata, UTF8);
			return new String[] {text};
		}
		int n = bdata.length / textLength;
		String[] text = new String[n];
		int k = 0;
		for (int i = 0; i < n; i++) {
			int end = k;
			int stop = Math.min(k + textLength, bdata.length);
			while (end < stop && bdata[end++] != 0) {
			}
			text[i] = new String(bdata, k, end - k - 1, UTF8);
			k += textLength;
		}
		return text;
	}

	/**
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(String s) {
		data = s.getBytes(UTF8);
		dimensions = new int[1];
		dimensions[0] = s.length();
		dtype = Dataset.STRING;
	}

	/**
	 * @param length of encoded string in bytes
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(int length, String s) {
		data = Arrays.copyOf(s.getBytes(UTF8), length);
		dimensions = new int[] {length};
		dtype = Dataset.STRING;
	}

	public NexusGroupData(byte... b) {
		this(new int[]{b.length}, b);
	}

	public NexusGroupData(int[] dims, byte... b) {
		dimensions = dims;
		data = b;
		dtype = Dataset.INT8;
	}

	public NexusGroupData(short... s) {
		this(new int[]{s.length}, s);
	}

	public NexusGroupData(int[] dims, short... s) {
		dimensions = dims;
		data = s;
		dtype = Dataset.INT16;
	}

	public NexusGroupData(short[][] s) {
		dimensions = new int[] {s.length, s[0].length};
		data = s;
		dtype = Dataset.INT16;
	}

	public NexusGroupData(int... i) {
		this(new int[]{i.length}, i);
	}

	public NexusGroupData(int[] dims, int... i) {
		dimensions = dims;
		data = i;
		dtype = Dataset.INT32;
	}

	public NexusGroupData(int[][] i) {
		dimensions = new int[] {i.length, i[0].length};
		data = i;
		dtype = Dataset.INT32;
	}

	public NexusGroupData(int[][][] i) {
		dimensions = new int[] {i.length, i[0].length, i[0][0].length};
		data = i;
		dtype = Dataset.INT32;
	}

	public NexusGroupData(long... l) {
		this(new int[]{l.length}, l);
	}

	public NexusGroupData(int[] dims, long... l) {
		dimensions = dims;
		data = l;
		dtype = Dataset.INT64;
	}

	public NexusGroupData(boolean b) {
		this(b?1:0);
	}

	public NexusGroupData(float... f) {
		this(new int[]{f.length}, f);
	}

	public NexusGroupData(int[] dims, float... f) {
		dimensions = dims;
		data = f;
		dtype = Dataset.FLOAT32;
	}

	public NexusGroupData(double... d) {
		this(new int[]{d.length}, d);
	}

	public NexusGroupData(int[] dims, double... d) {
		dimensions = dims;
		data = d;
		dtype = Dataset.FLOAT64;
	}

	public NexusGroupData(double[][] d) {
		dimensions = new int[] {d.length, d[0].length};
		data = d;
		dtype = Dataset.FLOAT64;
	}

	public NexusGroupData(double[][][] d) {
		dimensions = new int[] {d.length, d[0].length, d[0][0].length};
		data = d;
		dtype = Dataset.FLOAT64;
	}

	/**
	 * Set type to be unsigned
	 * @return this
	 */
	public NexusGroupData setUnsigned() {
		switch (dtype) {
		case Dataset.INT8:
		case Dataset.INT16:
		case Dataset.INT32:
		case Dataset.INT64:
			isUnsigned = true;
			break;
		default:
			throw new UnsupportedOperationException("Can not set type to unsigned");
		}
		return this;
	}

	/**
	 * @return true if data contains characters
	 */
	public boolean isChar() {
		return dtype == Dataset.STRING;
	}

	/**
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer() {
		return getBuffer(false);
	}

	/**
	 * @param raw if true, do not convert to bytes back to strings
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer(boolean raw) {
		if (!raw && dtype == Dataset.STRING) {
			return makeStrings((byte[]) data);
		}
		return data;
	}

	/**
	 * @return Returns the value of data and sets data to null
	 */
	public Object releaseData() {
		Object d = data;
		data = null;
		return d;
	}

	@Override
	public String toString() {
		StringBuffer msg = new StringBuffer("");
		msg.append("<dimensions>");
		for (int i : dimensions) {
			msg.append("<dimension>" + i + "</dimension>");
		}
		msg.append("</dimensions>");
		msg.append("<type>");
		switch (dtype) {
		case Dataset.STRING:
			msg.append("NX_CHAR");
			break;
		case Dataset.FLOAT64:
			msg.append("NX_FLOAT64");
			break;
		default:
			msg.append(dtype);
			break;
		}
		msg.append("</type>");
		return msg.toString();
	}

	private String getAsString() {
		if (data instanceof String[]) {
			return ((String[]) data)[0];
		} else if (data instanceof byte[]) {
			byte[] bdata = (byte[]) data;
			int i;
			for (i = 0; i < textLength; i++) {
				if (bdata[i] == 0) {
					break;
				}
			}
			return new String(bdata, 0, i);
		}
		return "";
	}

	/**
	 * @param newlineAfterEach
	 * @param dataAsString
	 * @param wrap
	 * @return XML representation of the object
	 */
	public String dataToTxt(boolean newlineAfterEach, boolean dataAsString, boolean wrap) {
		StringBuffer msg = new StringBuffer();
		if (data != null) {
			if (dtype == Dataset.STRING) {
				if (wrap)
					msg.append("<value>");
				msg.append(getAsString());
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append('\n');
				}
			} else {
				if (dataAsString) {
					if (wrap)
						msg.append("<value>");
					if (data instanceof byte[]) {
						for (byte d : (byte[]) data) {
							msg.append(Byte.toString(d));
							msg.append(',');
						}
					} else if (data instanceof short[]) {
						for (short d : (short[]) data) {
							msg.append(Short.toString(d));
							msg.append(',');
						}
					} else if (data instanceof int[]) {
						for (int d : (int[]) data) {
							msg.append(Integer.toString(d));
							msg.append(',');
						}
					} else if (data instanceof long[]) {
						for (long d : (long[]) data) {
							msg.append(Long.toString(d));
							msg.append(',');
						}
					} else if (data instanceof float[]) {
						for (float d : (float[]) data) {
							msg.append(Float.toString(d));
							msg.append(',');
						}
					} else if (data instanceof double[]) {
						for (double d : (double[]) data) {
							msg.append(Double.toString(d));
							msg.append(',');
						}
					} else {
						msg.append(data.toString());
						msg.append(',');
					}
//					msg.deleteCharAt(msg.length()-1);
					if (wrap)
						msg.append("</value>");
					if (newlineAfterEach) {
						msg.append('\n');
					}
				} else {
					msg.append("<values>");
					if (newlineAfterEach) {
						msg.append('\n');
					}
					if (data instanceof byte[]) {
						for (byte d : (byte[]) data) {
							msg.append("<value>");
							msg.append(Byte.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else if (data instanceof short[]) {
						for (short d : (short[]) data) {
							msg.append("<value>");
							msg.append(Short.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else if (data instanceof int[]) {
						for (int d : (int[]) data) {
							msg.append("<value>");
							msg.append(Integer.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else if (data instanceof long[]) {
						for (long d : (long[]) data) {
							msg.append("<value>");
							msg.append(Long.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else if (data instanceof float[]) {
						for (float d : (float[]) data) {
							msg.append("<value>");
							msg.append(Float.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else if (data instanceof double[]) {
						for (double d : (double[]) data) {
							msg.append("<value>");
							msg.append(Double.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append('\n');
							}
						}
					} else {
						msg.append(data.toString());
					}
					msg.append("</values>");
					if (newlineAfterEach) {
						msg.append('\n');
					}
				}
			}
		}
		return msg.toString();
	}

	public Serializable getFirstValue() {
		if (dimensions == null || data == null)
			return null;

		if (dimensions.length < 1)
			return null;

		if (dimensions[0] < 1)
			return null;

		Serializable value;

		if (data.getClass().isArray()) {
			if (dtype == Dataset.STRING) { // reinterpret byte arrays as String 
				return getStringFromArray(data);
			}
			value = getFromArray(data);
			if (value == null)
				return value;
		} else {
			value = data;
		}

		Class<? extends Serializable> clazz = value.getClass();
		if (dtype == Dataset.BOOL) {
			if (!clazz.equals(Boolean.class) || !clazz.equals(boolean.class)) {
				value = !value.equals(0);
			}
		} else {
			// promote to integers and doubles if possible
			if (clazz.equals(byte.class) || clazz.equals(Byte.class)) {
				value = ((Byte) value).intValue();
			} else if (clazz.equals(short.class) || clazz.equals(Short.class)) {
				value = ((Short) value).intValue();
			} else if (clazz.equals(float.class) || clazz.equals(Float.class)) {
				value = ((Float) value).doubleValue();
			}
		}

		return value;
	}

	/**
	 * Create a lazy dataset based on contents
	 * @return lazy dataset
	 */
	public ILazyWriteableDataset toLazyDataset() {
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", dtype, dimensions, null, chunkDimensions);
		return lazy;
	}

	/**
	 * Create a dataset based on contents
	 * @return dataset
	 */
	public Dataset toDataset() {
		return toDataset(true);
	}

	/**
	 * Create a dataset based on contents
	 * @param keepBitWidth
	 * @return dataset
	 */
	public Dataset toDataset(boolean keepBitWidth) {
		Dataset dataset = DatasetFactory.createFromObject(getBuffer(), dtype);
		if (!keepBitWidth && isUnsigned) {
			dataset = DatasetUtils.makeUnsigned(dataset);
		}
		dataset.setShape(dimensions);
		return dataset;
	}

	/**
	 * @return dataset type
	 */
	public int getDtype() {
		return dtype;
	}

	/**
	 * @return type of data e.g. "NX_CHAR"
	 */
	public String getType() {
		String type;
		switch (dtype) {
		case Dataset.STRING:
			type = "CHAR";
			break;
		case Dataset.INT8:
			type = "INT8";
			break;
		case Dataset.INT16:
			type = "INT16";
			break;
		case Dataset.INT32:
			type = "INT32";
			break;
		case Dataset.INT64:
			type = "INT64";
			break;
		case Dataset.FLOAT32:
			type = "FLOAT32";
			break;
		case Dataset.FLOAT64:
			type = "FLOAT64";
			break;
		default:
			return "UNKNOWN";
		}
		if (isUnsigned) {
			type = "U" + type;
		}
		return "NX_" + type;
	}


	private static Serializable getFromArray(Serializable array) {
		Serializable a = (Serializable) Array.get(array, 0);
		if (a == null)
			return null;
		if (a.getClass().isArray())
			return getFromArray(a);
		return a;
	}

	private static String getStringFromArray(Serializable array) {
		if (array instanceof byte[]) {
			return new String((byte[]) array);
		}
		Serializable a = (Serializable) Array.get(array, 0);
		if (a == null)
			return null;
		if (a instanceof String)
			return (String) a;
		if (a.getClass().isArray())
			return getStringFromArray(a);
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(obj, false);
	}

	public boolean equals(Object obj, boolean logWhenFalseInOneCase) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NexusGroupData other = (NexusGroupData) obj;
		if (data == null) {
			if (other.data != null) {
				return false;
			}
		} else {
			String dataToTxt = dataToTxt(false, false, false);
			String dataToTxt2 = other.dataToTxt(false, false, false);
			if (!dataToTxt.equals(dataToTxt2)) {
				if (logWhenFalseInOneCase)
					logger.info("expected = "+dataToTxt(false,true,false) + ", actual= " +other.dataToTxt(false,true,false));
				return false;
			}
		}
		return true;
	}

	private NexusGroupData asType(int type) {
		if (this.dtype == type)
			return this;

		NexusGroupData ngd = new NexusGroupData(dimensions, type, data);
		ngd.chunkDimensions = chunkDimensions;
		ngd.isDetectorEntryData = isDetectorEntryData;
		ngd.isUnsigned = isUnsigned;
		return ngd;
	}

	public int[] getDimensions() {
		if (dtype == Dataset.STRING) {
			int last = dimensions.length - 1;
			int[] dims = dimensions.clone();
			if (dimensions[last] == 1)
				dims[last] = textLength;
			return dims;
		}
		return dimensions;
	}

	/**
	 * @return data with output type as char
	 */
	public NexusGroupData asChar() {
		return asType(Dataset.STRING);
	}

	/**
	 * @return data with output type as byte
	 */
	public NexusGroupData asByte() {
		return asType(Dataset.INT8);
	}

	/**
	 * @return data with output type as short
	 */
	public NexusGroupData asShort() {
		return asType(Dataset.INT16);
	}

	/**
	 * @return data with output type as integer
	 */
	public NexusGroupData asInt() {
		return asType(Dataset.INT32);
	}

	/**
	 * @return data with output type as long
	 */
	public NexusGroupData asLong() {
		return asType(Dataset.INT64);
	}

	/**
	 * @return data with output type as float
	 */
	public NexusGroupData asFloat() {
		return asType(Dataset.FLOAT32);
	}

	
	/**
	 * @return data with output type as double
	 */
	public NexusGroupData asDouble() {
		return asType(Dataset.FLOAT64);
	}

	public boolean isFloat() {
		return dtype == Dataset.FLOAT32;
	}

	public boolean isDouble() {
		return dtype == Dataset.FLOAT64;
	}
}
