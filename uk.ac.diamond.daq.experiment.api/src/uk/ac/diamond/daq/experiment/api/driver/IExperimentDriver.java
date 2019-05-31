package uk.ac.diamond.daq.experiment.api.driver;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;

/**
 * The ExperimentDriver is any apparatus or mechanism that modifies the specimen
 * over time. This can include a mechanical test rig, a furnace, a chemical
 * delivery system, etc.
 * <p>
 * The ExperimentDriver will follow a user-specified profile which may be software-
 * or hardware-driven (i.e. driven by GDA or by some external controller). This
 * profile is generally the process referred to as <i>the experiment</i>, where
 * as scans performed during this time are termed <i>measurements</i>.
 * <p>
 * Additionally, an ExperimentDriver will have a set of readouts which can be used to monitor
 * progress and to trigger GDA/Malcolm measurements (see {@link ISampleEnvironmentVariable}).
 * These readouts need not necessarily be part of the driven hardware, but more generally
 * signals which are affected by the profile. As such, they may need to be calibrated and used
 * as software limits and/or abort conditions.
 *
 *
 * @author Douglas Winter
 */
public interface IExperimentDriver<T extends DriverModel> extends Findable {

	void setModel(T model) throws DeviceException;
	T getModel();

	String getQuantityName();

	String getQuantityUnits();

	/**
	 * @return signals which respond to the profile
	 */
	Map<String, Scannable> getReadouts();

	/**
	 * @return the name of the main readout
	 */
	String getMainReadoutName();

	/**
	 * Convenience method to get readout of given name
	 */
	default Scannable getReadout(String name) {
		Scannable readout = getReadouts().get(name);
		Objects.requireNonNull(readout, "This experiment driver does not have a readout named '" + name + "'");
		return readout;
	}

	/**
	 * Convenience method to get the names of all readouts associated with this driver
	 */
	default Set<String> getReadoutNames() {
		return new HashSet<>(getReadouts().keySet());
	}

	/**
	 * Calibrate
	 */
	void zero();

	/**
	 * Allowed from {@link DriverState.IDLE}
	 */
	void start();

	/**
	 * Allowed from {@link DriverState.RUNNING}
	 */
	void pause();

	/**
	 * Allowed from {@link DriverState.PAUSED}
	 */
	void resume();

	/**
	 * Allowed from {@link DriverState.RUNNING} or {@link DriverState.PAUSED}
	 */
	void abort();

	DriverState getState();

}
