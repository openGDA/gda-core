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

package uk.ac.gda.tomography.view;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.rest.ClientRestServices.getExperimentController;

import java.net.URL;
import java.util.function.Supplier;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentControllerException;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.ui.properties.AcquisitionsPropertiesHelper;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.response.RunAcquisitionResponse;

/**
 * Consolidates the logic controlling the {@link TomographyConfigurationView}.
 *
 * @author Maurizio Nagni
 */
public class ExperimentScanningAcquisitionController implements AcquisitionController<ScanningAcquisition> {

	private final AcquisitionsPropertiesHelper.AcquisitionPropertyType acquisitionType;

	private AcquisitionController<ScanningAcquisition> acquisitionController;

	public ExperimentScanningAcquisitionController(AcquisitionsPropertiesHelper.AcquisitionPropertyType acquisitionType) {
		this.acquisitionType = acquisitionType;
	}

	@Override
	public ScanningAcquisition getAcquisition() {
		return getAcquisitionController().getAcquisition();
	}

	@Override
	public void createNewAcquisition() {
		getAcquisitionController().createNewAcquisition();
	}

	@Override
	public void setDefaultNewAcquisitionSupplier(Supplier<ScanningAcquisition> newAcquisitionSupplier) {
		getAcquisitionController().setDefaultNewAcquisitionSupplier(newAcquisitionSupplier);
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
		return getAcquisitionController().runAcquisition();
	}

	@Override
	public void loadAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		getAcquisitionController().loadAcquisitionConfiguration(url);
	}

	@Override
	public void loadAcquisitionConfiguration(ScanningAcquisition acquisition) throws AcquisitionControllerException {
		getAcquisitionController().loadAcquisitionConfiguration(acquisition);
	}

	@Override
	public AcquisitionConfigurationResource<ScanningAcquisition> parseAcquisitionConfiguration(URL url)
			throws AcquisitionControllerException {
		return getAcquisitionController().parseAcquisitionConfiguration(url);
	}

	@Override
	public void deleteAcquisitionConfiguration(URL url) throws AcquisitionControllerException {
		getAcquisitionController().deleteAcquisitionConfiguration(url);
	}

	@Override
	public void releaseResources() {
		getAcquisitionController().releaseResources();
	}

	public AcquisitionController<ScanningAcquisition> getAcquisitionController() {
		if (acquisitionController == null) {
			setAcquisitionController((AcquisitionController<ScanningAcquisition>) getBean("scanningAcquisitionController", acquisitionType));
		}
		return acquisitionController;
	}

	public void setAcquisitionController(AcquisitionController<ScanningAcquisition> acquisitionController) {
		this.acquisitionController = acquisitionController;
	}
}
