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

import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.COLOUR_GREY;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;
import uk.ac.gda.client.livecontrol.CompositeFactory;

public class SampleTransferDialog extends TrayDialog {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferDialog.class);

	private List<CameraConfiguration> cameras;
	private CameraStreamViewer camerasViewer;

	private List<CompositeFactory> compositeFactories;

	protected SampleTransferDialog(Shell shell, List<CameraConfiguration> cameras, List<CompositeFactory> compositeFactories) {
		super(shell);
		setShellStyle(SWT.SHELL_TRIM | SWT.MIN | SWT.APPLICATION_MODAL);

		if (cameras == null || cameras.isEmpty()) {
	        throw new IllegalArgumentException("Cameras list must not be null or empty.");
	    }

		if (compositeFactories == null || compositeFactories.isEmpty()) {
	        throw new IllegalArgumentException("Composite list must not be null or empty.");
	    }

		this.cameras = cameras;
		this.compositeFactories = compositeFactories;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Sample Transfer System");
		newShell.setBackground(COLOUR_GREY);
	}

	@Override
    protected Control createDialogArea(Composite parent) {
		final Composite container = (Composite) super.createDialogArea(parent);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(container);
        GridLayoutFactory.fillDefaults().margins(20, 10).numColumns(2).applyTo(container);

        try {
            camerasViewer = new CameraStreamViewer(container, cameras);
            if (!camerasViewer.hasActiveStreams()) {
                createNoStreamLabel(container);
            }
        } catch (Exception e) {
            logger.error("Error creating CameraStreamViewer, continuing without camera stream.", e);
            createNoStreamLabel(container);
        }

        try {
            compositeFactories.forEach(factory -> factory.createComposite(container));
        } catch (Exception e) {
            throw new DialogCreationException("Failed to create composite UI", e);
        }

        return container;
    }

	private void createNoStreamLabel(Composite container) {
        Label noStreamLabel = new Label(container, SWT.NONE);
        noStreamLabel.setText("Camera streaming unavailable");
        GridDataFactory.fillDefaults().span(2, 1).applyTo(noStreamLabel);
	}

	@Override
	public boolean close() {
		logger.info("Dialog is closing: disposing resources.");
		disconnectStreams();
		boolean closed = super.close(); // cleanup controls
		logger.info("Dialog closed: {}", closed);
	    return closed;
	}


	private void disconnectStreams() {
	    if (camerasViewer != null) {
	        logger.info("Disconnecting stream connections before dialog close.");
	        for (var connection : camerasViewer.getStreamConnections()) {
	            try {
	                connection.disconnect();
	            } catch (LiveStreamException e) {
	                logger.error("Error disconnecting stream", e);
	            }
	        }
	    }
	}

	@Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "OK", true);
    }

	public static class DialogCreationException extends RuntimeException {
        public DialogCreationException(String message) {
            super(message);
        }

        public DialogCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
