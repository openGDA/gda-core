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

import gda.data.nexus.NexusGlobals;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data class that is returned by first class Nexus aware detectors
 */
public class NexusGroupData implements Serializable {
	transient private static final Logger logger = LoggerFactory.getLogger(NexusGroupData.class);		
	private Serializable data;
	
	/**
	 * dimensions - @see org.nexusformat.NexusFile.getInfo
	 */
	public int[] dimensions;

	/**
	 * This array may be used to indicate a preferred choice of chunking to the datawriter.
	 * The data-writer might well ignore that though (for now all will). 
	 */
	public int[] chunkDimensions = null;

	/**
	 * type of data for output e.g. NexusGlobals.NX_CHAR
	 */
	private int type;

	/**
	 * Setting this can advise a data-writer to use the specified compression algorithm
	 * for a choice see:
	 * 
	 * NexusGlobals.NX_COMP_*
	 */
	public Integer compressionType = null;

	/**
	 * Flag to indicate that when writing this value to a file the entry is to linked to the NXEntry/NXDetector section as a variable of the scan
	 */
	public boolean isDetectorEntryData = false;

	private boolean isUnsigned = false;

	NexusGroupData() {
	}

	/**
	 * @param dimensions
	 * @param type specified for output
	 * @param data
	 */
	NexusGroupData(int[] dimensions, int type, Serializable data) {
		this.dimensions = dimensions;
		this.type = type;
		this.data = data;
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
				type = NexusGlobals.NX_INT8;
			} else if (data instanceof byte[] || data instanceof Byte[]) {
				type = NexusGlobals.NX_INT8;
			} else if (data instanceof short[] || data instanceof Short[]) {
				type = NexusGlobals.NX_INT16;
			} else if (data instanceof int[] || data instanceof Integer[]) {
				type = NexusGlobals.NX_INT32;
			} else if (data instanceof long[] || data instanceof Long[]) {
				type = NexusGlobals.NX_INT64;
			} else if (data instanceof float[] || data instanceof Float[]) {
				type = NexusGlobals.NX_FLOAT32;
			} else if (data instanceof double[] || data instanceof Double[]) {
				type = NexusGlobals.NX_FLOAT64;
			} else {
				type = NexusGlobals.NX_UNLIMITED;
				throw new IllegalArgumentException("Unknown class of serializable array");
			}
		} else {
			type = NexusGlobals.NX_UNLIMITED;
			throw new IllegalArgumentException("Serializable must be an array");
		}
	}

	/**
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(String s) {
		try {
			data = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = s.getBytes();
		}
		dimensions = new int[1];
		dimensions[0] = s.length();
		type = NexusGlobals.NX_CHAR;
	}

	/**
	 * @param length
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(int length, String s) {
		try {
			data = Arrays.copyOf(s.getBytes("UTF-8"), length);
		} catch (UnsupportedEncodingException e) {
			data = Arrays.copyOf(s.getBytes(), length);
		}
		dimensions = new int[] {length};
		type = NexusGlobals.NX_CHAR;
	}

	public NexusGroupData(byte... b) {
		this(new int[]{b.length}, b);
	}

	public NexusGroupData(int[] dims, byte... b) {
		dimensions = dims;
		data = b;
		type = NexusGlobals.NX_INT8;
	}

	public NexusGroupData(short... s) {
		this(new int[]{s.length}, s);
	}

	public NexusGroupData(int[] dims, short... s) {
		dimensions = dims;
		data = s;
		type = NexusGlobals.NX_INT16;
	}

	public NexusGroupData(short[][] s) {
		dimensions = new int[] {s.length, s[0].length};
		data = s;
		type = NexusGlobals.NX_INT16;
	}

	public NexusGroupData(int... i) {
		this(new int[]{i.length}, i);
	}

	public NexusGroupData(int[] dims, int... i) {
		dimensions = dims;
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(int[][] i) {
		dimensions = new int[] {i.length, i[0].length};
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(int[][][] i) {
		dimensions = new int[] {i.length, i[0].length, i[0][0].length};
		data = i;
		type = NexusGlobals.NX_INT32;
	}

	public NexusGroupData(long... l) {
		this(new int[]{l.length}, l);
	}

	public NexusGroupData(int[] dims, long... l) {
		dimensions = dims;
		data = l;
		type = NexusGlobals.NX_INT64;
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
		type = NexusGlobals.NX_FLOAT32;
	}

	public NexusGroupData(double... d) {
		this(new int[]{d.length}, d);
	}

	public NexusGroupData(int[] dims, double... d) {
		dimensions = dims;
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	public NexusGroupData(double[][] d) {
		dimensions = new int[] {d.length, d[0].length};
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	public NexusGroupData(double[][][] d) {
		dimensions = new int[] {d.length, d[0].length, d[0][0].length};
		data = d;
		type = NexusGlobals.NX_FLOAT64;
	}

	/**
	 * Set type to be unsigned
	 * @return this
	 */
	public NexusGroupData setUnsigned() {
		switch (type) {
		case NexusGlobals.NX_INT8:
		case NexusGlobals.NX_INT16:
		case NexusGlobals.NX_INT32:
		case NexusGlobals.NX_INT64:
			isUnsigned = true;
			break;
		default:
			throw new UnsupportedOperationException("Can not set type to unsigned");
		}
		return this;
	}

	/**
	 * @return type of data e.g. NexusGlobals.NX_CHAR
	 */
	public int getType() {
		if (isUnsigned) {
			switch (type) {
			case NexusGlobals.NX_INT8:
				return NexusGlobals.NX_UINT8;
			case NexusGlobals.NX_INT16:
				return NexusGlobals.NX_UINT16;
			case NexusGlobals.NX_INT32:
				return NexusGlobals.NX_UINT32;
			case NexusGlobals.NX_INT64:
				return NexusGlobals.NX_UINT64;
			}
		}
		return type;
	}

	/**
	 * @return true if data contains characters
	 */
	public boolean isChar() {
		return getType() == NexusGlobals.NX_CHAR;
	}

	/**
	 * @return The data buffer compatible with type, null if data not extracted
	 */
	public Serializable getBuffer() {
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
		switch (type) {
		case NexusGlobals.NX_CHAR:
			msg.append("NX_CHAR");
			break;
		case NexusGlobals.NX_FLOAT64:
			msg.append("NX_FLOAT64");
			break;
		default:
			msg.append(type);
			break;
		}
		msg.append("</type>");
		return msg.toString();
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
			if (type == NexusGlobals.NX_CHAR && data instanceof byte[]) {
				if (wrap)
					msg.append("<value>");
				msg.append(new String((byte[]) data));
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append('\n');
				}
			} else if (type == NexusGlobals.NX_CHAR && data instanceof String[]) {
				if (wrap)
					msg.append("<value>");
				String s = ((String[]) data)[0];
				msg.append(s);
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
					msg.deleteCharAt(msg.length()-1);
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
			if (type == NexusGlobals.NX_CHAR) { // reinterpret byte arrays as String 
				return getStringFromArray(data);
			}
			value = getFromArray(data);
			if (value == null)
				return value;
		} else {
			value = data;
		}

		Class<? extends Serializable> clazz = value.getClass();
		if (type == NexusGlobals.NX_BOOLEAN) {
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
		if (this.type == type)
			return this;

		NexusGroupData ngd = new NexusGroupData(dimensions, type, data);
		ngd.chunkDimensions = chunkDimensions;
		ngd.isDetectorEntryData = isDetectorEntryData;
		ngd.isUnsigned = isUnsigned;
		return ngd;
	}

	/**
	 * @return data with output type as char
	 */
	public NexusGroupData asChar() {
		return asType(NexusGlobals.NX_CHAR);
	}

	/**
	 * @return data with output type as byte
	 */
	public NexusGroupData asByte() {
		return asType(NexusGlobals.NX_INT8);
	}

	/**
	 * @return data with output type as short
	 */
	public NexusGroupData asShort() {
		return asType(NexusGlobals.NX_INT16);
	}

	/**
	 * @return data with output type as integer
	 */
	public NexusGroupData asInt() {
		return asType(NexusGlobals.NX_INT32);
	}

	/**
	 * @return data with output type as long
	 */
	public NexusGroupData asLong() {
		return asType(NexusGlobals.NX_INT64);
	}

	/**
	 * @return data with output type as float
	 */
	public NexusGroupData asFloat() {
		return asType(NexusGlobals.NX_FLOAT32);
	}

	
	/**
	 * @return data with output type as double
	 */
	public NexusGroupData asDouble() {
		return asType(NexusGlobals.NX_FLOAT64);
	}
}
