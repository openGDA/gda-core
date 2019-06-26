package uk.ac.diamond.daq.experiment.api.driver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

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

	@Override
	public List<Dataset> getPlottableDatasets() {
		double[] x = new double[profile.size()+1];
		double[] y = new double[profile.size()+1];

		x[0] = 0;
		y[0] = profile.get(0).getStart();

		for (int i = 0; i < profile.size(); i++) {
			x[i+1] = profile.get(i).getDuration() + x[i];
			y[i+1] = profile.get(i).getStop();
		}

		final Dataset xDataset = DatasetFactory.createFromObject(x);
		xDataset.setName("Time");
		final Dataset yDataset = DatasetFactory.createFromObject(y);
		yDataset.setName("Load"); // FIXME obviously this should come from elsewhere

		return Arrays.asList(xDataset, yDataset);
	}

}