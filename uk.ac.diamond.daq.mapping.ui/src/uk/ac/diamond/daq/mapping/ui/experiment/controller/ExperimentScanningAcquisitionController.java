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

package uk.ac.diamond.daq.mapping.ui.experiment.controller;

import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import java.net.URL;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.ui.controller.ScanningAcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;
import uk.ac.gda.client.exception.AcquisitionControllerException;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.ui.tool.controller.AcquisitionController;
import uk.ac.gda.ui.tool.rest.ExperimentControllerServiceClient;

/**
 * Adds the experiment controller logic to a standard the {@link ScanningAcquisitionController}
 *
 * @see ExperimentController
 * @see ExperimentControllerServiceClient
 *
 * @author Maurizio Nagni
 */
@Controller(value = "experimentAcquisitionController")
public class ExperimentScanningAcquisitionController implements AcquisitionController<ScanningAcquisition> {

	@Autowired
	private AcquisitionController<ScanningAcquisition> acquisitionController;

	@Override
	public ScanningAcquisition getAcquisition() {
		return getAcquisitionController().getAcquisition();
	}

	@Override
	public void newScanningAcquisition(AcquisitionKeys acquisitionKey) throws AcquisitionControllerException {
		getAcquisitionController().newScanningAcquisition(acquisitionKey);
	}

	@Override
	public void saveAcquisitionConfiguration() throws AcquisitionControllerException {
		getAcquisitionController().saveAcquisitionConfiguration();
	}

	@Override
	public RunAcquisitionResponse runAcquisition() throws AcquisitionControllerException {
		if (!getExperimentController().isExperimentInProgress()) {
			throw new AcquisitionControllerException("Cannot start acquisition", new ExperimentControllerException("You must start an experiment first"));
		}
		try {
			URL acquisitionPath = getExperimentController().prepareAcquisition(acquisitionController.getAcquisition().getName());
			getAcquisitionController().getAcquisition().setAcquisitionLocation(acquisitionPath);
		} catch (GDAClientRestException e) {
			throw new AcquisitionControllerException("Cannot start acquisition", new ExperimentControllerException("Cannot prepare the experiment URL"));
		}
		return getAcquisitionController().runAcquisition();
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		getAcquisitionController().loadAcquisitionConfiguration(acquisition);
	}

	@Override
	public void initialise(AcquisitionKeys keys) throws AcquisitionControllerException {
		getAcquisitionController().initialise(keys);
	}

	@Override
	public AcquisitionConfigurationResource<ScanningAcquisition> createAcquisitionConfigurationResource(UUID uuid)
			throws AcquisitionControllerException {
		return getAcquisitionController().createAcquisitionConfigurationResource(uuid);
	}

	@Override
	public void deleteAcquisitionConfiguration(UUID uuid) throws AcquisitionControllerException {
		getAcquisitionController().deleteAcquisitionConfiguration(uuid);
	}

	@Override
	public void releaseResources() {
		getAcquisitionController().releaseResources();
	}

	@Override
	public AcquisitionKeys getAcquisitionKeys() {
		return getAcquisitionController().getAcquisitionKeys();
	}

	public AcquisitionController<ScanningAcquisition> getAcquisitionController() {
		return acquisitionController;
	}
}
