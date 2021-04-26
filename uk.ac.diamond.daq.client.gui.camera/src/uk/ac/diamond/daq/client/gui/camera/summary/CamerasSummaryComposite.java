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

import java.util.stream.IntStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.CameraConfigurationView;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;
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

	public CamerasSummaryComposite() {
	}

	@Override
	public Composite createComposite(final Composite parent, int style) {
		// Creates a table
		table = new Table(parent, SWT.VIRTUAL | SWT.BORDER);
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createClientGridDataFactory().applyTo(table);
		createTableColumns(table);

		getAllCameraConfigurationProperties().stream()
			.filter(p -> getCameraMonitors().contains(p.getId()))
			.forEach(this::createTableRow);

		return table;
	}

	private void createTableRow(CameraConfigurationProperties cameraProperties) {
		new CameraSummaryRow(table, cameraProperties);
	}

	private void createTableColumns(Table table) {
		ClientMessages[] headers = { CAMERA, EXPOSURE, MONITOR };
		IntStream.range(0, headers.length).forEach(c -> {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
			column.setText(ClientMessagesUtility.getMessage(headers[c]));
		});
	}
}
