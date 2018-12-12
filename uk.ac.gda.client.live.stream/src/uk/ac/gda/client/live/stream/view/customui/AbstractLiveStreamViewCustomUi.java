/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.client.live.stream.view.customui;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;

import uk.ac.gda.client.live.stream.LiveStreamConnection;

/**
 * Abstract base class for implementing {@link LiveStreamViewCustomUi}.
 *
 * @author James Mudd
 * @since GDA 9.11
 */
public abstract class AbstractLiveStreamViewCustomUi implements LiveStreamViewCustomUi {

	private LiveStreamConnection liveStreamConnection;

	private IPlottingSystem<Composite> plottingSystem;

	private IImageTrace imageTrace;

	private IActionBars actionBars;

	@Override
	public void setLiveStreamConnection(LiveStreamConnection liveStreamConnection) {
		this.liveStreamConnection = liveStreamConnection;
	}

	public LiveStreamConnection getLiveStreamConnection() {
		return liveStreamConnection;
	}

	@Override
	public void setPlottingSystem(IPlottingSystem<Composite> plottingSystem) {
		this.plottingSystem = plottingSystem;
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	@Override
	public void setImageTrace(IImageTrace trace) {
		this.imageTrace = trace;
	}

	public IImageTrace getImageTrace() {
		return imageTrace;
	}

	@Override
	public void setActionBars(IActionBars actionBars) {
		this.actionBars = actionBars;
	}

	public IActionBars getActionBars() {
		return actionBars;
	}

	@Override
	public void dispose() {
		// No-op dispose.
	}

}
