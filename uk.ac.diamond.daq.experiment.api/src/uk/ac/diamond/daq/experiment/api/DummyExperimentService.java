package uk.ac.diamond.daq.experiment.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.StepModel;

/**
 * For runtime testing and demoing until a real implementation is made
 */
public class DummyExperimentService implements ExperimentService {

	private final Map<String, ScanRequest<IROI>> scans;

	public DummyExperimentService() {
		scans = new HashMap<>();
		scans.put("diff_5x5", getDiffractionScan());
		scans.put("tr6_tomo", getTomographyScan());
	}

	private ScanRequest<IROI> getDiffractionScan() {
		IScanPathModel model = new GridModel("beam_x", "beam_y", 5, 5);
		IROI roi = new RectangularROI(0, 0, 5, 5, 0);
		return new ScanRequest<>(model, roi, null, null, null);
	}

	private ScanRequest<IROI> getTomographyScan() {
		IScanPathModel model = new StepModel("tr6_rot", 0, 180, 1);
		return new ScanRequest<>(model, null, null, null, null);
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

}
