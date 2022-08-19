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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Simple class to hold information about a 2D mapping scan path for plotting/metadata
 */
@JsonDeserialize(builder = MappingPathInfo.Builder.class)
public class MappingPathInfo implements IPathInfo {

	private static final String POINT_COUNT_FORMAT = "%,d";
	private static final String DOUBLE_FORMAT = "%.4g";

	private final UUID eventId;

	/**
	 * The source of the path, i.e. the mapping view.
	 */
	private final String sourceId;

	/*
	 * Number of 2D points in the scan
	 */
	private final int innerPointCount;

	/*
	 * Number of outer points in the scan.
	 */
	private final int outerPointCount;

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

	protected MappingPathInfo( // NOSONAR: the constructor should only be called by the Builder
			final UUID eventId,
			final String sourceId,
			final int innerPointCount,
			final int outerPointCount,
			final double smallestXStep,
			final double smallestYStep,
			final double smallestAbsStep,
			final double[] xCoordinates,
			final double[] yCoordinates) {
		this.eventId = eventId;
		this.sourceId = sourceId;
		this.innerPointCount = innerPointCount;
		this.outerPointCount = outerPointCount;
		this.smallestXStep = smallestXStep;
		this.smallestYStep = smallestYStep;
		this.smallestAbsStep = smallestAbsStep;
		this.xCoordinates = xCoordinates;
		this.yCoordinates = yCoordinates;
	}

	@Override
	public UUID getEventId() {
		return eventId;
	}

	@Override
	public String getSourceId() {
		return sourceId;
	}

	public int getInnerPointCount() {
		return innerPointCount;
	}

	public int getOuterPointCount() {
		return outerPointCount;
	}

	@Override
	public int getTotalPointCount() {
		return innerPointCount * outerPointCount;
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
		result = prime * result + sourceId.hashCode();
		result = prime * result + innerPointCount;
		long temp;
		temp = Double.doubleToLongBits(smallestAbsStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(smallestXStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(smallestYStep);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + outerPointCount;
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
		MappingPathInfo other = (MappingPathInfo) obj;
		if (!sourceId.equals(other.sourceId))
			return false;
		if (innerPointCount != other.innerPointCount)
			return false;
		if (Double.doubleToLongBits(smallestAbsStep) != Double.doubleToLongBits(other.smallestAbsStep))
			return false;
		if (Double.doubleToLongBits(smallestXStep) != Double.doubleToLongBits(other.smallestXStep))
			return false;
		if (Double.doubleToLongBits(smallestYStep) != Double.doubleToLongBits(other.smallestYStep))
			return false;
		if (outerPointCount != other.outerPointCount)
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
		return "PathInfo [innerPointCount=" + innerPointCount + ", outerPointCount=" + outerPointCount
				+ ", smallestXStep=" + smallestXStep + ", smallestYStep=" + smallestYStep + ", smallestAbsStep="
				+ smallestAbsStep + ", xCoordinates=" + coordsToString(xCoordinates) + ", yCoordinates="
				+ coordsToString(yCoordinates) + "]";
	}

	private String coordsToString(double[] coordinates) {
		return "[" + coordinates.length + " items]";
	}

	public static <B extends Builder<B>> Builder<B> builder() {
		return new Builder<>();
	}

	@JsonPOJOBuilder
	public static class Builder<B extends Builder<B>> {
		// Note: this uses the Builder Pattern with Inheritence and Generic pattern as discussed here: http://www.javabyexamples.com/lets-discuss-builder-pattern

		protected UUID eventId;
		protected String sourceId;
		protected int innerPointCount;
		protected int outerPointCount;
		protected double smallestXStep;
		protected double smallestYStep;
		protected double smallestAbsStep;
		protected double[] xCoordinates;
		protected double[] yCoordinates;

		public B withEventId(UUID eventId) {
			this.eventId = eventId;
			return self();
		}

		public B withSourceId(String sourceId) {
			this.sourceId = sourceId;
			return self();
		}

		public B withInnerPointCount(int innerPointCount) {
			this.innerPointCount = innerPointCount;
			return self();
		}

		public B withOuterPointCount(int outerPointCount) {
			this.outerPointCount = outerPointCount;
			return self();
		}

		public B withSmallestXStep(double smallestXStep) {
			this.smallestXStep = smallestXStep;
			return self();
		}

		public B withSmallestYStep(double smallestYStep) {
			this.smallestYStep = smallestYStep;
			return self();
		}

		public B withSmallestAbsStep(double smallestAbsStep) {
			this.smallestAbsStep = smallestAbsStep;
			return self();
		}

		@JsonProperty("xCoordinates")
		public B withXCoordinates(double[] xCoordinates) {
			this.xCoordinates = xCoordinates;
			return self();
		}

		@JsonProperty("yCoordinates")
		public B withYCoordinates(double[] yCoordinates) {
			this.yCoordinates = yCoordinates;
			return self();
		}

		public B withxCoordinateList(Collection<Double> xCoordinates) {
			this.xCoordinates = asArray(xCoordinates);
			return self();
		}

		public B withyCoordinateList(Collection<Double> yCoordinates) {
			this.yCoordinates = asArray(yCoordinates);
			return self();
		}

		@SuppressWarnings("unchecked")
		protected B self() {
			return (B) this;
		}

		private double[] asArray(Collection<Double> coordinates) {
			return coordinates.stream()
					.mapToDouble(Double::doubleValue)
					.toArray();
		}

		public MappingPathInfo build() {
			return new MappingPathInfo(
					eventId,
					sourceId,
					innerPointCount,
					outerPointCount,
					smallestXStep,
					smallestYStep,
					smallestAbsStep,
					Objects.requireNonNullElse(xCoordinates, new double[] {}),
					Objects.requireNonNullElse(yCoordinates, new double[] {}));
		}
	}
}