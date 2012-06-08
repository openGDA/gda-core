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

package gda.data.scan.datawriter;

import gda.data.DataManagerInterface;
import gda.data.NumTracker;
import gda.data.generic.GenericData;
import gda.data.generic.IGenericData;
import gda.data.srs.SrsBuffer;
import gda.data.srs.SrsFile;
import gda.factory.Finder;
import gda.scan.IScanDataPoint;

import java.io.IOException;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>DataWriter for scan package to fill a GenericData.
 * </p>
 * <p>
 * <b>Description: </b>This class is meant to be used with the <code>gda.scan</code> package to enable the filling of
 * a <code>gda.data.generic.GenericData</code> structure during a scan.
 * </p>
 */
@Deprecated
public class GenericDataHandler extends DataWriterBase implements DataWriter {
	
	private static final Logger logger = LoggerFactory.getLogger(GenericDataHandler.class);

	private IScanDataPoint mDataPoint = null;

	private boolean mFirstData = true;

	private DataManagerInterface mDataManager = null;

	private GenericData mData = null;

	private Vector<String> mNames = null;

	private int mNumberOfElements = 0;

	private String mHeader = "NONE";

	private String mCurrentScanName = "CurrentScan";

	private String mTemporaryScanName = "TemporaryScanData567234553";

	private NumTracker mRuns = null;

	/**
	 * Constructor.
	 */
	public GenericDataHandler() {
		try {
			this.mRuns = new NumTracker("tmp");
		} catch (IOException e) {
			logger.debug("ERROR: Could not instantiate NumTracker in GenericDataHandler");
		}
	}

	/**
	 * Add a data point to the structure.
	 * 
	 * @param newData
	 * @throws Exception 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void addData(IScanDataPoint newData) throws Exception {
		mDataPoint = newData;

		if (mFirstData) {
			this.prepareForCollection();
			mFirstData = false;
		}

		// Check that the sizes are consistent
		if (mNumberOfElements != (mDataPoint.getPositions().size() + mDataPoint.getDetectorData().size())) {
			logger.error(("ERROR: corrupt ScanDataPoint in GenericDataHandler"));
		} else {
			// Add the data to the structure
			// First build a Vector of the data.
			Vector<Object> theData = new Vector<Object>(mNumberOfElements);
			for (Object i : mDataPoint.getPositions()) {
				theData.add(i);
			}
			for (Object i : mDataPoint.getDetectorData()) {
				// Use base class to determine the Object and unpack it.
				String[] strData = DataWriterBase.getDetectorData(i);
				Vector<Double> doubleData = new Vector<Double>(mNumberOfElements);
				for (int k = 0; k < strData.length; k++) {
					doubleData.add(new Double(strData[k]));
				}
				theData.add(doubleData);
			}
			theData.trimToSize();
			// Check that the size of this data is equal to the expected size
			if (theData.size() != mNumberOfElements) {
				logger.error("ERROR: Number of data numbers does not match number of data names.");
			} else {
				// This assumes that the data supplied by ScanDataPoint is in
				// the same order as the names of the scannables and detectors.
				for (int i = 0; i < mNumberOfElements; i++) {
					((Vector<Object>) mData.get(mNames.elementAt(i))).add(theData.elementAt(i));
				}
			}
		}
		super.addData(newData);
	}

	/**
	 * Add the GenericData object to the DataManager.
	 * @throws Exception 
	 */
	@Override
	public void completeCollection() throws Exception {

		// Get the current run number
		long currentRun = mRuns.getCurrentFileNumber();
		// If a previous "CurrentScan" exists in the data manager, then rename
		// it to the run number
		IGenericData oldData = mDataManager.get(mCurrentScanName);
		if (oldData != null) {
			mDataManager.remove(mCurrentScanName);
			mDataManager.add((new Long(currentRun)).toString(), oldData);
		}

		// Now add the current data structure to the data manager
		mDataManager.add(mCurrentScanName, mData);

		// Remove the temporary scan data reference.
		mDataManager.remove(mTemporaryScanName);

		// Write the structure to a file
		logger.debug("Preparing to write to file...");
		SrsFile file = new SrsFile();
		logger.debug("Getting the SRS buffer...");
		SrsBuffer buf = new SrsBuffer(mData);
		// SrsBuffer buf = mData.getSrsBuffer();
		System.err.println("buf.getHeader(): " + buf.getHeader());
		System.err.println("buf.getTrailer(): " + buf.getTrailer());
		System.err.println("buf.getVariables(): " + buf.getVariables());
		for (String i : buf.getVariables()) {
			System.err.println("buf.getData(" + i + "): " + buf.getData(i));
		}

		try {
			logger.debug("Writing the file...");
			file.writeFile(buf, "C:\\my_new_file.srs");
		} catch (IOException e) {
			logger.error("ERROR: Could not write SRS file in GenericDataHandler.");
		}

		// And increment the run number
		if (mRuns.incrementNumber() == 0) {
			logger.error("ERROR: Could not increment run number in GenericDataHandler.");
		}

		// Now add the current data structure to the data manager again, but
		// using the new run number.
		mDataManager.add(new Long(mRuns.getCurrentFileNumber()).toString(), mData);
		
		super.completeCollection();
	}

	@Override
	public String getCurrentFileName() {
		return (new Long(mRuns.getCurrentFileNumber())).toString();
	}

	/**
	 * Return a reference to the GenericData structure.
	 * 
	 * @return Object
	 */
	public Object getData() {
		return mData;
	}

	/**
	 * Use this method to initialise the data structure.
	 */
	public void prepareForCollection() {
		// Get a reference to the generic data manager
		// TODO Remove hard coded name.
		if ((mDataManager = (DataManagerInterface) Finder.getInstance().find("DataManager")) == null) {
			logger.error("ERROR: Could not find " + "DataManager" + " using Finder.");
		} else {
			// Create a new GenericData structure
			mData = new GenericData();

			// Write the header/metadata to the data structure
			this.writeHeader();

			// Get the scannable names
			mNames = mDataPoint.getScannableNames();
			// Get the detector names;
			mNames.addAll(mDataPoint.getDetectorNames());

			mNumberOfElements = mNames.size();

			// Add the names to the GenericData structure and add empty data
			// vectors
			for (String i : mNames) {
				mData.put(i, new Vector<Object>());
			}

			// Put the GenericData into the DataManager at this point, so
			// that the DataManager holds a reference to the data from the
			// beginning of the scan. If the scan stops before it is finished then a
			// reference to partial data exists in the DataManager.
			mDataManager.add(mTemporaryScanName, mData);
		}
	}

	/**
	 * Set the header to be written to the data structure, which will be labelled "header".
	 * 
	 * @param header
	 */
	@Override
	public void setHeader(String header) {
		// Set the header/metadata information here.
		mHeader = header;
	}

	/**
	 * Write the header into the data structure.
	 */
	private void writeHeader() {
		// Put the header or metadata information into the data structure here.
		mData.put("header", mHeader);
		// Add any other header information here
	}

	/**
	 * Put the footer information into the data structure. This will be labelled "footer"
	 */
	public void writeFooter() {
		// Put any footer information into the data structure.
	}

	@Override
	public String getCurrentScanIdentifier() {
		return String.valueOf(mRuns.getCurrentFileNumber());
	}

}