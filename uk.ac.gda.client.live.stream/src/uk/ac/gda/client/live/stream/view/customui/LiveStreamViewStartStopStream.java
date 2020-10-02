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

package uk.ac.gda.client.live.stream.view.customui;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.LiveStreamView;

public class LiveStreamViewStartStopStream extends AbstractLiveStreamViewCustomUi {
	private static Logger logger = LoggerFactory.getLogger(LiveStreamViewStartStopStream.class);

	@Override
	public void createUi(Composite composite) {
		Composite mainComposite = new Composite(composite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(mainComposite);

		final Button startStreamButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(startStreamButton);
		startStreamButton.setText("Start stream");
		startStreamButton.addSelectionListener(widgetSelectedAdapter(e -> connect()));

		// Start/stop acquisition
		final Button stopStreamButton = new Button(mainComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(stopStreamButton);
		stopStreamButton.setText("Stop stream");
		stopStreamButton.addSelectionListener(widgetSelectedAdapter(e -> disconnect()));
	}

	/**
	 * Open the live stream view again to reconnect everything.
	 */
	private void connect() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LiveStreamView viewPart = (LiveStreamView) activePage.getActivePart();
		viewPart.reopenView();
	}

	/**
	 * Disconnect from the live stream
	 */
	private void disconnect() {
		if (!getLiveStreamConnection().isConnected()) {
			return;
		}
		try {
			getLiveStreamConnection().disconnect();
		} catch (LiveStreamException e) {
			logger.error("Problem disconnecting from live stream", e);
		}
	}

}
