/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.browser;

import gda.rcp.views.Browser;
import gda.rcp.views.TreeViewerBuilder;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.ui.browser.NameLabelProvider;
import uk.ac.diamond.daq.mapping.ui.browser.ScanningAcquisitionBrowserBase;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResource;
import uk.ac.gda.api.acquisition.resource.AcquisitionConfigurationResourceType;
import uk.ac.gda.client.composites.AcquisitionsBrowserCompositeFactory;
import uk.ac.gda.tomography.browser.provider.ExposureLabelProvider;
import uk.ac.gda.tomography.browser.provider.ProjectionsLabelProvider;
import uk.ac.gda.tomography.browser.provider.RangeProvider;

/**
 * Generates a {@link Browser} for the tomography configuration files, suitable for an {@link AcquisitionsBrowserCompositeFactory}
 *
 * @author Maurizio Nagni
 */
public class TomoBrowser extends ScanningAcquisitionBrowserBase {

	private static final int NAME_WIDTH = 250;
	private static final int DETAILS_WIDTH = 100;

	public TomoBrowser(AcquisitionController<ScanningAcquisition> controller) {
		super(AcquisitionConfigurationResourceType.TOMO, controller);
	}

	@Override
	public void addColumns(TreeViewerBuilder<AcquisitionConfigurationResource<ScanningAcquisition>> builder) {
		builder.addColumn("Name", NAME_WIDTH, new NameLabelProvider());
		builder.addColumn("Range", DETAILS_WIDTH, new RangeProvider());
		builder.addColumn("Projections", DETAILS_WIDTH, new ProjectionsLabelProvider());
		builder.addColumn("Exposure (s)", DETAILS_WIDTH, new ExposureLabelProvider());
	}

}
