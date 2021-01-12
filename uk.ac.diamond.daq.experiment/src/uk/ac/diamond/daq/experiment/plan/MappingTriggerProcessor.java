package uk.ac.diamond.daq.experiment.plan;

import java.net.URL;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.IdBean;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.scan.ScanningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.plan.Triggerable;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.scans.mapping.QueuePreventingScanSubmitter;
import uk.ac.diamond.daq.experiment.scans.mapping.TriggerableMap;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

public class MappingTriggerProcessor implements TriggerProcessor {
	
	private static final Logger logger = LoggerFactory.getLogger(MappingTriggerProcessor.class);
	
	private QueuePreventingScanSubmitter scanSubmitter;
	private IEventService eventService;
	
	public MappingTriggerProcessor() {
		scanSubmitter = new QueuePreventingScanSubmitter();
		scanSubmitter.setEventService(getEventService());
	}

	@Override
	public IdBean process(Triggerable triggerable) {
		TriggerableMap map = (TriggerableMap) triggerable;
		ScanBean scanBean = map.trigger();
		
		try {
			URL url = SpringApplicationContextFacade.getBean(ExperimentController.class).prepareAcquisition(map.getName());
			scanBean.getScanRequest().setFilePath(url.getFile());
		} catch (ExperimentControllerException e) {
			logger.error("Error getting URL for triggered scan - data will not reflect experiment structure", e);
		}
		
		try {
			if (map.isImportant()) {
				scanSubmitter.submitImportantScan(scanBean);
			} else {
				scanSubmitter.submitScan(scanBean);
			}
		} catch (ScanningException | EventException e) {
			logger.error("Could not submit scan", e);
		}
		
		return scanBean;
	}
	
	private IEventService getEventService() {
		if (eventService == null) {
			eventService = Activator.getService(IEventService.class);
		}
		return eventService;
	}

}
