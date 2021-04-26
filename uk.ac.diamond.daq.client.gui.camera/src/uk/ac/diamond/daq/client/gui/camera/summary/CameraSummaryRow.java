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

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientEmptyCell;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.client.gui.camera.CameraHelper;
import uk.ac.diamond.daq.client.gui.camera.ICameraConfiguration;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureTextField;
import uk.ac.diamond.daq.client.gui.camera.monitor.widget.CameraMonitorButton;
import uk.ac.gda.client.exception.GDAClientException;
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
	private static final Logger logger = LoggerFactory.getLogger(CameraSummaryRow.class);

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

		TableEditor editor = new TableEditor(table);
		Label nameLabel = createClientLabel(table, SWT.NONE, cameraProperties.getName());
		GridDataFactory.fillDefaults().applyTo(nameLabel);
		editor.grabHorizontal = true;
		editor.setEditor(nameLabel, tableItem, 0);

		editor = new TableEditor(table);

		ICameraConfiguration iCameraConfiguration = CameraHelper.createICameraConfiguration(cameraProperties);

		Text exposureText = new ExposureTextField(table, SWT.NONE, iCameraConfiguration::getCameraControl).getExposure();
		createClientGridDataFactory().applyTo(exposureText);
		editor.grabHorizontal = true;
		editor.setEditor(exposureText, tableItem, 1);

		editor = new TableEditor(table);
		try {
			Composite container = createClientCompositeWithGridLayout(table, SWT.NONE, 10);
			createClientGridDataFactory().grab(true, true).applyTo(container);
			createClientEmptyCell(container, 5, 1);
			Button monitor = new CameraMonitorButton(container, iCameraConfiguration).getButton();
			createClientGridDataFactory().align(SWT.END, SWT.BOTTOM).grab(true, true).span(5, 1).applyTo(monitor);

			editor.grabHorizontal = true;
			editor.setEditor(container, tableItem, 2);

		} catch (GDAClientException e) {
			logger.error("Cannot create column", e);
		}
	}
}