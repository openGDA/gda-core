package uk.ac.diamond.daq.experiment.scans;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.data.metadata.GDAMetadataProvider;
import gda.device.DeviceException;
import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.TriggerableScan;

public class TriggerableScanWrapper {

	private static final Logger logger = LoggerFactory.getLogger(TriggerableScanWrapper.class);

	public void saveScan(TriggerableScan scan, String name) {
		Services.getExperimentService().saveScan(scan, name, getVisit());
	}

	private String getVisit() {
		try {
			return GDAMetadataProvider.getInstance().getMetadataValue(GDAMetadataProvider.EXPERIMENT_IDENTIFIER);
		} catch (DeviceException e) {
			logger.error("Could not determine visit", e);
			return null;
		}
	}

}
