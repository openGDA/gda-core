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
import static uk.ac.diamond.daq.mapping.ui.sampletransfer.SampleTransferUtils.composite;

import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.live.stream.LiveStreamException;
import uk.ac.gda.client.live.stream.view.CameraConfiguration;

public class SampleTransferDialog extends TrayDialog {
	private static final Logger logger = LoggerFactory.getLogger(SampleTransferDialog.class);

	private List<CameraConfiguration> cameras;
	private CameraStreamViewer camerasViewer;

	private Composite composite;

	protected SampleTransferDialog(Shell shell, List<CameraConfiguration> cameras) {
		super(shell);
		setShellStyle(SWT.SHELL_TRIM | SWT.MIN);
		this.cameras = cameras;
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
        camerasViewer = new CameraStreamViewer(container, cameras);
        composite = composite(container, 1);
        setInitialComposite(composite);

        return container;
    }

	private void setInitialComposite(Composite parent) {
		composite = new SampleTransferComposite(parent);
	}

	@Override
	public boolean close() {
		disposeResources();
		return super.close();
	}

	private void disposeResources() {
		disposeCamerasView();
		composite.dispose();
	}

	private void disposeCamerasView() {
		camerasViewer.getPlottingComposites().forEach(Widget::dispose);
		camerasViewer.getStreamConnections().forEach(connection -> {
			try {
				connection.disconnect();
			} catch (LiveStreamException e) {
				logger.error("Error disconnecting camera", e);
			}
		});
	}

	@Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "OK", true);
    }
}
