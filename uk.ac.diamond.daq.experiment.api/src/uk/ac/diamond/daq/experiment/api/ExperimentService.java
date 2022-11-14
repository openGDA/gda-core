/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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
