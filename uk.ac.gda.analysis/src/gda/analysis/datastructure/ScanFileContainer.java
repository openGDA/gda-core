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

package gda.analysis.datastructure;

import gda.analysis.numerical.linefunction.AbstractFunction;
import gda.analysis.numerical.linefunction.CompositeFunction;
import gda.analysis.numerical.linefunction.Gaussian1D;
import gda.analysis.numerical.optimization.objectivefunction.chisquared;
import gda.analysis.numerical.optimization.optimizers.mc.MonteCarlo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

/**
 * Class which holds all the data from a single file Curently this is capable of holding SRS data files, and Pilatus
 * TIFFS
 */
public class ScanFileContainer {

	/**
	 * container for all the lines of the data file
	 */
	Vector<ScanLineContainer> lines;

	/**
	 * Container for an image which is associated with the file.
	 */
	DataVector image;

	/**
	 * Location of the Pilatus Data conversion program.
	 */
	String pilatusConversionLocation = "/home/ssg37927/TIFF/PilatusTiffToDat";

	/**
	 * Constructor
	 */
	public ScanFileContainer() {

	}

	/**
	 * Gets a copy of the Image data, in a 2D DataVector format.
	 * 
	 * @return A new Datavector containing the image information.
	 */
	public DataVector getImage() {
		return (DataVector) image.clone();
	}

	/**
	 * function to allow the location of the pilatus Conversion program to be set.
	 * 
	 * @param fileName
	 */
	public void setPilatusConversionLocation(String fileName) {
		pilatusConversionLocation = fileName;
	}

	/**
	 * Loads a pilatus tiff from the specified file into the image datavector. This function curently requires the
	 * collowing c++ program to be available
	 * 
	 * <pre>
	 *  #include &lt;stdio.h&gt;
	 *  #include &lt;stdlib.h&gt;
	 *  #include &quot;tiffio.h&quot;
	 *  
	 *  //int main() {
	 *  int main(int argc, char* argv[]) { 
	 *  
	 *  printf(&quot;TIFF Loader by Mark Basham\n&quot;);
	 *  
	 *    TIFF* tif = TIFFOpen(argv[1], &quot;r&quot;);
	 *  
	 *    if (tif) {
	 *      //tdata_t *buf;
	 *      uint8 *buf;
	 *      uint32 strip;
	 *      uint32 i,j;
	 *      uint32 pixelnumber;
	 *      uint32 w, h;
	 *      
	 *      uint32 * pixels;
	 *  
	 *      
	 *      TIFFGetField(tif, TIFFTAG_IMAGEWIDTH, &amp;w);
	 *      TIFFGetField(tif, TIFFTAG_IMAGELENGTH, &amp;h);
	 *      
	 *      pixels = (uint32*) malloc(w*h*sizeof(uint32));
	 *  
	 *     printf(&quot;Size of image = %i %i total number of pixels = %i\n&quot;,w,h,w*h);
	 *  
	 *      pixelnumber = 0;
	 *  
	 *      buf = (uint8*)_TIFFmalloc(TIFFStripSize(tif));
	 *      
	 *    for (strip = 0; strip &lt; TIFFNumberOfStrips(tif); strip++){
	 *        uint32 size = TIFFReadEncodedStrip(tif,strip,buf,(tsize_t) -1);
	 *        //printf(&quot;Strip number %i with %i bytes of data\n&quot;,strip,size);
	 *       for(j = 0; j &lt; size/4; j++) {
	 *     if(buf == NULL) {
	 *       printf(&quot;Buffer is null?&quot;);
	 *     } else {
	 *       uint32 value = (uint32)(buf[j*4]);
	 *       value = value + ((uint32)(buf[(j*4)+1]))*255;
	 *       value = value + ((uint32)(buf[(j*4)+2]))*65025;
	 *       value = value + ((uint32)(buf[(j*4)+3]))*16581375;
	 *  
	 *       pixels[pixelnumber] = value;
	 *  
	 *       //printf(&quot; 4 BYTE Sequence %i = %i %i %i %i %i\n&quot;,pixelnumber,buf[j*4],buf[(j*4)+1],buf[(j*4)+2],buf[(j*4)+3],value);
	 *  
	 *     };
	 *     pixelnumber++;
	 *        };
	 *      };
	 *      _TIFFfree(buf);
	 *      
	 *      printf(&quot;Size of image = %i %i total number of pixels = %i\n&quot;,w,h,w*h);
	 *  
	 *      printf(&quot;Now save out the image as a basic file&quot;);
	 *      
	 *      FILE *fp;
	 *  
	 *      fp = fopen(argv[2],&quot;w&quot;);
	 *  
	 *      // first print out the height, followed by the width of the image
	 *      fprintf(fp,&quot;%i %i\n&quot;,h,w);
	 *  
	 *      for(j = 0; j &lt; h; j++) {
	 *        for(i = 0; i &lt; w ; i++) {
	 *  
	 *     fprintf(fp,&quot;%i &quot;, pixels[(j*w)+i]);
	 *        };
	 *        fprintf(fp,&quot;\n&quot;);
	 *      };
	 *  
	 *  
	 *      fclose(fp);
	 *  
	 *      free(pixels);
	 *  
	 *    }
	 *   
	 *    TIFFClose(tif);
	 *  
	 *    return(0);
	 *  
	 *  };
	 * </pre>
	 * 
	 * @param fileName
	 *            The filename of the Pilatus tiff file
	 */
	public void loadPilatusData(String fileName) {

		// convert the file from the 32bit Tiff to a data file
		Runtime rt = Runtime.getRuntime();

		try {
			rt.exec(pilatusConversionLocation + " " + fileName + " /tmp/PilatusTempFile.dat").waitFor();
		} catch (Exception e) {
			System.err.println(e);
			System.err
					.println("Cannot run command line call from inside gda.analysis.datastructure.ScanFileContainer.LoadPilatusData()");
		}

		try {
			BufferedReader in = new BufferedReader(new FileReader("/tmp/PilatusTempFile.dat"));
			String dataStr = "";
			String headStr = "";

			// read in the size of the image
			headStr = in.readLine();

			String[] ImageSize = headStr.split(" ");

			int imageHeight = Integer.parseInt(ImageSize[0]);
			int imageWidth = Integer.parseInt(ImageSize[1]);

			// create the vector to store the lines of data in
			double[] allData = new double[imageHeight * imageWidth];

			int counter = 0;

			// now read in the rest of the data
			while ((dataStr = in.readLine()) != null) {

				String[] dataBuffer = dataStr.split(" ");
				// now write all this data into the data list
				for (int i = 0; i < imageWidth; i++) {
					allData[counter] = Integer.parseInt(dataBuffer[i]);
					counter++;
				}
			}

			// now finaly sort out the dataVector image
			image = new DataVector(imageWidth, imageHeight, allData);

			in.close();

		} catch (IOException e) {
			System.err.println(e);
			System.err
					.println("Error reading file from inside gda.analysis.datastructure.ScanFileContainer.LoadPilatusData()");
		}

	}

	/**
	 * Loads data from the SRS datafile specified ! it is immportant however to understand that due to the limitations
	 * of the SRS format, all the hardware devices used in the scan must still be present to read in the data.
	 * 
	 * @param fileName
	 *            Filename of the SRS file ###.dat
	 */
	public void loadSRSData(String fileName) {

		// try to read the file given

		try {
			BufferedReader in = new BufferedReader(new FileReader(fileName));
			String dataStr = "";
			String headStr = "";

			// get rid of the header, and read the list of axis
			for (int i = 0; i < 7; i++) {
				headStr = in.readLine();
			}

			// create the vector
			lines = new Vector<ScanLineContainer>();

			// now read in the rest of the data
			while ((dataStr = in.readLine()) != null) {

				lines.add(new ScanLineContainer(headStr, dataStr));

			}

			in.close();

		} catch (IOException e) {
			System.out.println(e);
		}
	}

	@Override
	public String toString() {
		String Out = "";

		Integer outVal = lines.size();

		Out = Out + "ScanFileContainer object containing " + outVal.toString() + " Lines of data.";

		return Out;
	}

	/**
	 * gets the double value of a single pixel from the image data
	 * 
	 * @param xCoordinate
	 *            The x coordinate of the Pixel
	 * @param yCoordinate
	 *            The Y coordinate of the Pixel
	 * @return The double value coresponding to the Pixel value at the coordinate specified
	 */
	public double getPixel(int xCoordinate, int yCoordinate) {

		int lookup[] = { xCoordinate, yCoordinate };
		return image.getIndex(lookup);
	}

	/**
	 * This function takes all the entries for the specified axis accross all the lines in the file and returns the
	 * result as a DataVector. This will be 1D if all the datavalues are singles, and 2D if they are 1D vectors
	 * themselves.
	 * 
	 * @param AxisName
	 *            The name of the axis that is required
	 * @return A copy of the 1 or 2D DataVector
	 */
	public DataVector getAxis(String AxisName) {

		int dim = -1;

		// get the data for the first one, to base the rest on
		int dims[] = lines.get(0).getAxis(AxisName).getDimensions();

		dim = dims.length;

		// if any of the dimentions are only 1 in length, remove them

		int c = 0;
		while (c < dim) {
			// check to see if its 1
			if (dims[c] == 1) {
				dim--;
				// shift all the folowing numbers down
				for (int j = c; j < dim; j++) {
					dims[j] = dims[j + 1];
				}
			} else {
				c++;
			}
		}

		// now sort out the size of the new dataVector
		int newdim[] = new int[dim + 1];

		for (int i = 0; i < dim; i++) {
			newdim[i] = dims[i];
		}
		newdim[newdim.length - 1] = lines.size();

		DataVector result = new DataVector(newdim);

		// search through and pull out the data
		for (int i = 0; i < lines.size(); i++) {

			DataVector newData = lines.get(i).getAxis(AxisName);
			for (int j = 0; j < newData.size(); j++) {
				result.add((double) newData.get(j));
			}
		}

		return (result);
	}

	/**
	 * This function gets the axis information but transposes it, as to make for easier ploting
	 * 
	 * @param AxisName
	 *            the axis to grab
	 * @return the transposed datavector
	 */
	public DataVector getAxisT(String AxisName) {

		int dim = -1;

		// get the data for the first one, to base the rest on
		int dims[] = lines.get(0).getAxis(AxisName).getDimensions();

		dim = dims.length;

		// if any of the dimentions are only 1 in length, remove them

		int c = 0;
		while (c < dim) {
			// check to see if its 1
			if (dims[c] == 1) {
				dim--;
				// shift all the folowing numbers down
				for (int j = c; j < dim; j++) {
					dims[j] = dims[j + 1];
				}
			} else {
				c++;
			}
		}

		// now sort out the size of the new dataVector
		int newdim[] = new int[dim + 1];

		for (int i = 0; i < dim; i++) {
			// the flip takes place here initialy
			newdim[i + 1] = dims[dim - 1 - i];
		}
		newdim[0] = lines.size();

		DataVector result = new DataVector(newdim);

		// search through and pull out the data
		for (int i = 0; i < newdim[1]; i++) {
			for (int j = 0; j < newdim[0]; j++) {
				Double passValue = new Double(lines.get(j).getAxis(AxisName).get(i));
				result.add(passValue);
			}
		}

		return (result);
	}

	/**
	 * Plots the axis named on the DataVectoer Gui
	 * 
	 * @param axisName
	 *            Name of the axis to plot
	 */
	public void plot(String axisName) {

		// First get the datavector to be plotted
		DataVector toPlot = getAxis(axisName);

		// now ssort out the axis names
		Vector<String> Names = new Vector<String>();

		Names.add("Plot of " + axisName);
		Names.add("Scan Point");
		Names.add("Intensity");

		// if its bigger than a 2D forget it for the moment
		if (toPlot.getDimensions().length > 2) {
			// Throw exception
			System.err.println("x axis is bigger than 2D");
		}

		if (toPlot.getDimensions().length == 1) {
			// then we need to add the plotline name
			Names.add(axisName);
		}

		if (toPlot.getDimensions().length == 2) {
			// then we need to add a lot of names
			for (int i = 0; i < toPlot.getDimensions()[1]; i++) {
				Names.add(axisName + " Scan Point " + i);
			}
		}

	}

	/**
	 * Plots the y axis against the x axis value, the x axis must be a 1D datavector
	 * 
	 * @param xAxis
	 *            Name of the axis whihc will be the x axis
	 * @param yAxis
	 *            Name of the axis which will be the data
	 */
	public void plot(String xAxis, String yAxis) {

		int newdims[] = new int[2];

		// First get the datavectors to be plotted
		// make sure the x axis is a 1D vector
		DataVector forX = getAxis(xAxis);
		if (forX.getDimensions().length != 1) {
			// Throw Exception
			System.err.println("x axis is not 1D");
		}

		newdims[0] = forX.getDimensions()[0];

		DataVector forY = getAxis(yAxis);

		// if its bigger than a 2D forget it for the moment
		if (forY.getDimensions().length > 2) {
			// Throw exception
			System.err.println("x axis is bigger than 2D");
		}

		// if this is a 1D vector, just go ahead

		newdims[1] = 2;

		// If its a 2D vector, then it needs to be transposed, before adding it
		if (forY.getDimensions().length == 2) {
			forY = getAxisT(yAxis);
			newdims[1] = forY.getDimensions()[1] + 1;
		}

		// finaly, they need to be put together into 1 datavector

		DataVector data = new DataVector(newdims);

		// now read the data in
		for (int i = 0; i < forX.size(); i++) {
			data.add((double) forX.get(i));
		}
		for (int i = 0; i < forY.size(); i++) {
			data.add((double) forY.get(i));
		}

		// now sort out the axis names
		Vector<String> Names = new Vector<String>();

		Names.add("Plot of " + xAxis + " vrs " + yAxis);
		Names.add(xAxis);
		Names.add(yAxis);

		for (int i = 0; i < data.getDimensions()[1] - 1; i++) {
			Names.add(yAxis + " Point " + i);
		}

	}

	/**
	 * This function simply plots the data in the image 2D datavector to the Gui panel
	 */
	public void plot() {

		// plot out the Pilatus Data
		// First get the datavectors to be plotted
		// make sure the x axis is a 1D vector

		// DataVector data = image;

		// now sort out the axis names
		Vector<String> Names = new Vector<String>();

		Names.add("Plot of Pilatus");

	}

	/**
	 * @param xAxis
	 * @param yAxis
	 * @param LineType1
	 * @param Line1Centre
	 * @param Line1Variance
	 */
	public void fit(String xAxis, String yAxis, String LineType1, double Line1Centre, double Line1Variance) {

		AbstractFunction func1 = null;

		// first you need to generate the fit appropriatly
		if (LineType1.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line1Centre, 1.0 };
			func1 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func1.getParameters()[1].setLowerLimit(Line1Centre - Line1Variance);
			func1.getParameters()[1].setUpperLimit(Line1Centre + Line1Variance);
		} else {
			// Throw Exception
		}

		// now that the functions are set up, add hem into a composite function
		// object
		CompositeFunction func = new CompositeFunction();

		func.addFunction("Function 1", func1);

		// make the seperate datavectors
		DataVector vals[] = new DataVector[2];
		vals[0] = this.getAxis(xAxis);
		vals[1] = this.getAxis(yAxis);

		// now use the chisquared object to provide the objective function
		chisquared chi = new chisquared(func, vals);

		MonteCarlo monte = new MonteCarlo(chi);

		monte.Optimize(5000, -0.00001);

		// now the line function has been optimised, get the data to plot.

		DataVector output = chi.getVector();

		// now send it all accross
		sendToGui(output, "xy");

	}

	/**
	 * @param xAxis
	 * @param yAxis
	 * @param LineType1
	 * @param Line1Centre
	 * @param Line1Variance
	 * @param LineType2
	 * @param Line2Centre
	 * @param Line2Variance
	 */
	public void fit(String xAxis, String yAxis, String LineType1, double Line1Centre, double Line1Variance,
			String LineType2, double Line2Centre, double Line2Variance) {

		AbstractFunction func1 = null;
		AbstractFunction func2 = null;

		// first you need to generate the fit appropriatly
		if (LineType1.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line1Centre, 1.0 };
			func1 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func1.getParameters()[1].setLowerLimit(Line1Centre - Line1Variance);
			func1.getParameters()[1].setUpperLimit(Line1Centre + Line1Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType2.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line2Centre, 1.0 };
			func2 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func2.getParameters()[1].setLowerLimit(Line2Centre - Line2Variance);
			func2.getParameters()[1].setUpperLimit(Line2Centre + Line2Variance);
		} else {
			// Throw Exception
		}

		// now that the functions are set up, add hem into a composite function
		// object
		CompositeFunction func = new CompositeFunction();

		func.addFunction("Function 1", func1);
		func.addFunction("Function 2", func2);

		// make the seperate datavectors
		DataVector vals[] = new DataVector[2];
		vals[0] = this.getAxis(xAxis);
		vals[1] = this.getAxis(yAxis);

		// now use the chisquared object to provide the objective function
		chisquared chi = new chisquared(func, vals);

		MonteCarlo monte = new MonteCarlo(chi);

		monte.Optimize(5000, -0.00001);

		// now the line function has been optimised, get the data to plot.

		DataVector output = chi.getVector();

		// now send it all accross
		sendToGui(output, "xy");

	}

	/**
	 * @param xAxis
	 * @param yAxis
	 * @param LineType1
	 * @param Line1Centre
	 * @param Line1Variance
	 * @param LineType2
	 * @param Line2Centre
	 * @param Line2Variance
	 * @param LineType3
	 * @param Line3Centre
	 * @param Line3Variance
	 */
	public void fit(String xAxis, String yAxis, String LineType1, double Line1Centre, double Line1Variance,
			String LineType2, double Line2Centre, double Line2Variance, String LineType3, double Line3Centre,
			double Line3Variance) {

		AbstractFunction func1 = null;
		AbstractFunction func2 = null;
		AbstractFunction func3 = null;

		// first you need to generate the fit appropriatly
		if (LineType1.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line1Centre, 1.0 };
			func1 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func1.getParameters()[1].setLowerLimit(Line1Centre - Line1Variance);
			func1.getParameters()[1].setUpperLimit(Line1Centre + Line1Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType2.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line2Centre, 1.0 };
			func2 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func2.getParameters()[1].setLowerLimit(Line2Centre - Line2Variance);
			func2.getParameters()[1].setUpperLimit(Line2Centre + Line2Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType3.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line3Centre, 1.0 };
			func3 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func3.getParameters()[1].setLowerLimit(Line3Centre - Line3Variance);
			func3.getParameters()[1].setUpperLimit(Line3Centre + Line3Variance);
		} else {
			// Throw Exception
		}

		// now that the functions are set up, add hem into a composite function
		// object
		CompositeFunction func = new CompositeFunction();

		func.addFunction("Function 1", func1);
		func.addFunction("Function 2", func2);
		func.addFunction("Function 3", func3);

		// make the seperate datavectors
		DataVector vals[] = new DataVector[2];
		vals[0] = this.getAxis(xAxis);
		vals[1] = this.getAxis(yAxis);

		// now use the chisquared object to provide the objective function
		chisquared chi = new chisquared(func, vals);

		MonteCarlo monte = new MonteCarlo(chi);

		monte.Optimize(5000, -0.00001);

		// now the line function has been optimised, get the data to plot.

		DataVector output = chi.getVector();

		// now send it all accross
		sendToGui(output, "xy");

	}

	/**
	 * @param xAxis
	 * @param yAxis
	 * @param LineType1
	 * @param Line1Centre
	 * @param Line1Variance
	 * @param LineType2
	 * @param Line2Centre
	 * @param Line2Variance
	 * @param LineType3
	 * @param Line3Centre
	 * @param Line3Variance
	 * @param LineType4
	 * @param Line4Centre
	 * @param Line4Variance
	 */
	public void fit(String xAxis, String yAxis, String LineType1, double Line1Centre, double Line1Variance,
			String LineType2, double Line2Centre, double Line2Variance, String LineType3, double Line3Centre,
			double Line3Variance, String LineType4, double Line4Centre, double Line4Variance) {

		AbstractFunction func1 = null;
		AbstractFunction func2 = null;
		AbstractFunction func3 = null;
		AbstractFunction func4 = null;

		// first you need to generate the fit appropriatly
		if (LineType1.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line1Centre, 1.0 };
			func1 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func1.getParameters()[1].setLowerLimit(Line1Centre - Line1Variance);
			func1.getParameters()[1].setUpperLimit(Line1Centre + Line1Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType2.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line2Centre, 1.0 };
			func2 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func2.getParameters()[1].setLowerLimit(Line2Centre - Line2Variance);
			func2.getParameters()[1].setUpperLimit(Line2Centre + Line2Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType3.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line3Centre, 1.0 };
			func3 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func3.getParameters()[1].setLowerLimit(Line3Centre - Line3Variance);
			func3.getParameters()[1].setUpperLimit(Line3Centre + Line3Variance);
		} else {
			// Throw Exception
		}

		// first you need to generate the fit appropriatly
		if (LineType4.compareTo("G") == 0) {
			double[] inparams = { 1.0, Line4Centre, 1.0 };
			func4 = new Gaussian1D(inparams);
			// set the limits for the variance of the peak
			func4.getParameters()[1].setLowerLimit(Line4Centre - Line4Variance);
			func4.getParameters()[1].setUpperLimit(Line4Centre + Line4Variance);
		} else {
			// Throw Exception
		}

		// now that the functions are set up, add hem into a composite function
		// object
		CompositeFunction func = new CompositeFunction();

		func.addFunction("Function 1", func1);
		func.addFunction("Function 2", func2);
		func.addFunction("Function 3", func3);
		func.addFunction("Function 4", func4);

		// make the seperate datavectors
		DataVector vals[] = new DataVector[2];
		vals[0] = this.getAxis(xAxis);
		vals[1] = this.getAxis(yAxis);

		// now use the chisquared object to provide the objective function
		chisquared chi = new chisquared(func, vals);

		MonteCarlo monte = new MonteCarlo(chi);

		monte.Optimize(5000, -0.00001);

		// now the line function has been optimised, get the data to plot.

		DataVector output = chi.getVector();

		// now send it all accross
		sendToGui(output, "xy");

	}

	// FIXME this method doesn't do anything! I've put the @SuppressWarnings taggs in place rather than delete the 
	// method as its usage suggest that it isa intended to do something...
	private void sendToGui(@SuppressWarnings("unused") DataVector sendVector, @SuppressWarnings("unused") String Header) {

		Vector<String> V1 = new Vector<String>();
		V1.add("Data Vector");
		V1.add("X Axis");
		V1.add("Y Axis");
		V1.add("Data");
		V1.add("Fit");
		V1.add("Parameter 1");
		V1.add("Parameter 2");
		V1.add("Parameter 3");
		V1.add("Parameter 4");

	}

	/**
	 * This function gets the Y values of all the crossing points of the datavector with the particular X coordinate.
	 * 
	 * @param XAxis
	 *            Name of the X axis that needs to be looked at
	 * @param YAxis
	 *            Name of the Y axis that needs to be looked at
	 * @param xPosition
	 *            The position to compare the dataset too
	 * @return An array of doubles containing all the Y coordinates of where the line crosses.
	 */
	public Vector<Double> getInterpolatedY(String XAxis, String YAxis, double xPosition) {

		// get the appropriate DataVectors
		DataVector xVals = this.getAxis(XAxis);
		DataVector yVals = this.getAxis(YAxis);

		Vector<Double> results = new Vector<Double>();

		// run through all the different sets of points on the line, and see if
		// any set bounds the region of interest.
		for (int i = 0; i < xVals.size() - 1; i++) {
			// check thefirst direction
			if ((xVals.get(i) <= xPosition) && (xVals.get(i + 1) > xPosition)) {
				// work out the crosdsing point, and interpolate lineraly.
				double xdiff = xVals.get(i + 1) - xVals.get(i);
				double ydiff = yVals.get(i + 1) - yVals.get(i);

				double proportion = (xPosition - xVals.get(i)) / xdiff;

				results.add(yVals.get(i) + (proportion * ydiff));
			}
			// now for a negative crossing
			if ((xVals.get(i) > xPosition) && (xVals.get(i + 1) <= xPosition)) {
				// work out the crosdsing point, and interpolate lineraly.
				double xdiff = xVals.get(i) - xVals.get(i + 1);
				double ydiff = yVals.get(i) - yVals.get(i + 1);

				double proportion = (xPosition - xVals.get(i + 1)) / xdiff;

				results.add(yVals.get(i + 1) + (proportion * ydiff));
			}

		}

		return results;

	}

	/**
	 * This function gets the X values of all the crossing points of the datavector with the particular Y coordinate.
	 * 
	 * @param XAxis
	 *            Name of the X axis that needs to be looked at
	 * @param YAxis
	 *            Name of the Y axis that needs to be looked at
	 * @param yPosition
	 *            The position to compare the dataset too
	 * @return An array of doubles containing all the X coordinates of where the line crosses.
	 */
	public Vector<Double> getInterpolatedX(String XAxis, String YAxis, double yPosition) {

		// get the appropriate DataVectors
		DataVector xVals = this.getAxis(XAxis);
		DataVector yVals = this.getAxis(YAxis);

		Vector<Double> results = new Vector<Double>();

		// run through all the different sets of points on the line, and see if
		// any set bounds the region of interest.
		for (int i = 0; i < xVals.size() - 1; i++) {
			// check thefirst direction
			if ((yVals.get(i) <= yPosition) && (yVals.get(i + 1) > yPosition)) {
				// work out the crosdsing point, and interpolate lineraly.
				double ydiff = yVals.get(i + 1) - yVals.get(i);
				double xdiff = xVals.get(i + 1) - xVals.get(i);

				double proportion = (yPosition - yVals.get(i)) / ydiff;

				results.add(xVals.get(i) + (proportion * xdiff));
			}
			// now for a negative crossing
			if ((yVals.get(i) > yPosition) && (yVals.get(i + 1) <= yPosition)) {
				// work out the crosdsing point, and interpolate lineraly.
				double xdiff = xVals.get(i) - xVals.get(i + 1);
				double ydiff = yVals.get(i) - yVals.get(i + 1);

				double proportion = (yPosition - yVals.get(i + 1)) / ydiff;

				results.add(xVals.get(i + 1) + (proportion * xdiff));
			}

		}

		return results;

	}

	/**
	 * This function takes all the entries for the specified axis accross all the lines in the file and returns the
	 * result as a DataVector. This will be 1D if all the datavalues are singles, and 2D if they are 1D vectors
	 * themselves.
	 * 
	 * @param deviceName
	 *            The name of the axis that is required
	 * @return A copy of the 1 or 2D DataVector
	 */
	public DataVector getVector(String deviceName) {
		return getAxis(deviceName);
	}

}
