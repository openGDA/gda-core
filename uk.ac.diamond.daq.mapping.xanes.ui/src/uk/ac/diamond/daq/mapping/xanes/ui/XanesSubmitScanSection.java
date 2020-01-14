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

package uk.ac.diamond.daq.mapping.xanes.ui;

import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_SCAN_REQUEST_JSON;
import static org.eclipse.scanning.api.script.IScriptService.VAR_NAME_XANES_EDGE_PARAMS_JSON;

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.script.IScriptService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.concurrent.Async;
import uk.ac.diamond.daq.mapping.api.XanesEdgeParameters;
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
		final XanesEdgeParameters xanesEdgeParameters = paramsSection.getScanParameters();
		xanesEdgeParameters.setVisitId(InterfaceProvider.getBatonStateProvider().getBatonHolder().getVisitID());

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
