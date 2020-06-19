package uk.ac.diamond.daq.mapping.ui.diffraction.base;

import uk.ac.diamond.daq.mapping.api.document.DetectorDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.diffraction.model.ShapeType;
import uk.ac.gda.api.acquisition.AcquisitionParameters;

/**
 * The base class for describe a diffraction acquisition.
 *
 * @author Maurzio Nagni
 */
public class DiffractionParameters implements AcquisitionParameters {

	private ShapeType shapeType;

	private DetectorDocument detector;

	private ScanpathDocument scanpathDocument;

	public DiffractionParameters() {
		super();
	}

	public DiffractionParameters(DiffractionParameters configuration) {
		super();
		this.shapeType = configuration.getShapeType();
		this.detector = configuration.getDetector();

		this.scanpathDocument = configuration.getScanpathDocument();
	}

	public ShapeType getShapeType() {
		return shapeType;
	}

	public void setShapeType(ShapeType shapeType) {
		this.shapeType = shapeType;
	}

	public DetectorDocument getDetector() {
		return detector;
	}

	public void setDetector(DetectorDocument detector) {
		this.detector = detector;
	}

	public ScanpathDocument getScanpathDocument() {
		return scanpathDocument;
	}

	public void setScanpathDocument(ScanpathDocument scanpathDocument) {
		this.scanpathDocument = scanpathDocument;
	}

	@Override
	public String toString() {
		return "DiffractionParameters [" + ", shapeType=" + shapeType + ", detector=" + detector
				+ ", scanpathDocument=" + scanpathDocument + "]";
	}
}
