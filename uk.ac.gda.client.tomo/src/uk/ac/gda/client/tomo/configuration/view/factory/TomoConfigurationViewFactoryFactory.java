/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.configuration.view.factory;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;
import uk.ac.gda.client.tomo.configuration.view.TomoConfigurationView;

/**
 * Factory method that invokes the View object
 */
public class TomoConfigurationViewFactoryFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(TomoConfigurationViewFactoryFactory.class);
	private String viewPartName;
	private String name;

	private TomoAlignmentViewController tomoAlignmentViewController;

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("Creating tomoalignment view");
		TomoConfigurationView tomographyAlignmentView = new TomoConfigurationView();
		tomographyAlignmentView.setViewPartName(viewPartName);
		return tomographyAlignmentView;
	}

	public TomoAlignmentViewController getTomoAlignmentViewController() {
		return tomoAlignmentViewController;
	}

	public void setTomoAlignmentViewController(TomoAlignmentViewController tomoAlignmentViewController) {
		this.tomoAlignmentViewController = tomoAlignmentViewController;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
	}

	@Override
	public void afterPropertiesSet() throws Exception {

	}

}