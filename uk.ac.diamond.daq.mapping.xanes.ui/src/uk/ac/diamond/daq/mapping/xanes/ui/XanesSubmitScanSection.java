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

import org.eclipse.dawnsci.analysis.api.persistence.IMarshallerService;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.jython.ICommandRunner;
import gda.jython.InterfaceProvider;
import uk.ac.diamond.daq.mapping.ui.experiment.SubmitScanSection;

/**
 * Submit a XANES scan
 * <p>
 * This combines the standard mapping bean with the specific parameters from the {@link XanesEdgeParametersSection} and
 * calls the script run_xanes_scan().<br>
 * The script is beamline-specific but must be on the Jython path.
 */
public class XanesSubmitScanSection extends SubmitScanSection {

	private static final Logger logger = LoggerFactory.getLogger(XanesSubmitScanSection.class);

	@Override
	public void createControls(Composite parent) {
		setButtonColour(new RGB(179, 204, 255));
		super.createControls(parent);
	}

	@Override
	protected void submitScan() {
		final XanesEdgeParametersSection paramsSection = getMappingView().getSection(XanesEdgeParametersSection.class);
		final XanesScanParameters xanesScanParameters = new XanesScanParameters(paramsSection.getScanParameters());
		final IMarshallerService marshaller = getService(IMarshallerService.class);
		final ICommandRunner commandRunner = InterfaceProvider.getCommandRunner();

		try {
			final String parameterString = marshaller.marshal(xanesScanParameters).replaceAll("'", "\\\\'");
			final String command = String.format("run_xanes_scan('%s')", parameterString);
			logger.debug("Executing Jython command: {}", command);
			commandRunner.runCommand(command);
		} catch (Exception e) {
			logger.error("Error submitting XANES scan", e);
		}
	}

	@Override
	protected void onShow() {
		setParametersVisibility(true);
	}

	@Override
	protected void onHide() {
		setParametersVisibility(false);
	}

	/*
	 * Show or hide the corresponding parameters section
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

	/**
	 * Class to hold all parameters required by the XANES scan
	 * <p>
	 * This will be serialised to JSON and passed to the XANES script.
	 */
	private class XanesScanParameters {
		// XANES-specific parameters
		public final String linesToTrack;
		public final String trackingMethod;

		// Standard mscan command
		public final String mscanCommand;

		XanesScanParameters(XanesEdgeParameters xanesParams) {
			linesToTrack = xanesParams.getLinesToTrack();
			trackingMethod = xanesParams.getTrackingMethod();
			mscanCommand = createScanCommand();
		}

		@Override
		public String toString() {
			return "XanesScanParameters [linesToTrack=" + linesToTrack + ", trackingMethod=" + trackingMethod
					+ ", mscanCommand=" + mscanCommand + "]";
		}
	}
}
