/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.api.document.scanpath;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Simple class to hold information about a 2D mapping scan path for plotting/metadata
 */
@JsonDeserialize(builder = PathInfo.Builder.class)
public final class PathInfo implements Serializable {

	private static final long serialVersionUID = 7753482435874684328L;

	private static final String POINT_COUNT_FORMAT = "%,d";
	private static final String DOUBLE_FORMAT = "%.4g";

	/*
	 * Number of 2D points in the scan
	 */
	private final int innerPointCount;

	/**
	 * Total number of points in the scan including any outer axes
	 */
	private final int totalPointCount;

	/*
	 * Smallest change in x position between any two points
	 */
	private final double smallestXStep;

	/*
	 * Smallest change in y position between any two points
	 */
	private final double smallestYStep;

	/*
	 * Smallest change in position between any two points
	 */
	private final double smallestAbsStep;

	/*
	 * Positions in x axis of all coordinates
	 */
	private final double[] xCoordinates;

	/*
	 * Positions in y axis of all coordinates
	 */
	private final double[] yCoordinates;

	public PathInfo(
			final int innerPointCount,
			final int totalPointCount,
			final double smallestXStep,
			final double smallestYStep,
			final double smallestAbsStep,
			final double[] xCoordinates,
			final double[] yCoordinates) {
		super();
		this.innerPointCount = innerPointCount;
		this.totalPointCount = totalPointCount;
		this.smallestXStep = smallestXStep;
		this.smallestYStep = smallestYStep;
		this.smallestAbsStep = smallestAbsStep;
		this.xCoordinates = xCoordinates;
		this.yCoordinates = yCoordinates;
	}

	public int getInnerPointCount() {
		return innerPointCount;
	}

	public int getTotalPointCount() {
		return totalPointCount;
	}

	public int getReturnedPointCount() {
		return xCoordinates.length;
	}

	public double getSmallestXStep() {
		return smallestXStep;
	}

	public double getSmallestYStep() {
		return smallestYStep;
	}

	public double getSmallestAbsStep() {
		return smallestAbsStep;
	}

	public String getFormattedPointCount() {
		return String.format(POINT_COUNT_FORMAT, innerPointCount);
	}

	public String getFormattedSmallestXStep() {
		return formatDouble(smallestXStep);
	}

	public String getFormattedSmallestYStep() {
		return formatDouble(smallestYStep);
	}

	public String getFormattedSmallestAbsStep() {
		return formatDouble(smallestAbsStep);
	}

	private String formatDouble(double value) {
		if (value == Double.MAX_VALUE) {
			return "N/A";
		}
		return String.format(DOUBLE_FORMAT, value);
	}

	@JsonProperty("xCoordinates")
	public double[] getXCoordinates() {
		return xCoordinates;

	}

	@JsonProperty("yCoordinates")
	public double[] getYCoordinates() {
		return yCoordinates;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + innerPointCount;
		long temp;
		temp = Double.doubleToLongBits(smallestAbsStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(smallestXStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(smallestYStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + totalPointCount;
		result = prime * result + Arrays.hashCode(xCoordinates);
		result = prime * result + Arrays.hashCode(yCoordinates);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathInfo other = (PathInfo) obj;
		if (innerPointCount != other.innerPointCount)
			return false;
		if (Double.doubleToLongBits(smallestAbsStep) != Double.doubleToLongBits(other.smallestAbsStep))
			return false;
		if (Double.doubleToLongBits(smallestXStep) != Double.doubleToLongBits(other.smallestXStep))
			return false;
		if (Double.doubleToLongBits(smallestYStep) != Double.doubleToLongBits(other.smallestYStep))
			return false;
		if (totalPointCount != other.totalPointCount)
			return false;
		if (!Arrays.equals(xCoordinates, other.xCoordinates))
			return false;
		if (!Arrays.equals(yCoordinates, other.yCoordinates))
			return false;
		return true;
	}

	@Override
	public String toString() {
		// As the coordinate arrays may be very large only the number of elements is displayed.
		return "PathInfo [innerPointCount=" + innerPointCount + ", totalPointCount=" + totalPointCount
				+ ", smallestXStep=" + smallestXStep + ", smallestYStep=" + smallestYStep + ", smallestAbsStep="
				+ smallestAbsStep + ", xCoordinates=" + coordsToString(xCoordinates) + ", yCoordinates="
				+ coordsToString(yCoordinates) + "]";
	}

	private String coordsToString(double[] coordinates) {
		return "[" + coordinates.length + " items]";
	}

	public static Builder builder() {
		return new Builder();
	}

	@JsonPOJOBuilder
	public static final class Builder {
		private int innerPointCount;
		private int totalPointCount;
		private double smallestXStep;
		private double smallestYStep;
		private double smallestAbsStep;
		private double[] xCoordinates;
		private double[] yCoordinates;

	    public Builder withInnerPointCount(int innerPointCount) {
	        this.innerPointCount = innerPointCount;
	        return this;
	    }

	    public Builder withTotalPointCount(int totalPointCount) {
	        this.totalPointCount = totalPointCount;
	        return this;
	    }

	    public Builder withSmallestXStep(double smallestXStep) {
	        this.smallestXStep = smallestXStep;
	        return this;
	    }

	    public Builder withSmallestYStep(double smallestYStep) {
	        this.smallestYStep = smallestYStep;
	        return this;
	    }

	    public Builder withSmallestAbsStep(double smallestAbsStep) {
	        this.smallestAbsStep = smallestAbsStep;
	        return this;
	    }

	    @JsonProperty("xCoordinates")
	    public Builder withXCoordinates(double[] xCoordinates) {
	    	this.xCoordinates = xCoordinates;
	    	return this;
	    }

	    @JsonProperty("yCoordinates")
	    public Builder withYCoordinates(double[] yCoordinates) {
	    	this.yCoordinates = yCoordinates;
	    	return this;
	    }

	    public Builder withxCoordinateList(Collection<Double> xCoordinates) {
	    	this.xCoordinates = asArray(xCoordinates);
	    	return this;
	    }

	    public Builder withyCoordinateList(Collection<Double> yCoordinates) {
	    	this.yCoordinates = asArray(yCoordinates);
	    	return this;
	    }

	    private double[] asArray(Collection<Double> coordinates) {
	    	return coordinates.stream()
	    			.mapToDouble(Double::doubleValue)
	    			.toArray();
	    }

	    public PathInfo build() {
	    	return new PathInfo(
	    			innerPointCount,
	    			totalPointCount,
	    			smallestXStep,
	    			smallestYStep,
	    			smallestAbsStep,
	    			Objects.requireNonNullElse(xCoordinates, new double[] {}),
	    			Objects.requireNonNullElse(yCoordinates, new double[] {}));
	    }
	}
}