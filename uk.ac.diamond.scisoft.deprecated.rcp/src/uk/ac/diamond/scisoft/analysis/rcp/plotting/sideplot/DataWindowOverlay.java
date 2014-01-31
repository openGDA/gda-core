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

package uk.ac.diamond.scisoft.analysis.rcp.plotting.sideplot;


import gda.observable.IObservable;
import gda.observable.IObserver;

import java.util.Iterator;
import java.util.LinkedList;

import org.dawnsci.plotting.jreality.impl.DataSet3DPlot3D;
import org.dawnsci.plotting.jreality.overlay.Overlay2DConsumer;
import org.dawnsci.plotting.jreality.overlay.Overlay2DProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayProvider;
import org.dawnsci.plotting.jreality.overlay.OverlayType;
import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;
import org.dawnsci.plotting.jreality.overlay.primitives.PrimitiveType;
import org.dawnsci.plotting.jreality.tool.IImagePositionEvent;
import org.dawnsci.plotting.roi.SurfacePlotROI;
import org.eclipse.jface.preference.IPreferenceStore;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.histogram.ColorMappingUpdate;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.rcp.views.DataWindowView;

/**
 *
 */
@Deprecated
public class DataWindowOverlay implements Overlay2DConsumer, IObservable {

	private Overlay2DProvider provider = null;
	private LinkedList<IObserver> observers;
	private DataWindowView view;
	private int selectPrimID = -1;
	private int selectStartX;
	private int selectStartY;
	private int selectEndX;
	private int selectEndY;
	private int oldSelectEndX = 0;
	private int oldSelectEndY = 0;
	private int xScale;
	private int yScale;
	private boolean allowUndersampling;
	private int xSamplingMode;
	private int ySamplingMode;
	private int xAspect = 0;
	private int yAspect = 0;
	
	private java.awt.Color outLineColour = java.awt.Color.green;
	private static final java.awt.Color overSampleLineColour = java.awt.Color.orange;
	private static final java.awt.Color normalColour = java.awt.Color.green;
	
	/**
	 * Constructor of the DataWindowOverlay
	 * @param xScaling scaling factor for the x dimension
	 * @param yScaling scaling factor for the y dimension
	 * @param view View this overlay is connected to
	 */
	
	public DataWindowOverlay(int xScaling, int yScaling,
							 DataWindowView view) {
		xScale = xScaling;
		yScale = yScaling;
		observers = new LinkedList<IObserver>();
		this.view = view;
	}
	
	
	/**
	 * Set the scaling factors 
	 * @param xScaling scaling factor for the x dimension
	 * @param yScaling scaling factor for the y dimension
	 */
	
	public void setScaling(int xScaling, int yScaling) {
		xScale = xScaling;
		yScale = yScaling;
		selectStartX = 0;
		selectStartY = 0;
		selectEndX = DataSet3DPlot3D.MAXDIM / xScaling;
		selectEndY = DataSet3DPlot3D.MAXDIM / yScaling;
		selectPrimID = -1;
		drawOverlay();
	}
	
	public void setAllowUndersampling(boolean newRule) {
		allowUndersampling = newRule;
	}
	
	public void setSamplingMode(int xMode, int yMode) {
		xSamplingMode = xMode;
		ySamplingMode = yMode;
	}
	
	public void setAspects(int newXAspect, int newYAspect) {
		xAspect = newXAspect;
		yAspect = newYAspect;
	}
	
	/**
	 * Set new selection position 
	 * @param startX start position in x dimension
	 * @param startY start position in y dimension
	 * @param width
	 * @param height
	 */
	public void setSelectPosition(int startX, int startY, int width, int height) {
		setSelectPosition(startX, startY, width, height, 0, 0);
	}

	/**
	 * Set new selection position
	 * @param startX start position in x dimension
	 * @param startY start position in y dimension
	 * @param width
	 * @param height
	 * @param xSize
	 * @param ySize
	 */
	public void setSelectPosition(int startX, int startY, int width, int height, int xSize, int ySize) {
		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM)
			if (selectPrimID == -1) 
				selectPrimID = provider.registerPrimitive(PrimitiveType.BOX);
		
		selectStartX = startX / xScale;
		selectStartY = startY / yScale;
		selectEndX = selectStartX + (width / xScale);
		selectEndY = selectStartY + (height / yScale);
		if(getDefaultPlottingSystemChoice()==PreferenceConstants.PLOT_VIEW_DATASETPLOTTER_PLOTTING_SYSTEM){
			if (!allowUndersampling)
				clampToMax();
			else
				checkIfAboveMax();
		
			drawOverlay();
		}
		view.setSpinnerValues(selectStartX * xScale, selectStartY * yScale,
				  Math.abs(selectEndX - selectStartX) * xScale,
				  Math.abs(selectEndY - selectStartY) * yScale);
		// verify that the start and end point are within the data size
		if(xSize != 0 && ySize != 0){
			if(selectStartX < 0) selectStartX = 0;
			if(selectStartY < 0) selectStartY = 0;
			if(selectEndX > xSize) selectEndX = xSize;
			if(selectEndY > ySize) selectEndY = ySize;
		}
		notifyObservers();
	}
	
	@Override
	public void hideOverlays() {
		// Nothing to do

	}

	@Override
	public void showOverlays() {
		// Nothing to do

	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		this.provider = (Overlay2DProvider)provider;
	}

	private void drawOverlay() {
		provider.begin(OverlayType.VECTOR2D);
		if (selectPrimID != -1) {
		    //provider.drawSector(primID, 100,100,40,50,45,90);
		    provider.setStyle(selectPrimID, VectorOverlayStyles.OUTLINE);
		    provider.setColour(selectPrimID, outLineColour);
		    provider.drawBox(selectPrimID, selectStartX, selectStartY, 
		    							   selectEndX, selectEndY);
		}
		provider.end(OverlayType.VECTOR2D);
	}
	
	@Override
	public void removePrimitives() {
		if (selectPrimID != -1)
			provider.unregisterPrimitive(selectPrimID);
	}

	@Override
	public void unregisterProvider() {
		provider = null;
	}

	private void checkIfAboveMax() {
		int distX = Math.abs(selectEndX-selectStartX) * xScale;
		int distY = Math.abs(selectEndY-selectStartY) * yScale;
        if (distX * distY > DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) {
        	outLineColour = overSampleLineColour;
        } else
        	outLineColour = normalColour;
	}
	
	private void clampToMax() {
		int distX = Math.abs(selectEndX-selectStartX) * xScale;
		int distY = Math.abs(selectEndY-selectStartY) * yScale;
        if (distX * distY > DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) {
        	float ratio = (float)(DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) /
        				   (float)(distX * distY);
        	
        	int deltaX = Math.abs(selectEndX - oldSelectEndX);
        	int deltaY = Math.abs(selectEndY - oldSelectEndY);
        	
        	float xChangeRatio = (float)deltaX/(float)(deltaX+deltaY);
        	
        	float xRatio = 1.0f - (1.0f - ratio) * xChangeRatio;
        	float yRatio = 1.0f - (1.0f - ratio) * (1.0f- xChangeRatio);
        	
        	if (selectEndX > selectStartX) 
        		selectEndX = selectStartX + (int)(Math.abs(selectEndX-selectStartX) * xRatio);
        	else
        		selectEndX = selectStartX - (int)(Math.abs(selectEndX-selectStartX) * xRatio);

        	if (selectEndY > selectStartY) 
        		selectEndY = selectStartY + (int)(Math.abs(selectEndY-selectStartY) * yRatio);
        	else
        		selectEndY = selectStartY - (int)(Math.abs(selectEndY-selectStartY) * yRatio);	
       	
        }

	}
	
	@Override
	public void imageDragged(IImagePositionEvent event) {
		selectEndX = event.getImagePosition()[0];
		selectEndY = event.getImagePosition()[1];
		if (!allowUndersampling)
			clampToMax();
		else
			checkIfAboveMax();
		drawOverlay();
		view.setSpinnerValues(selectStartX * xScale, selectStartY * yScale,
				  Math.abs(selectEndX - selectStartX) * xScale,
				  Math.abs(selectEndY - selectStartY) * yScale);

		oldSelectEndX = selectEndX;
		oldSelectEndY = selectEndY;
	}

	@Override
	public void imageFinished(IImagePositionEvent event) {
		selectEndX = event.getImagePosition()[0];
		selectEndY = event.getImagePosition()[1];
		if (!allowUndersampling) 
			clampToMax();
		else
			checkIfAboveMax();
		drawOverlay();
		view.setSpinnerValues(selectStartX * xScale, selectStartY * yScale,
				  Math.abs(selectEndX - selectStartX) * xScale,
				  Math.abs(selectEndY - selectStartY) * yScale);
		
		notifyObservers();
	}

	@Override
	public void imageStart(IImagePositionEvent event) {
		if (selectPrimID == -1) 
			selectPrimID = provider.registerPrimitive(PrimitiveType.BOX);
		selectStartX = event.getImagePosition()[0];
		selectStartY = event.getImagePosition()[1];
		oldSelectEndX = selectStartX;
		oldSelectEndY = selectStartY;
	}


	private void notifyObservers() {
		int distX = Math.abs(selectEndX-selectStartX) * xScale;
		int distY = Math.abs(selectEndY-selectStartY) * yScale;
        int xSampleMode = ((distX * distY > 
        					DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) ? xSamplingMode : 0);
        int ySampleMode = ((distX * distY > 
							DataSet3DPlot3D.MAXDIM * DataSet3DPlot3D.MAXDIM) ? ySamplingMode : 0);
        
		SurfacePlotROI roi = new SurfacePlotROI(selectStartX * xScale, 
												selectStartY * yScale, 
												selectEndX * xScale,
												selectEndY * yScale,
												xSampleMode,
												ySampleMode,
												xAspect,
												yAspect);
		Iterator<IObserver> iter = observers.iterator();
		while (iter.hasNext()) {
			IObserver observer = iter.next();
			observer.update(this, roi);
		}
	}

	/**
	 * Update the color mapping
	 * @param update
	 */
	public void updateColorMapping(ColorMappingUpdate update){
		Iterator<IObserver> iter = observers.iterator();
		while (iter.hasNext()) {
			IObserver observer = iter.next();
			observer.update(this, update);
		}
		notifyObservers();
	}

	@Override
	public void addIObserver(IObserver anIObserver) {
		observers.add(anIObserver);
	}


	@Override
	public void deleteIObserver(IObserver anIObserver) {
		observers.remove(anIObserver);
	}


	@Override
	public void deleteIObservers() {
		observers.clear();
	}

	public void cleanIObservers(){
		if(observers.size()>1){
			IObserver observer = observers.getLast();
			observers.clear();
			observers.add(observer);
		}
	}

	private int getDefaultPlottingSystemChoice() {
		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		return preferenceStore.isDefault(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM) ? 
				preferenceStore.getDefaultInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM)
				: preferenceStore.getInt(PreferenceConstants.PLOT_VIEW_PLOTTING_SYSTEM);
	}
}
