package uk.ac.diamond.daq.experiment.scans.mapping;

import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.diamond.daq.experiment.scans.TriggerableScanWrapper;
import uk.ac.diamond.daq.mapping.api.ScanRequestSavedEvent;

public class MapSaveListener extends TriggerableScanWrapper implements ApplicationListener<ScanRequestSavedEvent> {

	@Override
	public void onApplicationEvent(ScanRequestSavedEvent event) {
		TriggerableScan scan = new TriggerableMap(event.getScanRequest(), false);
		saveScan(scan, event.getScanName());
	}

}
