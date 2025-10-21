/*-
 * Copyright © 2025 Diamond Light Source Ltd.
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

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.livecontrol.CompositeFactory;
import uk.ac.gda.client.livecontrol.DialogFactory;

public class SampleTransferDialogFactory implements DialogFactory {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferDialogFactory.class);

	private List<CameraConfiguration> cameras;
	private List<CompositeFactory> compositeFactories;

	public void setCameras(List<CameraConfiguration> cameras) {
		this.cameras = cameras;
	}

	public void setCompositeFactories(List<CompositeFactory> compositeFactories) {
		this.compositeFactories = compositeFactories;
	}

	@Override
	public TrayDialog create(Shell shell) {
		try {
		    return new SampleTransferDialog(shell, cameras, compositeFactories);
		} catch (IllegalArgumentException e) {
			logger.warn("Invalid arguments provided for dialog configuration", e);
		    MessageDialog.openError(shell, "Invalid Input", e.getMessage());
		} catch (Exception ex) {
	        logger.error("Unexpected error while opening dialog", ex);
	        MessageDialog.openError(shell, "Unexpected Error", "Something went wrong.");
	    }
		return null;
	}
}
