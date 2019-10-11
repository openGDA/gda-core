package uk.ac.diamond.daq.beamline.configuration;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowException;
import uk.ac.diamond.daq.beamline.configuration.api.WorkflowItem;

/**
 * This base class is responsible for moving {@link Scannable scannables} to their desired positions
 * (concrete implementations provide these through {@link #getPositions(Properties)}) and also stopping
 * the scannables if the workflow item is aborted.
 */
public abstract class WorkflowItemBase implements WorkflowItem {

	private static final Logger logger = LoggerFactory.getLogger(WorkflowItemBase.class);

	@Override
	public void start(Properties properties) throws WorkflowException {
		Map<Scannable, Object> positions = getPositions(properties);
		moveScannables(positions);
	}

	@Override
	public void abort() throws WorkflowException {
		logger.info("Workflow item aborted: stopping contained scannables");
		for (Scannable scannable : getScannables()) {
			try {
				scannable.stop();
			} catch (DeviceException e) {
				throw new WorkflowException("Error stopping scannable '" + scannable.getName() + "'", e);
			}
		}
	}

	/**
	 * Read whatever property you need and get me a map of Scannable to its target position (as Double)
	 */
	protected abstract Map<Scannable, Object> getPositions(Properties properties) throws WorkflowException;

	/**
	 * All scannables configured in this item
	 */
	protected abstract Set<Scannable> getScannables();

	/**
	 * Moves scannables asynchronously, then blocks until they reach their destinations
	 */
	protected void moveScannables(Map<Scannable, Object> positions) throws WorkflowException {
		for (Map.Entry<Scannable, Object> entry : positions.entrySet()) {
			Scannable scannable = entry.getKey();
			Object position = entry.getValue();
			move(scannable, position);
		}

		for (Scannable scannable : positions.keySet()) {
			waitWhileBusy(scannable);
		}
	}

	private void move(Scannable scannable, Object position) throws WorkflowException {
		try {
			scannable.asynchronousMoveTo(position);
		} catch (DeviceException e) {
			throw new WorkflowException("Error moving scannable '" + scannable.getName() + "'", e);
		}
	}

	private void waitWhileBusy(Scannable scannable) throws WorkflowException {
		try {
			scannable.waitWhileBusy();
		} catch (InterruptedException | DeviceException e) {
			throw new WorkflowException("Error while scannable '" + scannable.getName() + "' is moving", e);
		}
	}
}
