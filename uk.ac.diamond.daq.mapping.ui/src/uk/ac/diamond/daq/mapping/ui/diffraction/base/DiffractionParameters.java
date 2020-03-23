package uk.ac.diamond.daq.mapping.ui.diffraction.base;

import uk.ac.diamond.daq.mapping.api.document.DetectorDocument;
import uk.ac.gda.api.acquisition.AcquisitionParameters;

/**
 * The base class for describe a diffraction acquisition.
 *
 *  @author Maurzio Nagni
 */
public class DiffractionParameters implements AcquisitionParameters {
	private String name;

	private DetectorDocument detector;

	public DiffractionParameters() {
		super();
	}

	public DiffractionParameters(DiffractionParameters configuration) {
		super();
		this.name = configuration.getName();
		this.detector = configuration.getDetector();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DetectorDocument getDetector() {
		return detector;
	}

	public void setDetector(DetectorDocument detector) {
		this.detector = detector;
	}
}
