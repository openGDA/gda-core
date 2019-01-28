/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBeanProvider;

/**
 * Subclasses {@link AbstractSectionsView} to show the standard set of views
 * required for a mapping scan.
 */
public class MappingExperimentView extends AbstractSectionsView {

	public static final String ID = "uk.ac.diamond.daq.mapping.ui.experiment.mappingExperimentView";

	private final MappingViewConfiguration mappingViewConfiguration;

	@Inject
	public MappingExperimentView(IMappingExperimentBeanProvider beanProvider) {
		super(beanProvider);
		mappingViewConfiguration = PlatformUI.getWorkbench().getService(MappingViewConfiguration.class);
		Objects.requireNonNull(mappingViewConfiguration, "Cannot get MappingViewConfiguration");
	}

	@Override
	protected List<IMappingSection> getScrolledSections() {
		return mappingViewConfiguration.getScrolledSections();
	}

	@Override
	protected List<IMappingSection> getUnscrolledSections() {
		return mappingViewConfiguration.getUnscrolledSections();
	}
}
