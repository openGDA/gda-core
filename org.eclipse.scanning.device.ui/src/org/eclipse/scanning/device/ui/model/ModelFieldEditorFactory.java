/*-
 *******************************************************************************
 * Copyright (c) 2011, 2016 Diamond Light Source Ltd.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Gerring - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.scanning.device.ui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
import org.eclipse.richbeans.widgets.cell.CComboWithEntryCellEditor;
import org.eclipse.richbeans.widgets.cell.CComboWithEntryCellEditorData;
import org.eclipse.richbeans.widgets.cell.NumberCellEditor;
import org.eclipse.richbeans.widgets.file.FileDialogCellEditor;
import org.eclipse.scanning.api.annotation.ui.DeviceType;
import org.eclipse.scanning.api.annotation.ui.FieldDescriptor;
import org.eclipse.scanning.api.annotation.ui.FieldValue;
import org.eclipse.scanning.api.annotation.ui.FileType;
import org.eclipse.scanning.api.device.IRunnableDeviceService;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.scanning.api.event.scan.DeviceInformation;
import org.eclipse.scanning.api.filter.IFilterService;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.scanning.device.ui.util.PageUtil;
import org.eclipse.scanning.device.ui.util.SortNatural;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Factory for creating editors for FieldValue
 *
 * @author Matthew Gerring
 *
 */
public class ModelFieldEditorFactory {

	private static final Logger logger = LoggerFactory.getLogger(ModelFieldEditorFactory.class);

	private static ISelectionListener selectionListener;
	private static ToolTip currentHint;
	private IScannableDeviceService cservice;
	private IRunnableDeviceService dservice;

	public ModelFieldEditorFactory() {
		try {
			cservice = ServiceHolder.getRemote(IScannableDeviceService.class);
			dservice = ServiceHolder.getRemote(IRunnableDeviceService.class);
		} catch (Exception e) {
			logger.error("Cannot get remote services!", e);
		}
	}

	/**
	 * Create a new editor for a field.
	 * @param field
	 *
	 * @return null if the field is not editable.
	 */
	public CellEditor createEditor(FieldValue field, Composite parent) {

		Object value;
		try {
			value = field.get();
		} catch (Exception e) {
			logger.error("Error creating CellEditor", e);
			return null;
		}

		Class<? extends Object> clazz = null;
		if (value != null) {
			clazz = value.getClass();
		} else {
			try {
				clazz = field.getType();
			} catch (NoSuchFieldException | SecurityException e) {
				logger.error("Error creating CellEditor", e);
			}
		}

		final FieldDescriptor anot = field.getAnnotation();
		if (!isEnabled(anot))
			return null;

		final CellEditor ed;
		if (clazz == Boolean.class) {
			ed = new CheckboxCellEditor(parent, SWT.NONE);

		} else if (Number.class.isAssignableFrom(clazz) || isNumberArray(clazz)) {
			ed = getNumberEditor(field, clazz, parent);

		} else if (IROI.class.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Have not ported RegionCellEditor to scanning yet!");
			// TODO FIXME Need way of editing regions.
			// ed = new RegionCellEditor(parent);

		} else if (Enum.class.isAssignableFrom(clazz)) {
			ed = getChoiceEditor((Class<? extends Enum>) clazz, parent);

		} else if (CComboWithEntryCellEditorData.class.isAssignableFrom(clazz)) {
			ed = getChoiceWithEntryEditor((CComboWithEntryCellEditorData) value, parent);

		} else if (FileDialogCellEditor.isEditorFor(clazz) || (anot != null && anot.file() != FileType.NONE)) {
			final FileDialogCellEditor fe = new FileDialogCellEditor(parent);
			fe.setValueClass(clazz);
			ed = fe;
			if (anot != null) {
				fe.setDirectory(anot.file().isDirectory());
				fe.setNewFile(anot.file().isNewFile());
			}

		} else if (String.class.equals(clazz)) {
			ed = getSimpleTextEditor(parent);

		} else {
			ed = null;
		}

		// Show the tooltip, if there is one
		if (ed != null && anot != null) {
			final String hint = anot.hint();
			if (hint != null && !hint.isEmpty()) {
				showHint(hint, parent);
			}
		}

		return ed;
	}

	private CellEditor getSimpleTextEditor(Composite parent) {
		return new TextCellEditor(parent) {
			@Override
			protected void doSetValue(Object value) {
				final String string = value != null ? value.toString() : "";
				super.doSetValue(string);
			}
		};
	}

	public CellEditor getDeviceEditor(DeviceType deviceType, Composite parent) throws ScanningException {

		final List<String> items;
		if (deviceType == DeviceType.SCANNABLE) {
			items = IFilterService.DEFAULT.filter("org.eclipse.scanning.scannableFilter", cservice.getScannableNames());
		} else if (deviceType == DeviceType.RUNNABLE) {
			final Collection<DeviceInformation<?>> infos = dservice.getDeviceInformation();
			final List<String> names = new ArrayList<String>(infos.size());
			infos.forEach(info -> {
				if (info.getDeviceRole().isDetector())
					names.add(info.getName());
			});
			items = IFilterService.DEFAULT.filter("org.eclipse.scanning.detectorFilter", names);
		} else {
			throw new ScanningException("Unrecognised device " + deviceType);
		}

		if (items != null) {
			final List<String> sorted = new ArrayList<>(items);
			Collections.sort(sorted, new SortNatural<>(false));
			final String[] finalItems = sorted.toArray(new String[sorted.size()]);

			return new CComboCellEditor(parent, finalItems) {
				private Object lastValue;

				@Override
				protected void doSetValue(Object value) {
					if (value instanceof Integer) {
						value = finalItems[((Integer) value).intValue()];
					}
					lastValue = value;
					super.doSetValue(value);
				}

				@Override
				protected Object doGetValue() {
					try {
						final Integer ordinal = (Integer) super.doGetValue();
						return finalItems[ordinal];
					} catch (IndexOutOfBoundsException ne) {
						return lastValue;
					}
				}
			};
		} else {
			return new TextCellEditor(parent) {
				@Override
				protected void doSetValue(Object value) {
					final String string = value != null ? value.toString() : "";
					super.doSetValue(string);
				}
			};
		}
	}

	public static boolean isEnabled(FieldDescriptor anot) {
		return anot == null || anot.editable();
	}

	private static void showHint(final String hint, final Composite parent) {
		if (parent == null || parent.isDisposed()) {
			return;
		}

		parent.getDisplay().asyncExec(() -> {
			currentHint = new DefaultToolTip(parent, ToolTip.NO_RECREATE, true);
			((DefaultToolTip) currentHint).setText(hint);
			currentHint.setHideOnMouseDown(true);
			currentHint.show(new Point(0, parent.getSize().y));

			if (selectionListener == null && PageUtil.getPage() != null) {
				selectionListener = (part, selection) -> {
					if (currentHint != null) {
						currentHint.hide();
					}
				};

				PageUtil.getPage().addSelectionListener(selectionListener);
			}
		});
	}

	private static boolean isNumberArray(Class<? extends Object> clazz) {
		if (clazz == null) {
			return false;
		}
		if (!clazz.isArray()) {
			return false;
		}

		return double[].class.isAssignableFrom(clazz) || float[].class.isAssignableFrom(clazz)
				|| int[].class.isAssignableFrom(clazz) || long[].class.isAssignableFrom(clazz);
	}

	@SuppressWarnings("rawtypes")
	private static CellEditor getChoiceEditor(final Class<? extends Enum> clazz, Composite parent) {

		final Enum[] values = clazz.getEnumConstants();
		final String[] items = Arrays.toString(values).replaceAll("^.|.$", "").split(", ");

		return new CComboCellEditor(parent, items) {
			@Override
			protected void doSetValue(Object value) {
				if (value instanceof Enum) {
					value = ((Enum) value).ordinal();
				}
				super.doSetValue(value);
			}

			@Override
			protected Object doGetValue() {
				final Integer ordinal = (Integer) super.doGetValue();
				return values[ordinal];
			}
		};
	}

	private static CellEditor getChoiceWithEntryEditor(final CComboWithEntryCellEditorData data, Composite parent) {

		final String[] items = data.getItems();

		return new CComboWithEntryCellEditor(parent, items) {
			@Override
			protected void doSetValue(Object value) {
				super.doSetValue(((CComboWithEntryCellEditorData) value).getActiveItem());
			}

			@Override
			protected Object doGetValue() {
				return new CComboWithEntryCellEditorData(data, (String) super.doGetValue());
			}
		};
	}

	private CellEditor getNumberEditor(FieldValue field, final Class<? extends Object> clazz, Composite parent) {

		final FieldDescriptor anot = field.getAnnotation();
		final NumberCellEditor textEd;
		if (anot != null) {
			textEd = new NumberCellEditor(parent, clazz, getMinimum(anot), getMaximum(anot), getUnit(anot), SWT.NONE);

			if (anot.numberFormat() != null && !anot.numberFormat().isEmpty()) {
				textEd.setDecimalFormat(anot.numberFormat());
			}

		} else {
			textEd = new NumberCellEditor(parent, clazz, SWT.NONE);
		}

		return textEd;
	}

	private String getUnit(FieldDescriptor anot) {
		if (anot.unit().length() > 0) {
			return anot.unit();
		}
		return null;
	}

	private Number getMinimum(FieldDescriptor anot) {
		if (!Double.isInfinite(anot.minimum())) {
			return anot.minimum();
		}
		return null;
	}

	private Number getMaximum(FieldDescriptor anot) {
		if (!Double.isInfinite(anot.maximum())) {
			return anot.maximum();
		}
		return null;
	}
}
