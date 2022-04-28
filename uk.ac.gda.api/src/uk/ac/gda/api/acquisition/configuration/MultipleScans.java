package uk.ac.gda.api.acquisition.configuration;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Defines the configuration in case of multiple scans
 *
 * @author Maurizio Nagni
 */
@JsonDeserialize(builder = MultipleScans.Builder.class)
public class MultipleScans {

	private MultipleScansType multipleScansType;
	/**
	 * The number of time scan has to be repeated
	 */
	private int numberRepetitions;
	/**
	 * The time, in milliseconds, to wait between each scan
	 */
	private int waitingTime;

	public MultipleScans(MultipleScansType multipleScansType, int numberRepetitions, int waitingTime) {
		this.multipleScansType = multipleScansType;
		this.numberRepetitions = numberRepetitions;
		this.waitingTime = waitingTime;
	}

	public int getNumberRepetitions() {
		return numberRepetitions;
	}

	public void setNumberRepetitions(int numberRepetitions) {
		this.numberRepetitions = numberRepetitions;
	}

	public int getWaitingTime() {
		return waitingTime;
	}

	public void setWaitingTime(int waitingTime) {
		this.waitingTime = waitingTime;
	}

	public MultipleScansType getMultipleScansType() {
		return multipleScansType;
	}

	public void setMultipleScansType(MultipleScansType multipleScansType) {
		this.multipleScansType = multipleScansType;
	}

	@JsonPOJOBuilder
	public static class Builder {
		private MultipleScansType multipleScansType;
		/**
		 * The number of time scan has to be repeated
		 */
		private int numberRepetitions;
		/**
		 * The time, in milliseconds, to wait between each scan
		 */
		private int waitingTime;

		public static Builder cloneMultipleScansDocument(MultipleScans multipleScan) {
			Builder builder = new Builder();
			if (Objects.isNull(multipleScan)) {
				return builder;
			}
			builder.withMultipleScansType(multipleScan.multipleScansType);
			builder.withNumberRepetitions(multipleScan.numberRepetitions);
			builder.withWaitingTime(multipleScan.waitingTime);
			return builder;
		}

		public Builder withMultipleScansType(MultipleScansType multipleScansType) {
			this.multipleScansType = multipleScansType;
			return this;
		}

		public Builder withNumberRepetitions(int numberRepetitions) {
			this.numberRepetitions = numberRepetitions;
			return this;
		}

		public Builder withWaitingTime(int waitingTime) {
			this.waitingTime = waitingTime;
			return this;
		}

		public MultipleScans build() {
			return new MultipleScans(multipleScansType, numberRepetitions, waitingTime);
		}
	}
}
