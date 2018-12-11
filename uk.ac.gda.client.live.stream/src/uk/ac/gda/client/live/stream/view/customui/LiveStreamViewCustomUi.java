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
import uk.ac.gda.client.live.stream.view.LiveStreamView;

/**
 * <p>This interface allows you to define a custom UI class for use with an {@link LiveStreamView}.</p>
 *
 * <p>Instead of implementing this directly it is recommended to extend {@link AbstractLiveStreamViewCustomUi}</p>
 *
 * @since GDA 9.11
 * @author James Mudd
 */
public interface LiveStreamViewCustomUi {

	/**
	 * Called when the {@link LiveStreamView} is drawing this custom UI. At this point the {@link IPlottingSystem} and {@link LiveStreamConnection} will be setup so then can be used in this method.
	 *
	 * @param composite The composite to draw custom UI onto.
	 */
	public void createUi(Composite composite);

	/**
	 * Called when the composite is being disposed to allow listeners to be removed etc.
	 */
	public void dispose();

	/**
	 * Called by {@link LiveStreamView} when creating the custom UI to inject the {@link IPlottingSystem}
	 *
	 * @param plottingSystem the plotting system displaying the live stream.
	 */
	public void setPlottingSystem(IPlottingSystem<Composite> plottingSystem);

	/**
	 * Called by {@link LiveStreamView} when creating the custom UI to inject {@link LiveStreamConnection}
	 *
	 * @param liveStreamConnection the connection to the live stream displayed.
	 */
	public void setLiveStreamConnection(LiveStreamConnection liveStreamConnection);

	/**
	 * Called by {@link LiveStreamView} when creating the custom UI to inject the {@link IImageTrace}
	 *
	 * @param iTrace trace of the live stream displayed.
	 */
	public void setImageTrace(IImageTrace iTrace);

	/**
	 * Called by {@link LiveStreamView} when creating the custom UI to inject the {@link IActionBars}
	 *
	 * @param actionBars toolbar of the {@link LiveStreamView}
	 */
	public void setActionBars(IActionBars actionBars);

}
