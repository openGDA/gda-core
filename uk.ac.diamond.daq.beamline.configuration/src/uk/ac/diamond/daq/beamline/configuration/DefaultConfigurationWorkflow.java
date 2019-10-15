package uk.ac.diamond.daq.beamline.configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.FindableBase;
import gda.observable.IObserver;
import gda.observable.ObservableComponent;
import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowUpdate;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowStatus;
import uk.ac.gda.api.remoting.ServiceInterface;

@ServiceInterface(ConfigurationWorkflow.class)
public class DefaultConfigurationWorkflow extends FindableBase implements ConfigurationWorkflow {

	public static final String INTERRUPTED_MESSAGE = "Interrupted by user";
	public static final String ERROR_WHILE_ABORTING_MESSAGE = "Error aborting workflow: ";

	private static final Logger logger = LoggerFactory.getLogger(DefaultConfigurationWorkflow.class);

	private WorkflowStatus status = WorkflowStatus.IDLE;
	private String eventMessage;
	private double percentageCompletion;

	private final ObservableComponent observableComponent = new ObservableComponent();

	private volatile boolean aborting;

	private Map<WorkflowItem, ProgressInformation> itemsWithContext;

	public DefaultConfigurationWorkflow(Map<WorkflowItem, ProgressInformation> workflowItems) {
		this.itemsWithContext = new LinkedHashMap<>(workflowItems);
	}

	@Override
	public void start(Properties workflowProperties) {
		percentageCompletion = 0;
		try {
			status = WorkflowStatus.RUNNING;
			for (Map.Entry<WorkflowItem, ProgressInformation> item : itemsWithContext.entrySet()) {
				checkForAbort();
				broadcast(item.getValue().getDescription());
				item.getKey().start(workflowProperties);
				checkForAbort();
				percentageCompletion = item.getValue().getPercentage();
			}
			status = WorkflowStatus.IDLE;
			broadcast("Workflow complete");
		} catch (WorkflowException e) {
			logger.error("Error running workflow", e);
			status = WorkflowStatus.FAULT;
			broadcast(e.getMessage());
		} catch (InterruptedException e) { // NOSONAR Only used internally to signal abort
			logger.warn(INTERRUPTED_MESSAGE);
			status = WorkflowStatus.INTERRUPTED;
			broadcast(INTERRUPTED_MESSAGE);
		}
	}

	private void broadcast(String message) {
		eventMessage = message;
		broadcastEvent();
	}

	private void broadcastEvent() {
		observableComponent.notifyIObservers(this, getState());
	}

	@Override
	public WorkflowUpdate getState() {
		return new WorkflowUpdate(status, eventMessage, percentageCompletion);
	}

	/**
	 * throws InterruptedException when {@link #aborting}
	 */
	private void checkForAbort() throws InterruptedException {
		if (aborting) {
			aborting = false;
			throw new InterruptedException(INTERRUPTED_MESSAGE);
		}
	}

	@Override
	public void abort() {
		aborting = true;
		try {
			for (WorkflowItem item : itemsWithContext.keySet()) {
				item.abort();
			}
		} catch (WorkflowException e) {
			logger.error(ERROR_WHILE_ABORTING_MESSAGE, e);
			status = WorkflowStatus.FAULT;
			broadcast(ERROR_WHILE_ABORTING_MESSAGE + e.getMessage());
		}
	}

	@Override
	public void addIObserver(IObserver observer) {
		observableComponent.addIObserver(observer);
	}

	@Override
	public void deleteIObserver(IObserver observer) {
		observableComponent.deleteIObserver(observer);
	}

	@Override
	public void deleteIObservers() {
		observableComponent.deleteIObservers();
	}
}
