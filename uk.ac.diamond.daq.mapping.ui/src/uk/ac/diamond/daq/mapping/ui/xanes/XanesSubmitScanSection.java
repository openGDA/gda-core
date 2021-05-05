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

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_XANES_EDGE_PARAMS_JSON;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters.LinesToTrackEntry;
import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;

/**
 * Submit a XANES scan
 * <p>
 * This combines the standard {@link ScanRequest} with the specific parameters from the
 * {@link XanesEdgeParametersSection} and calls the script <code>{beamline config}/scanning/submit_xanes_scan.py</code>
 * <p>
 * The parameters are passed in JSON format to avoid serialisation problems.
 */
public class XanesSubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(XanesSubmitScanSection.class);

	private String scriptFilePath = "scanning/submit_xanes_scan.py";
	private String energyScannableName;

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(179, 204, 255));
		super.createControls(parent);
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(submitComposite);
		createSubmitButton(submitComposite);
		createStopButton(submitComposite);
	}

	@Override
	protected void submitScan() {
		final IScriptService scriptService = getService(IScriptService.class);
		final ScanRequest scanRequest = getScanRequest(getMappingBean());
		final XanesEdgeParametersSection paramsSection = getMappingView().getSection(XanesEdgeParametersSection.class);
		if (paramsSection.isEnforcedShape()) {
			final CompoundModel newModel = new CompoundModel(scanRequest.getCompoundModel());
			final List<IScanPointGeneratorModel> models = newModel.getModels();
			final List<IScanPointGeneratorModel> enforcedShapes = new ArrayList<>(models.size());
			for (IScanPointGeneratorModel model : models) {
				enforcedShapes.add(enforce(model));
			}
			newModel.setModels(enforcedShapes);
			scanRequest.setCompoundModel(newModel);
		}

		final XanesEdgeParameters xanesEdgeParameters = paramsSection.getScanParameters();
		xanesEdgeParameters.setVisitId(InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID());

		// Add XANES parameters as metadata to the ScanRequest, so they appear in the Nexus file
		final ScanMetadata xanesMetadata = new ScanMetadata(MetadataType.ENTRY);
		xanesMetadata.addField("tracking_method", xanesEdgeParameters.getTrackingMethod());
		xanesMetadata.addField("visit_id", xanesEdgeParameters.getVisitId());

		final LinesToTrackEntry linesToTrackEntry = xanesEdgeParameters.getLinesToTrack();
		if (linesToTrackEntry == null || linesToTrackEntry.getLine() == null || linesToTrackEntry.getLine().isEmpty()) {
			// The entry for a blank "lines to track" contains an unmodifiable Collection, which causes problems in
			// marshalling, so make sure it is set null.
			xanesEdgeParameters.setLinesToTrack(null);
			xanesMetadata.addField("line", "None");
		} else {
			xanesMetadata.addField("line", linesToTrackEntry.getLine());
			xanesMetadata.addField("file_paths", new ArrayList<String>(linesToTrackEntry.getFilePaths()));
		}

		final List<ScanMetadata> scanMetadata = new ArrayList<>(scanRequest.getScanMetadata());
		scanMetadata.add(xanesMetadata);
		scanRequest.setScanMetadata(scanMetadata);

		try {
			final IMarshallerService marshallerService = getService(IMarshallerService.class);
			scriptService.setNamedValue(VAR_NAME_SCAN_REQUEST_JSON, marshallerService.marshal(scanRequest));
			scriptService.setNamedValue(VAR_NAME_XANES_EDGE_PARAMS_JSON, marshallerService.marshal(xanesEdgeParameters));
		} catch (Exception e) {
			logger.error("Scan submission failed", e);
			MessageDialog.openError(getShell(), "Error Submitting Scan", "The scan could not be submitted. See the error log for more details.");
			return;
		}

		Async.execute(() -> runScript(scriptFilePath, "XANES scanning script"));
	}

	private IScanPointGeneratorModel enforce(IScanPointGeneratorModel model) {
		if (model instanceof TwoAxisLineStepModel) {
			return AbstractBoundingLineModel.enforceShape((TwoAxisLineStepModel) model);
		}
		if (model instanceof TwoAxisGridStepModel) {
			return AbstractTwoAxisGridModel.enforceShape((TwoAxisGridStepModel) model);
		}
		return model;
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
		selectOuterScannable(energyScannableName, true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
		selectOuterScannable(energyScannableName, false);
	}

	/**
	 * Show or hide the corresponding parameters section
	 *
	 * @param visible
	 *            <code>true</code> to show the section, <code>false</code> to hide it
	 */
	private void setParametersVisibility(boolean visible) {
		final XanesEdgeParametersSection xanesParams = getMappingView().getSection(XanesEdgeParametersSection.class);

		if (xanesParams == null) {
			logger.error("No XANES parameters section found");
		} else {
			xanesParams.setVisible(visible);
			relayoutMappingView();
		}
	}

	public void setEnergyScannableName(String energyScannableName) {
		this.energyScannableName = energyScannableName;
	}

	public void setScriptFilePath(String scriptFilePath) {
		this.scriptFilePath = scriptFilePath;
	}
}
