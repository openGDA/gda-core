/*-
 * Copyright © 2010 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.tomography.datacollection.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import gda.rcp.views.JythonTerminalView;
import uk.ac.diamond.daq.tomography.datacollection.ui.views.DataCollectionMJPegView;
import uk.ac.diamond.daq.tomography.datacollection.ui.views.DataCollectionPCOArrayView;
import uk.ac.diamond.daq.tomography.datacollection.ui.views.DataCollectionPCOHistogramView;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.liveplot.LivePlotView;

/**
 * This defines the Data Collection perspective, originally in I13 Imaging
 * bundle.<br>
 * To make this perspective available in your client, include
 * uk.ac.diamond.daq.tomography.datacollection.ui.feature
 * <p>
 * In addition, you should ensure that you have:
 * <p>
 * - A bean called "statusView" that defines a
 * CompositeFactoryExecutableExtension. The details of this are
 * beamline-specific, but it is intended to show the status of the Ring and, to
 * look good on the screen, should be only 2-3 rows high, though it can fill the
 * whole window width-wise. For an example, see statusView.xml in i13i-config.
 * <p>
 * - A bean that defines a DataCollectionADControllerImpl. This will be
 * beamline-specific, but for an example, see adController.xml in i13i-config.
 * <p>
 * - An OSGi service named dataCollectionADService that makes the AD controller
 * available. For an example, see osgi_services.xml in i13i-config.
 */
public class DataCollectionPerspective implements IPerspectiveFactory {

	private static final String STATUS_VIEW_ID = "uk.ac.diamond.daq.tomography.datacollection.ui.views.StatusView";

	private IPageLayout factory;

	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		addViews();
		addActionSets();
		addNewWizardShortcuts();
		addViewShortcuts();
	}

	private void addViews() {
		// Creates the overall folder layout.
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		String editorArea = factory.getEditorArea();
		factory.setEditorAreaVisible(false);

		factory.addStandaloneView(STATUS_VIEW_ID, false, IPageLayout.TOP, 0.15f, editorArea);
		IViewLayout statusLayout = factory.getViewLayout(STATUS_VIEW_ID);
		statusLayout.setCloseable(false);
		statusLayout.setMoveable(false);

		IFolderLayout left = factory.createFolder("left", IPageLayout.LEFT, 0.95f, editorArea);
		left.addView(DataCollectionMJPegView.ID);

		IFolderLayout rightTop = factory.createFolder("rightTop", IPageLayout.RIGHT, (float) 0.50, "left");
		rightTop.addView("uk.ac.diamond.daq.tomography.datacollection.ui.DetectorPlot");
		rightTop.addPlaceholder("uk.ac.diamond.daq.tomography.datacollection.ui.NormalisedImage");
		rightTop.addPlaceholder(LivePlotView.ID);
		rightTop.addPlaceholder("uk.ac.gda.video.views.cameraview");
		rightTop.addPlaceholder(DataCollectionPCOHistogramView.ID);
		rightTop.addPlaceholder(DataCollectionPCOArrayView.Id);

		IFolderLayout rightBottom = factory.createFolder("rightBottom", IPageLayout.BOTTOM, (float) 0.5, "rightTop");
		rightBottom.addView(JythonTerminalView.ID);
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view");
		rightBottom.addPlaceholder("data.dispenser.browser");
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view:data.dispenser.browser");
		rightBottom.addPlaceholder(CommandQueueViewFactory.ID);
		rightBottom.addPlaceholder("org.dawb.workbench.plotting.views.toolPageView.*");
	}

	private void addActionSets() {
		factory.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
	}

	private void addNewWizardShortcuts() {
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		factory.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");
	}

	private void addViewShortcuts() {
		factory.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		factory.addShowViewShortcut(IPageLayout.ID_OUTLINE);
	}
}
