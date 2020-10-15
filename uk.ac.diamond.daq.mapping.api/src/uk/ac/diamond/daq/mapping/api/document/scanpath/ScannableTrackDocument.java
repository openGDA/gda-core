/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import gda.device.Scannable;
import gda.factory.Finder;

/**
 * Describes how an acquisition should move along a {@code Scannable} values.
 *
 * <p>
 * The {@code Scannable} is assumed one-dimensional and its boundaries defined by {@code start} and {@code stop} values.
 * <p>
 *
 * <p>
 * The movement can be defined in two mutually exclusive modes:
 * <ul>
 * <li>per {@code points}: the user defines the number of acquisition points between {@code start} and {@code stop}</li>
 * <li>per {@code step}: the user defines the length of each movement from {@code start} and {@code stop}</li>
 * </ul>
 * Once one of the two is set the other is forced to the {@code MIN_VALUE} of its primitive type. The rule is enforced
 * by the {@link Builder}.
 * <p>
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = ScannableTrackDocument.Builder.class)
public class ScannableTrackDocument {

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * scannable
	 */
	private final String scannable;
	/**
	 * A label to identify uniquely the role of this scannable
	 */
	private final String axis;
	/**
	 * The interval starting point
	 */
	private final double start;
	/**
	 * The interval ending point
	 */
	private final double stop;
	/**
	 * The required step from {@code start} to {@code stop}
	 */
	private final double step;
	/**
	 * The required number of points from {@code start} to {@code stop}
	 */
	private final int points;

	/**
	 * @param scannable
	 *            the scannable identifier
	 * @param start
	 *            the start point
	 * @param stop
	 *            the end point
	 * @param points
	 *            the number of points in the scan
	 * @param step
	 *            the step length for each movement from {@code start} to {@code stop}
	 */
	ScannableTrackDocument(String scannable, String axis, double start, double stop, int points, double step) {
		super();
		this.scannable = scannable;
		this.axis = axis;
		this.start = start;
		this.stop = stop;
		this.points = points;
		this.step = step;
	}

	public String getScannable() {
		return scannable;
	}

	public String getAxis() {
		return axis;
	}

	public double getStart() {
		return start;
	}

	public double getStop() {
		return stop;
	}

	public double getStep() {
		return step;
	}

	public int getPoints() {
		return points;
	}

	@JsonIgnore
	public boolean hasNegativeValues() {
		return getStart() < 0 || getStop() < 0;
	}

	@JsonIgnore
	public double length() {
		return getStop() - getStart();
	}

	@JsonIgnore
	public Optional<Scannable> getScannableBean() {
		return Finder.findOptional(getScannable());
	}

	@Override
	@JsonIgnore
	public String toString() {
		return "ScannableTrackDocument [scannable=" + scannable + ", axis=" + axis + ", start=" + start + ", stop="
				+ stop + ", step=" + step + ", points=" + points + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + points;
		result = prime * result + ((scannable == null) ? 0 : scannable.hashCode());
		long temp;
		temp = Double.doubleToLongBits(start);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(step);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(stop);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		ScannableTrackDocument other = (ScannableTrackDocument) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equals(other.axis))
			return false;
		if (points != other.points)
			return false;
		if (scannable == null) {
			if (other.scannable != null)
				return false;
		} else if (!scannable.equals(other.scannable))
			return false;
		if (Double.doubleToLongBits(start) != Double.doubleToLongBits(other.start))
			return false;
		if (Double.doubleToLongBits(step) != Double.doubleToLongBits(other.step))
			return false;
		if (Double.doubleToLongBits(stop) != Double.doubleToLongBits(other.stop))
			return false;
		return true;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String scannable;
		private String axis;
		private double start;
		private double stop;
		private double step = Double.MIN_VALUE;
		private int points = Integer.MIN_VALUE;

		public Builder() {
		}

		public Builder(final ScannableTrackDocument parent) {
			this.scannable = parent.getScannable();
			this.axis = parent.getAxis();
			this.start = parent.getStart();
			this.stop = parent.getStop();
			this.step = parent.getStep();
			this.points = parent.getPoints();
		}

		public Builder withScannable(String scannable) {
			this.scannable = scannable;
			return this;
		}

		public Builder withAxis(String axis) {
			this.axis = axis;
			return this;
		}

		public Builder withStart(double start) {
			this.start = start;
			return this;
		}

		public Builder withStop(double stop) {
			this.stop = stop;
			return this;
		}

		public Builder withStep(double step) {
			this.step = step;
			if (this.step > Double.MIN_NORMAL) {
				this.points = Integer.MIN_VALUE;
			}
			return this;
		}

		public Builder withPoints(int points) {
			this.points = points;
			if (this.points > Integer.MIN_VALUE) {
				this.step = Double.MIN_VALUE;
			}
			return this;
		}

		public ScannableTrackDocument build() {
			return new ScannableTrackDocument(scannable, axis, start, stop, points, step);
		}
	}
}
