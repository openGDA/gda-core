package uk.ac.diamond.daq.experiment.scans;

import gda.data.metadata.GDAMetadataProvider;
import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.TriggerableScan;

public class TriggerableScanWrapper {

	public void saveScan(TriggerableScan scan, String name) {
		Services.getExperimentService().saveScan(scan, name, getVisit());
	}

	private String getVisit() {
		return GDAMetadataProvider.getInstance().getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER);
	}

}
