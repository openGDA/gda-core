package uk.ac.diamond.daq.experiment.plan;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.experiment.api.plan.Payload;
import uk.ac.diamond.daq.experiment.api.plan.PayloadHandler;
import uk.ac.diamond.daq.experiment.api.plan.PayloadService;

@Service
public class SpringPayloadService implements PayloadService  {
	
	@Autowired
	private List<PayloadHandler<? extends Payload>> handlers;

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Payload> T wrap(Object rawPayload) {
		return (T) handlers.stream()
				.filter(handler -> handler.getSourceClass().equals(rawPayload.getClass()))
				.map(handler -> (PayloadHandler<? extends Payload>) handler)
				.findAny().orElseThrow().wrap(rawPayload);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Payload> Object handle(T payload) {
		return handlers.stream()
				.filter(handler -> handler.getTargetClass().equals(payload.getClass()))
				.map(handler -> (PayloadHandler<T>) handler)
				.findAny().orElseThrow().handle(payload);
	}

}
