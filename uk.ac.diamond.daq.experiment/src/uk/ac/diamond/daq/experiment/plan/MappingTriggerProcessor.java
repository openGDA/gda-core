package uk.ac.diamond.daq.experiment.plan;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.experiment.api.Activator;
import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.experiment.scans.mapping.QueuePreventingScanSubmitter;
import uk.ac.diamond.daq.experiment.scans.mapping.TriggerableMap;

@Component
public class MappingTriggerProcessor implements PayloadHandler<TriggerableMap> {
	
	private static final Logger logger = LoggerFactory.getLogger(MappingTriggerProcessor.class);
	
	private QueuePreventingScanSubmitter scanSubmitter;
	private IEventService eventService;
	
	@Autowired
	private ExperimentController experimentController;

	@Override
	public Class<?> getSourceClass() {
		return ScanRequest.class;
	}

	@Override
	public Class<TriggerableMap> getTargetClass() {
		return TriggerableMap.class;
	}

	@Override
	public TriggerableMap wrap(Object rawPayload) {
		var scanRequest = (ScanRequest) rawPayload;
		return new TriggerableMap(getName(scanRequest), scanRequest, false);
	}
	
	private String getName(ScanRequest scanRequest) {
		return scanRequest.getScanMetadata().stream()
			.filter(metadata -> metadata.getType().equals(MetadataType.SAMPLE))
			.map(metadata -> metadata.getFieldValue("name"))
			.map(String.class::cast)
			.findAny().orElse("Untitled acquisition");
	}

	@Override
	public Object handle(TriggerableMap payload) {
		
		var request = payload.getScanRequest();
		
		try {
			var url = experimentController.prepareAcquisition(payload.getName());
			request.setFilePath(url.getFile());
		} catch (ExperimentControllerException e) {
			logger.error("Error getting URL for triggered scan - data will not reflect experiment structure", e);
		}
		
		var scanBean = new ScanBean(payload.getScanRequest());
		
		try {
			if (payload.isImportant()) {
				getSubmitter().submitImportantScan(scanBean);
			} else {
				getSubmitter().submitScan(scanBean);
			}
		} catch (ScanningException | EventException e) {
			logger.error("Could not submit scan", e);
		}
		
		return scanBean;
	}
	
	private QueuePreventingScanSubmitter getSubmitter() {
		if (scanSubmitter == null) {
			scanSubmitter = new QueuePreventingScanSubmitter();
			scanSubmitter.setEventService(getEventService());
		}
		return scanSubmitter;
	}
	
	private IEventService getEventService() {
		if (eventService == null) {
			eventService = Activator.getService(IEventService.class);
		}
		return eventService;
	}

}
