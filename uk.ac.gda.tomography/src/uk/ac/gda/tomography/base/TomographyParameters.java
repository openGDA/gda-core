package uk.ac.gda.tomography.base;

import uk.ac.gda.tomography.model.AcquisitionParameters;
import uk.ac.gda.tomography.model.EndAngle;
import uk.ac.gda.tomography.model.ImageCalibration;
import uk.ac.gda.tomography.model.MultipleScans;
import uk.ac.gda.tomography.model.Projections;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.StartAngle;

/**
 * The base class for describe a tomography acquisition.
 *
 *  @author Maurzio Nagni
 */
public class TomographyParameters implements AcquisitionParameters {
	private String name;

	private ScanType scanType;
	private StartAngle start;
	private EndAngle end;

	private Projections projections;

	private ImageCalibration imageCalibration;

	private MultipleScans multipleScans;

	public TomographyParameters() {
		super();
	}

	public TomographyParameters(TomographyParameters configuration) {
		super();
		this.name = configuration.getName();
		this.scanType = configuration.getScanType();
		this.start = new StartAngle(configuration.getStart());
		this.end = new EndAngle(configuration.getEnd());
		this.projections = new Projections(configuration.getProjections());
		this.imageCalibration = new ImageCalibration(configuration.getImageCalibration());
		this.multipleScans = new MultipleScans(configuration.getMultipleScans());
	}

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
