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

package uk.ac.diamond.scisoft.analysis.rcp.views;

import gda.observable.IObserver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.dawnsci.plotting.jreality.overlay.Overlay1DConsumer;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay1DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;

import uk.ac.diamond.scisoft.analysis.histogram.functions.AbstractMapFunction;
import uk.ac.diamond.scisoft.analysis.histogram.functions.GlobalColourMaps;
import uk.ac.diamond.scisoft.analysis.histogram.functions.UserCustomFunction;
import uk.ac.diamond.scisoft.analysis.rcp.volimage.CommandClient;



/**
 *
 */
@Deprecated
public class TransferFunctionView extends HistogramView implements Overlay1DConsumer {

	
	private OverlayProvider oProvider = null;
	private int isoLinePrim = -1;
	private double isoValue = 0.02;
	private int currentMin = 0;
	private int currentMax = 256;
	private int customSize = 24;
	private int oldRedSelect = 3;
	private int oldGreenSelect = 3;
	private int oldBlueSelect = 3;
	private int oldAlphaSelect = 3;
	private int redCustomIDs[] = null;
	private int greenCustomIDs[] = null;
	private int blueCustomIDs[] = null;
	private int alphaCustomIDs[] = null;
	private int customSelectID = -1;
	private byte customChannelSelect = 0;
	
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		setPartName("TransferFunction ");
		histograms = new LinkedList<Dataset>();
		histogramSize = 256;
		redCustomIDs = new int[customSize];
		greenCustomIDs = new int[customSize];
		blueCustomIDs = new int[customSize];
		alphaCustomIDs = new int[customSize];
		for (int i = 0; i < customSize; i++) 
		{
			redCustomIDs[i] = -1;
			greenCustomIDs[i] = -1;
			blueCustomIDs[i] = -1;
			alphaCustomIDs[i] = -1;
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		histogramPlotter.registerOverlay(this);
	}
	
	private void notifyObservers(LinkedList<IDataset> list)
	{
		Iterator<IObserver> iter = observers.iterator();
		while (iter.hasNext()) 
		{
			IObserver ob = iter.next();
			ob.update(this, list);
		}
	}
	

	@Override
	protected boolean hasData() {
		return histograms.size() > 0;
	}
	
	@Override
	protected void generateHistogramUpdate() {
		if (histograms.size() > 0)
		{
			double maxValue = histograms.get(4).max().doubleValue();
			DoubleDataset red = (DoubleDataset)histograms.get(0);
			red.idivide(maxValue);
			DoubleDataset green = (DoubleDataset)histograms.get(1);
			green.idivide(maxValue);
			DoubleDataset blue = (DoubleDataset)(histograms.get(2));
			blue.idivide(maxValue);
			DoubleDataset alpha = (DoubleDataset)(histograms.get(3));
			alpha.idivide(maxValue);
			LinkedList<IDataset> transferFuncList = new LinkedList<IDataset>();
			transferFuncList.add(red);
			transferFuncList.add(green);
			transferFuncList.add(blue);
			transferFuncList.add(alpha);
			notifyObservers(transferFuncList);
		}
	}
	
	private void generateHistogramUpdate(int min, int max)
	{
		if (histograms.size() > 0)
		{
			AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(cmbRedColour
					.getSelectionIndex());
			AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(cmbGreenColour
					.getSelectionIndex());
			AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(cmbBlueColour
					.getSelectionIndex());
			AbstractMapFunction alphaFunc = GlobalColourMaps.mappingFunctions.get(cmbAlpha.getSelectionIndex());
			DoubleDataset redTransfer = DatasetFactory.zeros(DoubleDataset.class, 256);
			for (int i = 0; i < min; i++) {
				double value = redFunc.mapFunction(0.0);
				if (curRedInverse)
					value = 1.0 - value;					
				redTransfer.set(value,i);
			}
			for (int i = min; i < max; i++) {
				double x = (double)(i-min)/(double)(max-min);
				double value = redFunc.mapFunction(x);
				if (curRedInverse)
					value = 1.0 - value;
				
				redTransfer.set(value, i);
			}
			for (int i = max; i < 256; i++) {
				double value = redFunc.mapFunction(1.0);
				if (curRedInverse)
					value = 1.0 - value;
				redTransfer.set(value,i);
			}
			
			DoubleDataset greenTransfer = DatasetFactory.zeros(DoubleDataset.class, 256);
			for (int i = 0; i < min; i++) {
				double value = greenFunc.mapFunction(0.0);
				if (curGreenInverse)
					value = 1.0 - value;
				greenTransfer.set(value,i);
			}
			for (int i = min; i < max; i++) {
				double x = (double)(i-min)/(double)(max-min);
				double value = greenFunc.mapFunction(x);
				if (curGreenInverse)
					value = 1.0 - value;

				greenTransfer.set(value, i);
			}
			for (int i = max; i < 256; i++) {
				double value = greenFunc.mapFunction(1.0);
				if (curGreenInverse)
					value = 1.0 - value;

				greenTransfer.set(value,i);
			}
			
			DoubleDataset blueTransfer = DatasetFactory.zeros(DoubleDataset.class, 256);
			for (int i = 0; i < min; i++) {
				double value = blueFunc.mapFunction(0.0);
				if (curBlueInverse)
					value = 1.0 - value;
				blueTransfer.set(value,i);
			}
			for (int i = min; i < max; i++) {
				double x = (double)(i-min)/(double)(max-min);
				double value = blueFunc.mapFunction(x);
				if (curBlueInverse)
					value = 1.0 - value;

				blueTransfer.set(value, i);
			}
			for (int i = max; i < 256; i++) {
				double value = blueFunc.mapFunction(1.0);
				if (curBlueInverse)
					value = 1.0 - value;

				blueTransfer.set(value,i);
			}
			
			DoubleDataset alphaTransfer = DatasetFactory.zeros(DoubleDataset.class, 256);
			for (int i = 0; i < min; i++) {
				double value = alphaFunc.mapFunction(0.0);
				if (curAlphaInverse)
					value = 1.0 - value;
				alphaTransfer.set(value,i);
			}
			for (int i = min; i < max; i++) {
				double x = (double)(i-min)/(double)(max-min);
				double value = alphaFunc.mapFunction(x);
				if (curAlphaInverse)
					value = 1.0 - value;

				alphaTransfer.set(value, i);
			}
			for (int i = max; i < 256; i++) {
				double value = alphaFunc.mapFunction(1.0);
				if (curAlphaInverse)
					value = 1.0 - value;

				alphaTransfer.set(value,i);
			}
			
			LinkedList<IDataset> transferFuncList = new LinkedList<IDataset>();
			transferFuncList.add(redTransfer);
			transferFuncList.add(greenTransfer);
			transferFuncList.add(blueTransfer);
			transferFuncList.add(alphaTransfer);
			notifyObservers(transferFuncList);
		}
	}
	
	private void updateHistogram()
	{
		IDataset histogram = histograms.get(0);
		xAxis.clear();
		xAxis.setValues(DatasetFactory.createRange(histogram.getSize(), Dataset.FLOAT64));
		histogramPlotter.clearZoomHistory();
		histogramPlotter.setXAxisValues(xAxis, 1);
		updateChannelGraphs();
		buildGradientImage();
		if (oProvider == null)
			histogramPlotter.registerOverlay(this);					
		else if (isoLinePrim == -1)
			isoLinePrim = oProvider.registerPrimitive(PrimitiveType.LINE);
	}
	
	private void updateHistogram(double min, double max)
	{
		int startPos = (int)Math.floor(min);
		int endPos = (int)Math.ceil(max);
		histograms.clear();
		Dataset newHisto = data.getSlice(new int[]{startPos},new int[]{endPos}, new int[]{1});
		xAxis.clear();
		xAxis.setValues(DatasetFactory.createRange(newHisto.getSize(), Dataset.ARRAYFLOAT64));
		histograms.add(newHisto);
		histogramPlotter.setXAxisValues(xAxis,1);

		updateChannelGraphs();
	}
	
	@Override
	public void update(Object theObserved, Object changeCode) {
		if (theObserved instanceof CommandClient) {
			@SuppressWarnings("unchecked") ArrayList<Integer> list = (ArrayList<Integer>)changeCode;
			histograms.clear();
			data = DatasetFactory.createFromList(list);
			histograms.add(data);
			cmbAlpha.select(3);
			parent.getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateHistogram();
					generateHistogramUpdate();
					histogramPlotter.refresh(true);
					drawIsoLine();
				}
			});			
		}
		if (theObserved.equals(histogramUI)) {
			if (changeCode instanceof AreaSelectEvent) {
				AreaSelectEvent event = (AreaSelectEvent) changeCode;
				final double min = event.getPosition()[0];
				final double max = event.getPosition()[1];
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						currentMin = (int)Math.floor(min);
						currentMax = (int)Math.ceil(max);
						updateHistogram(min, max);
						generateHistogramUpdate((int)Math.floor(min),(int)Math.ceil(max));	

						histogramPlotter.refresh(true);
					}
				});
			}
		}		
	}

	private void drawCustomBoxes() {
		if (oProvider != null) {
			oProvider.begin(OverlayType.VECTOR2D);
			IDataset histo =histograms.get(histograms.size() - 1);
			double yRange = histo.max().doubleValue();
			AbstractMapFunction currentFunc = 
				GlobalColourMaps.mappingFunctions.get(curRedSelect);
			double deltaY = (1.0 / 64.0) * yRange;
			// draw red
			if (currentFunc instanceof UserCustomFunction) {
				for (int i = 0; i < customSize; i++) {
					if (redCustomIDs[i] != -1) {
						oProvider.setColour(redCustomIDs[i],java.awt.Color.RED);
						double yValue = currentFunc.mapFunction(i/(double)customSize);
						if (curRedInverse)
							yValue = 1.0 - yValue;
						double xPos = i * ((double)256/(double)customSize);
						double yPos = yValue * yRange;
						((Overlay1DProvider)oProvider).drawBox(redCustomIDs[i], xPos-4, yPos-deltaY, xPos+4, yPos+deltaY);
					}
				}
			}
			currentFunc =
				GlobalColourMaps.mappingFunctions.get(curGreenSelect);
			// draw green
			if (currentFunc instanceof UserCustomFunction) {
				for (int i = 0; i < customSize; i++) {
					if (greenCustomIDs[i] != -1) {
						oProvider.setColour(greenCustomIDs[i],java.awt.Color.GREEN);
						double yValue = currentFunc.mapFunction(i/(double)customSize);
						if (curGreenInverse)
							yValue = 1.0 - yValue;

						double xPos = i * ((double)256/(double)customSize);
						double yPos = yValue * yRange;
						((Overlay1DProvider)oProvider).drawBox(greenCustomIDs[i], xPos-4, yPos-deltaY, xPos+4, yPos+deltaY);
					}
				}							
			}
			currentFunc =
				GlobalColourMaps.mappingFunctions.get(curBlueSelect);
			// draw blue
			if (currentFunc instanceof UserCustomFunction) {
				for (int i = 0; i < customSize; i++) {
					if (blueCustomIDs[i] != -1) {
						oProvider.setColour(blueCustomIDs[i],java.awt.Color.BLUE);
						double yValue = currentFunc.mapFunction(i/(double)customSize);
						if (curBlueInverse)
							yValue = 1.0 - yValue;

						double xPos = i * ((double)256/(double)customSize);
						double yPos = yValue * yRange;
						((Overlay1DProvider)oProvider).drawBox(blueCustomIDs[i], xPos-4, yPos-deltaY, xPos+4, yPos+deltaY);
					}
				}							
			}
			currentFunc =
				GlobalColourMaps.mappingFunctions.get(curAlphaSelect);
			// draw alpha
			if (currentFunc instanceof UserCustomFunction) {
				for (int i = 0; i < customSize; i++) {
					if (alphaCustomIDs[i] != -1) {
						oProvider.setColour(alphaCustomIDs[i],java.awt.Color.YELLOW);
						double yValue = currentFunc.mapFunction(i/(double)customSize);
						if (curAlphaInverse)
							yValue = 1.0 - yValue;

						double xPos = i * ((double)256/(double)customSize);
						double yPos = yValue * yRange;
						((Overlay1DProvider)oProvider).drawBox(alphaCustomIDs[i], xPos-4, yPos-deltaY, xPos+4, yPos+deltaY);
					}
				}							
			}			
			oProvider.end(OverlayType.VECTOR2D);
		}
	}
	private void drawIsoLine()
	{
		if (isoLinePrim != -1 && oProvider != null)
		{
			oProvider.begin(OverlayType.VECTOR2D);
			oProvider.setLineThickness(isoLinePrim, 3.0);
			oProvider.setColour(isoLinePrim, java.awt.Color.MAGENTA);
			((Overlay1DProvider)oProvider).drawLine(isoLinePrim, isoValue * 256.0, 0.0, isoValue * 256.0, Float.MAX_VALUE);
			oProvider.end(OverlayType.VECTOR2D);
		}
		
	}
	
	/**
	 * @param newIsoValue
	 */
	public void setIsoValue(float newIsoValue)
	{
		this.isoValue = newIsoValue; 
		drawIsoLine();
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		this.oProvider = provider;
	}


	@Override
	public void removePrimitives() {
		isoLinePrim = -1;		
	}


	@Override
	public void unregisterProvider() {
		oProvider = null;		
	}


	@Override
	public void areaSelected(final AreaSelectEvent event) {
		
		if (event.getMode() == 0) {
			boolean found = false;
			customSelectID = event.getPrimitiveID();
			if (customSelectID != -1) {
				for (int i = 0; i < customSize; i++) if (customSelectID == redCustomIDs[i])
					{
						customSelectID = i;
						customChannelSelect = 0;
						found = true;
						break;
					}
				if (found == false) {
					for (int i = 0; i < customSize; i++) if (customSelectID == greenCustomIDs[i])
					{
						customSelectID = i;
						customChannelSelect = 1;
						found = true;
						break;
					}					
				} 
				if (found == false) {
					for (int i = 0; i < customSize; i++) if (customSelectID == blueCustomIDs[i])
					{
						customSelectID = i;
						customChannelSelect = 2;
						found = true;
						break;
					}					
				}
				if (found == false) {
					for (int i = 0; i < customSize; i++) if (customSelectID == alphaCustomIDs[i])
					{
						customSelectID = i;
						customChannelSelect = 3;
						found = true;
						break;
					}					
				}				
			}
		} else if (event.getMode() == 1) {
			if (customSelectID != -1) {
	//			parent.getDisplay().asyncExec(new Runnable() {
					
	//				public void run() {
				UserCustomFunction func = null;
				switch (customChannelSelect) {
					case 0 :
						func = (UserCustomFunction)GlobalColourMaps.mappingFunctions.get(curRedSelect);
					break;
					case 1 :
						func = (UserCustomFunction)GlobalColourMaps.mappingFunctions.get(curGreenSelect);
					break;					
					case 2 :
						func = (UserCustomFunction)GlobalColourMaps.mappingFunctions.get(curBlueSelect);
					break;
					case 3 :
						func = (UserCustomFunction)GlobalColourMaps.mappingFunctions.get(curAlphaSelect);
					break;					
				}
				IDataset histogram = histograms.get(0);
				double value = event.getPosition()[1] / histogram.max().doubleValue();
				if (func != null)
					func.setValue(customSelectID, value);
				updateChannelGraphs();
				drawCustomBoxes();						
	//			});
			}
		} else if (event.getMode() == 2) {
			if (customSelectID != -1) {
				parent.getDisplay().asyncExec(new Runnable() {
					@Override
					public void run() {
						generateHistogramUpdate(currentMin, currentMax);
						buildGradientImage();
					}				
				});
			}
		}
/*		parent.getDisplay().asyncExec(new Runnable() {
			public void run() {
				AbstractMapFunction redFunc = mappingFunctions.get(cmbRedColour
						.getSelectionIndex());
				AbstractMapFunction greenFunc = mappingFunctions.get(cmbGreenColour
						.getSelectionIndex());
				AbstractMapFunction blueFunc = mappingFunctions.get(cmbBlueColour
						.getSelectionIndex());
				AbstractMapFunction alphaFunc = mappingFunctions.get(cmbAlpha.getSelectionIndex());
				if (redFunc instanceof UserCustomFunction ||
					greenFunc instanceof UserCustomFunction ||
					blueFunc instanceof UserCustomFunction ||
					alphaFunc instanceof UserCustomFunction) {					
					int xPos = (int)Math.round(event.getPosition()[0]);
					DataSet histogram = histograms.get(0);
					double value = event.getPosition()[1] / histogram.max();
					if (redFunc instanceof UserCustomFunction) {
						((UserCustomFunction)redFunc).setValue(xPos, value);
					}
					if (greenFunc instanceof UserCustomFunction) {
						((UserCustomFunction)greenFunc).setValue(xPos, value);
					}					
					if (blueFunc instanceof UserCustomFunction) {
						((UserCustomFunction)blueFunc).setValue(xPos, value);
					}					
					if (alphaFunc instanceof UserCustomFunction) {
						((UserCustomFunction)alphaFunc).setValue(xPos, value);
					}
					updateChannelGraphs();
					if (event.getMode() == 2) {
						generateHistogramUpdate(currentMin, currentMax);
						buildGradientImage();
					}
				}
			}
		}); */
	}	

	private void removeOldRedPrimitives() {
		if (oldRedSelect != cmbRedColour.getSelectionIndex()) {
			AbstractMapFunction oldFunc = 
				GlobalColourMaps.mappingFunctions.get(oldRedSelect); 
				
			if (oldFunc instanceof UserCustomFunction) {
				if (oProvider != null) {
					ArrayList<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < customSize; i++) {
						list.add(redCustomIDs[i]);
						redCustomIDs[i] = -1;
					}
					oProvider.unregisterPrimitive(list);
				}
			}
		}		
	}
	
	private void removeOldGreenPrimitives() {
		if (oldGreenSelect != cmbGreenColour.getSelectionIndex()) {
			AbstractMapFunction oldFunc = 
				GlobalColourMaps.mappingFunctions.get(oldGreenSelect); 
				
			if (oldFunc instanceof UserCustomFunction) {
				if (oProvider != null) {
					ArrayList<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < customSize; i++) {
						list.add(greenCustomIDs[i]);
						greenCustomIDs[i] = -1;
					}
					oProvider.unregisterPrimitive(list);
				}
			}
		}		
	}
	
	private void removeOldBluePrimitives() {
		if (oldBlueSelect != cmbBlueColour.getSelectionIndex()) {
			AbstractMapFunction oldFunc = 
				GlobalColourMaps.mappingFunctions.get(oldBlueSelect); 
				
			if (oldFunc instanceof UserCustomFunction) {
				if (oProvider != null) {
					ArrayList<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < customSize; i++) {
						list.add(blueCustomIDs[i]);
						blueCustomIDs[i] = -1;
					}
					oProvider.unregisterPrimitive(list);
				}
			}
		}		
	}

	private void removeOldAlphaPrimitives() {
		if (oldAlphaSelect != cmbAlpha.getSelectionIndex()) {
			AbstractMapFunction oldFunc = 
				GlobalColourMaps.mappingFunctions.get(oldAlphaSelect); 
				
			if (oldFunc instanceof UserCustomFunction) {
				if (oProvider != null) {
					ArrayList<Integer> list = new ArrayList<Integer>();
					for (int i = 0; i < customSize; i++) {
						list.add(alphaCustomIDs[i]);
						alphaCustomIDs[i] = -1;
					}
					oProvider.unregisterPrimitive(list);
				}
			}
		}		
	}	
	
	@Override
	protected void handleChange(int channel) {
		switch (channel) {
			case 0:
			{
				AbstractMapFunction currentFunc = 
					GlobalColourMaps.mappingFunctions.get(cmbRedColour.getSelectionIndex());
				if (currentFunc instanceof UserCustomFunction) {
					AbstractMapFunction redFunc = GlobalColourMaps.mappingFunctions.get(oldRedSelect);
					for (int i = 0; i < customSize; i++) {
						double value = redFunc.mapFunction((i / (double)customSize));						
						((UserCustomFunction)currentFunc).setValue(i, value);
						if (oProvider != null)
							redCustomIDs[i] = oProvider.registerPrimitive(PrimitiveType.BOX);
					}
					drawCustomBoxes();
				} else
					removeOldRedPrimitives();
				oldRedSelect = cmbRedColour.getSelectionIndex();
				
			}
			break;
			case 1:
			{
				AbstractMapFunction currentFunc = 
					GlobalColourMaps.mappingFunctions.get(cmbGreenColour.getSelectionIndex());
				if (currentFunc instanceof UserCustomFunction) {
					AbstractMapFunction greenFunc = GlobalColourMaps.mappingFunctions.get(oldGreenSelect);
					for (int i = 0; i < customSize; i++) {
						double value = greenFunc.mapFunction((i / (double)customSize));
						((UserCustomFunction)currentFunc).setValue(i, value);
						if (oProvider != null)
							greenCustomIDs[i] = oProvider.registerPrimitive(PrimitiveType.BOX);
					}
					drawCustomBoxes();
				} else
					removeOldGreenPrimitives();
				oldGreenSelect = cmbGreenColour.getSelectionIndex();
			}
			break;
			case 2:
			{
				AbstractMapFunction currentFunc = 
					GlobalColourMaps.mappingFunctions.get(cmbBlueColour.getSelectionIndex());
				if (currentFunc instanceof UserCustomFunction) {
					AbstractMapFunction blueFunc = GlobalColourMaps.mappingFunctions.get(oldBlueSelect);
					for (int i = 0; i < customSize; i++) {
						double value = blueFunc.mapFunction((i / (double)customSize));
						((UserCustomFunction)currentFunc).setValue(i, value);
						if (oProvider != null)
							blueCustomIDs[i] = oProvider.registerPrimitive(PrimitiveType.BOX);
					}
					drawCustomBoxes();
				} else
					removeOldBluePrimitives();
				oldBlueSelect = cmbBlueColour.getSelectionIndex();
			}
			break;
			case 3:
			{
				AbstractMapFunction currentFunc = 
					GlobalColourMaps.mappingFunctions.get(cmbAlpha.getSelectionIndex());
				if (currentFunc instanceof UserCustomFunction) {
					AbstractMapFunction alphaFunc = GlobalColourMaps.mappingFunctions.get(oldAlphaSelect);
					for (int i = 0; i < customSize; i++) {
						double value = alphaFunc.mapFunction((i / (double)customSize));
						((UserCustomFunction)currentFunc).setValue(i, value);
						if (oProvider != null)
							alphaCustomIDs[i] = oProvider.registerPrimitive(PrimitiveType.BOX);
					}
					drawCustomBoxes();
				} else
					removeOldAlphaPrimitives();
				oldAlphaSelect = cmbAlpha.getSelectionIndex();
			}
			break;
		}
	}
}
