package uk.ac.gda.tomography.model;

/**
 * The base class for tomography scan parameter data.
 *
 *  @author Maurzio Nagni
 */
public class TomographyScanParameters implements ITomographyScanParameters {
	private String name;

	private ScanType scanType;
	private StartAngle start;
	private EndAngle end;

	private Projections projections;

	private ImageCalibration imageCalibration;

	private MultipleScans multipleScans;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ScanType getScanType() {
		return scanType;
	}

	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}

	public StartAngle getStart() {
		return start;
	}

	public void setStart(StartAngle start) {
		this.start = start;
	}

	public EndAngle getEnd() {
		return end;
	}

	public void setEnd(EndAngle end) {
		this.end = end;
	}

	public Projections getProjections() {
		return projections;
	}

	public void setProjections(Projections projections) {
		this.projections = projections;
	}

	public ImageCalibration getImageCalibration() {
		return imageCalibration;
	}

	public void setImageCalibration(ImageCalibration imageCalibration) {
		this.imageCalibration = imageCalibration;
	}

	public MultipleScans getMultipleScans() {
		return multipleScans;
	}

	public void setMultipleScans(MultipleScans multipleScans) {
		this.multipleScans = multipleScans;
	}
}
