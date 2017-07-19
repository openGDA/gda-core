/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view;

import java.util.Arrays;
import java.util.List;

import org.eclipse.dawnsci.plotting.api.IPlottingService;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.eclipse.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotView extends ViewPart {

	public static final String ID="uk.ac.gda.client.live.stream.view.snapshotview";

	private static final Logger logger=LoggerFactory.getLogger(SnapshotView.class);

	private IPlottingSystem<Composite> plottingsystem;

	private Composite parent;

	private Text errorText;
	private static IPlottingService plottingService;

	public SnapshotView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		this.parent=parent;
		if (plottingService == null) {
			displayAndLogError(parent, "Cannot create Snapshot: no plotting service is available");
			return;
		}
		IActionBars actionBars = getViewSite().getActionBars();
		try {
			plottingsystem = plottingService.createPlottingSystem();
			plottingsystem.createPlotPart(parent, "Snapshot", actionBars, PlotType.IMAGE, this);
		} catch (Exception e) {
			displayAndLogError(parent, "Could not create plotting system", e);
			return;
		}
		for (IAxis axis : plottingsystem.getAxes()) {
			axis.setVisible(false);
		}
		// Add useful plotting system actions
		configureActionBars(actionBars);
		plottingsystem.setKeepAspect(true);
	}

	private void configureActionBars(IActionBars actionBars) {
		IToolBarManager toolBarManager = actionBars.getToolBarManager();

		// Setup the plotting system toolbar options
		List<String> toolBarIdsToBeRemoved = Arrays.asList(
				BasePlottingConstants.RESCALE,
				BasePlottingConstants.SNAP_TO_GRID,
				ToolbarConfigurationConstants.UNDO.getId());

		// Remove all ToolBar contributions with Ids which are either undefined or not required
		Arrays.stream(toolBarManager.getItems())
			.filter(ci -> ci.getId() == null || toolBarIdsToBeRemoved.stream().anyMatch(ci.getId()::contains))
			.forEach(toolBarManager::remove);
			// If getId() returns null then the match will not be performed as the || short circuits it, this
			// also prevents the NPE which would result from trying to match on a null Id.

		actionBars.updateActionBars();
	}

	@Override
	public void setFocus() {
		if (plottingsystem != null) {
			plottingsystem.setFocus();
		}
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingsystem;

	}

	public static synchronized void setPlottingService(IPlottingService plottingService) {
		logger.debug("Plotting service set to: {}", plottingService);
		SnapshotView.plottingService = plottingService;
	}


	private void displayAndLogError(final Composite parent, final String errorMessage) {
		displayAndLogError(parent, errorMessage, null);
	}
	private void displayAndLogError(final Composite parent, final String errorMessage, final Throwable throwable) {
		logger.error(errorMessage, throwable);
		if (errorText == null) {
			errorText = new Text(parent, SWT.LEFT | SWT.WRAP | SWT.BORDER);
			errorText.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					errorText.dispose();
					parent.layout(true);
					errorText=null;
				}
			});
			errorText.setToolTipText("Double click this message to remove it.");
			parent.layout(true);
		}
		StringBuilder s = new StringBuilder(errorText.getText());
		s.append("\n").append(errorMessage);
		if (throwable != null) {
			s.append("\n\t").append(throwable.getMessage());
		}
		errorText.setText(s.toString());
	}
	@Override
	// This method is required for the plotting tools to work.
	public <T> T getAdapter(final Class<T> clazz) {
		if (plottingsystem != null) {
			T adapter = plottingsystem.getAdapter(clazz);
			if (adapter != null) {
				return adapter;
			}
		}
		return super.getAdapter(clazz);
	}
	@Override
	public void dispose() {
		if (plottingsystem != null) {
			plottingsystem.dispose();
			plottingsystem = null;
		}
		super.dispose();
	}
}
