/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;

/**
 * The base class for controllers associated with mapping scan views which sets up initialisation and makes the
 * {@link IMappingExperimentBeanProvider} available.
 *
 * @since GDA 9.13
 */
public abstract class AbstractMappingController {

	protected IMappingExperimentBeanProvider mappingExperimentBeanProvider;

	protected AtomicBoolean initialised =  new AtomicBoolean(false);

	/**
	 * Default minimal initialise method to ensure subclass initialisation is carried out within the atomic check and
	 * to make the mapping bean available. All subclasses must call this as the first line of their initialise methods
	 * and then override the {@link #oneTimeInitialisation()} method to perform their own one-time initialisation
	 * steps if necessary. ll this is controlled by the
	 * {@link #initialised} {@link AtomicBoolean} to ensure it can only happen once. If anything is thrown during this
	 * process. the flag is reset.
	 */
	public void initialise() {
		if (initialised.compareAndSet(false, true)) {
			try {
				mappingExperimentBeanProvider = getService(IMappingExperimentBeanProvider.class);
				oneTimeInitialisation();
			} catch (Exception e) {
				initialised.set(false);
				throw e;
			}
		}
	}

	/**
	 * Sub classes must implement this method to carry out their own on time initialisation task (if the have any to
	 * ensure that they are carried out inder the control of the {@link #initialised} flag.
	 */
	protected abstract void oneTimeInitialisation();

	protected IMappingExperimentBean getMappingBean() {
		checkInitialised();
		return mappingExperimentBeanProvider.getMappingExperimentBean();
	}

	protected void checkInitialised() {
		if (!initialised.get()) {
			throw new IllegalStateException("Controller is not initialised");
		}
	}

	public <S> S getService(Class<S> serviceClass) {
		return PlatformUI.getWorkbench().getService(serviceClass);
	}
}
