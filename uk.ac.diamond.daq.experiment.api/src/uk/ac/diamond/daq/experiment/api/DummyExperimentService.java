package uk.ac.diamond.daq.experiment.api;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;

import uk.ac.diamond.daq.experiment.api.driver.DriverProfileSection;
import uk.ac.diamond.daq.experiment.api.driver.ExperimentDriverModel;

/**
 * For runtime testing and demoing until a real implementation is made
 */
public class DummyExperimentService implements ExperimentService {

	private final Map<String, ScanRequest<IROI>> scans;
	
	private final Map<String, ExperimentDriverModel> driverProfiles;

	public DummyExperimentService() {
		scans = new HashMap<>();
		scans.put("diff_5x5", getDiffractionScan());
		scans.put("tr6_tomo", getTomographyScan());
		
		driverProfiles = new HashMap<>();
		driverProfiles.put("trapez_30s", getProfile());
	}

	private ScanRequest<IROI> getDiffractionScan() {
		IScanPathModel model = new GridModel("beam_x", "beam_y", 5, 5);
		IROI roi = new RectangularROI(0, 0, 5, 5, 0);
		return new ScanRequest<>(model, roi, null, null, null);
	}

	private ScanRequest<IROI> getTomographyScan() {
		IScanPathModel model = new StepModel("tr6_rot", 0, 180, 1);
		return new ScanRequest<>(model, null, null, null);
	}
	
	private ExperimentDriverModel getProfile() {
		ExperimentDriverModel profile = new ExperimentDriverModel();
		profile.setProfile(Arrays.asList(
				new DriverProfileSection(0, 5, 0.5),
				new DriverProfileSection(5, 5, 0.5),
				new DriverProfileSection(5, 0, 0.5)));
		return profile;
	}

	@Override
	public void saveScan(ScanRequest<IROI> scanRequest, String scanName, String experimentId) {
		// no.
	}

	@Override
	public ScanRequest<IROI> getScan(String scanName, String experimentId) {
		return scans.get(scanName);
	}

	@Override
	public Set<String> getScanNames(String experimentId) {
		return scans.keySet();
	}
	
	@Override
	public void saveDriverProfile(ExperimentDriverModel profile, String profileName, String driverName,
			String experimentId) {
		// no op
	}

	@Override
	public ExperimentDriverModel getDriverProfile(String driverName, String modelName, String experimentId) {
		return driverProfiles.get(modelName);
	}

	@Override
	public Set<String> getDriverProfileNames(String driverName, String experimentId) {
		return driverProfiles.keySet();
	}

}
