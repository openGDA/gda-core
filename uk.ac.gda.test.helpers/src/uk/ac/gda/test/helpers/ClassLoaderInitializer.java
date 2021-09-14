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

package uk.ac.gda.test.helpers;

import static java.util.Collections.emptySet;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import uk.ac.diamond.daq.classloading.GDAClassLoaderService;

/**
 * Initializer to set the context's class loader allowing Spring tests to run in Equinox.
 */
public class ClassLoaderInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	@Override
	public void initialize(GenericApplicationContext applicationContext) {
		var classLoader = GDAClassLoaderService.getClassLoaderService().getClassLoaderForLibraryWithGlobalResourceLoading(GenericApplicationContext.class, emptySet());
		applicationContext.setClassLoader(classLoader);
		applicationContext.getBeanFactory().setBeanClassLoader(classLoader);
	}
}
