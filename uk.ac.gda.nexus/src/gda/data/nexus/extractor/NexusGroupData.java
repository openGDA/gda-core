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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;

import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.ByteDataset;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyWriteableDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.InterfaceUtils;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.ShapeUtils;
import org.eclipse.january.dataset.ShortDataset;
import org.eclipse.january.dataset.StringDataset;

/**
 * Data class that is returned by first class Nexus aware detectors
 */
public class NexusGroupData implements Serializable {

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
	 * class of data for output e.g. StringDataset.class
	 */
	private Class<? extends Dataset> clazz = null;

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

	/**
	 * If this data represents a link to an external file (where the data value is the path to that file
	 * in the format nxfile://externalFilePath#nodePath), then this field specifies the rank of the data
	 * written at each point in the scan. Note that the actual rank of the dataset written will be this
	 * number plus the scan rank.
	 */
	public int externalDataRank = -1;

	private boolean isUnsigned = false;

	private int textLength = -1;

	/**
	 * When using {@code NexusScanDataWriter}, and {@link #isDetectorEntryData} is <code>true</code>,
	 * then setting this field causes the corresponding dataset in the nexus file to be added to an
	 * {@link NXdata} group with the given name.
	 * <p>
	 * If this field is <code>null</code>, then a new {@link NXdata} group will be created
	 * with the dataset as the signal field.
	 */
	public String dataGroupName = null;

	/**
	 * Maximum length of any text string encoded as bytes
	 */
	public static final int MAX_TEXT_LENGTH = 255;

	NexusGroupData() {
	}

	/**
	 * @param dimensions
	 * @param clazz
	 * @param data
	 * @param chunkDimensions
	 */
	NexusGroupData(int[] dimensions, Class<? extends Dataset> clazz, Serializable data, int[] chunkDimensions) {
		this.dimensions = dimensions;
		this.clazz = clazz;
		this.data = data;
		this.chunkDimensions = chunkDimensions;
	}

	/**
	 * @param dimensions
	 * @param eClass element class
	 */
	NexusGroupData(int[] dimensions, Class<?> eClass) {
		this(dimensions, InterfaceUtils.getInterfaceFromClass(1, eClass), null, null);
	}

	/**
	 * @param data
	 */
	public NexusGroupData(IDataset data) {
		Dataset ds = DatasetUtils.convertToDataset(data);
		dimensions = ds.getShapeRef();
		this.data = ds.getBuffer();
		clazz = ds.getClass();
		chunkDimensions = calcChunksFromInterface(dimensions, clazz);
	}

	/**
	 * @param dataset
	 * @return group data
	 */
	public static NexusGroupData createFromDataset(IDataset dataset) {
		return new NexusGroupData(dataset);
	}

	/**
	 * Set maximum length of any string when encoded as bytes
	 * @param length
	 */
	public void setMaxStringLength(int length) {
		this.textLength = length;
	}

	/**
	 * TODO replace with hdf.object.Dataset#stringToByte Makes fixed size byte array
	 *
	 * @param text
	 * @param maxLength
	 *            maximum encoded length in bytes of each string
	 * @return serializable
	 */
	private static Serializable makeBytes(String[] text, int maxLength) {
		int n = text.length;
		byte[][] lines = new byte[n][];
		int max = -1;
		for (int i = 0; i < n; i++) {
			String t = text[i];
			if (t == null)
				continue;
			byte[] l = t.getBytes(UTF_8);
			if (maxLength > 0) {
				if (l.length != maxLength) {
					l = Arrays.copyOf(l, maxLength);
				}
			}
			max = Math.max(max, l.length);
			lines[i] = l;
		}
		byte[] bdata = new byte[n * max];
		int k = 0;
		for (int i = 0; i < n; i++) {
			System.arraycopy(lines[i], 0, bdata, k, lines[i].length);
			k += max;
		}
		return bdata;
	}

	private static int getMaxLength(String[] text) {
		int n = text.length;
		int max = -1;
		for (int i = 0; i < n; i++) {
			String t = text[i];
			if (t == null)
				continue;
			byte[] line = t.getBytes(UTF_8);
			max = Math.max(max, line.length);
		}
		return max;
	}

	private static String[] makeStrings(byte[] bdata, int maxLength) {
		if (maxLength <= 0) { // single string case
			return new String[] { new String(bdata, UTF_8) };
		}
		int n = bdata.length / maxLength;
		String[] text = new String[n];
		if (n == 1) {
			int end = 0;
			int stop = Math.min(maxLength, bdata.length);
			while (end < stop && bdata[end++] != 0) {
			}
			if (end == stop) {
				end++;
			}
			text[0] = new String(bdata, 0, end - 1, UTF_8);
		} else {
			int k = 0;
			for (int i = 0; i < n; i++) {
				int end = k;
				int stop = Math.min(k + maxLength, bdata.length);
				while (end < stop && bdata[end++] != 0) {
				}
				text[i] = new String(bdata, k, end - k - 1, UTF_8);
				k += maxLength;
			}
		}
		return text;
	}

	/**
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(String s) {
		data = new String[] { s };
		dimensions = new int[] { 1 };
		clazz = StringDataset.class;
	}

	/**
	 * @param length of encoded string in bytes
	 * @param s Strings from which to make a NexusGroupData
	 */
	public NexusGroupData(int length, String... s) {
		this(length, new int[] {s.length}, s);
	}

	/**
	 * @param length of encoded string in bytes
	 * @param dims
	 * @param s Strings from which to make a NexusGroupData
	 */
	public NexusGroupData(int length, int[] dims, String[] s) {
		textLength = length;
		data = s;
		dimensions = dims;
		clazz = StringDataset.class;
	}

	public NexusGroupData(byte... b) {
		this(new int[] { b.length }, b);
	}

	public NexusGroupData(int[] dims, byte... b) {
		this(dims, ByteDataset.class, b, null);
	}

	public NexusGroupData(short... s) {
		this(new int[] { s.length }, s);
	}

	public NexusGroupData(int[] dims, short... s) {
		this(dims, ShortDataset.class, s, null);
	}

	public NexusGroupData(short[][] s) {
		this(ShapeUtils.getShapeFromObject(s), ShortDataset.class, s, null);
	}

	public NexusGroupData(int i) {
		this(new int[] { 1 }, new int[] { i });
	}

	public NexusGroupData(int... i) {
		this(new int[] { i.length }, i);
	}

	public NexusGroupData(int[] dims, int... i) {
		this(dims, IntegerDataset.class, i, null);
	}

	public NexusGroupData(int[][] i) {
		this(ShapeUtils.getShapeFromObject(i), IntegerDataset.class, i, null);
	}

	public NexusGroupData(int[][][] i) {
		this(ShapeUtils.getShapeFromObject(i), IntegerDataset.class, i, null);
	}

	public NexusGroupData(long... l) {
		this(new int[] { l.length }, l);
	}

	public NexusGroupData(int[] dims, long... l) {
		this(dims, LongDataset.class, l, null);
	}

	public NexusGroupData(boolean b) {
		this(b ? 1 : 0);
	}

	public NexusGroupData(float... f) {
		this(new int[] { f.length }, f);
	}

	public NexusGroupData(int[] dims, float... f) {
		this(dims, FloatDataset.class, f, null);
	}

	public NexusGroupData(double... d) {
		this(new int[] { d.length }, d);
	}

	public NexusGroupData(int[] dims, double... d) {
		this(dims, DoubleDataset.class, d, null);
	}

	public NexusGroupData(double[][] d) {
		this(ShapeUtils.getShapeFromObject(d), DoubleDataset.class, d, null);
	}

	public NexusGroupData(double[][][] d) {
		this(ShapeUtils.getShapeFromObject(d), DoubleDataset.class, d, null);
	}

	/**
	 * Set type to be unsigned
	 * @return this
	 */
	public NexusGroupData setUnsigned() {
		if (InterfaceUtils.isInteger(clazz)) {
			isUnsigned = true;
		} else {
			throw new UnsupportedOperationException("Can not set type to unsigned");
		}
		return this;
	}

	/**
	 * @return true if data contains characters
	 */
	public boolean isChar() {
		return StringDataset.class.equals(clazz);
	}

	/**
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer() {
		return getBuffer(false);
	}

	/**
	 * @param encode if true, convert to strings to UTF-8 bytes
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer(boolean encode) {
		if (isChar()) {
			if (!encode && data instanceof byte[]) {
				return makeStrings((byte[]) data, textLength);
			}
			if (encode && data instanceof String[]) {
				return makeBytes((String[]) data, textLength);
			}
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
		if (isChar()) {
			msg.append("NX_CHAR");
		} else if (DoubleDataset.class.equals(clazz)) {
			msg.append("NX_FLOAT64");
		} else {
			msg.append(clazz == null ? "unknown" : clazz.getCanonicalName());
		}
		msg.append("</type>");
		return msg.toString();
	}

	private String getAsString() {
		if (data instanceof String[]) {
			return ((String[]) data)[0];
		} else if (data instanceof byte[]) {
			byte[] bdata = (byte[]) data;
			int end = Math.min(bdata.length, textLength);
			int i;
			for (i = 0; i < end; i++) {
				if (bdata[i] == 0) {
					break;
				}
			}
			return new String(bdata, 0, i, UTF_8);
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
			if (isChar()) {
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

		if (ShapeUtils.calcSize(dimensions) < 1)
			return null;

		Serializable value;

		if (data.getClass().isArray()) {
			if (isChar()) { // reinterpret byte arrays as String
				return getStringFromArray(data);
			}
			value = getFromArray(data);
			if (value == null)
				return value;
		} else {
			value = data;
		}

		Class<? extends Serializable> vClazz = value.getClass();
		if (BooleanDataset.class.equals(clazz)) {
			if (!vClazz.equals(Boolean.class) || !vClazz.equals(boolean.class)) {
				value = !value.equals(0);
			}
		} else {
			// promote to integers and doubles if possible
			if (vClazz.equals(byte.class) || vClazz.equals(Byte.class)) {
				value = ((Byte) value).intValue();
			} else if (vClazz.equals(short.class) || vClazz.equals(Short.class)) {
				value = ((Short) value).intValue();
			} else if (vClazz.equals(float.class) || vClazz.equals(Float.class)) {
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
		int[] shape = dimensions;
		int[] chunks = chunkDimensions;
		if (isChar()) {
			if (data instanceof byte[] &&  dimensions.length > 1 && dimensions[dimensions.length - 1] == textLength) {
				shape = Arrays.copyOf(dimensions, dimensions.length - 1);
				if (chunks != null) {
					chunks = Arrays.copyOf(chunkDimensions, chunkDimensions.length - 1);
				}
			}
		}
		ILazyWriteableDataset lazy = NexusUtils.createLazyWriteableDataset("data", InterfaceUtils.getElementClass(clazz), shape, null, chunks);
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
		Dataset dataset = DatasetFactory.createFromObject(clazz, getBuffer());
		if (!keepBitWidth && isUnsigned) {
			dataset = DatasetUtils.makeUnsigned(dataset);
		}
		if (isChar()) {
			if (data instanceof byte[] &&  dimensions.length > 1 && dimensions[dimensions.length - 1] == textLength) {
				dataset.setShape(Arrays.copyOf(dimensions, dimensions.length - 1));
			}
		} else {
			dataset.setShape(dimensions);
		}
		return dataset;
	}

	/**
	 * @return dataset interface
	 */
	public Class<? extends Dataset> getInterface() {
		return clazz;
	}

	/**
	 * @return type of data e.g. "NX_CHAR"
	 */
	public String getType() {
		String type;
		if (StringDataset.class.isAssignableFrom(clazz)) {
			type = "CHAR";
		} else if (ByteDataset.class.isAssignableFrom(clazz)) {
			type = "INT8";
		} else if (ShortDataset.class.isAssignableFrom(clazz)) {
			type = "INT16";
		} else if (IntegerDataset.class.isAssignableFrom(clazz)) {
			type = "INT32";
		} else if (LongDataset.class.isAssignableFrom(clazz)) {
			type = "INT64";
		} else if (FloatDataset.class.isAssignableFrom(clazz)) {
			type = "FLOAT32";
		} else if (DoubleDataset.class.isAssignableFrom(clazz)) {
			type = "FLOAT64";
		} else {
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

	private NexusGroupData asType(Class<? extends Dataset> nClass) {
		if (clazz == nClass)
			return this;

		NexusGroupData ngd = new NexusGroupData(dimensions, nClass, data, chunkDimensions);
		ngd.isDetectorEntryData = isDetectorEntryData;
		ngd.isUnsigned = isUnsigned;
		ngd.textLength = textLength;
		return ngd;
	}

	public int[] getDimensions() {
		if (isChar()) {
			if (data instanceof String[]) {
				int[] dims;
				int rank = dimensions.length;
				if (textLength > 0) {
					dims = Arrays.copyOf(dimensions, rank + 1);
					dims[rank] = textLength;
				} else if (rank == 0 || (rank == 1 && dimensions[0] == 1)) {
					byte[] line = ((String[]) data)[0].getBytes(UTF_8);
					dims = new int[] { line.length };
				} else {
					dims = Arrays.copyOf(dimensions, rank + 1);
					dims[rank] = getMaxLength((String[]) data);
				}
				return dims;
			}
		}
		return dimensions;
	}

	/**
	 * @return data with output type as char
	 */
	public NexusGroupData asChar() {
		return asType(StringDataset.class);
	}

	/**
	 * @return data with output type as byte
	 */
	public NexusGroupData asByte() {
		return asType(ByteDataset.class);
	}

	/**
	 * @return data with output type as short
	 */
	public NexusGroupData asShort() {
		return asType(ShortDataset.class);
	}

	/**
	 * @return data with output type as integer
	 */
	public NexusGroupData asInt() {
		return asType(IntegerDataset.class);
	}

	/**
	 * @return data with output type as long
	 */
	public NexusGroupData asLong() {
		return asType(LongDataset.class);
	}

	/**
	 * @return data with output type as float
	 */
	public NexusGroupData asFloat() {
		return asType(FloatDataset.class);
	}


	/**
	 * @return data with output type as double
	 */
	public NexusGroupData asDouble() {
		return asType(DoubleDataset.class);
	}

	public boolean isFloat() {
		return FloatDataset.class.equals(clazz);
	}

	public boolean isDouble() {
		return DoubleDataset.class.equals(clazz);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(chunkDimensions);
		result = prime * result + ((compressionType == null) ? 0 : compressionType.hashCode());
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + Arrays.hashCode(dimensions);
		result = prime * result + Objects.hashCode(clazz);
		result = prime * result + (isDetectorEntryData ? 1231 : 1237);
		result = prime * result + (isUnsigned ? 1231 : 1237);
		result = prime * result + textLength;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NexusGroupData other = (NexusGroupData) obj;
		if (!Arrays.equals(chunkDimensions, other.chunkDimensions))
			return false;
		if (!Objects.equals(compressionType, other.compressionType))
			return false;
		if (!Arrays.equals(dimensions, other.dimensions))
			return false;
		if (!Objects.equals(clazz, other.clazz))
			return false;
		if (isDetectorEntryData != other.isDetectorEntryData)
			return false;
		if (isUnsigned != other.isUnsigned)
			return false;
		if (textLength != other.textLength)
			return false;
		if (!Objects.deepEquals(data, other.data))
			return false;
		return true;
	}

	private static int[] calcChunksFromInterface(int[] dims, Class<? extends Dataset> clazz) {
		try {
			int size = InterfaceUtils.getItemBytes(1, clazz);
			return NexusUtils.estimateChunking(dims, size, null, NexusUtils.ChunkingStrategy.SKEW_LAST);
		} catch (Exception e) {
			// do nothing
		}
		return null;
	}
}
