package uk.ac.diamond.daq.experiment.scans;

import gda.data.metadata.GDAMetadataProvider;
import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.TriggerableScan;

public class TriggerableScanWrapper {

	public void saveScan(TriggerableScan scan, String name) {
		Finder.findSingleton(ExperimentService.class).saveScan(scan, name, getVisit());
	}

	private String getVisit() {
		return GDAMetadataProvider.getInstance().getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER);
	}

}
