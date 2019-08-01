package uk.ac.gda.tomography.model;

/**
 * Defines the configuration in case of multiple scans
 *
 * @author Maurizio Nagni
 */
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

	/**
	 * Explicitly enable this configuration. In other words, avoids to set {@link #numberRepetitions} to 0 in order to disable this configuration.
	 */
	private boolean enabled;

	/**
	 *
	 */
	public MultipleScans() {
		super();
	}

	public MultipleScans(MultipleScans multipleScans) {
		super();
		this.multipleScansType = multipleScans.getMultipleScansType();
		this.numberRepetitions = multipleScans.getNumberRepetitions();
		this.waitingTime = multipleScans.getWaitingTime();
		this.enabled = multipleScans.isEnabled();
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

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
