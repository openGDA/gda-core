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

import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import gda.factory.FindableBase;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

/**
 * Save and load to file system.
 *
 * TODO store by experiment ID, currently unused!
 */

public class FileSystemBasedExperimentService extends FindableBase implements ExperimentService {

	private static final String EXPERIMENT_BASE = "experiments";
	private static final String PLAN_EXTENSION = "plan";
	private static final String DRIVER_PROFILE_EXTENSION = "prof";
	private static final String SCAN_EXTENSION = "scan";

	private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_\\ ]");
	private static final String INVALID_CHARACTER_REPLACEMENT = "_";

	private final SaveLoadTool saver = new SaveLoadTool(EXPERIMENT_BASE);

	private String getValidName(String fileName) {
		return INVALID_CHARACTERS_PATTERN.matcher(fileName).replaceAll(INVALID_CHARACTER_REPLACEMENT);
	}

	/* EXPERIMENT PLANS */

	@Override
	public void saveExperimentPlan(ExperimentPlanBean plan) {
		if (plan.getUuid() == null) {
			plan.setUuid(UUID.randomUUID());
		}
		saver.saveObject(plan, getValidName(plan.getPlanName()), PLAN_EXTENSION);
	}

	@Override
	public ExperimentPlanBean getExperimentPlan(String planName) {
		return saver.loadObject(ExperimentPlanBean.class, getValidName(planName), PLAN_EXTENSION);
	}

	@Override
	public Set<String> getExperimentPlanNames() {
		return saver.getSavedNames(PLAN_EXTENSION);
	}

	@Override
	public void deleteExperimentPlan(String planName) {
		saver.delete(getValidName(planName), PLAN_EXTENSION);
	}

	/* DRIVER PROFILES */

	@Override
	public void saveDriverProfile(DriverModel profile, String driverName, String experimentId) {
		saver.saveObject(profile, getDriverProfileName(profile.getName(), driverName), DRIVER_PROFILE_EXTENSION);
	}

	@Override
	public DriverModel getDriverProfile(String driverName, String modelName, String experimentId) {
		return saver.loadObject(SingleAxisLinearSeries.class, getDriverProfileName(modelName, driverName), DRIVER_PROFILE_EXTENSION);
	}

	@Override
	public Set<String> getDriverProfileNames(String driverName, String experimentId) {
		return saver.getSavedNames(DRIVER_PROFILE_EXTENSION, driverName);
	}

	@Override
	public void deleteDriverProfile(DriverModel profile, String driverName, String experimentId) {
		saver.delete(getDriverProfileName(profile.getName(), driverName), DRIVER_PROFILE_EXTENSION);
	}

	private String getDriverProfileName(String modelName, String driverName) {
		return Paths.get(getValidName(driverName), getValidName(modelName)).toString();
	}

	/* SCANS */

	@Override
	public void saveScan(TriggerableScan scanRequest, String fileName, String experimentId) {
		saver.saveObject(scanRequest, getValidName(fileName), SCAN_EXTENSION);
	}

	@Override
	public TriggerableScan getScan(String scanName, String experimentId) {
		return saver.loadObject(TriggerableScan.class, getValidName(scanName), SCAN_EXTENSION);
	}

	@Override
	public Set<String> getScanNames(String experimentId) {
		return saver.getSavedNames(SCAN_EXTENSION, experimentId);

	}

	@Override
	public void deleteScan(String scanName, String experimentId) {
		saver.delete(getValidName(scanName), SCAN_EXTENSION);

	}

}
