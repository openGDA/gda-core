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

package uk.ac.diamond.daq.mapping.ui.experiment;

import org.dawnsci.mapping.ui.IMapClickEvent;
import org.eclipse.dawnsci.plotting.api.axis.ClickEvent;
import org.eclipse.scanning.api.ui.IStageScanConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import gda.device.Scannable;
import gda.factory.Finder;

public class StageMoveHandler implements EventHandler {

	private static final Logger logger = LoggerFactory.getLogger(StageMoveHandler.class);

	private IStageScanConfiguration stageConfiguration;

	public void setMappingStageConfiguration(IStageScanConfiguration stageConfiguration) {
		this.stageConfiguration = stageConfiguration;
	}

	@Override
	public void handleEvent(Event event) {
		final IMapClickEvent mapClickEvent = (IMapClickEvent) event.getProperty("event");
		// moveTo is only handled for double-clicks
		if (mapClickEvent.isDoubleClick()) {
			final ClickEvent clickEvent = mapClickEvent.getClickEvent();

			// moveTo needs to run in UI thread as it displays a dialog asking for confirmation
			PlatformUI.getWorkbench().getDisplay().asyncExec(()->moveTo(clickEvent.getxValue(), clickEvent.getyValue()));
		}
	}

	private void moveTo(final double xLocation, final double yLocation) {
		logger.debug("moveTo({}, {})", xLocation, yLocation);

		String fastName = stageConfiguration.getActiveFastScanAxis();
		String slowName = stageConfiguration.getActiveSlowScanAxis();

		// Dialog to confirm move
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		MessageBox dialog = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		dialog.setText("Go Here?");
		dialog.setMessage("Do you want to move the stage to:\n"
				+ fastName + " = " + xLocation + "\n"
				+ slowName + " = " + yLocation);

		if (dialog.open() == SWT.CANCEL) return;

		// Do the move
		try {
			Scannable fastAxis = Finder.getInstance().find(fastName);
			fastAxis.asynchronousMoveTo(xLocation);
			Scannable slowAxis = Finder.getInstance().find(slowName);
			slowAxis.asynchronousMoveTo(yLocation);
		} catch (DeviceException e) {
			logger.error("Error encountered while moving stage", e);
		}

	}

}
