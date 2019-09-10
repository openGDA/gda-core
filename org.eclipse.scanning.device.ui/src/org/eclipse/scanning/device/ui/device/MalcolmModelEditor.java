/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package org.eclipse.scanning.device.ui.device;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.AbstractModelEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MalcolmModelEditor extends AbstractModelEditor<IMalcolmModel> {

	private enum DetectorTableColumn {
		ENABLED("Enabled", false, 80, SWT.CENTER),
		DETECTOR_NAME("Name", false, 200, SWT.LEAD),
		FRAMES_PER_STEP("Frames per step", true, 120, SWT.CENTER),
		FRAME_TIME("Frame time", false, 120, SWT.CENTER), // calculated based on exposure time * frames-per-step
		EXPOSURE_TIME("Exposure time", true, 120, SWT.CENTER);

		public final String label;
		public final int columnWidth;
		public final int alignment;
		public final boolean editable;

		private DetectorTableColumn(String label, boolean editable, int columnWidth, int alignment) {
			this.label = label;
			this.columnWidth = columnWidth;
			this.editable = editable;
			this.alignment = alignment;
		}

	}

	private static final DetectorTableColumn[] DETECTOR_TABLE_COLUMNS = DetectorTableColumn.values();

	private static final Logger logger = LoggerFactory.getLogger(MalcolmModelEditor.class);

	private final DataBindingContext dataBindingContext = new DataBindingContext();

	private final IRunnableDeviceService runnableDeviceService;

	private TableViewer detectorsTable;

	public MalcolmModelEditor(IRunnableDeviceService runnableDeviceService, IMalcolmModel model) {
		this.runnableDeviceService = runnableDeviceService;
		setModel(model);
	}

	@Override
	public Composite createEditorPart(Composite parent) {
		final Composite composite = super.createEditorPart(parent);

		createStepTimeSection(composite);
		createDetectorsTable(composite);

		return composite;
	}

	private void createStepTimeSection(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Step time:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Text stepTimeText = new Text(parent, SWT.BORDER);
		stepTimeText.setToolTipText("The overall time for each frame of the scan");
		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(stepTimeText);
		IObservableValue<Double> textFieldValue = WidgetProperties.text(SWT.Modify).observe(stepTimeText);
		IObservableValue<Double> modelValue = PojoProperties.value("exposureTime").observe(getModel());
		dataBindingContext.bindValue(textFieldValue, modelValue);
		dataBindingContext.updateTargets();
	}

	private void createDetectorsTable(final Composite parent) {
		final Label label = new Label(parent, SWT.NONE);
		label.setText("Detectors:    (exposure time of 0 = use maximum)"); // TODO use a checkbox for maximum?
		GridDataFactory.swtDefaults().span(2, 1).applyTo(label);

		final boolean canEnableDetectors = canEnableDetectors();
		final Table table = new Table(parent, (canEnableDetectors ? SWT.CHECK : 0) | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		detectorsTable = canEnableDetectors ? new CheckboxTableViewer(table) : new TableViewer(table);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(SWT.DEFAULT, 150).applyTo(table);

		detectorsTable = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
//		ColumnViewerToolTipSupport.enableFor(detectorsTable); // TODO add tooltips

		// setup the context provider, label/checkstate provider and check state listener
		detectorsTable.setContentProvider(new ArrayContentProvider());
		final MalcolmDetectorsTableLabelProvider labelProvider = new MalcolmDetectorsTableLabelProvider();
		if (canEnableDetectors) {
			((CheckboxTableViewer) detectorsTable).setCheckStateProvider(new MalcolmDetectorsTableCheckStateProvider());
			((CheckboxTableViewer) detectorsTable).addCheckStateListener(
					evt -> ((IMalcolmDetectorModel) evt.getElement()).setEnabled(evt.getChecked()));
		}

		for (DetectorTableColumn columnInfo : DETECTOR_TABLE_COLUMNS) {
			final TableViewerColumn column = new TableViewerColumn(detectorsTable, columnInfo.alignment);
			column.setLabelProvider(labelProvider);
			column.getColumn().setText(columnInfo.label);
			column.getColumn().setWidth(columnInfo.columnWidth);
			if (columnInfo.editable) {
				column.setEditingSupport(new MalcolmDetectorEditSupport(detectorsTable, columnInfo));
			}
		}

		detectorsTable.setInput(getModel().getDetectorModels());
	}

	private boolean canEnableDetectors() {
		try {
			final IMalcolmDevice malcolmDevice =
					(IMalcolmDevice) runnableDeviceService.<IMalcolmModel>getRunnableDevice(getModel().getName());
			return malcolmDevice.getVersion().isVersionOrAbove(MalcolmVersion.VERSION_4_2);
		} catch (ScanningException e) {
			logger.error("Could not get version of malcolm device: " + getModel().getName(), e);
			return false;
		}
	}

	private class MalcolmDetectorsTableLabelProvider extends ColumnLabelProvider {

		private DetectorTableColumn column;

		@Override
		public void update(ViewerCell cell) {
			column = DETECTOR_TABLE_COLUMNS[cell.getColumnIndex()];
			super.update(cell);
		}

		@Override
		public String getText(Object element) {
			final IMalcolmDetectorModel detectorModel = (IMalcolmDetectorModel) element;
			switch (column) {
				case ENABLED: return "";
				case DETECTOR_NAME: return detectorModel.getName();
				case FRAMES_PER_STEP: return Integer.toString(detectorModel.getFramesPerStep());
				case EXPOSURE_TIME: return String.format("%.3f", detectorModel.getExposureTime());
				case FRAME_TIME: return String.format("%.3f", getModel().getExposureTime() / detectorModel.getFramesPerStep());
				default: throw new IllegalArgumentException("Unknown column: " + column);
			}
		}

		@Override
		public String getToolTipText(Object element) {
			return super.getToolTipText(element); // TODO add tooltips
		}

	}

	private static class MalcolmDetectorsTableCheckStateProvider implements ICheckStateProvider {

		@Override
		public boolean isChecked(Object element) {
			return ((IMalcolmDetectorModel) element).isEnabled();
		}

		@Override
		public boolean isGrayed(Object element) {
			return false;
		}

	}

	private class MalcolmDetectorEditSupport extends EditingSupport {

		private final DetectorTableColumn column;

		public MalcolmDetectorEditSupport(TableViewer tableViewer, DetectorTableColumn column) {
			super(tableViewer);
			this.column = column;
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor((Table) getViewer().getControl());
		}

		@Override
		protected boolean canEdit(Object element) {
			return column.editable;
		}

		@Override
		protected Object getValue(Object element) {
			final IMalcolmDetectorModel detectorModel = (IMalcolmDetectorModel) element;
			switch (column) {
				case FRAMES_PER_STEP: return Integer.toString(detectorModel.getFramesPerStep());
				case EXPOSURE_TIME: return Double.toString(detectorModel.getExposureTime());
				default: throw new IllegalArgumentException("Column should not be editable: " + column);
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			final IMalcolmDetectorModel detectorModel = (IMalcolmDetectorModel) element;
			switch (column) {
				case FRAMES_PER_STEP:
					try {
						detectorModel.setFramesPerStep(Integer.parseInt((String) value));
					} catch (NumberFormatException e) {
						// value is not a valid integer, ignore
					}
					break;
				case EXPOSURE_TIME:
					try {
						detectorModel.setExposureTime(Double.parseDouble((String) value));
					} catch (NumberFormatException e) {
						// value is not a valid double, ignore
					}
					break;
				default:
					throw new IllegalArgumentException("Column should not be editable: " + column);
			}
			detectorsTable.update(detectorModel, null);
		}
	}

}
