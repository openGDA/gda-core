/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.devices.specs.phoibos.api;

import java.io.Serializable;

/**
 * Immutable class to hold live data generated from a SPECS analyser. The idea is this object can be built on the server
 * side and then pushed to clients and it will contain every thing they need to display the GUI.
 *
 * @author James Mudd
 */
public class SpecsPhoibosLiveDataUpdate implements Serializable {

	/**
	 * Generated serial ID
	 */
	private static final long serialVersionUID = 4498165026512874271L;

	private final String regionName;
	private final String positionString;
	private final int totalPoints;
	private final int currentPoint;
	private final int totalIterations;
	private final int currentPointInIteration;
	private final double[] spectrum;
	private final double[][] image;
	private final double[] keEnergyAxis;
	private final double[] beEnergyAxis;
	private final double[] yAxis;
	private final String yAxisUnits;

	public static class Builder {

		private String regionName;
		private String positionString;
		private int totalPoints;
		private int currentPoint;
		private int totalIterations;
		private int currentPointInIteration;
		private double[] spectrum;
		private double[][] image;
		private double[] keEnergyAxis;
		private double[] beEnergyAxis;
		private double[] yAxis;
		private String yAxisUnits;

		public Builder regionName(String val) {
			regionName = val;
			return this;
		}

		public Builder positionString(String val) {
			positionString = val;
			return this;
		}

		public Builder totalPoints(int val) {
			totalPoints = val;
			return this;
		}

		public Builder currentPoint(int val) {
			currentPoint = val;
			return this;
		}

		public Builder totalIterations(int val) {
			totalIterations = val;
			return this;
		}

		public Builder currentPointInIteration(int val) {
			currentPointInIteration = val;
			return this;
		}

		public Builder spectrum(double[] val) {
			spectrum = val;
			return this;
		}

		public Builder image(double[][] val) {
			image = val;
			return this;
		}

		public Builder keEnergyAxis(double[] val) {
			keEnergyAxis = val;
			return this;
		}

		public Builder beEnergyAxis(double[] val) {
			beEnergyAxis = val;
			return this;
		}

		public Builder yAxis(double[] val) {
			yAxis = val;
			return this;
		}

		public Builder yAxisUnits(String val) {
			yAxisUnits = val;
			return this;
		}

		public SpecsPhoibosLiveDataUpdate build() {
			return new SpecsPhoibosLiveDataUpdate(this);
		}
	}

	private SpecsPhoibosLiveDataUpdate(Builder builder) {

		regionName = builder.regionName;
		positionString = builder.positionString;
		totalPoints = builder.totalPoints;
		currentPoint = builder.currentPoint;
		totalIterations = builder.totalIterations;
		currentPointInIteration = builder.currentPointInIteration;
		spectrum = builder.spectrum;
		image = builder.image;
		keEnergyAxis = builder.keEnergyAxis;
		beEnergyAxis = builder.beEnergyAxis;
		yAxis = builder.yAxis;
		yAxisUnits = builder.yAxisUnits;
	}

	public String getPositionString() {
		return positionString;
	}

	public String getRegionName() {
		return regionName;
	}

	public int getTotalPoints() {
		return totalPoints;
	}

	public int getCurrentPoint() {
		return currentPoint;
	}

	public int getTotalIterations() {
		return totalIterations;
	}

	public int getcurrentPointInIteration() {
		return currentPointInIteration;
	}

	public double[] getSpectrum() {
		return spectrum;
	}

	public double[][] getImage() {
		return image;
	}

	public double[] getKeEnergyAxis() {
		return keEnergyAxis;
	}

	public double[] getBeEnergyAxis() {
		return beEnergyAxis;
	}

	public double[] getyAxis() {
		return yAxis;
	}

	public String getYAxisUnits() {
		return yAxisUnits;
	}

	public boolean isFirstUpdate() {
		return getCurrentPoint() == 1 && getcurrentPointInIteration() == 1;
	}

}
