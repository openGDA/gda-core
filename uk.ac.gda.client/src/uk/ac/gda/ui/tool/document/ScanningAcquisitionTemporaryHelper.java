/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.tool.document;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.AcquisitionType;
import uk.ac.gda.api.acquisition.configuration.processing.ProcessingRequestPair;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.spring.ClientSpringContext;

/**
 * Temporary class to collect together the most used getters from the active {@link AcquisitionController#getAcquisition()}.
 * <p>
 * The goal of this class is to reduce the duplication of similar getters but at the same time is a temporary solution
 * before consolidate it with classes from {@code uk.ac.diamond.daq.mapping.api.document.helper} package.
 * </p>
 * <p>
 * The crucial characteristic of these methods is to return {@link Optional} objects.
 * </p>
 *
 * @author Maurizio Nagni
 */
@Component
public class ScanningAcquisitionTemporaryHelper {

	private static final Logger logger = LoggerFactory.getLogger(ScanningAcquisitionTemporaryHelper.class);

	@Autowired
	private ClientSpringContext clientSpringContext;

	private ClientSpringContext getClientSpringContext() {
		return clientSpringContext;
	}

	public Optional<AcquisitionController<ScanningAcquisition>> getAcquisitionController() {
		return getClientSpringContext().getAcquisitionController();
	}

	public AcquisitionController<ScanningAcquisition> getAcquisitionControllerElseThrow() {
		return getClientSpringContext()
				.getAcquisitionController()
				.orElseThrow();
	}

	public void setNewScanningAcquisition(AcquisitionKeys acquisitionKey) throws AcquisitionControllerException {
		getAcquisitionControllerElseThrow()
			.newScanningAcquisition(acquisitionKey);
	}

	public AcquisitionType getAcquisitionType() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getType)
				.orElseGet(() -> AcquisitionType.GENERIC);
	}

	public Optional<ScanningAcquisition> getScanningAcquisition() {
		return getAcquisitionController()
				.map(AcquisitionController<ScanningAcquisition>::getAcquisition);
	}

	public Optional<ScanningParameters> getScanningParameters() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getAcquisitionConfiguration)
				.map(ScanningConfiguration::getAcquisitionParameters);
	}

	public Optional<List<ProcessingRequestPair<?>>> getProcessingRequest() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getAcquisitionConfiguration)
				.map(ScanningConfiguration::getProcessingRequest);
	}

	public Optional<ScanningConfiguration> getAcquisitionConfiguration() {
		return getScanningAcquisition()
				.map(ScanningAcquisition::getAcquisitionConfiguration);
	}

	public Optional<ScanpathDocument> getScanpathDocument() {
		return getScanningParameters()
				.map(ScanningParameters::getScanpathDocument);
	}

	//------- NEW/SAVE/RUN -------
	public void saveAcquisition() {
		boolean hasUUID = getScanningAcquisition()
				.map(ScanningAcquisition::getUuid)
				.map(Objects::nonNull)
				.orElse(false);
		if (hasUUID && !UIHelper.showConfirm("Override the existing configuration?")) {
			return;
		}

		try {
			getAcquisitionControllerElseThrow().saveAcquisitionConfiguration();
		} catch (AcquisitionControllerException e) {
			UIHelper.showError("Cannot save acquisition", e, logger);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(ClientMessages.NO_CONTROLLER, e);
		}
	}

	public void runAcquisition() {
		try {
			getAcquisitionControllerElseThrow().runAcquisition();
		} catch (AcquisitionControllerException e) {
			UIHelper.showError(e.getMessage(), ExceptionUtils.getRootCauseMessage(e));
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(ClientMessages.NO_CONTROLLER, e);
		}
	}

	//------- NEW/SAVE/RUN -------
}
