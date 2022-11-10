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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.innerComposite;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureTextField;
import uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButton;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Displays in tabular form some essential camera properties
 *
 * <p>
 * The actual implementations includes three columns: camera name, exposure time and a monitor widget
 * </p>
 *
 * @author Maurizio Nagni
 * @see ExposureTextField
 * @see CameraMonitorButton
 */
class CameraSummaryRow {

	private final TableItem tableItem;

	/**
	 * @param table
	 *            the {@link Table} where attach the {@link TableItem}
	 * @param cameraProperties
	 *            the camera properties
	 */
	public CameraSummaryRow(Table table, CameraConfigurationProperties cameraProperties) {
		this.tableItem = new TableItem(table, SWT.NONE);
		addColumns(cameraProperties);
	}

	private void addColumns(CameraConfigurationProperties cameraProperties) {
		Table table = tableItem.getParent();
		// column 1: detector name
		tableItem.setText(cameraProperties.getName());

		// column 2: exposure control
		TableEditor editor = new TableEditor(table);
		ICameraConfiguration iCameraConfiguration = CameraHelper.createICameraConfiguration(cameraProperties);

		Text exposureText = new ExposureTextField(table, () -> iCameraConfiguration).getExposure();
		createClientGridDataFactory().applyTo(exposureText);
		editor.grabHorizontal = true;
		editor.setEditor(exposureText, tableItem, 1);

		// column 3: start/stop monitor button
		editor = new TableEditor(table);
		Composite container = innerComposite(table, 1, false);
		var monitor = new CameraMonitorButton(iCameraConfiguration).draw(container);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(monitor);

		editor.grabHorizontal = true;
		editor.setEditor(container, tableItem, 2);

	}
}