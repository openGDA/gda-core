package uk.ac.diamond.daq.experiment.api;

import java.util.Set;

import gda.factory.Findable;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

/**
 * This service allows us to save and retrieve configurations (e.g. defined
 * scans) associated with a given experiment ID.
 */
public interface ExperimentService extends Findable {

	/**
	 * Save a scan request with the given name and associate it with the given
	 * experiment ID
	 */
	public void saveScan(TriggerableScan scanRequest, String fileName, String experimentId);


	void deleteScan(String scanName, String experimentId);


	/**
	 * Get the scan request saved with the given scan name associated with the given
	 * experiment ID
	 */
	TriggerableScan getScan(String scanName, String experimentId);


	/**
	 * Get the names of all defined scans for the given experiment ID
	 */
	Set<String> getScanNames(String experimentId);

	/* Driver profiles */

	void saveDriverProfile(DriverModel profile, String driverName, String experimentId);

	void deleteDriverProfile(DriverModel profile, String driverName, String experimentId);

	DriverModel getDriverProfile(String driverName, String profileName, String experimentId);

	Set<String> getDriverProfileNames(String driverName, String experimentId);

	/* Experiment Plans */

	Set<String> getExperimentPlanNames();

	ExperimentPlanBean getExperimentPlan(String planName);

	void saveExperimentPlan(ExperimentPlanBean plan);

	void deleteExperimentPlan(String planName);
}
