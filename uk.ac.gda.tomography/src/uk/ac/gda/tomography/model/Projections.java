package uk.ac.gda.tomography.model;

/**
 * Defines the number of projections during a scan
 *
 * @author Maurizio Nagni
 */
public class Projections {


	/**
	 *
	 */
	public Projections() {
		super();
	}

	/**
	 * @param totalProjections
	 * @param anglarStep
	 */
	public Projections(int totalProjections, double anglarStep) {
		super();
		this.totalProjections = totalProjections;
		this.anglarStep = anglarStep;
	}

	/**
	 * The absolute total number of projections
	 */
	private int totalProjections;
	/**
	 * The angular step for the scan
	 */
	private double anglarStep;

	public int getTotalProjections() {
		return totalProjections;
	}
	public void setTotalProjections(int totalProjections) {
		this.totalProjections = totalProjections;
	}
	public double getAnglarStep() {
		return anglarStep;
	}
	public void setAnglarStep(double anglarStep) {
		this.anglarStep = anglarStep;
	}
}
