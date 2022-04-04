/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import org.eclipse.scanning.api.event.EventConstants;
import org.eclipse.scanning.api.event.status.StatusBean;
import org.eclipse.scanning.event.ui.view.StatusQueueView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.ui.experiment.MappingExperimentView;

public class TensorTomoScanPerspective implements IPerspectiveFactory {

	public static final String ID = "uk.ac.gda.beamline.i22.i22tensortomoperspective";

	private static final Logger logger = LoggerFactory.getLogger(TensorTomoScanPerspective.class);

	@Override
	public void createInitialLayout(IPageLayout layout) {
		logger.trace("Building Tensor Tomography perspective");
		layout.setEditorAreaVisible(false);

		final IFolderLayout topLeft = layout.createFolder("mappeddata", IPageLayout.LEFT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		topLeft.addView("org.dawnsci.mapping.ui.mappeddataview");
		IViewLayout viewLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mappeddataview");
		viewLayout.setCloseable(false);

		// TODO: bottom left is a placeholder for the Tomography Scan Schemetic View
		final IFolderLayout bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.5f, "mappeddata");
		bottomLeft.addView(IPageLayout.ID_PROJECT_EXPLORER); // TODO: just a placeholder view, remove

		final IFolderLayout topMiddle = layout.createFolder("map", IPageLayout.RIGHT, 0.2f, IPageLayout.ID_EDITOR_AREA);
		topMiddle.addView("org.dawnsci.mapping.ui.mapview");
		viewLayout = layout.getViewLayout("org.dawnsci.mapping.ui.mapview");
		viewLayout.setCloseable(false);

		// The top right panel contains the I22 Tensor Tomo view and the
		final IFolderLayout topRight = layout.createFolder("scanSetup", IPageLayout.RIGHT, 0.58f, "map");
		topRight.addView(TensorTomoScanSetupView.ID);
		viewLayout = layout.getViewLayout(TensorTomoScanSetupView.ID);
		viewLayout.setCloseable(false);
		topRight.addView(MappingExperimentView.ID);
		viewLayout = layout.getViewLayout(MappingExperimentView.ID);
		viewLayout.setCloseable(false);

		// TODO: bottom middle is a placeholder for the Tomogram Visual Representation View
		final IFolderLayout bottomMiddle = layout.createFolder("visual", IPageLayout.BOTTOM, 0.5f, "map");
		bottomMiddle.addView(IPageLayout.ID_PROGRESS_VIEW); // TODO: just a placeholder view, remove

		final IFolderLayout bottomRight = layout.createFolder("jython", IPageLayout.BOTTOM, 0.5f, "params");
		bottomRight.addView("org.dawnsci.mapping.ui.spectrumview");
		viewLayout = layout.getViewLayout("org.dawnsci.mapping.ui.spectrumview");
		viewLayout.setCloseable(false);
		bottomRight.addView("gda.rcp.jythonterminalview");
		String queueViewId = StatusQueueView.createId(LocalProperties.get(LocalProperties.GDA_ACTIVEMQ_BROKER_URI, ""),
				"org.eclipse.scanning.api",
				StatusBean.class.getName(),
				EventConstants.STATUS_TOPIC,
				EventConstants.SUBMISSION_QUEUE);

		queueViewId = queueViewId + "partName=Queue";
		bottomRight.addView(queueViewId);

		bottomRight.addView("uk.ac.gda.client.livecontrol.LiveControlsView");
		bottomRight.addView("uk.ac.gda.client.liveplot.mjpeg.LiveMJPEGView");

		logger.trace("Finished building Tensor Tomography perspective");
	}

}
