/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.sampletransfer;

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.displayError;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public class SampleTransferHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			final Shell activeShell = Display.getCurrent().getActiveShell();
			final SampleTransferCameras cameraList = Finder.find("sample_transfer_cameras");
			final List<CameraConfiguration> cameras = cameraList.getCameras();
			final SampleTransferDialog dialog = new SampleTransferDialog(activeShell, cameras);
			dialog.create();
			dialog.open();
		} catch (Exception e) {
			displayError("Error configuring Sample Transfer System", "Cannot open sample transfer system dialog", e, logger);
		}
		return null;
	}


}
