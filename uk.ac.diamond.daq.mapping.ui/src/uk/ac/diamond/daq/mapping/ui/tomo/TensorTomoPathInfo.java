/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.scanning.api.points.models.AxialArrayModel;
import org.eclipse.scanning.api.points.models.AxialMultiStepModel;
import org.eclipse.scanning.api.points.models.AxialPointsModel;
import org.eclipse.scanning.api.points.models.AxialStepModel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import uk.ac.diamond.daq.mapping.api.document.scanpath.MappingPathInfo;

/**
 * Extends {@link MappingPathInfo} to add information about the two tomography angles.
 */
@JsonDeserialize(builder = TensorTomoPathInfo.Builder.class)
public final class TensorTomoPathInfo extends MappingPathInfo {

	/**
	 * An instance of this class represents the calculated step sizes for the secondary angle
	 * of a tomo scan. The step sizes are doubles, which can be retrieved using a different method
	 * for depending on the rank (as returned by {@link #getRank()}):
	 * <ul>
	 *   <li><b>rank 0:</b> this object is either empty or contains a single scalar step size.
	 *      <ul><li>if {@link #isNone()} returns <code>true</code> then this object contains no step sizes,
	 *          calling {@link #getStepSize()}  or any other method to get the step sizes will throw an {@link IllegalArgumentException}.
	 *          This will be the case when the model for the secondary angle is an {@link AxialArrayModel}</li>
	 *          <li>if {@link #isNone()} returns <code>false</code> then this object contains a single (scalar) step size
	 *          which can be retrieved by calling {@link #getStepSize()}. This will be the case
	 *          when {@link #getStepSizeForIndex(int)} has been called on a {@link StepSizes} object of rank 1 to get the
	 *          step size for a particular index (see below)</li>
	 *      </ul>
	 *   </li>
	 *   <li><b>rank 1:</b> this object represents a one dimensional array of step sizes, which can be
	 *        retrieved by calling {@link #getOneDStepSizes()}. This will be the case when the model for the
	 *        secondary angle is a {@link AxialPointsModel} or {@link AxialStepModel};
	 *   </li>
	 *   <li><b>rank 2:</b> this object represents a two dimensional array of step sizes, which can be
	 *        retrieved by calling {@link #getTwoDStepSizes()}. This will be the case when the model for the
	 *        secondary angle is a {@link AxialMultiStepModel}.
	 *   </li>
	 * </ul>
	 *
	 * Calling {@link #getStepSizeForIndex(int)} returns a {@link StepSizes} object representing the step size(s)
	 * for a particular secondary angle position index.
	 */
	public static class StepSizes {

		private static final StepSizes NONE = new StepSizes(0, null);

		/**
		 * The step sizes, one of a Double, double[] or double[][].
		 */
		private final Object stepSizesObj;

		/**
		 * The rank of the step sizes object in this class, i.e. scalar, 1d or 2d.
		 */
		private final int rank;

		private StepSizes(int rank, Object stepSizesObj) {
			this.rank = rank;
			this.stepSizesObj = stepSizesObj;
		}

		public double getStepSize() {
			if (rank != 0) throw new IllegalArgumentException("Rank must be 0");
			return (double) stepSizesObj;
		}

		public double[] getOneDStepSizes() {
			if (rank != 1) throw new IllegalArgumentException("Rank must be 1");
			return (double[]) stepSizesObj;
		}

		public double[][] getTwoDStepSizes() {
			if (rank != 2) throw new IllegalArgumentException("Rank must be 2");
			return (double[][]) stepSizesObj;
		}

		public StepSizes getStepSizeForIndex(int angle1PosIndex) {
			if (rank < 1) throw new IllegalStateException("Cannot get step(s) for index, as rank is less than 1");
			return new StepSizes(rank - 1, Array.get(stepSizesObj, angle1PosIndex));
		}

		public int getRank() {
			return rank;
		}

		public int getLength() {
			if (rank == 0) return 0;
			return Array.getLength(stepSizesObj);
		}

		public boolean isNone() {
			return stepSizesObj == null;
		}

		@Override
		public String toString() {
			if (isNone()) return "<No steps>";
			return switch (rank) {
				case 0 -> "scalar value: " + ((Double) stepSizesObj).toString();
				case 1 -> "1d array: " + Arrays.toString(getOneDStepSizes());
				case 2 -> "2d array: " + Arrays.deepToString(getTwoDStepSizes());
				default -> throw new IllegalStateException("Rank greater than 2 is not supported");
			};
		}

		public static StepSizes none() {
			return NONE;
		}

		public static StepSizes forSingleStepSizes(double[] stepSizes) {
			return new StepSizes(1, stepSizes);
		}

		public static StepSizes forMultiStepSizes(double[][] multiStepSizes) {
			return new StepSizes(2, multiStepSizes);
		}

		@Override
		public int hashCode() {
			return Objects.hash(rank, stepSizesObj);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StepSizes other = (StepSizes) obj;
			return rank == other.rank && Objects.equals(stepSizesObj, other.stepSizesObj);
		}

	}

	/**
	 * The positions of the primary angle.
	 */
	private final double[] angle1Positions;

	/**
	 * The positions of the secondary angle for the primary angle with the corresponding index.
	 */
	private final double[][] angle2Positions;

	/**
	 * The step sizes for the secondary angle, a {@link StepSizes} object, which is either empty
	 * (in the case of {@link AxialArrayModel}), a one-dimensional array, or a two-dimensional
	 * array (in the case of {@link AxialMultiStepModel}. The element (or array) at each index
	 * of the array represents the step size(s) for the angle1 position at the same index.
	 */
	private final StepSizes angle2StepSizes;

	private TensorTomoPathInfo(UUID eventId, String sourceId, int innerPointCount, int totalPointCount,
			double smallestXStep, double smallestYStep, double smallestAbsStep,
			double[] xCoordinates, double[] yCoordinates,
			double[] angle1Positions, double[][] angle2Positions, StepSizes angle2StepSizes) {
		super(eventId, sourceId, innerPointCount, totalPointCount, smallestXStep, smallestYStep, smallestAbsStep, xCoordinates,
				yCoordinates);
		this.angle1Positions = angle1Positions;
		this.angle2Positions = angle2Positions;
		this.angle2StepSizes = angle2StepSizes;
	}

	public double[] getAngle1Positions() {
		return angle1Positions;
	}

	public double[][] getAngle2Positions() {
		return angle2Positions;
	}

	public StepSizes getAngle2StepSizes() {
		return angle2StepSizes;
	}

	@Override
	public String toString() {
		return "TensorTomoPathInfo [angle1Positions=" + Arrays.toString(angle1Positions)
				+ ", angle2Positions=" + Arrays.deepToString(angle2Positions)
				+ ", angle2StepSizes=" + angle2StepSizes
				+ ", " + super.toString() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(angle1Positions);
		result = prime * result + Arrays.deepHashCode(angle2Positions);
		result = prime * result + Objects.hash(angle2StepSizes);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TensorTomoPathInfo other = (TensorTomoPathInfo) obj;
		return Arrays.equals(angle1Positions, other.angle1Positions)
				&& Arrays.deepEquals(angle2Positions, other.angle2Positions)
				&& Objects.equals(angle2StepSizes, other.angle2StepSizes);
	}

	@SuppressWarnings("unchecked")
	public static Builder builder() {
		// note, this method cannot be have a generic type argument as it would still have type
		// same type erasure from the super-class method, despite no longer hiding it
		return new Builder();
	}

	@JsonPOJOBuilder
	public static final class Builder extends MappingPathInfo.Builder<Builder> {
		private double[] angle1Positions;
		private double[][] angle2Positions;
		private StepSizes angle2StepSizes;

		public Builder withAngle1Positions(double[] angle1Positions) {
			this.angle1Positions = angle1Positions;
			return this;
		}

		public Builder withAngle2Positions(double[][] angle2Positions) {
			this.angle2Positions = angle2Positions;
			return this;
		}

		public Builder withAngle2StepSizes(StepSizes angle2StepSizes) {
			this.angle2StepSizes = angle2StepSizes;
			return this;
		}

		@Override
		public TensorTomoPathInfo build() {
			return new TensorTomoPathInfo(
					eventId,
					sourceId,
					innerPointCount,
					outerPointCount,
					smallestXStep,
					smallestYStep,
					smallestAbsStep,
					Objects.requireNonNullElse(xCoordinates, new double[] {}),
					Objects.requireNonNullElse(yCoordinates, new double[] {}),
					angle1Positions,
					angle2Positions,
					angle2StepSizes);
		}

	}

}