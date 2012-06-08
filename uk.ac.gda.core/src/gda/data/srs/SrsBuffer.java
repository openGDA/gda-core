/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.srs;

import gda.data.generic.GenericData;
import gda.data.generic.IGenericData;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * <b>Title: </b>Buffer class for SRS data.
 * </p>
 * <p>
 * <b>Description: </b>Use this class to encapsulate SRS data before transportation to elsewhere (eg. a file). The
 * buffer contains a header string, a trailer string, and set of variables names and a set of data for each variable in
 * string format.
 * </p>
 */

public class SrsBuffer {
	/** String for the header. */
	private String mHeader = null;

	/** String for the trailer. */
	private String mTrailer = null;

	/** Map for the Variable/Data pairs. */
	private ConcurrentHashMap<String, Vector<String>> mData = new ConcurrentHashMap<String, Vector<String>>();

	/**
	 * Constructor.
	 */
	public SrsBuffer() {
		// Generate the default header
		this.generateDefaultHeader();
	}

	/**
	 * Constructor which takes in a GenericData. This will use the SrsBuffer#setGenericData(GenericData gdata) method to
	 * fill this SRS buffer.
	 * 
	 * @param gdata
	 * @see SrsBuffer#setGenericData(GenericData)
	 */
	public SrsBuffer(GenericData gdata) {
		this.setGenericData(gdata);

	}

	/**
	 * Put a variable and a corresponding data item into the buffer. It either adds the data to an existing variable
	 * data list, or if the variable does not exist in the buffer it adds it in along with the data.
	 * 
	 * @param var
	 *            Variable name
	 * @param data
	 *            The data item for this variable.
	 */
	public void setData(String var, String data) {
		if (mData.containsKey(var)) {
			if (mData.get(var) != null) {
				mData.get(var).add(data);
			}
		} else {
			Vector<String> newVec = new Vector<String>();
			newVec.add(data);
			mData.put(var, newVec);
		}
	}

	/**
	 * Get a list of all the variables in this SRS file.
	 * 
	 * @return Set - Variable list
	 */
	public Vector<String> getVariables() {
		Vector<String> vec = new Vector<String>();
		for (String i : mData.keySet()) {
			vec.add(i);
		}
		return vec;
	}

	/**
	 * Get the data item for a variable.
	 * 
	 * @param variable
	 *            string array.
	 * @return Vector<String>
	 */
	public Vector<String> getData(String variable) {
		return mData.get(variable);
	}

	/**
	 * Fill the header with default information.
	 */
	public void generateDefaultHeader() {
		// Add any default header information here.
	}

	/**
	 * Set a SRS header, and override any existing header. If the header does not contain the &SRS and &END flags, these
	 * will be added by this method.
	 * 
	 * @param header
	 */
	public void setHeader(String header) {
		if (!header.contains("&SRS")) {
			mHeader = "&SRS\n" + header + "\n&END\n";
		} else {
			mHeader = header;
		}
	}

	/**
	 * Get the SRS header
	 * 
	 * @return header
	 */
	public String getHeader() {
		return mHeader;
	}

	/**
	 * Get the SRS trailer.
	 * 
	 * @return trailer
	 */
	public String getTrailer() {
		return mTrailer;
	}

	/**
	 * Set the SRS trailer
	 * 
	 * @param trailer
	 */
	public void setTrailer(String trailer) {
		mTrailer = trailer;
	}

	/**
	 * Put a GenericData into the SRS buffer.
	 * 
	 * @param gdata
	 */
	public void setGenericData(GenericData gdata) {
		for (String i : gdata.keySet()) {
			// Test if the element is a vector
			if (gdata.get(i) instanceof Vector<?>) {
				// Test if the Vector elements are also vectors
				if (((Vector<?>) gdata.get(i)).elementAt(0) instanceof Vector<?>) {
					// Loop over each vector in the vector
					int vecs = ((Vector<?>) gdata.get(i)).size();
					System.err.println("vecs: " + vecs);
					for (int vec = 0; vec < vecs; vec++) {
						// Get the number of points in each vector
						int size = ((Vector<?>) (((Vector<?>) gdata.get(i)).elementAt(0))).size();
						System.err.println("vector vector size: " + size);
						for (int points = 0; points < size; points++) {
							this.setData(i + "_" + points, (((Vector<?>) (((Vector<?>) gdata.get(i)).elementAt(vec)))
									.elementAt(points)).toString());
						}
					}
				} else {
					// Loop over the data points, and put each one in the
					// SRS buffer.
					int size = ((Vector<?>) gdata.get(i)).size();
					System.err.println("vector size: " + size);
					for (int points = 0; points < size; points++) {
						this.setData(i, (String) ((Vector<?>) gdata.get(i)).elementAt(points));
					}
				}
			} else {
				this.setHeader((String) gdata.get(i));
			}
		}
	}

	/**
	 * Return a GenericData object. This method converts this SRS buffer into a GenericData.
	 * 
	 * @return GenericData
	 */
	public IGenericData getGenericData() {
		GenericData gdata = new GenericData();
		// Get the header info
		if (this.getHeader() != null) {
			// Cut off the SRS specific header flags
			String header = this.getHeader().substring((this.getHeader().indexOf("&SRS\n") + 5),
					(this.getHeader().indexOf("\n&END")));
			gdata.put("Header", header);
		}

		// Get the data out and put into this GenericData
		for (String i : this.getVariables()) {
			gdata.put(i, this.getData(i));
		}

		return gdata;
	}

}
