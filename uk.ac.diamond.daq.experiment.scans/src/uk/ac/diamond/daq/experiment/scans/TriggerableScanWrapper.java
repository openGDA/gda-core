package uk.ac.diamond.daq.experiment.scans;

import gda.data.metadata.GDAMetadataProvider;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.diamond.osgi.services.ServiceProvider;

public class TriggerableScanWrapper {

	public void saveScan(TriggerableScan scan, String name) {
		ServiceProvider.getService(ExperimentService.class).saveScan(scan, name, getVisit());
	}

	private String getVisit() {
		return GDAMetadataProvider.getInstance().getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER);
	}

}
