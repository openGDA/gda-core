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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.PaletteData;

/**
 * Used to send a palette data and a min and max value after a PaletteEvent
 */
@Deprecated
public class ColorMappingUpdate implements ISelection{

	private PaletteData palette;
	private double  minValue;
	private double  maxValue;
	
	/**
	 * Constructor of a HistogramUpdate
	 * @param palette red channel map function
	 * @param minValue minimum value
	 * @param maxValue maximum value
	 */
		
	public ColorMappingUpdate(PaletteData palette,
						   double minValue,
						   double maxValue){
		this.palette = palette;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	/**
	 * Get the PaletteData
	 * @return PaletteData
	 */
	public PaletteData getPaletteData(){
		return palette;
	}

	/**
	 * Get the minimum value
	 * @return the minimum value
	 */
	public double getMinValue(){
		return minValue;
	}

	/**
	 * Get the maximum value
	 * @return the maximum value
	 */
	public double getMaxValue(){
		return maxValue;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}
}
