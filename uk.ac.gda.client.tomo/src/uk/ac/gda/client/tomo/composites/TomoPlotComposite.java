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

package uk.ac.gda.client.tomo.composites;

import gda.analysis.io.ScanFileHolderException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.IAxis;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.DoublePointList.DoublePoint;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;
import uk.ac.gda.client.tomo.ViewerDisplayMode;

/**
 *
 */
public class TomoPlotComposite extends Composite {

	public enum MODE {
		HISTOGRAM, PROFILE, TILT, NONE;
	}

	private IRegion xHair;

	private IAxis x1;

	private MODE mode = MODE.NONE;

	private static final String SET_EXPOSURE_TIME = "Apply Exposure Time";

	private static final String REGION_FIT2 = "RegionFit2";

	private static final String SCALING_REGION = "ScalingRegion";

	private static final Logger logger = LoggerFactory.getLogger(TomoPlotComposite.class);

	private AbstractPlottingSystem plottingSystem;

	private IntegerDataset rawDataSlice;

	private long timeSinceLastUpdate = 0;

	private AbstractDataset rawImgDs;

	private AbstractDataset darkImgDs;

	private uk.ac.diamond.scisoft.analysis.dataset.function.Histogram histogram;

	private boolean shouldUpdatePlot = true;

	private ROIBase xBounds;

	private ILineTrace histogramTrace;

	private ILineTrace profileLineTrace;

	private Composite lowerBarContainingApplyButton;

	public interface PlottingSystemActionListener {

		/**
		 * Informs the listener when the profile line is moved to a certain location
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
		 * @param from
		 * @param to
		 */
		void histogramChangedRoi(double minValue, double maxValue, double from, double to);

		/**
		 * Informs the listener when the apply histogram button is clicked.
		 */
		void applyExposureTimeButtonClicked();
	}

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public TomoPlotComposite(Composite parent, int style) throws Exception {
		super(parent, style);
		this.setBackground(ColorConstants.white);

		GridLayout layout = getGridLayoutZeroSettings();
		this.setLayout(layout);
		Composite plotComposite = new Composite(this, SWT.None);
		plotComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		plotComposite.setLayout(new FillLayout());

		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(plotComposite, "", null, PlotType.PT1D_MULTI, null);
		lineListeners = new ArrayList<TomoPlotComposite.PlottingSystemActionListener>();

		lowerBarContainingApplyButton = new Composite(this, SWT.None);
		lowerBarContainingApplyButton.setBackground(ColorConstants.white);
		lowerBarContainingApplyButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		lowerBarContainingApplyButton.setLayout(getGridLayoutZeroSettings());

		Button btnApplyExposureSettings = new Button(lowerBarContainingApplyButton, SWT.PUSH);
		btnApplyExposureSettings.setText(SET_EXPOSURE_TIME);
		btnApplyExposureSettings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (PlottingSystemActionListener lis : lineListeners) {
					lis.applyExposureTimeButtonClicked();
				}
			}
		});
		GridData layoutData = new GridData();
		layoutData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_END;
		btnApplyExposureSettings.setLayoutData(layoutData);
		//
		x1 = plottingSystem.getSelectedXAxis();

	}

	private GridLayout getGridLayoutZeroSettings() {
		GridLayout layout = new GridLayout();

		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;

		return layout;
	}

	private ArrayList<PlottingSystemActionListener> lineListeners;

	public boolean addOverlayLineListener(PlottingSystemActionListener overlayLineListener) {
		return lineListeners.add(overlayLineListener);
	}

	public boolean removeOverlayLineListener(PlottingSystemActionListener overlayLineListener) {
		return lineListeners.remove(overlayLineListener);
	}

	public void updateProfilePlots(IProgressMonitor monitor, final int xStart, final int xEnd, final int y) {
		// the below delay routine is added on Mark Basham's suggestion which is needed so that the plotter does'nt get
		// over-updated. Time check if below 0.1s ignore request

		if (mode != MODE.PROFILE) {
			plottingSystem.clear();
		}

		mode = MODE.PROFILE;
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

		if (rawImgDs != null) {
			long currentTimeMillis = System.currentTimeMillis();
			if (currentTimeMillis - timeSinceLastUpdate < 100) {
				// ignore the request
				return;
			}
			timeSinceLastUpdate = currentTimeMillis;
			final ArrayList<AbstractDataset> plotDataSets = new ArrayList<AbstractDataset>();

			// DoubleDataset axis = DoubleDataset.arange(xStart, xEnd, 1);
			DoubleDataset axis = DoubleDataset.arange(4008);

			if (rawImgDs != null) {
				rawDataSlice = (IntegerDataset) rawImgDs.getSlice(new int[] { y - 1, 0 }, new int[] { y, 4008 },
						new int[] { 1, 1 });
				rawDataSlice.squeeze();
				plotDataSets.add(rawDataSlice);
			}
			if (darkImgDs != null) {
				IntegerDataset darkDataSlice = (IntegerDataset) darkImgDs.getSlice(new int[] { y - 1, 0 }, new int[] {
						y, 4008 }, new int[] { 1, 1 });
				darkDataSlice.squeeze();
				plotDataSets.add(darkDataSlice);
			}

			List<ITrace> profileLineTraces = plottingSystem.updatePlot1D(axis, plotDataSets, monitor);
			if (!profileLineTraces.isEmpty()) {
				profileLineTrace = (ILineTrace) profileLineTraces.get(0);
			}
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					plottingSystem.setTitle("Intensity plot");
					plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
					plottingSystem.getSelectedYAxis().setRange(0, 65600);

					createMouseFollowLineRegion();

					lowerBarContainingApplyButton.setVisible(false);
				}
			});
		}

	}

	public void setImagesToPlot(String rawPlotImageFilename, String darkImgFileName) {
		if (rawPlotImageFilename != null) {
			rawImgDs = loadDatasetForTiffImg(rawPlotImageFilename, "INTENSITY");
		} else {
			rawImgDs = null;
		}
		if (darkImgFileName != null) {
			darkImgDs = loadDatasetForTiffImg(darkImgFileName, "DARK");
		} else {
			darkImgDs = null;
		}
	}

	private AbstractDataset loadDatasetForTiffImg(String fileName, String dsName) {
		TIFFImageLoader tiffImageLoader = new TIFFImageLoader(fileName);
		AbstractDataset dataset = null;
		try {
			DataHolder dataHolder = tiffImageLoader.loadFile();
			dataset = dataHolder.getDataset(0);
			dataset.setName(dsName);

		} catch (ScanFileHolderException e) {
			logger.error("Tiff loading problem", e);
		} catch (Exception e) {
			logger.error("profile loading exception", e);
		}
		return dataset;
	}

	public void updatePlotPoints(final IProgressMonitor progress, final TiltPlotPointsHolder tiltPoints) {
		mode = MODE.TILT;
		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				// Dont show the apply button
				lowerBarContainingApplyButton.setVisible(false);
				//
				plottingSystem.clear();
				final DoublePointList centers1 = tiltPoints.getCenters1();
				final DoublePointList centers2 = tiltPoints.getCenters2();

				final DoublePointList line2 = tiltPoints.getLine2();
				DoubleDataset y3 = new DoubleDataset(line2.getYDoubleArray());
				DoubleDataset x3 = new DoubleDataset(line2.getXDoubleArray());
				ArrayList<AbstractDataset> singletonList = new ArrayList<AbstractDataset>(1);
				singletonList.add(y3);
				plottingSystem.updatePlot1D(x3, singletonList, progress);

				plottingSystem.setTitle("Tilt alignment");
				plottingSystem.getSelectedYAxis().setFormatPattern("######.#");

				try {
					IRegion region1 = plottingSystem.getRegion("RegionFit1");
					if (region1 != null) {
						plottingSystem.removeRegion(region1);
					}

					region1 = plottingSystem.createRegion("RegionFit1", RegionType.ELLIPSEFIT);
					region1.setLineWidth(1);
					region1.setRegionColor(ColorConstants.black);

					PolygonalROI roi1 = new PolygonalROI();
					for (DoublePoint dp : centers1.getDoublePointList()) {
						roi1.insertPoint(dp.getX(), dp.getY());
					}
					EllipticalFitROI roi = new EllipticalFitROI(roi1);

					plottingSystem.addRegion(region1);
					region1.setROI(roi);
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
					for (DoublePoint dp : centers2.getDoublePointList()) {
						roi2.insertPoint(dp.getX(), dp.getY());
					}

					plottingSystem.addRegion(region2);
					region2.setROI(roi2);
					region2.setMobile(false);

				} catch (Exception e) {
					logger.error("Problem plotting tilt points.", e);
				}

			}
		});
	}

	private synchronized void setShouldUpdatePlot(boolean updatePlot) {
		this.shouldUpdatePlot = updatePlot;
	}

	public void updateHistogramData(final ViewerDisplayMode displayMode, final ImageData image) {
		if (shouldUpdatePlot) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (mode != MODE.HISTOGRAM) {
						histogramTrace = null;
						removeXHairListeners();
						removeOtherRegions();
						xHair = null;
					}
					// show the apply button if streaming is happening in the left window.
					if (displayMode == ViewerDisplayMode.FLAT_STREAM_LIVE
							|| displayMode == ViewerDisplayMode.SAMPLE_STREAM_LIVE) {
						lowerBarContainingApplyButton.setVisible(true);
					} else {
						lowerBarContainingApplyButton.setVisible(false);
					}

					mode = MODE.HISTOGRAM;
					createMouseFollowLineRegion();
					if (plottingSystem.getTrace("Histogram") == null) {
						plottingSystem.clear();
					}

					// removeOtherRegions(xHair.getName());
					if (histogram == null) {
						histogram = new uk.ac.diamond.scisoft.analysis.dataset.function.Histogram(256);
					}

					ImageData imgD = (ImageData) image.clone();
					int intensities[] = new int[imgD.width * imgD.height];
					int i = 0;
					if (image.depth > 16) {
						for (int h = 0; h < imgD.height; h++) {
							for (int w = 0; w < imgD.width; w++) {
								int pixel = imgD.getPixel(w, h);

								// to equate it to a 10 bit number of 1024 shift it right by 14
								intensities[i++] = pixel >> 8;
							}
						}
					} else {
						for (int h = 0; h < imgD.height; h++) {
							for (int w = 0; w < imgD.width; w++) {
								int pixel = imgD.getPixel(w, h);

								// to equate it to a 10 bit number of 1024 shift it right by 14
								intensities[i++] = pixel;
							}
						}

					}

					IntegerDataset ds = new IntegerDataset(intensities, imgD.width, imgD.height);

					int max = ds.max().intValue();
					int min = ds.min().intValue();
					histogram.setMinMax(min, max);
					ds.setName("Histogram Dataset");
					AbstractDataset histogramDs = histogram.value(ds).get(0);
					histogramDs.setName("Histogram Dataset");

					IntegerDataset xaxisRange = IntegerDataset.arange(min, max, 255);
					xaxisRange.setName("Intensities");

					if (histogramTrace == null) {
						histogramTrace = plottingSystem.createLineTrace("Histogram");
					}

					histogramTrace.setData(xaxisRange, histogramDs);

					if (plottingSystem.getTrace("Histogram") == null) {
						plottingSystem.addTrace(histogramTrace);
					}
					plottingSystem.repaint();

					// plottingSystem.updatePlot1D(xaxisRange, dsHisto, null);
					plottingSystem.setTitle("Histogram Plot");
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
			if (xHair == null || plottingSystem.getRegion(xHair.getName()) == null) {
				this.xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("DragLine", plottingSystem),
						IRegion.RegionType.XAXIS_LINE);

				xHair.addROIListener(mouseFollowRoiListener);

				addMouseFollowLineRegion(xHair);
				if (mode == MODE.HISTOGRAM) {
					xHair.addMouseListener(mouseFollowRegionMouseListner);
				}
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
			if (mode == MODE.PROFILE) {
				for (PlottingSystemActionListener lis : lineListeners) {
					int[] intPoint = evt.getROI().getIntPoint();
					lis.profileLineMovedTo(intPoint[0], profileLineTrace.getYData().getInt(intPoint[0]));
				}
			}
		}

		@Override
		public void roiChanged(ROIEvent evt) {
			update(evt);
		}

	};

	private MouseListener mouseFollowRegionMouseListner = new MouseListener.Stub() {
		@Override
		public void mousePressed(org.eclipse.draw2d.MouseEvent me) {
			if (mode == MODE.HISTOGRAM) {
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
		}
	};

	private void removeXHairListeners() {
		if (xHair != null) {
			xHair.removeMouseListener(mouseFollowRegionMouseListner);
		}
	}

	private IRegion createStaticRegion(String nameStub, final ROIBase bounds, final Color snapShotColor,
			final RegionType regionType) throws Exception {

		final IRegion region = plottingSystem.createRegion(RegionUtils.getUniqueName(nameStub, plottingSystem),
				regionType);
		region.setRegionColor(snapShotColor);
		plottingSystem.addRegion(region);
		region.setROI(bounds);

		region.addROIListener(new IROIListener() {

			private Double pointX;

			@Override
			public void roiDragged(ROIEvent evt) {
				if (pointX == null) {
					pointX = evt.getROI().getPointX();
					logger.debug("distance started:{}", pointX);
				}
			}

			@Override
			public void roiChanged(ROIEvent evt) {
				// need to introspect the movement and propagate the change to hardware.
				double minValue = histogramTrace.getXData().min().doubleValue();
				double maxValue = histogramTrace.getXData().max().doubleValue();
				double endPoint = evt.getROI().getPointX();
				logger.debug("distance ended:{}", endPoint);
				for (PlottingSystemActionListener lis : lineListeners) {
					lis.histogramChangedRoi(minValue, maxValue, pointX, endPoint);
				}

				plottingSystem.removeRegion(region);
				region.removeROIListener(this);
				setShouldUpdatePlot(true);
			}
		});

		return region;
	}

	private void addMouseFollowLineRegion(IRegion region) {
		region.setVisible(true);
		region.setTrackMouse(true);
		region.setRegionColor(ColorConstants.red);
		region.setUserRegion(false); // They cannot see preferences or change it!
		plottingSystem.addRegion(region);
	}

}