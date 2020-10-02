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

package uk.ac.diamond.daq.mapping.ui.services;

import org.dawnsci.mapping.ui.api.IMapFileController;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.PlatformUI;
import org.springframework.stereotype.Service;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;
import uk.ac.diamond.daq.mapping.api.IScanBeanSubmitter;
import uk.ac.diamond.daq.mapping.ui.experiment.RegionAndPathController;
import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;
import uk.ac.gda.ui.tool.spring.ClientRemoteServices;

/**
 * Provides various remote services.
 * This class
 * <ul>
 * <li>
 *  consolidates any client {@code PlatformUI.getWorkbench()} invocation
 * </li>
 * <li>
 *  being annotated as @Service, can be injected in other Spring component using @Autowire
 * </li>
 * <li>
 *  Spring enabled test can mock it in a natural way
 * </li>
 * </ul>
 *
 * @author Maurizio Nagni
 *
 * @see ClientRemoteServices
 */
@Service
public class MappingRemoteServices {

	private MappingRemoteServices() {}

	public RegionAndPathController getRegionAndPathController() {
		return getService(RegionAndPathController.class);
	}

	public ScanManagementController getScanManagementController() {
		return getService(ScanManagementController.class);
	}

	public IMappingExperimentBeanProvider getIMappingExperimentBeanProvider() {
		return getService(IMappingExperimentBeanProvider.class);
	}

	public IScanBeanSubmitter getIScanBeanSubmitter() {
		return getService(IScanBeanSubmitter.class);
	}

	public IEclipseContext getIEclipseContext() {
		return getService(IEclipseContext.class);
	}

	public IMapFileController getIMapFileController() {
		return getService(IMapFileController.class);
	}

	private <T> T getService(Class<T> klass) {
		return PlatformUI.getWorkbench().getService(klass);
	}
}
