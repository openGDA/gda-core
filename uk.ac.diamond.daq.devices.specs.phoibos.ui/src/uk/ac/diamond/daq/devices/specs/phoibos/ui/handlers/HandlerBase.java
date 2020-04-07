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

package uk.ac.diamond.daq.devices.specs.phoibos.ui.handlers;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.factory.Finder;
import uk.ac.diamond.daq.devices.specs.phoibos.api.ISpecsPhoibosAnalyser;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequence;
import uk.ac.diamond.daq.devices.specs.phoibos.api.SpecsPhoibosSequenceValidation;
import uk.ac.diamond.daq.devices.specs.phoibos.ui.SpecsUiConstants;

public class HandlerBase {

	ISpecsPhoibosAnalyser analyser;
	@Inject
	IEventBroker eventBroker;
	@Inject
	EPartService partService;

	public HandlerBase() {
		logger.trace("Constructor called");

		// Get an analyser
		List<ISpecsPhoibosAnalyser> analysers = Finder.listLocalFindablesOfType(ISpecsPhoibosAnalyser.class);
		if (analysers.size() != 1) {
			String msg = "No Analyser was found! (Or more than 1)";
			logger.error(msg);
			throw new IllegalStateException(msg);
		}
		analyser = analysers.get(0);
		logger.debug("Connected to analyser: {}", analyser);
	}

	private static final Logger logger = LoggerFactory.getLogger(HandlerBase.class);

	protected SpecsPhoibosSequenceValidation validateSequence(Shell shell, SpecsPhoibosSequence sequence,
			ISpecsPhoibosAnalyser analyser) throws DeviceException {
		logger.trace("About to configure analyser with sequence: {}", sequence);
		try {
			return analyser.validateSequence(sequence);
		} catch (DeviceException exception) {
			logger.error("Device errors encountered during sequence validation", exception);
			MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
			validationDialog.setText("Device errors encountered");
			validationDialog.setMessage("Device errors were encountered while trying to validate sequence.");
			validationDialog.open();
			throw exception;
		}
	}

	protected void presentValidationResults(SpecsPhoibosSequenceValidation sequenceValidationResult,
			IEventBroker eventBroker, EPartService partService, Shell shell) {

		if(!sequenceValidationResult.isValid()){
			partService.showPart("uk.ac.diamond.daq.devices.specs.phoibos.ui.part.regionvalidation", PartState.VISIBLE);
			logger.info("Sequence has invalid regions");
			Display.getDefault().asyncExec(() -> {
				MessageBox validationDialog = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
				validationDialog.setText("One or more regions have invalid values");
				validationDialog.setMessage("Select a region and see the 'Validation Errors' view for details.");
				validationDialog.open();
			});
		}

		eventBroker.send(SpecsUiConstants.REGION_VALIDATION_EVENT, sequenceValidationResult);

	}

}
