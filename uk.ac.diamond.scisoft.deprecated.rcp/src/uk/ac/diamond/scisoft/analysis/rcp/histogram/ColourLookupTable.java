/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.diamond.scisoft.analysis.rcp.histogram;

import org.eclipse.dawnsci.plotting.api.jreality.core.ScaleType;
import org.eclipse.dawnsci.plotting.api.jreality.data.ColourImageData;
import org.eclipse.dawnsci.plotting.api.jreality.util.ScalingUtility;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IndexIterator;
import org.eclipse.january.dataset.RGBDataset;
import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;
import uk.ac.diamond.scisoft.analysis.histogram.functions.AbstractMapFunction;

/**
 *
 */
@Deprecated(since="GDA 8.38")
public class ColourLookupTable {

	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ColourLookupTable.class);
	private static final int LOOKUPTABLESIZE = 256;
	
	private ColourLookupTable() {
		
	}

	/**
	 * Returns a full DataSet to ImageData map 
	 * @param data DataSet that should be converted
	 * @param redFunc red channel mapping function
	 * @param greenFunc green channel mapping function
	 * @param blueFunc blue channel mapping function
	 * @param alphaFunc alpha channel mapping function
	 * @param inverseRed invert red channel
	 * @param inverseGreen invert green channel
	 * @param inverseBlue invert blue channel
	 * @param inverseAlpha invert alpha channel
	 * @param minValue minimum value
	 * @param maxValue maximum value
	 * @param useLogarithmic should the colour table be logarithmic
	 * @return the result ImageData 
	 */
	public static ColourImageData generateColourTable(IDataset data,
											    AbstractMapFunction redFunc,
											    AbstractMapFunction greenFunc,
											    AbstractMapFunction blueFunc,
											    AbstractMapFunction alphaFunc,
												boolean inverseRed,
												boolean inverseGreen,
												boolean inverseBlue,
												boolean inverseAlpha,
												double minValue,
												double maxValue,
												boolean useLogarithmic)
	{
		logger.deprecatedMethod("generateColourTable(IDataset, AbstractMapFunction, AbstractMapFunction, AbstractMapFunction, AbstractMapFunction, boolean, boolean, boolean, boolean, double, double, boolean)");
		int width = data.getShape()[1];
		int height = data.getShape()[0];
		ColourImageData returnImage = new ColourImageData(width,height);
		if (useLogarithmic)
		{
			ScalingUtility.setSmallLogFlag(false);
			minValue = ScalingUtility.valueScaler(minValue,ScaleType.LN);
			maxValue = ScalingUtility.valueScaler(maxValue, ScaleType.LN);
		}
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				
				double value = data.getDouble(y, x);
				if (useLogarithmic)
					value = ScalingUtility.valueScaler(value, ScaleType.LN);
				value = (value - minValue) / (maxValue-minValue);
				value = Math.min(value,1.0);
				value = Math.max(0.0, value);
				short red = redFunc.mapToByte(value);
				if (inverseRed)
					red = (short)(255 - red);
				short green = greenFunc.mapToByte(value);
			    if (inverseGreen)
			    	green = (short)(255 - green);
			    short blue = blueFunc.mapToByte(value);
			    if (inverseBlue)
			    	blue = (short)(255 - blue);
			    short alpha = alphaFunc.mapToByte(value);
			    if (inverseAlpha)
			    	alpha = (short)(255 - alpha);
				int imageValue =  ((alpha&0xff) << 24)+((red&0xff) << 16)+((green&0xff) << 8)+(blue&0xff);
				returnImage.set(imageValue, x+y*width);			    
			}
		}
		return returnImage;
	}

	/**
	 * Returns a dataset mapped to RGB
	 * @param data dataset that should be converted
	 * @param redFunc red channel mapping function
	 * @param greenFunc green channel mapping function
	 * @param blueFunc blue channel mapping function
	 * @param inverseRed invert red channel
	 * @param inverseGreen invert green channel
	 * @param inverseBlue invert blue channel
	 * @param minValue minimum value
	 * @param maxValue maximum value
	 * @param useLogarithmic should the colour table be logarithmic
	 * @return the result RGB dataset
	 */
	public static RGBDataset generateColourImage(IDataset data, AbstractMapFunction redFunc,
			AbstractMapFunction greenFunc, AbstractMapFunction blueFunc,
			boolean inverseRed, boolean inverseGreen, boolean inverseBlue, double minValue,
			double maxValue, boolean useLogarithmic) {
		logger.deprecatedMethod("generateColourImage(IDataset, AbstractMapFunction, AbstractMapFunction, AbstractMapFunction, boolean, boolean, boolean, double, double, boolean)");
		int[] shape = data.getShape();
		RGBDataset image = DatasetFactory.zeros(RGBDataset.class, shape);
		if (useLogarithmic) {
			ScalingUtility.setSmallLogFlag(false);
			minValue = ScalingUtility.valueScaler(minValue, ScaleType.LN);
			maxValue = ScalingUtility.valueScaler(maxValue, ScaleType.LN);
		}
		Dataset idata = DatasetUtils.convertToDataset(data);
		IndexIterator iti = image.getIterator();
		IndexIterator itd = idata.getIterator();

		short[] rgb = new short[3];
		while (iti.hasNext() && itd.hasNext()) {
			double value = idata.getElementDoubleAbs(itd.index);

			if (useLogarithmic)
				value = ScalingUtility.valueScaler(value, ScaleType.LN);

			value = (value - minValue) / (maxValue - minValue);
			value = Math.min(value, 1.0);
			value = Math.max(0.0, value);
			short red = redFunc.mapToByte(value);
			if (inverseRed)
				red = (short) (255 - red);
			rgb[0] = red;
			short green = greenFunc.mapToByte(value);
			if (inverseGreen)
				green = (short) (255 - green);
			rgb[1] = green;
			short blue = blueFunc.mapToByte(value);
			if (inverseBlue)
				blue = (short) (255 - blue);
			rgb[2] = blue;
			image.setAbs(iti.index, rgb);
		}
		return image;
	}

	/**
	 * Return a lookup Table encoded into a ImageData map
	 * @param redFunc red channel mapping function
	 * @param greenFunc green channel mapping function
	 * @param blueFunc blue channel mapping function
	 * @param alphaFunc alpha channel mapping function
	 * @param inverseRed invert red channel
	 * @param inverseGreen invert green channel
	 * @param inverseBlue invert blue channel
	 * @param inverseAlph invert alpha channel
	 * @return the result lookup table
	 */
	public static ColourImageData generateColourLookupTable(AbstractMapFunction redFunc,
													  AbstractMapFunction greenFunc,
													  AbstractMapFunction blueFunc,
													  AbstractMapFunction alphaFunc,
													  boolean inverseRed,
													  boolean inverseGreen,
													  boolean inverseBlue,
													  boolean inverseAlph)
	{
		logger.deprecatedMethod("generateColourLookupTable(AbstractMapFunction, AbstractMapFunction, AbstractMapFunction, AbstractMapFunction, boolean, boolean, boolean, boolean)");
		ColourImageData returnData = new ColourImageData(LOOKUPTABLESIZE,1);
		for (int x = 0; x < LOOKUPTABLESIZE; x++)
		{
			double currentValue = (double)x/(double)LOOKUPTABLESIZE;
			short red = redFunc.mapToByte(currentValue);
			if (inverseRed)
				red = (short) (255 - red);
			short green = greenFunc.mapToByte(currentValue);
			if (inverseGreen)
				green = (short)(255 - green);
			short blue = blueFunc.mapToByte(currentValue);
			if (inverseBlue)
				blue = (short)(255 - blue);
			short alpha = alphaFunc.mapToByte(currentValue);
			if (inverseAlph)
				alpha = (short)(255 - alpha);
			int value =  ((alpha&0xff) << 24)+((red&0xff) << 16)+((green&0xff) << 8)+(blue&0xff);
			returnData.set(value, x);
		}
		return returnData;
	}
}
