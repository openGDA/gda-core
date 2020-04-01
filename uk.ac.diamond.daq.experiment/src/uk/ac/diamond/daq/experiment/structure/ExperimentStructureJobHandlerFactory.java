package uk.ac.diamond.daq.experiment.structure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentStructureJobRequest;
import uk.ac.diamond.daq.experiment.api.structure.IndexFileCreationRequest;

/**
 * Each concrete implementation of {@link ExperimentStructureJobRequest} must be mapped
 * to a {@link ExperimentStructureJobHandler} in this class.
 */
public class ExperimentStructureJobHandlerFactory {
	
	private static final Map<Class<? extends ExperimentStructureJobRequest>, Class<? extends ExperimentStructureJobHandler<? extends ExperimentStructureJobRequest>>> REQUEST_HANDLERS;
	
	static {
		Map<Class<? extends ExperimentStructureJobRequest>, Class<? extends ExperimentStructureJobHandler<? extends ExperimentStructureJobRequest>>> handlers = new HashMap<>();
		handlers.put(IndexFileCreationRequest.class, IndexFileCreator.class);
		
		
		REQUEST_HANDLERS = Collections.unmodifiableMap(handlers);
	}
	
	/**
	 * Gets the appropriate handler for the given request.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ExperimentStructureJobRequest> ExperimentStructureJobHandler<T> getHandler(T request) throws InstantiationException, IllegalAccessException {
		return (ExperimentStructureJobHandler<T>) REQUEST_HANDLERS.get(request.getClass()).newInstance();
	}

}
