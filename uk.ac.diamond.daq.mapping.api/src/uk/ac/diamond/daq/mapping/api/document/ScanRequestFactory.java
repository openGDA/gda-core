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

package uk.ac.diamond.daq.mapping.api.document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ProcessingRequest;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.api.scan.models.ScanModel;

/**
 * A helper class to generate a {@link ScanRequest} from a {@link ScanRequestDocument}.
 *
 * @author Maurizio Nagni
 */
public class ScanRequestFactory {

	private final ScanRequestDocument srd;

	public ScanRequestFactory(ScanRequestDocument srd) {
		super();
		this.srd = srd;
	}

	public ScanRequest createScanRequest(IRunnableDeviceService runnableDeviceService) throws ScanningException {
		CompoundModel scanModel = new CompoundModel();
		scanModel.setModels(new ArrayList<>());
		scanModel.setRegions(new ArrayList<>());
		scanModel.setMutators(new ArrayList<>());

		addPathDefinitionToCompoundModel(srd, scanModel);

		// Populate the {@link ScanRequest} with the assembled objects
		ScanRequest scanRequest = new ScanRequest();
		if (srd.getFilePath() != null) {
			scanRequest.setFilePath(srd.getFilePath().getPath());
		}
		scanRequest.setCompoundModel(scanModel);
		scanRequest.setDetectors(parseDetectors(srd.getDetectors(), runnableDeviceService));
		scanRequest.setMonitorNamesPerPoint(parseMonitorNamesPerPoint(srd));
		scanRequest.setTemplateFilePaths(parseTemplateFilePaths(srd));
		scanRequest.setProcessingRequest(parseProcessingRequest(srd));
		return scanRequest;
	}

	private Map<String, Object> parseDetectors(DetectorDocument[] detectors, IRunnableDeviceService runnableDeviceService) throws ScanningException {
		Map<String, Object> ret = new HashMap<>();

		for (DetectorDocument det : detectors) {
			IRunnableDevice<?> detector = runnableDeviceService.getRunnableDevice(det.getName());

			double exposure = det.getExposure();
			// nullCheck(detector, IRunnableDevice.class.getSimpleName());
			IDetectorModel model = (IDetectorModel) detector.getModel();
			if (model == null) {
				throw new ScanningException(String.format("Could not get model for detector %s", detector.getName()));
			}
			if (exposure > 0) {
				model.setExposureTime(exposure);
			}
			ret.put(detector.getName(), model);
		}
		return ret;
	}

	private Collection<String> parseMonitorNamesPerPoint(ScanRequestDocument document) {
		return new ArrayList<>();
	}

	private Set<String> parseTemplateFilePaths(ScanRequestDocument document) {
		return new HashSet<>();
	}

	private ProcessingRequest parseProcessingRequest(ScanRequestDocument document) {
		return new ProcessingRequest();
	}

	/**
	 * Accepts an empty Scan model to be populated with the state set in the context for a scan path clause. The content
	 * is validated first to check that all the necessary elements for a scan path clause are present. Flags are also
	 * set to indicate successful storage of the scan path and processing of the clause.
	 *
	 * @param scanModel
	 *            The {@link ScanModel} to be filled in
	 * @throws ScanningException
	 * @throws IllegalArgumentException
	 *             if the {@link IROI} fails to validate the supplied parameters on creation
	 */
	private void addPathDefinitionToCompoundModel(ScanRequestDocument srd, final CompoundModel scanModel)
			throws ScanningException {
		scanModel.setData(srd.getScanpath().getIScanPointGeneratorModel(), srd.getScanpath().getROI());
	}
}
