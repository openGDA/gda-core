/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.analysis;

import java.util.List;

import org.eclipse.dawnsci.analysis.api.io.IFileLoader;
import org.eclipse.dawnsci.analysis.api.io.IFileSaver;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.metadata.IMetadata;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.PilatusTiffLoader;

/**
 * @deprecated Use {@link LoaderFactory} and {@link DataHolder}
 */
@Deprecated
public interface IScanFileHolder {

	/**
	 * Most low level load function.
	 *
	 * @param loader
	 *            An object which implements the IFileLoader interface this specifically designed to load in the
	 *            appropriate file type
	 * @throws ScanFileHolderException
	 */
	public void load(IFileLoader loader) throws ScanFileHolderException;

	/**
	 * @param loader
	 * @param mon
	 * @throws ScanFileHolderException
	 */
	public void load(IFileLoader loader, IMonitor mon) throws ScanFileHolderException;

	/**
	 *
	 * @return a IMeataData object
	 */
	public IMetadata getMetadata();

	/**
	 * Lowest level save routine
	 *
	 * @param saver
	 *            An object which implements the IFileSaver interface this specifically designed to save in the
	 *            appropriate file type
	 * @throws ScanFileHolderException
	 */
	public void save(IFileSaver saver) throws ScanFileHolderException;

	/**
	 * function to allow the location of the pilatus conversion program to be set.
	 *
	 * @param fileName
	 * @throws ScanFileHolderException
	 * @deprecated
	 */
	@Deprecated
	public void setPilatusConversionLocation(String fileName)
			throws ScanFileHolderException;

	/**
	 * Gets the location of the file where the Pilatus conversion software is
	 *
	 * @return A string containing the filename.
	 * @throws ScanFileHolderException
	 * @deprecated
	 */
	@Deprecated
	public String getPilatusConversionLocation() throws ScanFileHolderException;

	/**
	 * Loads a pilatus tiff from the specified file into the image dataset. See the preader source code:
	 * /dls_sw/apps/PilatusReader/ for the transcription program which uses libtiff to read in a Pilatus tiff
	 * and dumps a simplified binary formatted image
	 *
	 * @param fileName
	 *            The filename of the Pilatus tiff file
	 * @throws ScanFileHolderException
	 * @deprecated Use {@link #load(IFileLoader)} with {@link PilatusTiffLoader}
	 */
	@Deprecated
	public void loadPilatusData(String fileName) throws ScanFileHolderException;

	/**
	 * Loads data from the SRS datafile specified
	 *
	 * @param fileName
	 *            Filename of the SRS file ###.dat
	 * @throws ScanFileHolderException
	 */
	public void loadSRS(String fileName) throws ScanFileHolderException;

	/**
	 * Function that displays the info in the Object in string form
	 */
	public void info();

	/**
	 * gets the double value of a single pixel from the image data (in screen coordinates)
	 *
	 * @param xCoordinate
	 *            The x coordinate of the Pixel
	 * @param yCoordinate
	 *            The Y coordinate of the Pixel
	 * @return The double value corresponding to the Pixel value at the coordinate specified
	 * @deprecated Use {@link Dataset#getDouble(int...)}
	 */
	@Deprecated
	public double getPixel(int xCoordinate, int yCoordinate);

	/**
	 * Replace a dataset of axisName. Add a new axis if not exist
	 *
	 * @param axisName
	 * @param inData
	 * @throws ScanFileHolderException
	 */
	public void setAxis(String axisName, IDataset inData) throws ScanFileHolderException;

	/**
	 * This function takes all the entries for the specified axis across all the lines in the file and returns the
	 * result as a dataset. This will be 1D if all the data values are singles, and 2D if they are 1D vectors
	 * themselves.
	 *
	 * @param axisName
	 *            The name of the axis that is required
	 * @return A copy of the dataset
	 * @throws IllegalArgumentException
	 *             Thrown if the name cannot be found
	 */
	public Dataset getAxis(String axisName) throws IllegalArgumentException;

	/**
	 * Function that returns the appropriate dataset
	 *
	 * @param axisNumber
	 *            The number associated with the wanted axis
	 * @return A dataset which is a copy of the stored dataset
	 * @throws IllegalArgumentException
	 */
	public Dataset getAxis(int axisNumber) throws IllegalArgumentException;

	/**
	 * This function gets the X values of all the crossing points of the dataset with the particular Y value
	 *
	 * @param XAxis
	 *            Name of the X axis that needs to be looked at
	 * @param YAxis
	 *            Name of the Y axis that needs to be looked at
	 * @param yPosition
	 *            The position to compare the dataset too
	 * @return An array of doubles containing all the X coordinates of where the line crosses.
	 */
	public List<Double> getInterpolatedX(String XAxis, String YAxis, double yPosition);

	/**
	 * Function that uses the getInterpolatedX function but prunes the result, so that multiple crossings within a
	 * certain tolerance of the overall distance of the line length
	 *
	 * @param XAxis
	 *            The name of the X axis
	 * @param YAxis
	 *            The name of the Y axis
	 * @param yPosition
	 *            The Y Position the X values are required for
	 * @param VarianceProportion
	 *            The proportion of the overall X distance to smear the results.
	 * @return A vector containing all the unique crossing points
	 */
	public List<Double> getInterpolatedX(String XAxis, String YAxis, double yPosition,
			double VarianceProportion);

	/**
	 * @see #getInterpolatedX(String, String, double)
	 * @deprecated Use {@link DatasetUtils#crossings(Dataset, Dataset, double)}
	 */
	@Deprecated
	public List<Double> getInterpolatedX(Dataset XAxis, Dataset YAxis, double yPosition);

	/**
	 * @see #getInterpolatedX(String, String, double, double)
	 * @deprecated Use {@link DatasetUtils#crossings(Dataset, Dataset, double, double)}
	 */
	@Deprecated
	public List<Double> getInterpolatedX(Dataset XAxis, Dataset YAxis, double yPosition,
			double VarianceProportion);

	/**
	 * This function takes all the entries for the specified axis across all the lines in the file and returns the
	 * result as a dataset. This will be 1D if all the data values are singles, and 2D if they are 1D vectors
	 * themselves.
	 *
	 * @param deviceName
	 *            The name of the axis that is required
	 * @return A copy of the 1 or 2D dataset
	 */
	public IDataset getDataset(String deviceName);

	/**
	 * @see #getDataset(String)
	 */
	public Dataset getDataSet(String deviceName);

	/**
	 * @see #setAxis(String, IDataset)
	 */
	public void setDataset(String name, IDataset inData) throws ScanFileHolderException;

	/**
	 * @see #setDataset(String, IDataset)
	 */
	public void setDataSet(String name, IDataset inData) throws ScanFileHolderException;

	/**
	 * Adds a dataset to the Lists
	 *
	 * @param name
	 * @param inData
	 * @throws ScanFileHolderException
	 */
	public void addDataset(String name, IDataset inData) throws ScanFileHolderException;

	/**
	 * @see #addDataset(String, IDataset)
	 */
	public void addDataSet(String name, IDataset inData) throws ScanFileHolderException;

	/**
	 * Function that lists the axis in the file that has just been loaded
	 */
	public void ls();

	/**
	 * Function that gets the minimum value in the dataset that corresponds to the Axis given
	 *
	 * @param Axis
	 *            The name of the axis to be interrogated
	 * @return A double value which is the minimum value
	 */
	public double getMin(String Axis);

	/**
	 * Function that gets the maximum value in the dataset that corresponds to the Axis given
	 *
	 * @param Axis
	 *            The name of the axis to be interrogated
	 * @return A double value which is the maximum value
	 */
	public double getMax(String Axis);

	/**
	 * Function that gets the position in the dataset of the minimum value of that dataset
	 *
	 * @param Axis
	 *            The name of the axis to be interrogated
	 * @return An integer which is the minimum position (in DataSet coordinates)
	 */
	public int[] getMinPos(String Axis);

	/**
	 * Function that gets the position in the dataset of the maximum value of that dataset
	 *
	 * @param Axis
	 *            The name of the axis to be interrogated
	 * @return An integer which is the maximum position (in DataSet coordinates)
	 */
	public int[] getMaxPos(String Axis);

	/**
	 * Function that gets the position in terms of an X axis as to the location of the minimum value of a parameter
	 * specified in the Y axis
	 *
	 * @param XAxis
	 *            The name of the x axis where the data will be measured
	 * @param YAxis
	 *            The name of the axis where the minimum value is calculated
	 * @return A double corresponding to the position on the X axis where the minima in the Y axis occurs
	 */
	public double getMinPos(String XAxis, String YAxis);

	/**
	 * Function that gets the position in terms of an X axis as to the location of the maximum value of a parameter
	 * specified in the Y axis
	 *
	 * @param XAxis
	 *            The name of the x axis where the data will be measured
	 * @param YAxis
	 *            The name of the axis where the maximum value is calculated
	 * @return A double corresponding to the position on the X axis where the maxima in the Y axis occurs
	 */
	public double getMaxPos(String XAxis, String YAxis);

	/**
	 * Function that gets the centroid of a dataset Y when corresponding to the associated x values
	 *
	 * @param x
	 *            A Dataset containing the positions values of the data
	 * @param y
	 *            A Dataset containing the data values for the centroid calculation
	 * @return The double value showing the centroid of the data given the positions and values
	 * @deprecated Use {@link DatasetUtils#centroid(Dataset, Dataset...)}
	 */
	@Deprecated
	public double centroid(DoubleDataset x, DoubleDataset y);

	/**
	 * @return String[] Headings
	 */
	public String[] getHeadings();

	/**
	 * @return return the number of datasets
	 */
	public int getNumberOfAxis();

	/**
	 * Clear out all data held in holder
	 */
	public void clear();

}