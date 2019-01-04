package uk.ac.diamond.daq.experiment.api.driver;

import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.plan.ISampleEnvironmentVariable;
import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;

import java.util.Map;

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
 * Additionally, an ExperimentDriver will have a set of sensors which can be used to monitor
 * progress and to trigger GDA/Malcolm measurements (see {@link ISampleEnvironmentVariable}).
 * These sensors need not necessarily be part of the driven hardware, but more generally 
 * signals which are affected by the profile. As such, they may need to be calibrated and used
 * as software limits and/or abort conditions.
 * 
 * 
 * @author Douglas Winter
 */
public interface IExperimentDriver extends Findable {
	
	void setModel(ExperimentDriverModel model);
	ExperimentDriverModel getModel();
	
	/**
	 * @return signals which respond to the profile
	 */
	Map<String, SEVSignal> getSensors();
	
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
