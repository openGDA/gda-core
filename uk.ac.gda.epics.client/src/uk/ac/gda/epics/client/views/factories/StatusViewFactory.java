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

package uk.ac.gda.epics.client.views.factories;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.epics.client.views.StatusView;
import uk.ac.gda.epics.client.views.StatusViewController;


/**
 *
 */
public class StatusViewFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(StatusViewFactory.class);

	private StatusViewController statusViewController;
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

	public StatusViewController getStatusViewController() {
		return statusViewController;
	}

	public void setStatusViewController(StatusViewController statusViewController) {
		this.statusViewController = statusViewController;
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
		StatusView statusView = new StatusView();
		statusView.setViewPartName(viewPartName);
		statusView.setStatusViewController(statusViewController);
		return statusView;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException {
		System.out.println("set initialization data called");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (statusViewController == null) {
			throw new IllegalArgumentException("statusViewController not set");
		}
		if (viewPartName == null) {
			throw new IllegalArgumentException("viewPartName not set");
		}
	}
}
