/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.ui.AbstractViewSection;

public abstract class AbstractMappingSection
		extends AbstractViewSection<IMappingExperimentBean, MappingExperimentView> {

	// this class implements no methods, but is useful for binding the type variables, avoiding having to do this for each subclass

	protected DataBindingContext dataBindingContext;

	/**
	 * Remove all existing bindings in {@link #dataBindingContext}
	 */
	@Override
	protected void removeOldBindings() {
		if (dataBindingContext == null) {
			return;
		}

		// copy the bindings to prevent concurrent modification exception
		final List<Binding> bindings = new ArrayList<>(dataBindingContext.getBindings());
		for (Binding binding : bindings) {
			dataBindingContext.removeBinding(binding);
			binding.dispose();
		}
	}

}
