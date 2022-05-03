package uk.ac.diamond.daq.mapping.api.document.scanning;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

	private List<DetectorDocument> detectors;

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
		this.detectors = configuration.getDetectors();

		this.scanpathDocument = configuration.getScanpathDocument();
		this.position = configuration.getStartPosition();
	}

	@Override
	public List<DetectorDocument> getDetectors() {
		return detectors;
	}

	/**
	 * Adds the given document to the list of detectors.
	 * If an existing document has a name equal to the given document, the old one is replaced.
	 */
	public void setDetector(DetectorDocument detector) {
		if (detectors == null) detectors = new ArrayList<>();
		detectors.stream().filter(doc -> doc.getId().equals(detector.getId())).findFirst().ifPresent(detectors::remove);
		detectors.add(detector);
	}

	public void setDetectors(List<DetectorDocument> detectors) {
		this.detectors = detectors;
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
		return "ScanningParameters [detector=" + detectors + ", scanpathDocument="
				+ scanpathDocument + ", position=" + position + "]";
	}
}
