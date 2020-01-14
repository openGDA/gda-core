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

import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.device.models.IMalcolmModel;
import org.eclipse.scanning.api.malcolm.IMalcolmDevice;
import org.eclipse.scanning.api.malcolm.MalcolmVersion;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.AbstractModelEditor;
import org.eclipse.scanning.device.ui.Activator;
import org.eclipse.scanning.device.ui.util.ViewerUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MalcolmModelEditor extends AbstractModelEditor<IMalcolmModel> {

	private enum DetectorTableColumn {

		ENABLED("Enabled", false, false, 60, SWT.CENTER, "Whether to enable this detector"),

		DETECTOR_NAME("Name", false, false, 150, SWT.LEAD, "The name of the detector") {
			@Override
			public String getLabel(IMalcolmDetectorModel model) {
				return model.getName();
			}
		},

		FRAMES_PER_STEP("Frames per step", true, true, 120, SWT.CENTER, "The number of frames recorded by this detector per scan point\n requested (actual)") {
			@Override
			public String getLabel(IMalcolmDetectorModel model) {
				return Integer.toString(model.getFramesPerStep());
			}
		},

		EXPOSURE_TIME("Exposure time", true, true, 110, SWT.CENTER, "The exposure time for this detector for each frame.\nrequested (actual)") {
			@Override
			public String getLabel(IMalcolmDetectorModel model) {
				return String.format("%.3f", model.getExposureTime());
			}
		},

		FRAME_TIME("Frame time", false, true, 100, SWT.CENTER, "The time for each frame of this scan.\nrequested (actual).\nThis field is not editable. Calculated as: exposure time / frames per step") {
			@Override
			public String getLabel(IMalcolmDetectorModel detectorModel, IMalcolmModel malcolmModel) {
				// calculated as step-time / frames-per-step
				return String.format("%.3f", malcolmModel.getExposureTime() / detectorModel.getFramesPerStep());
			}
		},

		WAIT_TIME("Wait time", false, true, 100, SWT.CENTER, "The time between exposures.\nrequested (actual)") {
			@Override
			public String getLabel(IMalcolmDetectorModel detectorModel, IMalcolmModel malcolmModel) {
				final double frameTime = malcolmModel.getExposureTime() / detectorModel.getFramesPerStep();
				final double waitTime = frameTime - detectorModel.getExposureTime();
				return String.format("%.3f", waitTime);
			}
		};

		public final String label;
		public final boolean editable;
		public final boolean showActualValue;
		public final int columnWidth;
		public final int alignment;
		public final String toolTipText;

		private DetectorTableColumn(String label, boolean editable, boolean showActualValue,
				int columnWidth, int alignment, String toolTipText) {
			this.label = label;
			this.editable = editable;
			this.showActualValue = showActualValue;
			this.columnWidth = columnWidth;
			this.alignment = alignment;
			this.toolTipText = toolTipText;
		}

		@SuppressWarnings("unused")
		public String getLabel(IMalcolmDetectorModel detectorModel) {
			return null;
		}

		@SuppressWarnings("unused")
		public String getLabel(IMalcolmDetectorModel detectorModel, IMalcolmModel malcolmModel) {
			return getLabel(detectorModel);
		}

	}

	private static final DetectorTableColumn[] DETECTOR_TABLE_COLUMNS = DetectorTableColumn.values();

	private static final Logger logger = LoggerFactory.getLogger(MalcolmModelEditor.class);

	private final DataBindingContext dataBindingContext = new DataBindingContext();

	private final IRunnableDeviceService runnableDeviceService;

	private TableViewer detectorsTable;

	private IMalcolmModel modifiedModel = null;

	public MalcolmModelEditor(IRunnableDeviceService runnableDeviceService, IMalcolmModel model) {
		this.runnableDeviceService = runnableDeviceService;
		setModel(model);
	}

	@Override
	public Composite createEditorPart(Composite parent) {
		final Composite composite = super.createEditorPart(parent);

		createStepTimeSection(composite);
		createDiagram(composite);
		createDetectorsTable(composite);

		return composite;
	}

	@Override
	public void dispose() {
		super.dispose();
		dataBindingContext.dispose();
	}

	private void createStepTimeSection(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Step time:");
		GridDataFactory.swtDefaults().applyTo(label);

		final Text stepTimeText = new Text(composite, SWT.BORDER);
		stepTimeText.setToolTipText("The overall time for each frame of the scan");
		GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(stepTimeText);
		IObservableValue<Double> textFieldValue = WidgetProperties.text(SWT.Modify).observe(stepTimeText);
		IObservableValue<Double> modelValue = PojoProperties.value("exposureTime").observe(getModel());
		// recalculate the derived values for the detectors when the step time changes
		modelValue.addValueChangeListener(event -> updateValidatedModel(null));
		dataBindingContext.bindValue(textFieldValue, modelValue);
		dataBindingContext.updateTargets();
	}

	private void createDiagram(final Composite parent) {
		// TODO: this diagram is quite large and leaves some blank space to the left of it. Can this be fixed?
		final Label diagramLabel = new Label(parent, SWT.NONE);
		final Image image = Activator.getImage("icons/detector-sync.png");
		diagramLabel.setImage(image);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(diagramLabel);
	}

	private void createDetectorsTable(final Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

		final Label label = new Label(composite, SWT.NONE);
		label.setText("Detectors:");
		GridDataFactory.swtDefaults().applyTo(label);

		createDetectorsTableExplainerLabel(composite);

		final boolean canEnableDetectors = canEnableDetectors();
		final Table table = new Table(composite, (canEnableDetectors ? SWT.CHECK : 0) | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		detectorsTable = canEnableDetectors ? new CheckboxTableViewer(table) : new TableViewer(table);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).hint(SWT.DEFAULT, 150).applyTo(table);

		detectorsTable = new CheckboxTableViewer(table);
		table.setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(detectorsTable);

		// setup the context provider, label/checkstate provider and check state listener
		detectorsTable.setContentProvider(new ArrayContentProvider());
		if (canEnableDetectors) {
			((CheckboxTableViewer) detectorsTable).setCheckStateProvider(ViewerUtils.createCheckStateProvider(IMalcolmDetectorModel::isEnabled));
			((CheckboxTableViewer) detectorsTable).addCheckStateListener(
					evt -> ((IMalcolmDetectorModel) evt.getElement()).setEnabled(evt.getChecked()));
		}

		for (DetectorTableColumn columnInfo : DETECTOR_TABLE_COLUMNS) {
			if (!canEnableDetectors && columnInfo == DetectorTableColumn.ENABLED) continue;

			final TableViewerColumn column = new TableViewerColumn(detectorsTable, columnInfo.alignment);
			column.setLabelProvider(new DelegatingStyledCellLabelProvider(new MalcolmDetectorsTableLabelProvider(columnInfo)));
			column.getColumn().setText(columnInfo.label);
			column.getColumn().setWidth(columnInfo.columnWidth);
			if (columnInfo.editable) {
				column.setEditingSupport(new MalcolmDetectorEditSupport(detectorsTable, columnInfo));
			}
		}

		detectorsTable.setInput(getModel().getDetectorModels());
	}

	private void createDetectorsTableExplainerLabel(final Composite parent) {
		final StyledText explainerLabel = new StyledText(parent, SWT.NONE);
		explainerLabel.setBackground(parent.getBackground());
		GridDataFactory.swtDefaults().indent(20, 0).applyTo(explainerLabel);

		final StyleRange[] styleRanges = new StyleRange[2];
		final StringBuilder sb = new StringBuilder("exposure time 0 = use maximum, "); // TODO use a checkbox for maximum?
		styleRanges[0] = new StyleRange(sb.length(), 5, JFaceResources.getColorRegistry().get(JFacePreferences.COUNTER_COLOR), null);
		sb.append("(0.1) = actual value, ");
		styleRanges[1] = new StyleRange(sb.length(), 4, JFaceResources.getColorRegistry().get(JFacePreferences.QUALIFIER_COLOR), null);
		sb.append("grey = derived value");
		explainerLabel.setText(sb.toString());
		explainerLabel.setStyleRanges(styleRanges);
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

	public void updateValidatedModel(IMalcolmModel modifiedModel) {
		this.modifiedModel = modifiedModel;
		detectorsTable.refresh();
	}

	protected Optional<IMalcolmDetectorModel> getModifiedDetectorModel(String detectorName) {
		if (modifiedModel != null) {
			return modifiedModel.getDetectorModels().stream()
					.filter(det -> det.getName().equals(detectorName))
					.findFirst();
		}
		return Optional.empty();
	}

	private class MalcolmDetectorsTableLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

		private final DetectorTableColumn column;

		public MalcolmDetectorsTableLabelProvider(DetectorTableColumn column) {
			this.column = column;
		}

		@Override
		public StyledString getStyledText(Object element) {
			final IMalcolmDetectorModel detectorModel = (IMalcolmDetectorModel) element;
			final Optional<IMalcolmDetectorModel> validatedModel = getModifiedDetectorModel(detectorModel.getName());
			final String requestedValue = column.getLabel(detectorModel, getModel());
			final StyledString result = new StyledString();
			if (requestedValue != null) {
				final boolean derived = !column.editable && column != DetectorTableColumn.DETECTOR_NAME;
				result.append(requestedValue, derived ? StyledString.DECORATIONS_STYLER : null);
			}
			if (column.showActualValue && validatedModel.isPresent()) {
				result.append(" (" + column.getLabel(validatedModel.get(), modifiedModel) + ")",
						column.editable ? StyledString.COUNTER_STYLER : StyledString.DECORATIONS_STYLER);
			}
			return result;
		}

		@Override
		public String getToolTipText(Object element) {
			return column.toolTipText;
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
			final Object oldValue = getValue(element);

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
			if (!value.equals(oldValue)) {
				detectorsTable.getControl().getDisplay().asyncExec(() -> updateValidatedModel(null));
			}
		}

	}

}
