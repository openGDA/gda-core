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
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.TIFFImageLoader;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.OverlayType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.enums.PrimitiveType;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DConsumer;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.Overlay1DProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.overlay.OverlayProvider;
import uk.ac.diamond.scisoft.analysis.rcp.plotting.tools.AreaSelectEvent;
import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
import uk.ac.gda.client.tomo.DoublePointList;
import uk.ac.gda.client.tomo.DoublePointList.DoublePoint;
import uk.ac.gda.client.tomo.TiltPlotPointsHolder;

/**
 *
 */
public class TomoPlotComposite extends Composite implements Overlay1DConsumer {

	private static final String SCALING_REGION = "ScalingRegion";

	private static final Logger logger = LoggerFactory.getLogger(TomoPlotComposite.class);

	private Overlay1DProvider provider;

	private AbstractPlottingSystem plottingSystem;
	private ArrayList<IDataset> dataSets;

	private int registerPrimitive = -1;

	private IntegerDataset rawDataSlice;

	public interface OverlayLineListener {
		public void overlayAt(double xVal, long intensity);
	}

	/**
	 * @param parent
	 * @param style
	 * @throws Exception
	 */
	public TomoPlotComposite(Composite parent, int style) throws Exception {
		super(parent, style);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		// this.setLayout(layout);
		this.setLayout(new FillLayout());
		plottingSystem = PlottingFactory.createPlottingSystem();
		plottingSystem.createPlotPart(this, "", null, PlotType.PT1D_MULTI, null);

		dataSets = new ArrayList<IDataset>();
		// dataSetPlotter.registerOverlay(this);
		// lineListeners = new ArrayList<TomoPlotComposite.OverlayLineListener>();
		// TODO-Ravi check this is required?
		// dataSetPlotter.registerOverlay(page_rightWindow_profile);
	}

	@Override
	public void registerProvider(OverlayProvider provider) {
		logger.info("Registered Overlay1DProvider", provider);
		this.provider = (Overlay1DProvider) provider;
	}

	@Override
	public void unregisterProvider() {
		provider = null;
	}

	@Override
	public void removePrimitives() {
		logger.info("Remove primitives called");
	}

	@Override
	public void areaSelected(AreaSelectEvent event) {
		clearOverlays();
		provider.begin(OverlayType.VECTOR2D);
		registerPrimitive = provider.registerPrimitive(PrimitiveType.LINE);
		// provider.setTransparency(registerPrimitive, 0.5);
		provider.setColour(registerPrimitive, java.awt.Color.RED);
		try {
			provider.drawLine(registerPrimitive, event.getX(), 0, event.getX(), 70000);
		} catch (Exception ex) {
			logger.error("Error while drawing line:", ex);
		}
		provider.end(OverlayType.VECTOR2D);

		for (OverlayLineListener lis : lineListeners) {
			double x = event.getX();
			long intensity = rawDataSlice.getLong((int) event.getX());
			lis.overlayAt(x, intensity);
		}
	}

	private ArrayList<OverlayLineListener> lineListeners;

	public boolean addOverlayLineListener(OverlayLineListener overlayLineListener) {
		// return lineListeners.add(overlayLineListener);
		return true;
	}

	public boolean removeOverlayLineListener(OverlayLineListener overlayLineListener) {
		// return lineListeners.remove(overlayLineListener);
		return true;
	}

	private void clearOverlays() {
		if (registerPrimitive != -1) {
			provider.begin(OverlayType.VECTOR2D);
			provider.unregisterPrimitive(registerPrimitive);
			provider.end(OverlayType.VECTOR2D);
		}
	}

	private long timeSinceLastUpdate = 0;

	private AbstractDataset rawImgDs;

	private AbstractDataset darkImgDs;

	private uk.ac.diamond.scisoft.analysis.dataset.function.Histogram histogram;

	private IRegion scalingRegion;

	private boolean shouldUpdatePlot = true;

	public void updatePointPlot(final List<Point> points) {

	}

	public void updateProfilePlots(IProgressMonitor monitor, final int xStart, final int xEnd, final int y) {
		// the below delay routine is added on Mark Basham's suggestion which is needed so that the plotter does'nt get
		// over-updated. Time check if below 0.1s ignore request
		if (getDisplay() != null && !getDisplay().isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					removeOtherRegions();
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
			clearOverlays();
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

			plottingSystem.clear();
			plottingSystem.createPlot1D(axis, plotDataSets, monitor);
			getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					plottingSystem.setTitle("Intensity plot");
					plottingSystem.getSelectedYAxis().setFormatPattern("######.#");
					plottingSystem.getSelectedYAxis().setRange(0, 65600);
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

	public void cleanUp() {
		// dataSetPlotter.cleanUp();
		// provider.setPrimitiveVisible(registerPrimitive, false);
	}

	public void updatePlotPoints(final IProgressMonitor progress, final TiltPlotPointsHolder tiltPoints) {

		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				plottingSystem.clear();
				final DoublePointList centers1 = tiltPoints.getCenters1();
				final DoublePointList centers2 = tiltPoints.getCenters2();

				final DoublePointList line2 = tiltPoints.getLine2();
				DoubleDataset y3 = new DoubleDataset(line2.getYDoubleArray());
				DoubleDataset x3 = new DoubleDataset(line2.getXDoubleArray());
				ArrayList<AbstractDataset> singletonList = new ArrayList<AbstractDataset>(1);
				singletonList.add(y3);
				plottingSystem.createPlot1D(x3, singletonList, progress);

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
					IRegion region2 = plottingSystem.getRegion("RegionFit2");
					if (region2 != null) {
						plottingSystem.removeRegion(region2);
					}

					region2 = plottingSystem.createRegion("RegionFit2", RegionType.ELLIPSEFIT);
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
					logger.error("TODO put description of error here", e);
				}

			}
		});
	}

	private synchronized void setShouldUpdatePlot(boolean updatePlot) {
		this.shouldUpdatePlot = updatePlot;
	}

	public void updateHistogramData(final ImageData image) throws Exception {
		if (shouldUpdatePlot) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					plottingSystem.clear();
					removeOtherRegions(SCALING_REGION);
					if (histogram == null) {
						histogram = new uk.ac.diamond.scisoft.analysis.dataset.function.Histogram(256);
					}

					ImageData imgD = (ImageData) image.clone();
					// Need to consider a clone because once the original data is set below the high values then it can
					// be
					// reverted
					// for values above.
					// imgDataClone.palette.isDirect = false;
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

					// IntegerDataset ds = new IntegerDataset(image.data, image.width, image.height);
					int max = ds.max().intValue();
					int min = ds.min().intValue();
					histogram.setMinMax(min, max);
					AbstractDataset value = histogram.value(ds).get(0);

					ArrayList<AbstractDataset> dsHisto = new ArrayList<AbstractDataset>(1);
					dsHisto.add(value);
					//

					plottingSystem.createPlot1D(IntegerDataset.arange(min, max, 255), dsHisto, null);
					plottingSystem.getSelectedYAxis().setFormatPattern("#####");
					plottingSystem.getSelectedXAxis().setFormatPattern("#####");
					// plottingSystem.getSelectedYAxis().setRange(0, 15000);

					scalingRegion = plottingSystem.getRegion(SCALING_REGION);
					
					if (scalingRegion == null) {
						try {
							scalingRegion = plottingSystem.createRegion(SCALING_REGION, RegionType.XAXIS_LINE);
						} catch (Exception e) {
							logger.error("TODO put description of error here", e);
						}
						scalingRegion.setLineWidth(4);
						scalingRegion.setRegionColor(ColorConstants.darkGreen);

//						ScalingRegionMouseListener scalingRegionMouseListener = new ScalingRegionMouseListener();
//
//						scalingRegion.addMouseListener(scalingRegionMouseListener);
//						scalingRegion.addMouseMotionListener(scalingRegionMouseListener);
						
						scalingRegion.setROI(new LinearROI(new double[] { 25000, 0 }, new double[] { 25000, 15000 }));
						plottingSystem.addRegion(scalingRegion);
					}
					
					scalingRegion.setMobile(true);
					scalingRegion.setUserRegion(true);
					
					IROIListener roiListener = new IROIListener.Stub(){
						@Override
						public void roiChanged(ROIEvent evt) {
							super.roiChanged(evt);
							setShouldUpdatePlot(true);
						}
						
						@Override
						public void roiDragged(ROIEvent evt) {
							super.roiDragged(evt);
							setShouldUpdatePlot(false);
						}
					};
					
					scalingRegion.addROIListener(roiListener);
				}
			});
		}
	}

	class ScalingRegionMouseListener implements MouseListener, MouseMotionListener {

		@Override
		public void mousePressed(MouseEvent me) {
			logger.debug("Scaling region mouse pressed:{}", me.getSource());
		}

		@Override
		public void mouseReleased(MouseEvent me) {
			logger.debug("Scaling region mouseReleased:{}", me.getSource());
		}

		@Override
		public void mouseDoubleClicked(MouseEvent me) {
			logger.debug("Scaling region mouseDoubleClicked:{}", me.getSource());
		}

		@Override
		public void mouseDragged(MouseEvent me) {
			logger.debug("Scaling region mouseDragged:{}", me.getSource());
		}

		@Override
		public void mouseEntered(MouseEvent me) {
			logger.debug("Scaling region mouseEntered:{}", me.getSource());
		}

		@Override
		public void mouseExited(MouseEvent me) {
			logger.debug("Scaling region mouseExited:{}", me.getSource());
		}

		@Override
		public void mouseHover(MouseEvent me) {
			logger.debug("Scaling region mouseHover:{}", me.getSource());
		}

		@Override
		public void mouseMoved(MouseEvent me) {
			logger.debug("Scaling region mouseMoved:{}", me.getSource());
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

}
