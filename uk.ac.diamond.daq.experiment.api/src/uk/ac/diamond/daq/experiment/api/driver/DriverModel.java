package uk.ac.diamond.daq.experiment.api.driver;

import java.io.Serializable;
import java.util.List;

import org.eclipse.january.dataset.Dataset;

import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;

public interface DriverModel extends Findable, Serializable, EditableWithListWidget {

	/**
	 * Each dataset should be named appropriately, especially if the driver which
	 * runs this model treats this variable as a readout as well.
	 */
	List<Dataset> getPlottableDatasets();

}
