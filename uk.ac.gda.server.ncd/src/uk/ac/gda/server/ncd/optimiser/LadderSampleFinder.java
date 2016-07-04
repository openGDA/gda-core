/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.server.ncd.optimiser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.roi.ROIList;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROI;
import org.eclipse.dawnsci.analysis.dataset.roi.XAxisLineBoxROIList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.SDAPlotter;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiBean;
import uk.ac.diamond.scisoft.analysis.plotserver.GuiParameters;

public class LadderSampleFinder {
	private static final Logger logger = LoggerFactory.getLogger(LadderSampleFinder.class);

	private List<Point> points;
	private String plotPanel;
	private double peakHeight = 0.5;
	private double minWidth = 0;

	public LadderSampleFinder() {
	}

	public LadderSampleFinder(String plotPanel) {
		this.plotPanel = plotPanel;
	}

	public void setPlotPanel(String plotPanel) {
		this.plotPanel = plotPanel;
	}

	public void setPeakHeight(double peakHeight) {
		this.peakHeight = peakHeight;
	}

	public void setMinWidth(double width) {
		this.minWidth = width;
	}

	public List<Double> process(double[] x, double[] y) {
		setPoints(x, y);
		int[] normal = getNormalisedArray();
		List<Double> peaks = getAllPeaks(normal);
		if (plotPanel != null && plotPanel.length() > 0) {
			logger.debug("Plotting found features on plot: '{}'", plotPanel);
			plotResults(peaks, x, y);
		}
		return peaks;
	}

	private void setPoints(double[] x, double[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Arrays not equal length");
		}
		points = new ArrayList<Point>();
		for (int i = 0; i < y.length; i++) {
			points.add(new Point(x[i], y[i]));
		}
		Collections.sort(points);
	}

	private int[] getNormalisedArray() {
		int len = points.size();
		DescriptiveStatistics ds = getStatistics();
		double base = ds.getPercentile(50);
		double halfSD = peakHeight * ds.getStandardDeviation();
		int[] normal = new int[len];
		for (int i = 0; i < len; i++) {
			if (Math.abs(points.get(i).y - base) < halfSD) {
				normal[i] = 0;
			} else {
				normal[i] = 1;
			}
		}
		return normal;
	}

	private DescriptiveStatistics getStatistics() {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (Point p : points) {
			ds.addValue(p.y);
		}
		return ds;
	}

	private List<Double> getAllPeaks(int[] normal) {
		List<Double> peaks = new ArrayList<Double>();
		if (normal.length == 0) {
			return peaks;
		}
		int lastBase = -1;
		int lastValue = normal[0];

		for (int i = 0; i < normal.length; i++) {
			int current = normal[i];
			if (current > lastValue) {
				//start of peak
				lastBase = i-1;
			} else if (current < lastValue) {
				//end of peak
				if (lastBase > -1) {
					double width = points.get(i).x - points.get(lastBase).x;
					if (minWidth == 0 || width > minWidth) {// ignore outliers
						double centre = (points.get(lastBase).x + points.get(i).x) / 2;
						peaks.add(centre);
					}
				}
			}
			lastValue = current;
		}
		return peaks;
	}

	private void plotResults(List<Double> features, double[] x, double[] y) {
		try {
			SDAPlotter.clearPlot(plotPanel);
			SDAPlotter.plot(plotPanel, DatasetFactory.createFromObject(x), DatasetFactory.createFromObject(y));
			GuiBean guiBean = SDAPlotter.getGuiBean(plotPanel);
			ROIList<XAxisLineBoxROI> list = new XAxisLineBoxROIList();
			int i = 0;
			for (Double feature : features) {
				XAxisLineBoxROI roi = new XAxisLineBoxROI(feature, 0, 0, 0, 0);
				roi.setName("feature-" + i++);
				list.add(roi);
			}
			guiBean.put(GuiParameters.ROIDATALIST, list);
			SDAPlotter.setGuiBean(plotPanel, guiBean);
		} catch (Exception e) {
			logger.error("Failed to plot features", e);
		}
	}

	private class Point implements Comparable<Point> {
		double x;
		double y;
		Point(double x, double y) {
			this.x = x;
			this.y = y;
		}
		@Override
		public int compareTo(Point o) {
			return ((Double)x).compareTo(o.x);
		}
	}
}
