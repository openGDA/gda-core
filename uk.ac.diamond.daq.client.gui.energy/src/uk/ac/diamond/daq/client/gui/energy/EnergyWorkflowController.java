package uk.ac.diamond.daq.client.gui.energy;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowStatus;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowUpdate;

public class EnergyWorkflowController implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(EnergyWorkflowController.class);
	private static final String UNRECOGNISED_TYPE_MSG = "Unrecognised beam type";

	public enum EnergySelectionType {
		PINK, MONO, BOTH
	}

	private final EnergySelectionType energySelectionType;

	private final ConfigurationWorkflow monoWorkflow;
	private final ConfigurationWorkflow pinkWorkflow;

	private final Set<EnergyControllerListener> listeners;

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	public EnergyWorkflowController(EnergySelectionType energySelectionType, ConfigurationWorkflow mono, ConfigurationWorkflow pink) {
		this.energySelectionType = energySelectionType;
		this.monoWorkflow = mono;
		this.pinkWorkflow = pink;
		listeners = new HashSet<>();
	}

	public EnergySelectionType getEnergySelectionType() {
		return energySelectionType;
	}

	public void startWorkflow(BeamEnergyBean bean) {
		logger.info("Changing energy: {}", generateEnergyRequestSummary(bean));
		executor.submit(() -> {
			ConfigurationWorkflow wf = getWorkflow(bean);
			wf.addIObserver(this);
			listeners.forEach(EnergyControllerListener::operationStarted);
			wf.start(getProperties(bean));
		});
	}

	private String generateEnergyRequestSummary(BeamEnergyBean bean) {
		return bean.getType().toString() + " -> " +
				(bean.getType() == EnergyType.MONOCHROMATIC ? bean.getMonoEnergy() : bean.getPolyEnergy().getLabel());
	}

	public void stopWorkflow(BeamEnergyBean bean) {
		getWorkflow(bean).abort();
	}

	private Properties getProperties(BeamEnergyBean bean) {
		Properties properties = new Properties();

		switch (bean.getType()) {
		case MONOCHROMATIC:
			properties.setProperty("energy", Double.toString(bean.getMonoEnergy()));
			break;

		case POLYCHROMATIC:
			properties.setProperty("lowBand", Double.toString(bean.getPolyEnergy().getLow()));
			properties.setProperty("highBand", Double.toString(bean.getPolyEnergy().getHigh()));
			break;

		default:
			throw new IllegalArgumentException(UNRECOGNISED_TYPE_MSG);
		}

		return properties;
	}

	private ConfigurationWorkflow getWorkflow(BeamEnergyBean bean) {
		ConfigurationWorkflow workflow;

		switch (bean.getType()) {
		case MONOCHROMATIC:
			workflow = monoWorkflow;
			break;

		case POLYCHROMATIC:
			workflow = pinkWorkflow;
			break;

		default:
			throw new IllegalArgumentException(UNRECOGNISED_TYPE_MSG);
		}

		return workflow;
	}

	/**
	 * If an active workflow is found, the listener will be notified of this workflow's state immediately
	 */
	public void addListener(EnergyControllerListener listener) {
		listeners.add(listener);
		getActiveWorkflow().ifPresent(workflow ->
			notifyListeners(workflow.getState(), Collections.singleton(listener)));
	}

	public void removeListener(EnergyControllerListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void update(Object source, Object arg) {
		WorkflowUpdate update = (WorkflowUpdate) arg;
		logger.debug("Received update: {}", update);
		notifyListeners(update, listeners);
	}

	private void notifyListeners(WorkflowUpdate update, Set<EnergyControllerListener> listeners) {
		switch (update.getStatus()) {
		case IDLE:
			listeners.forEach(EnergyControllerListener::operationFinished);
			break;
		case RUNNING:
			listeners.forEach(listener -> listener.progressMade(update.getMessage(), update.getPercentComplete()));
			break;
		case FAULT: // drop to INTERRUPTED
		case INTERRUPTED:
			listeners.forEach(listener -> listener.operationFailed(update.getMessage()));
			break;
		default:
			throw new IllegalArgumentException("Unrecognised state! " + update.getStatus());
		}
	}

	private Optional<ConfigurationWorkflow> getActiveWorkflow() {
		switch (getEnergySelectionType()) {
		case BOTH:
			return filterActiveWorkflow(monoWorkflow, pinkWorkflow);
		case MONO:
			return filterActiveWorkflow(monoWorkflow);
		case PINK:
			return filterActiveWorkflow(pinkWorkflow);
		default:
			throw new IllegalArgumentException("Unrecognised energy type! " + getEnergySelectionType());
		}
	}

	private Optional<ConfigurationWorkflow> filterActiveWorkflow(ConfigurationWorkflow... workflows) {
		return Arrays.asList(workflows).stream()
				.filter(workflow -> workflow.getState().getStatus() != WorkflowStatus.IDLE)
				.findFirst();
	}

}
