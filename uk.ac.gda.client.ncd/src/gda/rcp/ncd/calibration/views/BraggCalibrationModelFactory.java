/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package gda.rcp.ncd.calibration.views;

import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.gda.server.ncd.calibration.BraggCalibrationService;

public class BraggCalibrationModelFactory extends AbstractServiceFactory {
	private ClientBraggCalibrationModel model;
	@Override
	public synchronized Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		if (model == null) {
			model = new ClientBraggCalibrationModel(locator.getService(BraggCalibrationService.class));
		}
		return model;
	}

}
