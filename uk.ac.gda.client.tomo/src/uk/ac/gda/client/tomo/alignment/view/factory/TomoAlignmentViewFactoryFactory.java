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

package uk.ac.gda.client.tomo.alignment.view.factory;

import gda.rcp.views.FindableExecutableExtension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.alignment.view.TomoAlignmentView;
import uk.ac.gda.client.tomo.alignment.view.controller.TomoAlignmentViewController;

/**
 * Factory method that invokes the View object
 */
public class TomoAlignmentViewFactoryFactory implements FindableExecutableExtension {
	private final Logger logger = LoggerFactory.getLogger(TomoAlignmentViewFactoryFactory.class);
	private String viewPartName;
	private String name;
	/**
	 * The screen pixel size in mm.
	 */
	private Double screenPixelSize;

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
		TomoAlignmentView tomographyAlignmentView = new TomoAlignmentView();
		tomographyAlignmentView.setViewPartName(viewPartName);
		tomographyAlignmentView.setScreenPixelSize(screenPixelSize);
		tomographyAlignmentView.setTomoAlignmentViewController(tomoAlignmentViewController);
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

	/**
	 * @return Returns the screenPixelSize.
	 */
	public Double getScreenPixelSize() {
		return screenPixelSize;
	}

	/**
	 * @param screenPixelSize
	 *            The screenPixelSize to set.
	 */
	public void setScreenPixelSize(Double screenPixelSize) {
		this.screenPixelSize = screenPixelSize;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (screenPixelSize == null) {
			throw new IllegalArgumentException("'screenPixelSize' should be provided.");
		}
	}
}