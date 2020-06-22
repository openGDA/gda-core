package uk.ac.gda.tomography.base;

import uk.ac.diamond.daq.mapping.api.document.DetectorDocument;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.diffraction.ShapeType;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.tomography.model.ScanType;

/**
 * The base class for describe a tomography acquisition.
 *
 *  @author Maurzio Nagni
 */
public class TomographyParameters implements AcquisitionParametersBase {

	private ScanType scanType;

	private ShapeType shapeType;

	private DetectorDocument detector;

	private ScanpathDocument scanpathDocument;

	public TomographyParameters() {
		super();
	}

	public TomographyParameters(TomographyParameters configuration) {
		super();
		this.scanType = configuration.getScanType();
		this.detector = configuration.getDetector();
		this.scanpathDocument = configuration.getScanpathDocument();
	}

	public ScanType getScanType() {
		return scanType;
	}

	public void setScanType(ScanType scanType) {
		this.scanType = scanType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	@Override
	public ShapeType getShapeType() {
		return shapeType;
	}

	@Override
	public DetectorDocument getDetector() {
		return detector;
	}

	public void setDetector(DetectorDocument detector) {
		this.detector = detector;
	}

	@Override
	public ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}

	public void setScanpathDocument(ScanpathDocument scanpathDocument) {
		this.scanpathDocument = scanpathDocument;
	}
}