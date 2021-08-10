package uk.ac.diamond.daq.mapping.api.document.scanning;

import java.util.HashSet;
import java.util.Set;

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.parameters.AcquisitionParameters;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;

/**
 * The base class for describe a scanning acquisition.
 *
 * @author Maurzio Nagni
 */
public class ScanningParameters implements AcquisitionParametersBase {

	private DetectorDocument detector;

	private ScanpathDocument scanpathDocument;

	/**
	 * Defines the acquisition starting position. It could be extracted by the {@link ScanpathDocument#getScannableTrackDocuments()}
	 * but there are cases where motors others than the one involved in the scan should position the sample.
	 * It is assumed that any engine can move the sample where {@link ScanpathDocument#getScannableTrackDocuments()}  command,
	 * consequently the motors here may or may not include those last ones.
	 * @see AcquisitionParameters#getStartPosition()
	 */
	private Set<DevicePositionDocument> position = new HashSet<>();

	public ScanningParameters() {
		super();
	}

	public ScanningParameters(ScanningParameters configuration) {
		super();
		this.detector = configuration.getDetector();

		this.scanpathDocument = configuration.getScanpathDocument();
		this.position = configuration.getStartPosition();
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

	@Override
	public Set<DevicePositionDocument> getStartPosition() {
		return position;
	}

	public void setStartPosition(Set<DevicePositionDocument> position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return "ScanningParameters [detector=" + detector + ", scanpathDocument="
				+ scanpathDocument + ", position=" + position + "]";
	}
}
