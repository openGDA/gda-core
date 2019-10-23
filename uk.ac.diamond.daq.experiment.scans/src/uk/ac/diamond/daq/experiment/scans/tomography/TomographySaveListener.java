package uk.ac.diamond.daq.experiment.scans.tomography;

import org.springframework.context.ApplicationListener;

import uk.ac.diamond.daq.experiment.api.TriggerableScan;
import uk.ac.diamond.daq.experiment.scans.TriggerableScanWrapper;
import uk.ac.gda.tomography.event.TomographySaveEvent;

public class TomographySaveListener extends TriggerableScanWrapper implements ApplicationListener<TomographySaveEvent> {

	@Override
	public void onApplicationEvent(TomographySaveEvent event) {
		TriggerableScan scan = new TriggerableTomography(event.getAcquisitionConfiguration(), event.getScriptPath());
		saveScan(scan, event.getName());
	}

}
