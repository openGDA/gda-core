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

package org.opengda.detector.electronanalyser.client.plot;

import gda.epics.connection.EpicsController;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBR_Double;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ILineTrace.PointStyle;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.opengda.detector.electronanalyser.server.VGScientaAnalyser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.DoublePointList.DoublePoint;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;

/**
 *
 */
public class SesPlotComposite extends Composite {

	public enum MODE {
		SPECTRUM, SLICE, IMAGE, EXTIO, NONE;
	}

	private FontRegistry fontRegistry;
	private static final Logger logger = LoggerFactory.getLogger(SesPlotComposite.class);

	private MODE mode = MODE.NONE;

	private static final String Y_lbl = "Y:";
	private static final String X_lbl = "X:";
	private Text txtYValue;
	private Text txtXValue;

	private VGScientaAnalyser analyser;
	private EpicsController epicsController;

	private Label lblProfileIntensityValue;

	private static final String INTENSITIES_lbl = "Intensities";

	private static final String HISTOGRAM_DATASET_lbl = "Histogram Dataset";

	private static final String HISTOGRAM_PLOT_TITLE_lbl = "Histogram Plot";

	private static final String HISTOGRAM_TRACE = "Histogram";

	private static final String SPECTRUM_PLOT = "Spectrum plot";

	private static final String BOLD_TEXT_11 = "bold-text_11";

	private static final String BOLD_TEXT_9 = "bold-text_9";

	private IRegion xHair;

	private static final String REGION_FIT2 = "RegionFit2";

	private AbstractPlottingSystem plottingSystem;

	private AbstractDataset rawDataSlice;

	private long timeSinceLastUpdate = 0;

	private AbstractDataset rawImgDs;

	private AbstractDataset darkImgDs;

	private uk.ac.diamond.scisoft.analysis.dataset.function.Histogram histogram;

	private boolean shouldUpdatePlot = true;

	private ROIBase xBounds;

	private ILineTrace histogramTrace;

	private ILineTrace profileLineTrace;

	private boolean streamLog = false;

	private boolean singleLog = false;

	private Composite statsComposite;

	private Spinner spinnerValue;
	private ArrayList<PlottingSystemActionListener> lineListeners;

	public interface PlottingSystemActionListener {

		/**
		 * Informs the listener when the profile line is moved to a certain
		 * location
		 * 
		 * @param xVal
		 *            - value to which the profile mouse has been moved to
		 * @param intensity
		 *            - the intensity at the current x
		 */
		void profileLineMovedTo(double xVal, long intensity);

		/**
		 * Informs the listener when the histogram roi is changed.
		 * 
		 * @param minValue
		 * @param maxValue
		 * @param factor
		 */
		void histogramChangedRoi(double minValue, double maxValue, double factor);

	}

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public SesPlotComposite(IWorkbenchPart part, Composite parent, int style)
			throws Exception {
		super(parent, style);
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
		}
		this.setBackground(ColorConstants.white);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		this.setLayout(layout);

		plotinfoComposite = new Composite(this, SWT.None);
		plotinfoComposite.setBackground(ColorConstants.white);
		plotinfoComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout3 = new GridLayout(2, true);
		layout3.marginHeight = 2;
		layout3.marginWidth = 2;
		layout3.horizontalSpacing = 2;
		layout3.verticalSpacing = 2;
		plotinfoComposite.setLayout(layout3);

		Composite xyComposite = new Composite(plotinfoComposite, SWT.BORDER
				| SWT.LEFT);
		GridData gd_xyvaluesComposite = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_xyvaluesComposite.heightHint = 30;
		xyComposite.setLayoutData(gd_xyvaluesComposite);
		xyComposite.setLayout(new GridLayout(4, false));
		xyComposite.setBackground(ColorConstants.yellow);

		Label lblX = new Label(xyComposite, SWT.RIGHT);
		lblX.setText(X_lbl);
		lblX.setBackground(ColorConstants.yellow);

		txtXValue = new Text(xyComposite, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		txtXValue.setText("00.000");
		txtXValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData gd_txtXValue = new GridData(GridData.FILL_HORIZONTAL);
		gd_txtXValue.widthHint = 50;
		txtXValue.setLayoutData(gd_txtXValue);
		txtXValue.setBackground(ColorConstants.white);

		Label lblY = new Label(xyComposite, SWT.RIGHT);
		lblY.setText(Y_lbl);
		lblY.setBackground(ColorConstants.yellow);

		txtYValue = new Text(xyComposite, SWT.LEFT | SWT.BORDER | SWT.READ_ONLY);
		txtYValue.setText("00000");
		txtYValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData gd_txtYValue = new GridData(GridData.FILL_HORIZONTAL);
		gd_txtYValue.widthHint = 50;
		txtYValue.setLayoutData(gd_txtYValue);
		txtYValue.setBackground(ColorConstants.white);

		Composite sliceComposite = new Composite(plotinfoComposite, SWT.BORDER
				| SWT.RIGHT);
		GridData gd_sliceComposite = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_sliceComposite.heightHint = 30;
		sliceComposite.setLayoutData(gd_sliceComposite);
		sliceComposite.setLayout(new GridLayout(2, false));
		sliceComposite.setBackground(ColorConstants.yellow);

		Label lblSlice = new Label(sliceComposite, SWT.RIGHT);
		lblSlice.setText("Slice:");
		lblSlice.setBackground(ColorConstants.yellow);

		spinnerValue = new Spinner(sliceComposite, SWT.LEFT | SWT.BORDER);
		GridData gd_spinnerValue = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_spinnerValue.widthHint = 30;
		spinnerValue.setLayoutData(gd_spinnerValue);
		spinnerValue.setSelection(1);
		spinnerValue.setFont(fontRegistry.get(BOLD_TEXT_11));
		spinnerValue.setBackground(ColorConstants.white);

		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());
		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "Spectrum", part instanceof IViewPart ? ((IViewPart) part).getViewSite().getActionBars()
				: null, PlotType.XY_STACKED, part);
		lineListeners = new ArrayList<PlottingSystemActionListener>();

		statsComposite = new Composite(this, SWT.None);
		statsComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		statsComposite.setBackground(ColorConstants.yellow);
		layout3 = new GridLayout(8, true);
		layout3.marginHeight = 0;
		layout3.marginWidth = 0;
		layout3.horizontalSpacing = 0;
		layout3.verticalSpacing = 0;
		statsComposite.setLayout(layout3);

		Label lblPosition = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblPosition.setBackground(ColorConstants.yellow);
		lblPosition.setText("Postion:");

		Text txtPosition = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtPosition.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtPosition.setBackground(ColorConstants.yellow);
		txtPosition.setText("0.00000");

		Label lblHeight = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblHeight.setBackground(ColorConstants.yellow);
		lblHeight.setText("Height:");

		Text txtHeight = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtHeight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtHeight.setBackground(ColorConstants.yellow);
		txtHeight.setText("0000");

		Label lblFWHM = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblFWHM.setBackground(ColorConstants.yellow);
		lblFWHM.setText("FWHM:");

		Text txtFWHM = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtFWHM.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtFWHM.setBackground(ColorConstants.yellow);
		txtFWHM.setText("N/A");

		Label lblArea = new Label(statsComposite, SWT.None | SWT.RIGHT);
		lblArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lblArea.setBackground(ColorConstants.yellow);
		lblArea.setText("Area:");

		Text txtArea = new Text(statsComposite, SWT.None | SWT.LEFT
				| SWT.READ_ONLY);
		txtArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtArea.setBackground(ColorConstants.yellow);
		txtArea.setText("0000");
	}

	public void loadErrorInDisplay(final String dialogTitle,
			final String errorMsg) {
		if (!this.getDisplay().isDisposed()) {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(SesPlotComposite.this.getShell(), dialogTitle, "Problem with data display \n"
							+ errorMsg);
				}
			});
		}
	}

	public boolean addOverlayLineListener(
			PlottingSystemActionListener overlayLineListener) {
		return lineListeners.add(overlayLineListener);
	}

	public boolean removeOverlayLineListener(
			PlottingSystemActionListener overlayLineListener) {
		return lineListeners.remove(overlayLineListener);
	}

	public void updateProfilePlots(final IProgressMonitor monitor, final int y) {
	}

	public void updatePlotPoints(final IProgressMonitor progress,
			final TiltPlotPointsHolder tiltPoints) {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.clear();
				final DoublePointList centers1 = tiltPoints.getCenters1();
				final DoublePointList centers2 = tiltPoints.getCenters2();

				final DoublePointList line2 = tiltPoints.getLine2();
				DoubleDataset y3 = new DoubleDataset(line2.getYDoubleArray());
				y3.setName("Line1");
				DoubleDataset x3 = new DoubleDataset(line2.getXDoubleArray());
				ArrayList<AbstractDataset> singletonList = new ArrayList<AbstractDataset>(1);
				singletonList.add(y3);
				plottingSystem.updatePlot1D(x3, singletonList, progress);

				plottingSystem.getSelectedYAxis().setFormatPattern("######.#");

				try {

					ITrace centers1Trace = plottingSystem.getTrace("Centers1");
					if (centers1Trace != null) {
						plottingSystem.removeTrace(centers1Trace);
					}
					DoubleDataset centers1Yds = new DoubleDataset(centers1.getYDoubleArray());
					centers1Yds.setName("Centers1");
					DoubleDataset centers1Xds = new DoubleDataset(centers1.getXDoubleArray());
					ArrayList<AbstractDataset> dsList = new ArrayList<AbstractDataset>();
					dsList.add(centers1Yds);
					List<ITrace> traces1 = plottingSystem.updatePlot1D(centers1Xds, dsList, progress);
					((ILineTrace) traces1.get(0)).setUserTrace(true);
					((ILineTrace) traces1.get(0)).setPointStyle(PointStyle.CROSS);

					ITrace centers2Trace = plottingSystem.getTrace("Centers2");
					if (centers2Trace != null) {
						plottingSystem.removeTrace(centers2Trace);
					}
					centers2Trace = plottingSystem.createLineTrace("Centers2");
					centers2Trace.setUserTrace(true);
					centers2Trace.setVisible(true);
					DoubleDataset centers2Yds = new DoubleDataset(centers2.getYDoubleArray());
					centers2Yds.setName("Centers2");
					DoubleDataset centers2Xds = new DoubleDataset(centers2.getXDoubleArray());
					dsList = new ArrayList<AbstractDataset>();
					dsList.add(centers2Yds);
					plottingSystem.updatePlot1D(centers2Xds, dsList, progress);

					IRegion region1 = plottingSystem.getRegion("RegionFit1");
					if (region1 != null) {
						plottingSystem.removeRegion(region1);
					}

					region1 = plottingSystem.createRegion("RegionFit1", RegionType.ELLIPSEFIT);
					region1.setLineWidth(1);

					PolygonalROI roi1 = new PolygonalROI();
					for (DoublePoint dp : tiltPoints.getEllipse1().getDoublePointList()) {
						roi1.insertPoint(dp.getX(), dp.getY());
					}

					plottingSystem.addRegion(region1);
					region1.setROI(roi1);
					region1.setMobile(false);

					//
					IRegion region2 = plottingSystem.getRegion(REGION_FIT2);
					if (region2 != null) {
						plottingSystem.removeRegion(region2);
					}

					region2 = plottingSystem.createRegion(REGION_FIT2, RegionType.ELLIPSEFIT);
					region2.setLineWidth(1);
					region2.setRegionColor(ColorConstants.blue);
					PolygonalROI roi2 = new PolygonalROI();
					for (DoublePoint dp : tiltPoints.getEllipse2().getDoublePointList()) {
						roi2.insertPoint(dp.getX(), dp.getY());
					}

					plottingSystem.addRegion(region2);
					region2.setROI(roi2);
					region2.setMobile(false);
					if (tiltPoints.getTiltPointsTitle() != null) {
						plottingSystem.setTitle(tiltPoints.getTiltPointsTitle());
					}
				} catch (Exception e) {
					logger.error("Problem plotting tilt points.", e);
				}

			}
		});
	}

	private synchronized void setShouldUpdatePlot(boolean updatePlot) {
		this.shouldUpdatePlot = updatePlot;
	}

	/**
	 * Updates the histogram data for stream
	 * 
	 * @param histogramFromStats
	 */
	public void updateHistogramData(final double[] histogramFromStats) {
		if (shouldUpdatePlot && !this.isDisposed()) {
			this.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					double[] subarray = ArrayUtils.subarray(histogramFromStats, 0, 1024);

					if (streamLog) {
						int count = 0;
						for (double d : histogramFromStats) {
							if (d != 0) {
								subarray[count] = Math.log10(d);
							}
							count++;
						}
					}

					DoubleDataset ds = new DoubleDataset(subarray, 1024);

					int min = 0;
					ds.setName(HISTOGRAM_DATASET_lbl);

					IntegerDataset xaxisRange = IntegerDataset.arange(min, 65536, 64);
					xaxisRange.setName(INTENSITIES_lbl);

					if (histogramTrace == null) {
						histogramTrace = plottingSystem.createLineTrace(HISTOGRAM_TRACE);
					}

					histogramTrace.setData(xaxisRange, ds);
					histogramTrace.setTraceColor(ColorConstants.blue);
					if (plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
						plottingSystem.addTrace(histogramTrace);
					}
					plottingSystem.repaint();

					plottingSystem.setTitle(HISTOGRAM_PLOT_TITLE_lbl);
					plottingSystem.getSelectedYAxis().setFormatPattern("#####");
					plottingSystem.getSelectedXAxis().setFormatPattern("#####");
				}
			});
		}
	}

	public void applyLog(boolean log, ImageData imgData) {
		singleLog = log;
		updateHistogramData(imgData);
	}

	public void applyLog(boolean log) {
		streamLog = log;
	}

	/**
	 * Update histogram data for single.
	 * 
	 * @param imageData
	 */
	public void updateHistogramData(final ImageData imageData) {
		if (shouldUpdatePlot) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {

					// removeOtherRegions(xHair.getName());
					if (histogram == null) {
						histogram = new uk.ac.diamond.scisoft.analysis.dataset.function.Histogram(256);
					}

					ImageData imgD = (ImageData) imageData.clone();
					int intensities[] = new int[imgD.width * imgD.height];
					int i = 0;
					if (imageData.depth > 16) {
						for (int h = 0; h < imgD.height; h++) {
							for (int w = 0; w < imgD.width; w++) {
								int pixel = imgD.getPixel(w, h);

								// to equate it to a 10 bit number of 1024 shift
								// it right by 14
								intensities[i++] = pixel >> 8;
							}
						}
					} else {
						for (int h = 0; h < imgD.height; h++) {
							for (int w = 0; w < imgD.width; w++) {
								int pixel = imgD.getPixel(w, h);

								// to equate it to a 10 bit number of 1024 shift
								// it right by 14
								intensities[i++] = pixel;
							}
						}

					}

					IDataset ds = new IntegerDataset(intensities, imgD.width, imgD.height);

					int max = ds.max().intValue();
					int min = ds.min().intValue();
					histogram.setMinMax(min, max);
					ds.setName(HISTOGRAM_DATASET_lbl);

					AbstractDataset histogramDs = histogram.value(ds).get(0);
					histogramDs.setName(HISTOGRAM_DATASET_lbl);
					if (singleLog) {
						int shape = histogramDs.getShape()[0];
						double[] logData = new double[shape];
						int count = 0;
						if (histogramDs instanceof IntegerDataset) {
							IntegerDataset iDs = (IntegerDataset) histogramDs;
							for (int intensity : iDs.getData()) {
								if (intensity != 0) {
									logData[count] = Math.log10(intensity);
								}
								count++;
							}
							histogramDs = new DoubleDataset(logData, shape);
						}

					}

					IntegerDataset xaxisRange = IntegerDataset.arange(min, max, 255);
					xaxisRange.setName(INTENSITIES_lbl);

					if (histogramTrace == null) {
						histogramTrace = plottingSystem.createLineTrace(HISTOGRAM_TRACE);
					}

					histogramTrace.setData(xaxisRange, histogramDs);
					histogramTrace.setTraceColor(ColorConstants.darkBlue);
					if (plottingSystem.getTrace(HISTOGRAM_TRACE) == null) {
						plottingSystem.addTrace(histogramTrace);
					}
					plottingSystem.repaint();

					// plottingSystem.updatePlot1D(xaxisRange, dsHisto, null);
					plottingSystem.setTitle(HISTOGRAM_PLOT_TITLE_lbl);

					plottingSystem.getSelectedYAxis().setFormatPattern("#####");
					plottingSystem.getSelectedXAxis().setFormatPattern("#####");
				}
			});
		}
	}

	/**
	 * Removes other regions than the one provided.
	 * 
	 * @param regionName
	 */
	private void removeOtherRegions(String... regionName) {
		Collection<IRegion> regions = plottingSystem.getRegions();
		for (IRegion iRegion : regions) {
			if (regionName.length > 0) {
				for (String r : regionName) {
					if (!r.equals(iRegion.getName())) {
						plottingSystem.removeRegion(iRegion);
					}
				}
			} else {
				plottingSystem.removeRegion(iRegion);
			}
		}
	}

	@Override
	public void dispose() {
		if (!plottingSystem.isDisposed()) {
			plottingSystem.clear();
		}
		super.dispose();
	}

	private void createMouseFollowLineRegion() {

		if (plottingSystem == null)
			return;
		try {
			if (xHair == null
					|| plottingSystem.getRegion(xHair.getName()) == null) {
				this.xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("DragLine", plottingSystem), IRegion.RegionType.XAXIS_LINE);

				xHair.addROIListener(mouseFollowRoiListener);

				addMouseFollowLineRegion(xHair);
				xHair.addMouseListener(mouseFollowRegionMouseListner);
			}

		} catch (Exception ne) {
			logger.error("Cannot create cross-hairs!", ne);
		}
	}

	private IROIListener mouseFollowRoiListener = new IROIListener() {

		private void update(ROIEvent evt) {
			final IRegion region = (IRegion) evt.getSource();
			ROIBase roi = region.getROI();
			xBounds = roi;
		}

		@Override
		public void roiDragged(ROIEvent evt) {
			update(evt);
			for (PlottingSystemActionListener lis : lineListeners) {
				int[] intPoint = evt.getROI().getIntPoint();
				lis.profileLineMovedTo(intPoint[0], profileLineTrace.getYData().getInt(intPoint[0]));
			}
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			update(evt);
		}

		@Override
		public void roiSelected(ROIEvent evt) {

		}
	};

	private MouseListener mouseFollowRegionMouseListner = new MouseListener.Stub() {
		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {
			try {

				final Color snapShotColor = ColorConstants.blue;
				createStaticRegion("DragLine", xBounds, snapShotColor, xHair.getRegionType());

				setShouldUpdatePlot(false);

				removeXHairListeners();
				plottingSystem.removeRegion(xHair);
				xHair = null;

			} catch (Exception ne) {
				logger.error(ne.getMessage(), ne);
			}
		}
	};

	private void removeXHairListeners() {
		if (xHair != null) {
			xHair.removeMouseListener(mouseFollowRegionMouseListner);
		}
	}

	// private Double histogramFactor = DEFAULT_HISTOGRAM_FACTOR;

	private Composite plotinfoComposite;

	// private PageBook pgBook_plotinfo;

	private Button btnApplyExposureSettings;

	private IRegion createStaticRegion(String nameStub, final ROIBase bounds,
			final Color snapShotColor, final RegionType regionType)
			throws Exception {

		final IRegion region = plottingSystem.createRegion(RegionUtils.getUniqueName(nameStub, plottingSystem), regionType);
		region.setRegionColor(snapShotColor);
		plottingSystem.addRegion(region);
		region.setROI(bounds);
		region.addROIListener(new IROIListener() {

			Double histogramFrom = null;

			@Override
			public void roiDragged(ROIEvent evt) {
				if (histogramFrom == null) {
					histogramFrom = evt.getROI().getPointX();
					logger.debug("distance started:{}", histogramFrom);
				}
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				// need to introspect the movement and propagate the change to
				// hardware.
				double minValue = histogramTrace.getXData().min().doubleValue();
				double maxValue = histogramTrace.getXData().max().doubleValue();
				double histogramTo = evt.getROI().getPointX();
				logger.debug("distance ended:{}", histogramTo);

				plottingSystem.removeRegion(region);
				region.removeROIListener(this);
				setShouldUpdatePlot(true);
			}

			@Override
			public void roiSelected(ROIEvent evt) {

			}
		});

		return region;
	}

	private void addMouseFollowLineRegion(IRegion region) {
		region.setVisible(true);
		region.setTrackMouse(true);
		region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change
										// it!
		plottingSystem.addRegion(region);
	}

	public void clearPlots() {
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				mode = MODE.NONE;
				plottingSystem.setTitle("");
				// pgBook_plotinfo.showPage(pg_plotinfo_none);
				plottingSystem.reset();
			}
		});

	}

	public void setYLabelValue(final String yLblValue) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					txtYValue.setText(yLblValue);
				}
			});
		}
	}

	public void setXLabelValue(final String formattedXVal) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					txtXValue.setText(formattedXVal);
				}
			});
		}
	}

	public void setProfileIntensityValue(final String profileIntensityValue) {
		if (!getDisplay().isDisposed()) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					lblProfileIntensityValue.setText(profileIntensityValue);
				}
			});
		}
	}

	private class SpectrumDataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving spectrum data from "
					+ ((Channel) (arg0.getSource())).getName() + " to plot on "
					+ plottingSystem.getPlotName() + " with axes from "
					+ analyser.getName());
			mode = MODE.SPECTRUM;
			DBR dbr = arg0.getDBR();
			double[] value = null;
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue();
			}
			IProgressMonitor monitor = new NullProgressMonitor();
			updateSpectrumPlot(monitor, value);
		}
	}

	private void updateSpectrumPlot(final IProgressMonitor monitor,
			double[] value) {
		if (getDisplay() != null && !getDisplay().isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (xHair != null) {
						removeOtherRegions(xHair.getName());
					} else {
						removeOtherRegions();
					}
				}
			});
		}
		final ArrayList<AbstractDataset> plotDataSets = new ArrayList<AbstractDataset>();
		plotDataSets.add(new DoubleDataset(value, new int[] { value.length }));
		try {
			double[] xdata = analyser.getEnergyAxis(); // TODO once per analyser region
			final DoubleDataset axis = new DoubleDataset(xdata, new int[] { xdata.length });
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						final List<ITrace> profileLineTraces = plottingSystem.updatePlot1D(axis, plotDataSets, monitor);

						if (!profileLineTraces.isEmpty()) {
							profileLineTrace = (ILineTrace) profileLineTraces.get(0);
							profileLineTrace.setTraceColor(ColorConstants.blue);
						}

						plottingSystem.setTitle(SPECTRUM_PLOT);
						plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
						plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
						// plottingSystem.getSelectedYAxis().setRange(0, 5);
						// plottingSystem.getSelectedXAxis().setRange(0, 4008);
						createMouseFollowLineRegion();

					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live spectrum plot", e);
		}
	}
	private class ExtIODataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving external IO data from "
					+ ((Channel) (arg0.getSource())).getName() + " to plot on "
					+ plottingSystem.getPlotName() + " with axes from "
					+ analyser.getName());
			mode = MODE.EXTIO;
			DBR dbr = arg0.getDBR();
			double[] value = null;
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue();
			}
			IProgressMonitor monitor = new NullProgressMonitor();
			updateExtIOPlot(monitor, value);
		}
	}
	private void updateExtIOPlot(final IProgressMonitor monitor,
			double[] value) {
		if (getDisplay() != null && !getDisplay().isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (xHair != null) {
						removeOtherRegions(xHair.getName());
					} else {
						removeOtherRegions();
					}
				}
			});
		}
		final ArrayList<AbstractDataset> plotDataSets = new ArrayList<AbstractDataset>();
		plotDataSets.add(new DoubleDataset(value, new int[] { value.length }));
		try {
			double[] xdata = analyser.getEnergyAxis(); // TODO once per analyser region
			final DoubleDataset axis = new DoubleDataset(xdata, new int[] { xdata.length });
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {
					@Override
					public void run() {
						final List<ITrace> profileLineTraces = plottingSystem.updatePlot1D(axis, plotDataSets, monitor);

						if (!profileLineTraces.isEmpty()) {
							profileLineTrace = (ILineTrace) profileLineTraces.get(0);
							profileLineTrace.setTraceColor(ColorConstants.blue);
						}

						plottingSystem.setTitle(SPECTRUM_PLOT);
						plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
						plottingSystem.getSelectedXAxis().setFormatPattern("#####.#");
						// plottingSystem.getSelectedYAxis().setRange(0, 5);
						// plottingSystem.getSelectedXAxis().setRange(0, 4008);
						createMouseFollowLineRegion();

					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live spectrum plot", e);
		}
	}

	private class ImageDataListener implements MonitorListener {

		@Override
		public void monitorChanged(MonitorEvent arg0) {
			logger.debug("receiving image data from " + arg0.toString()
					+ " to plot on " + plottingSystem.getPlotName()
					+ " with axes from " + analyser.getName());
			mode = MODE.IMAGE;
			DBR dbr = arg0.getDBR();
			double[] value = null;
			if (dbr.isDOUBLE()) {
				value = ((DBR_Double) dbr).getDoubleValue();
			}
			IProgressMonitor monitor = new NullProgressMonitor();
			try {
				updateImagePlot(monitor, value);
			} catch (Exception e) {
				logger.error("exception caught preparing analyser live plot", e);
			}
		}
	}

	private void updateImagePlot(final IProgressMonitor monitor,
			final double[] value) {
		try {
			int[] dims = new int[] {
					analyser.getNdArray().getPluginBase().getArraySize1_RBV(),
					analyser.getNdArray().getPluginBase().getArraySize0_RBV() };
			int arraysize = dims[0] * dims[1];
			if (arraysize < 1)
				return;
			double[] values = Arrays.copyOf(value, arraysize);
			final AbstractDataset ds = new DoubleDataset(values, dims);

			double[] xdata = analyser.getEnergyAxis(); //TODO do this once per analyser region
			double[] ydata = analyser.getAngleAxis();
			DoubleDataset xAxis = new DoubleDataset(xdata, new int[] { xdata.length });
			DoubleDataset yAxis = new DoubleDataset(ydata, new int[] { ydata.length });
			xAxis.setName("energies (eV)");
			if ("Transmission".equalsIgnoreCase(analyser.getLensMode())) {
				yAxis.setName("location (mm)");
			} else {
				yAxis.setName("angles (deg)");
			}
			final ArrayList<AbstractDataset> axes = new ArrayList<AbstractDataset>();
			axes.add(xAxis);
			axes.add(yAxis);
			if (!getDisplay().isDisposed()) {
				getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						plottingSystem.updatePlot2D(ds, axes, monitor);
					}
				});
			}
		} catch (Exception e) {
			logger.error("exception caught preparing analyser live image plot", e);
		}
	}
}
