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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.LiveStreamView;
import uk.ac.gda.client.live.stream.view.SnapshotView;

public class SnapshotFromLiveStream extends AbstractHandler {
	public static final String SNAPSHOT_DATA = "Snapshot Data";
	private static final Logger logger = LoggerFactory.getLogger(SnapshotFromLiveStream.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		LiveStreamView livestreamview = (LiveStreamView) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActivePart();
		SnapshotData snapshot = livestreamview.snapshot();
		try {
			IViewPart showView = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(SnapshotView.ID);
			if (showView instanceof SnapshotView) {
				SnapshotView snapshotview = (SnapshotView) showView;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						IPlottingSystem<Composite> plottingSystem = snapshotview.getPlottingSystem();
						plottingSystem.clear();
						plottingSystem.updatePlot2D(snapshot.getDataset(), null, SNAPSHOT_DATA, new NullProgressMonitor());
						plottingSystem.setTitle(snapshot.getTitle());
					}
				});
			}
		} catch (PartInitException e) {
			logger.error("View '{}'cannot be initialised: ", e.getMessage());
			throw new ExecutionException("View '" + SnapshotView.ID + "' Cannot be initialised.", e);
		}
		return null;
	}

}
