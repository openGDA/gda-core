/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

package uk.ac.gda.client.liveplot;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.observable.IObserver;
import gda.rcp.GDAClientActivator;
import gda.scan.Scan;
import uk.ac.gda.preferences.PreferenceConstants;

public class LivePlotViewManager implements IObserver {

	private static final Logger logger = LoggerFactory.getLogger(LivePlotViewManager.class);

	/**
	 * Open the primary (default) live scan plot view. The default view to open is the normal live XY scan plot (ID
	 * uk.ac.gda.client.livescanplot) but a different view ID will be used if a value is set in the preference
	 * PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START_ID
	 *
	 * @return The primary live plot view, or <code>null</code> if there was an error activating the view
	 */
	public static IViewPart openPrimaryLivePlotView() {
		IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
		String viewId = preferenceStore.getString(PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START_ID);
		final String viewIdFinal = viewId != "" ? viewId : LivePlotView.ID;

		IViewPart view = null;
		try {
			view = getActiveWorkbenchPage().showView(viewIdFinal, null, IWorkbenchPage.VIEW_VISIBLE);
		} catch (PartInitException e) {
			logger.error("Error opening view '{}'", viewIdFinal, e);
		}
		return view;
	}

	private static IWorkbenchPage getActiveWorkbenchPage() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	/*
	 * At the start of a scan, check the preferences to see if we should open a live scan data plot, and if so, make
	 * sure one is visible and listening for new scan data points
	 */
	@Override
	public void update(Object source, Object arg) {
		if (arg instanceof Scan.ScanStatus
				&& ((Scan.ScanStatus) arg) == Scan.ScanStatus.RUNNING
				&& GDAClientActivator.getDefault().getPreferenceStore()
						.getBoolean(PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START)) {
			// must run sync to ensure the view is opened before a scan data point arrives
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					ensureAtLeastOneLivePlotViewIsConnectedAndVisible();
				}
			});
		}
	}

	private void ensureAtLeastOneLivePlotViewIsConnectedAndVisible() {
		List<LivePlotView> livePlots = getLivePlotViews();
		if (livePlots.size() == 0) {
			// There are no live plots; reopen the primary live plot view
			openPrimaryLivePlotView();
		} else {
			// There are live plot views; need to make sure one is connected and visible
			List<LivePlotView> connectedLivePlots = getConnectedLivePlotViews(livePlots);
			if (connectedLivePlots.size() > 0) {
				// At least one live plot is connected; make sure it is visible
				makeAtLeastOneViewVisible(connectedLivePlots);
			} else {
				// No live plots are connected; make sure one is visible and then connect it
				LivePlotView livePlot = makeAtLeastOneViewVisible(livePlots);
				if (livePlot != null) {
					livePlot.connect();
				} else {
					openPrimaryLivePlotView();
				}
			}
		}
	}

	private List<LivePlotView> getLivePlotViews() {
		IViewReference[] viewRefs = getActiveWorkbenchPage().getViewReferences();
		List<LivePlotView> livePlotViews = new ArrayList<LivePlotView>();
		for (IViewReference viewRef : viewRefs) {
			IViewPart view = viewRef.getView(false);
			if (view != null && view instanceof LivePlotView) {
				livePlotViews.add((LivePlotView) view);
			}
		}
		return livePlotViews;
	}

	private List<LivePlotView> getConnectedLivePlotViews(List<LivePlotView> livePlots) {
		List<LivePlotView> connectedLivePlots = new ArrayList<LivePlotView>();
		for (LivePlotView livePlot : livePlots) {
			if (livePlot.isConnected()) {
				connectedLivePlots.add(livePlot);
			}
		}
		return connectedLivePlots;
	}

	/**
	 * Make sure that at least one of the given list of views is visible in the active workbench window
	 *
	 * @param views
	 * @return A view that is now visible, or <code>null</code>
	 */
	private static <T extends IViewPart> T makeAtLeastOneViewVisible(List<T> views) {
		for (T view : views) {
			if (getActiveWorkbenchPage().isPartVisible(view)) {
				// A view is already visible; return it
				return view;
			}
		}
		// No views are visible; show the first for which a reference can be found
		for (T view : views) {
			IViewReference viewRef = (IViewReference) getActiveWorkbenchPage().getReference(view);
			if (viewRef != null) {
				try {
					getActiveWorkbenchPage().showView(viewRef.getId(), viewRef.getSecondaryId(),
							IWorkbenchPage.VIEW_VISIBLE);
					// A view has now been made visible; return it
					return view;
				} catch (PartInitException e) {
					logger.error("Error opening view '{}' with secondary ID '{}'", viewRef.getId(),
							viewRef.getSecondaryId(), e);
				}
			}
		}
		// No views could be made visible; return null
		return null;
	}
}
