package uk.ac.gda.tomography.model;

/**
 * Defines the number of projections during a scan
 *
 * @author Maurizio Nagni
 */
public class Projections {

	/**
	 * The exposure time expressed in miliseconds
	 */
	private double acquisitionExposure;

	/**
	 * The absolute total number of projections
	 */
	private int totalProjections;
	/**
	 * The angular step for the scan
	 */
	private double angularStep;

	/**
	 *
	 */
	public Projections() {
		super();
	}

	/**
	 * @param totalProjections
	 * @param angularStep
	 */
	public Projections(int totalProjections, double angularStep) {
		super();
		this.totalProjections = totalProjections;
		this.angularStep = angularStep;
	}

	public Projections(Projections projections) {
		this(projections.getTotalProjections(), projections.getAngularStep());
	}

	/**
	 * @return the projection exposure time in milliseconds
	 */
	public double getAcquisitionExposure() {
		return acquisitionExposure;
	}

	public void setAcquisitionExposure(double acquisitionExposure) {
		this.acquisitionExposure = acquisitionExposure;
	}

	public int getTotalProjections() {
		return totalProjections;
	}

	public void setTotalProjections(int totalProjections) {
		this.totalProjections = totalProjections;
	}

	public double getAngularStep() {
		return angularStep;
	}

	public void setAngularStep(double angularStep) {
		this.angularStep = angularStep;
	}
}
