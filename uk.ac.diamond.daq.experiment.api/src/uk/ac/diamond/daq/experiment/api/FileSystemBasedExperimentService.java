package uk.ac.diamond.daq.experiment.api;

import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

/**
 * Save and load to file system.
 *
 * FIXME Only extending dummy impl until scan support is added!
 * TODO store by experiment ID, currently unused!
 */
public class FileSystemBasedExperimentService extends DummyExperimentService {

	private static final String EXPERIMENT_BASE = "experiments";
	private static final String PLAN_EXTENSION = "plan";
	private static final String DRIVER_PROFILE_EXTENSION = "prof";

	private static final Pattern INVALID_CHARACTERS_PATTERN = Pattern.compile("[^a-zA-Z0-9\\.\\-\\_\\ ]");
	private static final String INVALID_CHARACTER_REPLACEMENT = "_";

	private final SaveLoadTool saver = new SaveLoadTool(EXPERIMENT_BASE);

	/* EXPERIMENT PLANS */

	@Override
	public void saveExperimentPlan(ExperimentPlanBean plan) {
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

	private String getValidName(String fileName) {
		return INVALID_CHARACTERS_PATTERN.matcher(fileName).replaceAll(INVALID_CHARACTER_REPLACEMENT);
	}

}
