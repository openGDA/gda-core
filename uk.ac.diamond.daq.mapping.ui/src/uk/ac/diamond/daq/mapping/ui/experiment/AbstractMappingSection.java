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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

public abstract class AbstractMappingSection {

	protected final MappingExperimentView mappingView;
	protected final IMappingExperimentBean mappingBean;
	protected final IEclipseContext context;

	AbstractMappingSection(final MappingExperimentView mappingView, IEclipseContext context) {
		this.mappingView = mappingView;
		this.mappingBean = mappingView.getBean();
		this.context = context;
	}

	public boolean shouldShow() {
		return true;
	}

	public abstract void createControls(Composite parent);

	protected void updateStatusLabel() {
		mappingView.getStatusPanel().updateStatusLabel();
	}

	protected void setStatusMessage(String message) {
		mappingView.getStatusPanel().setMessage(message);
	}

}
