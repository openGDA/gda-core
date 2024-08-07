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

package uk.ac.diamond.daq.client.gui.camera.roi;

import java.util.UUID;

import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.plotting.api.region.IROIListener;
import org.eclipse.dawnsci.plotting.api.region.ROIEvent;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.client.gui.camera.event.ROIChangeEvent;
import uk.ac.gda.client.live.stream.view.LivePlottingComposite;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * Listen to {@link ROIEvent} messages and republishes as {@link ROIChangeEvent}
 * messages using Spring. On receiving {@link ROIEvent}, the listener does not
 * propagate any message not matching the original roi name.
 *
 * @author Maurizio Nagni
 *
 */
public class ROIListener implements IROIListener {

	private final Composite parent;
	private final LivePlottingComposite livePlottingComposite;
	private final UUID regionID;

	/**
	 *
	 * @param parent                the Composite containing the ROI element. This
	 *                              parent is used as reference to discriminate
	 *                              between multiple {@link ROIChangeEvent}
	 * @param livePlottingComposite the graphical object where the ROI has been
	 *                              drawn
	 * @param regionID              The UUID of the region generating this event
	 */
	public ROIListener(Composite parent, LivePlottingComposite livePlottingComposite, UUID regionID) {
		this.parent = parent;
		this.livePlottingComposite = livePlottingComposite;
		this.regionID = regionID;
	}

	@Override
	public void roiDragged(ROIEvent event) {
		if (isSameROI(event)) {
			publishEvent(event);
		}
	}

	/**
	 * Intercepts {@link ROIEvent} nd publishes {@link ROIChangeEvent} using Spring
	 */
	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.dawnsci.plotting.api.region.IROIListener#roiChanged(org.eclipse.
	 * dawnsci.plotting.api.region.ROIEvent)
	 */
	@Override
	public void roiChanged(ROIEvent event) {
		if (isSameROI(event)) {
			publishEvent(event);
		}
	}

	@Override
	public void roiSelected(ROIEvent event) {
		if (isSameROI(event)) {
			// do nothing
		}
	}

	private boolean isSameROI(ROIEvent event) {
		if (event.getROI() == null || event.getROI().getName() == null) {
			return false;
		}
		return event.getROI().getName().equals(regionID.toString());
	}

	private void publishEvent(ROIEvent event) {
		if (livePlottingComposite.getITrace() == null) {
			return;
		}
		RectangularROI roi = ((RectangularROI) event.getROI().getBounds()).copy();
		ROIChangeEvent intEvent = new ROIChangeEvent(this, roi, livePlottingComposite.getITrace().getData(),
				ClientSWTElements.findParentUUID(parent));
		SpringApplicationContextProxy.publishEvent(intEvent);
	}
}
