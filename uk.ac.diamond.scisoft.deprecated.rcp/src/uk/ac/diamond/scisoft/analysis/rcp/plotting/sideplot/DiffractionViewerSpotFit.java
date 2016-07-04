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


import java.awt.Color;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dawnsci.plotting.jreality.overlay.Overlay1DConsumer;
import org.dawnsci.plotting.jreality.tool.AreaSelectEvent;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IPeak;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DoubleDataset;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.plotting.api.jreality.core.AxisMode;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DAppearance;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DGraphTable;
import org.eclipse.dawnsci.plotting.api.jreality.impl.Plot1DStyles;
import org.eclipse.dawnsci.plotting.api.jreality.impl.PlotException;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.Overlay1DProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayProvider;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.OverlayType;
import org.eclipse.dawnsci.plotting.api.jreality.overlay.primitives.PrimitiveType;
import org.eclipse.dawnsci.plotting.api.jreality.util.PlotColorUtility;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII;
import uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.DataSetPlotter;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.PlottingMode;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;

@Deprecated
public class DiffractionViewerSpotFit extends Composite implements Overlay1DConsumer {

	private static final Logger logger = LoggerFactory.getLogger(DiffractionViewerSpotFit.class);

	private static final int DECIMAL_PLACES = 3;

	public DataSetPlotter lpPlotter;
	private DiffractionViewer diffView;

	private Overlay1DProvider provider = null;
	// private Overlay1DProvider gaussianCurves = null;

	private AxisValues axis;

	ArrayList<Integer> peakLines = new ArrayList<Integer>();
	// private int[] peakLines;
	private double[] returnPeaks;

	Plot1DGraphTable colourTable;
	Plot1DAppearance plotAppearace;
	int width, step, length;

	private double dataMaxval;

	private Text maxD;
	private Text minD;
	private Text sigma;
	private Text averageDSpacing;

	@SuppressWarnings("unused")
	private Class<? extends IPeak> peak;
	@SuppressWarnings("unused")
	private int maxNumPeaks;

	private boolean autoStopping;

	@SuppressWarnings("unused")
	private int stoppingThreashold;

	private org.eclipse.swt.widgets.List rawTable;

	private DecimalFormat decimalFormat;

	public DiffractionViewerSpotFit(Composite parent, int style, DiffractionViewer diffViews) {
		super(parent, style);
		this.diffView = diffViews;
		setLayout(new FillLayout(SWT.VERTICAL));

		// GUI creation and layout
		lpPlotter = new DataSetPlotter(PlottingMode.ONED, this, false);
		// Composite plotComp = lpPlotter.getComposite();
		lpPlotter.setAxisModes(AxisMode.CUSTOM, AxisMode.LINEAR, AxisMode.LINEAR);
		lpPlotter.setXAxisLabel("Distance along line");
		lpPlotter.registerOverlay(this);
		// various this needed for plotting

		axis = new AxisValues();

		colourTable = lpPlotter.getColourTable();

		// Results of d space calculation
		TabFolder tabFolder = new TabFolder(this, SWT.NONE);
		{
			TabItem summarydSpacing = new TabItem(tabFolder, SWT.NONE);
			summarydSpacing.setText("Summary");
			{
				Composite peakFittingResults = new Composite(tabFolder, SWT.NONE);
				GridLayout gridLayout = new GridLayout(6, false);
				peakFittingResults.setLayout(gridLayout);
				summarydSpacing.setControl(peakFittingResults);
				{
					Label lblAverageDSpacing = new Label(peakFittingResults, SWT.NONE);
					lblAverageDSpacing.setText("Average d spacing");
				}
				{
					averageDSpacing = new Text(peakFittingResults, SWT.READ_ONLY);
					averageDSpacing.setBackground(peakFittingResults.getBackground());
				}
				new Label(peakFittingResults, SWT.NONE).setText("\u00C5");
				{
					Label lblSigma = new Label(peakFittingResults, SWT.NONE);
					lblSigma.setText("Standatd deviation");
				}
				{
					sigma = new Text(peakFittingResults, SWT.READ_ONLY);
					sigma.setBackground(peakFittingResults.getBackground());
				}
				new Label(peakFittingResults, SWT.NONE).setText("\u00C5");
				{
					Label lblmaxD = new Label(peakFittingResults, SWT.NONE);
					lblmaxD.setText("Maximum d spacing");
				}
				{
					maxD = new Text(peakFittingResults, SWT.READ_ONLY);
					maxD.setBackground(peakFittingResults.getBackground());
				}
				new Label(peakFittingResults, SWT.NONE).setText("\u00C5");
				{
					Label lblMaxD = new Label(peakFittingResults, SWT.NONE);
					lblMaxD.setText("Minimum d spacing");
				}
				{
					minD = new Text(peakFittingResults, SWT.READ_ONLY);
					minD.setBackground(peakFittingResults.getBackground());
				}
				new Label(peakFittingResults, SWT.NONE).setText("\u00C5");
				
			}
			TabItem rawDSpacing = new TabItem(tabFolder, SWT.NONE);
			rawDSpacing.setText("Raw Data");
			{
				Composite rawComposite = new Composite(tabFolder, SWT.NONE);
				rawDSpacing.setControl(rawComposite);
				rawComposite.setLayout(new FillLayout(SWT.HORIZONTAL));
				rawTable = new org.eclipse.swt.widgets.List(rawComposite, SWT.V_SCROLL);
			}
		}
		decimalFormat = new DecimalFormat();
		decimalFormat.setMaximumFractionDigits(DECIMAL_PLACES);
	}

	@Override
	public void registerProvider(OverlayProvider overlay) {
		provider = (Overlay1DProvider) overlay;
	}

	@Override
	public void unregisterProvider() {
		provider = null;

	}

	@Override
	public void dispose() {
		if (lpPlotter != null)
			lpPlotter.cleanUp();
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {
		// clicking on plot not implemented

	}

	public void updatePlot() {
		lpPlotter.updateAllAppearance();
		lpPlotter.refresh(false);
	}

	@Override
	public void removePrimitives() {

		if (provider == null)
			return;
		if (peakLines == null || peakLines.size() < 1)
			return;
		for (int i = peakLines.size() - 1; i >= 0; i--) {
			provider.setPrimitiveVisible(peakLines.get(i), false);
			peakLines.remove(i);
		}

	}

	/**
	 * Takes an the data from the plot view and the ROI and controls the plotting, peak fitting and d space calculations
	 * 
	 * @param roi
	 * @param data
	 */
	public void processROI(Dataset data, LinearROI roi) {
		if (roi.getLength() <= 1 || data.getSize() < 1)
			return;
		Dataset[] dataSets = ROIProfile.line(data, roi, DiffractionViewer.lineStep);
		// DataSet[] dataSets = ROIProfile.line(Dataset.toDataSet(data), roi, DiffractionViewer.lineStep);
		dataMaxval = dataSets[0].max().doubleValue();
		plotDataSets(dataSets);

		List<IdentifiedPeak> fitterCurves = fitPeaks(dataSets[0]);
		Collections.sort(fitterCurves, new Compare());
		drawPeakLines(peakPosition(fitterCurves), dataMaxval);
		//plotFittedCurves(fitterCurves, dataSets[0]);
		dSpacingBetweenPeaks(fitterCurves, roi);
	}

	private static class Compare implements Comparator<IdentifiedPeak> {
		@Override
		public int compare(IdentifiedPeak arg0, IdentifiedPeak arg1) {
			if (arg0.getPos() > arg1.getPos())
				return 1;
			if (arg0.getPos() < arg1.getPos())
				return -1;
			return 0;
		}

	}

	/**
	 * @param peakPositionsForPlotting
	 *            x positions of the peaks
	 * @param datamax
	 *            max value of the lroi
	 */
	public void drawPeakLines(double[] peakPositionsForPlotting, double datamax) {
		if (provider == null || peakPositionsForPlotting.length == 0) {
			return;
		}
		clearOverlays();
		provider.begin(OverlayType.VECTOR2D);
		returnPeaks = new double[4];
		for (int i = 0; i < peakPositionsForPlotting.length; i++) {
			returnPeaks[0] = peakPositionsForPlotting[i] * DiffractionViewer.lineStep;
			returnPeaks[1] = 0;
			returnPeaks[2] = peakPositionsForPlotting[i] * DiffractionViewer.lineStep;
			returnPeaks[3] = datamax;
			int primID = provider.registerPrimitive(PrimitiveType.LINE);
			provider.setColour(primID, java.awt.Color.BLUE);
			provider.drawLine(primID, returnPeaks[0], returnPeaks[1], returnPeaks[2], returnPeaks[3]);
			peakLines.add(primID);
		}
		provider.end(OverlayType.VECTOR2D);
	}

	private void clearOverlays() {
		provider.begin(OverlayType.VECTOR2D);
		for (Integer i : peakLines) {
			provider.unregisterPrimitive(i);
		}
		provider.end(OverlayType.VECTOR2D);
	}

	private void plotDataSets(IDataset[] dataSets) {
		DoubleDataset axis = DatasetFactory.createRange(DoubleDataset.class, dataSets[0].getSize());
		axis.imultiply(DiffractionViewer.lineStep);
		this.axis.setValues(axis);

		colourTable.clearLegend();
		plotAppearace = new Plot1DAppearance(PlotColorUtility.getDefaultColour(0), Plot1DStyles.SOLID, "Line 1");

		colourTable.addEntryOnLegend(plotAppearace);

		List<IDataset> plots = new ArrayList<IDataset>();
		List<AxisValues> plotAxis = new ArrayList<AxisValues>();

		plots.add(dataSets[0]);
		plotAxis.add(this.axis);

		Color colour = PlotColorUtility.getDefaultColour(1);
		plotAppearace = new Plot1DAppearance(colour, Plot1DStyles.SOLID, "Line 2");
		colourTable.addEntryOnLegend(plotAppearace);

		try {
			lpPlotter.replaceAllPlots(plots, plotAxis);
			updatePlot();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<IdentifiedPeak> fitPeaks(Dataset currentDataSet) {
		if (currentDataSet == null || currentDataSet.getSize() < 1)
			return null;
		length = currentDataSet.getSize();
		return Generic1DFitter.findPeaks(DatasetFactory.createRange(length, Dataset.INT), currentDataSet, (int) (length *0.1));

	}

	private double[] peakPosition(List<IdentifiedPeak> fitterCurves) {
		double[] peaksXValues = new double[fitterCurves.size()];
		for (int i = 0; i < peaksXValues.length; i++) {
			peaksXValues[i] = fitterCurves.get(i).getPos();
		}
		Arrays.sort(peaksXValues);
		return peaksXValues;
	}

	
	@SuppressWarnings("unused")
	/**
	 * Since no longer peak fitting then this method is non longer required
	 */
	private void plotFittedCurves(List<? extends IPeak> fitterCurves, Dataset dataSets) {
		ArrayList<Dataset> plottingData = new ArrayList<Dataset>();
		CompositeFunction compFunc = new CompositeFunction();
		if (!fitterCurves.isEmpty()) {
			for (IPeak fp : fitterCurves) {
				compFunc.addFunction(fp);
			}
			plottingData.add(dataSets);
			plottingData.add(compFunc.calculateValues(DatasetFactory.createRange(DoubleDataset.class, dataSets.getSize())));

			try {
				lpPlotter.replaceAllPlots(plottingData);
			} catch (PlotException e) {
				e.printStackTrace();
			}
			updatePlot();
		}
	}

	private void dSpacingBetweenPeaks(List<IdentifiedPeak> list, LinearROI roi) {
		if (list == null || list.size() < 1) {
			logger.warn("No peaks found");
			return;
		}

		int[] peakPixVal = new int[list.size() * 2];
		double[] dSpacing = new double[list.size() - 1];

		for (int i = 0; i < list.size(); i++) {
			int[] tempPeakPxLoc = roi.getIntPoint(list.get(i).getPos()
					* DiffractionViewer.lineStep / roi.getLength());
			peakPixVal[i * 2] = tempPeakPxLoc[0];
			peakPixVal[i * 2 + 1] = tempPeakPxLoc[1];
		}
		try {
			dSpacing = DSpacing.dSpacingsFromPixelCoords(diffView.detConfig, diffView.diffEnv, peakPixVal);
		} catch (IllegalArgumentException e) {
			logger.warn("pixel values were found to be identical");
		} catch (Exception e) {
			logger.error("Could not calculate d spacing between these peaks");
		}

		// calculate some stats that might or might not be useful
		double mean = 0;
		double StandardDev = 0;
		double minimumD = Double.MAX_VALUE;
		double maximumD = -Double.MAX_VALUE;

		for (double d : dSpacing) {
			mean += d;
			if (d > maximumD)
				maximumD = d;
			if (d < minimumD)
				minimumD = d;

		}
		mean = mean / dSpacing.length;
		for (double d : dSpacing) {
			StandardDev += (d - mean) * (d - mean);
		}

		final double tmpSigma = Math.sqrt((StandardDev / (dSpacing.length - 1)));
		final double tempmean = mean;
		final double tempMin = minimumD;
		final double tempMax = maximumD;
		addDSpacingToList(dSpacing);
		
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				averageDSpacing.setText(decimalFormat.format(tempmean));
				sigma.setText(decimalFormat.format(tmpSigma));
				minD.setText(decimalFormat.format(tempMin));
				maxD.setText(decimalFormat.format(tempMax));
			}
		});
	}

	private void addDSpacingToList(final double[] dSpacing) {
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				rawTable.removeAll();
				int i = 1;
				for (double d : dSpacing) {
					rawTable.add("Peak " + (i++) + " and peak " + i + " has distance of " + d + " \u00C5");
				}

			}
		});

	}

	public void pushPreferences(String peakName, int numPeaks, boolean stopping, int threashold) {

		autoStopping = stopping;
		if (autoStopping)
			maxNumPeaks = -1;
		else
			maxNumPeaks = numPeaks;
		stoppingThreashold = threashold;

		if (peakName.compareToIgnoreCase("Gaussian") == 0) {
			peak = Gaussian.class;
		} else if (peakName.compareToIgnoreCase("Lorentzian") == 0) {
			peak = Lorentzian.class;
		} else if (peakName.compareToIgnoreCase("Pearson VII") == 0) {
			peak = PearsonVII.class;
		} else if (peakName.compareToIgnoreCase("PseudoVoigt") == 0) {
			peak = PseudoVoigt.class;
		} else {
			peak = Gaussian.class;
		}
	}
}
