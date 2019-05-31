package uk.ac.diamond.daq.experiment.api.driver;

import java.util.Collections;
import java.util.List;

import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public class SingleAxisLinearSeries extends DriverModelBase {

	private static final long serialVersionUID = -834737476722369747L;

	private List<DriverProfileSection> profile;

	public SingleAxisLinearSeries() {
		profile = Collections.emptyList();
	}

	public List<DriverProfileSection> getProfile() {
		return profile;
	}

	public void setProfile(List<DriverProfileSection> series) {
		this.profile = series;
	}

	@Override
	public EditableWithListWidget createDefault() {
		DriverModel model = new SingleAxisLinearSeries();
		model.setName("New Profile");
		return model;
	}

}