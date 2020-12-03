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

package uk.ac.gda.ui.tool.rest;

import uk.ac.diamond.daq.experiment.api.structure.ExperimentController;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;

/**
 * Provides the client with access to various external rest services.
 *
 * @author Maurizio Nagni
 */
public class ClientRestServices {

	private ClientRestServices() {
	}

	/**
	 * Provides access to a {@link ExperimentController} service
	 *
	 * @return an experiment controller service
	 */
	public static final ExperimentController getExperimentController() {
		return SpringApplicationContextFacade.getBean(ExperimentControllerServiceClient.class);
	}

	/**
	 * Provides access to a {@code ScanningAcquisitionService}
	 *
	 * @return an scanning acquisition service
	 */
	public static final ScanningAcquisitionRestServiceClient getScanningAcquisitionRestServiceClient() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionRestServiceClient.class);
	}
}
