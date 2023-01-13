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

package uk.ac.gda.epics.client.pixium.views.factories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.client.pixium.views.PixiumView;
import uk.ac.gda.epics.client.pixium.views.PixiumViewController;

/**
 *
 */
public class PixiumViewFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(PixiumViewFactory.class);

	private PixiumViewController pixiumViewController;
	private String viewPartName;
	private String name;

	@Override
	public void setName(String name) {
		this.name = name;

	}

	@Override
	public String getName() {
		return name;
	}

	public PixiumViewController getPixiumViewController() {
		return pixiumViewController;
	}

	public void setPixiumViewController(PixiumViewController viewController) {
		this.pixiumViewController = viewController;
	}

	public String getViewPartName() {
		return viewPartName;
	}

	public void setViewPartName(String viewPartName) {
		this.viewPartName = viewPartName;
	}

	@Override
	public Object create() throws CoreException {
		logger.info("Creating status view class");
		PixiumView view = new PixiumView();
		view.setViewPartName(viewPartName);
		view.setPixiumViewController(pixiumViewController);
		return view;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		System.out.println("set initialization data called");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (pixiumViewController == null) {
			throw new IllegalArgumentException("pixiumViewController not set");
		}
		if (viewPartName == null) {
			throw new IllegalArgumentException("viewPartName not set");
		}
	}
}
