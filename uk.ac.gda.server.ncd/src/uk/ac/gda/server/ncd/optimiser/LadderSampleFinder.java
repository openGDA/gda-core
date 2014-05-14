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

/**
 * 	# find base line
	# find excursions from base line and back
	# count if reasonable number, redo with refined criteria if necessary
	# find longest regular spacing and reject points not on it
	# return
 */
public class LadderSampleFinder {
	
	private List<Point> points;
	private String plotPanel;

	public void setPlotPanel(String plotPanel) {
		this.plotPanel = plotPanel;
	}
	
	public List<Double> process(double[] x, double[] y) {
		setPoints(x,y);
		int[] normal = getNormalisedArray(); 
		List<Peak> peaks = getAllPeaks(normal);
		List<Double> regularPositions = getRegularPeakPositions(peaks);
		
		return validPeakList(regularPositions, peaks);
	}
	
	private void setPoints(double[] x, double[] y) {
		if (x.length != y.length) {
			throw new IllegalArgumentException("Arrays not equal lengths");
		}
		points = new ArrayList<LadderSampleFinder.Point>();
		for (int i = 0; i < y.length; i++) {
			points.add(new Point(x[i], y[i]));
		}
		Collections.sort(points);
	}
	
	private double maxY() {
		double max = Double.MIN_VALUE;
		for (Point p : points) {
			max = Math.max(max, p.y);
		}
		return max;
	}
	
	private double minY() {
		double min = Double.MAX_VALUE;
		for (Point p : points) {
			min = Math.min(min, p.y);
		}
		return min;
	}
	
	private int[] getNormalisedArray() {
		int len = points.size();
		double base = findBaseValue();
		double halfSD = 0.5 * findSD();
		int[] normal = new int[len];
		for (int i = 0; i < len; i++) {
			if (Math.abs(points.get(i).y - base) < Math.abs(halfSD)) {
				normal[i] = 0;
			} else {
				normal[i] = 1;
			}
		}
		return normal;
	}
	
	private double findBaseValue() {
		double span = maxY() - minY();
		double step = span / 100;
		
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (Point p : points) {
			ds.addValue(Math.round(p.y / step)*step);
		}
		return ds.getPercentile(50);
	}

	private double findSD() {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (Point p : points) {
			ds.addValue(p.y);
		}
		return ds.getStandardDeviation();
	}
	
	private List<Peak> getAllPeaks(int[] normal) {
		List<Peak> peaks = new ArrayList<Peak>();
		int peakStart = 0;
		for (int i = 0; i < normal.length; i++) {
			if (peakStart == i && normal[i] == 0) { //base line
				peakStart++;
			} else if (peakStart < i && normal[i] == 0) { //end of peak
				int end = i - 1;
				int peakCentre = end - (end - peakStart)/2;
				peaks.add(new Peak(points.get(peakStart), points.get(end), points.get(peakCentre)));
				peakStart = i + 1;
			}
		}
		return peaks;
	}
	
	private List<Double> getRegularPeakPositions(List<Peak> peaks) {
		double spacing = findMedianGap(peaks);
		double[] optimised = optimiseSpacing(peaks, spacing);
		int basePos = (int) optimised[0];
		spacing = optimised[1];
		Peak basePeak = peaks.get(basePos);
		
		List<Double> regularPeakPositions = calculateRegularPeaks(basePeak, spacing, peaks.get(0).left.x, peaks.get(peaks.size() - 1).right.x);

		return regularPeakPositions;
	}
	
	private double findMedianGap(List<Peak> peaks) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		Peak prev = peaks.get(0);
		for (int gap = 0; gap < peaks.size() - 1; gap++) {
			Peak peak = peaks.get(gap+1);
			ds.addValue(peak.centre.x - prev.centre.x);
			prev = peak;
		}
		return ds.getPercentile(50);
	}
	
	private double[] optimiseSpacing(List<Peak> peaks, double spacing) {
		double variation = spacing * 0.2; //ARGH MAGIC NUMBERS
		double spacingStep = spacing * 0.01;
		double lowerSpacingBound = spacing - variation;
		double upperSpacingBound = spacing + variation;
		int optimalBase = 0;
		double optimalSpacing = 0;
		double minimumError = Double.MAX_VALUE;
		for (spacing = lowerSpacingBound; Math.abs(spacing) < Math.abs(upperSpacingBound); spacing += spacingStep) {
			for (int i = 0; i < peaks.size(); i++) {
				double offset = peaks.get(i).centre.x;
				double sum = 0;
				for (int j = 0; j < peaks.size(); j++) {
					double error = Math.abs((j - i) * spacing - peaks.get(j).centre.x + offset);
					sum += error;
				}
				if (sum < minimumError) {
					minimumError = sum;
					optimalBase = i;
					optimalSpacing = spacing;
				}
			}
		}
		return new double[] {optimalBase, optimalSpacing};
	}
	
	private List<Double> calculateRegularPeaks(Peak basePeak, double spacing, double lowerLimit, double upperLimit) {
		List<Double> regularPeaks = new ArrayList<Double>();
//		double lowerLimit = points.get(0).x;
//		double upperLimit = points.get(points.size() - 1).x;
		int count = (int) ((upperLimit - lowerLimit) / spacing);
		
		for (int i = -count; i < count; i++) {
			double newPeak = basePeak.centre.x - i * spacing;
			if (newPeak > lowerLimit && newPeak < upperLimit) {
				regularPeaks.add(newPeak);
			}
		}
		return regularPeaks;
	}
	
	private List<Double> validPeakList(List<Double> regularPeaks, List<Peak> peaks) {
		Collections.sort(regularPeaks);
		int i = 0;
		for (int j = 0; j < regularPeaks.size(); j++) {
			double d = regularPeaks.get(j);
			while (i <  peaks.size() - 1 && peaks.get(i).right.x < d) {
				i++;
			}
			Peak peak = peaks.get(i);
			if (d > peak.left.x) {
				regularPeaks.set(j, peak.centre.x);
			}
		}
		
		return regularPeaks;
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
	
	private class Peak implements Comparable<Peak> {
		Point left;
		Point right;
		Point centre;
		Peak(Point l, Point r, Point c) {
			left = l;
			right = r;
			centre = c;
		}
		@Override
		public int compareTo(Peak o) {
			return ((Double)centre.x).compareTo(o.centre.x);
		}
		@Override
		public String toString() {
			return String.format("%.1f",centre.x);
		}
	}
}
