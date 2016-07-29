/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.gda.client.microfocus.views;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExafsSelectionHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(ExafsSelectionHandler.class);

	@Override
	public void handleEvent(final Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// we handle only single-click events
		if (!mapClickEvent.isDoubleClick()) {
			ClickEvent clickEvent = mapClickEvent.getClickEvent();
			final double xLocation = clickEvent.getxValue();
			final double yLocation = clickEvent.getyValue();
			logger.debug("Received map click event with x={}, y={}",
					clickEvent.getxValue(), clickEvent.getyValue());

			final Double zLocation = getZLocation();

			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					updateExafsSelectionView(xLocation, yLocation, zLocation);
				}
			});
		}
	}

	private void updateExafsSelectionView(Double xLocation, Double yLocation, Double zLocation) {
		final Double[] locationArray = new Double[] { xLocation, yLocation, zLocation };

		ExafsSelectionView exafsSelectionView = (ExafsSelectionView) EclipseUtils.getActivePage()
				.findView(ExafsSelectionView.ID);
		if (exafsSelectionView != null) {
			exafsSelectionView.setSelectedPoint(locationArray);
		}
	}

	private Double getZLocation() {
		// FIXME: we need a way to get the z value. This may come from the
		// motor position, or from the nexus file. If the map is from a file, it may have been done
		// with a different stage than is current set up
		return 0d;
	}

}
