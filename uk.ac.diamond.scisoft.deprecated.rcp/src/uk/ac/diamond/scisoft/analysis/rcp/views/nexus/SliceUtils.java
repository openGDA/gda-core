/*
 * Copyright 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.scisoft.analysis.rcp.views.nexus;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiPlotMode;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlotWindow;
import uk.ac.diamond.scisoft.analysis.rcp.util.PlotUtils;
import uk.ac.gda.doe.DOEUtils;
import uk.ac.gda.monitor.ProgressMonitorWrapper;

public class SliceUtils {

    private static Logger logger = LoggerFactory.getLogger(SliceUtils.class);

    /**
     * Generates a list of slice information for a given set of dimensional data.
     * 
     * The data may have fields which use DOE annotations and hence can be expanded.
     * This allows slices to be ranges in one or more dimensions which is a simple
     * form of summing sub-sets of data.
     * 
     * @param dimsDataHolder
     * @param dataShape
     * @param sliceObject
     * @return a list of slices
     */
    public static SliceObject createSliceObject(final DimsDataList dimsDataHolder,
    		                                    final int[]        dataShape,
    		                                    final SliceObject  sliceObject)  {

    	final SliceObject currentSlice = sliceObject.clone();

    	// This ugly code results from the ugly API to the slicing.
    	final int[] start  = new int[dimsDataHolder.size()];
    	final int[] stop   = new int[dimsDataHolder.size()];
    	final int[] step   = new int[dimsDataHolder.size()];
     	AbstractDataset x  = null;
    	AbstractDataset y  = null;
    	final StringBuilder buf = new StringBuilder();

     	for (int i = 0; i < dimsDataHolder.size(); i++) {

    		final DimsData dimsData = dimsDataHolder.getDimsData(i);
    		
    		start[i] = getStart(dimsData);
    		stop[i]  = getStop(dimsData,dataShape[i]);
    		step[i]  = getStep(dimsData);

    		if (dimsData.getAxis()<0) {
     			// TODO deal with range
    			buf.append("\n ('Dimension "+(dimsData.getDimension()+1)+"' = "+(dimsData.getSliceRange()!=null?dimsData.getSliceRange():dimsData.getSlice())+")");
    		}

    		if (dimsData.getAxis()==0) {
    			x = createAxisDataset(dataShape[i]);
    			x.setName("Dimension "+(dimsData.getDimension()+1));
    			currentSlice.setX(dimsData.getDimension());
    			
    		}
    		if (dimsData.getAxis()==1) {
    			y = createAxisDataset(dataShape[i]);
    			y.setName("Dimension "+(dimsData.getDimension()+1));
    			currentSlice.setY(dimsData.getDimension());
    		}
    	}


    	if (x==null || y==null || x.getSize()<2 || y.getSize()<2) { // Nothing to plot
    		logger.debug("Cannot slice into an image because one of the dimensions is size of 1");
    		return null;
    	}


    	currentSlice.setSliceStart(start);
    	currentSlice.setSliceStop(stop);
    	currentSlice.setSliceStep(step);
    	currentSlice.setShapeMessage(buf.toString());
    	currentSlice.setAxes(Arrays.asList(new AbstractDataset[]{x,y}));

    	return currentSlice;
	}


	private static int getStart(DimsData dimsData) {
		if (dimsData.getAxis()>-1) {
			return 0;
		} else  if (dimsData.getSliceRange()!=null) {
			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
			return (int)exp[0];
		}
		return dimsData.getSlice();
	}
	
	private static int getStop(DimsData dimsData, final int size) {
		if (dimsData.getAxis()>-1) {
			return size;
		} else  if (dimsData.getSliceRange()!=null) {
			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
			return (int)exp[1];
			
		}
		return dimsData.getSlice()+1;
	}

	private static int getStep(DimsData dimsData) {
		if (dimsData.getAxis()>-1) {
			return 1;
		} else  if (dimsData.getSliceRange()!=null) {
			final double[] exp = DOEUtils.getRange(dimsData.getSliceRange(), null);
			return (int)exp[2];
			
		}
		return 1;
	}


	public static AbstractDataset createAxisDataset(int size) {
		final int[] data = new int[size];
		for (int i = 0; i < data.length; i++) data[i] = i;
		IntegerDataset ret = new IntegerDataset(data, size);
		return ret;
	}

	/**
	 * Thread safe and time consuming part of the slice.
	 * @param currentSlice
	 * @param monitor
	 */
	public static void plotSlice(final SliceObject       currentSlice,
			                     final int[]             dataShape,
			                     final GuiPlotMode       mode,
			                     final PlotWindow        plotWindow,
			                     final IProgressMonitor  monitor) throws Exception {

		final AbstractDataset slice = LoaderFactory.getSlice(currentSlice, new ProgressMonitorWrapper(monitor));
		slice.setName("Slice of "+currentSlice.getName()+" (full shape "+Arrays.toString(dataShape)+")"+currentSlice.getShapeMessage());
		
		// We sum the data in the dimensions that are not axes
		AbstractDataset sum    = slice;
		final int       len    = dataShape.length;
		for (int i = len-1; i >= 0; i--) {
			if (!currentSlice.isAxis(i) && dataShape[i]>1)
				sum = sum.sum(i);
		}

		if (currentSlice.getX() > currentSlice.getY())
			sum = sum.transpose();
		sum.setName(slice.getName());
		
		if (monitor.isCanceled()) return;
		
		PlotUtils.createPlot(sum, currentSlice.getAxes(), mode, plotWindow, monitor);


	}

}
