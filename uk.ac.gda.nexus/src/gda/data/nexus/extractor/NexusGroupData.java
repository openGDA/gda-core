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

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import org.nexusformat.NexusFile;
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
	 * The datawriter might well ignore that though (for now all will). 
	 */
	public int[] chunkDimensions = null;

	/**
	 * type of data e.g. NexusFile.NX_CHAR
	 */
	public final int type;

	/**
	 * Setting this can advise a datawriter to use the specified compression algorithm
	 * for a choice see:
	 * 
	 * NexusFile.NX_COMP_*
	 */
	public final int compressionType = NexusFile.NX_COMP_NONE;

	/**
	 * Flag to indicate that when writing this value to a file the entry is to linked to the NXEntry/NXDetector section as a variable of the scan
	 */
	public boolean isDetectorEntryData = false;
	
	/**
	 * @param dimensions
	 * @param type
	 * @param data
	 */
	public NexusGroupData(int[] dimensions, int type, Serializable data) {
		super();
		this.dimensions = dimensions;
		this.type = type;
		this.data = data;
	}

	/**
	 * @param s String from which to make a NexusGroupData
	 */
	public NexusGroupData(String s) {
		super();
		try {
			data = s.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			data = s.getBytes();
		}
		dimensions = new int[1];
		dimensions[0] = s.length();
		type = NexusFile.NX_CHAR;
	}

	public NexusGroupData(Integer i) {
		super();
		int [] idata = new int[]{i};
		dimensions = new int[]{idata.length};
		data = idata;
		type = NexusFile.NX_INT32;
	}

	public NexusGroupData(Boolean b) {
		this(b?1:0);
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
		case NexusFile.NX_CHAR:
			msg.append("NX_CHAR");
			break;
		case NexusFile.NX_FLOAT64:
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
			if (type == NexusFile.NX_CHAR && data instanceof byte[]) {
				if (wrap)
					msg.append("<value>");
				msg.append(new String((byte[]) data));
				if (wrap)
					msg.append("</value>");
				if (newlineAfterEach) {
					msg.append("\n");
				}
			} else {
				if (dataAsString) {
					if (wrap)
						msg.append("<value>");
					if (data instanceof double[]) {
						double[] ddata = (double[]) (data);
						for (double d : ddata) {
							msg.append(Double.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof int[]) {
						int[] ddata = (int[]) (data);
						for (int d : ddata) {
							msg.append(Integer.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof byte[]) {
						byte[] ddata = (byte[]) (data);
						for (byte d : ddata) {
							msg.append(Byte.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof float[]) {
						float[] ddata = (float[]) (data);
						for (float d : ddata) {
							msg.append(Float.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else if (data instanceof long[]) {
						long[] ddata = (long[]) (data);
						for (long d : ddata) {
							msg.append(Long.toString(d) + ",");
						}
						msg.deleteCharAt(msg.length()-1);
					} else {
						msg.append(data.toString());
					}
					if (wrap)
						msg.append("</value>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
				} else {
					msg.append("<values>");
					if (newlineAfterEach) {
						msg.append("\n");
					}
					if (data instanceof double[]) {
						double[] ddata = (double[]) (data);
						for (double d : ddata) {
							msg.append("<value>");
							msg.append(Double.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof int[]) {
						int[] ddata = (int[]) (data);
						for (int d : ddata) {
							msg.append("<value>");
							msg.append(Integer.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof byte[]) {
						byte[] ddata = (byte[]) (data);
						for (byte d : ddata) {
							msg.append("<value>");
							msg.append(Byte.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof float[]) {
						float[] ddata = (float[]) (data);
						for (float d : ddata) {
							msg.append("<value>");
							msg.append(Float.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else if (data instanceof long[]) {
						long[] ddata = (long[]) (data);
						for (long d : ddata) {
							msg.append("<value>");
							msg.append(Long.toString(d));
							msg.append("</value>");
							if (newlineAfterEach) {
								msg.append("\n");
							}
						}
					} else {
						msg.append(data.toString());
					}
					msg.append("</values>");
					if (newlineAfterEach) {
						msg.append("\n");
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

		// promote to int or double if possible
		switch (type) {
		case NexusFile.NX_CHAR:
			if (data instanceof String)
				value = data;
			else
				value = new String((byte[]) data);
			break;
		case NexusFile.NX_FLOAT64:
			value = ((double[]) data)[0];
			break;
		case NexusFile.NX_FLOAT32:
			value = (double) ((float[]) data)[0];
			break;
		case NexusFile.NX_INT64:
			value = ((long[]) data)[0];
			break;
		case NexusFile.NX_INT32:
			value = ((int[]) data)[0];
			break;
		case NexusFile.NX_INT16:
			value = (int) ((short[]) data)[0];
			break;
		case NexusFile.NX_INT8:
			value = (int) ((byte[]) data)[0];
			break;
		default:
			value = null;
			break;
		}

		return value;
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
		} else if (!dataToTxt(false, false, false).equals(other.dataToTxt(false, false, false))) {
			if (logWhenFalseInOneCase)
				logger.info("expected = "+dataToTxt(false,true,false) + ", actual= " +other.dataToTxt(false,true,false));
			return false;
		}
		return true;
	}
}
