/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.sample.plate.management.ui.edit;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.sample.plate.management.ui.models.ScanModel;

public class ScanNameEditingSupport extends EditingSupport {
	private static final Logger logger = LoggerFactory.getLogger(ScanNameEditingSupport.class);

	private final TableViewer viewer;

	private final CellEditor editor;

	public ScanNameEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
		this.editor = new TextCellEditor(viewer.getTable()) {
			@Override
			public void activate(ColumnViewerEditorActivationEvent activationEvent) {
				logger.debug(String.valueOf(activationEvent.eventType));
				if (activationEvent.eventType == ColumnViewerEditorActivationEvent.MOUSE_CLICK_SELECTION
						&& ((MouseEvent) activationEvent.sourceEvent).button != 3) {
					activationEvent.cancel = true;
				}
			}
		};
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return editor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		return ((ScanModel) element).getName();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((ScanModel) element).setName(String.valueOf(value));
        viewer.update(element, null);
	}
}
