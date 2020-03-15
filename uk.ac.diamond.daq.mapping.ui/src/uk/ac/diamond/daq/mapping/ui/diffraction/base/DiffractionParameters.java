package uk.ac.diamond.daq.mapping.ui.diffraction.base;

import uk.ac.gda.api.acquisition.AcquisitionParameters;

/**
 * The base class for describe a diffraction acquisition.
 *
 *  @author Maurzio Nagni
 */
public class DiffractionParameters implements AcquisitionParameters {
	private String name;

	public DiffractionParameters() {
		super();
	}

	public DiffractionParameters(DiffractionParameters configuration) {
		super();
		this.name = configuration.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
