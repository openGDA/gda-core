/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.scripting.ui.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

import gda.rcp.views.JythonTerminalView;
import uk.ac.gda.client.CommandQueueViewFactory;
import uk.ac.gda.client.liveplot.LivePlotView;

/**
 * This defines the Scripting perspective, originally in I13 Imaging
 * bundle.<br>
 * To make this perspective available in your client, include
 * uk.ac.diamond.daq.scripting.ui.feature
 * <p>
 * In addition, you should ensure that you have:
 * <p>
 * - A bean called "statusView" that defines a
 * CompositeFactoryExecutableExtension. The details of this are
 * beamline-specific, but it is intended to show the status of the Ring and, to
 * look good on the screen, should be only 2-3 rows high, though it can fill the
 * whole window width-wise. For an example, see statusView.xml in i13-config.
 */
public class ScriptingPerspective implements IPerspectiveFactory {
	private static final String STATUS_VIEW_ID = "uk.ac.diamond.daq.scripting.ui.views.StatusView";
	private static final String DETECTOR_PLOT_VIEW_ID = "uk.ac.diamond.daq.scripting.ui.DetectorPlot";

	private IPageLayout factory;

	@Override
	public void createInitialLayout(IPageLayout factory) {
		this.factory = factory;
		factory.setEditorAreaVisible(false);

		addViews();
		addActionSets();
		addNewWizardShortcuts();
		addViewShortcuts();
	}

	private void addViews() {
		// Creates the overall folder layout.
		// Note that each new Folder uses a percentage of the remaining EditorArea.
		factory.addStandaloneView(STATUS_VIEW_ID, false, IPageLayout.TOP, 0.13f, factory.getEditorArea());
		IViewLayout statusLayout = factory.getViewLayout(STATUS_VIEW_ID);
		statusLayout.setCloseable(false);
		statusLayout.setMoveable(false);

		IFolderLayout left = factory.createFolder("left", IPageLayout.LEFT, (float) 0.20, factory.getEditorArea());
		left.addView(IPageLayout.ID_PROJECT_EXPLORER);

		IFolderLayout right = factory.createFolder("right", IPageLayout.RIGHT, (float) 0.50, factory.getEditorArea());
		right.addView(LivePlotView.ID);

		IFolderLayout rightBottom = factory.createFolder("rightBottom", IPageLayout.BOTTOM, (float) 0.50, "right");
		rightBottom.addView(DETECTOR_PLOT_VIEW_ID);
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view");
		rightBottom.addPlaceholder("data.dispenser.browser");
		rightBottom.addPlaceholder("org.eclipse.ui.browser.view:data.dispenser.browser");
		rightBottom.addPlaceholder("uk.ac.diamond.scisoft.analysis.rcp.plotViewDP");

		IFolderLayout middleBottom = factory.createFolder("middleBottom",
				IPageLayout.BOTTOM, 0.30f, factory.getEditorArea());
		middleBottom.addView(JythonTerminalView.ID);
		middleBottom.addPlaceholder(CommandQueueViewFactory.ID);
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
