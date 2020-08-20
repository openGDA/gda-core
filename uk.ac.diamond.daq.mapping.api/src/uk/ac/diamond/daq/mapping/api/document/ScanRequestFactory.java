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

import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionConfigurationBase;
import uk.ac.diamond.daq.mapping.api.document.base.AcquisitionParametersBase;
import uk.ac.diamond.daq.mapping.api.document.model.AcquisitionTemplateFactory;
import uk.ac.gda.api.exception.GDAException;

/**
 * A helper class to generate a {@link ScanRequest} from a {@link ScanRequestDocument}.
 *
 * @author Maurizio Nagni
 */
public class ScanRequestFactory {

	private final AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition;

	public ScanRequestFactory(
			AcquisitionBase<? extends AcquisitionConfigurationBase<? extends AcquisitionParametersBase>> acquisition) {
		super();
		this.acquisition = acquisition;
	}

	public ScanRequest createScanRequest(IRunnableDeviceService runnableDeviceService) throws ScanningException {
		CompoundModel scanModel = new CompoundModel();
		scanModel.setModels(new ArrayList<>());
		scanModel.setRegions(new ArrayList<>());
		scanModel.setMutators(new ArrayList<>());

		setDataToCompoundModel(scanModel);

		// Populate the {@link ScanRequest} with the assembled objects
		ScanRequest scanRequest = new ScanRequest();
		if (acquisition.getAcquisitionLocation() != null) {
			scanRequest.setFilePath(acquisition.getAcquisitionLocation().getPath());
		}
		scanRequest.setCompoundModel(scanModel);

		if (acquisition.getAcquisitionEngine() != null) {
			parseAcquisitionEngine(scanRequest, runnableDeviceService);
		}

		scanRequest.setMonitorNamesPerPoint(parseMonitorNamesPerPoint());
		scanRequest.setTemplateFilePaths(parseTemplateFilePaths());
		scanRequest.setProcessingRequest(parseProcessingRequest());
		return scanRequest;
	}

	private void parseAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService)
			throws ScanningException {
		switch (acquisition.getAcquisitionEngine().getType()) {
		case MALCOLM:
			prepareMalcolmAcquisitionEngine(scanRequest, runnableDeviceService);
			break;
		default:

		}
	}

	private void prepareMalcolmAcquisitionEngine(ScanRequest scanRequest, IRunnableDeviceService runnableDeviceService)
			throws ScanningException {
		final Map<String, IDetectorModel> ret = new HashMap<>();
		scanRequest.setDetectors(ret);

		IRunnableDevice<IDetectorModel> detector = runnableDeviceService
				.getRunnableDevice(acquisition.getAcquisitionEngine().getId());
		final IDetectorModel model = detector.getModel();

		if (model == null) {
			throw new ScanningException(String.format("Could not get model for detector %s",
					detector.getName()));
		}
		ret.put(detector.getName(), model);
	}

	private Collection<String> parseMonitorNamesPerPoint() {
		return new ArrayList<>();
	}

	private Set<String> parseTemplateFilePaths() {
		return new HashSet<>();
	}

	private ProcessingRequest parseProcessingRequest() {
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
	private void setDataToCompoundModel(final CompoundModel scanModel)
			throws ScanningException {
		try {
			AcquisitionTemplate modelDocument = AcquisitionTemplateFactory.buildModelDocument(getAcquisitionParameters().getScanpathDocument());
			scanModel.setData(modelDocument.getIScanPointGeneratorModel(), modelDocument.getROI());
			scanModel.setDuration(getAcquisitionParameters().getDetector().getExposure());
		} catch (GDAException e) {
			throw new ScanningException(e.getMessage(), e);
		}
	}

	private AcquisitionParametersBase getAcquisitionParameters() {
		return acquisition.getAcquisitionConfiguration().getAcquisitionParameters();
	}
}
