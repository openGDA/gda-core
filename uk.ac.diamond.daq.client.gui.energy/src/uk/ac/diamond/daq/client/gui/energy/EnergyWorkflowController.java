package uk.ac.diamond.daq.client.gui.energy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.beamline.configuration.api.ConfigurationWorkflow;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;

public class EnergyWorkflowController {

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
		executor.submit(() -> {
			try {
				listeners.forEach(EnergyControllerListener::workflowStarted);
				getWorkflow(bean).start(getProperties(bean));
				listeners.forEach(EnergyControllerListener::workflowFinished);
			} catch (WorkflowException e) {
				logger.error("Workflow failed", e);
				listeners.forEach(listener -> listener.workflowFailed(e.getMessage()));
			}
		});
	}

	public void stopWorkflow(BeamEnergyBean bean) {
		try {
			getWorkflow(bean).abort();
		} catch (WorkflowException e) {
			logger.error("Error aborting workflow", e);
			listeners.forEach(listener -> listener.workflowFailed(e.getMessage()));
		}
	}

	public boolean isRunning() {
		return Arrays.asList(monoWorkflow, pinkWorkflow).stream()
				.filter(Objects::nonNull)
				.anyMatch(ConfigurationWorkflow::isRunning);
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

	public void addListener(EnergyControllerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(EnergyControllerListener listener) {
		listeners.remove(listener);
	}

}
