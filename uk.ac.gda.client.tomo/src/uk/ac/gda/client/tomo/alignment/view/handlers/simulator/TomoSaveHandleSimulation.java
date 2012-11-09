/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.alignment.view.handlers.simulator;

import gda.device.DeviceException;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import uk.ac.gda.client.tomo.alignment.view.controller.SaveableConfiguration;
import uk.ac.gda.client.tomo.alignment.view.handlers.ITomoConfigResourceHandler;
import uk.ac.gda.tomography.parameters.TomoExperiment;

public class TomoSaveHandleSimulation implements ITomoConfigResourceHandler {

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveConfiguration(IProgressMonitor monitor, SaveableConfiguration saveableConfiguration)
			throws DeviceException, InvocationTargetException, InterruptedException {
		// TODO Auto-generated method stub

	}

	@Override
	public TomoExperiment getTomoConfigResource(IProgressMonitor monitor, boolean shouldCreate)
			throws InvocationTargetException, InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

}
