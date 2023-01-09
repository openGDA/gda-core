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

import java.util.Objects;

import org.eclipse.scanning.api.points.models.IBoundsToFit;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
 * Once one of the two is set the other is forced to the {@code MIN_VALUE} of its primitive type.
 * <p>
 *
 * @author Maurizio Nagni
 */
public class ScannableTrackDocument {

	public enum Axis {
		X, Y, Z, THETA, STATIC
	}

	/**
	 * An identifier, usually a Spring bean name, to allow an acquisition controller to retrieve a real instance of the
	 * scannable
	 */
	private String scannable;
	/**
	 * A label to identify uniquely the role of this scannable
	 */
	private Axis axis;
	/**
	 * The interval starting point
	 */
	private double start;
	/**
	 * The interval ending point
	 */
	private double stop;
	/**
	 * The required step from {@code start} to {@code stop}
	 */
	private double step;
	/**
	 * The required number of points from {@code start} to {@code stop}
	 */
	private int points;

	private boolean continuous;

	private boolean alternating;

	public ScannableTrackDocument() {}

	public ScannableTrackDocument(ScannableTrackDocument other) {
		this.axis = other.getAxis();
		this.scannable = other.getScannable();
		this.start = other.getStart();
		this.stop = other.getStop();
		this.step = other.getStep();
		this.points = other.getPoints();
		this.alternating = other.isAlternating();
		this.continuous = other.isContinuous();
	}

	public String getScannable() {
		return scannable;
	}

	public void setScannable(String scannable) {
		this.scannable = scannable;
	}

	public Axis getAxis() {
		return axis;
	}

	public void setAxis(Axis axis) {
		this.axis = axis;
	}

	public double getStart() {
		return start;
	}

	public void setStart(double start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(double stop) {
		this.stop = stop;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public boolean isAlternating() {
		return alternating;
	}

	public void setAlternating(boolean alternating) {
		this.alternating = alternating;
	}

	public boolean isContinuous() {
		return continuous;
	}

	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}

	/**
	 * Calculates the points when {@link #getPoints()} == 0, with the following priorities
	 * <ul>
	 * <li>if points > 0 or step = 0 then points = getPoints()</li>
	 * <li>if step > 0 then points = (stop - start) / step, + 1 if {@code IBoundsToFit} behaviour not expected </li>
	 * </ul>
	 * @return the steps for this track document
	 */
	@JsonIgnore
	public int calculatedPoints() {
		if (getPoints() > 0 || getStep() == 0) {
			return getPoints();
		}
		final var isBoundsToFit = Boolean.getBoolean(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT);
		return (isBoundsToFit ? 0 : 1) + (int) Math.abs(length() / getStep());
	}

	/**
	 * Calculates the {@link #getStep()} when {@link #getStop()} - {@link #getStart()} is not zero and {@link #getStep()} > {@code Double.MIN_VALUE}
	 * <ul> If {@code IBoundsToFit} behaviour is expected, number of steps = number of points, else number steps = number points - 1
	 * <li>if step > Double.MIN_VALUE then step = getStep()</li>
	 * <li>if number of steps = 0 then step = 0</li>
	 * <li>if number of steps = 1 then step = length</li>
	 * <li>if number of steps > 1 then step = (stop - start) / number of steps</li>
	 * </ul>
	 * @return the steps for this track document
	 */
	@JsonIgnore
	public double calculatedStep() {
		if (getStep() > Double.MIN_VALUE) {
			return getStep();
		}
		if (getPoints() == 0 || getPoints() == 1)
			return 0;
		final var isBoundsToFit = Boolean.getBoolean(IBoundsToFit.PROPERTY_DEFAULT_BOUNDS_FIT);
		final double steps = isBoundsToFit ? points : points - 1;
		return length() / steps;
	}

	@JsonIgnore
	public double length() {
		return Math.abs(getStop() - getStart());
	}

	@Override
	public String toString() {
		return "ScannableTrackDocument [scannable=" + scannable + ", axis=" + axis + ", start=" + start + ", stop="
				+ stop + ", step=" + step + ", points=" + points + ", continuous=" + continuous + ", alternating="
				+ alternating + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(alternating, axis, continuous, points, scannable, start, step, stop);
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
		return alternating == other.alternating && axis == other.axis && continuous == other.continuous
				&& points == other.points && Objects.equals(scannable, other.scannable)
				&& Double.doubleToLongBits(start) == Double.doubleToLongBits(other.start)
				&& Double.doubleToLongBits(step) == Double.doubleToLongBits(other.step)
				&& Double.doubleToLongBits(stop) == Double.doubleToLongBits(other.stop);
	}


}
