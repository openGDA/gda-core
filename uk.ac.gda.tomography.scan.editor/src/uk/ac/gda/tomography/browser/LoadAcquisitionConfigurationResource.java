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

package uk.ac.gda.tomography.browser;

import org.eclipse.jface.action.Action;

import gda.rcp.views.Browser;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.AcquisitionControllerException;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.tomography.base.TomographyParameterAcquisition;

/**
 * Defines a load operation to be used by a {@link Browser} row.
 *
 * @author Maurizio Nagni
 */
class LoadAcquisitionConfigurationResource extends Action {
	private final AcquisitionConfigurationResource<TomographyParameterAcquisition> resource;
	private final AcquisitionController<TomographyParameterAcquisition> controller;

	public LoadAcquisitionConfigurationResource(AcquisitionConfigurationResource<TomographyParameterAcquisition> resource,
			AcquisitionController<TomographyParameterAcquisition> controller) {
		super("Deleted resource");
		this.resource = resource;
		this.controller = controller;
	}

	@Override
	public void run() {
		try {
			controller.loadAcquisitionConfiguration(resource);
		} catch (AcquisitionControllerException e) {
		}
	}
}
