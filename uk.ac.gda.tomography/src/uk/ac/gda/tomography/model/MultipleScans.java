package uk.ac.gda.tomography.model;

/**
 * Defines the configuration in case of multiple scans
 *
 * @author Maurizio Nagni
 */
public class MultipleScans {

	private MultipleScansType multipleScansType;



	/**
	 *
	 */
	public MultipleScans() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param multipleScansType
	 * @param numberRepetitions
	 * @param waitingTime
	 */
	public MultipleScans(MultipleScansType multipleScansType, int numberRepetitions, int waitingTime) {
		super();
		this.multipleScansType = multipleScansType;
		this.numberRepetitions = numberRepetitions;
		this.waitingTime = waitingTime;
	}

	/**
	 * The number of time scan has to be repeated
	 */
	private int numberRepetitions;
	/**
	 * The time, in milliseconds, to wait between each scan
	 */
	private int waitingTime;

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
}
