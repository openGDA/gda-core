/*-
 * Copyright © 2024 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.gda.core.sampletransfer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.bean.IBeanListener;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.core.ISubscriber;
import org.eclipse.scanning.api.event.status.Status;
import org.eclipse.scanning.api.ui.CommandConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.osgi.services.ServiceProvider;

public class SampleTransfer {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransfer.class);

	private URI uri;
	private IPublisher<StepBean> publisher;
	private ISubscriber<IBeanListener<SequenceMessage>> subscriber;

	private ExecutorService executorService;
	private Future<?> currentTask;

	private Status status = Status.NONE;
	private Step currentStep;
	private Sequence currentSequence;
	private StepProperties properties;

	private List<Sequence> sequences;

	public SampleTransfer(List<Sequence> sequences) {
		this.sequences = sequences;
		initialiseProperties();
		initialiseExecutorService();
		connect();
	}

	private void initialiseProperties() {
		properties = new StepProperties();
	}

	private void initialiseExecutorService() {
		executorService = Executors.newSingleThreadExecutor();
	}

	private void connect() {
		var eventService = ServiceProvider.getService(IEventService.class);
		try {
			uri = new URI(CommandConstants.getScanningBrokerUri());
		} catch (URISyntaxException e) {
			logger.error("Could not create URI", e);
		}
		publisher = eventService.createPublisher(uri, EventConstants.SAMPLE_TRANSFER_SERVER_TOPIC);
		subscriber = eventService.createSubscriber(uri, EventConstants.SAMPLE_TRANSFER_CMD_TOPIC);
		try {
			subscriber.addListener(event -> handleMessage(event.getBean()));
		} catch (EventException e) {
			logger.error("Could not connect to remote event", e);
		}
	}

    private void handleMessage(SequenceMessage message) {
    	properties.setSample(message.sample());

    	var command = message.command();
    	logger.debug("Received command: {}", command);

    	switch(command) {
    		case START -> startSequence(message.sequence());
    		case RESUME -> resumeCurrentStep();
    		case RETRY -> retryCurrentStep();
    		case STOP -> {
    			stopSequence();
    			update(Status.TERMINATED);
    		}
    		default -> logger.warn("Received unknown command: {}", command);
    	}
    }

	private void resumeCurrentStep() {
		if (currentStep != null) currentStep.resume();
	}

    private void retryCurrentStep() {
    	if (currentTask.isDone()) {
        	var index = currentSequence.steps().indexOf(currentStep);
        	executorService.submit(() -> executeSequence(index));
    	}
    }

    /**
     * If there is a matching sequence and another sequence is not running,
     * a sequence execution will be submitted as a task.
     * @param sequenceId
     */
	private void startSequence(SequenceID sequenceId) {
		if (isSequenceRunning()) {
			logger.error("Sequence {} cannot be started. A sequence is already running", sequenceId);
			return;
		}
		var matchingSequence = sequences.stream()
				.filter(s -> s.id().equals(sequenceId))
				.findFirst();

		if (matchingSequence.isPresent()) {
			this.currentSequence = matchingSequence.get();
			submitTask();
		} else {
			var errorMessage = String.format("%s sequence is not found", sequenceId);
			update(Status.FAILED, errorMessage);
		}
	}

	private void submitTask() {
		if (executorService.isShutdown()) initialiseExecutorService();
		logger.info("Submitting task to execute sample transfer sequence");
		currentTask = executorService.submit(() -> executeSequence(0));
	}

	private void executeSequence(int index) {
		logger.info("Starting sequence {} actions", currentSequence);
		var remainingSteps = currentSequence.steps().subList(index, currentSequence.steps().size());
		for (var step: remainingSteps) {
			currentStep = step;

			if (Thread.currentThread().isInterrupted()) {
				handleThreadInterruption(null);
				return;
			}

			// update and broadcast current step information
			update(Status.RUNNING);

			try {
				currentStep.execute(properties);
			} catch (InterruptedException e) {
				handleThreadInterruption(e);
				return;
			} catch (Exception e) {
				handleError(e);
				return;
			}
		}
		update(Status.COMPLETE);
	}

	private void handleThreadInterruption(InterruptedException e) {
		logger.debug("Thread interrupted during step execution, stopping sequence.", e);
		update(Status.TERMINATED);
		Thread.currentThread().interrupt();
	}

	private void handleError(Exception e) {
		terminateAction();
		logger.error(String.format("Error executing step %s, stopping sequence due to %s", currentStep, e.getMessage()));
		update(Status.FAILED, e.getMessage());
	}

	private void terminateAction() {
		logger.debug("Terminating the current action and stopping the sequence.");
		try {
			currentStep.terminate();
			logger.info("Step action was terminated.");
		} catch (Exception e) {
			logger.error("Failed to terminate the current step action.", e);
			update(Status.FAILED, e.toString());
		}
	}

	/**
	 * Attempts to terminate the current step action and the running sequence.
	 * Cancelling the task will cause an InterruptedException if there is a running sequence,
	 * which signals to stop the task and handle the interruption.
	 */
	private void stopSequence() {
		if (currentStep != null) {
			terminateAction();
		}
		if (currentTask != null && !currentTask.isDone()) {
			currentTask.cancel(true);
		}
	}

	private boolean isSequenceRunning() {
		return status.equals(Status.RUNNING) && currentTask != null && !currentTask.isDone();
	}

	private void update(Status status) {
		update(status, "");
	}

	private void update(Status status, String message) {
		this.status = status;
		StepBean stepBean = new StepBean();
		stepBean.setStatus(status);
		stepBean.setMessage(message);
		if (status.equals(Status.RUNNING)) {
			stepBean.setDescription(currentStep.getDescription());
			stepBean.setClientAction(currentStep instanceof ClientAction);
		}
		broadcast(stepBean);
	}

	private void broadcast(StepBean stepBean) {
		logger.info("Broadcasting message about step: {} with status {}", stepBean, stepBean.getStatus());
        try {
            publisher.broadcast(stepBean);
            logger.info("Message was succesfully broadcasted");
        } catch (EventException e) {
            logger.error("Failed to broadcast message: {}", stepBean, e);
        }
	}
}
