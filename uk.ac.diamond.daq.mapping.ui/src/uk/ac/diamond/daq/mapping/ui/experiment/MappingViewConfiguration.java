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

import java.util.List;
import java.util.Objects;

import gda.factory.ConfigurableBase;
import gda.factory.FactoryException;
import uk.ac.diamond.daq.osgi.OsgiService;

@OsgiService(MappingViewConfiguration.class)
public class MappingViewConfiguration extends ConfigurableBase {

	private List<AbstractMappingSection> scrolledSections;
	private List<AbstractMappingSection> unscrolledSections;

	@Override
	public void configure() throws FactoryException {
		Objects.requireNonNull(scrolledSections, "Scrolled sections must be configured");
		Objects.requireNonNull(unscrolledSections, "Unscrolled sections must be configured");
		setConfigured(true);
	}

	public List<AbstractMappingSection> getScrolledSections() {
		return scrolledSections;
	}

	public void setScrolledSections(List<AbstractMappingSection> scrolledSections) {
		this.scrolledSections = scrolledSections;
	}

	public List<AbstractMappingSection> getUnscrolledSections() {
		return unscrolledSections;
	}

	public void setUnscrolledSections(List<AbstractMappingSection> unscrolledSections) {
		this.unscrolledSections = unscrolledSections;
	}

}
