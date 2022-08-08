/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import static org.eclipse.january.dataset.DatasetUtils.convertToDataset;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.region.IRegion.RegionType;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.Maths;
import org.eclipse.january.dataset.Slice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.rcp.ncd.calibration.CalibrationSet;
import gda.rcp.ncd.calibration.ObservedFeature;
import gda.rcp.ncd.calibration.views.BraggCalibrationModel.CalibrationListener;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.gda.server.ncd.calibration.ExpectedFeature.FeatureType;

public class BraggScanView extends ViewPart implements CalibrationListener {
	private static final Logger logger = LoggerFactory.getLogger(BraggScanView.class);

	public static final String ID = "gda.rcp.ncd.calibration.views.braggscan";

	private static final Color OBSERVED_FEATURE_COLOUR = new Color(Display.getDefault(), 250, 20, 10);
	private static final Color EXPECTED_FEATURE_COLOUR = new Color(Display.getDefault(), 20, 20, 20);
	private static final Color IGNORED_FEATURE_COLOUR = new Color(Display.getDefault(), 100, 100, 100);

	private static final int IGNORED_FEATURE_THICKNESS = 1;
	private static final int FEATURE_LINE_THICKNESS = 3;

	private static final String EXAFS_TITLE = "EXAFS";
	private static final String EXAFS_DERIVATIVE_TITLE = "EXAFS Derivative";

	private static final String OBSERVED_PREFIX = "obs";
	private static final String EXPECTED_PREFIX = "exp";

	private IPlottingSystem<Composite> exafsPlot;
	private IPlottingSystem<Composite> exafsDerivativePlot;

	private ObservedValueListener plotListener;
	private ObservedValueListener derivativePlotListener;
	private BraggCalibrationModel service;
	private int featureCount;

	private class ObservedValueListener extends IROIListener.Stub {
		private final IPlottingSystem<Composite> plot;
		public ObservedValueListener(IPlottingSystem<Composite> plot) {
			this.plot = plot;
		}
		@Override
		public void roiChanged(ROIEvent evt) {
			IRegion reg = (IRegion) evt.getSource();
			var f = (ObservedFeature)reg.getUserObject();
			// This is a hack to work around the x-point of an x-axis line not being
			// the x position of the line. There is instead an invisible region where
			// the left edge is always the edge the user moves but the lower x value
			// is the report x position. If the axis is inverted, these are different.
			double x;
			if (plot.getSelectedXAxis().isInverted()) {
				x = reg.getROI().getBounds().getEndPoint()[0];
			} else {
				x = reg.getROI().getPointX();
			}
			f.setObservation(x);
			logger.debug("roiChanged: {} ({})", reg, reg.getUserObject());
			service.featureChanged();
		}
	}

	@PostConstruct
	public void init() {
		service = getSite().getService(BraggCalibrationModel.class);
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(2, false));
		createPlots(parent);
		service.addListener(this);
	}

	private void createPlots(Composite parent) {
		Composite plots = new Composite(parent, SWT.NONE);

		plots.setLayout(new GridLayout(2, false));
		plots.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

		try {
			exafsPlot = createPlot(plots, EXAFS_TITLE);
			exafsPlot.getSelectedXAxis().setInverted(true);
			plotListener = new ObservedValueListener(exafsPlot);
			exafsDerivativePlot = createPlot(plots, EXAFS_DERIVATIVE_TITLE);
			exafsDerivativePlot.getSelectedXAxis().setInverted(true);
			exafsDerivativePlot.getSelectedYAxis().setInverted(true);
			derivativePlotListener = new ObservedValueListener(exafsDerivativePlot);
			setAxisLabels();
		} catch (Exception e) {
			logger.error("Could not create plots", e);
		}
	}

	private void setAxisLabels() {
		exafsPlot.getAxes().get(0).setTitle("Bragg");
		exafsDerivativePlot.getAxes().get(0).setTitle("Bragg");
	}

	private IPlottingSystem<Composite> createPlot(Composite parent, String title) throws Exception {
		final IPlottingSystem<Composite> system = PlottingFactory.createPlottingSystem();
		system.createPlotPart(parent, title, null, PlotType.XY, this);
		system.setTitle(title);
		system.getPlotComposite().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		return system;
	}

	private void refreshPlots() {
		var currentFeature = service.getSelectedEdge();
		exafsPlot.clear();
		exafsDerivativePlot.clear();

		refreshFeatures(currentFeature);
		refreshData(currentFeature);
	}

	private void refreshData(CalibrationSet currentFeature) {
		exafsPlot.clear();
		exafsDerivativePlot.clear();
		if (currentFeature != null) {
			exafsPlot.setTitle(currentFeature.getName() + " " + EXAFS_TITLE);
			exafsDerivativePlot.setTitle(currentFeature.getName() + " " + EXAFS_DERIVATIVE_TITLE);
			plotData(currentFeature);
		}
	}

	private void refreshFeatures(CalibrationSet currentFeature) {
		exafsPlot.clearRegions();
		exafsDerivativePlot.clearRegions();
		featureCount = 0;
		Collection<ObservedFeature> features = currentFeature.getFeatures();
		features.forEach(f -> {
			if (f.getType() != FeatureType.DERIVATIVE) {
				addFeatureLine(exafsPlot, f.getExpected(), true, f.isActive())
						.ifPresent(l -> l.setUserObject(f));
				addFeatureLine(exafsPlot, f.getObservation(), false, f.isActive())
						.ifPresent(l -> {
							l.setUserObject(f);
							l.addROIListener(plotListener);
						});
			}
			if (f.getType() != FeatureType.SCAN) {
				addFeatureLine(exafsDerivativePlot, f.getExpected(), true, f.isActive())
						.ifPresent(l -> l.setUserObject(f));
				addFeatureLine(exafsDerivativePlot, f.getObservation(), false, f.isActive())
						.ifPresent(l -> {
							l.setUserObject(f);
							l.addROIListener(derivativePlotListener);
						});
			}
		});
	}

	private void plotData(CalibrationSet edge) {
		try {
			IDataset xData = edge.getDataFile().map(sf -> {
				try {
					var dataset = LoaderFactory.getData(sf.toString());
					return dataset.getLazyDataset(service.braggDataPath()).getSlice((Slice)null);
				} catch (Exception e) {
					return null;
				}
			}).orElse(null);
			if (xData == null) {
				return;
			}

			IDataset yData = edge.getDataFile().map(sf -> {
				try {
					var dataset = LoaderFactory.getData(sf.toString());
					return dataset.getLazyDataset(service.exafsPath()).getSlice((Slice)null);
				} catch (Exception e) {
					return null;
				}
			}).orElse(null);
			if (yData != null) {
				var dataName = edge.getDataFile().map(e -> e.getFileName().toString()).orElse("data");
				ILineTrace trace = TraceUtils.replaceCreateLineTrace(exafsPlot, "data");
				trace.setData(xData, yData);
				trace.setName(dataName);
				exafsPlot.addTrace(trace);
				ILineTrace derivativeTrace = TraceUtils.replaceCreateLineTrace(exafsDerivativePlot, "data");
				IDataset dyData = Maths.derivative(convertToDataset(xData), convertToDataset(yData), 3);
				derivativeTrace.setData(xData, dyData);
				derivativeTrace.setName(dataName);
				exafsDerivativePlot.addTrace(derivativeTrace);
			}

			exafsPlot.repaint();
			exafsDerivativePlot.repaint();
		} catch (Exception e) {
			logger.error("Could't plot data", e);
		}
	}

	private Optional<IRegion> addFeatureLine(IPlottingSystem<Composite> system, double kev, boolean fixed, boolean included) {
		String prefix = fixed ? EXPECTED_PREFIX : OBSERVED_PREFIX;
		if (!fixed && !included) {
			// Don't plot observed lines for ignored features
			return Optional.empty();
		}
		try {
			final IRegion line = system.createRegion(String.format("%s%d", prefix, featureCount++), RegionType.XAXIS_LINE);
			//all except initial parameter are ignored
			XAxisLineBoxROI lroi = new XAxisLineBoxROI(kev, 0, 0, 0, 0);
			line.setROI(lroi);
			system.addRegion(line);

			if (included && fixed) { // expected feature
				line.setRegionColor(EXPECTED_FEATURE_COLOUR);
				line.setLineWidth(FEATURE_LINE_THICKNESS);
				line.setMobile(false);
			} else if (included) { // (must be !fixed) => observed feature
				line.setRegionColor(OBSERVED_FEATURE_COLOUR);
				line.setLineWidth(FEATURE_LINE_THICKNESS);
			} else { // (must be fixed && !included) => Ignored expected feature
				line.setRegionColor(IGNORED_FEATURE_COLOUR);
				line.setLineWidth(IGNORED_FEATURE_THICKNESS);
				line.setActive(false);
			}
			line.setShowLabel(true);
			return Optional.of(line);
		} catch (Exception e) {
			logger.error("Could not create feature line", e);
		}
		return Optional.empty();
	}

	@Override
	public void setFocus() {
		// no-op
	}

	@Override
	public void dispose() {
		service.removeListener(this);
		super.dispose();
	}

	private void inDisplayThread(Runnable job) {
		if (Display.getCurrent() != null) {
			job.run();
		} else {
			Display.getDefault().syncExec(job);
		}
	}

	@Override
	public void newScanData(CalibrationSet calibration) {
		logger.debug("New scan data");
		inDisplayThread(this::refreshPlots);
	}

	@Override
	public void selectedEdgeChanged(CalibrationSet calibration) {
		logger.debug("calibration set changed: {}", calibration);
		inDisplayThread(this::refreshPlots);
	}

	@Override
	public void featureChanged() {
		inDisplayThread(() -> refreshFeatures(service.getSelectedEdge()));
	}
}
