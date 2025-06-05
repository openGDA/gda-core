/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.xanes;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_CUSTOM_PARAMS;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.AbstractBoundingLineModel;
import org.eclipse.scanning.api.points.models.AbstractTwoAxisGridModel;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
import org.eclipse.scanning.api.points.models.TwoAxisGridStepModel;
import org.eclipse.scanning.api.points.models.TwoAxisLineStepModel;
import org.eclipse.scanning.api.scan.models.ScanMetadata;
import org.eclipse.scanning.api.scan.models.ScanMetadata.MetadataType;
import org.eclipse.scanning.api.script.IScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LineToTrack;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

/**
 * Submit a XANES scan
 * <p>
 * This combines the standard {@link ScanRequest} with the specific parameters from the
 * {@link XanesParametersSection} and calls the script <code>{beamline config}/scanning/submit_xanes_scan.py</code>
 * <p>
 * The parameters are passed in JSON format to avoid serialisation problems.
 */
public class XanesSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesSubmitScanSection.class);

	private static final String XANES_FILE_EXTENSION = "_xanes.json";

	protected XanesParametersSection xanesParametersSection;

	@Override
	protected void submitScan() {
		final ScanRequest scanRequest = getScanRequest(getBean());

		final XanesEdgeParameters xanesEdgeParameters = getParameters();
		final List<ScanMetadata> scanMetadata = new ArrayList<>(scanRequest.getScanMetadata());
		scanMetadata.add(getXanesMetadata(xanesEdgeParameters));
		scanRequest.setScanMetadata(scanMetadata);

		final CompoundModel newModel = getConsistentShapeModel(scanRequest);
		scanRequest.setCompoundModel(newModel);

		runScript(scanRequest, xanesEdgeParameters);
	}

	protected void runScript(ScanRequest scanRequest, XanesEdgeParameters xanesEdgeParameters) {
		final IScriptService scriptService = getService(IScriptService.class);
		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_CUSTOM_PARAMS, marshallerService.marshal(xanesEdgeParameters));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(getScriptFilePath(), "XANES scanning script"));

	}

	private ScanMetadata getXanesMetadata(XanesEdgeParameters xanesEdgeParameters) {
		final ScanMetadata xanesMetadata = new ScanMetadata(MetadataType.ENTRY);
		xanesMetadata.addField("visit_id", InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID());
		xanesMetadata.addField("tracking_method", xanesEdgeParameters.getTrackingMethod().toString());

		LineToTrack line = xanesEdgeParameters.getLineToTrack();
		if (line != null) {
			xanesMetadata.addField("line", line.getElement() + "-" + line.getLine());
		} else {
			xanesMetadata.addField("line", "None");
		}

		return xanesMetadata;
	}

	/**
	 * A new CompoundModel is created with the same number of models of the given CompoundModel,
	 * if any of the models is a StepModel, this has been transformed into their equivalent PointsModel,
	 * which they always have the same shape.
	 * This prevents some errors given by performing similar XANES scans but with small offsets,
	 * which can catch on floating point calculation errors, giving off-by-one in dataset shapes when reconstructing.
	 * @param scanRequest
	 * @return newModel
	 */
	private CompoundModel getConsistentShapeModel(ScanRequest scanRequest) {
		final CompoundModel newModel = new CompoundModel(scanRequest.getCompoundModel());
		final List<IScanPointGeneratorModel> models = newModel.getModels();
		final List<IScanPointGeneratorModel> enforcedShapes = new ArrayList<>(models.size());
		for (IScanPointGeneratorModel model : models) {
			enforcedShapes.add(enforce(model));
		}

		newModel.setModels(enforcedShapes);

		return newModel;
	}

	private IScanPointGeneratorModel enforce(IScanPointGeneratorModel model) {
		if (model instanceof TwoAxisLineStepModel twoaxislinestepmodel) {
			return AbstractBoundingLineModel.enforceShape(twoaxislinestepmodel);
		}
		if (model instanceof TwoAxisGridStepModel twoaxisgridstepmodel) {
			return AbstractTwoAxisGridModel.enforceShape(twoaxisgridstepmodel);
		}
		return model;
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
	}

	protected void setParametersVisibility(boolean visible) {
		if (xanesParametersSection != null) {
			xanesParametersSection.setVisible(visible);

			relayoutView();

			selectOuterScannable(getOuterScannableName(), visible);
			selectDetector(getDetectorName(), visible);
		}
	}

	/**
	 * Sets the XanesParametersSection that needs to be shown or hidden in the mapping view
	 * @param xanesParametersSection
	 */
	public void setXanesParametersSection(XanesParametersSection xanesParametersSection) {
		this.xanesParametersSection = xanesParametersSection;
	}

	private XanesEdgeParameters getParameters() {
		return xanesParametersSection.getScanParameters();
	}

	@Override
	protected String saveScanRequest() {
		var filename = super.saveScanRequest();
		var xanesFilename = filename.substring(0, filename.lastIndexOf(".")) + XANES_FILE_EXTENSION;
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		try {
			final String json = marshaller.marshal(getParameters());
			Files.write(Paths.get(xanesFilename), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		} catch (Exception e) {
			logger.error("Could not save the mapping scan to file: " + xanesFilename, e);
		}
		return filename;
	}

}
