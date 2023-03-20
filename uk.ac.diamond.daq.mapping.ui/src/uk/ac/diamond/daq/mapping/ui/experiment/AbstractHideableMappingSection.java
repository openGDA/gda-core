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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

/**
 * Abstract class that can be used as a base for classes that implement {@link HideableViewSection}
 */
public abstract class AbstractHideableMappingSection extends AbstractMappingSection implements HideableViewSection<IMappingExperimentBean, MappingExperimentView> {

	protected Composite content;

	private boolean visible = false;

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public void setVisible(boolean visible) {
		this.visible = visible;
		setContentVisibility();
	}

	protected void setContentVisibility() {
		if (content != null) {
			setSeparatorVisibility(visible);
			content.setVisible(visible);
			((GridData) content.getLayoutData()).exclude = !visible;
		}
	}
}
