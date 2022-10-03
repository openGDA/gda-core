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
package org.eclipse.scanning.device.ui.device;

import java.util.Arrays;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.richbeans.widgets.cell.CComboCellEditor;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement;
import org.eclipse.scanning.api.scan.ui.MonitorScanUIElement.MonitorScanRole;
import org.eclipse.swt.widgets.Composite;

public class MonitorScanRoleEditingSupport extends EditingSupport {

	public MonitorScanRoleEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected CellEditor getCellEditor(Object element) {

		final MonitorScanRole[] roles = MonitorScanRole.values();
		final String[] roleLabels = Arrays.stream(roles).map(MonitorScanRole::getLabel).toArray(String[]::new);

		return new CComboCellEditor((Composite) getViewer().getControl(), roleLabels) {
			@Override
			protected void doSetValue(Object value) {
				final int index = ((MonitorScanRole) value).ordinal();
				super.doSetValue(index);
			}

			@Override
			protected Object doGetValue() {
				final Integer ordinal = (Integer) super.doGetValue();
				try {
					return roles[ordinal];
				} catch (IndexOutOfBoundsException ne) {
					return roles[0];
				}
			}
		};
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((MonitorScanUIElement) element).getMonitorScanRole();
	}

	@Override
	protected void setValue(Object element, Object value) {
		final MonitorScanUIElement wrapper = (MonitorScanUIElement) element;
		final MonitorScanRole scanRole = (MonitorScanRole) value;
		wrapper.setMonitorScanRole(scanRole);
		getViewer().refresh(element);
	}

}
