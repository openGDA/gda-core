package uk.ac.diamond.daq.experiment.ui.driver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;

public interface ProfileEditor {
	
	void createControl(Composite parent);

	default List<DriverProfileSection> getProfile() {
		return new ArrayList<>();
	}

}
