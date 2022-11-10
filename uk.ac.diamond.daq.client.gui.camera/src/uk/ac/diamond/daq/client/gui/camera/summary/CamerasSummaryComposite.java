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

package uk.ac.diamond.daq.client.gui.camera.summary;

import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getAllCameraConfigurationProperties;
import static uk.ac.diamond.daq.client.gui.camera.CameraHelper.getCameraMonitors;
import static uk.ac.gda.ui.tool.ClientMessages.CAMERA;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.MONITOR;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraConfigurationView;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;

/**
 * Displays a summary of the monitored cameras properties and allows to open the more general {@link CameraConfigurationView}
 *
 * @author Maurizio Nagni
 *
 */
public class CamerasSummaryComposite implements CompositeFactory {

	private Table table;

	@Override
	public Composite createComposite(final Composite parent, int style) {
		table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createClientGridDataFactory().applyTo(table);
		createTableColumns();

		getAllCameraConfigurationProperties().stream()
			.filter(p -> getCameraMonitors().contains(p.getId()))
			.forEach(detector -> new CameraSummaryRow(table, detector));

		// redistribute available width when the table is resized
		table.addListener(SWT.Resize, e -> resizeColumns());

		return table;
	}

	private void resizeColumns() {
		var tableWidth = table.getClientArea().width;
		// 50% for name
		table.getColumn(0).setWidth((int) (tableWidth * 0.5));

		// 30% for exposure controls
		table.getColumn(1).setWidth((int) (tableWidth * 0.3));

		// 20% for start/stop monitor button
		table.getColumn(2).setWidth((int) (tableWidth * 0.2));
	}

	private void createTableColumns() {
		createColumn(CAMERA);
		createColumn(EXPOSURE);
		createColumn(MONITOR);
		resizeColumns();
	}

	private void createColumn(ClientMessages heading) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(ClientMessagesUtility.getMessage(heading));
	}
}
