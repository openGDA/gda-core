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
 * Describes a {@code Scannable} essential information to start an acquisition. A {@code Scannable} is described as a component which
 * can modify one parameter in a well defined interval either by {@code step} or continuously.
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
	 * The interval starting point
	 */
	private final double start;
	/**
	 * The interval ending point
	 */
	private final double stop;
	/**
	 * The required step from {@code start} to {@code end}
	 */
	private final double step;

	/**
	 * @param scannable the scannable identifier
	 * @param start the start point
	 * @param stop the end point
	 * @param step the required step
	 */
	public ScannableTrackDocument(String scannable, double start, double stop, double step) {
		super();
		this.scannable = scannable;
		this.start = start;
		this.stop = stop;
		this.step = step;
	}

	public String getScannable() {
		return scannable;
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

	@JsonIgnore
	public int getAxisPoints() {
		if (getStep() == 0.0) {
			return 1;
		}
		long d_steps = Math.round(Math.abs(((getStart() - getStop()) / getStep())));
		double i_range = d_steps * getStep();
		return i_range > getStop() ? (int) d_steps - 1 : (int) d_steps;
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
		return Finder.getInstance().findOptional(getScannable());
	}

	@JsonPOJOBuilder
	public static class Builder {
		private String scannable;
		private double start;
		private double stop;
		private double step;

		Builder withScannable(String scannable) {
			this.scannable = scannable;
			return this;
		}

		Builder withStart(double start) {
			this.start = start;
			return this;
		}

		Builder withStop(double stop) {
			this.stop = stop;
			return this;
		}

		Builder withStep(double step) {
			this.step = step;
			return this;
		}

		public ScannableTrackDocument build() {
			return new ScannableTrackDocument(scannable, start, stop, step);
		}
	}
}
