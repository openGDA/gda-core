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

package uk.ac.gda.client.live.stream.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.SnapshotView;

/**
 * A handler to update a {@link SnapshotView} with the current data of the {@link LiveStreamView}.
 */
public class SnapshotFromLiveStreamHandler extends AbstractHandler {

	public static final String SNAPSHOT_DATA = "Snapshot Data";
	private static final Logger logger = LoggerFactory.getLogger(SnapshotFromLiveStreamHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final LiveStreamView liveStreamView = (LiveStreamView) HandlerUtil.getActivePart(event);
		final SnapshotData snapshot = liveStreamView.getSnapshot();
		try {
			final SnapshotView snapshotView = (SnapshotView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(SnapshotView.ID);
			Display.getDefault().asyncExec(() -> updateSnapshotView(snapshotView, snapshot));
			return null;
		} catch (PartInitException e) {
			logger.error("View '{}'cannot be initialised: ", e.getMessage());
			throw new ExecutionException("View '" + SnapshotView.ID + "' Cannot be initialised.", e);
		}
	}

	private void updateSnapshotView(SnapshotView snapshotView, SnapshotData snapshotData) {
		// perform the update of the snapshot view with the given dataset
		final IPlottingSystem<?> plottingSystem = snapshotView.getPlottingSystem();
		plottingSystem.clear();
		plottingSystem.updatePlot2D(snapshotData.getDataset(), null, SNAPSHOT_DATA, new NullProgressMonitor());
		plottingSystem.setTitle(snapshotData.getTitle());
	}

}
